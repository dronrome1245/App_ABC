# PROJECT_STATUS.md

Дата обновления: 2026-08-21

## Текущий этап

**Milestone 1 — DONE (100%). Milestone 2 — DONE (100%). Milestone 3 — M3.2 IMPLEMENTATION_COMPLETE / M3.3 NEXT.**

PR #7 с M3.1 слит в `main` merge-коммитом `73b62801c39dd0ccf859f1d3bdaeefa2e8c89b52`. M3.2 реализован в `feature/m3-curriculum-audio-full` / PR #8.

## Milestone 2 — финальные статусы

- MILESTONE_2_STATUS: DONE (100%)
- OWNER_ACCEPTANCE_STATUS: ACCEPTED
- LEARNING_POLICY_VERSION: 3
- DATABASE_SCHEMA_VERSION: 2

## M3.1

- M3_1_IMPLEMENTATION_STATUS: COMPLETE
- JVM_TESTS_STATUS: PASS
- DEBUG_BUILD_STATUS_M3_1: PASS
- M3_1_RUNTIME_STATUS: NOT_TESTED
- M3_1_OWNER_ACCEPTANCE_STATUS: PENDING

## M3.2 — статусы

- M3_2_IMPLEMENTATION_STATUS: IMPLEMENTATION_COMPLETE
- CURRICULUM_VERSION: 3
- CURRICULUM_LEVELS: 8
- CURRICULUM_LETTERS: 33
- LEVEL_POOL_SIZES: 2 / 4 / 6 / 9 / 12 / 15 / 19 / 33
- SESSION_QUESTION_COUNT: 10 (UNCHANGED)
- LEVEL_UNLOCK_THRESHOLD: >=80% / 8 OF 10 (UNCHANGED)
- AUDIO_ASSETS_COUNT: 33_LETTERS_FULL
- AUDIO_FEEDBACK_ASSETS_COUNT: 3
- AUDIO_STRATEGY: LOCAL_OGG_FIRST / TTS_FALLBACK
- AUDIO_MAPPING_STATUS: ALL_33_LETTERS
- ROOM_SCHEMA_VERSION: 2 (UNCHANGED)
- LEARNING_POLICY_VERSION: 3 (UNCHANGED)
- PARENTAL_GATE_STATUS: UNCHANGED
- CURRICULUM_TEST_STATUS: PASS
- AUDIO_ASSET_CATALOG_TEST_STATUS: PASS
- PARENT_DASHBOARD_TEST_STATUS: PASS
- DEBUG_BUILD_STATUS_M3_2: PASS
- ANDROID_CI_STATUS_M3_2: PASS — run 32462108198
- M3_2_RUNTIME_AUDIO_STATUS: NOT_TESTED

## D024 normalized matrix

- Level 1: `А М`
- Level 2 new: `О У`
- Level 3 new: `С Н`
- Level 4 new: `И Т К`
- Level 5 new: `Л Р В`
- Level 6 new: `Д П Б`
- Level 7 new: `З Г Е Я`
- Level 8 new: `Ш Ж Ч Щ Х Ц Э Ю Ё Ы Ь Ъ Й Ф`

Product Lead подтвердил нормализацию Level 8 с добавлением `Й` и `Ф`, чтобы D024 фактически покрывал все 33 буквы.

## Следующий этап

**M3.3: Retention Decay.** Реализовать D025 как LearningPolicy v4: 7-дневное затухание mastery с детерминированным clock/time source и time-based JVM unit tests. Room schema не повышать без отдельной необходимости.
