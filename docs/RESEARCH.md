# Обоснование решений Android Care

Проверено 22 ноября 2026 года по доступным первоисточникам. Это целевой разбор механизмов Android, используемых скриптом, **не утверждение, что изучены все существующие оптимизаторы, телефоны и прошивки**. Физическое устройство не подключено; результатов игровых бенчмарков у проекта нет.

Ниже ссылки закреплены на ревизиях, чтобы решение можно было проверить после изменения исходников upstream. Скрипт не скачивает и не исполняет эти проекты на телефоне.

## 1. Штатный игровой режим вместо произвольных overrides

Источники AOSP:

- [GameManagerShellCommand.java](https://github.com/aosp-mirror/platform_frameworks_base/blob/1cdfff555f4a21f71ccc978290e2e212e2f8b168/services/core/java/com/android/server/app/GameManagerShellCommand.java): `runListGameModes`, `runSetGameMode`, `isPackageGame`, `onHelp`.
- [GameManager.java](https://github.com/aosp-mirror/platform_frameworks_base/blob/1cdfff555f4a21f71ccc978290e2e212e2f8b168/core/java/android/app/GameManager.java): `GAME_MODE_*`, `getAvailableGameModes`, `setGameMode`; предупреждение о расходе батареи в performance.
- [GameManagerService.java](https://github.com/aosp-mirror/platform_frameworks_base/blob/1cdfff555f4a21f71ccc978290e2e212e2f8b168/services/core/java/com/android/server/app/GameManagerService.java): `updateInterventions`, `updateFps`, `resetFps`.
- Для сравнения интерфейса просмотрена и [ветка Android 13](https://github.com/aosp-mirror/platform_frameworks_base/blob/android13-release/services/core/java/com/android/server/app/GameManagerShellCommand.java): набор shell-команд между выпусками отличается.

**Вывод:** выбор режима `cmd game mode` и принудительная запись интервенций `cmd game set` / `device_config` — разные операции. Performance должен уже поддерживаться пакетом. Существующий режим может запускать OEM/игровую политику, включая FPS-ограничение и масштабирование. Слово performance не гарантирует прирост в конкретной игре.

**В коде:** `game PACKAGE` читает режимы и конфигурации; `game-boost PACKAGE --apply` выбирает только уже поддерживаемый performance, сохраняет прежний режим и номер Android-пользователя, использует явный `--user`, проверяет фактический результат и умеет откатывать. Неизвестный формат ответа или отсутствие `list-modes` — отказ от записи, а не попытка угадать режим по номеру Android. Не используются `cmd game set`, `device_config put` и разблокировка FPS через подмену устройства. Одновременно хранится снимок одной игры, повторное применение не затирает его.

## 2. Thermal Status — не температура, а изменение режима — не охлаждение

Источники:

- [ThermalManagerService.java](https://github.com/aosp-mirror/platform_frameworks_base/blob/1cdfff555f4a21f71ccc978290e2e212e2f8b168/services/core/java/com/android/server/power/ThermalManagerService.java): `dumpInternal`, поля `IsStatusOverride`, `Thermal Status`, `HAL Ready`.
- [PowerManager.java](https://github.com/aosp-mirror/platform_frameworks_base/blob/1cdfff555f4a21f71ccc978290e2e212e2f8b168/core/java/android/os/PowerManager.java): `THERMAL_STATUS_*` и их смысл.
- [BatteryService.java](https://github.com/aosp-mirror/platform_frameworks_base/blob/1cdfff555f4a21f71ccc978290e2e212e2f8b168/services/core/java/com/android/server/BatteryService.java): `dumpInternal`, `temperature`, предупреждение `UPDATES STOPPED` при симуляции состояния.

**Вывод:** батарейный датчик не заменяет датчики SoC. Нулевая температура и нулевой thermal status не должны подставляться вместо отсутствующего доступа. Система может публиковать тестовый override или ещё не иметь готового thermal HAL.

**В коде:** перед включением performance нужны действительный thermal-ответ без override с `HAL Ready: true`, статус 0 или 1 и доступная температура батареи ниже 40 °C. При `UPDATES STOPPED` батарейное показание не используется. Статус 2 и выше, неполные данные, запрет доступа и неизвестный формат блокируют применение.

**40 °C — консервативная политика этого инструмента, не универсальный опасный порог производителя и не медицинский/аппаратный диагноз.** Проверка выполняется до изменения режима. Скрипт не работает постоянно: последующий нагрев не отслеживается автоматически и performance не отменяется фоновым демоном. Thermal protection Android остаётся включённой.

## 3. HWUI-процентили нельзя выдавать за игровой FPS

Источники:

- [ProfileData.cpp](https://github.com/aosp-mirror/platform_frameworks_base/blob/1cdfff555f4a21f71ccc978290e2e212e2f8b168/libs/hwui/ProfileData.cpp): `dump`, `findPercentile`, `reset`; счётчики кадров, jank и гистограммы.
- [Android performance samples](https://github.com/android/performance-samples/blob/05c8130adfd2d7e35d4cd25033cb4f89b1008c1f/README.md): разные инструменты для macrobenchmark, microbenchmark, JankStats и трассировки.
- [Perfetto](https://github.com/google/perfetto/blob/06996a03bcf7b3df866eef1fafc74a37d5103152/README.md) и [FrameTimeline](https://github.com/google/perfetto/blob/06996a03bcf7b3df866eef1fafc74a37d5103152/docs/data-sources/frametimeline.md): временная шкала выполнения и причины jank; у источников и типов рендера есть ограничения покрытия.

**Вывод:** накопленная статистика Android UI не является универсальным измерителем кадров игрового движка. Данные меню и игрового SurfaceView/Vulkan могут отличаться. Нельзя преобразовать p95 в «средний FPS» или назвать пустую выборку 0 FPS. Частота экрана тоже не измерение рендера.

**В коде:** `frames PACKAGE` сохраняет только агрегаты `gfxinfo`: total, jank, p50/p90/p95/p99 и GPU-процентили, если есть ненулевая HWUI-выборка. Процессы, activity и полные frame dumps не выводятся. Счётчики не сбрасываются и игра не перезапускается. При отсутствии выборки/доступа результат помечается как неизмеренный. Для глубокой причины рекомендуется отдельная трассировка Perfetto; скрипт её автоматически не записывает и ничего не загружает.

## 4. Почему нет RAM-cleaner и периодического drop_caches

Источник: [Linux sysctl vm, drop_caches](https://github.com/torvalds/linux/blob/4843f6873f7c4835e62f2c73ba0fd538e57a2f22/Documentation/admin-guide/sysctl/vm.rst#drop_caches).

Документация ядра объясняет автоматическое освобождение кэшей по необходимости и предупреждает о затратах CPU/IO на их повторное создание. Использование `drop_caches` вне тестирования/отладки не рекомендуется.

**В коде:** нет записи в `/proc/sys/vm/drop_caches`, убийства всех процессов или цикла очистки. Отчёт показывает `MemAvailable` и доступные PSI. `trim --apply` — отдельная, подтверждаемая пользователем очистка восстанавливаемого *дискового кэша приложений* через Package Manager, не «чистка ОЗУ» и не игровой boost.

## 5. Зачем наблюдение во времени

Разовый снимок не описывает прогрев, длительную нагрузку или изменение состояния thermal. `monitor [1..12]` пишет ограниченную серию CSV без искусственного стресс-теста. Между отсчётами пауза 5 с плюс время запросов; это не строго периодический лабораторный измеритель.

- CPU busy вычисляется из разницы агрегированных счётчиков `/proc/stat`, без повторного суммирования guest-time.
- Первый CPU-отсчёт и недоступные данные — `NA`, а не выдуманный ноль.
- Доступная ОЗУ — kB из `/proc/meminfo`; это не объём памяти игры.
- Thermal status — шкала Android, батарея — °C. CPU/GPU clock и FPS из этих величин не вычисляются.
- Фоновая нагрузка и сам мониторинг влияют на результат. В Termux Android может ограничивать фоновые процессы; длительность отсчётов может расти.

Для сравнения изменений: одна игра, одна сцена, одинаковые графика/яркость/частота экрана, сопоставимые заряд и температура, несколько прогонов. Не сравнивайте холодный старт с уже прогретым игровым процессом. Без игрового счётчика/подходящей трассировки прирост игрового FPS **не доказан**.

## 6. Shizuku — мост разрешений, не автоматическое получение root

Источник: [официальный RISH README](https://github.com/RikkaApps/Shizuku-API/blob/5ab1d6f844ca6d24fff691bdaa3bc44c308ce1ca/rish/README.md): передача аргументов удалённому shell, `RISH_PRESERVE_ENV` и недоступность приватного Termux PATH для shell UID.

**В коде:** launcher использует `rish -c`, сохраняет stdin для подтверждения, не переносит Termux окружение в привилегированный shell. Уже имеющийся UID 2000/0 работает напрямую. Без моста работает только read-only app-режим. Нет автоматической установки DEX неизвестного происхождения, обхода авторизации, SELinux или запроса root. Ошибку операции записи нельзя «исправлять» её повторным запуском в другом режиме.

## Что пока не доказано

Mock-тесты проверяют ветвления и безопасность операций, **не FPS, автономность и стабильность реального телефона**. OEM-форматы, системные разрешения, многопользовательские гонки и изменения игры могут ограничить возможности. Снимки и проверка после записи уменьшают риск, но не обеспечивают атомарность при отключении питания, SIGKILL или внешнем изменении настроек. Для конкретных рекомендаций нужны модель устройства, версия Android, игра и полученные на нём отчёты.
