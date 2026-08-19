# AI Roles — маршрутизация задач и экономия Agent Mode

Дата: 2026-08-19

Этот документ задаёт обязательный порядок выбора исполнителя. Цель — использовать Gemini Android Studio Agent только там, где локальный Android-контекст действительно необходим.

## 1. Роли

### Владелец
Product Owner и финальный принимающий. Выполняет простые действия в интерфейсе Android Studio/Git, запускает приложение и проверяет UX. Не обязан редактировать код.

### Gemini Web
Product Lead / Orchestrator. Обсуждает продукт, UX и LearningPolicy, принимает вход от владельца, декомпозирует работу и выбирает исполнителя.

### ChatGPT
Repository Engineer + Code Reviewer. Основной исполнитель изменений в GitHub: код, документация, Issues, PR, review и актуализация проектных документов.

### Gemini Android Studio Agent
Local Android Specialist. Ограниченный ресурс только для задач, где нужен локальный IDE/runtime контекст.

## 2. Обязательный Routing Gate

Перед выдачей любого задания Gemini Web обязан классифицировать его ровно в одну категорию:

- `OWNER_SIMPLE_ACTION` — детерминированное действие в UI/терминале, не требующее анализа;
- `CHATGPT_REPOSITORY` — работа с кодом/документацией/GitHub, которую можно выполнить без локального Android runtime;
- `ANDROID_STUDIO_AGENT` — проблема требует IDE, SDK, Gradle environment, emulator/device, Logcat или Android runtime;
- `OWNER_DECISION` — продуктовый выбор, требующий решения владельца.

**Android Studio Agent нельзя выбирать, пока не доказано, что задача не относится к первым двум категориям.**

## 3. Жёсткий запрет на расход Agent Mode

По умолчанию НЕ отправлять в Android Studio Agent:

- `git status`, commit, push, pull, fetch, checkout, создание обычной ветки;
- простое открытие проекта;
- обычный Run, если приложение уже собирается;
- запуск известной Gradle-команды, если задача лишь получить результат;
- Markdown и проектную документацию;
- GitHub Issues/PR;
- product/UX/roadmap обсуждение;
- статический code review;
- обычный Kotlin/Compose/Room/DataStore coding, который можно изменить через GitHub;
- **pure Kotlin domain logic и unit tests**;
- LearningPolicy/Curriculum реализацию без Android-specific зависимости;
- форматирование, переименование и рефакторинг, не зависящий от IDE/runtime.

Для этих задач использовать владельца или ChatGPT.

## 4. OWNER_SIMPLE_ACTION — нулевая стоимость Agent

Если действие можно выполнить последовательностью из 1–5 очевидных шагов, Gemini Web должен дать владельцу короткую инструкцию вместо prompt для Agent.

Типовые примеры:

### Commit + Push в Android Studio
1. `Git` → `Commit`.
2. Выбрать изменённые файлы.
3. Ввести сообщение commit.
4. `Commit and Push`.
5. Убедиться, что Push завершился без ошибки.

### Pull
`Git` → `Pull`.

### Run
Выбрать `app` и нажать зелёную кнопку Run.

### Если простое действие завершилось ошибкой
Только тогда собрать текст ошибки/скрин/Logcat и перевести задачу в `ANDROID_STUDIO_AGENT` либо передать ChatGPT, в зависимости от типа ошибки.

## 5. Когда Android Studio Agent действительно разрешён

Agent допускается, если присутствует хотя бы один обязательный локальный фактор:

- Gradle Sync падает и требуется диагностика локальной среды;
- несовместимость JDK/SDK/build tools/Android Gradle Plugin;
- build/test падает и причина зависит от локальной Android toolchain;
- приложение crash'ится на emulator/device;
- нужен анализ Logcat/stack trace;
- TTS, SpeechRecognizer, microphone, permission или Android API ведут себя неправильно на устройстве;
- проблема Manifest/resources/Compose Preview/device configuration;
- баг воспроизводится только в runtime;
- требуется проверить фактический UI/TTS/device behavior и одной ручной проверки владельца недостаточно.

## 6. Правило «сначала дешёвый путь»

Перед Agent Mode применять порядок:

1. Можно ли решить простой ручной операцией владельца? → дать 1–5 шагов.
2. Можно ли решить изменением GitHub/статическим анализом? → ChatGPT.
3. Можно ли получить нужное доказательство через CI? → CI + ChatGPT review.
4. Только если нет → Android Studio Agent.

## 7. Agent Admission Statement

Перед каждым task packet для Android Studio Agent Gemini Web обязан вывести две строки:

`ROUTING: ANDROID_STUDIO_AGENT`

`WHY_AGENT_REQUIRED: <конкретная причина, почему OWNER_SIMPLE_ACTION и CHATGPT_REPOSITORY недостаточны>`

Если убедительную причину сформулировать нельзя — Agent использовать запрещено.

## 8. Формат задания ChatGPT

```text
ROUTING: CHATGPT_REPOSITORY
ИСПОЛНИТЕЛЬ: ChatGPT / Repository Engineer
РЕПОЗИТОРИЙ: dronrome1245/App_ABC

ЦЕЛЬ:
...

СНАЧАЛА ПРОЧИТАТЬ:
...

ЧТО ИЗМЕНИТЬ:
...

НЕ МЕНЯТЬ:
...

КРИТЕРИИ ПРИЁМКИ:
...

GIT:
...

ПОСЛЕ ИЗМЕНЕНИЙ:
...

НУЖЕН ЛИ ПОТОМ ANDROID STUDIO AGENT:
нет / да, только для <конкретная runtime-проверка>
```

## 9. Формат задания Android Studio Agent

```text
ROUTING: ANDROID_STUDIO_AGENT
WHY_AGENT_REQUIRED: <конкретная локальная причина>
ИСПОЛНИТЕЛЬ: Gemini Android Studio Agent

ЦЕЛЬ:
<одна узкая локальная задача>

ВЕТКА/COMMIT:
...

СДЕЛАТЬ:
...

НЕ ДЕЛАТЬ:
- не расширять scope;
- не менять продуктовые решения;
- не делать несвязанный refactor;
- не редактировать документацию, если это не необходимо для фиксации факта локальной проверки.

ВЕРНУТЬ:
- что запускалось;
- точный результат;
- ошибка/Logcat при наличии;
- какие файлы изменены;
- commit/push состояние, если были реальные исправления.
```

## 10. Git-only задачи

**Никогда не формировать Agent task packet только ради commit/push/pull.**

Это `OWNER_SIMPLE_ACTION`. Если Git UI выдаёт ошибку, владелец передаёт текст ошибки Gemini Web; только после этого выбирается исполнитель.

## 11. Pure Kotlin rule

Если задача объявлена как `Strictly Pure Kotlin`, `без android.*`, `JVM unit tests`, `domain layer only`, она автоматически маршрутизируется в **ChatGPT**, а не в Android Studio Agent.

Исключение: после реализации можно отдельно использовать Agent только если локальная Gradle-сборка падает и ошибку нельзя диагностировать по CI/логу без Android Studio.

## 12. Milestone gate

Gemini Web не объявляет milestone завершённым только по наличию кода. Отдельно фиксируются:

- static code review;
- automated tests/CI;
- local Android build/runtime validation;
- owner product acceptance.

Следующий milestone не стартует, пока обязательные gate текущего milestone не выполнены.