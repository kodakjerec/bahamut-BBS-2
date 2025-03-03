import sys
import time
import socket
import gzip
from io import BytesIO

fAppend = sys.argv[1]
hh = sys.argv[2]
document = sys.argv[3:]

if not document:
    sys.exit()

def dump1list(host, fileRelative):
    timeInit = time.time()
    port = 80

    EOL = "\r\n"
    msgGetGzip = (
        f"GET /{fileRelative} HTTP/1.0{EOL}"
        f"Host: {host}{EOL}"
        "Accept: text/html, text/plain, text/sgml, */*;q=0.01{EOL}"
        "Accept-Encoding: gzip, compress{EOL}"
        "Accept-Language: en{EOL}"
        "User-Agent: Python{EOL}"
        "{EOL}"
    )

    msgGet = msgGetGzip

    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.connect((host, port))
    sock.sendall(msgGet.encode() + EOL.encode())

    print("getting...")
    response = b""
    sock.settimeout(0.05)
    while True:
        try:
            buf = sock.recv(10000)
            if not buf:
                break
            response += buf
        except socket.timeout:
            if time.time() - timeInit > 10:
                break

    sock.close()

    idx_rnrn = response.find(b"\r\n\r\n")
    if idx_rnrn < 0:
        return ""

    header = response[:idx_rnrn+4].decode().split("\r\n")
    content = response[idx_rnrn+4:]

    isVaryAcceptEncoding = False
    contentLength = 0
    contentEncoding = "text"
    for line in header:
        if "Vary:" in line and "Accept-Encoding" in line:
            isVaryAcceptEncoding = True
        elif line.startswith("Content-Length:"):
            contentLength = int(line.split(" ")[1])
        elif line.startswith("Content-Encoding:"):
            contentEncoding = line.split(" ")[1].strip()

    if (len(content) != contentLength and contentLength > 0) or len(content) == 0:
        return ""

    if "gzip" in contentEncoding:
        content = gzip.GzipFile(fileobj=BytesIO(content)).read()

    return htm2list(content.decode())

def htm2list(content):
    lineBuf = [""] * 12
    outlist = ""
    for line in content.split("\n"):
        line = line.strip()
        lineBuf.pop(0)
        lineBuf.append(line)

        if "readPost" in lineBuf[9] or ("queryUser" in lineBuf[5] and lineBuf[9].startswith("<font")):
            num = int(lineBuf[1].split(">")[1].split("<")[0])
            mark = lineBuf[3].split("</td>")[0].lower()[0]
            date = lineBuf[4].split("</td>")[0].split("../")[1]
            author = lineBuf[5].split("</a>")[0].split(">")[1]

            if '"javascript:thread(' in lineBuf[7]:
                title = lineBuf[7].split("'")[1]
            elif lineBuf[9].startswith("<font"):
                title = lineBuf[9].split("</font>")[0].split(">")[1]

            title = "◇ " + title if not title.startswith("Re: ") else "Re " + title[4:]
            title = title.replace("&gt;", ">").replace("&lt;", "<").replace("&nbsp;", " ").replace("&quot;", "\"").replace("&amp;", "&")

            gy = lineBuf[10].split("<em>")[1].split("</em>")[0] if "<em>" in lineBuf[10] else ""
            gy = min(int(gy), 99) if gy else ""
            gy = f"{gy:02}" if gy else ""

            outlist += f" {num:5} {mark:1}{gy:2}{date:5} {author:12} {title}\r\n"

    return outlist

with open(fAppend, "ab") as hdmp:
    for doc in document:
        print(f"http://{hh}/{doc}")
        list_content = dump1list(hh, doc)
        if list_content:
            print(" -> get")
            hdmp.write(list_content.encode())
        else:
            print(" -> fail")

