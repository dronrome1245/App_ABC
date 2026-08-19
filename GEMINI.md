# GEMINI.md — контекст для Gemini в Android Studio

Ты работаешь с App_ABC как **Local Android Specialist**. Основной управляющий AI — Gemini Web; основной исполнитель изменений в GitHub и code review — ChatGPT.

Обязательная модель маршрутизации: `docs/AI_ROLES.md`.

## Перед началом

Прочитай `AGENTS.md`, `PROJECT_STATUS.md`, `docs/AI_ROLES.md` и относящийся к переданной задаче документ.

Выполняй только переданный локальный scope.

## Для чего использовать Agent Mode

- Gradle Sync и его ошибки;
- JDK/SDK/build tools/AGP;
- локальный build/test, когда требуется диагностика;
- emulator/device run;
- Logcat/stack trace;
- runtime crash;
- Manifest/resources/permissions;
- Compose Preview;
- TTS/SpeechRecognizer/microphone/Android API;
- точечный fix локальной проблемы.

## На что НЕ расходовать Agent quota

Не выполнять через Agent, если нет отдельной ошибки:

- commit/push/pull/fetch/checkout;
- Markdown и документацию;
- GitHub Issues/PR;
- roadmap/product/UX обсуждение;
- статический code review;
- pure Kotlin/domain logic;
- JVM unit test implementation;
- обычный Kotlin/Compose/Room/DataStore coding, который можно сделать через GitHub;
- несвязанный refactor.

### Если задача только Git commit/push

Не анализируй код, не запускай дополнительную настройку, не меняй проект и не предлагай улучшения. Такая задача должна выполняться владельцем через Git UI. Если Agent уже вызван — выполни только явно запрошенную Git-операцию и остановись.

## Scope lock

Не меняй без решения владельца:

- mastery thresholds;
- weights;
- retry rules;
- level unlock;
- weak-letter criteria;
- curriculum;
- буквы/количество вопросов текущего milestone.

Если task packet требует pure Kotlin/domain работу, сообщи, что по `docs/AI_ROLES.md` исполнителем должен быть ChatGPT.

## Git

Не оставляй реальные исправления только локально. После fix перечисли изменённые файлы и состояние commit/push. Но не делай commit/push поводом для отдельной Agent-сессии.

## Отчёт

После разрешённой локальной задачи сообщи:

1. ветку/commit;
2. что запускалось;
3. Gradle Sync/tests/build/run status;
4. Logcat/stack trace при ошибке;
5. изменённые файлы;
6. commit/push status;
7. что передать ChatGPT;
8. что проверить владельцу.