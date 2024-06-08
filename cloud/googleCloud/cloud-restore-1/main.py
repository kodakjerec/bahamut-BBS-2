import flask
import json
import functions_framework
from google.cloud import firestore

@functions_framework.http
def cloud_restore(request: flask.request):
    try:
      userId = request.form['userId']

      if not userId or not jsonData:
        return {"error": "No userId or data"}, 200

      db = firestore.Client(project="api-4721967000810992547-515520")
      cloud_ref = db.collection('bahamutCloud').document(userId)
      doc = cloud_ref.get()

      if doc.exists:
        data = doc.to_dict()
        return {"error": "", "jsonData": data.get("jsonData")}
      else:
        return {"error": "No Data"}
    except Exception as e:
      print(f"Error cloud backup: {e}")
      return {"error":"An error occured"}