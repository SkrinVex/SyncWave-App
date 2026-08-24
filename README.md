# SyncWave Android App 📱🌊

> **Официальный мобильный клиент SyncWave для Android на базе Jetpack Compose и Media3 (ExoPlayer).**
> *Стриминг, фоновое воспроизведение на экране блокировки, оффлайн-скачивание треков и управление личной медиатекой.*

[![Platform: Android](https://img.shields.io/badge/Platform-Android_7.0+_(API_24+)-3DDC84?style=flat-square&logo=android)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=flat-square&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Media3 ExoPlayer](https://img.shields.io/badge/Media3-ExoPlayer_1.5-FF0033?style=flat-square)]()
[![Design: Obsidian Studio](https://img.shields.io/badge/Theme-Obsidian_Studio-0F0F12?style=flat-square)]()

---

## 🌟 Ключевые возможности клиента

- **⚡ Мгновенный стриминг и воспроизведение**: Интеграция с `Media3 ExoPlayer` и HTTP 206 Partial Content стримингом сервера — моментальный отклик и перемотка.
- **📥 Полноценный оффлайн-режим (YouTube Music Style)**:
  - Скачивание треков в память устройства в фоновом режиме с отображением прогресса и скорости (МБ/с).
  - При отсутствии интернета приложение автоматически открывает медиатеку со скачанными треками без запроса авторизации.
  - Мгновенное переключение фильтром «Скачанные» в медиатеке.
- **🎧 Фоновое воспроизведение и шторка (MediaSessionService)**:
  - Системное уведомление воспроизведения с кнопками управления, перемоткой на ±10 сек, прогресс-баром и отображением обложек.
  - Управление воспроизведением с экрана блокировки, через Bluetooth-наушники и часы.
- **💎 Дизайн Obsidian Studio**:
  - Строгая монохромная темная тема с акцентным фиолетовым и изумрудным статусом.
  - Тактильный аудиоплеер (Bottom Sheet), эквалайзер, регулировка скорости воспроизведения (0.5x – 2.0x), режимы повтора и перемешивания.
- **📦 Массовые операции**:
  - Мультивыбор треков долгим нажатием или кнопкой «Выбрать все».
  - Пакетное скачивание и пакетное удаление треков.
- **📡 Синхронизация и мониторинг**:
  - Вкладка «Синхронизация» с отображением очередей сервера и мобильного устройства, живыми терминальными логами и статусом фоновых задач.

---

## 📐 Архитектура приложения

Приложение построено по канонам **Clean Architecture + MVVM + Unidirectional Data Flow (MVI-style)**:

```
app/src/main/java/com/SkrinVex/syncwave/app/
├── data/
│   ├── local/            # DataStore (сессия, настройки) & DownloadStorage (реестр и локальные аудиофайлы)
│   ├── remote/           # Retrofit API сервис & Interceptors (динамический URL, Bearer Auth)
│   └── repository/       # Реализации репозиториев (Auth, Track, Playlist, Sync, Settings)
├── domain/
│   ├── model/            # Domain модели (Track, Playlist, DownloadTask, User, SyncProgress)
│   ├── repository/       # Интерфейсы репозиториев
│   └── usecase/          # Специфичные сценарии (Авторизация, Воспроизведение, Загрузки)
├── download/             # DownloadManager & DownloadForegroundService (фоновое скачивание)
├── player/               # AudioPlayerManager, SyncWaveMediaService & ExoPlayer
├── di/                   # DependencyContainer (Service Locator / Dependency Injection)
└── ui/
    ├── components/       # Переиспользуемые UI компоненты (StudioCard, StudioBadge, FullPlayer, TrackRow)
    ├── screens/          # Экраны (Auth, Library, Playlists, Sync, Settings, DownloadedTracks)
    └── theme/            # Obsidian Studio палитра, типографика, формы
```

---

## 🛠️ Сборка и запуск локально

### Требования
- **Android Studio Ladybug (2024.2+)** или новее.
- **JDK 17** (рекомендуется Eclipse Temurin или JetBrains Runtime).
- **Android SDK Platform 35** (Android 15) с Build-Tools 35.0.0.

### Команды Gradle

```bash
cd "SyncWave App"

# 1. Проверка компиляции Kotlin:
./gradlew compileDebugKotlin

# 2. Сборка отладочного APK:
./gradlew assembleDebug
# Готовый APK: app/build/outputs/apk/debug/app-debug.apk

# 3. Сборка релизного подписанного APK:
./gradlew assembleRelease \
  -PKEYSTORE_FILE=/path/to/keystore.jks \
  -PKEYSTORE_PASSWORD="ваш_пароль" \
  -PKEY_ALIAS="ваш_алиас" \
  -PKEY_PASSWORD="пароль_ключа"
# Готовый APK: app/build/outputs/apk/release/app-release.apk
```

---

## 🚀 Автоматическая сборка в GitHub Actions

В репозитории настроен автоматический CI/CD воркер: `.github/workflows/android-build.yml`.

- **При каждом push в `main`**: Собирается актуальный APK и публикуется в артефакты сборки.
- **При пуше тега версии (`v1.0.0`)**: Автоматически собирается подписанный релизный APK и создается официальный **GitHub Release** с прикрепленным файлом `syncwave-app-1.0.0.apk`.

Подробная инструкция по настройке ключей и пушу тегов находится в файле:
👉 **[CI/CD & Руководство по релизам (CI_CD_GUIDE.md)](./CI_CD_GUIDE.md)**.

