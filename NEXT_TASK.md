# NEXT_TASK.md — M2.2 Per-letter statistics and progression persistence

## Единственная следующая задача

**Реализовать статистику по каждой букве на основе Room Attempt history и хранение простого состояния progression/unlocked level в DataStore.**

## Scope M2.2

1. Рассчитывать статистику отдельно по каждой букве:
   - attempts;
   - correct;
   - accuracy;
   - recent errors;
   - response time summary, если это можно сделать без преждевременной сложной аналитики.
2. На экране результата показывать разбивку по буквам текущей сессии, закрывая перенос D019.
3. Использовать Room Attempt как источник подробной истории; DataStore не использовать для истории попыток.
4. В DataStore хранить только простое progression state, например текущий максимальный разблокированный уровень.
5. Применять `LevelUnlockPolicy` LearningPolicy v2: полная сессия 10 вопросов, >=80% (8/10) открывает следующий утверждённый уровень.
6. Не терять прошлые Attempt и их `learningPolicyVersion` / `curriculumVersion`.
7. Добавить JVM tests на per-letter aggregation и progression transitions.

## Отдельно остаётся обязательным в M2

- retry queue после ошибки (D019 / LearningEngine);
- mastery states;
- weighted selection;
- delayed checks;
- реальные WAV/OGG assets и runtime-проверка local-audio-first после их появления.

## Decision sources

- D019 — per-letter result и retry перенесены из M1 в M2;
- D020 — local audio first + TTS fallback;
- D021 — Curriculum Levels 1–3, 10 вопросов, unlock >=80% (8/10), curriculumVersion 2, learningPolicyVersion 2.

## Android Studio Agent

По умолчанию не нужен. Подключать только если возникает конкретная проблема DataStore/Room migration/runtime, которую невозможно подтвердить кодом, JVM tests или CI.
