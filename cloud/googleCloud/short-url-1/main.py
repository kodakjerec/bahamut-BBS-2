import random
import flask
import functions_framework
from bs4 import BeautifulSoup

import requests
import math

@functions_framework.http
def short_url(request: flask.Request):
    # Replace with your actual API key
    api_key = getApiKey()

    headers = {
      'reurl-api-key': api_key,
      'Content-Type': 'application/json'
    }

    data = {
        'url': request.form['url']
    }

    try:
        # Send POST request to Rebrandly API to shorten the URL
        response = requests.post("https://api.reurl.cc/shorten", headers=headers, json=data)
        response.raise_for_status()  # Raise an exception for non-200 status codes

        response_json = response.json()
        if response_json.get("res") is None:
            return response_json, 200

        short_url = response_json["short_url"]

        # Fetch the HTML content from the shortened URL (optional)
        response2 = requests.get(short_url + "+", headers={'Content-type': 'text/html;charset=UTF-8'})
        html = response2.text if response2.status_code == 200 else None
        # 使用BeautifulSoup解析HTML
        soup = BeautifulSoup(html, "html.parser")
        description = soup.find('meta', attrs={'name':'description'}).get('content','')
        title = soup.find_all('span', {'class':'text-muted'})[0].text
        response_json['title'] = title
        response_json['description'] = description

        return response_json

    except requests.exceptions.RequestException as e:
        print(f"Error fetching short URL: {e}")
        return {}

# get api key
def getApiKey():
#   keys = ["4070ff49d794e63211533b663c974755ecd6b736959d04df8a38b58d65165567c4f5d6","4070ff49d794e63218563b663c974755ecd6b432909304df8a38b58d65165567c4f5d6"]
#   key = keys[math.floor(random.random()*2)]
  key = "4070ff49d794e63211533b663c974755ecd6b736959d04df8a38b58d65165567c4f5d6"
  return key