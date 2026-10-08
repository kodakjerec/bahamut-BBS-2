const cheerio = require('cheerio');

// ===== 辅助函数：URL 正规化 =====
function normalizeUrl(imgUrl, pageUrl) {
	if (!imgUrl) return "";
	try {
		if (imgUrl.startsWith("//")) {
			return "https:" + imgUrl;
		}
		return new URL(imgUrl, pageUrl).href;
	} catch (e) {
		return imgUrl;
	}
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

			// 檢查是否有相同 URL 的資料
			const stmt = DATABASE.prepare('SELECT * FROM urls WHERE url = ?').bind(fromUrl);
			const { results } = await stmt.all();
			if (results && results.length > 0) {
				let cachedContentType = results[0].contentType || "";
				let cachedImageUrl = results[0].imageUrl || "";
				cachedImageUrl = normalizeUrl(cachedImageUrl, fromUrl);

				return Response.json({
					title: results[0].title || "",
					desc: results[0].desc || "",
					imageUrl: cachedImageUrl,
					contentType: cachedContentType,
					isMedia: cachedContentType.startsWith("image/") || cachedContentType.startsWith("video/") || cachedContentType.startsWith("audio/")
				});
			}

			let myUrl = fromUrl;
			let urlStructure = new URL(myUrl);
			let isTwitter = false;
			let isYoutube = false;
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

			// ===== 新增 HEAD Request 判斷是否為 Media =====
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

			// ===== 發送 GET 請求並限制下載範圍 (256KB) =====
			let responseFrom = await fetch(myUrl, {
				method: "GET",
				headers: (isTwitter || isYoutube) ? headers : {
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
					// 将HTML文本解码并解析
					let decoder = new TextDecoder(charset);
					const html = await responseFrom.arrayBuffer();
					const htmlBuffer = decoder.decode(html);
					const soup = cheerio.load(htmlBuffer);
					const originHtml = soup.html();
					
					// 记录HTML大小和基本信息
					const parseMetrics = {
						htmlSize: htmlBuffer.length,
						charset: charset,
						hasBody: soup("body").length > 0,
						headSize: soup("head").html()?.length || 0,
						metaTags: soup("meta").length,
						titleTags: soup("title").length
					};
					
					// Parse JSON-LD
					const jsonLd = parseJsonLd(soup);

					// ===== 提取标题 =====
					title = extractPageTitle(soup, jsonLd);
					
					// ===== 提取描述 =====
					desc = extractPageDescription(soup, jsonLd);
					
					// ===== 提取图片 =====
					imageUrl = extractPageImage(soup, originHtml, urlStructure, jsonLd);

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
						env.ANALYTICS.writeDataPoint({
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
						env.ANALYTICS.writeDataPoint({
							indexes: ["getUrl"],
							blobs: ["non_html", contentType],
							doubles: [0]
						})
					);
				} else {
					title = urlStructure.pathname.split('/').pop() || urlStructure.pathname;
					
					ctx.waitUntil(
						env.ANALYTICS.writeDataPoint({
							indexes: ["getUrl"],
							blobs: ["non_html", contentType],
							doubles: [0]
						})
					);
				}
			}
			
			// ===== 辅助函数：解析 JSON-LD =====
			function parseJsonLd(soup) {
				let result = null;
				soup('script[type="application/ld+json"]').each((i, el) => {
					try {
						const data = JSON.parse(soup(el).html());
						
						const supportedTypes = [
							'Article', 'NewsArticle', 'BlogPosting', 'WebPage',
							'VideoObject', 'ImageObject', 'WebSite', 'ProfilePage', 'CollectionPage'
						];

						const processData = (item) => {
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
						
						if (data['@graph'] && Array.isArray(data['@graph'])) {
							data['@graph'].forEach(processData);
						} else if (Array.isArray(data)) {
							data.forEach(processData);
						} else {
							processData(data);
						}
					} catch(e) {}
				});
				return result;
			}

			// ===== 辅助函数：提取页面标题 =====
			function extractPageTitle(soup, jsonLd) {
				if (soup("title").length > 0) return soup("title").text();
				if (soup('meta[property="og:title"]').length > 0) return soup('meta[property="og:title"]').attr("content");
				if (soup('meta[name="twitter:title"]').length > 0) return soup('meta[name="twitter:title"]').attr("content");
				if (jsonLd && jsonLd.headline) return jsonLd.headline;
				return "";
			}
			
			// ===== 辅助函数：提取页面描述 =====
			function extractPageDescription(soup, jsonLd) {
				if (soup('meta[name="description"]').length > 0) return soup('meta[name="description"]').attr("content");
				if (soup('meta[property="og:description"]').length > 0) return soup('meta[property="og:description"]').attr("content");
				if (soup('meta[name="twitter:description"]').length > 0) return soup('meta[name="twitter:description"]').attr("content");
				if (jsonLd && jsonLd.description) return jsonLd.description;
				return "";
			}
			
			// ===== 辅助函数：提取页面图片 =====
			function extractPageImage(soup, htmlContent, urlObj, jsonLd) {
				const hostname = urlObj.hostname;
				let img = "";
				
				const siteImageExtractors = {
					"ptt": () => {
						if (soup("div.richcontent").length > 0) {
							return soup("div.richcontent>img").attr("src") || "";
						}
						return "";
					},
					"iherb": () => soup('meta[property="og:images"]').attr("content") || "",
					"amazon": () => soup("#landingImage").attr("src") || "",
					"meee": () => {
						const findString = urlObj.pathname.replace("/", "") + ".";
						const findIndex = htmlContent.indexOf(findString);
						if (findIndex > -1) {
							const imageName = "/" + htmlContent.substring(findIndex, findIndex + findString.length + 3);
							return urlObj.href.replace(urlObj.pathname, imageName);
						}
						return "";
					}
				};
				
				for (const [siteKeyword, extractor] of Object.entries(siteImageExtractors)) {
					if (hostname.indexOf(siteKeyword) > -1) {
						img = extractor();
						if (img) break;
					}
				}
				
				if (!img) {
					img = soup('meta[property="og:image"]').attr("content") ||
						  soup('meta[property="og:image:url"]').attr("content") ||
						  soup('meta[name="twitter:image"]').attr("content") ||
						  soup('meta[property="og:images"]').attr("content") ||
						  soup("#landingImage").attr("src") ||
						  (jsonLd && jsonLd.image ? jsonLd.image : "") ||
						  soup('link[rel*="icon"]').first().attr("href") || "";
				}
				
				return normalizeUrl(img, urlObj.href);
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