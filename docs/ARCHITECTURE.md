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
├── core
│   └── audio
├── data
│   ├── local
│   └── repository
├── domain
│   ├── curriculum
│   ├── engine
│   ├── learning
│   └── model
└── ui
    ├── home
    ├── exercise
    └── result
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
- exerciseType при необходимости;
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

- текущего/max разблокированного уровня;
- настроек озвучки;
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

Для M2.1 создан централизованный `LearningPolicyConfig` с `learningPolicyVersion = 2` и owner-approved level unlock `8/10 = 80%` по D021.

Остальные mastery/weighted/retry параметры реализуются последующими slices M2 и требуют сохранения version discipline.

## Curriculum config

Curriculum хранится централизованно в pure Kotlin domain-модели.

Для каждой буквы минимум:

- symbol;
- spokenName;
- stageIntroduced;
- confusableWith/preferredDistractors при необходимости.

`curriculumVersion = 2` содержит утверждённые Levels 1–3 по D021. Старые буквы входят в пул следующего уровня.

## UI

Compose получает состояние из ViewModel.

Учебную логику не хранить внутри `@Composable`.

Позиция вариантов ответа должна перемешиваться независимо от target, чтобы UI не становился скрытой подсказкой.

Детский и родительский потоки логически разделить, но не усложнять навигацию.

## Audio — D020

UI/ViewModel работает через интерфейс `AudioPlayer` и не зависит напрямую от `TextToSpeech`/`MediaPlayer`.

`HybridAudioPlayer` реализует стратегию:

1. определить ожидаемое имя локального ресурса для буквы;
2. найти asset в `res/raw`;
3. если asset существует — воспроизвести его через Android `MediaPlayer`;
4. если asset отсутствует или воспроизведение завершается ошибкой — использовать существующий `TextToSpeechWrapper` как fallback.

Пока реальные WAV/OGG не добавлены, fallback является штатным поведением, а не исключением.

Для TTS service visibility Android Manifest содержит `<queries>` с `android.intent.action.TTS_SERVICE`.

Для буквы `spokenName` хранится в Curriculum (`М` / `эм`), чтобы fallback не зависел от произношения одиночного Unicode-символа.

Низкоуровневый `TtsAudioPlayer` не удаляется и остаётся fallback engine.

## Speech recognition

Отдельный модуль/feature flag. Не смешивать с базовым LearningEngine до тестирования.

До M6 приложение не должно запрашивать микрофон без явной необходимости.

## DI

Hilt/Koin на старте не нужен, если зависимости можно передать просто и понятно.

Текущий composition root — `MainActivity`: там создаются repository, `TtsAudioPlayer` и `HybridAudioPlayer`, после чего интерфейс `AudioPlayer` передаётся в ViewModel.

Подключить DI framework позднее только если он реально уменьшит сложность.

## Сеть

Собственный backend для MVP не нужен.

По умолчанию нет analytics/crash SDK. Их добавление требует отдельного privacy/product решения.

## CI

GitHub Actions должен проверять:

- JVM unit tests;
- debug build;
- позднее lint при необходимости.

## Качество

На каждом этапе:

- unit tests учебного алгоритма;
- debug build;
- ручной запуск при runtime-зависимых изменениях;
- отсутствие crash в основном сценарии;
- соответствие `DEFINITION_OF_DONE.md`;
- проверка новых рисков по `RISK_REGISTER.md`.
