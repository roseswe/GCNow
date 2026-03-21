// @(#) $Id: README.md,v 1.4 2026/03/21 21:00:07 ralph Exp $

# GC-Now App!

# GC Now! – ReadMe – Geocache Helper Tool
**GC Now!** is a fast, adaptive dashboard app designed for Android 12 and above. With a strong focus on high-contrast visibility and extensive user customization, it provides a reliable template for real-time data display and personal branding on mobile devices.

The main goal was to create an Android app that shows your current date and time together with your geocaching nickname — and, if available, your personal geocache QR code, image, or logo.

Beyond simply switching between light and dark mode, **GC Now!** lets you fully personalize your experience: adjust your geocaching nickname, upload your own picture, and choose a custom background color to make it truly yours.



# GC Now! – Liesmich – Geocache-Helfer-App
**GC Now!** ist eine schnelle, anpassungsfähige Dashboard-App für Android 12 und höher. Mit besonderem Fokus auf kontrastreiche Darstellung und umfassende Benutzeranpassung bietet sie eine zuverlässige Vorlage für Echtzeit-Datenanzeige und persönliches Branding auf mobilen Geräten.

Das Ziel war es, eine Android-App zu entwickeln, die das aktuelle Datum und die Uhrzeit zusammen mit deinem Geocaching-Nickname anzeigt – sowie, falls vorhanden, deinen persönlichen Geocache-QR-Code, dein Bild oder dein Logo.

Neben dem einfachen Wechsel zwischen Hell- und Dunkelmodus kannst du in **GC Now!** alles nach deinen Wünschen gestalten: deinen Geocaching-Nicknamen anpassen, ein eigenes Bild hochladen und die Hintergrundfarbe frei wählen – damit die App perfekt zu dir passt.

## App Screen Examples

![light](GCNow_LightMode.png "Light Mode") ![dark mode](GCNow_DarkMode.png  "Dark Mode")


***


## 🛠 Development Environment

* **IDE:** Android Studio Ladybug | 2024.2.1 (or latest Meerkat/Koala version)
* **Language:** Kotlin 2.x
* **UI Framework:** Jetpack Compose with Material 3
* **Build System:** Gradle (Kotlin DSL)
* **Target SDK:** Android 15 (API 35) / Android 16 (Preview)
* **Min SDK:** Android 12 (API 31)

## 🌟 Key Features & Technical Details

### 1. Adaptive & Responsive Layout

The UI utilizes a dynamic configuration check to switch layouts on the fly.

* **Portrait Mode:** A centered vertical stack (`Column`) optimized for one-handed use.
* **Landscape Mode (90°):** A side-by-side arrangement (`Row`) where the profile image shifts to the left and text data to the right, maximizing the wide aspect ratio of modern mobile displays.

### 2. Intelligent Text Scaling (Anti-Overflow)

To prevent UI breakage with long usernames, the app implements a custom font-scaling algorithm:

* **Default:** 52.sp (up to 10 characters).
* **Medium:** 36.sp (11–15 characters).
* **Small:** 28.sp (15+ characters).
This ensures the brand name remains on a single line regardless of length.

### 3. Real-Time Engine

The dashboard features a live digital clock (HH:mm:ss) driven by a `LaunchedEffect` coroutine. It polls the system time every 1,000ms, ensuring minimal battery impact while maintaining precision.

### 4. Advanced Persistence & Permissions

Unlike standard image pickers, **GC Now!** requests **Persistable URI Permissions**. This allows the app to retain access to user-selected gallery images even after a full device reboot without requiring a permanent "All Files Access" permission.

* **Data Storage:** All user preferences (Theme, Text, Background Color, Image Path) are saved using `SharedPreferences` with KTX extensions for clean, asynchronous writing.

### 5. Custom Theming Engine

* **Dual-Tone Support:** A manual toggle for Dark and Light modes.
* **12-Color Palette:** A curated selection of background colors specifically chosen to provide optimal contrast for both white and black transparent PNG assets.
* **High Contrast:** Neon Turquoise (`#00FFCC`) and Bright Yellow accents for maximum legibility.

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
}

```
GC-Now App
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

* `MainActivity.kt`: Contains the Core UI logic, State Management, and Permission Handling.
* `AndroidManifest.xml`: Configured for `Edge-to-Edge` display and URI persistence.
* `res/drawable/`: Contains the default `user_image.png` (Fallback asset).

---

### 📄 License & Credits

**Initial Build Date:** 30.01.2026

**Developed by:** ROSE_SWE, Ralph Roth

**(C) 2000-2026 by ROSE_SWE, Ralph Roth. All rights reserved.**

