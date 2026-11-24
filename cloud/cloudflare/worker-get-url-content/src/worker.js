// ===== 辅助函数：解码 HTML Entities =====
function decodeHtmlEntities(str) {
	if (!str || typeof str !== "string") return "";
	return str
		.replace(/&#x([0-9a-fA-F]+);/gi, (_, hex) => {
			try {
				return String.fromCodePoint(parseInt(hex, 16));
			} catch {
				return _;
			}
		})
		.replace(/&#(\d+);/g, (_, dec) => {
			try {
				return String.fromCodePoint(parseInt(dec, 10));
			} catch {
				return _;
			}
		})
		.replace(/&quot;/gi, '"')
		.replace(/&apos;/gi, "'")
		.replace(/&lt;/gi, '<')
		.replace(/&gt;/gi, '>')
		.replace(/&nbsp;/gi, ' ')
		.replace(/&amp;/gi, '&');
}

// ===== 辅助函数：URL 正规化 =====
function normalizeUrl(imgUrl, pageUrl) {
	if (!imgUrl) return "";
	imgUrl = decodeHtmlEntities(imgUrl);
	let resultUrl = imgUrl;
	try {
		if (imgUrl.startsWith("//")) {
			resultUrl = "https:" + imgUrl;
		} else {
			resultUrl = new URL(imgUrl, pageUrl).href;
		}
	} catch (e) {
		resultUrl = imgUrl;
	}

	// 限定 facebook.com：拿掉 imageUrl 中 ? 後面的字串
	try {
		const pageHost = pageUrl ? new URL(pageUrl).hostname.toLowerCase() : "";
		if (pageHost === "facebook.com" || pageHost.endsWith(".facebook.com") || pageHost === "facebed.com" || pageHost.endsWith(".facebed.com")) {
			resultUrl = resultUrl.split("?")[0];
		}
	} catch (e) {}

	return resultUrl;
}

// ===== 辅助函数：依据图片网址副档名取得 Content-Type =====
function getImageContentType(url) {
	if (!url) return "";
	const ext = url.split('.').pop()?.split(/[?#]/)[0]?.toLowerCase();
	switch (ext) {
		case "jpg":
		case "jpeg":
			return "image/jpeg";
		case "png":
			return "image/png";
		case "gif":
			return "image/gif";
		case "webp":
			return "image/webp";
		case "svg":
			return "image/svg+xml";
		case "avif":
			return "image/avif";
		case "bmp":
			return "image/bmp";
		case "mp4":
			return "video/mp4";
		default:
			return "image/jpeg";
	}
}

// ===== 辅助函数：解析 JSON-LD =====
function parseJsonLdFromHtml(html) {
	let result = null;
	const regex = /<script[^>]*type=["']application\/ld\+json["'][^>]*>([\s\S]*?)<\/script>/gi;
	let match;
	const supportedTypes = [
		'Article', 'NewsArticle', 'BlogPosting', 'WebPage',
		'VideoObject', 'ImageObject', 'WebSite', 'ProfilePage', 'CollectionPage'
	];

	const processData = (item) => {
		if (!item || typeof item !== 'object') return;
		const type = item["@type"];
		if (supportedTypes.includes(type)) {
			if (!result) result = {};
			if (item.headline && !result.headline) result.headline = item.headline;
			if (item.description && !result.description) result.description = item.description;

			let img = item.image;
			if (Array.isArray(img) && img.length > 0) img = img[0];
			if (img && typeof img === 'object') {
				img = img.url || img.contentUrl || "";
			}
			if (img && typeof img === 'string' && !result.image) result.image = img;
		}
	};

	while ((match = regex.exec(html)) !== null) {
		try {
			const data = JSON.parse(match[1]);
			if (data['@graph'] && Array.isArray(data['@graph'])) {
				data['@graph'].forEach(processData);
			} else if (Array.isArray(data)) {
				data.forEach(processData);
			} else {
				processData(data);
			}
		} catch (e) {}
	}
	return result;
}

export default {
	async fetch(request, env, ctx) {
		let fromUrl = "";
		let getFormData;
		try {
			getFormData = await request.formData();
			fromUrl = getFormData.get("url");
			if (!fromUrl) throw new Error("No url");
		} catch {
			return Response.json({
				title: "",
				desc: "",
				imageUrl: "",
				contentType: "",
				isMedia: false
			});
		}

		// 如果前端有傳來 title, desc, imageUrl 就直接存到資料庫，不用再爬一次
		const titleFromFront = getFormData.get("title") || "";
		const descFromFront = getFormData.get("description") || "";
		const imageUrlFromFront = getFormData.get("imageUrl") || "";
		const contentTypeFromFront = getFormData.get("contentType") || "";
		const forceRefresh = getFormData.get("refresh") === "true" || getFormData.get("noCache") === "true";

		// 連接 D1 資料庫
		const { DATABASE } = env;

		try {
			// 已經有 title 或 desc 或 imageUrl 就直接存到資料庫，不用再爬一次
			if (titleFromFront || descFromFront || imageUrlFromFront) {
				await DATABASE.prepare(`
					INSERT INTO urls VALUES (?, ?, ?, ?, ?)
					ON CONFLICT(url) DO UPDATE SET 
					title = excluded.title,
					desc = excluded.desc,
					imageUrl = excluded.imageUrl,
					contentType = excluded.contentType
				`).bind(fromUrl, titleFromFront, descFromFront, imageUrlFromFront, contentTypeFromFront).run();

				return Response.json({
					title: titleFromFront,
					desc: descFromFront,
					imageUrl: imageUrlFromFront,
					contentType: contentTypeFromFront,
					isMedia: contentTypeFromFront.startsWith("image/") || contentTypeFromFront.startsWith("video/") || contentTypeFromFront.startsWith("audio/")
				});
			}

			// 檢查是否有相同 URL 的資料 (除非指定 forceRefresh)
			if (!forceRefresh) {
				const stmt = DATABASE.prepare('SELECT * FROM urls WHERE url = ?').bind(fromUrl);
				const { results } = await stmt.all();
				if (results && results.length > 0) {
					let cachedContentType = results[0].contentType || "";
					let cachedImageUrl = results[0].imageUrl || "";
					cachedImageUrl = normalizeUrl(cachedImageUrl, fromUrl);

					// 如果是 meee 圖床，驗證快取的 imageUrl 是否為有效直連網址 (必須包含 i.meee.com.tw)
					const isMeeeUrl = fromUrl.includes("meee.com.tw") || fromUrl.includes("meee");
					if (isMeeeUrl) {
						if (!cachedImageUrl || !cachedImageUrl.includes("i.meee.com.tw")) {
							cachedImageUrl = ""; // 快取為無效/錯誤網址，強制重新爬取
						} else {
							cachedContentType = getImageContentType(cachedImageUrl);
						}
					}

					if (!isMeeeUrl || cachedImageUrl) {
						return Response.json({
							title: decodeHtmlEntities(results[0].title || ""),
							desc: isMeeeUrl ? "" : decodeHtmlEntities(results[0].desc || ""),
							imageUrl: cachedImageUrl,
							contentType: cachedContentType,
							isMedia: cachedContentType.startsWith("image/") || cachedContentType.startsWith("video/") || cachedContentType.startsWith("audio/")
						});
					}
				}
			}

			let myUrl = fromUrl;
			let urlStructure = new URL(myUrl);
			let isTwitter = false;
			let isYoutube = false;
			let isMeee = urlStructure.hostname.includes("meee");
			let title = "";
			let desc = "";
			let imageUrl = "";
			let contentType = "";
			let charset = "utf-8";
			let headers = {
				"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.0.0",
				"Accept-Language": "zh-TW,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6,zh-HK;q=0.5",
				"Accept-Charset": "utf-8",
			};

			const siteKeywords = ["instagram", "amazon", "kodakjerec"];
			for (let i = 0; i < siteKeywords.length; i++) {
				const keyword = siteKeywords[i];
				if (urlStructure.hostname.indexOf(keyword) > -1) {
					return Response.json({
						title,
						desc,
						imageUrl,
						contentType,
						isMedia: false
					});
				}
			}

			// youtube 轉址 oembed
			if (urlStructure.hostname.includes("youtube.com") || urlStructure.hostname.includes("youtu.be")) {
				myUrl = `https://www.youtube.com/oembed?url=${myUrl}`;
				isYoutube = true;
			}

			// twitter 轉址
			function isTwitterHost(hostname) {
				return (
					hostname === 'twitter.com' ||
					hostname.endsWith('.twitter.com') ||
					hostname === 'x.com' ||
					hostname.endsWith('.x.com')
				);
			}

			if (isTwitterHost(urlStructure.hostname)) {
				myUrl = myUrl.replace(urlStructure.hostname, "api.vxtwitter.com");
				isTwitter = true;
				headers["User-Agent"] = "PostmanRuntime/2.10.1";
			}

			// threads 轉址
			if (urlStructure.hostname === "threads.net" || urlStructure.hostname.endsWith(".threads.net") || 
				urlStructure.hostname === "threads.com" || urlStructure.hostname.endsWith(".threads.com")) {
				myUrl = myUrl.replace(urlStructure.hostname, "fixthreads.seria.moe");
				urlStructure = new URL(myUrl);
			}

			// facebook 轉址
			if (urlStructure.hostname === "facebook.com" || urlStructure.hostname.endsWith(".facebook.com") || 
				urlStructure.hostname === "m.facebook.com" || urlStructure.hostname.endsWith(".m.facebook.com")) {
				myUrl = myUrl.replace(urlStructure.hostname, "facebed.com");
				urlStructure = new URL(myUrl);
				headers["User-Agent"] = "PostmanRuntime/2.10.1";
			}
			
			// ptt 加變數
			if (urlStructure.hostname.indexOf("ptt") > -1) {
				headers.cookie = "over18=1";
			}

			// ===== HEAD Request 判斷是否為 Media =====
			if (!isTwitter && !isYoutube) {
				let headContentType = "";
				try {
					const headResp = await fetch(myUrl, {
						method: 'HEAD',
						headers,
						redirect: 'follow'
					});
					headContentType = headResp.headers.get('content-type') || '';
				} catch (e) {
					// Ignore HEAD failure
				}

				let isDirectMedia = headContentType.startsWith('image/') || 
									headContentType.startsWith('video/') || 
									headContentType.startsWith('audio/');
				
				if (isDirectMedia) {
					return Response.json({
						title: '',
						desc: '',
						imageUrl: myUrl,
						contentType: headContentType,
						isMedia: true
					});
				}
			}

			// ===== 發送 GET 請求並限制下載範圍 (256KB，meee 頁面包含龐大 CSS 故不限制) =====
			let responseFrom = await fetch(myUrl, {
				method: "GET",
				headers: (isTwitter || isYoutube || isMeee) ? headers : {
					...headers,
					"Range": "bytes=0-262144"
				},
				redirect: "follow"
			});
			contentType = responseFrom.headers.get("content-type") || "";
			// 指定 charset
			if (contentType.indexOf("charset=") > -1)
				charset = contentType.substring(contentType.indexOf("charset=") + 8).trim().replaceAll("'", "").replaceAll('"', "").toLowerCase();
			// 特例: shift jis
			if (charset === "windows-31j")
				charset = "shift_jis";
			if (responseFrom.headers.get("target")) {
				myUrl = responseFrom.headers.get("target");
				urlStructure = new URL(myUrl);
			}

			let isMedia = contentType.startsWith("image/") || contentType.startsWith("video/") || contentType.startsWith("audio/");

			if (isTwitter) {
				const html = await responseFrom.json();
				title = html.user_name + " @" + html.user_screen_name;
				desc = html.text;
				if (html.mediaURLs && html.mediaURLs.length > 0)
					imageUrl = html.mediaURLs[0];
			} else if (isYoutube) {
				try {
					const json = await responseFrom.json();
					title = json.title || "";
					desc = json.author_name || "";
					imageUrl = json.thumbnail_url || "";
				} catch (e) {
					// JSON 解析失敗時
				}
			} else {
				if (contentType.indexOf("text/html") > -1 || contentType.indexOf("application/xhtml+xml") > -1) {
					// 将HTML文本解码
					let decoder = new TextDecoder(charset);
					const html = await responseFrom.arrayBuffer();
					const htmlBuffer = decoder.decode(html);

					// 使用 Cloudflare 原生 HTMLRewriter 解析 HTML（0 外部依賴）
					let pageTitle = "";
					let metaMap = {};
					let metaCount = 0;
					let titleCount = 0;
					let hasBody = false;
					let iconUrl = "";
					let amazonImage = "";
					let pttImage = "";
					let currentTitle = "";

					const rewriter = new HTMLRewriter()
						.on("title", {
							element() {
								titleCount++;
								currentTitle = "";
							},
							text(chunk) {
								currentTitle += chunk.text;
								if (chunk.lastInTextNode && !pageTitle) {
									pageTitle = currentTitle.trim();
								}
							}
						})
						.on("meta", {
							element(el) {
								metaCount++;
								const name = el.getAttribute("name")?.toLowerCase();
								const property = el.getAttribute("property")?.toLowerCase();
								const content = el.getAttribute("content");
								if (content) {
									if (property && !metaMap[property]) metaMap[property] = content;
									if (name && !metaMap[name]) metaMap[name] = content;
								}
							}
						})
						.on("link", {
							element(el) {
								const rel = el.getAttribute("rel")?.toLowerCase() || "";
								const href = el.getAttribute("href");
								if (href && rel.includes("icon") && !iconUrl) {
									iconUrl = href;
								}
							}
						})
						.on("#landingImage", {
							element(el) {
								const src = el.getAttribute("src");
								if (src && !amazonImage) amazonImage = src;
							}
						})
						.on("div.richcontent img", {
							element(el) {
								const src = el.getAttribute("src");
								if (src && !pttImage) pttImage = src;
							}
						})
						.on("body", {
							element() {
								hasBody = true;
							}
						});

					await rewriter.transform(new Response(htmlBuffer)).text();

					// 计算 Head 大小
					const headMatch = htmlBuffer.match(/<head[^>]*>([\s\S]*?)<\/head>/i);
					const headSize = headMatch ? headMatch[1].length : 0;

					// 记录HTML大小和基本信息
					const parseMetrics = {
						htmlSize: htmlBuffer.length,
						charset: charset,
						hasBody: hasBody,
						headSize: headSize,
						metaTags: metaCount,
						titleTags: titleCount
					};

					// Parse JSON-LD
					const jsonLd = parseJsonLdFromHtml(htmlBuffer);

					// ===== 提取标题 =====
					title = decodeHtmlEntities(
						pageTitle || 
						metaMap["og:title"] || 
						metaMap["twitter:title"] || 
						(jsonLd && jsonLd.headline ? jsonLd.headline : "")
					);

					// ===== 提取描述 =====
					desc = decodeHtmlEntities(
						metaMap["description"] || 
						metaMap["og:description"] || 
						metaMap["twitter:description"] || 
						(jsonLd && jsonLd.description ? jsonLd.description : "")
					);

					// ===== 提取图片 =====
					const hostname = urlStructure.hostname;
					const siteImageExtractors = {
						"ptt": () => pttImage,
						"iherb": () => metaMap["og:images"] || "",
						"amazon": () => amazonImage || "",
						"meee": () => {
							// 1. 優先從 __NUXT_DATA__ 精準擷取上傳的圖片檔名（避免網址搜尋誤判）
							const nuxtMatch = htmlBuffer.match(/<script[^>]*id="__NUXT_DATA__"[^>]*>([\s\S]*?)<\/script>/i);
							if (nuxtMatch) {
								const fileMatch = nuxtMatch[1].match(/["']([a-zA-Z0-9_-]+\.(?:png|jpe?g|gif|webp|svg|avif|bmp|mp4))["']/i);
								if (fileMatch) {
									return `https://i.meee.com.tw/${fileMatch[1]}`;
								}
							}

							// 2. 備案：從網址 ID 搜尋帶有副檔名的檔案名稱
							let id = "";
							try {
								id = decodeURIComponent(urlStructure.pathname.replace(/^\/+|\/+$/g, '').split('/')[0]);
							} catch (e) {
								id = urlStructure.pathname.replace(/^\/+|\/+$/g, '').split('/')[0];
							}

							if (id) {
								const idMatch = htmlBuffer.match(new RegExp(`(?:["'\\\\/]|\\b)(${id}\\.[a-zA-Z0-9]+)(?:["'\\\\/\\s,;]|\\b)`, 'i'));
								if (idMatch) {
									return `https://i.meee.com.tw/${idMatch[1]}`;
								}

								const looseMatch = htmlBuffer.match(new RegExp(`${id}\\.[a-zA-Z0-9]+`, 'i'));
								if (looseMatch) {
									return `https://i.meee.com.tw/${looseMatch[0]}`;
								}
							}

							return "";
						}
					};

					for (const [siteKeyword, extractor] of Object.entries(siteImageExtractors)) {
						if (hostname.indexOf(siteKeyword) > -1) {
							imageUrl = extractor();
							if (imageUrl) break;
						}
					}

					if (!imageUrl) {
						imageUrl = metaMap["og:image"] ||
								   metaMap["og:image:url"] ||
								   metaMap["twitter:image"] ||
								   metaMap["og:images"] ||
								   amazonImage ||
								   (jsonLd && jsonLd.image ? jsonLd.image : "") ||
								   iconUrl || "";
					}

					imageUrl = normalizeUrl(imageUrl, urlStructure.href);

					if (isMeee) {
						if (imageUrl && imageUrl.includes("i.meee.com.tw")) {
							contentType = getImageContentType(imageUrl);
							isMedia = true;
						} else {
							imageUrl = "";
						}
					}

					// Bilibili 特殊處理
					if (urlStructure.hostname.indexOf("bilibili") > -1) {
						const start = htmlBuffer.indexOf('window.__INITIAL_STATE__=');
						if (start > -1) {
							const end = htmlBuffer.indexOf(';(function()', start);
							if (end > start) {
								try {
									const jsonText = htmlBuffer.substring(start + 'window.__INITIAL_STATE__='.length, end);
									const state = JSON.parse(jsonText);
									if (state?.video?.viewInfo) {
										if (!desc) desc = state.video.viewInfo.desc || "";
										if (!imageUrl) imageUrl = normalizeUrl(state.video.viewInfo.pic, urlStructure.href);
									}
								} catch(e) {}
							}
						}
					}
					
					// 🎯 发送到 Analytics Engine
					const parseQuality = {
						isComplete: !!title && !!desc && !!imageUrl,
						isMissingTitle: !title,
						isMissingDesc: !desc,
						isMissingImage: !imageUrl,
						suspiciousEmpty: !title && !desc && !imageUrl,
						possiblyBlocked: htmlBuffer.length < 1000 && !title,
						likelyJavaScript: htmlBuffer.includes("<noscript>") || htmlBuffer.includes("javascript")
					};
					
					ctx.waitUntil(
						env.ANALYTICS?.writeDataPoint?.({
							indexes: ["getUrl"],
							blobs: [
								"parse_result",  // 事件类型
								urlStructure.hostname,  // 网站
								parseQuality.isComplete ? "complete" : "incomplete",  // 完整性
								parseQuality.possiblyBlocked ? "blocked" : "normal"  // 状态
							],
							doubles: [
								parseMetrics.htmlSize,
								title.length || 0,
								desc.length || 0,
								parseMetrics.metaTags,
								parseQuality.isComplete ? 1 : 0
							]
						})
					);

					// ✅ 只在解析失敗時保存 htmlBuffer 到数据库 (Limit to 500KB)
					if (!title && !desc && !imageUrl && htmlBuffer.length < 512000) {
						await DATABASE.prepare(`
							INSERT INTO urls_html VALUES (?, ?, ?)
							ON CONFLICT(url) DO UPDATE SET 
							htmlContent = excluded.htmlContent,
							createdAt = excluded.createdAt
						`).bind(fromUrl, htmlBuffer, new Date().toISOString()).run();
					}
				} else if (isMedia) {
					// 媒體檔案備案處理：使用路徑作為標題，用URL作為圖片 (防呆處理，如果 HEAD 請求失敗)
					title = urlStructure.pathname.split('/').pop() || urlStructure.pathname;
					imageUrl = myUrl;
					
					ctx.waitUntil(
						env.ANALYTICS?.writeDataPoint?.({
							indexes: ["getUrl"],
							blobs: ["non_html", contentType],
							doubles: [0]
						})
					);
				} else {
					title = urlStructure.pathname.split('/').pop() || urlStructure.pathname;
					
					ctx.waitUntil(
						env.ANALYTICS?.writeDataPoint?.({
							indexes: ["getUrl"],
							blobs: ["non_html", contentType],
							doubles: [0]
						})
					);
				}
			}

			if (title || desc || imageUrl) {
				await DATABASE.prepare(`
					INSERT INTO urls VALUES (?, ?, ?, ?, ?)
					ON CONFLICT(url) DO UPDATE SET 
					title = excluded.title,
					desc = excluded.desc,
					imageUrl = excluded.imageUrl,
					contentType = excluded.contentType
				`).bind(fromUrl, title, desc, imageUrl, contentType).run();
			}

			return Response.json({
				title: title || "",
				desc: desc || "",
				imageUrl: imageUrl || "",
				contentType: contentType || "",
				isMedia
			});
		} catch (e) {
			console.log(e);
			return Response.json({
				title: "",
				desc: "",
				imageUrl: "",
				contentType: "",
				isMedia: false
			});
		}
	}
};
