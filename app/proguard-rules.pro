# 保留行號與原始檔名，利於崩潰日誌 (Crash StackTrace) 追蹤除錯
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Gson 序列化保護規則
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn com.google.gson.**

# 保留使用 @SerializedName 標註的欄位
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 保留專案中 JSON/Gson 序列化與資料模型
-keep class com.kota.Bahamut.pages.model.** { *; }
-keep class com.kota.Bahamut.service.** { *; }
-keep class com.kota.telnet.model.** { *; }
