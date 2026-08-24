# SyncWave Android App 📱🌊

> **Официальный мобильный клиент SyncWave для Android на базе Jetpack Compose и Media3 (ExoPlayer).**
> *Стриминг аудио без потерь, фоновое воспроизведение на экране блокировки, оффлайн-скачивание треков, управление медиатекой и прямая интеграция с сервером SyncWave.*

---

[![Platform: Android](https://img.shields.io/badge/Platform-Android_7.0+_(API_24+)-3DDC84?style=flat-square&logo=android)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=flat-square&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Media3 ExoPlayer](https://img.shields.io/badge/Media3-ExoPlayer_1.5-FF0033?style=flat-square)](https://developer.android.com/media/media3)
[![Network](https://img.shields.io/badge/OkHttp_Retrofit-Streaming-009688?style=flat-square)]()
[![Design: Obsidian Studio](https://img.shields.io/badge/Design-Obsidian_Studio-0F0F12?style=flat-square)]()
[![Coolify / Traefik](https://img.shields.io/badge/Coolify-Traefik_Ready-2496ED?style=flat-square&logo=traefik)]()

---

## 🌟 Ключевые возможности клиента

### 1. ⚡ Продвинутый медиа-движок Media3 (ExoPlayer)
- **RFC 7233 HTTP 206 Streaming**: Мгновенный запуск воспроизведения без ожидания полной загрузки трека и плавная перемотка.
- **Двухуровневый кэш (500 МБ LRU)**: Прослушанные треки кэшируются на диске устройства (`SimpleCache`), снижая трафик при повторном воспроизведении.
- **Полноценный фоновый сервис (`SyncWaveMediaService`)**:
  - Системный медиа-контроллер в шторке Android и на экране блокировки (кнопки Play/Pause, треки Вперед/Назад, перемотка на ±10 сек, таймлайн и обложка).
  - Поддержка Bluetooth-гарнитур, умных часов и Android Auto (`MediaSessionCompat`).
  - Умное управление аудиофокусом (автопауза при звонках и уведомлениях с возможностью отключения в настройках).
- **Гибкое управление воспроизведением**:
  - Регулировка темпа (0.25x – 3.0x с быстрыми пресетами `0.75x`, `1.0x`, `1.25x`, `1.5x`, `2.0x`).
  - Режимы зацикливания (Выкл, Вся очередь, Один трек).
  - Честное перемешивание очереди (True Shuffle) с сохранением текущего трека во главе списка.

---

### 2. 📥 Полноценный оффлайн-режим (YouTube Music Style)
- **Фоновый менеджер загрузок (`DownloadForegroundService`)**:
  - Высокоскоростное скачивание аудиопотока через оптимизированный буфер (64 КБ) с использованием системного вызова `sendfile` на сервере.
  - Живой расчет скорости (МБ/с, КБ/с) и оставшегося времени (ETA).
  - Системное уведомление с прогресс-баром и возможностью отмены всей очереди в 1 клик.
- **Offline First**:
  - При отсутствии подключения к серверу или активации тумблера «Оффлайн» приложение мгновенно открывает локальную медиатеку без экрана логина.
  - Локальный реестр треков (`downloaded_tracks_registry.json`) сохраняет метаданные, обложки и форматы.
- **Умная синхронизация оффлайн-базы**:
  - Автоскачивание новых треков, добавленных на сервере.
  - Автоудаление локальных файлов, которые были удалены с сервера.

---

### 3. 🎨 Дизайн Obsidian Studio
- **Строгая OLED-эстетика**: Глубокие оттенки `#0F0F12` и `#16161B`, акцентный индиго/фиолетовый (`#7F52FF`) и изумрудный статус-индикатор (`#10B981`).
- **Тактильный BottomSheet аудиоплеер**:
  - Плавная анимация раскрытия на весь экран.
  - Кастомный плавный скраббер таймлайна (`StudioAudioScrubber`).
  - Анимированный динамический эквалайзер при воспроизведении.
  - Бегущая строка (`MarqueeText`) для длинных названий треков и авторов.
  - Быстрый доступ к очереди треков прямо из полноэкранного плеера.

---

### 4. 📚 Медиатека, Плейлисты и Загрузки
- **Поиск и фильтрация**: Мгновенный поиск по названию, исполнителю или альбому с сортировкой по дате, названию и размеру.
- **Плейлисты**: Создание, редактирование, удаление плейлистов и ручной запуск серверной синхронизации для выбранного плейлиста.
- **Менеджер выгрузки (`UploadManager`)**: Загрузка собственных треков на сервер с выбором файлов и отображением статуса в шторке.
- **Массовые операции**: Пакетный выбор треков долгим нажатием с возможностью массового скачивания или удаления с сервера.

---

### 5. 🔄 Синхронизация и Google Auth
- **Вкладка «Синхронизация»**: Мониторинг фонового процесса скачивания yt-dlp на сервере в реальном времени (SSE-события) с живым терминальным логом.
- **Встроенный Google Auth WebView**: Вход в аккаунт YouTube Music прямо из приложения для безопасного экспорта cookies на сервер в один клик.
- **Динамический адрес сервера**: Мгновенное переключение между локальной сетью (например, `http://192.168.1.50:8080`) и внешним доменом (`https://music.yourdomain.com`).

---

## 📐 Архитектура проекта

Приложение следует принципам **Clean Architecture** и **MVI / MVVM (Unidirectional Data Flow)**:

```
SyncWave App/app/src/main/java/com/SkrinVex/syncwave/app/
├── cookies/              # GoogleAuthManager (WebView для захвата YouTube сессии)
├── data/
│   ├── local/            # SessionDataStore (DataStore Preferences) & DownloadStorage (реестр файлов)
│   ├── remote/           # Retrofit API, DTO модели, Interceptors (Auth, DynamicBaseUrl)
│   └── repository/       # Реализации репозиториев (Auth, Track, Playlist, Sync, Settings)
├── di/                   # DependencyContainer (легковесный Service Locator)
├── domain/
│   ├── model/            # Чистые Domain-модели (Track, Playlist, DownloadTask, User)
│   ├── repository/       # Интерфейсы репозиториев
│   └── usecase/          # Специфические сценарии использования (Use Cases)
├── download/             # DownloadManager & DownloadForegroundService (скачивание треков)
├── player/               # AudioPlayerManager, SyncWaveMediaService (ExoPlayer & MediaSession)
├── upload/               # UploadManager (выгрузка файлов на сервер)
└── ui/
    ├── components/       # StudioCard, StudioBadge, StudioAudioScrubber, MarqueeText, MiniPlayerBar, FullPlayer
    ├── screens/
    │   ├── auth/         # LoginScreen, AuthViewModel
    │   ├── library/      # LibraryScreen, LibraryViewModel
    │   ├── playlists/    # PlaylistsScreen, PlaylistsViewModel
    │   ├── sync/         # SyncScreen, SyncViewModel
    │   └── settings/     # SettingsScreen, SettingsViewModel, DownloadedTracksBottomSheet
    └── theme/            # Obsidian Studio тема (Color, Type, Theme)
```

---

## 🚀 Развертывание сервера (Coolify, Traefik, Docker)

Сервер SyncWave идеально работает в контейнере за **Traefik** в **Coolify** или **Docker Compose**.

### Настройка Traefik в Coolify / Docker Compose

Для достижения максимальной скорости стриминга и отдачи треков без буферизации и троттлинга сервер SyncWave отправляет заголовки `X-Accel-Buffering: no`, `Accept-Ranges: bytes` и `Cache-Control: no-transform`.

Пример конфигурации для **Traefik** (`docker-compose.yml`):

```yaml
services:
  syncwave:
    build: .
    container_name: syncwave
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - SYNCWAVE_DATA_DIR=/data
      - SYNCWAVE_JWT_SECRET=ваш_секретный_ключ
    volumes:
      - ./data:/data
    labels:
      - "traefik.enable=true"
      - "traefik.http.routers.syncwave.rule=Host(`music.yourdomain.com`)"
      - "traefik.http.routers.syncwave.entrypoints=websecure"
      - "traefik.http.routers.syncwave.tls.certresolver=letsencrypt"
      - "traefik.http.services.syncwave.loadbalancer.server.port=8080"
      # Отключаем буферизацию Traefik для мгновенного стриминга:
      - "traefik.http.middlewares.syncwave-buffering.buffering.maxResponseBodyBytes=0"
```

> 💡 **Совет для Cloudflare**: Если домен направлен через Cloudflare, для маршрутов `/api/v1/tracks/*` рекомендуется создать правило *Cache Rules -> Bypass Cache*, либо переключить запись в режим *DNS Only* («серый флажок»), чтобы избежать ограничений на бинарный стриминг.

---

## 🛠️ Сборка и запуск локально

### Системные требования
- **Android Studio Ladybug (2024.2+)** или новее.
- **JDK 17** (рекомендуется Eclipse Temurin или JetBrains Runtime).
- **Android SDK 35 (Android 15)** с Build-Tools 35.0.0.
- Минимальная версия Android на устройстве: **Android 7.0 (API 24)**.

### Команды сборки Gradle

```bash
cd "SyncWave App"

# 1. Проверка компиляции проекта:
./gradlew compileDebugKotlin

# 2. Сборка отладочного APK:
./gradlew assembleDebug
# Готовый файл: app/build/outputs/apk/debug/app-debug.apk

# 3. Сборка подписанного релизного APK:
./gradlew assembleRelease \
  -PKEYSTORE_FILE="/путь/к/keystore.jks" \
  -PKEYSTORE_PASSWORD="ваш_пароль_хранилища" \
  -PKEY_ALIAS="ваш_алиас" \
  -PKEY_PASSWORD="пароль_ключа"
# Готовый файл: app/build/outputs/apk/release/app-release.apk
```

---

## 🔐 Автоматический CI/CD (GitHub Actions)

В репозитории настроен автоматический воркер [android-build.yml](file:///home/skrinvex/projects/SyncWave/SyncWave%20App/.github/workflows/android-build.yml).

- При создании тега версии (`git tag v1.0.0 && git push origin v1.0.0`) воркер автоматически компилирует и подписывает релизный `.apk` и публикует его в **GitHub Releases**.
- Подробная инструкция по добавлению ключей и настройке секретов находится в [CI_CD_GUIDE.md](file:///home/skrinvex/projects/SyncWave/SyncWave%20App/CI_CD_GUIDE.md).

---

## 📄 Лицензия

Проект распространяется под лицензией **MIT**. Подробности в файле `LICENSE`.