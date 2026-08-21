# Learning Engine — алгоритм обучения

Дата: 2026-08-21

## 1. Главный принцип

Приложение тренирует извлечение из памяти. После ошибки буква возвращается через несколько других вопросов; старые буквы продолжают появляться после открытия новых уровней. Игровой уровень и учебное состояние буквы — разные сущности.

## 2. Версия политики

Текущая runtime-версия после реализации D025:

`learningPolicyVersion = 4`

LearningPolicy v4 сохраняет правила D021/D022 без изменений: сессия содержит 10 вопросов, следующий игровой уровень открывается при результате >=80% (минимум 8/10), mastery thresholds и retry spacing остаются прежними. Исторические `Attempt` старых версий не переписываются.

Изменение v4 — long-term Retention Decay по D025: `MASTERED` требует повторной проверки, если после последнего успешного подтверждения знания прошло более 7 суток.

## 3. Mastery states — LearningPolicy v4

Базовая квалификация остаётся прежней:

- `INTRODUCED` — менее 3 валидных попыток;
- `PRACTICING` — от 3 попыток до выполнения критерия `MASTERED`;
- `MASTERED` — минимум 5 попыток и recent accuracy >=85%.

Recent accuracy считается по окну до последних 10 попыток по букве.

После базовой квалификации применяется Retention Decay. Для буквы, которая базово квалифицируется как `MASTERED`, берётся timestamp последней успешной попытки. Если с этого момента прошло **более** 7 суток, эффективный статус становится `PRACTICING`, а вычисляемый флаг `isDecayed = true`. Ровно на границе 7 суток decay ещё не применяется.

## 4. Данные

### Attempt

Минимально:

- `id`;
- `timestamp`;
- `sessionId`;
- `levelId`;
- `targetLetter`;
- `selectedLetter`;
- `isCorrect`;
- `responseTimeMs`;
- `learningPolicyVersion`;
- `curriculumVersion`.

### Производные данные

Для адаптивного выбора и Parent Dashboard вычисляются/кэшируются:

- total attempts;
- correct attempts;
- recent accuracy;
- recent errors;
- delayed successes;
- last seen;
- last successful confirmation;
- mastery state;
- retention-decay flag;
- confusion `target -> selectedWrong`.

Сырые `Attempt` остаются источником подробной истории и не изменяются при time-based evaluation.

## 5. Взвешенный выбор target

Пул = буквы текущего уровня + ранее изученные.

Централизованные веса v4:

- актуальный `MASTERED = 1.0`;
- затухший `MASTERED` (`PRACTICING`, `isDecayed = true`) = `2.0`;
- `INTRODUCED = 2.0`;
- обычный `PRACTICING = 2.0...3.0` в зависимости от доли ошибок в recent window.

Одна target-буква не должна появляться более двух раз подряд.

## 6. Retry queue после ошибки

После неправильного ответа:

1. target ставится в `InSessionRetryQueue`;
2. при достаточном остатке 10-вопросной сессии target не выбирается обычным weighted flow до момента retry;
3. retry возвращается после 2–4 других вопросов;
4. немедленного повтора быть не должно;
5. повторная ошибка может породить новый retry только если остаётся место;
6. длина сессии 10 вопросов не увеличивается ради retry.

## 7. Delayed success

Внутрисессионный delayed success валиден, если между двумя предъявлениями target было минимум 2 других вопроса и повторный ответ правильный.

Межсессионные интервалы анализируются по сохранённым timestamps `Attempt`. Delayed success не является дополнительным gate для открытия уровня.

## 8. D025 — long-term retention decay

Константа политики:

`RETENTION_DECAY_MILLIS = 7L * 24 * 60 * 60 * 1000 = 604_800_000 ms`

Формула оценки для буквы, базово квалифицирующейся как `MASTERED`:

`isDecayed = currentTimeMillis - lastSuccessfulTimestamp > RETENTION_DECAY_MILLIS`

При `isDecayed = true` эффективный `MasteryState` возвращается как `PRACTICING`, а `AdaptiveSessionGenerator` использует вес `2.0`, чтобы дать букве приоритет для повторной проверки.

После нового успешного ответа текущий timestamp становится новым `lastSuccessfulTimestamp`/`lastSeenTimestamp`. Если базовые mastery-критерии по истории сохраняются, буква снова получает эффективный статус `MASTERED`.

Оценка времени read-only: существующие строки `Attempt` и их `learningPolicyVersion` не переписываются. Room schema остаётся 2; новое состояние вычисляется из уже существующих timestamps.

D025 не вводит суточную блокировку и не запрещает тренироваться чаще.

## 9. Confusion

При каждой ошибке увеличивается счётчик пары `targetLetter -> selectedWrongLetter`. Фактическая confusion history остаётся доступна из `Attempt` и используется Parent mode / weak-letter режимами.

## 10. Открытие уровня — без изменений

Следующий уровень разблокируется, если:

- завершена полная сессия;
- ровно 10 вопросов;
- accuracy >=80%;
- минимум 8 правильных ответов.

Mastery state и D025 не заменяют этот gate.

## 11. Случайность, время и тестируемость

`AdaptiveSessionGenerator` — pure Kotlin и принимает фиксируемый `Random/seed`, а LearningPolicy v4 допускает управляемый `currentTimeMillis`. Детерминированные JVM-тесты проверяют:

- сохранение `MASTERED` в пределах 7 суток (`T0 + 6d`);
- отсутствие decay ровно на границе `T0 + 7d`;
- `MASTERED -> PRACTICING` при `T0 + 7d + 1s`;
- вес `2.0` для затухшей буквы;
- восстановление `MASTERED` после нового успешного подтверждения;
- неизменность исторических `Attempt` при time-based evaluation;
- distractor invariant, retry spacing, bounded retry, target streak, weighted selection, delayed success, confusion и version discipline.

## 12. Что LearningEngine не решает

- визуальный дизайн;
- игровые награды;
- состав Curriculum Levels 1–8 вне утверждённого D024;
- медицинские выводы;
- распознавание речи;
- UX Parental Gate.
