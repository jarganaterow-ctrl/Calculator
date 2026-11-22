#!/system/bin/sh
# Android Care — read-only app mode, or full mode via Shizuku/rish / ADB.
# No root, downloads, telemetry, eval, package removal or background services.
set -u
PATH=/system/bin:/system/xbin:/vendor/bin
export PATH
umask 077
STATE=/data/local/tmp/android-care
KEYS='window_animation_scale transition_animation_scale animator_duration_scale'
LOCKED=0
CHANGING=0
BASIC=0
REPORT=

say() { printf '%s\n' "$*"; }
fail() { say "ОШИБКА: $*" >&2; exit 1; }
usage() {
    while IFS= read -r line; do printf '%s\n' "$line"; done <<'HELP'
Android Care
  audit                 Диагностика (по умолчанию); настройки не меняются
  menu                  Интерактивное меню
  optimize              План ускорения анимаций, без изменений
  optimize --apply      Сохранить исходные значения и поставить анимации 0.5x
  restore               План отката
  restore --apply       Восстановить исходные анимации
  trim                  План очистки восстанавливаемого кэша
  trim --apply          Попросить Android освободить место до цели 1 GiB
  help                  Эта справка
Изменения требуют отдельного ввода ДА. --apply сам по себе не подтверждение.
HELP
}
cleanup() {
    if [ "$CHANGING" = 1 ]; then
        say "Изменение прервано. Снимок сохранён: $STATE/animations.tsv" >&2
        say 'Выполните restore --apply для восстановления.' >&2
    fi
    if [ "$LOCKED" = 1 ]; then
        rm -f "$STATE/command.tmp" "$STATE/command.tmp.packages" "$STATE/animations.tmp"
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
    for file in animations.tsv animations.tmp command.tmp command.tmp.packages; do
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
    elif bounded 10 dumpsys battery > "$STATE/command.tmp" 2>/dev/null; then
        awk '
            /^[[:space:]]*temperature:/ && $2 ~ /^-?[0-9]+$/ {
                found=1; t=$2/10
                printf "Температура батареи: %.1f °C.\n", t
                if (t >= 40) print "Батарея тёплая/горячая: снизьте нагрузку, особенно при зарядке. 40 °C — ориентир, не диагноз."
            }
            END { if (!found) print "Температура батареи: нет достоверного показания." }
        ' "$STATE/command.tmp"
    else say 'Батарея: оценка температуры недоступна.'; fi
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
    say 'План: window_animation_scale, transition_animation_scale, animator_duration_scale → 0.5x.'
    say 'Это сокращает ожидание анимаций, но не повышает вычислительную мощность. Есть откат.'
    [ "$APPLY" = 1 ] || { say 'Сейчас ничего не изменено. Для применения: optimize --apply.'; return 0; }
    [ ! -e "$STATE/animations.tsv" ] || fail 'Снимок уже есть. Сначала выполните restore --apply; исходные значения не перезаписываются.'
    confirm || { say 'Отменено.'; return 0; }
    snapshot || fail 'Не удалось сохранить корректные исходные значения. Настройки не менялись.'
    CHANGING=1
    for key in $KEYS; do
        if ! write_value "$key" 0.5; then
            say "Ошибка записи/проверки $key. Пробую откат…" >&2
            restore_values || say 'Откат неполный. Снимок сохранён, повторите restore --apply.' >&2
            return 1
        fi
    done
    CHANGING=0
    say "Готово: анимации 0.5x. Исходные значения: $STATE/animations.tsv"
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
menu() {
    while :; do
        say ''; say 'ANDROID CARE'
        if [ "$BASIC" = 1 ]; then say 'Базовый режим: только диагностика; изменения требуют shell-доступа.'; fi
        say '1 — Проверить устройство'; say '2 — Анимации 0.5x (с подтверждением)'
        say '3 — Восстановить анимации (с подтверждением)'; say '4 — Очистить кэш (с подтверждением)'; say '0 — Выход'
        IFS= read -r choice || return 0
        if [ "$BASIC" = 1 ]; then
            case "$choice" in 2|3|4) say 'Недоступно без Shizuku/rish или ADB. Настройки не менялись.'; continue ;; esac
        fi
        APPLY=1
        case "$choice" in
            1) audit ;; 2) optimize ;; 3) if valid_snapshot; then restore; else say 'Нет корректного снимка.'; fi ;;
            4) trim_cache ;; 0) return 0 ;; *) say 'Выберите 0–4.' ;;
        esac
        # Stop on incomplete transactions; never allow a new snapshot over one.
        [ "$CHANGING" = 0 ] || return 1
    done
}
main() {
    ACTION=${1:-audit}
    APPLY=0
    [ "$#" -le 2 ] || { usage; exit 2; }
    if [ "$#" -eq 2 ]; then
        [ "$2" = --apply ] || { usage; exit 2; }
        case "$ACTION" in optimize|restore|trim) APPLY=1 ;; *) usage; exit 2 ;; esac
    fi
    case "$ACTION" in
        help|--help|-h) usage; return 0 ;;
        audit|menu|optimize|restore|trim) ;; *) usage; exit 2 ;;
    esac
    init
    case "$ACTION" in audit) audit ;; menu) menu ;; optimize) optimize ;; restore) restore ;; trim) trim_cache ;; esac
}
main "$@"
