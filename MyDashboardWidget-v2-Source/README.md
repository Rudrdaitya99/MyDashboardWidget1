# My Dashboard Widget v2

Custom Android home-screen widget; it does not replace your realme launcher.

## Features
- System clock/date
- Battery percentage on widget refresh
- Weather for a manually configured city using Open-Meteo APIs (internet required, no API key)
- Countdown to a date entered in the app
- Three configurable app shortcuts using Android package names
- Settings, camera and music-app shortcuts
- Play/pause/next/previous through active Android media sessions when Notification access is enabled and the player supports controls
- Glass-inspired tinted background (not true wallpaper blur)
- Configuration screen
- GitHub Actions workflow for a debug APK

## Configure
1. Install and open My Dashboard.
2. Enter a city, countdown title/date (YYYY-MM-DD), and optional package names for three app shortcuts.
3. Save and refresh.
4. Long-press home screen > Widgets > My Dashboard.
5. Tap Enable music controls in the app and enable its Notification access.

## Build
Requirements: JDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3, Android SDK platform 35.
Upload the extracted files to a GitHub repository and run Actions > Build Android APK. Download the `MyDashboard-v2-debug-apk` artifact from a successful workflow.

## Status
Source project generated. It has NOT been compiled in this environment or tested on a realme Android 16 phone. The included cloud build workflow has not been run. Build and device testing are required before release use.

## Limitations
- Android may defer scheduled widget updates; weather/battery refresh is not instant.
- Weather needs internet and a valid city name.
- Music controls require user-granted notification access and an active media session; not every app supports all commands.
- App shortcuts require valid installed package names.
- Glass is simulated with translucent tint and a border; true background blur is not guaranteed by Android widgets.
