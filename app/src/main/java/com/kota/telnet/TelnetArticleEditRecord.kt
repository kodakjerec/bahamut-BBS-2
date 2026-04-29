package com.kota.telnet

/**
 * 文章修改紀錄
 * 
 * 儲存單筆文章修改紀錄，從 "※ 修改: 作者 (IP), 日期時間" 格式解析
 * 
 * 格式範例: "※ 修改: abc123 (123.456.789.012), 04/29/2024 12:00:00"
 */
class TelnetArticleEditRecord {
    /** 修改者 ID */
    var author: String = ""
    
    /** 修改者 IP */
    var ip: String = ""
    
    /** 修改日期時間（原始字串） */
    var dateTime: String = ""
    
    /** 原始修改紀錄行（完整文字） */
    var rawString: String = ""
}
