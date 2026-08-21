# NEXT_TASK.md — M3.2 Full Curriculum Runtime Expansion

## Единственная следующая задача

**Этап M3.2: расширение Curriculum в коде до Уровней 4–8 и добавление 27 аудио-файлов букв в `res/raw`.**

## Перед стартом

M3.2 начинается только после завершения M3.1 и зелёного CI.

Обязательный preflight:

1. Внести в `docs/CURRICULUM.md` точную owner-approved D024 matrix распределения оставшихся 27 букв между Levels 4–8. Текущий M3.1 task packet сообщает только 8 уровней и полный охват 33 букв, но не содержит буквального состава каждого из Levels 4–8; AI не должен придумывать его.
2. Зафиксировать новый `curriculumVersion` для фактического runtime-расширения.
3. Проверить spokenName каждой новой буквы и имена локальных raw resources.

## Scope M3.2

- расширить `ApprovedCurriculum` с Levels 1–3 до Levels 1–8 по D024;
- сохранить накопленный пул ранее введённых букв;
- не менять 10 вопросов и unlock `>=80%` / `8 из 10`;
- добавить 27 локальных OGG/WAV ресурсов для оставшихся букв;
- расширить mapping `HybridAudioPlayer` на все 33 буквы;
- сохранить TTS fallback для отсутствующего/ошибочного asset;
- обновить seed данных букв без destructive migration;
- добавить deterministic curriculum tests для состава уровней, накопленного пула и distractor invariant;
- прогнать JVM tests, `assembleDebug`, CI и затем owner smoke новых уровней/озвучки.

## Не менять в M3.2

- Room schema 2 без отдельной необходимости;
- LearningPolicy v3 и mastery thresholds;
- D025 7-day decay до отдельного implementation task/version decision;
- Parent Dashboard M3.1 без необходимости;
- внешние analytics/ads/backend.

## Android Studio Agent

Не нужен по умолчанию. Подключать только при конкретной локальной audio/runtime проблеме на устройстве.
