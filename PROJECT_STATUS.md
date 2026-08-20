# PROJECT_STATUS.md

Дата обновления: 2026-08-20

## Текущий этап

**Milestone 1 — DONE (100%). Milestone 2 — M2.3 KICKOFF.**

M2.1 находится в `main`. M2.2 реализован в ветке `feature/m2-room-letter-stats` и PR #4; автоматические gate M2.2 пройдены. Ручной runtime/smoke evidence M2.1–M2.2 сохраняется для итогового M2 acceptance и будет собран в M2.3.

## Статусы M2.2

- IMPLEMENTATION_STATUS: COMPLETE
- STATIC_REVIEW_STATUS: PASS
- TESTS_CI_STATUS: PASS
- DEBUG_BUILD_STATUS: PASS
- DATABASE_SCHEMA_VERSION: 2
- ROOM_MIGRATION_1_2_STATUS: IMPLEMENTED
- HISTORICAL_ATTEMPT_BACKFILL_STATUS: IMPLEMENTED
- D019_PER_LETTER_RESULT_STATUS: IMPLEMENTED
- LOCAL_ANDROID_RUNTIME_STATUS: PENDING_M2.3_SMOKE
- PHYSICAL_DEVICE_RUNTIME_STATUS: PENDING_M2.3_SMOKE
- OWNER_ACCEPTANCE_STATUS: PENDING_FOR_M2

GitHub Actions PR #4 подтвердил прохождение JVM unit tests и `assembleDebug` на коде M2.2.

## M2.2 — реализовано

### Room / persistence

- сырые `Attempt` остаются единственным детальным источником истории;
- добавлен `LetterProgressEntity` как производный устойчивый агрегат по букве;
- добавлен `SessionResultEntity` для истории завершённых сессий с уникальным `sessionId`;
- добавлены `LetterProgressDao` и `SessionResultDao`;
- добавлен `ProgressRepository`;
- финализация сессии выполняется в Room transaction и идемпотентна по `sessionId`;
- `databaseSchemaVersion = 2`;
- migration `1 -> 2` не destructive;
- migration backfill агрегирует существующие M1/M2.1 `Attempt` в новые таблицы без изменения исторических строк.

### Session Summary / D019

После завершения сессии доступны:

- общий счёт `X / 10`;
- точность;
- pass/fail по D021 (`>=8/10`);
- сообщение об открытии следующего уровня;
- разбивка по каждой target-букве текущей сессии: attempts/correct/errors;
- понятная success/error индикация;
- `Повторить уровень`;
- `Далее` / `К выбору уровней`.

Перенос D019 по per-letter session result технически закрыт. Retry queue из D019 остаётся отдельным обязательным пунктом M2.

### Автоматические проверки

JVM tests покрывают:

- расчёт per-letter breakdown;
- объединение новой статистики с существующим `LetterProgressEntity`;
- correct/attempt counts;
- average response time;
- last seen timestamp.

CI также подтвердил debug build с Room/KSP schema version 2.

## Runtime boundary

Изменения M2.2 затрагивают Room migration и Compose ResultScreen. Автоматические проверки не заменяют запуск на устройстве. В M2.3 нужен обычный owner smoke test: обновление существующей установки, прохождение 10 вопросов, проверка per-letter breakdown, повторного запуска и сохранности прогресса.

## Активный slice — M2.3

M2.3: добавить утверждённые локальные WAV/OGG assets в `res/raw`, проверить local-audio-first + TTS fallback, проверить уже существующий экран выбора уровней и выполнить объединённый ручной smoke test M2.1–M2.2.

## Что ещё остаётся обязательным в M2

- retry queue из D019;
- mastery states;
- weighted selection;
- delayed checks;
- weak-letter weighting;
- реальные pre-recorded WAV/OGG assets и runtime-проверка local-audio-first;
- итоговый M2 closure audit и owner acceptance.
