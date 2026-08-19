# Техническая архитектура MVP

Дата: 2026-08-19

## Платформа

Android native:

- Kotlin;
- Jetpack Compose;
- Material 3;
- Gradle Kotlin DSL.

Изначально не проектируем кроссплатформенный слой.

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
    ├── parent
    └── weakletters
```

Не создавать слои только ради шаблона.

## Room

Хранить исходную историю Attempt. `LetterProgress` можно вычислять или кэшировать, но не использовать как единственный источник истории.

Attempt должен поддерживать метрики из `SUCCESS_METRICS.md`.

Минимальные поля:

- id;
- timestamp;
- sessionId;
- levelId;
- exerciseType;
- targetLetter;
- selectedLetter;
- isCorrect;
- responseTimeMs;
- learningPolicyVersion;
- curriculumVersion.

### Версии

Отдельно отслеживать:

- Room `databaseSchemaVersion`;
- `learningPolicyVersion`;
- `curriculumVersion`.

Не использовать destructive migration для накопленной реальной статистики без явного решения владельца.

После появления стабильной схемы migrations должны иметь тесты.

## Session

Каждая тренировка имеет `sessionId`.

Это позволяет:

- отличать внутрисессионное повторение от межсессионного;
- измерять реальные интервалы;
- рассчитывать delayed retention;
- анализировать усталость внутри сессии.

Отдельная таблица Session допустима, если реально нужна для start/end timestamps и session summary; не создавать её только ради архитектурной симметрии.

## DataStore

Подходит для:

- текущего открытого уровня;
- настроек TTS;
- onboarding;
- размера рекомендуемой сессии;
- feature flags;
- Parent mode settings, если это простые значения.

Не хранить в DataStore подробную историю попыток.

## LearningEngine

Чистый Kotlin без Compose и Android Context.

Получает:

- набор букв curriculum;
- LearningPolicy config;
- статистику/историю;
- retry queue;
- последние target'ы;
- источник случайности.

Возвращает следующую цель/структуру задания.

В unit tests должен поддерживаться фиксированный seed/random provider для воспроизводимости.

## LearningPolicy config

Пороги и веса не должны быть разбросаны по UI/классам.

Они должны находиться в централизованной конфигурации, связанной с `learningPolicyVersion`.

## Curriculum config

Для каждой буквы хранить минимум:

- symbol;
- spokenName;
- stageIntroduced;
- confusableWith/preferredDistractors при необходимости.

Политика описана в `CURRICULUM.md`.

## UI

Compose получает состояние из ViewModel.

Учебную логику не хранить внутри `@Composable`.

Позиция вариантов ответа должна перемешиваться независимо от target, чтобы UI не становился скрытой подсказкой.

Детский и родительский потоки логически разделить, но не усложнять навигацию.

## TTS

Отдельный wrapper.

Для буквы хранить `symbol` и `spokenName` (`М` / `эм`), чтобы не зависеть от того, как TTS интерпретирует одиночный символ.

Каждый spokenName до массового включения проверяется на реальном устройстве.

## Speech recognition

Отдельный модуль/feature flag. Не смешивать с базовым LearningEngine до тестирования.

До M6 приложение не должно запрашивать микрофон без явной необходимости.

## DI

Hilt/Koin на старте не нужен, если зависимости можно передать просто и понятно. Подключить DI позднее только если он реально уменьшит сложность.

## Сеть

Собственный backend для MVP не нужен.

По умолчанию нет analytics/crash SDK. Их добавление требует отдельного privacy/product решения.

## CI

После создания Gradle-проекта добавить GitHub Actions:

- unit tests;
- debug build;
- позднее lint при необходимости.

До появления Android-проекта workflow не создавать, чтобы CI не был заведомо красным.

## Качество

На каждом этапе:

- unit tests учебного алгоритма;
- debug build;
- ручной запуск на реальном телефоне;
- отсутствие crash в основном сценарии;
- соответствие `DEFINITION_OF_DONE.md`;
- проверка новых рисков по `RISK_REGISTER.md`.
