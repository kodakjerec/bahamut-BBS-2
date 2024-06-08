from urllib.request import urlopen, Request
from bs4 import BeautifulSoup
import flask
import functions_framework
from urllib.parse import urlparse

@functions_framework.http
def get_url_content(request: flask.Request):
    url = request.form.get('url', None)
    if url is None:
        return { "error": "No url" }, 200
    
    # params
    parsed_uri = urlparse(url)
    isTwitter = False
    title = ''
    desc = ''
    imageUrl = ''
    contentType = ''
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36 Edg/125.0.0.0"
    }
            
    # url check
    # twitter
    twitterKeywords = ['x.com', 'twitter']
    for keyword in twitterKeywords:
        if parsed_uri.netloc.find(keyword) > -1:
            url = url.replace(parsed_uri.netloc, 'api.vxtwitter.com')
            isTwitter = True

    # ptt
    if parsed_uri.netloc.find('ptt'):
        headers['cookie'] = 'over18=1'
    
    # start
    try:
        req = Request(url)
        req.headers = headers
        response =  urlopen(req)
        
        # 轉址後網址尋找
        parsed_uri = urlparse(response.url)
        contentType = response.headers['Content-Type']

        if isTwitter:
            html = response.read()
            result = flask.json.loads(html)
            title = result['user_name'] + ' @' + result['user_screen_name']
            desc = result['text']
            imageUrl = result['media_extended'][0]['thumbnail_url']
        else:
            # content-Type
            if contentType.find('text')>-1:
                html = response.read()
                soup = BeautifulSoup(html, "html.parser")

                title = soup.find('title')
                if title:
                    title = title.get_text()
                else:
                    title = soup.find('meta', attrs={'property': 'og:title'})
                    if title:
                        title = title.get('content', '')

                desc = soup.find('meta', attrs={'name': 'description'})
                if desc:
                    desc = desc.get('content', '')
                else:
                    desc = soup.find('meta', attrs={'property': 'og:description'})
                    if desc:
                        desc = desc.get('content', '')
                        
                imageUrl = soup.find('meta', attrs={'property': 'og:image'})
                if imageUrl:
                    imageUrl = imageUrl.get('content', '')
                else:
                    # ptt
                    if parsed_uri.netloc.find('ptt')>-1:
                        imageUrl = soup.find('div', class_='richcontent').find('img')
                        if imageUrl:
                            imageUrl = imageUrl['src']
                    # iherb
                    elif parsed_uri.netloc.find('iherb')>-1:
                        imageUrl = soup.find('meta', attrs={'property': 'og:images'})
                        if imageUrl:
                            imageUrl = imageUrl.get('content', '')
                    #amazon
                    elif parsed_uri.netloc.find('amazon')>-1:
                        imageUrl = soup.find('img', { 'id': 'landingImage'})
                        if imageUrl:
                            imageUrl = imageUrl['src']

            else:
                title = parsed_uri.path.split('/')[-1]
                imageUrl = url
    except Exception as e:
        print(e)
        pass

    # format
    if title is None:
        title = ''
    if desc is None:
        desc = ''
    if imageUrl is None:
        imageUrl = ''
    if contentType is None:
        contentType = ''

    return {
        'title': title,
        'desc': desc,
        'imageUrl': imageUrl,
        'contentType': contentType
    }
