import flask
import functions_framework
from google.cloud import firestore

@functions_framework.http
def get_imgur_token(request: flask.Request):
    db = firestore.Client(project="api-4721967000810992547-515520")

    # Reference the bahamutBBS collection and 'imgur_token' document
    bahamut_ref = db.collection('bahamutBBS').document('imgur_token')

    try:
      # Get the document
      doc = bahamut_ref.get()
      if doc.exists:
        data = doc.to_dict()
        return {"accessToken": data.get('accessToken')}
      else:
        return {"error": "imgur_token not found in Firestore"}
    except Exception as e:
        print(f"Error retrieving imgur_token: {e}")
        return {"error": "An error occurred"}
