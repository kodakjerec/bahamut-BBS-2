# Bahamut/dialogs/uploadImgMethod

## 概述
uploadImgMethod 模組提供圖片上傳功能，支援多個圖床服務。

## 主要元件

### UploaderLitterCatBox
LitterCatBox 圖床上傳器
- 網址：https://litterbox.catbox.moe/
- 特點：免註冊、臨時儲存

### UploaderPostimageorg
Postimage.org 圖床上傳器
- 網址：https://postimages.org/
- 特點：永久儲存、無須註冊

## 功能
- 圖片壓縮
- 圖片上傳
- 取得圖片連結
- 上傳進度顯示
- 錯誤處理

## 上傳流程
1. 選擇或拍攝圖片
2. 圖片壓縮（可選）
3. 上傳到圖床
4. 取得圖片 URL
5. 插入到文章內容

## 圖床選擇
使用者可以在設定中選擇偏好的圖床服務

## 技術特點
- HTTP POST 上傳
- 多執行緒上傳
- 圖片壓縮演算法
- 使用 Kotlin 開發
