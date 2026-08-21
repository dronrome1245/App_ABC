# Definition of Done — App_ABC

Дата: 2026-08-21

## 1. Общий Definition of Done для любой задачи

Задача считается завершённой только если:

- реализовано ровно согласованное изменение;
- основной сценарий не сломан;
- проект компилируется;
- доступные unit tests проходят;
- при изменении UI выполнена ручная проверка на устройстве или явно отмечено, что она ожидает владельца;
- новые данные/настройки не теряются после перезапуска, если по смыслу должны сохраняться;
- документация обновлена, если изменились продуктовые решения, архитектура, статус или следующий шаг;
- нет заведомо оставленных критических TODO без записи в backlog;
- владелец получает короткий отчёт: что сделано, что проверено, что проверить вручную, следующий шаг.

## 1.1. Обязательный Milestone Closure Audit

Перед переводом любого milestone в `DONE/COMPLETED` AI обязан сопоставить **каждый** критерий соответствующего раздела DoD с доказательством.

Допустимые статусы строки аудита:

- `PASS — CODE/TEST/CI/RUNTIME/OWNER_EVIDENCE`;
- `DEFERRED_BY_OWNER — <DECISION_ID>`;
- `FAIL`;
- `UNKNOWN`.

Milestone нельзя закрывать при `FAIL` или `UNKNOWN`. `DEFERRED_BY_OWNER` допустим только при явном решении владельца, записанном в `docs/DECISIONS.md`, и обязательном переносе требования в следующий milestone/backlog.

Нельзя считать owner acceptance доказательством того, что существует функция, которой фактически нет в коде. Нельзя считать наличие кода доказательством runtime-проверки.

## 2. M1 — первый вертикальный срез

Статус: **DONE (100%) / OWNER ACCEPTED**.

M1 DONE, если выполнен принятый владельцем базовый вертикальный срез:

- Android-проект создан и открывается в Android Studio;
- есть экран Home;
- Level 1 содержит `А` и `М`;
- TTS корректно просит найти букву;
- запрос можно повторить;
- ребёнок выбирает одну из двух крупных букв;
- есть спокойная обратная связь;
- выполняется 10 вопросов;
- каждая Attempt сохраняет target, selected, correctness, response time, timestamp, level/session;
- история сохраняется после закрытия приложения;
- есть общий экран результата X/10;
- нет серии одной цели длиннее установленного M1-лимита;
- unit tests на реализованные инварианты LearningEngine проходят;
- debug build успешен;
- владелец проверил базовый сценарий на реальном телефоне.

### Owner-approved deferral из M1 в M2

Согласно D019 следующие требования ранней редакции M1 DoD были перенесены в M2 и закрыты там:

- разбивка результата по отдельным буквам;
- возврат ошибочной target-буквы позднее через retry queue;
- декларация `<queries>` для `android.intent.action.TTS_SERVICE`, если TTS используется как fallback.

## 3. M2 — LearningEngine + audio/curriculum foundation

Статус: **DONE (100%) / OWNER ACCEPTED**.

M2 DONE, если:

- учебный алгоритм отделён от UI;
- есть версия LearningPolicy;
- есть централизованные пороги и веса;
- реализованы mastery states;
- реализован weighted selection;
- реализован retry queue;
- ошибочная target-буква возвращается позднее без немедленного зацикливания;
- старая буква не исчезает после открытия новой;
- есть критерий открытия уровня;
- реализованы delayed checks;
- есть unit tests на основные инварианты;
- поведение алгоритма соответствует `LEARNING_ENGINE.md` и `SUCCESS_METRICS.md`;
- алгоритм можно проверить на детерминированных тестовых данных;
- результат сессии умеет показывать статистику по каждой букве;
- введён единый интерфейс `AudioPlayer`;
- основной источник озвучки — локальные pre-recorded audio assets, TTS работает как fallback согласно D020;
- Android Manifest содержит требуемую TTS service visibility declaration;
- модель Curriculum/уровней версионируется;
- перенесённые по D019 требования закрыты и имеют evidence.

## 4. M3 — Parent mode / Curriculum / retention

Статус: **DONE (100%) / OWNER ACCEPTED**.

Owner-approved gate M3 нормализован решениями D023–D025 и закрыт M3.4 Closure Evidence Audit. Для v1.0.0 обязательны:

- Parental Gate и Parent Dashboard;
- статистика по всем 33 буквам и persistence прошлых сессий;
- Curriculum v3: 8 уровней / 33 буквы;
- 33 letter OGG + 3 UI sounds, local-audio-first/TTS-fallback;
- LearningPolicy v4;
- 7-day retention decay с deterministic time-based tests;
- Room schema 2 без новой migration;
- CI/build evidence;
- physical-device smoke на Pixel 7a;
- owner acceptance.

Отдельная тренировка слабых букв, ручной выбор набора и дополнительные специализированные distractors относятся к Post-MVP Roadmap и не являются незакрытыми критериями App_ABC MVP v1.0.0 согласно финальному owner decision D026.

## 5. M4 — игровая оболочка / settings / release hardening

Статус: **DONE (100%) / OWNER ACCEPTED**.

M4 DONE, если:

- уровни визуально понятны;
- есть простая система звёзд/аналогичной позитивной награды;
- повторное прохождение разрешено;
- награды не меняют учебные правила;
- нет жизней/штрафов как основной механики;
- рекомендуемая сессия завершается позитивным экраном;
- доступны завершение/продолжение и повторная тренировка;
- после повторного входа приложение не заблокировано;
- persistent-настройки звука и безопасный reset доступны в Parent Dashboard;
- release build проходит R8/minification и resource shrinking;
- audio/TTS resources освобождаются по lifecycle;
- CI triple gate `test` / `assembleDebug` / `assembleRelease` проходит;
- финальный physical-device smoke проходит.

### M4.4 Closure Evidence Audit

Статус: **PASS — 7/7 / OWNER ACCEPTED**.

1. Уровни и игровой/позитивный UX — PASS — CODE/TEST/RUNTIME evidence.
2. Повторная тренировка и отсутствие блокирующей механики — PASS — CODE/RUNTIME evidence.
3. Награды не меняют LearningPolicy/Curriculum — PASS — STATIC/CODE evidence.
4. Persistent sound settings — PASS — CODE/TEST/RUNTIME evidence.
5. Safe progress reset — PASS — CODE/TEST/RUNTIME evidence.
6. Release hardening / R8 / resource shrinking / CI Triple Gate — PASS — CI evidence.
7. Финальный Pixel 7a smoke и owner acceptance — PASS — RUNTIME/OWNER evidence.

`FAIL` / `UNKNOWN` отсутствуют.

## 6. Post-MVP — Curriculum / UX validation

Прежний M5 больше не является gate MVP v1.0.0. Базовый полный алфавит уже реализован в Curriculum v3. Дополнительная проверка distractors, визуально похожих групп, расширенные UX-исследования и новые режимы относятся к Post-MVP Roadmap.

## 7. Post-MVP — эксперимент распознавания речи

Speech recognition не является обязательным условием MVP. При отдельном решении владельца эксперимент должен сохранять требования `ru-RU`, `CORRECT/WRONG/NOT_RECOGNIZED`, отсутствие штрафа за `NOT_RECOGNIZED` и отсутствие сохранения аудио без отдельного решения.

## 8. Post-MVP — публикация / distribution

Кодовая база v1.0.0 может считаться `PRODUCTION READY` до store publication. Фактическая публикация требует отдельного deployment gate:

- production signing key хранится вне Git;
- privacy/store checklist актуализирован;
- требования Google Play / RuStore проверены на дату публикации;
- при необходимости проведено closed/internal testing;
- принято отдельное решение владельца о публикации.

Debug signing, используемый для CI release validation, не является production signing strategy.

## 9. MVP v1.0.0 DONE

Согласно финальному owner decision D026, **App_ABC MVP v1.0.0 считается завершённым после M1–M4**, если одновременно выполнены:

- M1: DONE (100%) / OWNER ACCEPTED;
- M2: DONE (100%) / OWNER ACCEPTED;
- M3: DONE (100%) / OWNER ACCEPTED;
- M4: DONE (100%) / OWNER ACCEPTED;
- M4.4 Closure Evidence Audit: PASS — 7/7;
- `STATIC_REVIEW_STATUS: PASS`;
- CI Triple Gate `test` / `assembleDebug` / `assembleRelease`: PASS;
- финальный physical-device smoke: PASS — Pixel 7a;
- LearningPolicy v4 и Curriculum v3 версионированы;
- Room schema 2 сохраняет историю;
- критических release blockers, `FAIL` или `UNKNOWN` в closure audit нет.

Финальный статус: **App_ABC MVP v1.0.0 — COMPLETE / PRODUCTION READY (100%)**.

Post-MVP Curriculum/UX validation, speech recognition, слоги/мини-игры и store publication не блокируют закрытие MVP v1.0.0.
