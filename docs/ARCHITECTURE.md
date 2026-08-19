# Техническая архитектура MVP

## Платформа

Android native: Kotlin, Jetpack Compose, Material 3, Gradle Kotlin DSL.

## Ориентировочные пакеты

```text
com.dronrome1245.appabc
├── data
│   ├── db
│   ├── entity
│   └── repository
├── domain
│   ├── model
│   └── learning
├── speech
│   ├── tts
│   └── recognition
└── ui
    ├── home
    ├── training
    ├── result
    ├── statistics
    └── weakletters
```

Не создавать слои только ради шаблона.

## Room

Хранить исходную историю Attempt. `LetterProgress` можно вычислять или кэшировать, но не использовать как единственный источник истории.

## DataStore

Текущий открытый уровень, настройки TTS, onboarding, размер сессии, feature flags.

## LearningEngine

Чистый Kotlin без Compose и Android Context. Получает набор букв, статистику, retry queue и историю последних target'ов; возвращает следующую цель. Должен покрываться unit-тестами.

## UI

Compose получает состояние из ViewModel. Учебную логику не хранить внутри `@Composable`.

## TTS

Отдельный wrapper. Для буквы хранить `symbol` и `spokenName` (`М` / `эм`), чтобы не зависеть от того, как TTS интерпретирует одиночный символ.

## Speech recognition

Отдельный модуль/feature flag. Не смешивать с базовым LearningEngine до тестирования.

## DI

Hilt/Koin на старте не нужен, если зависимости можно передать просто и понятно.

## Сеть

Собственный backend для MVP не нужен.

## Качество

На каждом этапе: unit tests учебного алгоритма, debug build, ручной запуск на реальном телефоне, отсутствие crash в основном сценарии.
