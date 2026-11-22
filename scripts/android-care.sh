#!/system/bin/sh
# Android Care — read-only app mode, or full mode via Shizuku/rish / ADB.
# No root, downloads, telemetry, eval, package removal or background services.
set -u
PATH=/system/bin:/system/xbin:/vendor/bin
LC_ALL=C
export PATH LC_ALL
umask 077
STATE=/data/local/tmp/android-care
KEYS='window_animation_scale transition_animation_scale animator_duration_scale'
LOCKED=0
CHANGING=0
BASIC=0
GAMING=0
REPORT=

say() { printf '%s\n' "$*"; }
fail() { say "ОШИБКА: $*" >&2; exit 1; }
usage() {
    while IFS= read -r line; do printf '%s\n' "$line"; done <<'HELP'
Android Care
  audit                 Диагностика (по умолчанию); настройки не меняются
  menu                  Интерактивное меню
  game PACKAGE          Game Manager: режимы и существующие конфигурации
  frames PACKAGE        Накопленная HWUI-статистика (не игровой FPS)
  monitor [1..12]       CSV наблюдения: CPU, память, батарея, thermal; по умолчанию 6 отсчётов
  game-boost PACKAGE    План штатного performance-режима
  game-boost PACKAGE --apply  Применить после проверки поддержки, температуры и подтверждения
  game-restore [--apply] Вернуть сохранённый Game Mode
  optimize              План ускорения анимаций, без изменений
  optimize --apply      Ограничить анимации 0.5x, сохранив уже более быстрые значения
  restore               План отката
  restore --apply       Восстановить исходные анимации
  trim                  План очистки восстанавливаемого кэша
  trim --apply          Попросить Android освободить место до цели 1 GiB
  help                  Эта справка
Изменения требуют отдельного ввода ДА. --apply сам по себе не подтверждение.
HELP
}
cleanup() {
    if [ "$GAMING" = 1 ]; then
        say "Изменение Game Mode не завершено. Снимок: $STATE/game.tsv; выполните game-restore --apply." >&2
    fi
    if [ "$CHANGING" = 1 ]; then
        say "Изменение прервано. Снимок сохранён: $STATE/animations.tsv" >&2
        say 'Выполните restore --apply для восстановления.' >&2
    fi
    if [ "$LOCKED" = 1 ]; then
        rm -f "$STATE/command.tmp" "$STATE/command.tmp.packages" "$STATE/animations.tmp" "$STATE/game.tmp"
        rmdir "$STATE/lock" 2>/dev/null || :
    fi
}
init() {
    command -v getprop >/dev/null 2>&1 || fail 'Нужен Android. Запустите через run.sh / rish.'
    SDK=$(getprop ro.build.version.sdk)
    case "$SDK" in ''|*[!0-9]*) fail 'Не удалось определить Android API.' ;; esac
    [ "$SDK" -ge 23 ] || fail 'Нужен Android 6 или новее; Shizuku без root обычно требует Android 11+.'
    UID_NOW=$(id -u) || fail 'Не удалось проверить UID.'
    case "$UID_NOW" in
        ''|*[!0-9]*) fail 'Не удалось проверить UID.' ;;
        2000|0) ;;
        *)
            BASIC=1
            [ "$APPLY" = 0 ] || fail 'Нет shell-доступа: применение требует Shizuku/rish или ADB. Диагностика работает без них.'
            case "${HOME:-}" in /*) ;; *) fail 'Для базового режима нужен абсолютный путь HOME.' ;; esac
            STATE=$HOME/.android-care
            ;;
    esac
    # Never follow a pre-existing state directory symlink.
    [ ! -L "$STATE" ] || fail 'Каталог состояния — символическая ссылка. Отказ.'
    mkdir -p "$STATE" || fail 'Не удалось создать каталог отчётов.'
    [ "$(stat -c %u "$STATE")" = "$UID_NOW" ] || fail 'У каталога состояния другой владелец.'
    chmod 700 "$STATE" || fail 'Не удалось ограничить доступ к отчётам.'
    mkdir "$STATE/lock" 2>/dev/null || fail "Уже есть работающий запуск или старый lock: $STATE/lock. См. README."
    LOCKED=1
    trap cleanup 0
    trap 'exit 130' INT
    trap 'exit 143' TERM HUP
    cd / || fail 'Не удалось сменить каталог.'
    command -v timeout >/dev/null 2>&1 || fail 'Нет системной команды timeout; небезопасно запускать диагностику без лимитов.'
    for file in animations.tsv animations.tmp game.tsv game.tmp command.tmp command.tmp.packages; do
        [ ! -L "$STATE/$file" ] || fail "Недопустимая ссылка: $file"
    done
}
bounded() { timeout "$@"; }
# Capture before displaying so both a command's real exit status and truncation are visible.
section() {
    title=$1
    shift
    {
        printf '\n=== %s ===\n' "$title"
        bounded 20 "$@" > "$STATE/command.tmp" 2>&1
        result=$?
        sed -n '1,100p' "$STATE/command.tmp"
        lines=$(wc -l < "$STATE/command.tmp")
        [ "$lines" -le 100 ] || say '[Показаны первые 100 строк]'
        [ "$result" -eq 0 ] || say "[Недоступно / ошибка / таймаут, код $result. Это не доказательство неисправности.]"
    } >> "$REPORT"
}
prop_report() {
    for prop in ro.product.manufacturer ro.product.model ro.build.version.release ro.build.version.sdk ro.build.version.security_patch ro.hardware; do
        printf '%s: %s\n' "$prop" "$(getprop "$prop")"
    done
}
read_settings() {
    for key in $KEYS; do
        printf '%s: ' "$key"
        bounded 10 settings get global "$key"
    done
    printf 'low_power: '; bounded 10 settings get global low_power
    printf 'always_finish_activities: '; bounded 10 settings get global always_finish_activities
    printf 'background_process_limit (может отсутствовать): '; bounded 10 settings get global background_process_limit
}
package_counts() {
    bounded 15 pm list packages > "$STATE/command.tmp.packages" 2>&1 || {
        cat "$STATE/command.tmp.packages"; rm -f "$STATE/command.tmp.packages"; return 1;
    }
    printf 'Все видимые пакеты: '; grep -c '^package:' "$STATE/command.tmp.packages" || :
    bounded 15 pm list packages -d > "$STATE/command.tmp.packages" 2>&1 || {
        cat "$STATE/command.tmp.packages"; rm -f "$STATE/command.tmp.packages"; return 1;
    }
    printf 'Отключённые: '; grep -c '^package:' "$STATE/command.tmp.packages" || :
    rm -f "$STATE/command.tmp.packages"
}
findings() {
    say ''; say '=== Краткая оценка по доступным данным ==='
    awk '
        /^MemTotal:/ { total=$2 }
        /^MemAvailable:/ { avail=$2; found=1 }
        END {
            if (total > 0 && found) {
                p=100*avail/total
                printf "ОЗУ: доступно %.0f MiB из %.0f MiB (%.1f%%).\n", avail/1024, total/1024, p
                if (p < 10) print "Внимание: мало доступной ОЗУ. Проверьте тяжёлые приложения; не используйте RAM-cleaner."
            } else print "ОЗУ: MemAvailable недоступен, оценка пропущена."
        }
    ' /proc/meminfo 2>/dev/null || say 'ОЗУ: нет доступа.'
    if bounded 10 df -k /data > "$STATE/command.tmp" 2>/dev/null; then
        awk '
            $2 ~ /^[0-9]+$/ && $4 ~ /^[0-9]+$/ && $2 > 0 {
                found=1; p=100*$4/$2
                printf "Хранилище /data: свободно %.1f GiB (%.1f%%).\n", $4/1048576, p
                if (p < 10) print "Внимание: мало места. Начните с настроек хранилища и удаления ненужных загрузок."
            }
            END { if (!found) print "Хранилище: формат df не распознан, смотрите раздел выше." }
        ' "$STATE/command.tmp"
    else say 'Хранилище: свободное место не удалось прочитать.'; fi
    if [ "$BASIC" = 1 ]; then
        say 'Батарея: температура недоступна без shell-доступа; не оценивается.'
    elif measured=$(battery_temperature); then
        printf 'Температура батареи: %s °C.\n' "$measured"
        awk -v t="$measured" 'BEGIN { if(t>=40) print "Батарея тёплая/горячая: снизьте нагрузку, особенно при зарядке. 40 °C — ориентир, не диагноз." }'
    else say 'Батарея: оценка температуры недоступна.'; fi
    if [ "$BASIC" = 0 ]; then
        if status=$(thermal_status); then
            say "Thermal Status Android: $status (0 — нет ограничения, 1 — лёгкое, 2–6 — усиливающиеся ограничения)."
            case "$status" in 0|1) ;; *) say 'Есть тепловое ограничение. Не усиливайте нагрузку, дайте устройству остыть.' ;; esac
        else say 'Thermal Status: нет достоверного ответа без override и с готовым HAL.'; fi
    fi
    say 'Это разовый снимок. Отсутствие предупреждений не гарантирует исправность устройства.'
}
audit() {
    REPORT=$(mktemp "$STATE/report-$(date +%Y%m%d-%H%M%S)-XXXXXX") || fail 'Не удалось создать отчёт.'
    {
        say 'ANDROID CARE · локальная диагностика'
        date
        say "UID: $UID_NOW; Android API: $SDK"
        if [ "$BASIC" = 1 ]; then
            say 'Режим: БАЗОВЫЙ, без повышенных прав. Доступны только разрешённые Android показатели.'
            say 'Системные изменения заблокированы; ограниченные разделы не считаются проверенными.'
        else say 'Режим: ПОЛНЫЙ, shell-доступ подтверждён.'; fi
        say 'Снимок текущего состояния, не аппаратный тест. Закрытые OEM-данные могут быть недоступны.'
        say 'Идентификаторы, аккаунты, Wi-Fi сети, logcat и содержимое приложений не собираются.'
    } > "$REPORT"
    say 'Проверяю устройство. Некоторые разделы могут занять до 20 секунд каждый…'
    # Run shell functions here, not in timeout subprocesses.
    { say ''; say '=== Устройство ==='; prop_report; } >> "$REPORT"
    section 'Время работы и нагрузка CPU' sh -c 'uptime; cat /proc/loadavg'
    section 'Оперативная память (kB)' cat /proc/meminfo
    # Variables in these command strings intentionally expand in the child shell.
    # shellcheck disable=SC2016
    section 'Давление на память / CPU / IO (PSI)' sh -c 'for f in /proc/pressure/memory /proc/pressure/cpu /proc/pressure/io; do echo "$f"; cat "$f"; done'
    section 'Свободное место' df -h /data /storage/emulated/0
    if [ "$BASIC" = 0 ]; then
        section 'Батарея: level/scale, temperature в десятых °C; health зависит от Android' dumpsys battery
        section 'Термослужба: датчики и текущие ограничения' dumpsys thermalservice
    else
        printf '\n=== Батарея / термослужба ===\n' >> "$REPORT"
        say 'Не проверено: для системных dumpsys нужен shell-доступ.' >> "$REPORT"
    fi
    # shellcheck disable=SC2016
    section 'CPU: доступные частоты в kHz (это не измерение производительности)' sh -c 'for p in /sys/devices/system/cpu/cpufreq/policy*; do [ -d "$p" ] || continue; echo "$p"; for n in scaling_governor scaling_cur_freq scaling_min_freq scaling_max_freq cpuinfo_max_freq; do printf "%s: " "$n"; cat "$p/$n"; done; done'
    if [ "$BASIC" = 0 ]; then
        # Allowlist summaries: full dumps include user application names / whitelists.
        section 'Режим простоя Doze (без списков приложений)' sh -c 'dumpsys deviceidle | grep -E "^[[:space:]]*(mState=|mLightState=|mScreenOn=|mCharging=|mNetworkConnected=|mForceIdle=|mDeepEnabled=|mLightEnabled=)"'
        section 'Хранилище: агрегированная оценка Android' sh -c 'dumpsys diskstats | grep -E "^[[:space:]]*(Latency:|Data-Free:|Cache-Free:|System-Free:|File-based Encryption:|App Size:|App Data Size:|App Cache Size:|Photos Size:|Videos Size:|Audio Size:|Downloads Size:|System Size:|Other Size:)"'
        section 'Дисплей: заявленные Android режимы, НЕ FPS игры' sh -c 'dumpsys display | grep -E "^[[:space:]]*(mActiveModeId=|mDefaultModeId=|mSupportedModes=|mDesiredDisplayModeSpecs=)"'
    else
        say 'Doze, системная оценка хранилища и режимы дисплея: не проверены без shell-доступа.' >> "$REPORT"
    fi
    {
        if [ "$BASIC" = 0 ]; then
            say ''; say '=== Настройки (только чтение) ==='; read_settings
            say ''; say '=== Пакеты (без списка приложений) ==='; package_counts
        else
            say ''; say 'Настройки и пакеты: проверка пропущена без shell-доступа.'
        fi
        findings
        say ''; say '=== Как действовать ==='
        say '• Низкий MemFree сам по себе нормален: Android использует ОЗУ под кэш. Смотрите MemAvailable и PSI.'
        say '• Если /data почти заполнен, освободите место через системное хранилище. Желательно оставить 10–15%.'
        say '• При нагреве дайте телефону остыть и снимите нагрузку. Порог зависит от модели и датчика.'
        say '• Износ батареи и причины разряда нельзя надёжно определить одним запуском.'
        say '• optimize --apply меняет только длительность анимаций; CPU/GPU и FPS не ускоряет.'
        say '• 300+ FPS не гарантируются: нужны подходящие CPU/GPU, поддержка игры и дисплей. Гц — не измерение FPS.'
        say '• Для игр: выберите поддерживаемую частоту в настройках экрана, снизьте тяжёлую графику и избегайте перегрева.'
        say '• Скрипт не разблокирует лимит FPS игры и не подменяет модель телефона.'
        say '• trim --apply удаляет лишь разрешённый системой восстанавливаемый кэш; первый запуск приложений может стать медленнее.'
        say '• Doze, энергосбережение и фоновые лимиты здесь не меняются: это может ломать уведомления.'
        say '• Отчёт не проверяет вирусы, файловую систему, закрытые данные приложений или всё оборудование.'
        if [ -f "$STATE/animations.tsv" ]; then
            say '• Есть снимок анимаций. Для отката: restore --apply.'
        fi
    } >> "$REPORT" 2>&1
    cat "$REPORT"
    say "Отчёт сохранён: $REPORT"
}
confirm() {
    say 'Для подтверждения введите ДА (любое другое значение — отмена):'
    IFS= read -r answer || return 1
    [ "$answer" = 'ДА' ]
}
# The backup is data, never shell code. Validate the entire file before any write.
valid_snapshot() {
    [ -f "$STATE/animations.tsv" ] || return 1
    awk -F '\t' '
        BEGIN { k["window_animation_scale"]=1; k["transition_animation_scale"]=1; k["animator_duration_scale"]=1 }
        NF != 2 || !($1 in k) || seen[$1]++ { bad=1 }
        $2 != "null" && $2 !~ /^[0-9]+([.][0-9]+)?$/ { bad=1 }
        END { exit (bad || NR != 3) }
    ' "$STATE/animations.tsv"
}
read_value() {
    value=$(bounded 10 settings get global "$1") || return 1
    printf '%s\n' "$value" | grep -Eq '^(null|[0-9]+([.][0-9]+)?)$' || return 1
    printf '%s\n' "$value"
}
same_value() {
    if [ "$1" = null ] || [ "$2" = null ]; then [ "$1" = "$2" ]; else
        awk -v a="$1" -v b="$2" 'BEGIN { exit ((a + 0) != (b + 0)) }'
    fi
}
write_value() {
    [ "$BASIC" = 0 ] || return 1
    wkey=$1; wvalue=$2
    if [ "$wvalue" = null ]; then
        bounded 10 settings delete global "$wkey" >/dev/null 2>&1 || return 1
    else
        bounded 10 settings put global "$wkey" "$wvalue" >/dev/null 2>&1 || return 1
    fi
    actual=$(read_value "$wkey") || return 1
    same_value "$actual" "$wvalue"
}
snapshot() {
    : > "$STATE/animations.tmp" || return 1
    for key in $KEYS; do
        original=$(read_value "$key") || { rm -f "$STATE/animations.tmp"; return 1; }
        printf '%s\t%s\n' "$key" "$original" >> "$STATE/animations.tmp" || return 1
    done
    mv "$STATE/animations.tmp" "$STATE/animations.tsv" || return 1
    valid_snapshot
}
restore_values() {
    valid_snapshot || { say 'Снимок отсутствует или повреждён. Никакие значения не записаны.' >&2; return 1; }
    errors=0
    while IFS="$(printf '\t')" read -r key original; do
        if write_value "$key" "$original"; then say "Восстановлено: $key = $original"; else
            say "Не удалось восстановить: $key" >&2; errors=1
        fi
    done < "$STATE/animations.tsv"
    [ "$errors" = 0 ] || return 1
    rm -f "$STATE/animations.tsv" || return 1
    CHANGING=0
}
optimize() {
    say 'План: анимации не выше 0.5x. Уже выключенные или более быстрые анимации сохраняются.'
    say 'Это сокращает ожидание анимаций, но не повышает вычислительную мощность. Есть откат.'
    [ "$APPLY" = 1 ] || { say 'Сейчас ничего не изменено. Для применения: optimize --apply.'; return 0; }
    [ ! -e "$STATE/animations.tsv" ] || fail 'Снимок уже есть. Сначала выполните restore --apply; исходные значения не перезаписываются.'
    confirm || { say 'Отменено.'; return 0; }
    snapshot || fail 'Не удалось сохранить корректные исходные значения. Настройки не менялись.'
    CHANGING=1
    for key in $KEYS; do
        target=$(awk -F '\t' -v k="$key" '$1==k { if ($2!="null" && $2+0 <= 0.5) print $2; else print "0.5" }' "$STATE/animations.tsv")
        if ! write_value "$key" "$target"; then
            say "Ошибка записи/проверки $key. Пробую откат…" >&2
            restore_values || say 'Откат неполный. Снимок сохранён, повторите restore --apply.' >&2
            return 1
        fi
    done
    CHANGING=0
    say "Готово: анимации не выше 0.5x. Исходные значения: $STATE/animations.tsv"
    say 'Отмена изменений: restore --apply. Очистка кэша НЕ запускалась.'
}
restore() {
    valid_snapshot || fail 'Нет корректного снимка для отката. Ничего не изменено.'
    say 'Будут восстановлены эти исходные глобальные настройки:'
    cat "$STATE/animations.tsv"
    [ "$APPLY" = 1 ] || { say 'Для применения: restore --apply.'; return 0; }
    confirm || { say 'Отменено.'; return 0; }
    CHANGING=1
    restore_values
}
trim_cache() {
    say 'План: pm trim-caches 1G internal — попросить Android достичь 1 GiB свободного места.'
    say 'Это цель свободного места, НЕ объём удаления. При наличии 1 GiB эффект может быть нулевым.'
    say 'Android сам выбирает восстанавливаемый кэш приложений. Данные и настройки приложений не сбрасываются.'
    say 'Удалённый кэш вернуть нельзя; приложения создадут/скачают его снова. Это может временно замедлить их.'
    [ "$APPLY" = 1 ] || { say 'Сейчас ничего не изменено. Для применения: trim --apply.'; return 0; }
    confirm || { say 'Отменено.'; return 0; }
    say 'До:'; bounded 10 df -h /data || say 'Не удалось измерить свободное место.'
    if bounded 120 pm trim-caches 1G internal > "$STATE/command.tmp" 2>&1; then
        if grep -Eiq 'error|failure|exception|denied|not found|unknown' "$STATE/command.tmp"; then
            cat "$STATE/command.tmp"; say 'Android сообщил об ошибке очистки.' >&2; return 1
        fi
        cat "$STATE/command.tmp"
        say 'Запрос завершён. Фактический эффект зависит от системы; сравните свободное место.'
        bounded 10 df -h /data || say 'Не удалось измерить свободное место после запроса.'
    else
        cat "$STATE/command.tmp"
        say 'Очистка не завершена / не поддерживается / истёк лимит 120 с. Уже удалённый кэш не восстановить.' >&2
        return 1
    fi
}
# Strictly accept Java-style package names; never interpolate them into shell code.
valid_package() {
    pname=$1
    [ "${#pname}" -le 255 ] || return 1
    case "$pname" in *.*) ;; *) return 1 ;; esac
    while :; do
        case "$pname" in *.*) part=${pname%%.*}; pname=${pname#*.} ;; *) part=$pname; pname= ;; esac
        case "$part" in ''|[!a-zA-Z]*|*[!a-zA-Z0-9_]*) return 1 ;; esac
        [ -n "$pname" ] || break
    done
    case "$1" in *.) return 1 ;; esac
}
require_shell() {
    [ "$BASIC" = 0 ] || { say 'Недоступно без Shizuku/rish или ADB. Настройки не менялись.' >&2; return 1; }
}
current_user() {
    cu=$(bounded 5 am get-current-user) || return 1
    case "$cu" in ''|*[!0-9]*) return 1 ;; esac
    printf '%s\n' "$cu"
}
thermal_status() {
    bounded 3 dumpsys thermalservice > "$STATE/command.tmp" 2>/dev/null || return 1
    awk '/^[[:space:]]*IsStatusOverride: false[[:space:]]*$/ { real=1 }
        /^[[:space:]]*HAL Ready: true[[:space:]]*$/ { ready=1 }
        /^[[:space:]]*Thermal Status: [0-6][[:space:]]*$/ { n++; v=$3 }
        END { if (n == 1 && real && ready) print v; else exit 1 }' "$STATE/command.tmp"
}
battery_temperature() {
    bounded 3 dumpsys battery > "$STATE/command.tmp" 2>/dev/null || return 1
    awk '/UPDATES STOPPED/ { fake=1 }
        /^[[:space:]]*temperature:/ && $2 ~ /^-?[0-9]+$/ { n++; v=$2 }
        END { if (n == 1 && !fake && v >= -500 && v <= 1000) printf "%.1f\n", v/10; else exit 1 }' "$STATE/command.tmp"
}
game_report() {
    require_shell || return 1
    REPORT=$(mktemp "$STATE/game-report-XXXXXX") || return 1
    {
        say "ANDROID CARE · игра: $PACKAGE"; date
        say 'Только чтение. Доступность Game Manager зависит от Android, прошивки и категории приложения.'
    } > "$REPORT"
    section 'Поддерживаемые режимы и текущий выбор' cmd game list-modes "$PACKAGE"
    section 'Существующие вмешательства Android (НЕ создаются скриптом)' cmd game list-configs "$PACKAGE"
    section 'Бюджет кадров: справка' sh -c 'printf "60 Гц: 16.67 мс; 90 Гц: 11.11 мс; 120 Гц: 8.33 мс; 144 Гц: 6.94 мс.\nГц не равны фактическому FPS. Производительность надо измерять в самой игре.\n"'
    say 'Performance может увеличить расход и нагрев; эффект определяется игрой и системой.' >> "$REPORT"
    cat "$REPORT"; say "Отчёт сохранён: $REPORT"
}
frames_report() {
    require_shell || return 1
    REPORT=$(mktemp "$STATE/frames-report-XXXXXX") || return 1
    {
        say "ANDROID CARE · HWUI-статистика: $PACKAGE"; date
        say 'Это накопленные данные Android HWUI, НЕ замер FPS игры. Счётчики не сбрасываются.'
        say 'OpenGL/Vulkan/SurfaceView могут не отражаться; меню игры не представляет её игровой рендер.'
    } > "$REPORT"
    if bounded 15 dumpsys gfxinfo "$PACKAGE" > "$STATE/command.tmp" 2>&1; then
        if grep -Eiq 'error|exception|denied|not found|no process' "$STATE/command.tmp"; then
            say 'Статистика недоступна. Приложение может быть не запущено или доступ запрещён.' >> "$REPORT"
        elif grep -Eq '^[[:space:]]*Total frames rendered: [1-9][0-9]*' "$STATE/command.tmp"; then
            grep -E '^[[:space:]]*(Stats since:|Total frames rendered:|Janky frames|50th percentile:|90th percentile:|95th percentile:|99th percentile:|50th gpu percentile:|90th gpu percentile:|95th gpu percentile:|99th gpu percentile:|Number (Missed Vsync|High input latency|Slow UI thread|Slow bitmap uploads|Slow issue draw commands|Frame deadline missed))' "$STATE/command.tmp" >> "$REPORT"
        else
            say 'Нет ненулевой HWUI-выборки. Это НЕ 0 FPS и не доказательство отсутствия лагов.' >> "$REPORT"
        fi
    else say 'gfxinfo недоступен / таймаут. FPS и плавность не оценены.' >> "$REPORT"; fi
    say 'Для сравнения: одинаковый сценарий и настройки; несколько прогонов после прогрева. Для глубокой причины — Perfetto.' >> "$REPORT"
    cat "$REPORT"; say "Отчёт сохранён: $REPORT"
}
# Read the current user's AOSP text format. Unknown/OEM formats fail closed for writes.
read_game() {
    GAME_USER=$(current_user) || return 1
    bounded 10 cmd game list-modes "$1" > "$STATE/command.tmp" 2>&1 || return 1
    parsed=$(awk -v pkg="$1" '
        { prefix=pkg " current mode: "
          if (index($0,prefix) != 1) { bad=1; next }
          rest=substr($0,length(prefix)+1)
          if (split(rest,a,", available game modes: ") != 2) { bad=1; next }
          if (a[1] !~ /^(standard|performance|battery|custom)$/) { bad=1; next }
          if (a[2] !~ /^\[.*\]$/) { bad=1; next }
          modes=substr(a[2],2,length(a[2])-2); gsub(/ /,"",modes)
          if (modes !~ /^(standard|performance|battery|custom|unsupported)(,(standard|performance|battery|custom|unsupported))*$/) { bad=1; next }
          result=a[1] "|" modes
        }
        END { if (bad || NR != 1) exit 1; print result }
    ' "$STATE/command.tmp") || return 1
    [ "$(current_user)" = "$GAME_USER" ] || return 1
    GAME_CURRENT=${parsed%%|*}
    GAME_AVAILABLE=${parsed#*|}
}
valid_game_snapshot() {
    [ -f "$STATE/game.tsv" ] || return 1
    awk -F '\t' 'NF != 3 || $1 !~ /^[0-9]+$/ || $2 !~ /^[a-zA-Z][a-zA-Z0-9_]*([.][a-zA-Z][a-zA-Z0-9_]*)+$/ || length($2)>255 || $3 !~ /^(standard|performance|battery|custom)$/ { bad=1 }
        END { exit (bad || NR != 1) }' "$STATE/game.tsv"
}
write_game_mode() {
    require_shell || return 1
    [ "$(current_user)" = "$3" ] || return 1
    bounded 10 cmd game mode --user "$3" "$1" "$2" > "$STATE/command.tmp" 2>&1 || return 1
    read_game "$2" || return 1
    [ "$GAME_CURRENT" = "$1" ] && [ "$GAME_USER" = "$3" ]
}
restore_game_values() {
    valid_game_snapshot || { say 'Снимок Game Mode отсутствует или повреждён.' >&2; return 1; }
    IFS="$(printf '\t')" read -r saved_user saved_package saved_mode < "$STATE/game.tsv" || return 1
    if ! write_game_mode "$saved_mode" "$saved_package" "$saved_user"; then
        say 'Откат Game Mode не подтверждён. Переключитесь на исходного пользователя, проверьте доступ и повторите.' >&2
        return 1
    fi
    rm -f "$STATE/game.tsv" || return 1
    GAMING=0
    say "Восстановлено: $saved_package, пользователь $saved_user, режим $saved_mode."
}
game_boost() {
    say "План: выбрать штатный Game Mode performance для $PACKAGE, только если он поддерживается."
    say 'Не создаёт device_config overrides и не меняет термозащиту. Возможен рост расхода и нагрева.'
    say 'Выбранный режим может задействовать существующие настройки FPS/масштабирования Android или игры.'
    [ "$APPLY" = 1 ] || { say 'Ничего не изменено. Для применения: game-boost PACKAGE --apply.'; return 0; }
    require_shell || return 1
    [ ! -e "$STATE/game.tsv" ] || { say 'Есть снимок Game Mode: сначала game-restore --apply.' >&2; return 1; }
    read_game "$PACKAGE" || { say 'Нет совместимого Game Manager / игра не поддерживается / неизвестный формат ответа.' >&2; return 1; }
    case ",$GAME_AVAILABLE," in *,performance,*) ;; *) say 'Performance не поддерживается. Принудительные overrides не создаются.' >&2; return 1 ;; esac
    say "Пользователь $GAME_USER, текущий режим: $GAME_CURRENT; доступно: $GAME_AVAILABLE."
    [ "$GAME_CURRENT" != performance ] || { say 'Уже performance. Ничего не изменено.'; return 0; }
    plan_user=$GAME_USER
    confirm || { say 'Отменено.'; return 0; }
    # Re-read after user interaction, then journal before the first write.
    read_game "$PACKAGE" || return 1
    [ "$GAME_USER" = "$plan_user" ] || { say 'Пользователь изменился после подтверждения. Запустите действие заново.' >&2; return 1; }
    case ",$GAME_AVAILABLE," in *,performance,*) ;; *) return 1 ;; esac
    heat=$(thermal_status) || { say 'Нет надёжного Thermal Status. Усиление нагрузки не применяется.' >&2; return 1; }
    case "$heat" in 0|1) ;; *) say 'Есть умеренное или более сильное тепловое ограничение. Дайте устройству остыть.' >&2; return 1 ;; esac
    temp=$(battery_temperature) || { say 'Температура батареи недоступна. Performance не применяется.' >&2; return 1; }
    awk -v t="$temp" 'BEGIN { exit (t >= 40) }' || { say 'Батарея 40 °C или выше: performance не применяется.' >&2; return 1; }
    printf '%s\t%s\t%s\n' "$GAME_USER" "$PACKAGE" "$GAME_CURRENT" > "$STATE/game.tmp" || return 1
    mv "$STATE/game.tmp" "$STATE/game.tsv" || return 1
    valid_game_snapshot || return 1
    boost_user=$GAME_USER
    GAMING=1
    if ! write_game_mode performance "$PACKAGE" "$boost_user"; then
        say 'Применение Game Mode не подтверждено. Пробую восстановить исходный режим…' >&2
        restore_game_values || :
        return 1
    fi
    GAMING=0
    say 'Выбран штатный performance. Это не гарантия прироста: сравните одинаковые игровые сценарии.'
    say 'Для отката: game-restore --apply. Скрипт не остаётся работать в фоне.'
}
game_restore() {
    valid_game_snapshot || { say 'Нет корректного снимка Game Mode.' >&2; return 1; }
    say 'Будет восстановлен режим из снимка (пользователь / пакет / режим):'; cat "$STATE/game.tsv"
    [ "$APPLY" = 1 ] || { say 'Ничего не изменено. Для отката: game-restore --apply.'; return 0; }
    require_shell || return 1
    confirm || { say 'Отменено.'; return 0; }
    GAMING=1
    restore_game_values
}
cpu_counters() {
    # guest time is already part of user/nice: sum only the first eight counters.
    awk '$1=="cpu" { for(i=2;i<=9;i++) total+=$i; printf "%.0f %.0f\n",total,$5+$6; found=1; exit }
        END { if(!found) exit 1 }' /proc/stat 2>/dev/null
}
monitor_line() { say "$1"; say "$1" >> "$REPORT"; }
monitor() {
    REPORT=$(mktemp "$STATE/monitor-XXXXXX") || return 1
    say "Наблюдение: $SAMPLES отсчётов, пауза 5 с между ними плюс время чтения датчиков. Не стресс-тест."
    say "CSV: $REPORT; NA = недоступно. Датчики не отключаются; нагрузка самих измерений ненулевая."
    monitor_line 'epoch_s,cpu_busy_pct,mem_available_kib,battery_c,thermal_status'
    i=1; previous_total=; previous_idle=
    while [ "$i" -le "$SAMPLES" ]; do
        busy=NA
        counters=$(cpu_counters) || counters=
        if [ -n "$counters" ]; then
            total=${counters% *}; idle=${counters#* }
            if [ -n "$previous_total" ]; then
                busy=$(awk -v t="$total" -v p="$previous_total" -v d="$idle" -v q="$previous_idle" 'BEGIN { dt=t-p; di=d-q; if(dt>0 && di>=0 && di<=dt) printf "%.1f",100*(dt-di)/dt; else printf "NA" }')
            fi
            previous_total=$total; previous_idle=$idle
        else previous_total=; previous_idle=; fi
        memory=$(awk '/^MemAvailable:/ && $2 ~ /^[0-9]+$/ { print $2; f=1; exit } END { if(!f) print "NA" }' /proc/meminfo 2>/dev/null) || memory=NA
        bt=NA; ts=NA
        if [ "$BASIC" = 0 ]; then
            bt=$(battery_temperature) || bt=NA
            ts=$(thermal_status) || ts=NA
        fi
        monitor_line "$(date +%s),$busy,$memory,$bt,$ts"
        i=$((i + 1))
        if [ "$i" -le "$SAMPLES" ]; then sleep 5; fi
    done
    say 'Нагрузка CPU — агрегат по всем ядрам между отсчётами, не CPU игры и не FPS.'
    say 'Thermal Status 0–6 — шкала Android, не температура. Для сравнения нужны одинаковые условия.'
    say "Мониторинг сохранён: $REPORT"
}

menu() {
    while :; do
        say ''; say 'ANDROID CARE'
        if [ "$BASIC" = 1 ]; then say 'Базовый режим: только диагностика; изменения требуют shell-доступа.'; fi
        say '1 — Проверить устройство'; say '2 — Анимации 0.5x (с подтверждением)'
        say '3 — Восстановить анимации (с подтверждением)'; say '4 — Очистить кэш (с подтверждением)'
        say '5 — Проверить игру'; say '6 — HWUI-кадры приложения'; say '7 — Мониторинг (6 отсчётов)'
        say '8 — Штатный игровой performance (с подтверждением)'; say '9 — Откат Game Mode (с подтверждением)'; say '0 — Выход'
        IFS= read -r choice || return 0
        if [ "$BASIC" = 1 ]; then
            case "$choice" in 2|3|4|5|6|8|9) say 'Недоступно без Shizuku/rish или ADB. Настройки не менялись.'; continue ;; esac
        fi
        case "$choice" in
            5|6|8)
                say 'Введите имя пакета, например com.example.game:'
                IFS= read -r PACKAGE || return 0
                valid_package "$PACKAGE" || { say 'Некорректное имя пакета.'; continue; } ;;
        esac
        APPLY=1
        case "$choice" in
            1) audit ;; 2) optimize ;; 3) if valid_snapshot; then restore; else say 'Нет корректного снимка.'; fi ;;
            4) trim_cache ;; 5) game_report ;; 6) frames_report ;; 7) SAMPLES=6; monitor ;;
            8) game_boost ;; 9) game_restore ;; 0) return 0 ;; *) say 'Выберите 0–9.' ;;
        esac
        # Stop on incomplete transactions; never allow a new snapshot over one.
        [ "$CHANGING" = 0 ] && [ "$GAMING" = 0 ] || return 1
    done
}
main() {
    ACTION=${1:-audit}
    APPLY=0
    PACKAGE=
    SAMPLES=6
    case "$ACTION" in
        help|--help|-h) [ "$#" -le 1 ] || { usage; exit 2; }; usage; return 0 ;;
        audit|menu) [ "$#" -le 1 ] || { usage; exit 2; } ;;
        monitor)
            [ "$#" -le 2 ] || { usage; exit 2; }
            SAMPLES=${2-6}
            case "$SAMPLES" in 1|2|3|4|5|6|7|8|9|10|11|12) ;; *) usage; exit 2 ;; esac ;;
        game|frames|game-boost)
            [ "$#" -ge 2 ] && [ "$#" -le 3 ] || { usage; exit 2; }
            PACKAGE=$2
            valid_package "$PACKAGE" || { say 'Некорректное имя пакета.' >&2; exit 2; }
            if [ "$#" -eq 3 ]; then
                [ "$ACTION" = game-boost ] && [ "$3" = --apply ] || { usage; exit 2; }
                APPLY=1
            fi ;;
        optimize|restore|trim|game-restore)
            [ "$#" -le 2 ] || { usage; exit 2; }
            if [ "$#" -eq 2 ]; then
                [ "$2" = --apply ] || { usage; exit 2; }
                APPLY=1
            fi ;;
        *) usage; exit 2 ;;
    esac
    init
    case "$ACTION" in
        audit) audit ;; menu) menu ;; optimize) optimize ;; restore) restore ;; trim) trim_cache ;;
        game) game_report ;; frames) frames_report ;; monitor) monitor ;;
        game-boost) game_boost ;; game-restore) game_restore ;;
    esac
}
main "$@"
