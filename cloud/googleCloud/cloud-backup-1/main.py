import json
import flask
import functions_framework
from google.cloud import firestore

@functions_framework.http
def cloud_backup(request: flask.request):
    try:
      userId = request.form['userId']
      jsonData = request.form['jsonData']

      if not userId or not jsonData:
        return {"error": "No userId or data"}, 200

      db = firestore.Client(project="api-4721967000810992547-515520")
      cloud_ref = db.collection('bahamutCloud').document(userId)

      if cloud_ref.get().exists:
        cloud_ref.update(jsonData)
      else:
        cloud_ref.set(jsonData)
      return {"error": ""}, 200
    except Exception as e:
      print(f"Error cloud backup: {e}")
      return {"error":"An error occured"}