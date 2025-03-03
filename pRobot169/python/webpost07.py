import os
import sys
import time

# python webpost07.py Chat 12/5 uid_pass.txt
gBrdName = sys.argv[1]
gDate = sys.argv[2]
file_uid_pass = sys.argv[3]

if not (gBrdName.isalnum() and gDate.count('/') == 1 and os.path.isfile(file_uid_pass) and os.path.isfile("stat.txt")):
    print(f"Usage: python {sys.argv[0]} boardname date uid_pass_file")
    print("    The form of parameter 'date' is 'mm/dd'.")
    print("    Please execute this program in a directory with 'stat.txt'.")
    sys.exit()

os.system(f"python webpost07-1.py {gBrdName} {gDate} {file_uid_pass}")
time.sleep(5)
os.system(f"python webpost07-2.py {gBrdName} {gDate} {file_uid_pass}")
time.sleep(5)
os.system(f"python webpost07-3.py {gBrdName} {gDate} {file_uid_pass}")
time.sleep(5)
os.system(f"python webpost07-4.py {gBrdName} {gDate} {file_uid_pass}")

os.remove("baha_cookie.txt")
os.remove("post_info.txt")

