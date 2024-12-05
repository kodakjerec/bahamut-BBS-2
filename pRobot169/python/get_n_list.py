import os
import re
import subprocess
import sys

def readPage(urlA, outfileBahaA):
    print(urlA)
    listArticle = dump1list(urlA)

    with open(outfileBahaA, "a") as output:
        output.write(listArticle)

def check_continue(files):
    num = {}
    for file in files:
        with open(file) as continue_file:
            for line in continue_file:
                if not re.match(r"^[> ][ 0-9]{5}", line):
                    continue
                num[line[1:6]] = 1

    b = a = sorted(num.keys())
    a.insert(0, 1)
    list_ = []
    for i in range(len(b)):
        if b[i] - a[i] > 1:
            list_.append(f"{a[i]}-{b[i]-1}")

    return list_

def preEnd(files):
    preEnd = 1
    for file in files:
        with open(file) as infile:
            for line in infile:
                if not re.match(r"^[> ][ 0-9]{5}", line):
                    continue
                num = int(line[1:6])
                if num > preEnd:
                    preEnd = num
    return preEnd

def endOfList(chat):
    a = dump1list(chat).split("\n")
    last = int(a[-1][1:6])
    end = 0
    if len(sys.argv) == 1 and sys.argv[0].isdigit():
        end = int(sys.argv.pop(0))
        if last < end:
            end = last
    elif len(sys.argv) == 0:
        end = last
    return last, end

def dump1list(url):
    host = port = fileRelative = None
    match = re.match(r"^http://([\w\.-]+)(:(\d+))?/(.*)$", url)
    if match:
        host = match.group(1)
        port = match.group(3) if match.group(3) else None
        fileRelative = match.group(4)

    content = ""
    subprocess.run(["python", "-w", "dump_lists.py", "dump1list.temp.txt", host, fileRelative])
    with open("dump1list.temp.txt") as dmpfile:
        content = dmpfile.read()
    os.remove("dump1list.temp.txt")
    return content

# --- define some literals -------------------------
chat = "http://webbbs.gamer.com.tw/board.php?brd=Chat"
page = "&p"
# ==================================================

# --- get the last of current articles ------------------------
last, end = endOfList(chat)
print(f"{chat} -> {last}")
print(f"$end = {end}")
# ============================================================

# --- get baha files --------
fileBaha = []
outfileBaha = None
for i in range(1, 100):
    tFileBaha = f"baha{i:02d}.txt"
    if os.path.isfile(tFileBaha):
        fileBaha.append(tFileBaha)
    elif outfileBaha is None:
        outfileBaha = tFileBaha
# ===========================

# --- the ranges to dump ------------------------
if fileBaha:
    preEnd = 1
    sys.argv.extend(check_continue(fileBaha))
    preEnd = preEnd(fileBaha)
    print(f"preEnd = {preEnd}")
    sys.argv.append(f"{preEnd}-{end}")
else:
    sys.argv.insert(0, f"1-{end}")
# =============================================

print(",".join(sys.argv))
outfileBaha = outfileBaha
print(f"output file -> {outfileBaha}")

for range_ in sys.argv:
    match = re.match(r"^(\d+)-(\d+)$", range_)
    if not match:
        continue
    print(f"{match.group(1)}-{match.group(2)}", file=sys.stderr)
    start = int(match.group(1))
    end = int(match.group(2))
    if start > end:
        continue

    listDocs = []
    host = port = fileRelative = None
    for i in range((start + 29) // 30 - 1, (end + 29) // 30 + 2):
        if i <= 0:
            continue
        url = f"{chat}{page}={i}"
        match = re.match(r"^http://([\w\.-]+)(:(\d+))?/(.*)$", url)
        if match:
            host = match.group(1)
            port = match.group(3) if match.group(3) else None
            fileRelative = match.group(4)
            listDocs.append(fileRelative)

    if listDocs:
        subprocess.run(["python", "-w", "dump_lists.py", outfileBaha, host, *listDocs])
