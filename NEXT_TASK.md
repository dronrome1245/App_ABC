# NEXT_TASK.md — M2.4 LearningEngine adaptive policy

## Единственная следующая задача

**Закрыть оставшиеся обязательные критерии Milestone 2 по LearningEngine: retry queue, mastery states, weighted selection, delayed checks/weak-letter weighting и deterministic invariant tests.**

## Основание

M2.3 owner smoke от 2026-08-20:

- `AUDIO: PASS`;
- `LEVELS: PASS`;
- `SUMMARY: PASS`;
- `PERSISTENCE: PASS`;
- `MIGRATION_1_2: NOT_TESTED`.

M2.3 runtime-гейт пройден. Переход к M3 пока запрещён, потому что `docs/DEFINITION_OF_DONE.md` содержит незакрытые обязательные критерии LearningEngine.

## Scope M2.4

1. Реализовать retry queue по D019 / `LEARNING_ENGINE.md`:
   - после ошибки target помещается в retry queue;
   - при достаточном пуле возвращается примерно через 2–4 других задания;
   - не создаёт немедленного или бесконечного цикла;
   - сохраняет ограничение на длинную серию одной target-буквы.
2. Реализовать mastery states:
   - `NEW`;
   - `LEARNING`;
   - `FAMILIAR`;
   - `STABLE`;
   - переходы и критерии должны соответствовать действующей LearningPolicy/Success Metrics, без новых неутверждённых magic numbers.
3. Реализовать weighted selection:
   - новые/слабые буквы получают повышенный вес;
   - недавняя ошибка повышает вес;
   - давно не показанная буква получает приоритет;
   - сильная старая буква сохраняет ненулевую вероятность появления.
4. Реализовать delayed checks / delayed success:
   - внутрисессионная delayed-проверка валидна при минимум 2 других заданиях между предъявлениями target;
   - межсессионные интервалы учитывать только когда данные реально существуют;
   - delayed success не меняет D021 level unlock 8/10.
5. Централизовать LearningPolicy config:
   - никаких новых порогов/весов в UI;
   - изменения нормативных значений только через действующее решение и version discipline.
6. Добавить deterministic JVM tests:
   - ошибка возвращает target позднее;
   - retry не бесконечен;
   - нет запрещённой длинной серии одной цели;
   - слабая буква получает повышенный вес;
   - сильная буква имеет ненулевой шанс;
   - старая буква не исчезает после открытия новой;
   - delayed success считается только при допустимом интервале;
   - изменение версии политики не происходит неявно.
7. Закрыть migration evidence:
   - добавить automated migration test 1→2 с сохранением/backfill исторических `Attempt`, если это возможно в текущем test stack;
   - реальный device migration остаётся `NOT_TESTED`, пока нет schema-v1 установки, но automated migration evidence должен устранить `UNKNOWN` перед closure audit.

## Не менять

- Curriculum v2: Levels 1–3 `А/М`, `О/У`, `С/Н`;
- 10 вопросов на сессию;
- level unlock `>=80%` / `8 из 10`;
- Room schema 2 без отдельной необходимости;
- local-audio-first + TTS fallback;
- M3 не начинать.

## Decision / normative sources

- D019 — retry queue обязателен в M2;
- D021 — 10 вопросов и 80% unlock;
- `docs/LEARNING_ENGINE.md` — алгоритм, states, retry/delayed rules, invariants;
- `docs/SUCCESS_METRICS.md` — mastery/knowledge metrics;
- `docs/DEFINITION_OF_DONE.md` — M2 closure gate.

## После M2.4

Провести обязательный Milestone Closure Evidence Audit по каждой строке M2 DoD. Только при отсутствии `FAIL`/`UNKNOWN` запросить owner acceptance M2 и переходить к M3.

## Android Studio Agent

По умолчанию не нужен. Pure Kotlin LearningEngine, JVM tests и Room migration tests выполнять обычным repository/CI workflow. Подключать Android Studio Agent только при конкретной Android runtime/Room instrumentation проблеме.
