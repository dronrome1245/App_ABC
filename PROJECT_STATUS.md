# PROJECT_STATUS.md

Дата обновления: 2026-08-21

## Общий статус проекта

**App_ABC MVP v1.0.0 — COMPLETE / PRODUCTION READY (100%).**

- Milestone 1: DONE (100%) / OWNER ACCEPTED.
- Milestone 2: DONE (100%) / OWNER ACCEPTED.
- Milestone 3: DONE (100%) / OWNER ACCEPTED.
- Milestone 4: DONE (100%) / OWNER ACCEPTED.

Финальный owner-approved smoke-тест на физическом Pixel 7a: **PASS**.

## Финальный контур v1.0.0

- STATIC_REVIEW_STATUS: PASS
- TESTS_CI_STATUS: PASS — Triple Gate: `test` / `assembleDebug` / `assembleRelease`
- LOCAL_BUILD_STATUS: PASS
- PHYSICAL_DEVICE_STATUS: PASS — Pixel 7a
- OWNER_ACCEPTANCE_STATUS: ACCEPTED
- LEARNING_POLICY_VERSION: 4 — 7-day retention decay
- CURRICULUM_VERSION: 3 — 33 letters / 8 levels
- ROOM_DATABASE_SCHEMA: 2
- AUDIO_ASSETS: 33 letter OGGs + 3 UI sounds + `res/raw/keep.xml`
- AUDIO_STRATEGY: LOCAL OGG FIRST / TTS FALLBACK
- RELEASE_OPTIMIZATION: R8 MINIFIED + RESOURCE SHRINKING ENABLED
- RELEASE_BUILD_R8: PASS
- EXTERNAL_NETWORK_ANALYTICS_SDK: NONE
- MVP_VERSION: 1.0.0
- MVP_STATUS: COMPLETE / PRODUCTION READY (100%)

Production signing keys и store publication не хранятся/не выполняются в рамках закрытия MVP. Это отдельный deployment/Post-MVP шаг.

## Post-MVP active work

### AUDIO-02 — Audio Pack v2 / D027

- AUDIO_02_STATUS: IN PROGRESS / HOME-ONLY V2 INSTALLED / TECHNICAL GATES PASS / DEVICE SMOKE PENDING
- D027_SCOPE: 33 letter voice assets; existing `correct` / `wrong` / `level_complete` remain unchanged
- V2_RESOURCE_NAMING: `sound_letter_<token>_v2`
- AUDIO_RESOLUTION_ORDER: V2 LOCAL -> V1 LOCAL -> TTS
- BUNDLED_V2_LETTER_ASSETS: 33 / 33 (HOME-ONLY GCOMPRIS)
- BUNDLED_V1_LETTER_ASSETS: 33 / 33
- SAFE_MISSING_ASSET_FALLBACK: IMPLEMENTED
- RELEASE_LIFECYCLE: existing `stop()` / idempotent `release()` retained
- V2_DURATION_TARGET: 400–700 ms per letter, subject to justified articulation exceptions
- HOME_ONLY_V2_CONTENT_STATUS: INSTALLED — 33/33 human-recorded GCompris OGGs; distribution remains blocked until replacement/relicensing decision
- AUDIO_V2_BUNDLED_QC: PASS — run `32513695237`
- AUDIO_V2_ANDROID_CI: PASS — run `32513695238` (`test` / `assembleDebug` / `assembleRelease`)
- DEVICE_AUDIO_SMOKE: PENDING owner verification of installed HOME-ONLY v2 assets

Для домашнего использования владелец выбрал человеческий пакет GCompris: 33/33 v2-ассета установлены и имеют приоритет над v1. Bundled-audio QC, JVM/Compose tests, debug build и release build с R8/resource shrinking — PASS. AUDIO-02 остаётся `IN PROGRESS` только до физического прослушивания всех 33 букв на устройстве. При owner PASS задача закрывается как `DONE FOR HOME USE`; это не снимает distribution gate. Перед любой передачей или публикацией HOME-ONLY пакет должен быть заменён либо должно быть отдельно принято и выполнено лицензионное решение по распространению.

## Merge / CI evidence

- PR #7 / M3.1: merged — `73b62801c39dd0ccf859f1d3bdaeefa2e8c89b52`.
- PR #8 / M3.2: merged — `2c08aff6a49e26810de07734839ef7e73b58fa0f`.
- PR #9 / M3.3: merged — `3b61d21ef0fb923b9ac2cd9c40c874ac2b7eb74f`.
- PR #11 / Home selector hotfix: merged — `4d8320053770371ddde065fe90ed9a4402cae593`.
- PR #12 / M4.1 UI Delight: merged — `702f908630c810ab3ab41acf85e14d030c8a6bd5`.
- PR #13 / M4.2 Parent settings + safe reset: merged — `97472f3c5bf07684843e419987a4c86704aa2561`.
- PR #14 / M4.3 Release hardening: merged — `b4eb423ed5ec4986d53da597084d386a1cbd58d2`.
- PR #14 final CI evidence: run `32478003835` — SUCCESS.
- CI gates on final PR #14 head: `test` PASS / `assembleDebug` PASS / `assembleRelease` PASS.

## Milestone 1 — final

- MILESTONE_1_STATUS: DONE (100%)
- OWNER_ACCEPTANCE_STATUS: ACCEPTED
- BASE_FLOW: PASS
- SESSION_LENGTH: 10
- LEVEL_1: `А М`
- HISTORY_PERSISTENCE: PASS
- PHYSICAL_DEVICE_STATUS: PASS

## Milestone 2 — final

- MILESTONE_2_STATUS: DONE (100%)
- OWNER_ACCEPTANCE_STATUS: ACCEPTED
- M2_5_CLOSURE_EVIDENCE_AUDIT_STATUS: PASS — 7/7
- HYBRID_AUDIO: LOCAL OGG FIRST / TTS FALLBACK
- LEARNING_POLICY_VERSION_AT_CLOSE: 3
- ROOM_DATABASE_SCHEMA: 2
- MIGRATION_1_2_TEST: PASS
- TESTS_CI_STATUS: PASS
- PHYSICAL_DEVICE_STATUS: PASS

## Milestone 3 — final

- MILESTONE_3_STATUS: DONE (100%)
- OWNER_ACCEPTANCE_STATUS: ACCEPTED
- M3_4_CLOSURE_EVIDENCE_AUDIT_STATUS: PASS — 7/7
- PARENTAL_GATE_STATUS: PASS
- PARENT_DASHBOARD_STATUS: PASS
- CURRICULUM_VERSION: 3
- CURRICULUM_LEVELS: 8
- CURRICULUM_LETTERS: 33
- AUDIO_ASSETS: 33 letter OGGs + 3 UI sounds
- LEARNING_POLICY_VERSION: 4
- RETENTION_DECAY: 7 DAYS / DECAY WHEN ELAPSED > 7 DAYS
- RETENTION_DECAY_WEIGHT: 2.0
- ROOM_DATABASE_SCHEMA: 2
- TESTS_CI_STATUS: PASS
- PHYSICAL_DEVICE_STATUS: PASS — Pixel 7a

### D024 normalized matrix

- Level 1: `А М`
- Level 2 new: `О У`; pool 4
- Level 3 new: `С Н`; pool 6
- Level 4 new: `И Т К`; pool 9
- Level 5 new: `Л Р В`; pool 12
- Level 6 new: `Д П Б`; pool 15
- Level 7 new: `З Г Е Я`; pool 19
- Level 8 new: `Ш Ж Ч Щ Х Ц Э Ю Ё Ы Ь Ъ Й Ф`; pool 33

## Milestone 4 — final

- MILESTONE_4_STATUS: DONE (100%)
- OWNER_ACCEPTANCE_STATUS: ACCEPTED
- M4_4_CLOSURE_EVIDENCE_AUDIT_STATUS: PASS — 7/7
- STATIC_REVIEW_STATUS: PASS
- TESTS_CI_STATUS: PASS — Triple Gate
- LOCAL_BUILD_STATUS: PASS
- PHYSICAL_DEVICE_STATUS: PASS — Pixel 7a
- ROOM_DATABASE_SCHEMA: 2 (UNCHANGED)
- LEARNING_POLICY_VERSION: 4 (UNCHANGED)
- CURRICULUM_VERSION: 3 (UNCHANGED)

### M4.1 — UI Delight

- M4_1_STATUS: COMPLETE / MERGED
- HOME_LEVEL_SELECTOR: 4x2 / LEVELS 1–8 VISIBLE
- RESULT_CELEBRATION: NATIVE COMPOSE CANVAS
- RESULT_CELEBRATION_PARTICLES: 36
- RESULT_CELEBRATION_DURATION: 2800 ms
- ANSWER_CARD_PRESS_SCALE: 0.95 -> 1.0
- RESULT_COMPOSE_TESTS: PASS

### M4.2 — Parent settings / safe reset

- M4_2_STATUS: COMPLETE / MERGED
- SETTINGS_DATASTORE: COMPLETE
- VOICEOVER_SETTING: PERSISTENT / DEFAULT TRUE
- SOUND_EFFECTS_SETTING: PERSISTENT / DEFAULT TRUE
- HYBRID_AUDIO_SETTINGS_INTEGRATION: COMPLETE
- PROGRESS_RESET: COMPLETE
- RESET_ROOM_TABLES: `attempts` / `letter_progress` / `session_results`
- RESET_ROOM_TRANSACTION: YES
- LEVEL_PROGRESSION_RESET: Level 1
- SETTINGS_PRESERVED_BY_PROGRESS_RESET: YES
- TESTS: PASS

### M4.3 — Release hardening

- M4_3_STATUS: COMPLETE / MERGED
- PR: #14
- MERGE_SHA: `b4eb423ed5ec4986d53da597084d386a1cbd58d2`
- RELEASE_MINIFY: ENABLED
- RELEASE_SHRINK_RESOURCES: ENABLED
- RELEASE_PROGUARD_FILE: `app/proguard-rules.pro`
- RELEASE_SIGNING_FOR_CI: DEBUG KEY / VALIDATION ONLY
- ROOM_R8_KEEP_RULES: ADDED
- COMPOSE_R8_KEEP_RULES: ADDED
- DATASTORE_R8_KEEP_RULES: ADDED
- DYNAMIC_AUDIO_RESOURCE_KEEP: `res/raw/keep.xml` / `@raw/sound_*`
- HYBRID_AUDIO_RELEASE_IDEMPOTENT: YES
- VIEWMODEL_AUDIO_EXIT: `stop()` ON CLEAR
- ACTIVITY_AUDIO_OWNER: FINAL `release()` ON DESTROY
- ACTIVITY_INIT_COROUTINE_SCOPE: `lifecycleScope`
- CI_TEST_TASK: PASS
- CI_DEBUG_ASSEMBLE: PASS
- CI_RELEASE_ASSEMBLE: PASS — R8 + RESOURCE SHRINK
- RELEASE_BUILD_R8: PASS
- FINAL_ANDROID_CI_M4_3: PASS — run `32478003835`

### M4.4 — Closure Evidence Audit — 7/7

1. Уровни и позитивная игровая оболочка — PASS — CODE / TEST / RUNTIME evidence.
2. Повторная тренировка и отсутствие блокирующей/штрафной механики — PASS — CODE / RUNTIME evidence.
3. Награды/анимации не изменяют LearningPolicy v4 и Curriculum v3 — PASS — STATIC / CODE evidence.
4. Persistent sound settings в Parent Dashboard — PASS — CODE / TEST / RUNTIME evidence.
5. Safe progress reset с подтверждением — PASS — CODE / TEST / RUNTIME evidence.
6. Release hardening и CI Triple Gate — PASS — CI evidence.
7. Финальный release smoke Pixel 7a + owner acceptance — PASS — RUNTIME / OWNER evidence.

- M4_4_CLOSURE_EVIDENCE_AUDIT_STATUS: PASS — 7/7
- FAIL_COUNT: 0
- UNKNOWN_COUNT: 0
- OWNER_ACCEPTANCE_STATUS: ACCEPTED

## MVP closure

Согласно финальному решению владельца от 2026-08-21 и синхронизированному `docs/DEFINITION_OF_DONE.md`, обязательный scope App_ABC MVP v1.0.0 закрывается Milestone 1–4. Дополнительные curriculum/UX исследования, слоги, мини-игры, speech recognition и store publication перенесены в Post-MVP Roadmap.

**FINAL_STATUS: App_ABC MVP v1.0.0 — COMPLETE / PRODUCTION READY (100%).**

## Следующий шаг

MVP v1.0.0 остаётся закрытым. Текущий Post-MVP шаг — **AUDIO-02 device smoke** на физическом Pixel 7a: прослушать все 33 буквы, проверить правильность названий, отсутствие обрезания начала/конца и фактическое использование v2.

Если smoke = PASS, AUDIO-02 закрывается как `DONE FOR HOME USE`, после чего PR #16 может быть подготовлен к merge только по отдельному решению владельца. Следующая продуктовая задача D027 после AUDIO-02 — **UX-01** (визуальная иерархия действий на ResultScreen). Публичное распространение при текущем GCompris HOME-ONLY пакете остаётся заблокированным.
