# NEXT_TASK.md — M2 Kickoff

## Единственная следующая задача

**Спроектировать и реализовать первый законченный slice Milestone 2: интерфейс `AudioPlayer` для локальных аудио-ассетов (`SoundPool`/`MediaPlayer`) с системным TTS fallback, расширение модели уровней Curriculum и закрытие owner-approved переносов из M1.**

## Обязательный scope этой итерации

1. Ввести единый интерфейс `AudioPlayer`, не зависящий от Compose/UI.
2. Реализовать стратегию `pre-recorded audio first + TTS fallback`.
3. Подготовить работу с локальными `WAV`/`OGG` в `res/raw`; не генерировать случайные/временные записи вместо утверждённых ассетов.
4. Если TTS остаётся fallback, добавить корректную декларацию `<queries>` для `android.intent.action.TTS_SERVICE` в Manifest.
5. Расширить модель Curriculum/уровней так, чтобы состав букв и метаданные не были M1-only константой.
6. Реализовать retry queue: после ошибки target возвращается позднее по детерминируемому/тестируемому правилу без немедленного зацикливания.
7. Расширить результат сессии статистикой по каждой букве, начиная с А/М.
8. Продолжить M2 LearningEngine v1 по `docs/DEFINITION_OF_DONE.md`: централизованный LearningPolicy, версии, mastery states, weighted selection, level unlock, delayed checks и сохранение старых букв в пуле.
9. Добавить/обновить JVM unit tests на учебные инварианты; Android-specific audio integration проверять отдельно.
10. Не менять продуктовые пороги/веса/retry interval/curriculum order без отдельного `DECISION_SOURCE`.

## Архитектурные решения

- D019: незакрытые требования раннего M1 явно перенесены в M2.
- D020: основной звук — локальные pre-recorded assets; TTS — fallback.

## Критерий завершения текущего slice

Перед заявлением о готовности исполнитель обязан выдать DoD evidence matrix: каждый затронутый критерий M2 и каждый перенесённый пункт D019 должны иметь статус `PASS`, `PENDING_RUNTIME` либо `OWNER_DECISION_REQUIRED` с конкретным доказательством/причиной.

## Android Studio Agent

По умолчанию не нужен. Подключать только при конкретной Android runtime/Manifest/audio/TTS проблеме, которую нельзя подтвердить через код, JVM tests или CI.
