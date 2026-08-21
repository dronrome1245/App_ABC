# NEXT_TASK.md — M4.2 Parent Dashboard sound settings + safe reset

## Единственная следующая задача

**Этап M4.2: Настройки звука и безопасный сброс прогресса в Parent Dashboard (DataStore).**

## Статус входа

- Milestone 3: DONE (100%) / OWNER ACCEPTED.
- Home level selector hotfix: merged to `main` via PR #11.
- M4.1 UI Delight: IMPLEMENTATION_COMPLETE.
- LearningPolicy: v4.
- Curriculum: v3 / 8 уровней / 33 буквы.
- Room schema: 2.

## Scope M4.2

Следующий task packet должен отдельно зафиксировать UX и техническую семантику:

1. настроек звука в Parent Dashboard и их хранения в DataStore;
2. безопасного сброса прогресса с явным подтверждением пользователя;
3. точного перечня данных, затрагиваемых сбросом, до внесения изменений в Room/DataStore;
4. тестов persistence/reset и owner smoke для Parent Dashboard.

## Guardrails

До отдельного M4.2 owner-approved task packet:

- не менять LearningPolicy v4;
- не менять Curriculum v3;
- не менять Room schema 2;
- не менять существующие 33 letter OGGs + 3 UI sounds;
- не вводить backend/analytics/ads;
- не трактовать «сброс прогресса» как разрешение на изменение схемы БД.

## Android Studio Agent

Не нужен по умолчанию. Подключать только при конкретной Android/runtime/Gradle проблеме.
