# Техническая архитектура MVP

Дата: 2026-08-20

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
│   ├── session
│   └── model
└── ui
    ├── home
    ├── exercise
    └── result
```

Не создавать слои только ради шаблона.

## Room

Сырые `Attempt` — детальный источник истины по учебной истории. Производные таблицы не должны заменять или переписывать `Attempt`.

### Schema v2

`databaseSchemaVersion = 2`.

Таблицы:

- `attempts` — неизменяемая подробная история ответов;
- `letters` — curriculum metadata;
- `letter_progress` — производный persistent aggregate для быстрых запросов по букве;
- `session_results` — сводка завершённой сессии, уникальная по `sessionId`.

`LetterProgressEntity` хранит attempts/correct/lastSeen/averageResponseTime. `SessionResultEntity` хранит level, total/correct, passed и completedAt.

`ProgressRepository.finalizeSession()` выполняет обновление `letter_progress` и `session_results` внутри одной Room transaction. Финализация идемпотентна по `sessionId`, чтобы повторное создание ResultScreen не удваивало статистику.

Migration `1 -> 2` не destructive и делает backfill новых таблиц из существующих `attempts`, сохраняя накопленную M1/M2.1 историю.

Attempt должен поддерживать метрики из `SUCCESS_METRICS.md`.

### Версии

Отдельно отслеживать Room `databaseSchemaVersion`, `learningPolicyVersion` и `curriculumVersion`. Не использовать destructive migration для накопленной статистики без явного решения владельца.

## Session

Каждая тренировка имеет `sessionId`, что позволяет отличать внутрисессионное повторение от межсессионного, рассчитывать retention и строить Session Summary.

## DataStore

Используется для текущего/max разблокированного уровня и простых настроек. Подробная история попыток в DataStore не хранится.

## LearningEngine

Чистый Kotlin без Compose и Android Context. Curriculum, LearningPolicy, retry и статистические данные передаются в domain layer; UI не выбирает следующий вопрос самостоятельно.

## LearningPolicy config

Для M2 используется `learningPolicyVersion = 2` и owner-approved level unlock `8/10 = 80%` по D021. Остальные mastery/weighted/retry параметры являются отдельными обязательными slices M2.

## Curriculum config

`curriculumVersion = 2` содержит Levels 1–3 по D021: А/М, затем О/У, затем С/Н; старые буквы остаются в рабочем пуле.

## UI

Compose получает состояние из ViewModel. M2.2 Session Summary показывает общий счёт, pass/fail и D019 per-letter breakdown; агрегация Attempt history не выполняется внутри Composable.

## Audio — D020

UI/ViewModel работает через интерфейс `AudioPlayer` и не зависит напрямую от `TextToSpeech`/`MediaPlayer`.

`HybridAudioPlayer` реализует стратегию:

1. определить имя локального `res/raw` ресурса через `AudioAssetCatalog`;
2. найти Android raw resource;
3. воспроизвести его через `MediaPlayer`;
4. если mapping/resource отсутствует, `MediaPlayer.create()` неудачен или playback завершается ошибкой — использовать существующий `TextToSpeechWrapper` как fallback.

Начиная с M2.3 приложение содержит OGG Vorbis mono 22.05 kHz assets для `А`, `М`, `О`, `У`, `С`, `Н`, а также `sound_correct`, `sound_wrong` и `sound_level_complete`. `ExerciseViewModel` запускает feedback после ответа, `ResultViewModel` — completion cue на валидном Session Summary.

Для TTS service visibility Android Manifest содержит `<queries>` с `android.intent.action.TTS_SERVICE`. `spokenName` хранится в Curriculum, поэтому fallback не зависит от чтения одиночного Unicode-символа.

Качество локального голоса и фактический local-first runtime остаются owner smoke evidence; наличие файлов и mapping подтверждаются кодом/tests/build, но не подменяют проверку на устройстве.

## Speech recognition

Отдельный модуль/feature flag. До M6 приложение не должно запрашивать микрофон без явной необходимости.

## DI

Hilt/Koin не нужен, пока зависимости удобно создаются в `MainActivity`. Composition root создаёт Room repositories, `LevelProgressionStore`, `TtsAudioPlayer` и `HybridAudioPlayer`.

## Сеть

Собственный backend не нужен. По умолчанию нет analytics/crash SDK; их добавление требует отдельного privacy/product решения.

## CI

GitHub Actions проверяет JVM unit tests и debug build.

## Качество

На каждом этапе обязательны доступные unit tests, debug build, ручной runtime для Android-зависимых изменений, соответствие DoD и проверка новых рисков.
