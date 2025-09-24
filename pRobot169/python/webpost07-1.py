import sys
import os
import requests

gBrdName = sys.argv[1]
gDate = sys.argv[2]
file_uid_pass = sys.argv[3]

if not (gBrdName.isalnum() and gDate.count('/') == 1 and os.path.isfile(file_uid_pass) and os.path.isfile("stat.txt")):
    print(f"Usage: python {sys.argv[0]} boardname date uid_pass_file")
    print("    The form of parameter 'date' is 'mm/dd'.")
    print("    Please execute this program in a directory with 'stat.txt'.")
    sys.exit()

with open(file_uid_pass, 'r') as f:
    gUid, gPasswd = f.read().splitlines()
gUid = gUid.strip()
gPasswd = gPasswd.strip()[:8]

# --- login ---------------------------------------
url = 'https://user.gamer.com.tw/doLogin.php'
data = {
    'uid': gUid,
    'passwd': gPasswd
}
response = requests.post(url, data=data)

if response.ok:
    print(response.text)
else:
    print("Failed:", response.status_code, response.reason)

# =================================================

