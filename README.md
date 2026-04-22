<!-- // @(#) $Id: README.md,v 1.11 2026/04/22 14:12:43 ralph Exp $ -->

# GC-Now App!

>[!NOTE]
English description follows, Kurzübersicht in Deutsch weiter unten

## GC Now! – Read Me – Geocache Helper App

**GC Now!** was created to solve a practical problem: displaying the current date and time together with your Geocaching nickname — and, if desired, your personal geocache QR code, profile picture, or logo. The main inspiration was **Virtual Caches**, where a proof photo often needs to show your GC nickname, the date, and the cache. Since I often had two smartphones with me on spontaneous trips, but rarely paper and pen, **GC Now!** became my solution: everything I need is right at hand.

**GC Now!** is a fast and flexible app for Android 12 and later. With its clean, high-contrast design and extensive customization options, it offers an ideal way to display real-time data in an attractive, personalized form on a mobile device. You can switch between light and dark mode, personalize your Geocaching nickname, upload your own image, and choose the background color freely — so the app is not only functional, but also truly *yours*. The app requires no additional permissions and does not transmit any data.

**Installing the APK on Android**

1. **Download the APK**
   Download the desired APK file directly from the [GCNow Releases](https://github.com/roseswe/GCNow/releases) page — either on your Android device or via PC, then transfer it to your device.

2. **Prepare for Installation**
   Open your device’s *Settings*, go to *Security* or *Apps & Notifications*, and enable installation from *Unknown Sources* for your browser or file manager.

3. **Install the APK**
   Open the downloaded file from your Downloads folder or file manager and follow the on-screen prompts to complete the installation.

4. **Launch the App**
   After successful installation, you’ll find the app in your app drawer or on the home screen, ready to use.

>[!NOTE] Updating
Note: Since the app is not installed via the Play Store, you will need to manually update it.

---

## GC Now! – Lies mich – Geocache-Helfer-App

**GC Now!** entstand aus einem praktischen Bedürfnis: eine App zu schaffen, die das aktuelle Datum und die Uhrzeit zusammen mit deinem Geocaching-Nickname anzeigt — und auf Wunsch zusätzlich deinen persönlichen Geocache-QR-Code, dein Profilbild oder dein Logo einblendet. Die wichtigste Inspiration waren **Virtuelle Caches**, bei denen oft ein Beweisfoto verlangt wird, auf dem GC-Nickname, Datum und Cache erkennbar sind. Da ich bei spontanen Touren häufig zwei Smartphones, aber selten Papier und Stift dabeihabe, wurde die App zu meiner Lösung: Mit **GC Now!** habe ich alles Notwendige direkt griffbereit.

**GC Now!** ist eine schnelle und flexible App für Android 12 und höher. Mit ihrem klaren, kontrastreichen Design und umfangreichen Anpassungsmöglichkeiten bietet sie eine ideale Lösung für alle, die Echtzeitdaten ansprechend und individuell auf ihrem mobilen Gerät darstellen möchten. Neben einem einfachen Wechsel zwischen Hell- und Dunkelmodus kannst du in **GC Now!** jedes Detail nach deinen Wünschen gestalten: deinen Geocaching-Nicknamen personalisieren, ein eigenes Bild hochladen und die Hintergrundfarbe frei festlegen — damit die App nicht nur funktional, sondern auch ganz *deins* ist. Die App benötigt keine zusätzlichen Berechtigungen und überträgt keine Daten.

**Installation der APK auf Android**

1. **APK herunterladen**
   Lade die gewünschte APK-Datei direkt von den [GCNow Releases](https://github.com/roseswe/GCNow/releases) herunter — entweder über dein Android-Gerät oder per PC mit anschließender Übertragung.

2. **Installation vorbereiten**
   Öffne die *Einstellungen* deines Geräts, gehe zu *Sicherheit* oder *Apps & Benachrichtigungen* und aktiviere die Installation aus *Unbekannten Quellen* für deinen Browser oder Dateimanager.

3. **APK installieren**
   Öffne die heruntergeladene Datei im Download-Ordner oder Dateimanager und bestätige die Installation mit den angezeigten Schritten.

4. **App starten**
   Nach erfolgreicher Installation findest du die App im App-Menü oder auf dem Startbildschirm und kannst sie direkt nutzen.

>[!NOTE] Updates
Hinweis: Da die App nicht über den Play Store installiert wird, musst du Updates manuell durchführen.

---

## App Screen Examples

![light](GCNow_LightMode.png "Light Mode") ![dark mode](GCNow_DarkMode.png "Dark Mode")

***

## 🛠 Development Environment

* **IDE:** Android Studio Ladybug | 2024.2.1 (or latest Meerkat/Koala version), also tested with 2025.1.3 (Narwhal)
* **Language:** Kotlin 2.x
* **UI Framework:** Jetpack Compose with Material 3
* **Build System:** Gradle (Kotlin DSL)
* **Target SDK:** Android 15 (API 35) / Android 16 (Preview)
* **Min SDK:** Android 12 (API 31)

## 🌟 Key Features & Technical Details

### 1. Adaptive & Responsive Layout

The UI uses a dynamic configuration check to switch layouts on the fly.

* **Portrait Mode:** A centered vertical stack (`Column`) optimized for one-handed use.
* **Landscape Mode (90°):** A side-by-side arrangement (`Row`) where the profile image shifts to the left and text data to the right, maximizing the wide aspect ratio of modern mobile displays.

### 2. Intelligent Text Scaling (Anti-Overflow)

To prevent UI breakage with long usernames, the app implements a custom font-scaling algorithm:

* **Default:** 52.sp (up to 10 characters).
* **Medium:** 36.sp (11–15 characters).
* **Small:** 28.sp (15+ characters).

This keeps the brand name on a single line regardless of length.

### 3. Real-Time Engine

The dashboard features a live digital clock (HH:mm:ss) driven by a `LaunchedEffect` coroutine. It polls the system time every 1,000 ms, ensuring minimal battery impact while maintaining precision.

### 4. Advanced Persistence & Permissions

Unlike standard image pickers, **GC Now!** requests **Persistable URI Permissions**. This allows the app to retain access to user-selected gallery images even after a full device reboot without requiring a permanent "All Files Access" permission.

* **Data Storage:** User preferences such as Theme, Text, Background Color, and Image Path are saved using `SharedPreferences` with KTX extensions for clean, asynchronous writing.

### 5. Custom Theming Engine

* **Dual-Tone Support:** Manual toggle for Dark and Light modes.
* **12-Color Palette:** A curated selection of background colors chosen to provide optimal contrast for both white and black transparent PNG assets.
* **High Contrast:** Neon Turquoise (`#00FFCC`) and Bright Yellow accents for maximum legibility.

## 🌍 Navigation Features

GC Now! also includes geocaching-focused navigation features for easier field use.

* **WGS84 Standard Formatting:** Coordinates are displayed in the geocaching-friendly degrees and decimal minutes format: `DD° MM.MMM'`.
* **Live Accuracy Meter:** Displays the GPS signal quality in meters (`+/- X m`) so users can judge the reliability of the zero point.
* **Dynamic Update Interval:** A customizable **GPS Refresh Slider** (1 to 10 seconds) in the settings menu lets users prioritize either fast tracking or lower battery usage.
* **One-Tap Clipboard Copy:** A dedicated **Copy** button next to the coordinates copies the current location in single-line format for use in apps like *c:geo*, *Locus Map*, or *Google Maps*.

## 🛠️ Updated Technical Setup

To support the GPS engine, the following configurations were added:

#### **Permissions:**
The app requires precise location access to calculate WGS84 coordinates:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

#### **Dependencies:**
The app now uses the Google Play Services Location API for efficient power management:
```kotlin
// build.gradle.kts
dependencies {
    implementation("com.google.android.gms:play-services-location:21.2.0")
}
```

## 🔄 Persistence & Settings

The application now remembers the following additional user preferences:

* **GPS Visibility:** Whether the GPS display was active.
* **Interval Frequency:** The preferred refresh rate in seconds across app sessions.

## 📦 Dependencies

Add the following to your `app/build.gradle.kts` file:

```kotlin
dependencies {
    // Material Design 3 and Extended Icons
    implementation("androidx.compose.material:material-icons-extended")

    // Image Loading with Coil (SVG & PNG support)
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Android KTX for cleaner SharedPreferences and URI handling
    implementation("androidx.core:core-ktx:1.12.0")

    // Google Play Services Location
    implementation("com.google.android.gms:play-services-location:21.2.0")
}
```

## 🏗 Build Instructions

### Debug Build

For rapid testing on a physical device:

1. Navigate to `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
2. Locate the file in `app/build/outputs/apk/debug/app-debug.apk`.

### Release Build

For production:

1. Use `Build > Generate Signed Bundle / APK`.
2. Select `APK` and use your `.jks` keystore.
3. Choose the `release` build variant and enable `V2 (Full APK Signature)`.

## 📁 Repository Structure

[Github Repository of GCNow](https://github.com/roseswe/GCNow)

* `MainActivity.kt`: Contains the core UI logic, state management, permission handling, and GPS display logic.
* `AndroidManifest.xml`: Configured for `Edge-to-Edge` display, URI persistence, and location permissions.
* `res/drawable/`: Contains the default `user_image.png` fallback asset.

## 📄 License & Credits

**Initial Build Date:** 30.01.2026

**Developed by:** ROSE_SWE, Ralph Roth

**Current Version:** 2.0

**Build Date:** April 22, 2026

**CVS/SVN Info:** `$Id: README.md,v 1.11 2026/04/22 14:12:43 ralph Exp $`

**(C) 2000-2026 by ROSE_SWE, Ralph Roth. All rights reserved.**