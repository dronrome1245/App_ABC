# App_ABC — адаптивный тренажёр русских букв

Мобильное Android-приложение для коротких тренировок узнавания и запоминания русских букв. Основная идея заимствована из тренажёров нот: начинаем с малого набора символов, постепенно добавляем новые, а уже пройденные продолжают появляться в следующих упражнениях.

> [!WARNING]
> **AUDIO DISTRIBUTION BLOCKER:** если для D027 / Audio Pack v2 используется HOME-ONLY аудиотека (например, локальный пакет GCompris), перед любой публикацией или передачей APK/AAB/репозитория её необходимо полностью заменить на аудиопак с подтверждённой лицензией для распространения. HOME-ONLY аудио нельзя коммитить в публичный репозиторий. См. [docs/AUDIO_DISTRIBUTION_NOTICE.md](docs/AUDIO_DISTRIBUTION_NOTICE.md).

**Post-MVP D027 status:** GCompris HOME-ONLY Audio Pack v2 установлен `33/33`; bundled QC и Android CI (`test` / `assembleDebug` / `assembleRelease`) — PASS. До закрытия AUDIO-02 для домашней версии остаётся owner device smoke 33 букв.

## Текущий статус

| Milestone | Статус |
| --- | --- |
| Milestone 1 | DONE (100%) / OWNER ACCEPTED |
| Milestone 2 | DONE (100%) / OWNER ACCEPTED |
| Milestone 3 | DONE (100%) / OWNER ACCEPTED |
| Milestone 4 | DONE (100%) / OWNER ACCEPTED |

## Статус: Продукт полностью готов к релизу (Production Ready)

**App_ABC MVP v1.0.0 — COMPLETE / PRODUCTION READY (100%).**

Финальный Milestone 4 закрыт после M4.4 Closure Evidence Audit со статусом PASS 7/7. Владелец выполнил финальный smoke-тест на физическом Pixel 7a — PASS. Финальный release hardening проверен CI triple gate: `test`, `assembleDebug`, `assembleRelease` — PASS. Release build использует R8/minification и resource shrinking; динамически адресуемые 33 letter OGG + 3 UI sounds защищены `res/raw/keep.xml`.

Кодовая база `main` находится в релизном состоянии. Подготовка production signing keys и публикация в Google Play / RuStore являются отдельным deployment-шагом и не входят в закрытый MVP v1.0.0.

M1 принят владельцем на реальном Android-устройстве: Level 1 `А/М`, 10 вопросов, рабочая озвучка, выбор ответа, сохранение истории и основной пользовательский поток.

M2 закрыт после M2.5 Closure Evidence Audit: 7/7 критериев PASS, CI и `assembleDebug` PASS, owner device smoke PASS, owner acceptance ACCEPTED. В M2 реализованы hybrid local-audio-first/TTS-fallback, Curriculum Levels 1–3, per-letter Session Summary/Room persistence, adaptive LearningPolicy v3 с mastery/weighted selection/retry/delayed checks и automated migration 1→2 test.

M3 закрыт после M3.4 Closure Evidence Audit: 7/7 критериев PASS, CI/JVM tests/`assembleDebug` PASS, физический smoke-тест на Pixel 7a PASS, owner acceptance ACCEPTED. В M3 реализованы Parental Gate и Parent Dashboard, Curriculum v3 на 8 уровней и все 33 русские буквы, полный набор из 33 letter OGG + 3 UI sounds и LearningPolicy v4 с 7-дневным Retention Decay по D025.

M4 закрыт после M4.4 Closure Evidence Audit: UI delight/celebration, persistent sound settings, safe progress reset, release hardening R8/resource shrinking, lifecycle-safe audio cleanup, CI release gate и финальный Pixel 7a smoke — PASS / OWNER ACCEPTED.

Подробно: [PROJECT_STATUS.md](PROJECT_STATUS.md) и [NEXT_TASK.md](NEXT_TASK.md).

## Продуктовая стратегия

MVP v1.0.0 завершён как локальный семейный Android-инструмент. Дальнейшее развитие и публичная публикация выполняются отдельными Post-MVP шагами без изменения факта закрытия v1.0.0.

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
- родительского контроля прогресса и настроек.

## Что входит в первую версию

- русский алфавит из 33 заглавных букв;
- 8 игровых уровней Curriculum v3;
- упражнение «услышь название буквы → выбери её»;
- адаптивный подбор вопросов LearningPolicy v4;
- повторение старых букв в новых уровнях;
- статистика по буквам и история сессий;
- Parent Dashboard и Parental Gate;
- persistent-настройки озвучки букв и звуковых эффектов;
- безопасный сброс прогресса с подтверждением;
- мягкая игровая мотивация и позитивный экран результата;
- локальные 33 letter OGG + 3 UI sounds с TTS fallback;
- локальное хранение прогресса на устройстве без обязательной регистрации и сервера;
- release build с R8/minification и resource shrinking.

## Что сознательно НЕ входит в MVP

- фонетические упражнения со звуками речи;
- слоги, слова и чтение (`МА → МО → МУ` — Post-MVP);
- адаптивная визуальная иерархия действий на `ResultScreen` для детей без навыка чтения по D027 (Post-MVP);
- Audio Pack v2 с более мягкой, певучей и протяжной озвучкой букв длительностью 400–700 мс по D027 (Post-MVP);
- рисование букв пальцем;
- распознавание почерка;
- iOS;
- серверная часть, аккаунты и облачная синхронизация;
- реклама;
- сложные персонажи, магазин наград и игровая экономика;
- медицинская диагностика.

## Распознавание произношения ребёнка

Режим «покажи букву → ребёнок произносит её название → приложение оценивает ответ» остаётся отдельным Post-MVP экспериментом. Распознавание коротких ответов вроде «эм», «эн», «эль» может ошибаться, особенно на детской речи. `Не распознано` не должно превращаться в учебную ошибку.

См. [docs/SPEECH_RECOGNITION.md](docs/SPEECH_RECOGNITION.md).

## Технологический стек

- Kotlin;
- Jetpack Compose;
- Android Studio;
- Room schema 2 — статистика и история попыток;
- Preferences DataStore — progression и настройки;
- локальные audio assets — основной источник названий букв;
- Android TextToSpeech — fallback;
- R8/ProGuard + resource shrinking для release build.

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
17. [NEXT_TASK.md](NEXT_TASK.md) — следующий необязательный deployment/Post-MVP шаг.
18. [docs/AUDIO_DISTRIBUTION_NOTICE.md](docs/AUDIO_DISTRIBUTION_NOTICE.md) — обязательный release blocker для HOME-ONLY аудиопаков.

Для Gemini дополнительно: [prompts/GEMINI_WEB_SYSTEM_PROMPT.md](prompts/GEMINI_WEB_SYSTEM_PROMPT.md) и [GEMINI.md](GEMINI.md).

## Главный принцип разработки

Разработка идёт небольшими проверяемыми milestone/slice. Закрытие milestone требует evidence audit без `FAIL`/`UNKNOWN`, CI и требуемого runtime/owner evidence. `main` должен оставаться рабочей веткой. Существенные изменения учебного алгоритма требуют версии LearningPolicy и записи в `docs/DECISIONS.md`.
