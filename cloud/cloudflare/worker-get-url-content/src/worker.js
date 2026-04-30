const cheerio = require('cheerio')

export default {
	async fetch(request, env, ctx) {
		let url = "";
		const getFormData = await request.formData();

		try {
			url = getFormData.get("url");
		} catch {
			return Response.json({ "error": "No url " });
		}

		// 如果前端有傳來 title, desc, imageUrl 就直接存到資料庫，不用再爬一次
		const titleFromFront = getFormData.get("title") || "";
		const descFromFront = getFormData.get("description") || "";
		const imageUrlFromFront = getFormData.get("imageUrl") || "";
		const contentTypeFromFront = getFormData.get("contentType") || "";

		// 連接 D1 資料庫
		const { DATABASE } = env;

		try {
			// 已經有 title 或 desc 就直接存到資料庫，不用再爬一次
			if (titleFromFront || descFromFront) {
				// 修改儲存邏輯，使用 UPSERT 語法
				await DATABASE.prepare(`
					INSERT INTO urls VALUES (?, ?, ?, ?, ?)
					ON CONFLICT(url) DO UPDATE SET 
					title = excluded.title,
					desc = excluded.desc,
					imageUrl = excluded.imageUrl,
					contentType = excluded.contentType
				`).bind(url, titleFromFront, descFromFront, imageUrlFromFront, contentTypeFromFront).run();

				return Response.json({
					title: titleFromFront,
					desc: descFromFront,
					imageUrl: imageUrlFromFront,
					contentType: contentTypeFromFront
				});
			}

			// 檢查是否有相同 URL 的資料
			const stmt = DATABASE.prepare('SELECT * FROM urls WHERE url = ?').bind(url);
			const { results } = await stmt.all();
			if (results && results.length > 0) {
				return Response.json({
					title: results[0].title,
					desc: results[0].desc,
					imageUrl: results[0].imageUrl,
					contentType: results[0].contentType
				});
			}

			let urlStructure = new URL(url);
			let isTwitter = false;
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

			const siteKeywords = ["facebook", "instagram", "amazon", "threads", "youtu"];
			for (let i = 0; i < siteKeywords.length; i++) {
				const keyword = siteKeywords[i];
				if (urlStructure.hostname.indexOf(keyword) > -1) {
					return Response.json({
						title,
						desc,
						imageUrl,
						contentType
					});
				}
			}
			// twitter 轉址
			const twitterKeywords = ["x.com", "twitter"];
			twitterKeywords.forEach((keyword) => {
				if (urlStructure.hostname.indexOf(keyword) > -1) {
					url = url.replace(urlStructure.hostname, "api.vxtwitter.com");
					isTwitter = true;
				}
			});
			// ptt 加變數
			if (urlStructure.hostname.indexOf("ptt") > -1) {
				headers.cookie = "over18=1";
			}
			let responseFrom = await fetch(url, {
				method: "GET",
				headers,
				redirect: "follow"
			});
			contentType = responseFrom.headers.get("content-type");
			// 指定 charset
			if (contentType.indexOf("charset=") > -1)
				charset = contentType.substring(contentType.indexOf("charset=") + 8).trim().replaceAll("'", "").replaceAll('"', "").toLowerCase();
			// 特例: shift jis
			if (charset === "windows-31j")
				charset = "shift_jis";
			if (responseFrom.headers.get("target")) {
				url = responseFrom.headers.get("target");
				urlStructure = new URL(url);
			}
			if (isTwitter) {
				const html = await responseFrom.json();
				title = html.user_name + " @" + html.user_screen_name;
				desc = html.text;
				if (html.mediaURLs && html.mediaURLs.length > 0)
					imageUrl = html.mediaURLs[0];
			} else {
				if (contentType.indexOf("text") > -1) {
					// 将HTML文本解码并解析
					let decoder = new TextDecoder(charset);
					const html = await responseFrom.arrayBuffer();
					const soup = cheerio.load(decoder.decode(html));
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
					
					// ===== 提取标题 =====
					title = extractPageTitle(soup);
					
					// ===== 提取描述 =====
					desc = extractPageDescription(soup);
					
					// ===== 提取图片 =====
					imageUrl = extractPageImage(soup, originHtml, urlStructure);
					
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
							indexes: ["getUrl", "parse_result"],
							blobs: [
								url,
								urlStructure.hostname,
								parseQuality.isComplete ? "complete" : "incomplete",
								parseQuality.possiblyBlocked ? "blocked" : "normal"
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

				} else {
					// 非HTML文件：使用路径作为标题，用URL作为图片
					title = urlStructure.pathname;
					imageUrl = url;
					
					ctx.waitUntil(
						env.ANALYTICS.writeDataPoint({
							indexes: ["getUrl", "non_html"],
							blobs: [url, contentType],
							doubles: [0]
						})
					);
				}
			}
			
			// ===== 辅助函数：提取页面标题 =====
			function extractPageTitle(soup) {
				// 优先使用 <title> 标签
				if (soup("title").length > 0)
					return soup("title").text();
				
				// 其次使用 Open Graph 标签
				if (soup('meta[property="og:title"]').length > 0)
					return soup('meta[property="og:title"]').attr("content");
				
				return "";
			}
			
			// ===== 辅助函数：提取页面描述 =====
			function extractPageDescription(soup) {
				// 优先使用标准 meta description
				if (soup('meta[name="description"]').length > 0)
					return soup('meta[name="description"]').attr("content");
				
				// 其次使用 Open Graph description
				if (soup('meta[property="og:description"]').length > 0)
					return soup('meta[property="og:description"]').attr("content");
				
				return "";
			}
			
			// ===== 辅助函数：提取页面图片 =====
			function extractPageImage(soup, htmlContent, urlObj) {
				const hostname = urlObj.hostname;
				
				// 各网站的特殊处理规则
				const siteImageExtractors = {
					// PTT 论坛：从富文本区域提取
					"ptt": () => {
						if (soup("div.richcontent").length > 0) {
							return soup("div.richcontent>img").attr("src") || "";
						}
						return "";
					},
					
					// iHerb：从 og:images 元标签提取
					"iherb": () => {
						return soup('meta[property="og:images"]').attr("content") || "";
					},
					
					// 亚马逊：从特定ID的图片提取
					"amazon": () => {
						return soup("#landingImage").attr("src") || "";
					},
					
					// Meee：从HTML内容中查找特殊字符串
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
				
				// 查找匹配的网站规则
				for (const [siteKeyword, extractor] of Object.entries(siteImageExtractors)) {
					if (hostname.indexOf(siteKeyword) > -1) {
						const result = extractor();
						if (result) return result;
						break;
					}
				}
				
				// 若以上规则都无结果，使用通用的 Open Graph 图片标签
				return soup('meta[property="og:image"]').attr("content") || "";
			}

			// 修改儲存邏輯，使用 UPSERT 語法
			await DATABASE.prepare(`
					INSERT INTO urls VALUES (?, ?, ?, ?, ?)
					ON CONFLICT(url) DO UPDATE SET 
					title = excluded.title,
					desc = excluded.desc,
					imageUrl = excluded.imageUrl,
					contentType = excluded.contentType
				`).bind(url, title, desc, imageUrl, contentType).run();

			return Response.json({
				title,
				desc,
				imageUrl,
				contentType
			});
		} catch (e) {
			console.log(e);
			return Response.json({ "error": "發生錯誤\n" + e.toString() });
		}
	}
};