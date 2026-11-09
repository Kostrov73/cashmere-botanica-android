<div align="center">

# Cashmere Botanica 🌿

**A little more care. A little more green.**  
**Немного больше заботы. Немного больше зелени.**

[![Version](https://img.shields.io/badge/version-1.8.0-71856B)](#release-notes--история-версий)
[![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)](#)
[![Language](https://img.shields.io/badge/language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](#)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4)](#)

**An elegant companion for everyday plant care.**  
**Элегантный помощник для ежедневного ухода за растениями.**

</div>

---

## About / О проекте

**EN**  
Cashmere Botanica is an Android app for organizing houseplant care. Keep a collection of plants, plan watering and fertilizing, track completed care, and review everything in a calendar and history. The app is designed to work locally on the device; plant and care records are stored in a Room database.

**RU**  
Cashmere Botanica — Android-приложение для организации ухода за комнатными растениями. Создавайте коллекцию растений, планируйте полив и подкормку, отмечайте выполненные процедуры и просматривайте их в календаре и истории. Приложение рассчитано на локальную работу: сведения о растениях и процедурах хранятся на устройстве в базе Room.

## Features / Возможности

| English | Русский |
|---|---|
| Plant collection with photos and care settings | Коллекция растений с фотографиями и настройками ухода |
| Watering and fertilizing schedules | Расписание полива и подкормки |
| Automatic planning of the next care event | Автоматическое планирование следующей процедуры |
| Planned date and actual completion date | Плановая дата и фактическая дата выполнения |
| Monthly care calendar | Месячный календарь ухода |
| Care history and monthly statistics | История процедур и месячная статистика |
| Local reminders and notification time settings | Локальные напоминания и настройка времени уведомлений |
| Local-first storage; no account required | Локальное хранение данных; аккаунт не требуется |

## Screenshots / Скриншоты

Screenshots will be added here. / Скриншоты приложения будут добавлены сюда.

<!-- Suggested layout once real screenshots are available:
<p align="center">
  <img src="docs/screenshots/home.png" width="24%" alt="Home screen" />
  <img src="docs/screenshots/calendar.png" width="24%" alt="Calendar" />
  <img src="docs/screenshots/history.png" width="24%" alt="Care history" />
  <img src="docs/screenshots/plant-details.png" width="24%" alt="Plant details" />
</p>
-->

## Tech stack / Технологии

- Kotlin
- Jetpack Compose + Material 3
- AndroidX Room (local database)
- DataStore Preferences
- WorkManager (scheduled reminders)
- Coil (image loading)
- KSP

## Requirements / Требования

- Android Studio with Android SDK Platform 35
- JDK 17
- Android 8.0 (API 26) or newer

Project configuration: `minSdk = 26`, `targetSdk = 35`, `compileSdk = 35`.

## Getting started / Запуск проекта

1. Clone or download this repository. / Клонируйте репозиторий или скачайте его архив.
2. Open the repository root (the folder containing `settings.gradle.kts`) in Android Studio. / Откройте в Android Studio корневую папку проекта — там находится `settings.gradle.kts`.
3. Allow Gradle to sync and download dependencies. / Дождитесь синхронизации Gradle и загрузки зависимостей.
4. Connect an Android device or start an emulator running Android 8.0 or newer. / Подключите устройство или запустите эмулятор с Android 8.0 или новее.
5. Run the `app` configuration. / Запустите конфигурацию `app`.

To build an APK in Android Studio, choose **Build → Build Bundle(s) / APK(s) → Build APK(s)**.  
Чтобы собрать APK в Android Studio, выберите **Build → Build Bundle(s) / APK(s) → Build APK(s)**.

> **Build note / Примечание о сборке:** this repository currently does not include the Gradle Wrapper (`gradlew`). Use an Android Studio installation with a compatible local Gradle setup, or add and verify the Gradle Wrapper before relying on command-line/CI builds. / В репозитории пока нет Gradle Wrapper (`gradlew`). Для сборки используйте совместимую локальную конфигурацию Gradle в Android Studio либо добавьте и проверьте Wrapper перед сборкой из командной строки или CI.

## Project structure / Структура проекта

```text
CashmereBotanica/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── java/com/greenhome/app/
│       │   ├── data/                 # Room models, DAO and preferences
│       │   ├── notifications/        # WorkManager reminder worker
│       │   ├── GreenHomeApp.kt       # Database setup and migration
│       │   ├── GreenHomeViewModel.kt # App state and care scheduling
│       │   └── MainActivity.kt       # Compose UI
│       └── res/                      # App resources and launcher icon
├── .github/                          # GitHub templates and metadata
├── README.md
├── CHANGELOG.md
└── settings.gradle.kts
```

## Data & privacy / Данные и конфиденциальность

**EN**  
Plant and care information is stored locally using Room. Notification scheduling is handled on-device with WorkManager. The project does not currently include account-based cloud synchronization.

**RU**  
Информация о растениях и уходе хранится локально с помощью Room. Напоминания планируются на устройстве через WorkManager. На данный момент в проекте нет облачной синхронизации через аккаунт.

## Release notes / История версий

See [CHANGELOG.md](CHANGELOG.md) for the project change history.  
История изменений находится в [CHANGELOG.md](CHANGELOG.md).

Current release: **v1.8.0**.

## Contributing / Участие в разработке

Bug reports and feature ideas are welcome. Please use the GitHub issue templates and include your Android version, steps to reproduce, and screenshots where useful.  
Сообщения об ошибках и идеи новых функций приветствуются. Используйте шаблоны Issues и указывайте версию Android, шаги воспроизведения и скриншоты, если они помогают понять проблему.

## License / Лицензия

No open-source license has been selected for this repository yet. Unless a license is added, the source is published for viewing but reuse, redistribution, and modification are not granted.  
Для репозитория пока не выбрана открытая лицензия. Пока лицензия не добавлена, исходный код доступен для просмотра, но права на повторное использование, распространение и изменение не предоставляются.

---

📲 Download Cashmere Botanica

Latest version: 1.8.0

[⬇️ Download APK for Android](https://github.com/Kostrov73/cashmere-botanica-android/releases/latest)

Download the latest version of Cashmere Botanica directly from GitHub Releases.

Android application. Installation from outside Google Play may require permission to install unknown apps.

---

<div align="center">

Made with care for greener homes. 🌱  
Создано с заботой о зелёных домах. 🌱

</div>
