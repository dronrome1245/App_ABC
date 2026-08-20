# PROJECT_STATUS.md

Дата обновления: 2026-08-21

## Текущий этап

**Milestone 1 — DONE (100%). Milestone 2 — DONE (100%). Milestone 3 — In Planning / Kickoff.**

Milestone 2 официально закрыт после завершения M2.5 Closure Evidence Audit и явной приёмки владельцем. Все 7 критериев M2.5 имеют статус PASS; обязательные переносы D019 закрыты; LearningPolicy v3/D022, Curriculum v2, hybrid audio, per-letter statistics, persistence и adaptive retry/weighting входят в принятый контур M2.

## Milestone 2 — финальные статусы

- MILESTONE_2_STATUS: DONE (100%)
- M2_5_CLOSURE_AUDIT_STATUS: PASS — 7/7
- STATIC_REVIEW_STATUS: PASS
- TESTS_CI_STATUS: PASS
- DEBUG_BUILD_STATUS: PASS
- LOCAL_ANDROID_RUNTIME_STATUS: PASS — OWNER_EVIDENCE
- PHYSICAL_DEVICE_RUNTIME_STATUS: PASS — OWNER_EVIDENCE
- OWNER_ACCEPTANCE_STATUS: ACCEPTED
- MIGRATION_1_2_STATUS: AUTOMATED_TEST_PASS
- MIGRATION_1_2_DEVICE_STATUS: NOT_TESTED
- LEARNING_POLICY_VERSION: 3
- CURRICULUM_VERSION: 2
- DATABASE_SCHEMA_VERSION: 2

`MIGRATION_1_2_DEVICE_STATUS: NOT_TESTED` сохраняется как точное описание отсутствия отдельного device-upgrade прогона schema v1→v2. Это не является `UNKNOWN` для migration-кода: реальный SQLite automated migration test применяет `MIGRATION_1_2`, сохраняет исторические `Attempt` и проверяет backfill `letter_progress`/`session_results`; данный evidence принят в M2.5 Closure Audit.

## M2.5 Closure Evidence Audit — PASS

Проверены и приняты семь контуров evidence:

1. Domain/architecture: учебный алгоритм отделён от UI; LearningPolicy версионируется; thresholds/weights централизованы — PASS.
2. Adaptive learning: mastery states, weighted selection, retry spacing и delayed checks реализованы и покрыты deterministic JVM tests — PASS.
3. Curriculum/session invariants: Levels 1–3, старые буквы в накопленном пуле, distractor != target, 10 вопросов и unlock >=80% сохранены — PASS.
4. Audio: local `res/raw` first, TTS fallback, TTS service visibility и feedback/completion sounds — PASS, включая owner device smoke.
5. D019/session result: per-letter Session Summary и поздний retry ошибочной target-буквы реализованы — PASS.
6. Persistence/migration: Room schema 2, Attempt history, LetterProgress/SessionResult, DataStore progression и automated migration 1→2 backfill test — PASS.
7. Integration/acceptance: static review, JVM tests, `assembleDebug`, CI, physical-device smoke и owner acceptance — PASS.

## Принятый контур M2

- `AudioPlayer` + `HybridAudioPlayer`: pre-recorded local audio first, Android TTS fallback;
- Curriculum v2: Level 1 `А/М`, Level 2 `О/У`, Level 3 `С/Н`, накопленный пул букв;
- 10 вопросов на сессию, level unlock `>=80%` / `8 из 10`;
- Room schema 2, сырые `Attempt`, persistent per-letter/session aggregates;
- D019 per-letter Session Summary;
- LearningPolicy v3 / D022: `INTRODUCED / PRACTICING / MASTERED`;
- adaptive weighted selection, retry queue 2–4 других вопроса, delayed checks spacing >=2;
- deterministic invariant tests и automated Room migration evidence;
- owner smoke M2.3: AUDIO / LEVELS / SUMMARY / PERSISTENCE = PASS.

## Активный этап — Milestone 3 Kickoff

M3 находится только в стадии планирования. До отдельного owner-approved architecture/scope решения код M3 не начинать.

Kickoff должен подготовить архитектурный план для:

- Parent Dashboard / родительского профиля и экрана статистики;
- полного набора метрик по буквам и confusion matrix;
- планируемого расширения Curriculum до полного русского алфавита с отдельным versioned owner decision о порядке/группах;
- долгосрочного spaced repetition между днями/сессиями и необходимых временных метрик;
- границ Room/DataStore и UI для M3 без изменения принятого LearningPolicy v3 до нового решения.
