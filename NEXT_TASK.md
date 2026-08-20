# NEXT_TASK.md — M2.3 Local audio assets + M2 smoke test

## Единственная следующая задача

**Добавить утверждённые локальные аудиофайлы букв в `res/raw`, проверить local-audio-first/TTS fallback и провести объединённый ручной smoke test реализованных M2.1–M2.2 функций.**

## Scope M2.3

1. Добавить утверждённые WAV/OGG assets для букв текущего Curriculum v2:
   - `А`, `М`, `О`, `У`, `С`, `Н`;
   - имена ресурсов должны соответствовать `AudioAssetCatalog` либо mapping должен быть обновлён явно.
2. Не удалять системный TTS fallback.
3. Проверить на устройстве:
   - локальный asset воспроизводится при наличии;
   - при отсутствии/ошибке asset используется TTS fallback;
   - звук не блокирует переход между вопросами.
4. Проверить уже реализованный экран выбора уровней:
   - Level 1 доступен по умолчанию;
   - Level 2/3 появляются после соответствующего unlock;
   - не создавать второй экран/источник progression state без необходимости.
5. Выполнить M2 smoke test:
   - пройти 10 вопросов;
   - проверить unlock >=8/10;
   - проверить per-letter Session Summary из M2.2;
   - повторить уровень;
   - закрыть и повторно открыть приложение;
   - убедиться, что Room/DataStore прогресс сохранился;
   - отдельно проверить обновление существующей установки через migration 1->2, если на устройстве есть база schema v1.

## Уже реализовано в M2.2

- Room `databaseSchemaVersion = 2`;
- `LetterProgressEntity` и `SessionResultEntity`;
- migration 1->2 с backfill старых Attempt;
- `ProgressRepository`;
- per-letter breakdown результата D019;
- repeat/continue actions на Session Summary.

## После M2.3 остаётся обязательным в M2

- retry queue после ошибки (D019 / LearningEngine);
- mastery states;
- weighted selection;
- delayed checks;
- weak-letter weighting;
- итоговый M2 closure audit.

## Decision sources

- D019 — per-letter result и retry перенесены в M2;
- D020 — local pre-recorded audio first + TTS fallback;
- D021 — Curriculum Levels 1–3, 10 вопросов, unlock >=80% (8/10).

## Android Studio Agent

По умолчанию не нужен. Обычный Run/установка и smoke test выполняются владельцем. Android Studio Agent подключать только при конкретной IDE/SDK/Logcat/audio/Room migration ошибке.
