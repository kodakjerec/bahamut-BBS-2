import os
import sys
import time
import shutil
from datetime import datetime


def refreshFileBaha():
    global fileBaha, outfileBaha
    fileBaha = []
    outfileBaha = None
    for i in range(1, 100):
        tFileBaha = f"baha{i:02d}.txt"
        if os.path.isfile(tFileBaha):
            fileBaha.append(tFileBaha)
        elif outfileBaha is None:
            outfileBaha = tFileBaha

def runGetList(exeGetList):
    os.system(f"python {exeGetList}")
    refreshFileBaha()

def CSTtime():
    return time.time() + 8 * 3600

def date(strFormat="%a %b %e %H:%M:%S %Y"):
    strFormat = strFormat.lstrip("+")
    return datetime.fromtimestamp(CSTtime()).strftime(strFormat)

def cp(from_path, to_path):
    shutil.copyfile(from_path, to_path)

usage = """Usage:
  python {0} exeGetList uid_pass.txt
  exeGetList:      the python script to get article list
  uid_pass.txt:    uid & password file, select non-existent file means
                   not to webpost
""".format(sys.argv[0])

exeGetList = sys.argv[1] if len(sys.argv) > 1 else (print(usage) or sys.exit(1))
if not os.path.isfile(exeGetList):
    print(f"{exeGetList} not exists!\n{usage}")
    sys.exit(1)

fileUidPasswd = sys.argv[2] if len(sys.argv) > 2 else (print(usage) or sys.exit(1))
if not os.path.isfile(fileUidPasswd):
    print(f"{fileUidPasswd} not found -> will not post\n")

fileBaha = []
outfileBaha = None
refreshFileBaha()

do_at = {}
while True:
    hrmin = datetime.now().strftime("%H%M")
    hr = hrmin[:2]
    min = hrmin[2:]
    if hrmin <= "0300":
        break

    if hr in ["06", "18", "22"] and not do_at.get(f"{hr}00"):
        runGetList(exeGetList)
        do_at[f"{hr}00"] = 1
    elif hrmin > "2330" and not do_at.get("2330"):
        runGetList(exeGetList)
        do_at["2330"] = 1
    elif hrmin > "0700":
        refreshFileBaha()
        if len(fileBaha) == 0:
            runGetList(exeGetList)

    hrmin = datetime.now().strftime("%H%M")
    hr = hrmin[:2]
    min = hrmin[2:]
    minWait = 10

    if hrmin <= "0300":
        break
    if hrmin <= "0600":
        minWait = 60
    elif hrmin > "2330":
        minWait = 1

    secWait = minWait * 60
    print(f"{datetime.now()} - sleep for {secWait} secs")
    time.sleep(secWait)

p2d_yr, p2d_mon, p2d_mday, yesterday_yr, yesterday_mon, yesterday_mday, today_yr, today_mon, today_mday = os.popen("python dates.py").read().split()

yesterday = f"{yesterday_mon}/{yesterday_mday}"
yesterday8 = f"{yesterday_yr}{yesterday_mon}{yesterday_mday}"
if os.path.isdir(yesterday8):
    sys.exit()

print("Call last1.py...")
os.system(f"python last1.py {exeGetList} {p2d_yr} {p2d_mon} {p2d_mday} {yesterday_yr} {yesterday_mon} {yesterday_mday} {today_yr} {today_mon} {today_mday}")

if os.path.isfile(fileUidPasswd):
    print("Posting...")
    shutil.copyfile(f"{yesterday8}/stat.txt", "stat.txt")
    os.system(f"python webpost07.py Chat {yesterday} {fileUidPasswd}")
    os.remove("stat.txt")

sys.exit()

