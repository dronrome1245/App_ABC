# PROJECT_STATUS.md

Дата обновления: 2026-08-19

## Текущий этап

**Milestone 1 — DONE (100%). Milestone 2 — M2.1 implementation / validation.**

M1 принят владельцем и слит в `main`.

M2.1 реализуется в ветке `feature/m2-audio-curriculum`.

## Статусы M1

- STATIC_REVIEW_STATUS: PASS
- TESTS_CI_STATUS: PASS
- LOCAL_ANDROID_RUNTIME_STATUS: PASS
- PHYSICAL_DEVICE_RUNTIME_STATUS: PASS
- OWNER_ACCEPTANCE_STATUS: ACCEPTED

## M2.1 — что реализовано

### Audio

- введён единый интерфейс `AudioPlayer`;
- добавлен `HybridAudioPlayer`;
- стратегия D020: сначала поиск локального `res/raw` asset, затем автоматический TTS fallback;
- отсутствие локального файла не является ошибкой: используется существующий `TtsAudioPlayer`;
- `ExerciseViewModel` зависит от `AudioPlayer`, а не напрямую от TTS;
- `MainActivity` выполняет простую composition-root/DI-сборку зависимостей;
- в Manifest добавлен `<queries>` для `android.intent.action.TTS_SERVICE`;
- реальные WAV/OGG пока не добавлены, как и требовалось.

### Curriculum v2

По owner decision D021:

- Level 1: вводятся `А`, `М`; пул `А/М`;
- Level 2: вводятся `О`, `У`; пул `А/М/О/У`;
- Level 3: вводятся `С`, `Н`; пул `А/М/О/У/С/Н`;
- 10 вопросов в каждой сессии;
- distractors формируются из доступного изученного пула без совпадения с target;
- `curriculumVersion = 2`;
- Room seed синхронизируется с утверждёнными Levels 1–3 без переписывания истории Attempt.

### LearningPolicy v2 — level unlock

По D021:

- следующая ступень открывается после полной сессии из 10 вопросов;
- accuracy >=80%;
- минимум `8/10` правильных;
- 7/10 не открывает уровень;
- неполная сессия не открывает уровень;
- `learningPolicyVersion = 2`;
- исторические Attempt сохраняют свои версии.

## Автоматические проверки M2.1

Добавлены JVM tests на:

- состав Levels 1–3;
- накопление старых букв в пуле;
- правила distractor pool;
- генерацию 10 валидных вопросов LearningEngine для каждого Level 1–3;
- unlock 7/10 = false;
- unlock 8/10 = true;
- unlock 10/10 = true;
- запрет unlock для неполной сессии.

Текущий branch CI status обновляется после GitHub Actions run.

## Что ещё не входит в готовность M2 в целом

- реальные pre-recorded WAV/OGG assets;
- runtime-проверка локального asset playback после появления файлов;
- retry queue из D019;
- разбивка результата/статистика по каждой букве из D019;
- mastery states;
- weighted selection;
- delayed checks в полном LearningEngine v2;
- сохранение текущего разблокированного уровня в DataStore и UI выбора уровней.

## Следующий этап

После успешного CI M2.1 следующая задача — M2.2: per-letter statistics на основе Room Attempt history и сохранение простого состояния progression/unlocked level в DataStore. Подробно — `NEXT_TASK.md`.

Android Studio Agent по умолчанию не требуется. Он нужен только при конкретной runtime/audio/TTS/Manifest проблеме.
