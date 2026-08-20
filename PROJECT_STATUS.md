# PROJECT_STATUS.md

Дата обновления: 2026-08-20

## Текущий этап

**Milestone 1 — DONE (100%). Milestone 2 — M2.3 IMPLEMENTATION COMPLETE / OWNER SMOKE PENDING.**

M2.1 и M2.2 слиты в `main` (M2.2 — PR #4). M2.3 реализован в ветке `feature/m2-3-audio-assets-smoke`: локальные audio assets, feedback sounds и level-complete sound подключены к существующему `HybridAudioPlayer` с сохранением TTS fallback.

Важно: это не означает, что весь Milestone 2 закрыт. По текущему `docs/DEFINITION_OF_DONE.md` после M2.3 всё ещё обязательны retry queue, mastery states, weighted selection, delayed checks/weak-letter weighting и итоговый closure audit. Они не были перенесены отдельным owner decision и поэтому не могут считаться выполненными или исключёнными.

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
- LOCAL_ANDROID_RUNTIME_STATUS: PENDING_OWNER_SMOKE
- PHYSICAL_DEVICE_RUNTIME_STATUS: PENDING_OWNER_SMOKE
- AUDIO_QUALITY_ACCEPTANCE_STATUS: PENDING_OWNER_SMOKE
- OWNER_ACCEPTANCE_STATUS: PENDING_FOR_M2

GitHub Actions PR #5 подтвердил JVM unit tests и `assembleDebug` на M2.3 code/assets head. Документационные follow-up commits не меняют runtime-код; финальный branch CI также должен оставаться зелёным.

## M2.2 — в main

- Room `databaseSchemaVersion = 2`;
- migration `1 -> 2` с backfill старых Attempt;
- persistent `LetterProgressEntity` и `SessionResultEntity`;
- `ProgressRepository`;
- D019 per-letter Session Summary;
- repeat/continue actions.

## M2.3 — реализовано

### Local audio / D020

В `app/src/main/res/raw/` добавлены mono OGG Vorbis, 22.05 kHz:

- `sound_letter_a.ogg` — `А` / «а»;
- `sound_letter_m.ogg` — `М` / «эм»;
- `sound_letter_o.ogg` — `О` / «о»;
- `sound_letter_u.ogg` — `У` / «у»;
- `sound_letter_s.ogg` — `С` / «эс»;
- `sound_letter_n.ogg` — `Н` / «эн»;
- `sound_correct.ogg` — короткий положительный сигнал;
- `sound_wrong.ogg` — мягкий сигнал ошибки;
- `sound_level_complete.ogg` — короткий завершающий джингл.

Буквенные файлы синтезированы локально офлайн русским системным TTS-инструментом для M2 smoke candidate; внешние/персональные записи и облачные сервисы не использовались. Финальное качество голоса проверяет владелец на целевом устройстве.

`AudioAssetCatalog` содержит mapping для `А/М/О/У/С/Н` и нормализует регистр. Если mapping отсутствует, Android resource не найден или `MediaPlayer` возвращает ошибку, остаётся TTS fallback.

### UI audio hooks

- после ответа вызывается `AudioPlayer.playFeedback(isCorrect)`;
- на Session Summary вызывается `AudioPlayer.playLevelComplete()`;
- новый локальный звук не меняет LearningEngine, число вопросов или level unlock.

### Автоматическая проверка

JVM tests проверяют mapping всех букв Curriculum v2, lowercase normalization, имена feedback/completion assets и missing-resource fallback policy. CI подтвердил JVM tests и `assembleDebug` с добавленными OGG raw resources.

## Обязательный owner smoke перед любым утверждением runtime PASS

Следовать `NEXT_TASK.md`: проверить локальную озвучку всех шести букв, feedback, Summary, TTS fallback, unlock уровней, per-letter summary, persistence и migration на реальном устройстве.

## Что ещё остаётся обязательным в Milestone 2 после M2.3 smoke

- retry queue после ошибки (D019 / LearningEngine);
- mastery states;
- weighted selection;
- delayed checks;
- weak-letter weighting / централизованная LearningPolicy;
- полный набор тестов инвариантов LearningEngine;
- Milestone Closure Evidence Audit;
- owner acceptance.
