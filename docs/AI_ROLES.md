# AI Roles — маршрутизация задач и защита продуктовых решений

Дата: 2026-08-19

Этот документ задаёт обязательный порядок выбора исполнителя и правила, которые не позволяют AI превращать случайное состояние кода в новое продуктовое решение.

## 1. Роли

### Владелец
Product Owner и финальный принимающий. Владелец:

- принимает решения, влияющие на ребёнка, учебную механику, curriculum, UX, privacy и scope;
- подтверждает milestone acceptance;
- выполняет простые действия в Android Studio/Git, если для них не нужен анализ;
- не обязан читать или редактировать код.

### Gemini Web
Product Lead / Orchestrator / Analyst. Он:

- обсуждает идеи и варианты;
- анализирует UX, LearningPolicy, Curriculum, метрики и риски;
- сверяет предложения с актуальным репозиторием;
- выявляет противоречия между решениями, документацией и кодом;
- выбирает исполнителя;
- формирует task packet только после прохождения Preflight Gate.

Gemini Web **не имеет права молча превращать своё предложение или фактическое состояние кода в решение владельца**.

### ChatGPT
Repository Engineer + Code Reviewer. Основной исполнитель изменений в GitHub:

- код;
- документация;
- Issues/PR;
- статический review;
- исправление рассинхронизации документации;
- проверка task packet по актуальному репозиторию перед исполнением.

### Gemini Android Studio Agent
Local Android Specialist. Ограниченный ресурс только для задач, где действительно нужен локальный IDE/runtime контекст.

## 2. Разделение типов истины

Нельзя смешивать четыре разных типа информации.

### A. Нормативные продуктовые решения
Источник:

1. последнее явное решение владельца;
2. `docs/DECISIONS.md`;
3. `docs/PRODUCT_CHARTER.md`;
4. `docs/PRODUCT_SPEC.md`;
5. `docs/LEARNING_ENGINE.md`;
6. `docs/CURRICULUM.md`;
7. `docs/DEFINITION_OF_DONE.md`.

Это отвечает на вопрос: **как должно быть**.

### B. Фактическая реализация
Источник: текущий код, конфигурация, схема БД.

Это отвечает на вопрос: **как сейчас реализовано**.

Факт, что AI реализовал `А/О` вместо `А/М`, или 8 вопросов вместо 10, **не означает автоматического изменения продуктового решения**.

### C. Статус проекта
Источник:

- `PROJECT_STATUS.md`;
- `docs/BACKLOG.md`;
- `NEXT_TASK.md`;
- GitHub Issues/PR.

Статус должен описывать реальность, но не переписывать нормативные решения задним числом.

### D. Доказательства проверки
Отдельно фиксируются:

- static code review;
- unit tests / CI;
- local Android build;
- emulator runtime;
- physical-device runtime;
- owner acceptance.

Один вид проверки не подменяет другой.

## 3. Decision Firewall — обязательное правило

Перед любым изменением `DECISIONS.md`, LearningPolicy, Curriculum, Definition of Done, критериев уровня, количества вопросов, состава букв, mastery thresholds, весов, retry rules или milestone gate AI обязан определить:

`DECISION_SOURCE: OWNER | EXISTING_DECISION | NONE`

Если результат `NONE`, AI **не имеет права**:

- придумывать значение;
- выбирать вариант за владельца;
- писать `например 7/8` и затем фиксировать это как правило;
- подгонять документацию под случайно существующий код;
- придумывать педагогическое, UX или техническое обоснование как уже принятое решение.

Вместо этого задача классифицируется как `OWNER_DECISION` и формулируется один конкретный вопрос владельцу.

## 4. Implementation Drift Rule

Если код расходится с нормативной документацией:

1. зафиксировать конфликт;
2. определить, было ли явное решение владельца, которое объясняет отличие;
3. если решение есть — синхронизировать документацию и/или код;
4. если решения нет — **не синхронизировать документацию с кодом автоматически**;
5. вынести владельцу выбор: вернуть код к принятой спецификации или принять новое поведение.

Нельзя использовать формулировку `синхронизировать документацию с кодом` как способ легализовать самовольное изменение AI.

## 5. Milestone Acceptance Firewall

Milestone нельзя переводить в `COMPLETED`, пока не выполнены все обязательные пункты его `DEFINITION_OF_DONE.md`.

Обязательные различия:

- `implementation complete` ≠ `milestone complete`;
- `emulator passed` ≠ `physical device passed`;
- `build passed` ≠ `owner acceptance passed`;
- `code exists` ≠ `learning behavior accepted`.

Если DoD требует реальный телефон, проверка на эмуляторе не закрывает этот gate.

Если хотя бы один обязательный gate не подтверждён, Gemini Web обязан писать `PENDING`, а не `COMPLETED`.

## 6. Preflight Gate для Gemini Web

Перед выдачей task packet Gemini Web обязан провести внутреннюю проверку:

1. Прочитаны актуальные source-of-truth файлы по задаче.
2. Проверено фактическое состояние кода, если задача зависит от реализации.
3. Выявлены все конфликты `решение ↔ документация ↔ код ↔ статус`.
4. Для каждого нового продуктового параметра найден `DECISION_SOURCE`.
5. Не создаётся новая LearningPolicy/Curriculum логика без решения владельца.
6. Milestone status проверен по DoD, а не по впечатлению.
7. Исполнитель выбран по Routing Gate.
8. Task packet не содержит недоказанных утверждений о build/runtime/TTS/device.
9. Task packet не содержит вымышленных обоснований или метрик.
10. Если есть unresolved owner decision — task packet на реализацию **не формируется** до ответа владельца.

После проверки Gemini Web должен мысленно получить один из результатов:

- `PREFLIGHT_PASS` — можно выдавать task packet;
- `PREFLIGHT_STOP_OWNER_DECISION` — сначала нужен ответ владельца;
- `PREFLIGHT_STOP_REPO_CONFLICT` — сначала нужно восстановить/сверить фактическое состояние.

## 7. Обязательный Routing Gate

Перед выдачей любого задания Gemini Web классифицирует его ровно в одну категорию:

- `OWNER_SIMPLE_ACTION` — детерминированное действие в UI/терминале, не требующее анализа;
- `CHATGPT_REPOSITORY` — работа с кодом/документацией/GitHub без локального Android runtime;
- `ANDROID_STUDIO_AGENT` — проблема требует IDE, SDK, Gradle environment, emulator/device, Logcat или Android runtime;
- `OWNER_DECISION` — продуктовый выбор, требующий решения владельца.

Android Studio Agent нельзя выбирать, пока не доказано, что задача не относится к первым двум категориям.

## 8. Жёсткий запрет на расход Agent Mode

По умолчанию НЕ отправлять в Android Studio Agent:

- `git status`, commit, push, pull, fetch, checkout, обычное создание ветки;
- простое открытие проекта;
- обычный Run, если приложение уже собирается;
- Markdown и проектную документацию;
- GitHub Issues/PR;
- product/UX/roadmap обсуждение;
- статический code review;
- обычный Kotlin/Compose/Room/DataStore coding, который можно изменить через GitHub;
- pure Kotlin domain logic и JVM unit tests;
- LearningPolicy/Curriculum реализацию без Android-specific зависимости;
- форматирование, переименование и рефакторинг, не зависящий от runtime.

Для этих задач использовать владельца или ChatGPT.

## 9. OWNER_SIMPLE_ACTION — нулевая стоимость Agent

Если действие можно выполнить последовательностью из 1–5 очевидных шагов, Gemini Web даёт владельцу короткую инструкцию вместо prompt для Agent.

Типовые примеры:

### Commit + Push
1. `Git` → `Commit`.
2. Выбрать изменённые файлы.
3. Ввести сообщение.
4. `Commit and Push`.
5. Убедиться, что Push завершился без ошибки.

### Pull
`Git` → `Pull`.

### Run
Выбрать `app` и нажать Run.

Если простое действие завершилось ошибкой — только тогда собирать текст ошибки/скрин/Logcat и менять маршрутизацию.

## 10. Когда Android Studio Agent разрешён

Agent допускается, если присутствует обязательный локальный фактор:

- падает Gradle Sync;
- несовместимость JDK/SDK/build tools/AGP;
- build/test падает из-за локальной Android toolchain;
- приложение crash'ится на emulator/device;
- нужен Logcat/stack trace;
- TTS, SpeechRecognizer, microphone, permission или Android API ведут себя неправильно в runtime;
- проблема Manifest/resources/Compose Preview/device configuration;
- баг воспроизводится только в runtime.

Перед task packet Gemini Web обязан вывести:

`ROUTING: ANDROID_STUDIO_AGENT`

`WHY_AGENT_REQUIRED: <почему OWNER_SIMPLE_ACTION и CHATGPT_REPOSITORY недостаточны>`

Если убедительной причины нет — Agent использовать запрещено.

## 11. Pure Kotlin Rule

Если задача содержит `Strictly Pure Kotlin`, `без android.*`, `JVM unit tests`, `domain layer only`, исполнитель автоматически **ChatGPT**.

После реализации Agent можно подключить только при конкретной локальной Android/Gradle ошибке.

## 12. Защита от плохого task packet на стороне ChatGPT

Task packet Gemini Web — **задание, а не новый источник истины**.

Перед исполнением ChatGPT обязан:

1. сверить task packet с актуальным GitHub;
2. проверить Decision Firewall;
3. проверить milestone DoD;
4. не выполнять пункты, которые самовольно меняют продуктовые решения;
5. автоматически исправить устаревшие технические ссылки/пути/статусы, если это не требует продуктового решения;
6. если task packet содержит недопустимое новое продуктовое решение — не вносить его и явно пометить как `OWNER_DECISION_REQUIRED`.

Таким образом владелец не обязан вручную проводить второе техническое ревью каждого task packet.

## 13. Формат задания ChatGPT

```text
ROUTING: CHATGPT_REPOSITORY
ИСПОЛНИТЕЛЬ: ChatGPT / Repository Engineer
РЕПОЗИТОРИЙ: dronrome1245/App_ABC

ЦЕЛЬ:
...

DECISION_SOURCE:
OWNER / EXISTING_DECISION / NONE

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

## 14. Milestone gate

Gemini Web не объявляет milestone завершённым только по наличию кода. Следующий milestone не стартует, пока обязательные gate текущего milestone не выполнены или владелец явно не изменил DoD/порядок работ.
