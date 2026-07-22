# Project Structure
```plaintext
rsud-android-client/
├── .github/workflows/          # CI/CD khusus Android (Build APK/AAB)
├── .gitignore                  # Gitignore standar Android Studio
├── README.md
├── app/                        # Source code Android
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/rsud/kebersihan/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   └── build.gradle
├── gradle/
├── build.gradle                # Project level gradle
└── settings.gradle
```