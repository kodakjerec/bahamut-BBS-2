import sys
import os
import re
import requests
from http.cookiejar import LWPCookieJar

def getInfoToPost(html):
    postPHP = 0
    brd = viapost = userid = stamp = None
    for line in html.splitlines():
        if re.search(r'^<form\s+name=postform\s+method=post\s+action="post\.php">', line):
            postPHP = 1
        if match := re.search(r'<input\s+type=hidden\s+name=brd\s+value="(\w+)">', line):
            brd = match.group(1)
        if match := re.search(r'<input\s+.*\s+name="viapost"\s+value="(\w+)">', line):
            viapost = match.group(1)
        if match := re.search(r'<input\s+.*\s+name="userid"\s+value="(\w+)">', line):
            userid = match.group(1)
        if match := re.search(r'<input\s+.*\s+name="stamp"\s+value=(\w+)>', line):
            stamp = match.group(1)
    return postPHP, brd, viapost, userid, stamp

gBrdName = sys.argv[1]
gDate = sys.argv[2]
file_uid_pass = sys.argv[3]

if not (re.match(r'^\w+$', gBrdName) and re.match(r'^\d\d/\d\d$', gDate) and 
        os.path.isfile(file_uid_pass) and os.path.isfile("stat.txt")):
    print(f"Usage: python {sys.argv[0]} boardname date uid_pass_file\n"
          "    The form of parameter 'date' is 'mm/dd'.\n"
          "    Please execute this program in a directory with 'stat.txt'.")
    sys.exit()

with open(file_uid_pass, 'r') as idpass_file:
    gUid, gPasswd = idpass_file.read().splitlines()
gUid = gUid.strip()
gPasswd = gPasswd.strip()[:8]

with open("stat.txt", 'r') as stat_file:
    txtStat = stat_file.read()

cookie_jar = LWPCookieJar("baha_cookie.txt")
cookie_jar.load(ignore_discard=True)

session = requests.Session()
session.cookies = cookie_jar

# --- get some information when doing post ----------- 
response = session.get(f"http://webbbs.gamer.com.tw/post.php?brd={gBrdName}")
if not response.ok:
    print("Failed: ", response.status_code)
    print(response.text)
    sys.exit()

postPHP, brd, viapost, userid, stamp = getInfoToPost(response.text)

with open("post_info.txt", 'w') as info_file:
    info_file.write("\n".join([postPHP, brd, viapost, userid, stamp]))

# ====================================================
sys.exit()
