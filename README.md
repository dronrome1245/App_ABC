# App_ABC — адаптивный тренажёр русских букв

Мобильное Android-приложение для коротких тренировок узнавания и запоминания русских букв. Основная идея заимствована из тренажёров нот: начинаем с малого набора символов, постепенно добавляем новые, а уже пройденные продолжают появляться в следующих упражнениях.

## Текущий статус

| Milestone | Статус |
| --- | --- |
| Milestone 1 | DONE / OWNER ACCEPTED |
| Milestone 2 | DONE / OWNER ACCEPTED |
| Milestone 3 | DONE / OWNER ACCEPTED |
| Milestone 4 | In Planning / Kickoff |

M1 принят владельцем на реальном Android-устройстве: Level 1 `А/М`, 10 вопросов, рабочая озвучка, выбор ответа, сохранение истории и основной пользовательский поток.

M2 закрыт после M2.5 Closure Evidence Audit: 7/7 критериев PASS, CI и `assembleDebug` PASS, owner device smoke PASS, owner acceptance ACCEPTED. В M2 реализованы hybrid local-audio-first/TTS-fallback, Curriculum Levels 1–3, per-letter Session Summary/Room persistence, adaptive LearningPolicy v3 с mastery/weighted selection/retry/delayed checks и automated migration 1→2 test.

M3 закрыт после M3.4 Closure Evidence Audit: 7/7 критериев PASS, CI/JVM tests/`assembleDebug` PASS, физический smoke-тест на Pixel 7a PASS, owner acceptance ACCEPTED. В M3 реализованы Parental Gate и Parent Dashboard, Curriculum v3 на 8 уровней и все 33 русские буквы, полный набор из 33 letter OGG + 3 UI sounds и LearningPolicy v4 с 7-дневным Retention Decay по D025.

Активен **Milestone 4 — In Planning / Kickoff**. До утверждения scope код M4 не начинается; планирование охватывает UI/UX-полировку, анимации успеха/салют-конфетти в Compose, настройки звука в Parent Dashboard и подготовку ProGuard/R8 release build.

Подробно: [PROJECT_STATUS.md](PROJECT_STATUS.md) и [NEXT_TASK.md](NEXT_TASK.md).

## Продуктовая стратегия

Сначала делается **семейный пилот**: простой локальный Android-инструмент, который реально используется дома и накапливает достоверную статистику. Решение о превращении его в публичный продукт принимается после проверки полезности и UX.

См. [docs/PRODUCT_CHARTER.md](docs/PRODUCT_CHARTER.md) и [docs/SUCCESS_METRICS.md](docs/SUCCESS_METRICS.md).

## Цель MVP

Сделать простой тренажёр, который помогает закреплять визуальный образ буквы и её название за счёт:

- активного выбора ответа;
- постепенного увеличения количества букв;
- повторения уже изученных букв;
- более частого появления проблемных букв;
- коротких сессий примерно 3–7 минут;
- игровой прогрессии по уровням;
- статистики по каждой букве;
- отдельной тренировки слабых букв.

## Что входит в первую версию

- только русский алфавит;
- на старте — печатные заглавные буквы;
- уровни, которые постепенно добавляют буквы;
- упражнение «услышь название буквы → выбери её»;
- адаптивный подбор вопросов;
- повторение старых букв в новых уровнях;
- статистика: попытки, правильность, время ответа, последние ошибки;
- карта путаницы: какую букву ребёнок выбирает вместо правильной;
- экран «Трудные буквы» и отдельная тренировка проблемного набора;
- родительский доступ к статистике и настройкам;
- мягкая игровая мотивация: уровни, звёзды/награды, прогресс;
- рекомендуемая короткая сессия, но без блокировки приложения после её завершения;
- локальное хранение прогресса на устройстве без обязательной регистрации и сервера.

## Что сознательно НЕ входит в MVP

- звуки букв и фонетические упражнения;
- слоги, слова и чтение (`МА → МО → МУ` — отдельная будущая версия);
- рисование букв пальцем;
- распознавание почерка;
- iOS;
- серверная часть, аккаунты и облачная синхронизация;
- реклама;
- сложные персонажи, магазин наград и игровая экономика;
- медицинская диагностика.

## Распознавание произношения ребёнка

Режим «покажи букву → ребёнок произносит её название → приложение оценивает ответ» выделен в отдельный экспериментальный этап после основного MVP. Распознавание коротких ответов вроде «эм», «эн», «эль» может ошибаться, особенно на детской речи. `Не распознано` не должно превращаться в учебную ошибку.

См. [docs/SPEECH_RECOGNITION.md](docs/SPEECH_RECOGNITION.md).

## Технологический стек

- Kotlin;
- Jetpack Compose;
- Android Studio;
- Room — статистика и история попыток;
- DataStore — простые настройки и состояние;
- локальные audio assets — основной источник названий букв с M2;
- Android TextToSpeech — fallback для голосовых инструкций/названий букв;
- Android SpeechRecognizer — только для отдельного экспериментального режима произношения.

## Управление проектом

Репозиторий — источник истины. Основные документы:

1. [AGENTS.md](AGENTS.md) — обязательные правила для AI-разработчиков.
2. [docs/PRODUCT_CHARTER.md](docs/PRODUCT_CHARTER.md) — цель, пользователи и стратегия продукта.
3. [docs/SUCCESS_METRICS.md](docs/SUCCESS_METRICS.md) — как измеряем реальное улучшение.
4. [docs/PRODUCT_SPEC.md](docs/PRODUCT_SPEC.md) — требования MVP.
5. [docs/CURRICULUM.md](docs/CURRICULUM.md) — правила последовательности букв и distractors.
6. [docs/LEARNING_ENGINE.md](docs/LEARNING_ENGINE.md) — алгоритм обучения.
7. [docs/UX_GAME_DESIGN.md](docs/UX_GAME_DESIGN.md) — уровни, награды и UX ребёнка.
8. [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — техническая архитектура.
9. [docs/DEFINITION_OF_DONE.md](docs/DEFINITION_OF_DONE.md) — критерии завершения этапов.
10. [docs/RISK_REGISTER.md](docs/RISK_REGISTER.md) — реестр рисков.
11. [docs/DEVELOPMENT_WORKFLOW.md](docs/DEVELOPMENT_WORKFLOW.md) — ветки, PR, CI, rollback и управление задачами.
12. [docs/PRIVACY_RELEASE.md](docs/PRIVACY_RELEASE.md) — данные ребёнка, микрофон и выпуск.
13. [docs/DECISIONS.md](docs/DECISIONS.md) — журнал решений.
14. [docs/BACKLOG.md](docs/BACKLOG.md) — roadmap.
15. [docs/TEST_PLAN.md](docs/TEST_PLAN.md) — техническая и продуктовая проверка.
16. [PROJECT_STATUS.md](PROJECT_STATUS.md) — фактическая точка проекта.
17. [NEXT_TASK.md](NEXT_TASK.md) — одна ближайшая задача.

Для Gemini дополнительно: [prompts/GEMINI_WEB_SYSTEM_PROMPT.md](prompts/GEMINI_WEB_SYSTEM_PROMPT.md) и [GEMINI.md](GEMINI.md).

## Главный принцип разработки

Разработка идёт небольшими проверяемыми milestone/slice. Перед закрытием milestone AI обязан проверить каждый пункт DoD отдельно и не подменять отсутствие функции общей ручной приёмкой. Не реализованный критерий может быть перенесён только явным решением владельца с записью в `DECISIONS.md` и backlog следующего этапа.

`main` должен оставаться рабочей веткой. Существенные изменения учебного алгоритма требуют версии LearningPolicy и записи в `docs/DECISIONS.md`.
