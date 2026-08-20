# PROJECT_STATUS.md

Дата обновления: 2026-08-20

## Текущий этап

**Milestone 1 — DONE (100%). Milestone 2 — M2.4 IMPLEMENTATION COMPLETE / CI VALIDATION PENDING.**

M2.1–M2.3 находятся в `main`. M2.3 прошёл owner smoke на физическом устройстве: AUDIO/LEVELS/SUMMARY/PERSISTENCE = PASS; device migration 1→2 = NOT_TESTED.

M2.4 реализует owner-approved LearningPolicy v3 по D022 и готовится к автоматической валидации.

## M2.4 — реализовано

### LearningPolicy v3

- `learningPolicyVersion = 3`;
- mastery states: `INTRODUCED / PRACTICING / MASTERED`;
- `INTRODUCED`: <3 попыток;
- `MASTERED`: >=5 попыток и recent accuracy >=85%;
- recent window: до последних 10 попыток;
- `MASTERED` weight = 1.0;
- `INTRODUCED` weight = 2.0;
- `PRACTICING` weight = 2.0…3.0 по recent error ratio;
- 10 вопросов и unlock >=80% / 8 из 10 не изменены.

### AdaptiveSessionGenerator

- pure Kotlin;
- исторические Attempt загружаются до первого вопроса;
- weighted target selection использует persistent history;
- сильные буквы имеют ненулевой шанс;
- одна target не появляется более двух раз подряд;
- distractor не совпадает с target;
- confusion pair обновляется при ошибке.

### Retry / delayed checks

- `InSessionRetryQueue`;
- после ошибки target при наличии места возвращается через 2–4 других вопроса;
- пока target ждёт retry, обычный weighted flow её не выбирает;
- длина сессии остаётся 10;
- late-session error не расширяет сессию, если spacing >=2 физически не помещается;
- delayed success считается только при spacing >=2.

### Room migration evidence

Добавлен JVM Robolectric test, который:

- создаёт реальную SQLite-базу schema v1;
- вставляет исторические Attempt;
- выполняет фактический `MIGRATION_1_2`;
- проверяет сохранность Attempt;
- проверяет backfill `letter_progress`;
- проверяет backfill `session_results`.

Device migration 1→2 остаётся `NOT_TESTED`; automated evidence будет переведено в PASS только после CI.

## Статусы

- M2_4_IMPLEMENTATION_STATUS: COMPLETE
- LEARNING_POLICY_VERSION: 3
- ADAPTIVE_GENERATOR_STATUS: IMPLEMENTED
- RETRY_QUEUE_STATUS: IMPLEMENTED
- MASTERY_STATES_STATUS: IMPLEMENTED
- WEIGHTED_SELECTION_STATUS: IMPLEMENTED
- DELAYED_CHECK_STATUS: IMPLEMENTED
- CONFUSION_TRACKING_STATUS: IMPLEMENTED
- JVM_INVARIANT_TESTS_STATUS: ADDED / CI_PENDING
- MIGRATION_1_2_AUTOMATED_TEST_STATUS: ADDED / CI_PENDING
- MIGRATION_1_2_DEVICE_STATUS: NOT_TESTED
- TESTS_CI_STATUS: PENDING
- DEBUG_BUILD_STATUS: PENDING
- OWNER_ACCEPTANCE_STATUS: M2_OVERALL_PENDING

## Следующий gate

После зелёного CI M2.4 переходит в COMPLETE/PASS, а единственной следующей задачей становится M2.5 Milestone 2 Closure Audit. M3 не начинать до аудита и owner acceptance всего M2.
