# NEXT_TASK.md — M3.3 Retention Decay

## Единственная следующая задача

**Этап M3.3: реализация алгоритма 7-дневного затухания mastery (Retention Decay по D025) в LearningPolicy v4 и time-based JVM-тесты.**

## Decision source

`OWNER / D025`

Буква в состоянии `MASTERED`, не имеющая успешного подтверждения знания в течение 7 суток, должна снова требовать long-term re-check.

## Перед стартом

1. Завершить M3.2: зелёный JVM test suite и `assembleDebug` на финальном PR head.
2. Не менять D024 Curriculum v3 / 8 уровней / 33 буквы.
3. Зафиксировать точное runtime-поведение decay как LearningPolicy v4 до кодирования: влияет ли истечение 7 суток на mastery state, selection weight или отдельный retention flag.
4. Использовать детерминированный clock/time source; не привязывать pure domain tests к реальному системному времени.

## Scope M3.3

- повысить `learningPolicyVersion` с 3 до 4;
- реализовать 7-day retention boundary по timestamps Attempt;
- сохранить отсутствие суточной блокировки приложения;
- добавить time-based JVM tests для интервалов `<7 дней`, `ровно 7 дней`, `>7 дней`;
- проверить взаимодействие decay с adaptive weighting, retry queue и MASTERED;
- не переписывать исторические Attempt;
- сохранить Room schema 2, если новое состояние может быть вычислено из существующих данных;
- прогнать весь JVM suite, `assembleDebug` и CI.

## Не менять

- Curriculum v3 и состав Levels 1–8;
- 10 вопросов;
- unlock `>=80%` / `8 из 10`;
- 33 local letter assets и TTS fallback;
- Parental Gate;
- внешние analytics/ads/backend.

## Android Studio Agent

Не нужен по умолчанию. Подключать только при конкретной Android/runtime проблеме.
