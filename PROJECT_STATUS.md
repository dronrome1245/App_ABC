# PROJECT_STATUS.md

Дата обновления: 2026-08-19

## Текущий этап

**Milestone 1 — DONE (100%). Milestone 2 — In Planning / Kickoff.**

M1 принят владельцем после проверки на реальном Android-устройстве. Принятый вертикальный срез: Level 1 `А/М`, 10 вопросов, рабочая озвучка, выбор ответа, сохранение Attempt/history и основной пользовательский поток.

Три требования, которые были записаны в раннем M1 DoD, но фактически не реализованы, не считаются выполненными. По решению владельца D019 они явно перенесены в M2: разбивка результата по А/М, retry ошибочной target-буквы и декларация `<queries>` для `TTS_SERVICE` при сохранении TTS fallback.

## Статусы M1

- STATIC_REVIEW_STATUS: PASS
- TESTS_CI_STATUS: PASS
- LOCAL_ANDROID_RUNTIME_STATUS: PASS
- PHYSICAL_DEVICE_RUNTIME_STATUS: PASS
- OWNER_ACCEPTANCE_STATUS: ACCEPTED

## Что реализовано и принято в M1

- Android-проект на Kotlin + Jetpack Compose;
- главный экран;
- упражнение «услышь название → выбери букву»;
- Level 1 с буквами `А` и `М`;
- 10 вопросов в одной сессии;
- случайная перестановка двух вариантов;
- TTS с явно заданным `spokenName`;
- сохранение Attempt в Room с `sessionId`, `levelId`, версиями LearningPolicy/Curriculum;
- общий экран результата;
- чистый Kotlin LearningEngine;
- JVM unit tests;
- GitHub Actions: JVM unit tests + debug build;
- ручная проверка владельцем на реальном устройстве.

## Owner-approved перенос в M2

Обязательные задачи следующей итерации:

1. результат по каждой букве отдельно (`А`, `М`, далее по curriculum);
2. retry queue / управляемый возврат ошибочной target-буквы;
3. `<queries>` для `android.intent.action.TTS_SERVICE`, если TTS используется как fallback;
4. гибридный `AudioPlayer`: pre-recorded local audio first + TTS fallback;
5. расширение модели уровней Curriculum;
6. реализация LearningEngine v1 по M2 DoD: mastery states, weighted selection, level unlock, delayed checks и тесты инвариантов.

## Архитектурное решение M2 по аудио

Согласно D020 основной источник озвучки — локальные заранее записанные `WAV`/`OGG` в `res/raw`. Системный TTS остаётся fallback. UI и LearningEngine должны работать через единый интерфейс `AudioPlayer` и не зависеть от конкретного Android audio API.

## Контроль AI-процесса

После инцидента с неполным аудитом M1 усилен milestone closure gate: перед task packet на закрытие milestone AI обязан составить построчную матрицу соответствия каждому DoD-критерию с доказательством из кода/tests/CI/runtime либо явным `DEFERRED_BY_OWNER` с ссылкой на решение в `DECISIONS.md`.

Следующая и единственная текущая задача описана в `NEXT_TASK.md`.
