# AI 指引 (AI Guidelines)

## 開發規則
1. **禁止執行編譯與建置**：嚴禁 AI 自動執行 Gradle 編譯、建置或打包命令（例如 `./gradlew compileDebugKotlin`、`assembleDebug`、`build` 等）。編譯與驗證由使用者自行處理。
2. **共用元件與主題優先**：UI 開發盡量使用共用的 components（位於 `com.kota.Bahamut.ui.components.*`，如 `BahaButton`、`BahaText`、`BBSTopBar`、`BahaCheckbox` 等）、共用色彩與主題（`AppTheme.colors`、`BahamutAppTheme`、`ThemeBridge`），避免直接寫死色彩或建立重複元件。

