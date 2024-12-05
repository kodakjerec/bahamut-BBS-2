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

with open("stat.txt", "r") as txtStatFile:
    txtStat = "\n".join(txtStatFile.readlines())

with open("post_info.txt", "r") as infoFile:
    postPHP = infoFile.readline().strip()
    brd = infoFile.readline().strip()
    viapost = infoFile.readline().strip()
    userid = infoFile.readline().strip()
    stamp = infoFile.readline().strip()

session = requests.Session()
session.cookies.load("baha_cookie.txt", ignore_discard=True)

if postPHP == "1" and brd == gBrdName:
    response = session.post(f"http://webbbs.gamer.com.tw/post.php?brd={brd}", data={
        'brd': brd,
        'atype': '',
        'title': f"{gDate} 洽文統計",
        'content': txtStat,
        'sign': 0,
        'viapost': viapost,
        'userid': userid,
        'stamp': stamp
    })
    print(response.text)
else:
    print("Failed to post!")

