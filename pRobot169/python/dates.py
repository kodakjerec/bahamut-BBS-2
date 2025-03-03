import time
import datetime

def CSTtime():
    return time.time() + 8 * 3600

def year_mon_day(t=None):
    if t is None:
        t = time.time()
    dt = datetime.datetime.fromtimestamp(t)
    return f"{dt.year} {dt.month:02d} {dt.day:02d}"

today = year_mon_day(CSTtime())
yesterday = year_mon_day(CSTtime() - 86400)
P2D = year_mon_day(CSTtime() - 2 * 86400)

print(" ".join([P2D, yesterday, today]))

