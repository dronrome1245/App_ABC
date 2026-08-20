# PROJECT_STATUS.md

Дата обновления: 2026-08-20

## Текущий этап

**Milestone 1 — DONE (100%). Milestone 2 — M2.4 COMPLETE / M2.5 CLOSURE AUDIT KICKOFF.**

M2.1–M2.3 находятся в `main`. M2.3 прошёл owner smoke на физическом устройстве: AUDIO/LEVELS/SUMMARY/PERSISTENCE = PASS; device migration 1→2 = NOT_TESTED.

M2.4 реализован в `feature/m2-learning-engine-policy` / PR #6. LearningPolicy v3 зафиксирован owner decision D022. Автоматические gate M2.4 пройдены: JVM tests, реальный SQLite migration test 1→2 и `assembleDebug` — PASS.

## Статусы M2.4

- M2_4_IMPLEMENTATION_STATUS: COMPLETE
- LEARNING_POLICY_VERSION: 3
- ADAPTIVE_GENERATOR_STATUS: PASS — CODE/TEST
- RETRY_QUEUE_STATUS: PASS — CODE/TEST
- MASTERY_STATES_STATUS: PASS — CODE/TEST
- WEIGHTED_SELECTION_STATUS: PASS — CODE/TEST
- DELAYED_CHECK_STATUS: PASS — CODE/TEST
- CONFUSION_TRACKING_STATUS: PASS — CODE/TEST
- JVM_INVARIANT_TESTS_STATUS: PASS — CI
- MIGRATION_1_2_AUTOMATED_TEST_STATUS: AUTOMATED_TEST_PASS
- MIGRATION_1_2_DEVICE_STATUS: NOT_TESTED
- TESTS_CI_STATUS: PASS
- DEBUG_BUILD_STATUS: PASS
- OWNER_ACCEPTANCE_STATUS: M2_OVERALL_PENDING

## LearningPolicy v3 — D022

- mastery states: `INTRODUCED / PRACTICING / MASTERED`;
- `INTRODUCED`: <3 попыток;
- `PRACTICING`: от 3 попыток до выполнения критерия `MASTERED`;
- `MASTERED`: >=5 попыток и recent accuracy >=85%;
- recent window: до последних 10 попыток;
- `MASTERED` weight = 1.0;
- `INTRODUCED` weight = 2.0;
- `PRACTICING` weight = 2.0…3.0 по recent error ratio;
- 10 вопросов и unlock >=80% / 8 из 10 не изменены;
- исторические Attempt прежних policy versions не переписываются.

## AdaptiveSessionGenerator

- pure Kotlin;
- persistent Attempt history загружается до первого вопроса;
- weighted target selection использует историю по активным буквам;
- сильные буквы имеют ненулевой шанс;
- одна target не появляется более двух раз подряд;
- distractor не совпадает с target;
- confusion pair обновляется при ошибке;
- фиксированный `Random` обеспечивает deterministic tests.

## Retry / delayed checks

- после ошибки target ставится в `InSessionRetryQueue`;
- при достаточном остатке 10-вопросной сессии target возвращается после 2–4 других вопросов;
- до due retry target исключена из обычного weighted flow;
- retry не увеличивает длину сессии и не создаёт бесконечного цикла;
- delayed success валиден только при spacing >=2 других вопросов.

## Migration 1→2 evidence

JVM Robolectric test создаёт реальную SQLite schema v1, записывает исторические `Attempt`, применяет фактический `DatabaseMigrations.MIGRATION_1_2` и проверяет:

- исходные Attempt сохранены;
- `letter_progress` backfill содержит корректные attempts/correct/lastSeen/averageResponseTime;
- `session_results` backfill содержит корректные totals/correct/passed.

Статус migration-кода: **AUTOMATED_TEST_PASS**. Реальный апгрейд schema v1 на физическом устройстве по-прежнему честно отмечен `NOT_TESTED`, а не PASS.

## Автоматические проверки M2.4

GitHub Actions PR #6:

- JVM unit tests — PASS;
- deterministic adaptive invariants — PASS;
- migration 1→2 SQLite test — PASS;
- `assembleDebug` — PASS.

Первый CI attempt не дошёл до тестов из-за ошибочно указанной несуществующей зависимости Robolectric `4.13.2`; после исправления на опубликованную `4.13` финальный прогон полностью зелёный.

## Активный slice — M2.5 Closure Audit

Следующий и финальный шаг Milestone 2 — обязательный построчный Closure Evidence Audit по разделу M2 в `docs/DEFINITION_OF_DONE.md`.

M3 не начинать до:

- аудита каждой строки M2 DoD;
- отсутствия `FAIL` / `UNKNOWN`;
- проверки documentation drift;
- owner acceptance всего Milestone 2.
