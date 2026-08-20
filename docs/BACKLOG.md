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

## M2 — LearningEngine + audio/curriculum foundation

Статус: **M2.4 COMPLETE / M2.5 CLOSURE AUDIT ACTIVE; M2 OVERALL PENDING OWNER ACCEPTANCE**.

### M2.1 — Hybrid audio foundation + Curriculum Levels 1–3

- [x] интерфейс `AudioPlayer`;
- [x] `HybridAudioPlayer`: local `res/raw` first + TTS fallback;
- [x] `<queries>` для `android.intent.action.TTS_SERVICE`;
- [x] централизованная модель Curriculum;
- [x] `curriculumVersion = 2`;
- [x] Levels 1–3: `А/М`, `О/У`, `С/Н`;
- [x] старые буквы сохраняются в пуле следующих уровней;
- [x] 10 вопросов на сессию;
- [x] level unlock >=80% / 8 из 10;
- [x] current/max unlocked level в DataStore;
- [x] UI выбора разблокированного уровня;
- [x] runtime передача выбранного `levelId`.

### M2.2 — Per-letter statistics / Room 2

- [x] статистика по каждой букве из Room Attempt history;
- [x] разбивка результата по буквам (D019);
- [x] persistent `LetterProgressEntity`;
- [x] `SessionResultEntity` и история завершённых сессий;
- [x] migration 1→2 + backfill старой истории;
- [x] response-time aggregate;
- [x] JVM tests агрегации.

### M2.3 — Local assets + combined smoke

- [x] local OGG assets для `А/М/О/У/С/Н`;
- [x] `sound_correct` / `sound_wrong` / `sound_level_complete`;
- [x] mapping Curriculum v2 -> local raw resource;
- [x] feedback/completion audio hooks;
- [x] JVM tests mapping/fallback policy;
- [x] owner smoke: audio — PASS;
- [x] owner smoke: levels/unlock — PASS;
- [x] owner smoke: per-letter Summary — PASS;
- [x] owner smoke: persistence — PASS;
- [ ] device migration 1→2 — NOT_TESTED; не выдавать за device PASS.

### M2.4 — Adaptive LearningPolicy v3

Статус: **COMPLETE / CI PASS**.

- [x] D022: `learningPolicyVersion = 3`;
- [x] mastery states `INTRODUCED / PRACTICING / MASTERED`;
- [x] thresholds `<3`, `>=3`, `>=5 + recent accuracy >=85%`;
- [x] centralized recent window/weights/retry spacing без UI magic numbers;
- [x] weighted selection;
- [x] `MASTERED = 1.0`, `INTRODUCED = 2.0`, `PRACTICING = 2.0…3.0`;
- [x] сильная буква сохраняет ненулевой шанс;
- [x] retry queue (D019);
- [x] ошибочная target возвращается через 2–4 других вопроса при наличии места;
- [x] retry не бесконечен и не расширяет 10-вопросную сессию;
- [x] max target-series invariant;
- [x] delayed success при spacing >=2;
- [x] confusion pair tracking;
- [x] historical Attempt используются как вход adaptive policy;
- [x] deterministic JVM invariant tests;
- [x] automated real-SQLite migration 1→2 test;
- [x] preservation/backfill Attempt -> `letter_progress` / `session_results`;
- [x] JVM tests PASS;
- [x] `assembleDebug` PASS;
- [x] Android CI PASS.

### M2.5 — Closure Audit

Статус: **NEXT / ACTIVE AFTER M2.4**.

- [ ] построчный evidence audit каждого M2 DoD criterion;
- [ ] проверить documentation/code drift;
- [ ] подтвердить все обязательные LearningEngine invariants;
- [ ] подтвердить переносы D019;
- [ ] зафиксировать automated migration evidence и отдельно device `NOT_TESTED`;
- [ ] исключить `FAIL` / `UNKNOWN`;
- [ ] запросить owner acceptance всего M2;
- [ ] после owner acceptance перевести M2 в DONE и только затем открыть M3.

### Gate M2 → M3

- [x] adaptive LearningEngine реализован;
- [x] централизованные policy thresholds/weights;
- [x] version discipline / D022;
- [x] retry D019 реализован;
- [x] per-letter D019 реализован;
- [x] automated migration 1→2 evidence;
- [x] runtime evidence M2.3 собрано;
- [ ] Milestone Closure Evidence Audit без FAIL/UNKNOWN;
- [ ] owner acceptance M2.

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

`BACKLOG.md` показывает этапы продукта. Текущая одна ближайшая задача всегда фиксируется в `NEXT_TASK.md`.
