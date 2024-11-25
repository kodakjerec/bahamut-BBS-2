const cheerio = require('cheerio')

export default {
  async fetch(request, env, ctx) {
    let url = "";
    try {
      url = (await request.formData()).get("url");
    } catch {
      return Response.json({ "error": "No url " });
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
      "Accept-Charset": "utf-8"
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
    try {
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
          let decoder = new TextDecoder(charset);
          const html = await responseFrom.arrayBuffer();
          const soup = cheerio.load(decoder.decode(html));
          const originHtml = soup.html();
          if (soup("title").length > 0)
            title = soup("title").text();
          else if (soup('meta[property="og:title"]').length > 0)
            title = soup('meta[property="og:title"]').attr("content");
          if (soup('meta[name="description"]').length > 0)
            desc = soup('meta[name="description"]').attr("content");
          else if (soup('meta[property="og:description"]').length > 0)
            desc = soup('meta[property="og:description"]').attr("content");
          if (urlStructure.hostname.indexOf("ptt") > -1) {
            if (soup("div.richcontent").length > 0) {
              imageUrl = soup("div.richcontent>img").attr("src");
            }
          } else if (urlStructure.hostname.indexOf("iherb") > -1) {
            imageUrl = soup('meta[property="og:images"]').attr("content");
          } else if (urlStructure.hostname.indexOf("amazon") > -1) {
            imageUrl = soup("#landingImage").attr("src");
          } else if (urlStructure.hostname.indexOf("meee") > -1) {
            const findString = urlStructure.pathname.replace("/", "") + ".";
            if (originHtml.indexOf(findString) > -1) {
              const findIndex = originHtml.indexOf(findString);
              if (findIndex > -1) {
                const imageName = "/" + originHtml.substring(findIndex, findIndex + findString.length + 3);
                imageUrl = urlStructure.href.replace(urlStructure.pathname, imageName);
              }
            }
          }
          if (imageUrl.length === 0) {
            if (soup('meta[property="og:image"]').length > 0)
              imageUrl = soup('meta[property="og:image"]').attr("content");
          }
        } else {
          title = urlStructure.pathname;
          imageUrl = url;
        }
      }
    } catch (e) {
      console.log(e);
      return Response.json({ "error": " Something got error " });
    }
    if (title === null || title === void 0)
      title = "";
    if (desc === null || desc === void 0)
      desc = "";
    if (imageUrl === null || imageUrl === void 0)
      imageUrl = "";
    if (contentType === null || contentType === void 0)
      contentType = "";
    return Response.json({
      title,
      desc,
      imageUrl,
      contentType
    });
  }
};