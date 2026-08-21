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

Статус: **DONE (100%) / OWNER ACCEPTED**.

- [x] Android-проект;
- [x] Home;
- [x] Level 1 `А/М`;
- [x] 10 вопросов;
- [x] озвучка и выбор ответа;
- [x] Room Attempt / history persistence;
- [x] result screen;
- [x] unit tests / debug build / CI;
- [x] physical-device smoke;
- [x] owner acceptance.

Переносы D019 закрыты в M2.

## M2 — LearningEngine + audio/curriculum foundation

Статус: **DONE (100%) / OWNER ACCEPTED**.

### M2.1 — Hybrid audio foundation + Curriculum Levels 1–3

- [x] `AudioPlayer` abstraction;
- [x] `HybridAudioPlayer`: local `res/raw` first + TTS fallback;
- [x] TTS service visibility;
- [x] Curriculum Levels 1–3;
- [x] 10 вопросов;
- [x] level unlock >=80% / 8 из 10;
- [x] progression DataStore.

### M2.2 — Per-letter statistics / Room 2

- [x] per-letter statistics;
- [x] `LetterProgressEntity`;
- [x] `SessionResultEntity`;
- [x] migration 1→2 + backfill;
- [x] response-time aggregate;
- [x] JVM tests.

### M2.3 — Local assets + combined smoke

- [x] local OGG для первых 6 букв;
- [x] `correct` / `wrong` / `level_complete`;
- [x] local-audio mapping/fallback tests;
- [x] owner smoke: audio / levels / summary / persistence — PASS.

### M2.4 — Adaptive LearningPolicy v3

- [x] mastery states;
- [x] weighted selection;
- [x] retry queue / delayed checks;
- [x] confusion tracking;
- [x] deterministic JVM tests;
- [x] `assembleDebug` / CI PASS.

### M2.5 — Closure Audit

Статус: **COMPLETE / PASS 7 OF 7 / OWNER ACCEPTED**.

## M3 — Parent mode, полный Curriculum и long-term retention

Статус: **DONE (100%) / OWNER ACCEPTED**.

### M3.1 — Parent mode / Parent Dashboard — D023

- [x] арифметический Parental Gate;
- [x] Parent Dashboard;
- [x] сводка по всем 33 буквам;
- [x] детализация и статистика;
- [x] Room schema 2 unchanged;
- [x] JVM tests / `assembleDebug` / CI;
- [x] Pixel 7a smoke / owner acceptance.

### M3.2 — Curriculum v3 / full local audio — D024

- [x] `curriculumVersion = 3`;
- [x] 8 уровней;
- [x] 33 русские буквы;
- [x] 10 вопросов / unlock >=80%;
- [x] 33 letter OGG;
- [x] 3 UI sounds;
- [x] mapping всех 33 букв;
- [x] local OGG first / TTS fallback;
- [x] tests / CI / Pixel 7a smoke / owner acceptance.

### M3.3 — LearningPolicy v4 / Retention Decay — D025

- [x] `learningPolicyVersion = 4`;
- [x] 7-day retention horizon;
- [x] decay при elapsed `> 7 days`;
- [x] effective `PRACTICING` / weight 2.0;
- [x] successful re-check restore;
- [x] historical Attempt read-only;
- [x] deterministic time-based tests;
- [x] Parent Dashboard retention indicator;
- [x] CI / Pixel 7a smoke / owner acceptance.

### M3.4 — Closure Evidence Audit

Статус: **PASS — 7/7 / OWNER ACCEPTED**.

## M4 — Игровая оболочка / UI polish / release preparation

Статус: **DONE (100%) / OWNER ACCEPTED**.

### M4.1 — UI Delight

- [x] 4x2 Home level selector, все 8 уровней видимы;
- [x] native Compose celebration/confetti на успешном результате;
- [x] press-scale animation карточек ответа;
- [x] victory/failure Compose tests;
- [x] LearningPolicy/Curriculum/Room schema unchanged.

### M4.2 — Parent settings / safe progress reset

- [x] persistent Voiceover setting в Preferences DataStore;
- [x] persistent Sound FX setting в Preferences DataStore;
- [x] настройки привязаны к `HybridAudioPlayer`;
- [x] Parent Dashboard switches;
- [x] безопасный reset с confirmation dialog;
- [x] транзакционная очистка `attempts` / `letter_progress` / `session_results`;
- [x] progression reset к Level 1;
- [x] Settings / audio / reset / Compose tests.

### M4.3 — Release hardening

- [x] R8/minification ENABLED;
- [x] resource shrinking ENABLED;
- [x] `app/proguard-rules.pro`;
- [x] Room / Compose / DataStore keep rules;
- [x] `res/raw/keep.xml` для динамически адресуемых `sound_*`;
- [x] lifecycle-safe audio `stop()` / idempotent `release()`;
- [x] lifecycle-owned initialization coroutine;
- [x] CI triple gate: `test`, `assembleDebug`, `assembleRelease`;
- [x] release build R8 + resource shrink PASS.

### M4.4 — Closure Evidence Audit

Статус: **PASS — 7/7 / OWNER ACCEPTED**.

- [x] M4 DoD evidence audit — PASS 7/7;
- [x] static review — PASS;
- [x] tests CI — PASS;
- [x] `assembleDebug` — PASS;
- [x] `assembleRelease` with R8/resource shrink — PASS;
- [x] physical release smoke — PASS, Pixel 7a;
- [x] owner acceptance — ACCEPTED.

### Gate M4 → MVP v1.0.0

- [x] M1–M4 DONE (100%);
- [x] M4.4 Closure Evidence Audit PASS 7/7;
- [x] CI Triple Gate PASS;
- [x] Pixel 7a final smoke PASS;
- [x] owner acceptance ACCEPTED;
- [x] `main` release state confirmed;
- [x] App_ABC MVP v1.0.0 declared COMPLETE / PRODUCTION READY.

## Post-MVP Roadmap

Следующие пункты **не являются незакрытыми требованиями MVP v1.0.0**. Они выполняются только по отдельному решению владельца.

### Curriculum / UX validation

Часть прежнего M5 уже закрыта в M3.2: Curriculum v3, 8 уровней, 33 буквы и полный local audio suite реализованы. Дальнейшие улучшения:

- [ ] дополнительная проверка визуально похожих групп;
- [ ] специализированные distractors при необходимости;
- [ ] расширенное домашнее UX-тестирование;
- [ ] продуктовая проверка по `SUCCESS_METRICS.md`;
- [ ] дополнительные игровые показатели, если они дают пользу без изменения LearningPolicy.

### Слоги, чтение и мини-игры

- [ ] слоги (`МА → МО → МУ`);
- [ ] слова и раннее чтение;
- [ ] строчные буквы;
- [ ] короткие мини-игры на узнавание/повторение;
- [ ] дополнительные лёгкие достижения/визуальные награды без игровой экономики;
- [ ] новые режимы только после отдельного product decision.

### Speech recognition experiment

- [ ] отдельный test screen микрофона;
- [ ] ru-RU recognition;
- [ ] normalizer;
- [ ] `CORRECT / WRONG / NOT_RECOGNIZED`;
- [ ] `NOT_RECOGNIZED` не снижает прогресс;
- [ ] аудио ребёнка не сохраняется по умолчанию;
- [ ] тест на реальной детской речи;
- [ ] решение о включении/ограничении/отказе.

Speech recognition **не является обязательным условием MVP**.

### Публикация / distribution

Техническая кодовая база v1.0.0 уже имеет release build; store publication остаётся отдельным deployment-процессом:

- [x] release build собирается;
- [x] R8/minification и resource shrinking проверены;
- [x] безопасный локальный reset реализован;
- [ ] подготовить production signing key вне Git;
- [ ] подготовить store metadata / privacy text;
- [ ] проверить актуальные требования Google Play / RuStore;
- [ ] closed/internal testing при публикации;
- [ ] решение по лицензии репозитория;
- [ ] явное owner decision о публикации.

### Другие направления

- [ ] iOS;
- [ ] облачная синхронизация / аккаунты — только при отдельном privacy/product решении.

## Правило roadmap

`BACKLOG.md` показывает закрытый MVP и Post-MVP направления. `NEXT_TASK.md` не содержит обязательной задачи после v1.0.0: следующий шаг выполняется только по желанию владельца.
