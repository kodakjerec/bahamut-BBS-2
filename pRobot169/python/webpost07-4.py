import re
import os
import sys
import requests
from requests.packages.urllib3.exceptions import InsecureRequestWarning

# Suppress only the single InsecureRequestWarning from urllib3 needed for requests
requests.packages.urllib3.disable_warnings(InsecureRequestWarning)

gBrdName = sys.argv[1]
gDate = sys.argv[2]
file_uid_pass = sys.argv[3]

if not (re.match(r'^\w+$', gBrdName) and re.match(r'^\d\d/\d\d$', gDate) and os.path.isfile(file_uid_pass) and os.path.isfile("stat.txt")):
    print("""Usage: python {} boardname date uid_pass_file
    The form of parameter 'date' is 'mm/dd'.
    Please execute this program in a directory with 'stat.txt'.""".format(sys.argv[0]))
    sys.exit()

session = requests.Session()
session.cookies.load(ignore_discard=True, filename="./baha_cookie.txt")

# --- logout ----------------------------------------
req = requests.Request(
    method='HEAD',
    url='https://user.gamer.com.tw/logout.php'
)
prepped = session.prepare_request(req)
resp = session.send(prepped, verify=False)
print(resp.text)
# ===================================================
sys.exit()

