import flask
import requests
import functions_framework
from google.cloud import firestore

@functions_framework.http
def refresh_imgur_token(request: flask.Request):
    db = firestore.Client(project="api-4721967000810992547-515520")

    # Reference the bahamutBBS collection and 'imgur_token' document
    bahamut_ref = db.collection('bahamutBBS').document('imgur_token')

    try:
      # Get the document
      doc = bahamut_ref.get()
      if doc.exists:
        data = doc.to_dict()
        clientId = data.get('clientId')
        clientSecret = data.get('clientSecret')
        accessToken = data.get('accessToken')
        refreshToken = data.get('refreshToken')

        # 打 api 取得accessToken和refreshToken
        form_data = {
          'refresh_token': refreshToken,
          'client_id': clientId,
          'client_secret': clientSecret,
          'grant_type': 'refresh_token'
        }
        response = requests.post('https://api.imgur.com/oauth2/token', data=form_data).json()
        print(response)
        if ('access_token' in response):
          newAccessToken = response['access_token']
          newRefreshToken = response['refresh_token']
          data.update({ 'accessToken': newAccessToken, 'refreshToken': newRefreshToken })
        return response
      else:
        return {"error": "imgur_token not found in Firestore"}
    except Exception as e:
        print(f"Error retrieving imgur_token: {e}")
        return {"error": "An error occurred"}
