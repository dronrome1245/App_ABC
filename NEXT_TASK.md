# NEXT_TASK.md — M4.4 Closure Evidence Audit

## Единственная следующая задача

**Этап M4.4: Построчный Closure Evidence Audit Milestone 4, подготовка чеклиста финального смоук-теста релиза на Pixel 7a и официальное закрытие M4.**

## Статус входа

- Milestone 3: DONE (100%) / OWNER ACCEPTED.
- M4.1 UI Delight: IMPLEMENTATION_COMPLETE / MERGED.
- M4.2 Parent settings + safe progress reset: IMPLEMENTATION_COMPLETE / MERGED.
- M4.3 Release hardening: IMPLEMENTATION_COMPLETE / ожидается финальное CI evidence до merge.
- Release minification: enabled.
- Resource shrinking: enabled.
- CI release assemble: configured.
- LearningPolicy: v4.
- Curriculum: v3 / 8 уровней / 33 буквы.
- Room schema: 2.

## Scope M4.4

1. Построчно сопоставить критерии Milestone 4 из `docs/DEFINITION_OF_DONE.md` с CODE / TEST / CI / RUNTIME / OWNER evidence.
2. Проверить финальный release build после merge M4.3 и зафиксировать SHA/CI run.
3. Подготовить чеклист физического smoke-теста release APK на Pixel 7a, включая уровни, звук, Parent Dashboard, reset и повторный запуск.
4. Зафиксировать отсутствие критических release blockers либо статус `FAIL/UNKNOWN` по каждому неподтверждённому пункту.
5. Закрывать Milestone 4 только после owner/runtime evidence без `FAIL` или `UNKNOWN`.

## Guardrails

- не менять LearningPolicy v4;
- не менять Curriculum v3;
- не менять Room schema 2;
- не считать CI заменой физическому smoke-тесту;
- не считать наличие кода доказательством runtime-проверки;
- debug signing в release-конфигурации M4.3 используется только для CI/локальной валидации и не является production signing strategy.

## Android Studio Agent

Не нужен по умолчанию. Для финального smoke используется обычный release APK на Pixel 7a; Agent подключать только при конкретной runtime/Gradle проблеме.
