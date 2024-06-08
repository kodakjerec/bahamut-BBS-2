const cheerio = require("cheerio")

export default {
  async fetch(request, env, ctx) {
    let url = ''
    try {
      url = (await request.formData()).get('url')
    } catch {
      return Response.json({'error': 'No url '})
    }

    // params
    let urlStructure = new URL(url)

    let isTwitter = false
    let title = ''
    let desc = ''
    let imageUrl = ''
    let contentType = ''
    let charset = 'utf-8'
    let headers = {
      "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.0.0",
      "Accept-Language": "zh-TW,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6,zh-HK;q=0.5",
      'Accept-Charset': 'utf-8'
    }

    // url check
    
    // don't need scrape
    const siteKeywords = ['facebook','instagram','threads','amazon','tinyurl.com','iherb.co']
    for(let i=0;i<siteKeywords.length;i++) {
      const keyword = siteKeywords[i]
      if (urlStructure.hostname.indexOf(keyword)>-1) {
        return Response.json({
          title,
          desc,
          imageUrl,
          contentType
        })
      }
    }
    
    // twitter
    const twitterKeywords = ['x.com', 'twitter']
    twitterKeywords.forEach(keyword => {
      if (urlStructure.hostname.indexOf(keyword)>-1) {
        url = url.replace(urlStructure.hostname, 'api.vxtwitter.com')
        isTwitter = true
      }
    })

    // ptt
    if (urlStructure.hostname.indexOf('ptt')>-1) {
      headers.cookie = 'over18=1'
    }

    // start
    try {
      const responseFrom = await fetch(url, {
        method: 'GET',
        headers: headers
      })
      
      // contentType
      contentType = responseFrom.headers.get('content-type')

      // charset
      charset = contentType.substring(contentType.indexOf('charset=')+8).trim().toLowerCase()
      if (charset==='windows-31j')
        charset = 'shift_jis'
      

      if (responseFrom.headers.get('target')) {
        url = responseFrom.headers.get('target')
        urlStructure = new URL(url)
      }
      if (isTwitter) {
        const html = await responseFrom.json()
        title = html.user_name + " @" + html.user_screen_name
        desc = html.text
        imageUrl = html.media_extended[0].thumbnail_url
      } else {
        if (contentType.indexOf('text')>-1) {
          let decoder = new TextDecoder(charset)
          const html = await responseFrom.arrayBuffer()
          const soup = cheerio.load(decoder.decode(html))

          // title
          if (soup('title').length>0)
            title = soup('title').text()
          else if (soup('meta[property="og:title"]').length>0)
            title = soup('meta[property="og:title"]').attr('content')

          // desc
          if (soup('meta[name="description"]').length>0)
            desc = soup('meta[name="description"]').attr('content')
          else if (soup('meta[property="og:description"]').length>0)
            desc = soup('meta[property="og:description"]').attr('content')

          // imageUrl
          if (soup('meta[property="og:image"]').length>0) {
            imageUrl = soup('meta[property="og:image"]').attr('content')
          } else {
            // ptt
            if (urlStructure.hostname.indexOf('ptt')>-1)
              if (soup('div.richcontent').length>0) {
                imageUrl = soup('div.richcontent>img').attr('src')
              }

            // iherb
            if (urlStructure.hostname.indexOf('iherb')>-1)
              imageUrl = soup('meta[property="og:images"]').attr('content')

            // amazon
            if (urlStructure.hostname.indexOf('amazon')>-1)
              imageUrl = soup('#landingImage').attr('src')
          }
        } else {
          // jpg, video, audio
          title = urlStructure.pathname
          imageUrl = url
        }
      }
    } catch (e) {
      console.log(e)
      return Response.json({'error': ' Something got error '})
    }

    if (title === null || title === undefined)
      title = ''
    if (desc === null || desc === undefined)
      desc = ''
    if (imageUrl === null || imageUrl === undefined)
      imageUrl = ''
    if (contentType === null || contentType === undefined)
      contentType = ''

    return Response.json({
      title,
      desc,
      imageUrl,
      contentType
    })
  },
};