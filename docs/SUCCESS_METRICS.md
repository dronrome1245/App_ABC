# Success Metrics — как измеряем полезность App_ABC

Дата: 2026-08-20

## 1. Принцип

Успех оценивается по качеству знания буквы, а не по активности в приложении. Основные сигналы: accuracy по букве, response time, delayed success/retention, confusion rate и mastery state.

## 2. Данные Attempt

Каждая попытка хранит минимум:

- targetLetter;
- selectedLetter;
- isCorrect;
- responseTimeMs;
- timestamp;
- levelId;
- sessionId;
- learningPolicyVersion;
- curriculumVersion.

## 3. Recent accuracy

Recent accuracy по букве — доля правильных ответов в окне до последних 10 валидных попыток. Если попыток меньше 10, используется фактически доступное число.

## 4. Response time

Для пользовательской статистики предпочтительна медиана. Persistent `LetterProgressEntity` M2.2 хранит среднее время как быстрый агрегат; сырые Attempt сохраняются, поэтому медиана может вычисляться без потери данных.

## 5. Delayed success

Внутрисессионный delayed success фиксируется, если повторный ответ правильный и между двумя предъявлениями target было минимум 2 других вопроса.

Межсессионные окна `6h+`, `24h+`, `7d+` используются только при наличии реальных данных; отсутствие такого интервала означает `insufficient data`, а не неуспех.

## 6. Confusion rate

Для каждой ошибки сохраняется и может агрегироваться пара:

`targetLetter -> selectedWrongLetter`.

## 7. Mastery — LearningPolicy v3

После D022 используются состояния:

- `INTRODUCED` — менее 3 попыток;
- `PRACTICING` — от 3 попыток до выполнения критерия `MASTERED`;
- `MASTERED` — минимум 5 попыток и recent accuracy >=85%.

Количество `MASTERED` букв — одна из продуктовых метрик знания. Открытый игровой уровень сам по себе не означает mastery.

## 8. Проблемная/слабая буква

Для adaptive selection в LearningPolicy v3 основной оперативный сигнал — состояние `PRACTICING` и доля недавних ошибок. Чем больше recent errors, тем выше selection weight в диапазоне 2.0–3.0 относительно `MASTERED = 1.0`.

Confusion history и response time остаются дополнительными метриками для M3/Parent mode.

## 9. Метрики сессии

Можно показывать:

- общий X/10;
- accuracy;
- breakdown correct/attempts по каждой target-букве;
- ошибки/confusions;
- response time;
- изменения относительно прошлых сессий.

## 10. Не считать доказательством обучения сами по себе

- число открытых уровней;
- количество кликов;
- число минут;
- streak;
- звёзды/награды.

## 11. Version discipline

Mastery thresholds, weights, retry spacing и level unlock являются частью LearningPolicy и не меняются без owner decision, обновления `learningPolicyVersion`, документации и тестов.
