# NEXT_TASK.md — M2.2 Per-letter statistics

## Единственная следующая задача

**Реализовать статистику по каждой букве на основе Room Attempt history и показать разбивку по буквам на экране результата.**

## Scope M2.2

1. Рассчитывать статистику отдельно по каждой букве:
   - attempts;
   - correct;
   - accuracy;
   - recent errors;
   - response time summary, если это можно сделать без преждевременной сложной аналитики.
2. На экране результата показывать разбивку по буквам текущей сессии, закрывая перенос D019.
3. Использовать Room Attempt как источник подробной истории; DataStore не использовать для истории попыток.
4. Использовать уже реализованный в M2.1 `LevelProgressionStore` для selected/unlocked level; не создавать второй источник progression state.
5. Не терять прошлые Attempt и их `learningPolicyVersion` / `curriculumVersion`.
6. Добавить JVM tests на per-letter aggregation.

## Уже реализовано в M2.1

- Curriculum Levels 1–3;
- 10 вопросов;
- unlock >=80% (8/10);
- Preferences DataStore для highest unlocked / selected level;
- UI выбора разблокированного уровня;
- runtime передача выбранного `levelId` в Exercise;
- автоматическое открытие следующего уровня после успешной сессии.

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

По умолчанию не нужен. Подключать только при конкретной Room/DataStore/runtime проблеме, которую невозможно подтвердить кодом, JVM tests или CI.
