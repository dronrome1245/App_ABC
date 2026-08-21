# NEXT_TASK.md — M3.4 Closure Evidence Audit

## Единственная следующая задача

**M3.4: Построчный Closure Evidence Audit Milestone 3, запуск PR #9 и подготовка чеклиста Owner Acceptance Smoke Test.**

## Цель

Проверить Milestone 3 по нормативным решениям, фактическому коду и подтверждаемым evidence без автоматического закрытия требований, для которых отсутствует runtime/owner evidence.

## Проверить построчно

1. D023 — Parental Gate и Parent Dashboard.
2. D024 — Curriculum v3: 8 уровней, 33 буквы, неизменный 10-question / 80% unlock.
3. D025 — LearningPolicy v4: 7-day Retention Decay, deterministic time-based JVM tests, decay weight 2.0, восстановление после успешного re-check.
4. Room schema остаётся 2; миграция для M3.3 не создавалась.
5. Исторические `Attempt` не переписываются time-based evaluation.
6. JVM test suite и `assembleDebug` имеют подтверждаемое зелёное CI evidence на финальном PR head.
7. Отдельно отметить всё, что требует Owner Acceptance / runtime smoke и не доказывается JVM/CI.

## Owner Acceptance Smoke Test — подготовить чеклист

Чеклист должен как минимум покрыть:

- вход в Parent mode через существующий Parental Gate;
- отображение всех 33 букв;
- отображение «Требует повторения» и даты последней тренировки для decay-состояния;
- прохождение повторной успешной тренировки и возврат буквы в `MASTERED`;
- отсутствие регрессии Curriculum Levels 1–8;
- воспроизведение локального аудио / TTS fallback без изменения M3.2 audio strategy.

## Не менять в M3.4 без нового owner decision

- Curriculum v3;
- LearningPolicy v4 thresholds/weights/retention horizon;
- Room schema 2;
- Parental Gate;
- 10 вопросов и unlock `>=80%` / `8 из 10`.

## Android Studio Agent

Не нужен по умолчанию. Подключать только при конкретной Android/runtime проблеме.
