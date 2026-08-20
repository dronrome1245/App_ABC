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
- [x] результат 10 вопросов;
- [x] unit tests;
- [x] debug build;
- [x] запуск на телефоне;
- [x] owner acceptance;
- [x] CI: JVM unit tests + debug build.

Переносы D019 включены в M2, а не считаются выполненными в M1.

## M2 — Учебный движок v1/v2 и расширение curriculum

Статус: **M2.3 IMPLEMENTATION COMPLETE / OWNER SMOKE PENDING; M2 OVERALL NOT COMPLETE**.

### M2.1 — Hybrid audio foundation + Curriculum Levels 1–3

- [x] интерфейс `AudioPlayer`;
- [x] `HybridAudioPlayer`: local `res/raw` first + TTS fallback;
- [x] `<queries>` для `android.intent.action.TTS_SERVICE`;
- [x] dependency injection через простой composition root без Hilt/Koin;
- [x] централизованная модель Curriculum;
- [x] `curriculumVersion = 2`;
- [x] Level 1: `А`, `М`;
- [x] Level 2: `О`, `У`;
- [x] Level 3: `С`, `Н`;
- [x] старые буквы сохраняются в пуле следующих уровней;
- [x] 10 вопросов на сессию;
- [x] `learningPolicyVersion = 2`;
- [x] level unlock >=80% на полной сессии 10 вопросов (8/10);
- [x] хранение current/max unlocked level в Preferences DataStore;
- [x] UI выбора разблокированного уровня;
- [x] runtime передача выбранного `levelId` в Exercise.

### M2.2 — Per-letter statistics / Room 2

- [x] статистика по каждой букве из Room Attempt history;
- [x] разбивка результата по буквам (D019);
- [x] persistent `LetterProgressEntity`;
- [x] `SessionResultEntity` и история завершённых сессий;
- [x] migration 1->2 + backfill старой истории;
- [x] response-time aggregate;
- [x] переиспользование существующего `LevelProgressionStore`;
- [x] JVM tests агрегации.

### M2.3 — Local assets + combined smoke

- [x] local OGG assets для `А/М/О/У/С/Н`;
- [x] `sound_correct`;
- [x] `sound_wrong`;
- [x] `sound_level_complete`;
- [x] mapping Curriculum v2 -> local raw resource;
- [x] feedback sound hook после ответа;
- [x] Session Summary completion sound hook;
- [x] JVM tests mapping/fallback policy;
- [ ] owner smoke: качество локальной озвучки;
- [ ] owner smoke: levels/unlock;
- [ ] owner smoke: per-letter Summary;
- [ ] owner smoke: persistence;
- [ ] owner smoke: migration 1->2 на существующей установке, если доступна schema v1.

### Остальной обязательный M2 LearningEngine

- [ ] retry queue (D019);
- [ ] mastery states;
- [ ] weighted selection;
- [ ] delayed checks;
- [ ] weak-letter weighting;
- [ ] full LearningPolicy config без magic numbers;
- [ ] тесты всех инвариантов LearningEngine.

### Gate M2 → M3

- [ ] все инварианты LearningEngine покрыты тестами;
- [ ] пороги централизованы;
- [ ] алгоритм не меняется без версии/decision log;
- [ ] переносы D019 полностью закрыты, включая retry;
- [ ] runtime evidence M2 собран;
- [ ] Milestone Closure Evidence Audit без FAIL/UNKNOWN;
- [ ] owner acceptance;
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

## M4 — игровая оболочка

- [ ] карта/список уровней;
- [ ] 1–3 звезды;
- [ ] короткая анимация открытия;
- [ ] простые достижения;
- [ ] сессия 3–7 минут + «Ещё потренироваться»;
- [ ] игровые показатели не подменяют success metrics.

## M5 — полный алфавит

- [ ] согласовать curriculumVersion для 33 букв;
- [ ] согласовать оставшийся порядок 33 букв;
- [ ] проверить визуально похожие группы;
- [ ] подобрать distractors;
- [ ] проверить озвучку всех букв;
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
