# PROJECT_STATUS.md

Дата обновления: 2026-08-19

## Текущий этап

**Milestone 1 — DONE (100%). Milestone 2 — M2.1 implementation complete / runtime validation pending.**

M1 принят владельцем и слит в `main`.

M2.1 реализован в ветке `feature/m2-audio-curriculum` и оформлен draft PR #3.

## Статусы M1

- STATIC_REVIEW_STATUS: PASS
- TESTS_CI_STATUS: PASS
- LOCAL_ANDROID_RUNTIME_STATUS: PASS
- PHYSICAL_DEVICE_RUNTIME_STATUS: PASS
- OWNER_ACCEPTANCE_STATUS: ACCEPTED

## Статусы M2.1

- IMPLEMENTATION_STATUS: COMPLETE
- STATIC_REVIEW_STATUS: PASS
- TESTS_CI_STATUS: PASS
- DEBUG_BUILD_STATUS: PASS
- HYBRID_AUDIO_FALLBACK_STATIC_PATH_STATUS: PASS
- LOCAL_ANDROID_RUNTIME_STATUS: PENDING_OWNER_RUN
- PHYSICAL_DEVICE_RUNTIME_STATUS: PENDING_OWNER_RUN
- OWNER_ACCEPTANCE_STATUS: PENDING

GitHub Actions PR #3 подтвердил прохождение JVM unit tests и `assembleDebug` для полного M2.1 diff.

## M2.1 — что реализовано

### Audio

- введён единый интерфейс `AudioPlayer`;
- добавлен `HybridAudioPlayer`;
- стратегия D020: сначала поиск локального `res/raw` asset, затем автоматический TTS fallback;
- отсутствие локального файла штатно ведёт в существующий `TtsAudioPlayer`;
- `ExerciseViewModel` зависит от `AudioPlayer`, а не напрямую от TTS;
- `MainActivity` выполняет простой composition-root/DI;
- в Manifest добавлен `<queries>` для `android.intent.action.TTS_SERVICE`;
- реальные WAV/OGG не добавлены, как и требовалось.

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

JVM tests покрывают:

- состав Levels 1–3;
- накопление старых букв в пуле;
- правила distractor pool;
- генерацию 10 валидных вопросов LearningEngine для каждого Level 1–3;
- unlock 7/10 = false;
- unlock 8/10 = true;
- unlock 10/10 = true;
- запрет unlock для неполной сессии.

CI также подтвердил debug build.

## Runtime boundary

В репозитории намеренно отсутствуют реальные `res/raw` WAV/OGG, поэтому branch использует fallback path. Код статически гарантирует: если `getIdentifier(..., "raw", ...)` возвращает 0, вызывается существующий TTS wrapper.

Фактический запуск новой ветки на emulator/device не выполнялся в GitHub CI и не подменяется build/tests. Для полного runtime acceptance достаточно обычного owner Run; Android Studio Agent нужен только при конкретной ошибке.

## Что ещё не входит в готовность M2 в целом

- реальные pre-recorded WAV/OGG assets;
- runtime-проверка local asset playback после появления файлов;
- retry queue из D019;
- разбивка результата/статистика по каждой букве из D019;
- mastery states;
- weighted selection;
- delayed checks в полном LearningEngine v2;
- сохранение текущего разблокированного уровня в DataStore и UI выбора уровней.

## Следующий этап

M2.2: per-letter statistics на основе Room Attempt history и сохранение простого progression/unlocked-level state в DataStore. Подробно — `NEXT_TASK.md`.
