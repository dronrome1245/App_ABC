# Learning Engine — алгоритм обучения

Дата: 2026-08-21

## 1. Главный принцип

Приложение тренирует извлечение из памяти. После ошибки буква возвращается через несколько других вопросов; старые буквы продолжают появляться после открытия новых уровней. Игровой уровень и учебное состояние буквы — разные сущности.

## 2. Версия политики

Текущая runtime-версия после D022:

`learningPolicyVersion = 3`

LearningPolicy v3 сохраняет D021 без изменений: сессия содержит 10 вопросов, следующий игровой уровень открывается при результате >=80% (минимум 8/10). Исторические Attempt старых версий не переписываются.

D025 утверждает направление следующей long-term policy: 7-day retention decay для `MASTERED`. M3.1 это правило **не включает в runtime**, поэтому `learningPolicyVersion` остаётся 3 до отдельной реализации и version bump.

## 3. Mastery states — LearningPolicy v3

- `INTRODUCED` — менее 3 валидных попыток;
- `PRACTICING` — от 3 попыток до выполнения критерия `MASTERED`;
- `MASTERED` — минимум 5 попыток и recent accuracy >=85%.

Recent accuracy считается по окну до последних 10 попыток по букве.

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
- mastery state;
- confusion `target -> selectedWrong`.

Сырые Attempt остаются источником подробной истории.

## 5. Взвешенный выбор target

Пул = буквы текущего уровня + ранее изученные.

Централизованные веса v3:

- `MASTERED = 1.0`;
- `INTRODUCED = 2.0`;
- `PRACTICING = 2.0...3.0` в зависимости от доли ошибок в recent window.

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

Межсессионные интервалы анализируются по сохранённым timestamps Attempt. Delayed success не является дополнительным gate для открытия уровня.

## 8. D025 — long-term retention decay

Owner decision D025 вводит 7-дневный горизонт для `MASTERED`: если после последнего успешного подтверждения прошло 7 суток без нового успешного подтверждения, буква считается требующей long-term re-check.

Для M3.1 это только нормативное архитектурное решение. Оно не меняет текущий `MasteryState`, веса и runtime LearningPolicy v3. Перед реализацией необходимо определить точный эффект decay на selection weight/mastery transition, повысить `learningPolicyVersion` и добавить детерминированные тесты с управляемым clock/time source.

D025 не вводит суточную блокировку и не запрещает тренироваться чаще.

## 9. Confusion

При каждой ошибке увеличивается счётчик пары `targetLetter -> selectedWrongLetter`. Фактическая confusion history остаётся доступна из Attempt и используется будущими Parent mode / weak-letter режимами.

## 10. Открытие уровня — без изменений

Следующий уровень разблокируется, если:

- завершена полная сессия;
- ровно 10 вопросов;
- accuracy >=80%;
- минимум 8 правильных ответов.

Mastery state и D025 не заменяют этот gate.

## 11. Случайность и тестируемость

AdaptiveSessionGenerator — pure Kotlin и принимает фиксируемый Random/seed. Детерминированные тесты подтверждают distractor invariant, retry spacing, bounded retry, target streak, weighted selection, ненулевой шанс MASTERED, delayed success, confusion и version discipline.

Будущая реализация D025 должна дополнительно тестироваться с детерминированным временем для границ до 7 суток / ровно 7 суток / после 7 суток.

## 12. Что LearningEngine не решает

- визуальный дизайн;
- игровые награды;
- конкретную матрицу Curriculum Levels 4–8;
- медицинские выводы;
- распознавание речи;
- UX Parental Gate.
