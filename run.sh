#!/bin/sh
# One entry point: existing shell privileges, Shizuku/rish, or a read-only local audit.
set -u
ROOT=$(CDPATH='' cd -- "$(dirname -- "$0")" && pwd) || exit 1
if [ "${1:-}" = help ] || [ "${1:-}" = --help ] || [ "${1:-}" = -h ]; then
    exec sh "$ROOT/scripts/android-care.sh" "$@"
fi
# Already launched from an ADB/Shizuku shell? No second bridge is needed.
case "$(id -u)" in
    2000|0) exec sh "$ROOT/scripts/android-care.sh" "$@" ;;
esac
if [ -n "${RISH:-}" ]; then
    RISH_BIN=$RISH
    if [ ! -f "$RISH_BIN" ]; then
        printf '%s\n' 'Указанный RISH не найден. Исправьте путь или уберите переменную RISH.' >&2
        exit 1
    fi
elif [ -f "$ROOT/rish/rish" ]; then
    RISH_BIN=$ROOT/rish/rish
else
    RISH_BIN=$(command -v rish 2>/dev/null || :)
fi
if [ -z "$RISH_BIN" ] || [ ! -f "$RISH_BIN" ]; then
    printf '%s\n' 'Shizuku/rish не найден: запускаю базовый режим без повышенных прав.'
    printf '%s\n' 'Установка дополнительных компонентов не нужна. Изменение системы в этом режиме недоступно.'
    exec sh "$ROOT/scripts/android-care.sh" "$@"
fi
PAYLOAD=$(cat "$ROOT/scripts/android-care.sh") || exit 1
# Command string rather than stdin: menus and confirmations must keep interactive input.
export RISH_PRESERVE_ENV=0
sh "$RISH_BIN" -c "$PAYLOAD" android-care "$@"
result=$?
# A failed bridge must not prevent a default/read-only diagnostic from running locally.
# Never retry writes or a menu: a privileged action may already have partially completed.
if [ "$result" -ne 0 ] && [ "$#" -le 1 ]; then
    case "${1:-audit}" in
        audit)
            printf '%s\n' 'Проверка через rish не завершилась. Перехожу к локальной диагностике без изменений.' >&2
            exec sh "$ROOT/scripts/android-care.sh" audit ;;
    esac
fi
exit "$result"
