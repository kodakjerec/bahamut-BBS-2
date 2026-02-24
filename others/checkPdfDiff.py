import json
import os
import time
import concurrent.futures
from pdf2image import convert_from_path
import cv2
import numpy as np

DPI = 300
# 150 DPI: 網頁顯示、螢幕預覽
# 300 DPI: 標準列印品質，最常用的設定
# 600 DPI: 高品質列印
# 1200 DPI: 專業級列印品質

# (500,800) +-----------+
#           |           |
#           |           |
#           +-----------+ (550,850)
# region = [(500, 800, 550, 850)]
IGNORE_REGIONS = []  # 預設忽略區域 
FIRST_PAGE_ONLY = False  # 是否只檢查第一頁

DEBUG = True  # 是否啟用除錯模式
iTextFolder = 'pdfs/USIT1_G_iText/'
openPdfFolder = 'pdfs/USIT2_G_open/'


print("=====PDF差異檢查器=====")
print("=====  Settings  =====")
if len(IGNORE_REGIONS) > 0:
    print(f"忽略區域: {IGNORE_REGIONS}")
else:
    print(f"忽略區域: none")

if FIRST_PAGE_ONLY:
    print(f"檢查範圍: 只檢查第一頁")
else:
    print(f"檢查範圍: 全部檢查")

if DEBUG:
    print(f"除錯模式: 啟用")

print(f"DPI: {DPI}")

def are_regions_close(region1, region2, threshold=10):
    '''檢查兩個區域是否靠近'''
    x1, y1, x2, y2 = region1
    x3, y3, x4, y4 = region2
    
    # 檢查水平距離
    horizontal_close = (x1 - threshold <= x4 and x3 <= x2 + threshold)
    # 檢查垂直距離
    vertical_close = (y1 - threshold <= y4 and y3 <= y2 + threshold)
    
    return horizontal_close and vertical_close

def merge_regions(region1, region2):
    '''合併兩個區域'''
    x1 = min(region1[0], region2[0])
    y1 = min(region1[1], region2[1])
    x2 = max(region1[2], region2[2])
    y2 = max(region1[3], region2[3])
    return (x1, y1, x2, y2)

def find_difference_regions(img1, img2, threshold=0):
    '''找出圖像差異的區域'''
        
    # 檢查圖像尺寸是否相同，如果不同則調整
    if img1.shape[:2] != img2.shape[:2]:
        print(f"  警告: 圖像尺寸不同 - img1: {img1.shape[:2]}, img2: {img2.shape[:2]}")
        # 將 img2 調整為與 img1 相同的尺寸
        img2 = cv2.resize(img2, (img1.shape[1], img1.shape[0]))

    # 將圖像轉換為灰度
    gray1 = cv2.cvtColor(img1, cv2.COLOR_RGB2GRAY)
    gray2 = cv2.cvtColor(img2, cv2.COLOR_RGB2GRAY)
        
    # 加入高斯模糊，減少細微像素差異的影響
    gray1 = cv2.GaussianBlur(gray1, (3, 3), 0)
    gray2 = cv2.GaussianBlur(gray2, (3, 3), 0)

    # 指定區域不做差異比對
    mask = np.ones_like(gray1, dtype=np.uint8) * 255
    for region in IGNORE_REGIONS:
        x1, y1, x2, y2 = region
        mask[y1:y2, x1:x2] = 0

    # Apply mask to both images
    gray1 = cv2.bitwise_and(gray1, gray1, mask=mask)
    gray2 = cv2.bitwise_and(gray2, gray2, mask=mask)
    # 計算差異
    diff = cv2.absdiff(gray1, gray2)
    
    # 對差異進行閾值處理
    _, thresh = cv2.threshold(diff, threshold, 255, cv2.THRESH_BINARY)
    
    # 使用形態學操作去除小雜訊
    kernel = np.ones((3, 3), np.uint8)
    thresh = cv2.morphologyEx(thresh, cv2.MORPH_OPEN, kernel)  # 開運算去除小白點
    thresh = cv2.morphologyEx(thresh, cv2.MORPH_CLOSE, kernel)  # 閉運算填補小黑洞
    
    # 尋找輪廓
    contours, _ = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    
    # 獲取區域的邊界框
    regions = []
    for contour in contours:
        x, y, w, h = cv2.boundingRect(contour)
        # 添加內邊距
        padding = 5
        x = max(0, x - padding)
        y = max(0, y - padding)
        w = min(img1.shape[1] - x, w + 2*padding)
        h = min(img1.shape[0] - y, h + 2*padding)
        new_region = (x, y, x+w, y+h)
        
        # 檢查是否需要合併到現有區域
        merged = False
        for i, existing_region in enumerate(regions):
            if are_regions_close(existing_region, new_region):
                regions[i] = merge_regions(existing_region, new_region)
                merged = True
                break
        
        # 如果沒有合併到現有區域，則添加為新區域
        if not merged:
            regions.append(new_region)
    
    # 進行第二次合併檢查，確保所有鄰近區域都被合併
    i = 0
    while i < len(regions):
        j = i + 1
        while j < len(regions):
            if are_regions_close(regions[i], regions[j]):
                regions[i] = merge_regions(regions[i], regions[j])
                regions.pop(j)
            else:
                j += 1
        i += 1
    
    return regions

def compare_pdfs(sample, pdf1_path, pdf2_path):
    '''比較PDF檔案'''

    # 將PDF轉換為圖像
    pages1 = convert_from_path(pdf1_path, dpi=DPI, poppler_path='poppler-24.08.0/Library/bin')
    pages2 = convert_from_path(pdf2_path, dpi=DPI, poppler_path='poppler-24.08.0/Library/bin')

    # 儲存結果
    # 為此樣本創建結果目錄
    result_dir = f'results/{sample}'
    if not os.path.exists(result_dir):
        os.makedirs(result_dir)
    else:
        # 清除之前的結果
        for file in os.listdir(result_dir):
            file_path = os.path.join(result_dir, file)
            if os.path.isfile(file_path):
                os.remove(file_path)

    has_any_diff = False  # 追蹤是否有任何差異

    # 比較每一頁
    page_range = [(0, pages1[0], pages2[0])] if FIRST_PAGE_ONLY else enumerate(zip(pages1, pages2))
    
    for item in page_range:
        if FIRST_PAGE_ONLY:
            i, page1, page2 = item
        else:
            i, (page1, page2) = item
        # 將PIL圖像轉換為OpenCV格式
        img1 = cv2.cvtColor(np.array(page1), cv2.COLOR_RGB2BGR)
        img2 = cv2.cvtColor(np.array(page2), cv2.COLOR_RGB2BGR)
        
        regions = find_difference_regions(img1, img2, threshold=10)

        if regions:
            has_any_diff = True
            if DEBUG:
                print(f"第 {i+1} 頁有 {len(regions)} 處差異")
                print(f"第 {i+1} 頁差異區域: {regions}")
            # 為每個差異區域繪製矩形
            for region in regions:
                cv2.rectangle(img1, (region[0], region[1]), (region[2], region[3]), 
                            (0, 0, 255), 2)  # 紅色，線條寬度=2
                cv2.rectangle(img2, (region[0], region[1]), (region[2], region[3]), 
                            (0, 0, 255), 2)  # 紅色，線條寬度=2
            # ignore_regions 繪製綠色區域
            for region in IGNORE_REGIONS:
                cv2.rectangle(img1, (region[0], region[1]), (region[2], region[3]), 
                            (0, 255, 0), 2)  # 綠色，線條寬度=2
                cv2.rectangle(img2, (region[0], region[1]), (region[2], region[3]), 
                            (0, 255, 0), 2)  # 綠色，線條寬度=2
            
            # 儲存結果圖像
            cv2.imwrite(f'results/{sample}/page{i+1:02d}i.png', img1)
            cv2.imwrite(f'results/{sample}/page{i+1:02d}o.png', img2)
        else:
            if DEBUG:
                print(f"第 {i+1} 頁沒有發現差異")
                
    return has_any_diff

def prcoess_sample(sample):
    print(f"\n處理樣本: {sample}")
    
    pdf1_path = f'{iTextFolder}{sample}i.pdf'
    pdf2_path = f'{openPdfFolder}{sample}o.pdf'
    
    # 檢查兩個檔案是否存在
    if not os.path.exists(pdf1_path):
        print(f"錯誤: {pdf1_path} 不存在")
        return "missing_file"
        
    if not os.path.exists(pdf2_path):
        print(f"錯誤: {pdf2_path} 不存在")
        return "missing_file"
        
    sample_start_time = time.time()
    has_diff = compare_pdfs(sample, pdf1_path, pdf2_path)
    sample_end_time = time.time()
    sample_time = sample_end_time - sample_start_time
    
    print(f"樣本 {sample} 比較時間: {sample_time:.2f} 秒")
    return "has_diff" if has_diff else "no_diff"

def main():
    print("=====  開始檢查  =====")
    # 主程序
    start_time = time.time()

    # 從iText資料夾獲取所有PDF檔案
    samples = []

    if os.path.exists(iTextFolder):
        for filename in os.listdir(iTextFolder):
            if filename.endswith('.pdf'):
                # 移除副檔名和後綴'i'來獲取樣本名稱
                sample_name = filename[:-5]  # 移除'.pdf'
                if sample_name.endswith('i'):
                    sample_name = sample_name[:-1]  # 移除'i'
                samples.append(sample_name)
        
        samples.sort()  # 排序樣本名稱
        print(f"找到 {len(samples)} 個樣本: {samples}")
    else:
        print(f"錯誤: {iTextFolder} 資料夾不存在")
        return

    # DEBUG
    # samples = ["04071352"]

    # 統計變數
    total_samples = len(samples)
    has_diff_count = 0
    missing_file_count = 0
    no_diff_count = 0

    # 處理每個樣本
    # 使用線程池同時處理多個樣本，最多5個線程
    with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
        # 提交所有任務
        futures = [executor.submit(prcoess_sample, sample) for sample in samples]
        
        # 等待所有任務完成並收集結果
        for future in concurrent.futures.as_completed(futures):
            result = future.result()
            if result == "has_diff":
                has_diff_count += 1
            elif result == "missing_file":
                missing_file_count += 1
            elif result == "no_diff":
                no_diff_count += 1

    end_time = time.time()
    execution_time = end_time - start_time
    
    # 輸出統計結果
    print("\n=====  統計結果  =====")
    print(f"總樣本數: {total_samples}")
    print(f"有差異的PDF: {has_diff_count}")
    print(f"無差異的PDF: {no_diff_count}")
    print(f"缺少檔案的樣本: {missing_file_count}")
    print(f"成功處理的樣本: {has_diff_count + no_diff_count}")
    print(f"\n執行時間: {execution_time:.2f} 秒")

if __name__ == "__main__":
    main()