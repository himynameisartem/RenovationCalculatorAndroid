# Renovation Calculator Android

Android application for estimating apartment renovation costs, saving estimates, requesting renovation services, downloading the current price list, and using an AI assistant for renovation-related questions.

## Table of Contents

- [Русский](#русский)
- [English](#english)

## Русский

### О проекте

**Калькулятор ремонта** - это Android-приложение для предварительного расчета стоимости ремонта квартиры. Пользователь выбирает помещения, указывает площади, добавляет необходимые работы, получает смету и может сохранить расчет для дальнейшей работы.

### Возможности

- Расчет стоимости ремонта по помещениям, площадям и выбранным работам.
- Загрузка каталога работ и цен из удаленного JSON-источника.
- Выбор работ по категориям и разделам.
- Формирование итоговой сметы.
- Сохранение и просмотр ранее созданных смет.
- Форма заявки на ремонт.
- Экран с контактами компаний.
- Onboarding-подсказки для основных сценариев.
- AI-помощник по вопросам ремонта и услуг компании.
- Ограничение длины сообщения в AI-чате до 500 символов.
- Интеграция с AppMetrica.

### AI-Помощник

AI-чат реализован как floating-компонент поверх главного экрана. Приложение отправляет вопрос пользователя на backend по HTTPS и отображает полученный ответ в интерфейсе.

История AI-диалога не сохраняется между запусками приложения. Сообщения хранятся только в состоянии текущего экрана. Backend-часть RAG-системы вынесена отдельно и отвечает за поиск по базе знаний, работу с векторным хранилищем и генерацию ответа.

### Архитектура

Приложение построено на Kotlin и Jetpack Compose. Основная схема:

```text
Compose UI
  -> ViewModel / Screen State
  -> Repository / Storage / API Client
  -> Remote API / Local Storage
  -> UI State
  -> Compose Recomposition
```

Навигация:

```text
MainActivity
  -> NavHost
  -> Home / Calculator / Works / Final Estimate / Saved Estimates
```

AI-чат:

```text
ChatBubbleOverlay
  -> ChatViewModel
  -> ChatApiClient
  -> FastAPI RAG Backend
  -> AI Answer
```

### Структура проекта

```text
app/src/main/java/com/tekhnologiistroitelstva/renovationcalculator/
  MainActivity.kt
  RenovationCalculatorApp.kt

  data/
    CatalogModels.kt
    CatalogRepository.kt
    EstimateStorage.kt
    SavedEstimateModels.kt
    SavedEstimatesStore.kt

  ui/
    calculator/
    chat/
    finalestimate/
    home/
    models/
    onboarding/
    request/
    savedestimates/
    theme/
    works/

app/src/main/res/
  drawable/
  mipmap-*/
  values/
  xml/
```

### Технологии

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- AndroidX Lifecycle ViewModel
- Kotlin Coroutines
- StateFlow
- HttpURLConnection
- Local file storage
- AppMetrica
- FastAPI backend integration
- RAG backend integration

### Конфигурация

Основные настройки Android-приложения находятся в `app/build.gradle.kts`:

```kotlin
namespace = "com.tekhnologiistroitelstva.renovationcalculator"
applicationId = "com.tekhnologiistroitelstva.renovationcalculator"
versionCode = 1
versionName = "1.0"
```

Название приложения задается в `app/src/main/res/values/strings.xml`:

```xml
<string name="app_name">Калькулятор ремонта</string>
```

Endpoint AI-чата находится в `ChatApiClient.kt`.

### Запуск

1. Открыть проект в Android Studio.
2. Дождаться Gradle Sync.
3. Выбрать конфигурацию `app`.
4. Запустить приложение на эмуляторе или физическом устройстве.

Для работы AI-чата нужен доступ к backend endpoint, указанному в `ChatApiClient.kt`.

### Связанные Проекты

Backend и RAG-пайплайн вынесены в отдельный проект:

- Парсинг сайта и прайса.
- Подготовка документов.
- Chunking.
- Embeddings.
- Qdrant vector store.
- FastAPI endpoint для мобильных приложений.
- Интеграция с LLM.

## English

### Overview

**Renovation Calculator** is an Android application for preliminary apartment renovation cost estimation. Users can select rooms, enter areas, add required renovation work items, generate an estimate, and save it for later use.

### Features

- Renovation cost estimation based on rooms, areas, and selected work items.
- Remote JSON catalog and price loading.
- Work item selection by categories and sections.
- Final estimate generation.
- Saved estimates with later viewing.
- Renovation request form.
- Company contacts screen.
- Onboarding hints for key user flows.
- AI assistant for renovation and company service questions.
- 500-character user message limit for AI chat.
- AppMetrica integration.

### AI Assistant

The AI chat is implemented as a floating component on top of the home screen. The app sends the user's question to a HTTPS backend and renders the generated answer in the chat UI.

AI conversation history is not persisted between app launches. Messages are kept only in the current screen state. The RAG backend is implemented separately and handles knowledge retrieval, vector search, and answer generation.

### Architecture

The app is built with Kotlin and Jetpack Compose. Main structure:

```text
Compose UI
  -> ViewModel / Screen State
  -> Repository / Storage / API Client
  -> Remote API / Local Storage
  -> UI State
  -> Compose Recomposition
```

Navigation:

```text
MainActivity
  -> NavHost
  -> Home / Calculator / Works / Final Estimate / Saved Estimates
```

AI chat:

```text
ChatBubbleOverlay
  -> ChatViewModel
  -> ChatApiClient
  -> FastAPI RAG Backend
  -> AI Answer
```

### Project Structure

```text
app/src/main/java/com/tekhnologiistroitelstva/renovationcalculator/
  MainActivity.kt
  RenovationCalculatorApp.kt

  data/
    CatalogModels.kt
    CatalogRepository.kt
    EstimateStorage.kt
    SavedEstimateModels.kt
    SavedEstimatesStore.kt

  ui/
    calculator/
    chat/
    finalestimate/
    home/
    models/
    onboarding/
    request/
    savedestimates/
    theme/
    works/

app/src/main/res/
  drawable/
  mipmap-*/
  values/
  xml/
```

### Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- AndroidX Lifecycle ViewModel
- Kotlin Coroutines
- StateFlow
- HttpURLConnection
- Local file storage
- AppMetrica
- FastAPI backend integration
- RAG backend integration

### Configuration

Main Android application settings are located in `app/build.gradle.kts`:

```kotlin
namespace = "com.tekhnologiistroitelstva.renovationcalculator"
applicationId = "com.tekhnologiistroitelstva.renovationcalculator"
versionCode = 1
versionName = "1.0"
```

The application name is configured in `app/src/main/res/values/strings.xml`:

```xml
<string name="app_name">Калькулятор ремонта</string>
```

The AI chat endpoint is configured in `ChatApiClient.kt`.

### Running Locally

1. Open the project in Android Studio.
2. Wait for Gradle Sync to finish.
3. Select the `app` run configuration.
4. Run the app on an emulator or a physical device.

The AI chat requires access to the backend endpoint configured in `ChatApiClient.kt`.

### Related Projects

The backend and RAG pipeline are implemented as a separate project:

- Website and price list parsing.
- Document preparation.
- Chunking.
- Embedding generation.
- Qdrant vector store.
- FastAPI endpoint for mobile applications.
- LLM integration.
