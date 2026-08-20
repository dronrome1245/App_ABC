# PROJECT_STATUS.md

Дата обновления: 2026-08-20

## Текущий этап

**Milestone 1 — DONE (100%). Milestone 2 — M2.3 OWNER SMOKE PASS / M2.4 KICKOFF.**

M2.1 и M2.2 находятся в `main`. M2.3 реализован в `feature/m2-3-audio-assets-smoke` / PR #5 и проверен владельцем на реальном Android-устройстве.

M2 целиком пока не закрыт: по `docs/DEFINITION_OF_DONE.md` остаются обязательными retry queue, mastery states, weighted selection, delayed checks/weak-letter weighting, тесты инвариантов и Milestone Closure Evidence Audit.

## Статусы M2.3

- M2_2_MERGE_STATUS: MERGED_TO_MAIN
- M2_3_IMPLEMENTATION_STATUS: COMPLETE
- STATIC_REVIEW_STATUS: PASS
- TESTS_CI_STATUS: PASS
- DEBUG_BUILD_STATUS: PASS
- AUDIO_RESOURCE_BUILD_VALIDATION_STATUS: PASS
- AUDIO_ASSETS_STATUS: IMPLEMENTED
- CURRICULUM_V2_LOCAL_AUDIO_MAPPING_STATUS: IMPLEMENTED
- ANSWER_FEEDBACK_AUDIO_STATUS: IMPLEMENTED
- SESSION_COMPLETE_AUDIO_STATUS: IMPLEMENTED
- TTS_FALLBACK_STATUS: PRESERVED
- DATABASE_SCHEMA_VERSION: 2 (UNCHANGED)
- SESSION_RULE: 10 QUESTIONS / 80% UNLOCK (UNCHANGED)
- LOCAL_ANDROID_RUNTIME_STATUS: PASS — OWNER_EVIDENCE
- PHYSICAL_DEVICE_RUNTIME_STATUS: PASS — OWNER_EVIDENCE
- AUDIO_QUALITY_ACCEPTANCE_STATUS: PASS — OWNER_EVIDENCE
- LEVELS_RUNTIME_STATUS: PASS — OWNER_EVIDENCE
- SESSION_SUMMARY_RUNTIME_STATUS: PASS — OWNER_EVIDENCE
- PERSISTENCE_RUNTIME_STATUS: PASS — OWNER_EVIDENCE
- MIGRATION_1_2_DEVICE_STATUS: NOT_TESTED
- OWNER_ACCEPTANCE_STATUS: M2.3_ACCEPTED / M2_OVERALL_PENDING

Owner smoke report от 2026-08-20:

- `AUDIO: PASS`;
- `LEVELS: PASS`;
- `SUMMARY: PASS`;
- `PERSISTENCE: PASS`;
- `MIGRATION_1_2: NOT_TESTED`.

`MIGRATION_1_2: NOT_TESTED` означает только отсутствие реального device-upgrade evidence со schema v1. Migration 1→2 остаётся реализованной в коде с backfill; этот пробел evidence должен быть закрыт до финального M2 acceptance тестом миграции либо реальным обновлением старой установки.

## M2.2 — в main

- Room `databaseSchemaVersion = 2`;
- migration `1 -> 2` с backfill старых Attempt;
- persistent `LetterProgressEntity` и `SessionResultEntity`;
- `ProgressRepository`;
- D019 per-letter Session Summary;
- repeat/continue actions.

## M2.3 — реализовано и runtime-проверено

### Local audio / D020

В `app/src/main/res/raw/` добавлены mono OGG Vorbis, 22.05 kHz:

- `sound_letter_a.ogg` — `А` / «а»;
- `sound_letter_m.ogg` — `М` / «эм»;
- `sound_letter_o.ogg` — `О` / «о»;
- `sound_letter_u.ogg` — `У` / «у»;
- `sound_letter_s.ogg` — `С` / «эс»;
- `sound_letter_n.ogg` — `Н` / «эн»;
- `sound_correct.ogg`;
- `sound_wrong.ogg`;
- `sound_level_complete.ogg`.

`AudioAssetCatalog` содержит case-insensitive mapping для `А/М/О/У/С/Н`. При отсутствии mapping/resource или ошибке `MediaPlayer` сохраняется TTS fallback.

### UI audio hooks

- после ответа вызывается `AudioPlayer.playFeedback(isCorrect)`;
- на Session Summary вызывается `AudioPlayer.playLevelComplete()`;
- аудио не меняет LearningEngine, число вопросов или level unlock.

### Автоматическая и ручная проверка

- JVM tests: PASS;
- `assembleDebug`: PASS;
- Android CI: PASS;
- локальная озвучка/feedback/completion на телефоне: PASS;
- levels/unlock на телефоне: PASS;
- D019 per-letter Summary на телефоне: PASS;
- persistence Room/DataStore после перезапуска: PASS;
- migration 1→2 на реальной schema v1: NOT_TESTED.

## Активный slice — M2.4 LearningEngine adaptive policy

Следующий обязательный slice M2 должен закрыть оставшиеся критерии LearningEngine:

- retry queue из D019;
- возврат ошибочной target-буквы после 2–4 других заданий, если пул позволяет;
- mastery states `NEW / LEARNING / FAMILIAR / STABLE`;
- weighted selection с ненулевым шансом сильных букв;
- повышенный вес новых/слабых/недавно ошибочных/давно не показанных букв;
- delayed checks и delayed-success tracking;
- централизованную LearningPolicy config без magic numbers;
- deterministic JVM tests обязательных инвариантов;
- отдельное доказательство migration 1→2 до финального M2 closure.

Точные правила брать из `docs/LEARNING_ENGINE.md`, `docs/SUCCESS_METRICS.md` и действующих решений; не менять Curriculum v2, 10 вопросов и 80% unlock без нового owner decision.

## Gate M2 → M3

До M3 остаются обязательными:

- M2.4 implementation + tests;
- migration 1→2 evidence;
- Milestone Closure Evidence Audit по каждой строке M2 DoD;
- отсутствие `FAIL` / `UNKNOWN`;
- owner acceptance всего M2.
