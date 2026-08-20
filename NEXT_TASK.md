# NEXT_TASK.md — M2.5 Closure Audit Milestone 2

## Единственная следующая задача

**Провести обязательный Milestone 2 Closure Evidence Audit по каждой строке `docs/DEFINITION_OF_DONE.md`, подтвердить все инварианты LearningEngine и подготовить M2 к owner acceptance.**

## Перед стартом

M2.5 начинается только после зелёного CI M2.4:

- `./gradlew test` PASS;
- `./gradlew assembleDebug` PASS;
- automated migration 1→2 test PASS.

## Scope M2.5

1. Построчно сопоставить каждый критерий раздела M2 в `docs/DEFINITION_OF_DONE.md` с evidence.
2. Для каждой строки использовать только статусы:
   - `PASS — CODE/TEST/CI/RUNTIME/OWNER_EVIDENCE`;
   - `DEFERRED_BY_OWNER — <DECISION_ID>`;
   - `FAIL`;
   - `UNKNOWN`.
3. Отдельно проверить обязательные инварианты:
   - учебный алгоритм отделён от UI;
   - LearningPolicy версионируется;
   - пороги/веса централизованы;
   - mastery states реализованы;
   - weighted selection реализован;
   - retry queue возвращает ошибочную target позднее и не зацикливается;
   - старая буква не исчезает после открытия новой;
   - delayed checks реализованы;
   - per-letter Session Summary D019 работает;
   - AudioPlayer local-first + TTS fallback работает;
   - TTS service visibility присутствует;
   - Curriculum v2 версионируется;
   - переносы D019 закрыты.
4. Зафиксировать migration evidence:
   - automated migration 1→2 test должен быть PASS;
   - реальный device migration может остаться `NOT_TESTED`, если automated evidence исключает UNKNOWN для migration-кода; явно не выдавать это за device PASS.
5. Проверить `PROJECT_STATUS.md`, `BACKLOG.md`, `DECISIONS.md`, `LEARNING_ENGINE.md`, `SUCCESS_METRICS.md` на отсутствие drift.
6. Если аудит не содержит `FAIL`/`UNKNOWN`, запросить owner acceptance Milestone 2.
7. Только после owner acceptance перевести M2 в DONE и активировать M3.

## Не менять

- Curriculum v2;
- 10 вопросов;
- unlock >=80% / 8 из 10;
- LearningPolicy v3 без нового owner decision;
- Room schema 2;
- audio assets;
- не начинать M3 в рамках M2.5 до закрытия gate.

## Android Studio Agent

Не нужен по умолчанию. Подключать только при конкретной runtime/Room instrumentation проблеме, которую нельзя закрыть repository/CI evidence.
