import os
import sys
import time
import glob
import subprocess
from datetime import datetime, timedelta


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

def merge(files):
    strings = {}
    merged_list = []
    for file in files:
        with open(file, 'r') as infile:
            for line in infile:
                line = line.strip()
                if line.startswith(('>', ' ')) and line[1:6].isdigit():
                    num = line[1:6]
                    strings[num] = line.replace('>', ' ', 1)
    for key in sorted(strings):
        merged_list.append(strings[key])
    return merged_list

def check_continue(files):
    num = {}
    for file in files:
        with open(file, 'r') as continue_file:
            for line in continue_file:
                if line.startswith(('>', ' ')) and line[1:6].isdigit():
                    num[line[1:6]] = 1
    a = b = sorted(num.keys())
    a.insert(0, 1)
    missing_list = []
    for i in range(len(b)):
        if b[i] - a[i] > 1:
            missing_list.append(f"{a[i]}-{b[i]-1}")
    return missing_list

def runGetList(exeGetList, arg=None):
    cmd = ['perl', exeGetList]
    if arg:
        cmd.append(str(arg))
    subprocess.run(cmd)
    refreshFileBaha()

usage = """Usage:
  python {0} exeGetList [yr1 mon1 mday1 yr2 mon2 mday2 yr3 mon3 mday3]
  exeGetList:      the python script to get article list
  yr1,mon1,mday1:  the day before yesterday
  yr2,mon2,mday2:  yesterday
  yr3,mon3,mday3:  today
    yr? is 4-char integer; mon? is 2-char; mday is 2-char. If no enough
    digits, please add leading 0. If any of yr? or mon? or mday? is not
    supplied, dates.pl will be called to get these.
""".format(sys.argv[0])

exeGetList = sys.argv[1] if len(sys.argv) > 1 else None
if not exeGetList or not os.path.isfile(exeGetList):
    print(usage)
    sys.exit(1)

# --- get the year / month / mday ---------
p2d_yr = p2d_mon = p2d_mday = None
yesterday_yr = yesterday_mon = yesterday_mday = None
today_yr = today_mon = today_mday = None

if len(sys.argv) == 10:
    # get year / month / mday by argument list
    p2d_yr, p2d_mon, p2d_mday, yesterday_yr, yesterday_mon, yesterday_mday, today_yr, today_mon, today_mday = sys.argv[2:11]
else:
    # get year / month / mday by dates.pl
    result = subprocess.run(['perl', 'dates.pl'], capture_output=True, text=True)
    p2d_yr, p2d_mon, p2d_mday, yesterday_yr, yesterday_mon, yesterday_mday, today_yr, today_mon, today_mday = result.stdout.split()

today = f"{today_mon}/{today_mday}"
yesterday = f"{yesterday_mon}/{yesterday_mday}"
yesterday8 = f"{yesterday_yr}{yesterday_mon}{yesterday_mday}"
p2d = f"{p2d_mon}/{p2d_mday}"

# =========================================

fileBaha = []
outfileBaha = None
refreshFileBaha()

while True:
    tail30 = merge(fileBaha)[-30:]
    t = [s for s in tail30 if s.startswith(f"{' ' * 10}{today}")]
    if len(t) == 30:
        break
    time.sleep(30)
    runGetList(exeGetList)

for _ in range(10):
    t = check_continue(fileBaha)
    if not t:
        break
    runGetList(exeGetList, 1)

listYesterday8 = merge(fileBaha)
with open(f"{yesterday8}.txt", "w") as yesterday8_file:
    yesterday8_file.write("\n".join(listYesterday8))

# --- try to find the begin number of yesterday articles ---
begin = int(next((s[1:6] for s in listYesterday8 if s.startswith(f"{' ' * 10}{p2d}")), 0)) - 100
if begin <= 0:
    begin = 1
elif begin > 1000:
    begin = 1000

print("Generating statistics...")
subprocess.run(['perl', '../statistics.pl', f"{yesterday8}.txt", yesterday, str(begin)], stdout=open('stat.txt', 'w'))

if not os.path.exists(yesterday8):
    os.makedirs(yesterday8)

for file in fileBaha + [f"{yesterday8}.txt", "stat.txt"]:
    os.rename(file, os.path.join(yesterday8, file))

sys.exit()
