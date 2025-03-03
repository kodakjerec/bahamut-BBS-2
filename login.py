import websocket
import threading
import time
import keyboard
import os
from enum import Enum
from datetime import datetime, timedelta

class PageState(Enum):
    LOGIN = "login"
    ANNOUNCEMENT = "announcement"
    MAIN = "main"
    CHAT = "chat"

host = "wss://term.gamer.com.tw/bbs"
user = "seqa"
passwd = "0425"
global nowPage # 目前頁面
nowPage = PageState.LOGIN

global today # 今天日期
today = datetime.now().strftime("%m/%d")
global yesterday # 昨天日期
yesterday = (datetime.now() - timedelta(days=1)).strftime("%m/%d")

def recordArticle(content):
    """記錄文章"""
    print(content)
    global today
    global yesterday
    isContinue = False # 是否繼續記錄文章
    lines = content.split('\n\r')
    for line in lines:
        if line[1:5].strip().isdigit(): # 判斷是否為文章編號
            if (today in line[10:15]): # 判斷是否為今天或昨天的文章
                isContinue = True
                # with open("output.txt", "a", encoding="utf8") as f:
                #     f.write(line+"\n")
    if isContinue:
        # with open("output.txt", "a", encoding="utf8") as f:
        #     f.write(content+"\n\n\n\n")
        inputSend(ws, '\033[5~') # 按PageUp
        time.sleep(1)
        content = ""
                    

def judgePage(content):
    """判斷目前頁面"""
    global nowPage
    if "對戰で己を磨け" in content:
        nowPage = PageState.LOGIN
    elif "系 統 公 告" in content:
        nowPage = PageState.ANNOUNCEMENT
    elif "【主功能表】" in content:
        nowPage = PageState.MAIN
    elif "看板《Chat》" in content:
        nowPage = PageState.CHAT

def pageAction(ws,content):
    """頁面內動作"""
    global nowPage
    if nowPage == PageState.LOGIN:
        if "請輸入勇者代號" in content:
            inputSend(ws, user+"\n")
        if "請輸入勇者密碼" in content:
            inputSend(ws, passwd+"\n")
        if "您想刪除其他重複的" in content:
            inputSend(ws, 'y\n')
    elif nowPage == PageState.ANNOUNCEMENT:
        if "請按任意鍵繼續" in content:
            inputSend(ws, '\n')
    elif nowPage == PageState.MAIN:
        inputSend(ws, 'sChat\n\n')
        nowPage = PageState.CHAT
    elif nowPage == PageState.CHAT:
        # 將文章記錄到文件
        recordArticle(content)


def on_message(ws, message):
    global nowPage
    content = message.decode("big5","ignore")
    # Write binary message in hexadecimal format to output2.txt
    with open("output.txt", "a", encoding="utf8") as f:
        f.write(content+"\n\n\n\n")
    with open("output2.txt", "a") as f:
        f.write(' '.join(['{:02x}'.format(b) for b in message]) +"\n\n\n\n")

    # 判斷目前頁面
    judgePage(content)

    # 頁面內動作
    pageAction(ws, content)

def inputSend(ws, user_input):
    ws.send((user_input), websocket.ABNF.OPCODE_BINARY)

def on_error(ws, error):
    print(error)

def on_close(ws, close_status_code, close_msg):
    print("### closed ###")
    os._exit(0)  # Force exit Python process

def ws_thread(ws):
    ws.run_forever()

def on_open(ws):
    print("on_open")

if __name__ == "__main__":
    # Create WebSocket connection
    ws = websocket.WebSocketApp(host,on_open=on_open,on_message=on_message,on_error=on_error,on_close=on_close)
    
    # Clear output.txt at startup
    with open("output.txt", "w", encoding="utf8") as f:
        f.write("")
    with open("output2.txt", "w", encoding="utf8") as f:
        f.write("")

    # Start websocket thread
    wst = threading.Thread(target=ws_thread, args=(ws,))
    wst.daemon = True
    wst.start()

    # Main input loop
    while True:
        try:
            user_input = input()
            if user_input == 'u':
                user_input = '\x1b[A'
                ws.send((user_input), websocket.ABNF.OPCODE_BINARY)
            elif user_input == 'd':
                user_input = '\x1b[B'
                ws.send((user_input), websocket.ABNF.OPCODE_BINARY)
            elif user_input == 'r':
                user_input = '\x1b[C'
                ws.send((user_input), websocket.ABNF.OPCODE_BINARY)
            elif user_input == 'l':
                user_input = '\x1b[D'
                ws.send((user_input), websocket.ABNF.OPCODE_BINARY)
            elif user_input == 'pu':
                user_input = '\x1b[5~'
                ws.send((user_input), websocket.ABNF.OPCODE_BINARY)
            elif user_input == 'pd':
                user_input = '\x1b[6~' 
                ws.send((user_input), websocket.ABNF.OPCODE_BINARY)
            else:  
                ws.send((user_input+"\r\n"), websocket.ABNF.OPCODE_BINARY)     
            os.system('cls')
        except KeyboardInterrupt:
            ws.close()
            break
        except websocket.WebSocketConnectionClosedException:
            break
