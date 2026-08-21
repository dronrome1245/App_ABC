# PROJECT_STATUS.md

Дата обновления: 2026-08-21

## Текущий этап

**Milestone 1 — DONE (100%). Milestone 2 — DONE (100%). Milestone 3 — M3.1 IN_PROGRESS.**

M2 остаётся закрытым и owner accepted. M3.1 реализуется в `feature/m3-parent-dashboard-gate`: Parental Gate, Parent Dashboard и 33-буквенная сводка строятся поверх существующей Room schema 2 без изменения тренировочного цикла.

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
- RUNTIME_CURRICULUM_VERSION: 2
- DATABASE_SCHEMA_VERSION: 2

## M3 owner decisions

- D023: Parent mode открывается через случайный арифметический Parental Gate; отрицательные ответы для subtraction не генерируются.
- D024: целевой Curriculum содержит 8 уровней и покрывает все 33 русские буквы. Levels 1–3 сохраняют `А/М`, `О/У`, `С/Н`; Levels 4–8 вводят оставшиеся 27 букв. В полученном M3.1 packet отсутствует точная owner-approved матрица распределения этих 27 букв между уровнями, поэтому она не изобреталась и должна быть импортирована перед M3.2.
- D025: `MASTERED` требует long-term re-check после 7 суток без успешного подтверждения; runtime LearningPolicy v3 в M3.1 пока не меняется.

## M3.1 — реализуемый контур

- DOMAIN_PARENTAL_GATE_STATUS: IMPLEMENTED
- PARENTAL_GATE_NON_NEGATIVE_SUBTRACTION_STATUS: IMPLEMENTED / UNIT_TESTED
- PARENT_DASHBOARD_ROUTE_STATUS: IMPLEMENTED
- PARENT_DASHBOARD_VIEWMODEL_STATUS: IMPLEMENTED
- ALL_33_LETTERS_MATRIX_STATUS: IMPLEMENTED
- NOT_STARTED_DEFAULT_STATUS: IMPLEMENTED
- MASTERED_STATUS_SOURCE: LEARNING_POLICY_V3
- SUMMARY_METRICS_STATUS: IMPLEMENTED
- LETTER_DETAIL_DIALOG_STATUS: IMPLEMENTED
- HOME_PARENT_ENTRY_STATUS: IMPLEMENTED
- DATABASE_SCHEMA_VERSION: 2 (UNCHANGED)
- TRAINING_SESSION_RULE: 10 QUESTIONS / ADAPTIVE GENERATOR / 80% UNCHANGED
- JVM_TESTS_STATUS: PENDING_CI
- DEBUG_BUILD_STATUS_M3_1: PENDING_CI
- M3_1_RUNTIME_STATUS: NOT_TESTED

## Parent Dashboard data source

Dashboard не создаёт новую таблицу. Используются:

- raw `Attempt` для recent/mastery входа LearningPolicy;
- `LetterProgressEntity` для persistent attempts/correct/response-time aggregates;
- `SessionResultEntity` для количества завершённых сессий и общей точности;
- канонический 33-буквенный `RussianAlphabet` для дополнения отсутствующих строк статусом `NOT_STARTED`.

## Decision/version discipline

- runtime `learningPolicyVersion = 3` до отдельной реализации D025;
- runtime `curriculumVersion = 2` до M3.2;
- D024 требует новой Curriculum version только при фактическом кодировании Levels 4–8;
- Room schema остаётся 2;
- M3.1 не меняет audio assets и LearningEngine session behavior.

## Следующий шаг после M3.1

M3.2: расширить Curriculum в коде до Levels 4–8 и добавить 27 локальных audio assets. До кодирования состава Levels 4–8 в репозиторий должна быть внесена точная owner-approved матрица D024; отсутствующие параметры не выводить самостоятельно.
