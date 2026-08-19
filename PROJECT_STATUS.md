# PROJECT_STATUS.md

Дата обновления: 2026-08-20

## Текущий этап

**Milestone 1 — DONE (100%). Milestone 2 — M2.1 implementation complete / owner runtime re-check pending.**

M1 принят владельцем и слит в `main`.

M2.1 реализован в ветке `feature/m2-audio-curriculum` и оформлен draft PR #3.

## Статусы M2.1

- IMPLEMENTATION_STATUS: COMPLETE
- STATIC_REVIEW_STATUS: PASS
- TESTS_CI_STATUS: PASS
- DEBUG_BUILD_STATUS: PASS
- HYBRID_AUDIO_FALLBACK_STATIC_PATH_STATUS: PASS
- LEVEL_PROGRESSION_RUNTIME_WIRING_STATUS: IMPLEMENTED
- LOCAL_ANDROID_RUNTIME_STATUS: PENDING_OWNER_RECHECK
- PHYSICAL_DEVICE_RUNTIME_STATUS: PENDING_OWNER_RECHECK
- OWNER_ACCEPTANCE_STATUS: PENDING

GitHub Actions PR #3 подтвердил прохождение JVM unit tests и `assembleDebug` после исправления runtime wiring уровней.

## M2.1 — что реализовано

### Audio

- единый интерфейс `AudioPlayer`;
- `HybridAudioPlayer`;
- стратегия D020: локальный `res/raw` asset first, затем TTS fallback;
- отсутствие локального файла штатно ведёт в существующий `TtsAudioPlayer`;
- `ExerciseViewModel` зависит от `AudioPlayer`;
- в Manifest добавлен `<queries>` для `android.intent.action.TTS_SERVICE`;
- реальные WAV/OGG пока не добавлены.

### Curriculum v2

По D021:

- Level 1: `А`, `М`; пул `А/М`;
- Level 2: добавляются `О`, `У`; пул `А/М/О/У`;
- Level 3: добавляются `С`, `Н`; пул `А/М/О/У/С/Н`;
- 10 вопросов в каждой сессии;
- `curriculumVersion = 2`;
- Room seed содержит утверждённые Levels 1–3 без переписывания Attempt history.

### Runtime progression

Исправлен найденный при owner Run разрыв между domain Curriculum и UI/runtime:

- Home больше не использует `M1SessionConfig.letters`;
- unlocked/selected level хранится в Preferences DataStore;
- Home показывает кнопки всех разблокированных уровней;
- выбранный `levelId` передаётся в route `exercise/{levelId}`;
- `ExerciseViewModel` строит `LearningEngine` из `ApprovedCurriculum.curriculum.lettersAvailableAt(levelId)`;
- Attempt сохраняет фактический `levelId`;
- после полной сессии >=80% (`8/10`) Result открывает и автоматически выбирает следующий уровень;
- экран результата сообщает об открытии нового уровня.

### LearningPolicy v2 — level unlock

- полная сессия: 10 вопросов;
- accuracy >=80%;
- 8/10 и выше открывает следующий уровень;
- 7/10 и неполная сессия не открывают уровень;
- `learningPolicyVersion = 2`.

## Автоматические проверки

JVM tests и debug build проходят в GitHub Actions. Domain tests покрывают Levels 1–3, accumulated pools, distractors и unlock boundaries.

## Runtime boundary

Первый owner Run выявил старую M1 runtime-привязку; она устранена в PR #3. Новый head после исправления ещё требуется повторно запустить владельцу обычной кнопкой Run. До этого runtime gate не считается закрытым.

## Что ещё не входит в готовность M2 в целом

- реальные pre-recorded WAV/OGG assets;
- runtime-проверка local asset playback после появления файлов;
- retry queue из D019;
- разбивка результата/статистика по каждой букве из D019;
- mastery states;
- weighted selection;
- delayed checks.

## Следующий этап

M2.2: per-letter statistics на основе Room Attempt history и разбивка результата по каждой букве. Progression/unlocked-level DataStore уже реализован в M2.1. Подробно — `NEXT_TASK.md`.
