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

Статус: **DONE (100%) / OWNER ACCEPTED**.

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
- [x] automated migration 1→2 evidence — PASS;
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

Статус: **COMPLETE / PASS 7 OF 7 / OWNER ACCEPTED**.

- [x] построчный evidence audit M2 DoD;
- [x] documentation/code drift проверен;
- [x] обязательные LearningEngine invariants подтверждены;
- [x] переносы D019 закрыты;
- [x] automated migration evidence зафиксирован, device `NOT_TESTED` отделён;
- [x] `FAIL` / `UNKNOWN` отсутствуют;
- [x] owner acceptance всего M2 — ACCEPTED.

### Gate M2 → M3

- [x] adaptive LearningEngine реализован;
- [x] централизованные policy thresholds/weights;
- [x] version discipline / D022;
- [x] retry D019 реализован;
- [x] per-letter D019 реализован;
- [x] automated migration 1→2 evidence;
- [x] runtime evidence M2 собрано;
- [x] Milestone Closure Evidence Audit без FAIL/UNKNOWN;
- [x] owner acceptance M2.

## M3 — Parent mode, полный Curriculum и long-term retention

Статус: **DONE (100%) / OWNER ACCEPTED**.

Ранний предварительный M3 backlog заменён фактически утверждённым owner scope D023–D025. Пункты, которые не вошли в D023–D025, не считаются автоматически реализованными и могут быть возвращены в будущий scope отдельным решением владельца.

### M3.1 — Parent mode / Parent Dashboard — D023

- [x] арифметический Parental Gate;
- [x] сложение и вычитание без отрицательного результата;
- [x] Parent Dashboard;
- [x] сводка по всем 33 буквам с `NOT_STARTED` defaults;
- [x] карточка/детали буквы и статистика;
- [x] summary metrics Parent Dashboard;
- [x] Room schema остаётся 2;
- [x] JVM tests / `assembleDebug` / CI;
- [x] runtime smoke на Pixel 7a;
- [x] owner acceptance.

### M3.2 — Curriculum v3 / полный локальный audio suite — D024

- [x] `curriculumVersion = 3`;
- [x] 8 уровней;
- [x] полный набор 33 русских букв;
- [x] накопленные pool sizes 2 / 4 / 6 / 9 / 12 / 15 / 19 / 33;
- [x] 10 вопросов на сессию без изменений;
- [x] level unlock >=80% / 8 из 10 без изменений;
- [x] 33 letter OGG assets;
- [x] 3 UI sounds (`correct` / `wrong` / `level_complete`);
- [x] mapping всех 33 букв;
- [x] local OGG first / TTS fallback сохранён;
- [x] Curriculum/audio JVM tests;
- [x] `assembleDebug` / CI;
- [x] physical-device smoke на Pixel 7a;
- [x] owner acceptance.

### M3.3 — LearningPolicy v4 / Retention Decay — D025

- [x] `learningPolicyVersion = 4`;
- [x] 7-day retention horizon (`604800000 ms`);
- [x] decay только при elapsed `> 7 days`;
- [x] decayed `MASTERED` оценивается как effective `PRACTICING`;
- [x] decayed selection weight = `2.0`;
- [x] retention anchor = latest successful Attempt timestamp;
- [x] successful re-check восстанавливает `MASTERED`, если базовые критерии сохраняются;
- [x] historical Attempt не переписываются;
- [x] deterministic time-based JVM tests: `<7d`, `=7d`, `>7d`, restore;
- [x] Parent Dashboard: «Требует повторения» + дата последней тренировки;
- [x] Room schema остаётся 2;
- [x] `assembleDebug` / Android CI PASS;
- [x] physical-device smoke на Pixel 7a;
- [x] owner acceptance.

### M3.4 — Closure Evidence Audit

Статус: **PASS — 7/7 / OWNER ACCEPTED**.

- [x] D023 evidence — PASS;
- [x] D024 evidence — PASS;
- [x] D025 evidence — PASS;
- [x] Room schema 2 / no M3 migration — PASS;
- [x] historical Attempt read-only time evaluation — PASS;
- [x] JVM tests + `assembleDebug` + CI evidence — PASS;
- [x] Pixel 7a owner smoke / Owner Acceptance — PASS / ACCEPTED.

### Gate M3 → M4

- [x] M3 implementation complete;
- [x] M3.4 Closure Evidence Audit PASS 7/7;
- [x] physical device status PASS — Pixel 7a;
- [x] owner acceptance M3 — ACCEPTED;
- [x] PR #9 merged to `main`;
- [x] Milestone 4 moved to Planning / Kickoff.

## M4 — Игровая оболочка / UI polish / release preparation

Статус: **In Planning / Kickoff — код не начинать до owner approval scope**.

Kickoff scope для архитектурного планирования:

- [ ] UI/UX-полировка тренировочного потока;
- [ ] анимации успешного ответа;
- [ ] салют/конфетти на Jetpack Compose для завершения/успеха;
- [ ] настройки звука в Parent Dashboard;
- [ ] подготовка ProGuard/R8 release build plan и release checks;
- [ ] определить MUST / SHOULD / OUT OF SCOPE;
- [ ] подготовить test plan / owner smoke plan;
- [ ] получить owner approval scope до начала реализации.

Ранее запланированные игровые элементы, которые могут быть рассмотрены при утверждении M4 scope:

- [ ] карта/список уровней;
- [ ] 1–3 звезды;
- [ ] короткая анимация открытия;
- [ ] простые достижения;
- [ ] сессия 3–7 минут + «Ещё потренироваться»;
- [ ] игровые показатели не подменяют success metrics.

## M5 — Полный алфавит / post-M3 curriculum validation

Часть прежнего M5 фактически закрыта решением D024 и M3.2:

- [x] `curriculumVersion = 3` для 33 букв;
- [x] owner-approved порядок/распределение всех 33 букв по 8 уровням;
- [x] полный локальный audio suite 33 букв;

Остаются будущие продуктовые проверки:

- [ ] проверить визуально похожие группы;
- [ ] уточнить специализированные distractors при необходимости;
- [ ] определить число вариантов ответа по этапам, если оно будет меняться;
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
