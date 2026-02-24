from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import json
import time
import re
import requests


def is_time_over_threshold(time_str, hours_threshold=3):
    """
    判斷時間字串是否超過指定小時數
    例如（threshold=3）：「30 分鐘前」-> False, 「2 小時前」-> False, 「3 小時前」-> True, 「2 天前」-> True
    """
    if not time_str:
        return False
    
    # 匹配「X 小時前」，檢查是否超過閾值
    hour_match = re.search(r'(\d+)\s*小時前', time_str)
    if hour_match:
        hours = int(hour_match.group(1))
        return hours >= hours_threshold
    
    # 匹配「X 天前」、「X 週前」、「X 月前」、「X 年前」-> 一定超過
    if re.search(r'\d+\s*(天|週|周|月|年)前', time_str):
        return True
    
    # 「分鐘前」表示還沒超過閾值
    if re.search(r'\d+\s*分鐘前', time_str):
        return False
    
    # 其他情況（如完整日期）視為超過閾值
    if re.search(r'\d{4}[/-]\d{1,2}[/-]\d{1,2}', time_str):
        return True
    
    return False


def fetch_article_content(url):
    """
    讀取新聞內文
    """
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
        'Accept-Language': 'zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7',
    }
    
    try:
        response = requests.get(url, headers=headers, timeout=15)
        response.raise_for_status()
        response.encoding = 'utf-8'
        
        soup = BeautifulSoup(response.text, 'html.parser')
        
        # Yahoo 新聞內文通常在 article 或特定 class 內
        article = soup.select_one('article') or soup.select_one('div.caas-body')
        
        if article:
            # 移除不需要的元素
            for tag in article.select('script, style, iframe, aside'):
                tag.decompose()
            return article.get_text(separator='\n', strip=True)
        
        return ''
    except Exception as e:
        print(f"讀取內文錯誤: {e}")
        return ''


def extract_stock_info(content, title=''):
    """
    從新聞內文中提取股票資訊，分類正面/負面消息
    
    Returns:
        dict: 包含正面消息和負面消息的字典
    """
    full_text = title + '\n' + content
    
    # 股票代號模式：4位數字 + 空格/括號 + 中文名稱
    stock_pattern = r'(\d{4})\s*[（(]?([^）)\d\s]{2,6})[）)]?|([^）)\d\s]{2,6})[（(](\d{4})[）)]'
    
    # 正面關鍵字
    positive_keywords = [
        '上漲', '漲停', '大漲', '飆漲', '創新高', '利多', '獲利', '成長', 
        '營收增', '訂單', '參展', '合作', '簽約', '受惠', '看好', '推薦',
        '買超', '外資買', '法人買', '突破', '強勢', '熱門', '亮眼', '佳績',
        '擴產', '擴廠', '新產品', '新技術', 'AI', '人工智慧'
    ]
    
    # 負面關鍵字
    negative_keywords = [
        '下跌', '跌停', '大跌', '重挫', '創新低', '利空', '虧損', '衰退',
        '營收減', '流失', '退出', '取消', '警示', '看淡', '賣超', '外資賣',
        '法人賣', '跌破', '弱勢', '低迷', '下修', '縮減', '裁員', '關廠'
    ]
    
    # 產業分類關鍵字
    category_keywords = {
        '半導體/IC設計': ['半導體', 'IC設計', '晶圓', '晶片', '記憶體', 'DRAM', 'NAND'],
        '電信': ['電信', '5G', '6G', '行動通訊', '寬頻'],
        'AI/伺服器': ['AI', '人工智慧', '伺服器', 'GPU', 'HPC', '資料中心'],
        'EMS/代工': ['EMS', '代工', 'ODM', 'OEM'],
        '網通設備': ['網通', '交換器', '路由器', 'Wi-Fi', '基地台'],
        '衛星通訊': ['衛星', 'SpaceX', 'Starlink', 'LEO', '低軌'],
        '被動元件': ['被動元件', '電容', '電阻', 'MLCC'],
        '面板/顯示器': ['面板', 'LCD', 'OLED', '顯示器'],
        '汽車/電動車': ['汽車', '電動車', 'EV', '車用'],
        '生技醫療': ['生技', '醫療', '藥品', '疫苗'],
        '金融': ['金融', '銀行', '保險', '證券'],
        '塑膠/化工': ['塑膠', '化工', '石化'],
        '鋼鐵': ['鋼鐵', '鋼材', '鋼價'],
        '航運': ['航運', '貨櫃', '散裝'],
    }
    
    result = {
        '正面消息': [],
        '負面消息': []
    }
    
    # 找出所有提到的股票
    stocks_found = set()
    
    # 模式1: 代號 名稱 (如 "2330台積電" 或 "2330（台積電）")
    matches1 = re.findall(r'(\d{4})\s*[（(]?([^\d\s）)]{2,6})[）)]?', full_text)
    for code, name in matches1:
        if name and not re.match(r'^[\d\.\-]+$', name):
            stocks_found.add((code, name.strip()))
    
    # 模式2: 名稱（代號）(如 "台積電（2330）")
    matches2 = re.findall(r'([^\d\s）)]{2,6})[（(](\d{4})[）)]', full_text)
    for name, code in matches2:
        if name and not re.match(r'^[\d\.\-]+$', name):
            stocks_found.add((code, name.strip()))
    
    if not stocks_found:
        return result
    
    # 分析每支股票的情緒
    for code, name in stocks_found:
        stock_str = f"{code} {name}"
        
        # 找出股票在文中的上下文（前後50字）
        stock_context = ''
        for pattern in [f'{code}', name]:
            idx = full_text.find(pattern)
            if idx != -1:
                start = max(0, idx - 50)
                end = min(len(full_text), idx + len(pattern) + 50)
                stock_context += full_text[start:end] + ' '
        
        # 判斷正面或負面
        positive_count = sum(1 for kw in positive_keywords if kw in stock_context or kw in full_text[:500])
        negative_count = sum(1 for kw in negative_keywords if kw in stock_context or kw in full_text[:500])
        
        # 判斷產業類別
        category = '其他'
        for cat, keywords in category_keywords.items():
            if any(kw in full_text for kw in keywords):
                category = cat
                break
        
        # 提取原因（從標題或內文前200字找關鍵句）
        reason = ''
        if name in title or code in title:
            reason = title
        else:
            # 找包含股票名稱的句子
            sentences = re.split(r'[。！？\n]', full_text[:500])
            for sent in sentences:
                if name in sent or code in sent:
                    reason = sent.strip()[:100]
                    break
        
        if not reason:
            reason = title
        
        stock_info = {
            '類別': category,
            '股票': [stock_str],
            '原因': reason
        }
        
        if positive_count > negative_count:
            result['正面消息'].append(stock_info)
        elif negative_count > positive_count:
            result['負面消息'].append(stock_info)
        else:
            # 預設放正面
            result['正面消息'].append(stock_info)
    
    # 合併同類別的股票
    result = merge_same_category(result)
    
    return result


def merge_same_category(result):
    """
    合併同類別的股票
    """
    for sentiment in ['正面消息', '負面消息']:
        if not result[sentiment]:
            continue
        
        merged = {}
        for item in result[sentiment]:
            cat = item['類別']
            if cat not in merged:
                merged[cat] = {
                    '類別': cat,
                    '股票': [],
                    '原因': item['原因']
                }
            # 避免重複股票
            for stock in item['股票']:
                if stock not in merged[cat]['股票']:
                    merged[cat]['股票'].append(stock)
        
        result[sentiment] = list(merged.values())
    
    return result


def fetch_yahoo_stock_news(url="https://tw.stock.yahoo.com/tw-market/", max_scroll=50):
    """
    爬取 Yahoo 台股盤勢新聞（使用 Selenium 動態滾動）
    
    Args:
        url: 目標網址
        max_scroll: 最大滾動次數（防止無限滾動）
    
    Returns:
        list: 包含新聞標題、網址、來源、時間、摘要的字典列表
    """
    # 設定 Chrome 選項
    chrome_options = Options()
    chrome_options.add_argument('--headless')  # 無頭模式
    chrome_options.add_argument('--disable-gpu')
    chrome_options.add_argument('--no-sandbox')
    chrome_options.add_argument('--disable-dev-shm-usage')
    chrome_options.add_argument('--window-size=1920,1080')
    chrome_options.add_argument('--lang=zh-TW')
    chrome_options.add_argument('user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36')
    
    driver = None
    try:
        driver = webdriver.Chrome(options=chrome_options)
        driver.get(url)
        
        # 等待頁面載入
        WebDriverWait(driver, 10).until(
            EC.presence_of_element_located((By.CSS_SELECTOR, 'li.js-stream-content'))
        )
        
        news_list = []
        seen_urls = set()
        scroll_count = 0
        should_stop = False
        
        while scroll_count < max_scroll and not should_stop:
            # 解析當前頁面
            soup = BeautifulSoup(driver.page_source, 'html.parser')
            news_items = soup.select('li.js-stream-content')
            
            for item in news_items:
                try:
                    # 找到包含 data-test-locator="mega" 的 div
                    mega_div = item.select_one('div[data-test-locator="mega"]')
                    if not mega_div:
                        continue
                    
                    # 取得來源和時間
                    source_div = mega_div.select_one('div[class*="C(#959595)"]')
                    source = ''
                    time_ago = ''
                    if source_div:
                        spans = source_div.select('span')
                        if len(spans) >= 1:
                            source = spans[0].get_text(strip=True)
                        if len(spans) >= 2:
                            time_ago = spans[1].get_text(strip=True)
                    
                    # 取得標題和連結
                    h3 = mega_div.select_one('h3')
                    if not h3:
                        continue
                        
                    link = h3.select_one('a')
                    if not link:
                        continue
                    
                    href = link.get('href', '')
                    title = link.get_text(strip=True)
                    
                    # 確保是完整的 URL
                    if href and not href.startswith('http'):
                        href = 'https://tw.stock.yahoo.com' + href
                    
                    # 跳過已處理的 URL
                    if href in seen_urls or not href:
                        continue
                    
                    if not title or len(title) < 5:
                        continue
                    
                    # 檢查時間是否超過1小時
                    if is_time_over_threshold(time_ago, hours_threshold=1):
                        # 如果第一則新聞就超過閾值，表示沒有新的更新
                        if len(news_list) == 0:
                            print("目前沒有1小時內的新聞更新")
                            should_stop = True
                            break
                        else:
                            print(f"已達到1小時前的新聞：{title} ({time_ago})")
                            should_stop = True
                            break
                    
                    seen_urls.add(href)
                    
                    # 取得摘要
                    summary_p = mega_div.select_one('p[class*="LineClamp"]')
                    summary = summary_p.get_text(strip=True) if summary_p else ''
                    
                    # 讀取新聞內文並分析
                    print(f"    正在讀取內文...")
                    article_content = fetch_article_content(href)
                    stock_analysis = extract_stock_info(article_content, title)
                    
                    news_item = {
                        '標題': title,
                        '網址': href,
                        '來源': source,
                        '時間': time_ago,
                        '摘要': summary,
                        '建議內容': stock_analysis
                    }
                    
                    news_list.append(news_item)
                    print(f"[{len(news_list)}] {title} ({time_ago})")
                    if stock_analysis['正面消息'] or stock_analysis['負面消息']:
                        print(f"    -> 找到 {len(stock_analysis['正面消息'])} 則正面, {len(stock_analysis['負面消息'])} 則負面")
                    
                except Exception as e:
                    continue
            
            if should_stop:
                break
            
            # 滾動頁面
            last_height = driver.execute_script("return document.body.scrollHeight")
            driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
            time.sleep(1.5)  # 等待新內容載入
            
            new_height = driver.execute_script("return document.body.scrollHeight")
            if new_height == last_height:
                print("已到達頁面底部")
                break
            
            scroll_count += 1
            print(f"滾動 {scroll_count} 次...")
        
        return news_list
        
    except Exception as e:
        print(f"錯誤: {e}")
        return []
    
    finally:
        if driver:
            driver.quit()


def print_news(news_list):
    """
    格式化輸出新聞列表
    """
    if not news_list:
        print("沒有找到新聞")
        return
    
    print(f"\n共找到 {len(news_list)} 則新聞:\n")
    print("=" * 80)
    
    for i, news in enumerate(news_list, 1):
        print(f"[{i}] {news['標題']}")
        print(f"    來源: {news.get('來源', '')} | 時間: {news.get('時間', '')}")
        print(f"    網址: {news['網址']}")
        if news.get('摘要'):
            print(f"    摘要: {news['摘要']}")
        
        # 顯示建議內容
        suggestion = news.get('建議內容', {})
        if suggestion:
            if suggestion.get('正面消息'):
                print(f"    【正面消息】")
                for item in suggestion['正面消息']:
                    print(f"      - 類別: {item['類別']}")
                    print(f"        股票: {', '.join(item['股票'])}")
                    print(f"        原因: {item['原因']}")
            if suggestion.get('負面消息'):
                print(f"    【負面消息】")
                for item in suggestion['負面消息']:
                    print(f"      - 類別: {item['類別']}")
                    print(f"        股票: {', '.join(item['股票'])}")
                    print(f"        原因: {item['原因']}")
        print("-" * 80)


def save_to_json(news_list, filename="yahoo_news.json"):
    """
    將新聞列表儲存為 JSON 檔案
    """
    with open(filename, 'w', encoding='utf-8') as f:
        json.dump(news_list, f, ensure_ascii=False, indent=2)
    print(f"已儲存至 {filename}")


if __name__ == "__main__":
    print("正在爬取 Yahoo 台股盤勢新聞...")
    news = fetch_yahoo_stock_news()
    print_news(news)
    
    # 儲存結果
    if news:
        save_to_json(news)
