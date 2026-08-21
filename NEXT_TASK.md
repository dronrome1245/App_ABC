# NEXT_TASK.md — M4.3 Release build hardening

## Единственная следующая задача

**Этап M4.3: Релизная конфигурация сборки (ProGuard/R8 rules, shrinkResources, проверка отсутствия утечек и Release APK/AAB build).**

## Статус входа

- Milestone 3: DONE (100%) / OWNER ACCEPTED.
- Home level selector hotfix: MERGED.
- M4.1 UI Delight: IMPLEMENTATION_COMPLETE / MERGED.
- M4.2 Parent settings + safe progress reset: IMPLEMENTATION_COMPLETE.
- Settings DataStore: COMPLETE.
- Progress reset: COMPLETE.
- LearningPolicy: v4.
- Curriculum: v3 / 8 уровней / 33 буквы.
- Room schema: 2.

## Scope M4.3

1. Проверить и настроить release `ProGuard/R8` rules без поломки Room, Compose, DataStore и навигации.
2. Рассмотреть и включить `shrinkResources` только после подтверждённого release build/smoke.
3. Проверить lifecycle/release-path на отсутствие очевидных утечек ресурсов, прежде всего audio/TTS и долгоживущих coroutine scopes.
4. Собрать и проверить Release APK/AAB.
5. Зафиксировать release test/smoke checklist и rollback-критерии.

## Guardrails

- не менять LearningPolicy v4;
- не менять Curriculum v3;
- не повышать Room schema 2 без отдельной необходимости;
- не менять существующие 33 letter OGGs + 3 UI sounds;
- не вводить backend/analytics/ads;
- не ослаблять подтверждение сброса прогресса;
- не менять семантику persisted sound settings без отдельного owner decision.

## Android Studio Agent

Не нужен по умолчанию. Подключать только при конкретной Android/runtime/Gradle проблеме.
