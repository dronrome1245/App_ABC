# NEXT_TASK.md — Milestone 4 Kickoff

## Единственная следующая задача

**Milestone 4 Kickoff: архитектурное планирование UI/UX полировки (анимации успеха, салют/конфетти Compose), настроек звука в Parent Dashboard и подготовки ProGuard/R8 релизной сборки. Код M4 не начинать до утверждения скоупа.**

## Статус входа в M4

- Milestone 3: DONE (100%).
- M3.4 Closure Evidence Audit: PASS — 7/7.
- Owner Acceptance: ACCEPTED.
- Physical device smoke: PASS — Pixel 7a.
- LearningPolicy: v4.
- Curriculum: v3 / 8 уровней / 33 буквы.
- Room schema: 2.

## Цель Kickoff

Подготовить owner-reviewable scope Milestone 4 без реализации production-кода. Зафиксировать границы, UX-поведение, технические варианты, риски и критерии приёмки для следующих направлений:

1. UI/UX-полировка детского тренировочного потока.
2. Анимация успешного ответа и завершения уровня, включая вариант салюта/конфетти на Jetpack Compose.
3. Настройки звука в Parent Dashboard без изменения существующей local-OGG-first / TTS-fallback архитектуры до отдельного решения.
4. Подготовка release-конфигурации ProGuard/R8 и перечня обязательных release checks.

## Обязательный результат Kickoff

- предложенный scope M4 с разделением MUST / SHOULD / OUT OF SCOPE;
- архитектурные варианты без внесения кода;
- список затрагиваемых модулей и файлов;
- test plan / owner smoke plan для M4;
- риски и rollback-критерии;
- отдельный owner decision перед стартом реализации.

## Не делать до owner approval

- не начинать код M4;
- не менять LearningPolicy v4 и Curriculum v3;
- не менять Room schema 2;
- не менять Parental Gate;
- не менять и не заменять существующие аудио-ассеты;
- не включать release minification/shrinking в production-конфигурацию без утверждённого плана проверки.

## Android Studio Agent

Не нужен для Kickoff. Подключать только после утверждения scope и только при конкретной Android/runtime/Gradle проблеме.
