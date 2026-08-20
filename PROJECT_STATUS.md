# PROJECT_STATUS.md

Дата обновления: 2026-08-20

## Текущий этап

**Milestone 1 — DONE (100%). Milestone 2 — M2.2 ACTIVE / KICKOFF.**

M1 принят владельцем и слит в `main`.

M2.1 реализован и слит в `main` через PR #3. Автоматические gate M2.1 пройдены; повторная runtime-проверка исправленного progression остаётся отдельным evidence для итогового M2 acceptance и не блокирует начало следующего slice M2.2.

## Статусы M2.1

- MERGE_STATUS: MERGED_TO_MAIN
- IMPLEMENTATION_STATUS: COMPLETE
- STATIC_REVIEW_STATUS: PASS
- TESTS_CI_STATUS: PASS
- DEBUG_BUILD_STATUS: PASS
- HYBRID_AUDIO_FALLBACK_STATIC_PATH_STATUS: PASS
- LEVEL_PROGRESSION_RUNTIME_WIRING_STATUS: IMPLEMENTED
- LOCAL_ANDROID_RUNTIME_STATUS: PENDING_OWNER_RECHECK
- PHYSICAL_DEVICE_RUNTIME_STATUS: PENDING_OWNER_RECHECK
- OWNER_ACCEPTANCE_STATUS: PENDING_FOR_M2

GitHub Actions PR #3 подтвердил прохождение JVM unit tests и `assembleDebug` после исправления runtime wiring уровней.

## M2.1 — реализовано и находится в main

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

- unlocked/selected level хранится в Preferences DataStore;
- Home показывает кнопки разблокированных уровней;
- выбранный `levelId` передаётся в `exercise/{levelId}`;
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

## Активный slice — M2.2

Цель M2.2: per-letter statistics на основе Room Attempt history и разбивка результата по каждой букве, закрывающая соответствующий перенос D019.

В M2.2 необходимо:

- агрегировать attempts/correct/accuracy по каждой букве;
- добавить recent errors и разумный response-time summary;
- вывести per-letter breakdown на экране результата;
- использовать Room Attempt как единственный источник подробной истории;
- переиспользовать существующий `LevelProgressionStore`, не создавать второй progression state;
- добавить JVM unit tests агрегации;
- не переписывать исторические Attempt и их `learningPolicyVersion` / `curriculumVersion`.

Подробный scope — `NEXT_TASK.md`.

## Что ещё остаётся обязательным в M2 после M2.2

- retry queue из D019;
- mastery states;
- weighted selection;
- delayed checks;
- реальные pre-recorded WAV/OGG assets;
- runtime-проверка local asset playback после появления файлов.

## Runtime boundary

Первый owner Run M2.1 выявил старую M1 runtime-привязку; она устранена и изменения слиты после зелёного CI. Повторный Run исправленного progression всё ещё нужен как runtime evidence перед итоговым закрытием M2, но не используется как препятствие для последовательной разработки M2.2 внутри того же milestone.
