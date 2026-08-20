# NEXT_TASK.md — Milestone 3 Kickoff

## Единственная следующая задача

**Milestone 3 Kickoff: спланировать и зафиксировать архитектурный scope Parent Dashboard/родительской статистики, подготовить versioned план расширения Curriculum до полного русского алфавита и спроектировать долгосрочное spaced repetition между днями/сессиями.**

## Статус перед стартом

- Milestone 1: DONE (100%).
- Milestone 2: DONE (100%) / OWNER ACCEPTED.
- M2.5 Closure Evidence Audit: PASS — 7/7.
- LearningPolicy v3: accepted.
- Curriculum v2: accepted.
- Room schema: 2.

## Scope M3 Kickoff — только планирование и архитектура

1. Parent Dashboard / родительский профиль:
   - определить экраны и navigation boundary;
   - определить, какие метрики показываются родителю;
   - recent accuracy, response time, mastery state, confusion matrix, слабые буквы, история сессий;
   - определить ручной выбор букв, настройки и reset flow;
   - не загромождать детский training flow.
2. Data architecture статистики:
   - определить, какие данные читаются из существующих `Attempt`, `LetterProgressEntity`, `SessionResultEntity`;
   - определить, нужны ли новые derived views/queries/entities;
   - не повышать Room schema без отдельной необходимости и migration plan.
3. Curriculum полного алфавита:
   - подготовить proposal для versioned Curriculum, включающего все 33 русские буквы;
   - определить порядок/группы ввода, distractor strategy и визуально похожие пары;
   - учесть фактическую confusion matrix конкретного ребёнка;
   - не менять `curriculumVersion = 2` и текущие Levels 1–3 до отдельного owner decision.
4. Spaced Repetition across days:
   - спроектировать межсессионные интервалы и retention metrics;
   - использовать реальные timestamps Attempt, а не искусственные календарные блокировки;
   - определить правила 6h+/24h+/7d+ и связь с mastery без изменения LearningPolicy v3 до утверждения новой версии;
   - описать deterministic test strategy для времени/интервалов.
5. Подготовить архитектурный пакет M3:
   - proposed scope и out-of-scope;
   - data-flow Parent Dashboard;
   - требуемые Decision Log записи;
   - предполагаемые изменения Room/DataStore/API между слоями;
   - UX wire-level описание без реализации;
   - DoD и test plan для M3.

## Decision Firewall

На этапе Kickoff не кодировать:

- полный алфавит;
- новый LearningPolicy;
- новые mastery thresholds;
- новые Room tables;
- Parent Dashboard UI.

Сначала должны быть утверждены M3 architecture/scope и все новые продуктовые параметры. Полный порядок 33 букв и long-term spacing policy нельзя считать утверждёнными только на основании этого planning task.

## Нельзя менять без нового owner decision

- LearningPolicy v3;
- mastery states `INTRODUCED / PRACTICING / MASTERED`;
- Curriculum v2 Levels 1–3;
- 10 вопросов на сессию;
- unlock `>=80%` / 8 из 10;
- local-audio-first + TTS fallback;
- Room schema 2.

## Результат Kickoff

Следующий implementation task packet должен появиться только после утверждения владельцем архитектуры M3 и новых versioned решений по Curriculum/Spaced Repetition, если они действительно меняются.

## Android Studio Agent

Не нужен для Kickoff. Это repository/product architecture task без Android runtime debugging.
