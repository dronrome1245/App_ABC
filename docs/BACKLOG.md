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

Статус: **реализация готова к ручной приёмке владельцем; M1 ещё не завершён**.

- [x] создать Android-проект;
- [x] Home;
- [x] Level 1: А/М;
- [x] TTS-запрос;
- [x] выбор ответа;
- [x] случайно менять позиции букв между вопросами;
- [x] Room Attempt с `sessionId`;
- [x] предусмотреть поля версии LearningPolicy/Curriculum;
- [x] результат 10 вопросов (сессия);
- [x] unit tests;
- [x] debug build;
- [ ] запуск на телефоне (acceptance владельцем);
- [ ] ручной acceptance по `DEFINITION_OF_DONE.md`;
- [x] Gradle-проект настроен;
- [x] CI: JVM unit tests + debug build.

### Gate M1 → M2

Нельзя переходить к M2, пока владелец не проверит на реальном телефоне:

- размер кнопок;
- TTS;
- скорость упражнения;
- реакцию на ошибку;
- отсутствие угадывания по фиксированному расположению;
- сохранение данных;
- прохождение полной сессии из 10 вопросов на `А/М`.

## M2 — Учебный движок v1

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
- [ ] GitHub Actions: unit tests + debug build, если не добавлено ранее.

### Gate M2 → M3

- [ ] все инварианты LearningEngine покрыты тестами;
- [ ] пороги централизованы;
- [ ] алгоритм не меняется без версии/decision log;
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
- [ ] проверить TTS-названия всех букв;
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
