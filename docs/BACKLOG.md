# Backlog / roadmap

## M0 — Repository bootstrap

Статус: **готово**.

- [x] сформулировать цель;
- [x] определить MVP;
- [x] выбрать Android/Kotlin/Compose;
- [x] зафиксировать уровни и статистику;
- [x] вынести speech recognition в эксперимент;
- [x] подготовить инструкции AI-агентам.

## M0.1 — Product & management baseline

Статус: **готово**.

- [x] Product Charter;
- [x] Success Metrics;
- [x] Curriculum management rules;
- [x] Definition of Done;
- [x] Risk Register;
- [x] Development Workflow;
- [x] Privacy & Release rules;
- [x] увязать AGENTS/README/STATUS/TEST PLAN/DECISIONS.

## M1 — Первый вертикальный срез

Статус: **DONE / OWNER ACCEPTED**.

- [x] создать Android-проект;
- [x] Home;
- [x] Level 1: А/М;
- [x] TTS-запрос;
- [x] выбор ответа;
- [x] случайно менять позиции букв между вопросами;
- [x] Room Attempt с `sessionId`;
- [x] предусмотреть поля версии LearningPolicy/Curriculum;
- [x] результат 10 вопросов (общий итог);
- [x] unit tests на реализованные M1-инварианты;
- [x] debug build;
- [x] запуск на реальном телефоне;
- [x] Owner Acceptance;
- [x] Gradle-проект настроен;
- [x] CI: JVM unit tests + debug build.

### Owner-approved deferral D019

Следующие требования ранней редакции M1 не считаются реализованными и перенесены в M2:

- [ ] разбивка результата по каждой букве;
- [ ] retry ошибочной target-буквы через управляемую очередь;
- [ ] `<queries>` для `android.intent.action.TTS_SERVICE` при использовании TTS fallback.

## M2 — Учебный движок v1 + audio/curriculum foundation

Статус: **In Planning / Kickoff**.

### Переносы из M1 — обязательны

- [ ] разбивка результата по каждой букве;
- [ ] retry queue / поздний возврат ошибочной target-буквы;
- [ ] TTS service visibility declaration в Manifest при TTS fallback.

### Audio foundation — D020

- [ ] единый интерфейс `AudioPlayer`;
- [ ] pre-recorded local audio first;
- [ ] TTS fallback;
- [ ] поддержка локальных `WAV`/`OGG` в `res/raw`;
- [ ] Android-specific тест/проверка fallback после реализации;
- [ ] не добавлять случайные/неутверждённые аудиофайлы как финальные ассеты.

### LearningEngine v1

- [ ] конфиг LearningPolicy v1;
- [ ] `learningPolicyVersion`;
- [ ] `curriculumVersion`;
- [ ] mastery states;
- [ ] weighted selection;
- [ ] retry queue;
- [ ] level unlock;
- [ ] повторение старых букв;
- [ ] delayed checks;
- [ ] тесты алгоритма;
- [ ] расширить модель Curriculum/уровней за пределы M1-only константы;
- [x] GitHub Actions: unit tests + debug build.

### Gate M2 → M3

- [ ] все инварианты LearningEngine покрыты тестами;
- [ ] пороги централизованы;
- [ ] алгоритм не меняется без версии/decision log;
- [ ] owner-approved переносы D019 закрыты;
- [ ] audio-first + TTS fallback проверены в допустимом runtime scope;
- [ ] milestone closure evidence matrix не содержит `FAIL`/`UNKNOWN`;
- [ ] нет High-риска LearningEngine без меры контроля.

## M3 — Статистика и Parent mode

- [ ] экран прогресса;
- [ ] статистика буквы;
- [ ] recent accuracy;
- [ ] median response time;
- [ ] confusion matrix;
- [ ] список трудных;
- [ ] тренировка трудных;
- [ ] ручной выбор букв;
- [ ] Parent mode / Settings;
- [ ] сброс локального прогресса с подтверждением;
- [ ] отображение недостатка данных как `недостаточно данных`, а не как плохой результат.

## M4 — Игровая оболочка

- [ ] карта/список уровней;
- [ ] 1–3 звезды;
- [ ] короткая анимация открытия;
- [ ] простые достижения;
- [ ] сессия 3–7 минут + «Ещё потренироваться»;
- [ ] игровые показатели не подменяют success metrics.

## M5 — Полный алфавит

- [ ] согласовать curriculumVersion для 33 букв;
- [ ] согласовать порядок 33 букв;
- [ ] проверить визуально похожие группы;
- [ ] подобрать distractors;
- [ ] проверить локальное аудио/TTS fallback для всех букв;
- [ ] определить число вариантов ответа по этапам;
- [ ] домашнее UX-тестирование;
- [ ] baseline и продуктовая проверка по `SUCCESS_METRICS.md`;
- [ ] устранить High-риски полного curriculum.

## M6 — Speech recognition experiment

- [ ] тестовый экран микрофона;
- [ ] ru-RU recognition;
- [ ] normalizer;
- [ ] CORRECT / WRONG / NOT_RECOGNIZED;
- [ ] микрофон запрашивается только в этом режиме;
- [ ] аудио не сохраняется по умолчанию;
- [ ] тест на реальной детской речи;
- [ ] решение о включении/ограничении/отказе.

M6 **не является обязательным условием завершения MVP**.

## M7 — Release candidate

- [ ] icon;
- [ ] privacy text;
- [ ] release build;
- [ ] безопасная подпись;
- [ ] reset local data;
- [ ] privacy audit;
- [ ] проверить актуальные требования Google Play;
- [ ] closed testing;
- [ ] решение по лицензии репозитория;
- [ ] явное решение о публикации.

## Future

Звуки букв, слоги, слова, чтение, строчные буквы, iOS, облачная синхронизация.

## Правило roadmap

`BACKLOG.md` показывает этапы продукта. Конкретные исполняемые задачи после начала активной разработки желательно вести через GitHub Issues. Текущая одна ближайшая задача всегда фиксируется в `NEXT_TASK.md`.
