# My Dashboard Hub — Glass Edition

- Package preserved: `com.rudraditya.mydashboard`
- Version: `3.1-hub` (versionCode 6)
- Minimum Android: 8.0 (API 26)
- Intended for a lightweight Android home-screen widget app.

## Features in this starter source
- Clean dark glass app UI
- Custom vector app icon
- All-in-one dashboard widget
- Separate clock, battery, age-in-days, countdown and AI folder widgets
- Birthday and countdown date settings (`YYYY-MM-DD`)
- ChatGPT and Gemini quick-launch links in the app and AI widget
- Only INTERNET permission; no notification listener or accessibility service

## Not implemented yet
- Live weather (placeholder text only)
- In-app AI conversations (shortcuts only)
- Automatic wallpaper colour extraction
- Guaranteed minute-by-minute widget refresh (Android controls widget update timing)

## Build status
This source ZIP was generated but **has not been compiled or tested on a phone in this session**. The GitHub Actions run must succeed before installing the APK.

## GitHub build
Upload the extracted files to the repository root, commit to `main`, open Actions, run `Build My Dashboard Hub APK`, then download the `MyDashboard-Hub-debug-apk` artifact. Keep the `.github/workflows/android.yml` path exactly as shown.
