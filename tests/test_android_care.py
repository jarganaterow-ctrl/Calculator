"""Host-only tests. Android services are mocked; this is not a device certification."""
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
KEYS = ["window_animation_scale", "transition_animation_scale", "animator_duration_scale"]
MOCK = r'''#!/usr/bin/env python3
import json, os, sys
from pathlib import Path
name = Path(sys.argv[0]).name
args = sys.argv[1:]
base = Path(os.environ['MOCK_HOME'])
with (base / 'calls').open('a') as f:
    f.write(json.dumps([name] + args) + '\n')
if name == 'id':
    print(os.environ.get('MOCK_UID', '2000'))
elif name == 'stat':
    print(os.environ.get('MOCK_UID', '2000'))
elif name == 'getprop':
    print('35' if args[0] == 'ro.build.version.sdk' else 'Mock Android')
elif name == 'sleep':
    pass
elif name == 'am':
    count_file = base / 'user-reads'
    count = int(count_file.read_text()) + 1 if count_file.exists() else 1
    count_file.write_text(str(count))
    if os.environ.get('MOCK_SWITCH_USER_AT') and count >= int(os.environ['MOCK_SWITCH_USER_AT']):
        print('10'); sys.exit(0)
    switch = base / 'active-user'
    print(switch.read_text() if switch.exists() else os.environ.get('MOCK_USER', '0'))
elif name == 'cmd':
    assert args[0] == 'game'
    db = base / 'game.json'
    mode = json.loads(db.read_text())['mode']
    if os.environ.get('MOCK_NO_GAME_SERVICE'):
        print('Unknown command: ' + args[1]); sys.exit(1)
    if args[1] == 'list-modes':
        if os.environ.get('MOCK_GAME_UNKNOWN_FORMAT'):
            print('OEM game state unknown'); sys.exit(0)
        available = os.environ.get('MOCK_GAME_MODES', 'standard,performance,battery,custom')
        print(args[2] + ' current mode: ' + mode + ', available game modes: [' + available + ']')
    elif args[1] == 'list-configs':
        print(args[2] + ' interventions: none')
    elif args[1] == 'mode':
        assert args[2] == '--user'
        target = args[4]
        if target == 'performance' and os.environ.get('MOCK_GAME_FAIL'):
            sys.exit(1)
        if target == 'performance' and os.environ.get('MOCK_GAME_SILENT_FAIL'):
            print('Error: permission denied'); sys.exit(0)
        if target != 'performance' and os.environ.get('MOCK_GAME_RESTORE_FAIL'):
            sys.exit(1)
        db.write_text(json.dumps({'mode': target}))
        if os.environ.get('MOCK_SWITCH_AFTER_WRITE'):
            (base / 'active-user').write_text('10')
        print('Set game mode to ' + target)
    else: sys.exit(2)
elif name == 'dumpsys':
    if args[0] == os.environ.get('MOCK_DUMP_FAIL'):
        print('Permission Denial'); sys.exit(1)
    if args[0] == 'thermalservice':
        print('IsStatusOverride: ' + os.environ.get('MOCK_THERMAL_OVERRIDE', 'false'))
        print('HAL Ready: ' + os.environ.get('MOCK_HAL_READY', 'true'))
        print('Thermal Status: ' + os.environ.get('MOCK_THERMAL', '0'))
    elif args[0] == 'gfxinfo':
        frames = os.environ.get('MOCK_FRAMES', '100')
        print('Applications Graphics Acceleration Info: private.process.name')
        print('Stats since: 123456789ns')
        print('Total frames rendered: ' + frames)
        print('Janky frames: 4 (4.00%)')
        print('50th percentile: 5ms\n90th percentile: 10ms\n95th percentile: 15ms\n99th percentile: 20ms')
        print('50th gpu percentile: 4ms\n99th gpu percentile: 9ms')
        print('Profile data in ms: private.activity.name')
    elif args[0] == 'deviceidle':
        print('  mState=ACTIVE\n  Whitelist user apps: private.example.app')
    elif args[0] == 'display':
        print('  mSupportedModes=[{id=1, refreshRate=120.0}]\n  uniqueId=private-display-id')
    elif args[0] == 'diskstats':
        print('Data-Free: 1234K / 9999K total = 12% free\nPackage Names: [private.example.app]')
    else:
        if os.environ.get('MOCK_BATTERY_SIMULATED'): print("(UPDATES STOPPED -- use 'reset' to restart)")
        print('level: 75\ntemperature: ' + os.environ.get('MOCK_TEMP', '320') + '\nstatus: mock')
elif name == 'settings':
    db = base / 'settings.json'
    data = json.loads(db.read_text())
    action, namespace, key = args[:3]
    assert namespace == 'global'
    if action == 'get':
        print(data.get(key, 'null'))
    else:
        if key == os.environ.get('MOCK_RESTORE_FAIL') and (action == 'delete' or args[3] != '0.5'):
            sys.exit(1)
        if key == os.environ.get('MOCK_FAIL') and action == 'put' and args[3] == '0.5':
            sys.exit(1)
        if key == os.environ.get('MOCK_SILENT_FAIL') and action == 'put' and args[3] == '0.5':
            print('Error: write denied'); sys.exit(0)
        if action == 'delete': data.pop(key, None)
        elif action == 'put': data[key] = args[3]
        else: sys.exit(2)
        db.write_text(json.dumps(data))
elif name == 'pm':
    if args[0] == 'trim-caches':
        if os.environ.get('MOCK_TRIM_FAIL'): print('Error: unsupported')
    elif args[:2] == ['list', 'packages']:
        print('package:private.example.app')
    else: sys.exit(2)
else:
    sys.exit(2)
'''


class CareTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.base = Path(self.tmp.name)
        self.bin = self.base / 'bin'
        self.bin.mkdir()
        self.state = self.base / 'state'
        self.initial = dict(zip(KEYS, ['1.0', 'null', '1.25']))
        (self.base / 'settings.json').write_text(json.dumps(self.initial))
        (self.base / 'game.json').write_text(json.dumps({'mode': 'standard'}))
        self.home = self.base / 'home'
        self.home.mkdir()
        self.env = dict(os.environ, MOCK_HOME=str(self.base), HOME=str(self.home))
        mock = self.bin / 'android-mock'
        mock.write_text(MOCK)
        mock.chmod(0o755)
        for name in ['id', 'stat', 'getprop', 'dumpsys', 'settings', 'pm', 'am', 'cmd', 'sleep']:
            (self.bin / name).symlink_to(mock)
        # Only relocate the production constants in an isolated copy.
        # Production contains no environment-based privilege/test bypass.
        self.script = self.base / 'scripts/android-care.sh'
        self.script.parent.mkdir()
        content = (ROOT / 'scripts/android-care.sh').read_text()
        content = content.replace('STATE=/data/local/tmp/android-care', f'STATE={self.state}')
        content = content.replace('PATH=/system/bin:/system/xbin:/vendor/bin', f'PATH={self.bin}:/usr/bin:/bin')
        self.script.write_text(content)

    def run_care(self, *args, stdin='', **env):
        return subprocess.run(['sh', str(self.script), *args], input=stdin, text=True,
                              capture_output=True, env=dict(self.env, **env), timeout=60)

    def calls(self):
        path = self.base / 'calls'
        return [json.loads(line) for line in path.read_text().splitlines()] if path.exists() else []

    def writes(self):
        return [c for c in self.calls() if c[0] == 'settings' and c[1] in ('put', 'delete')]

    def database(self):
        return json.loads((self.base / 'settings.json').read_text())

    def test_help_no_android_required(self):
        p = self.run_care('help')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertEqual(self.calls(), [])

    def test_original_launcher_help_works_without_android_path(self):
        p = subprocess.run(['sh', str(ROOT / 'run.sh'), 'help'],
                           text=True, capture_output=True, timeout=10)
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertIn('Android Care', p.stdout)
        self.assertEqual(p.stderr, '')

    def test_default_audit_is_read_only_and_private(self):
        p = self.run_care()
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertEqual(self.writes(), [])
        report = next(self.state.glob('report-*'))
        text = report.read_text()
        self.assertIn('Оперативная память', text)
        self.assertIn('Как действовать', text)
        self.assertNotIn('private.example.app', text)
        self.assertNotIn('private-display-id', text)
        self.assertIn('refreshRate=120.0', text)
        self.assertIn('НЕ FPS игры', text)
        self.assertEqual(report.stat().st_mode & 0o777, 0o600)
        self.assertFalse((self.state / 'lock').exists())

    def test_unavailable_diagnostic_is_reported_not_fatal(self):
        p = self.run_care('audit', MOCK_DUMP_FAIL='thermalservice')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertIn('Недоступно / ошибка / таймаут', p.stdout)
        self.assertIn('Как действовать', p.stdout)

    def test_plans_do_not_write_or_trim(self):
        for action in ['optimize', 'trim']:
            p = self.run_care(action)
            self.assertEqual(p.returncode, 0, p.stderr)
        self.assertEqual(self.writes(), [])
        self.assertFalse(any(c[:2] == ['pm', 'trim-caches'] for c in self.calls()))
        self.assertFalse((self.state / 'animations.tsv').exists())

    def test_confirmation_and_eof_cancel(self):
        for stdin in ['', 'yes\n', 'нет\n']:
            p = self.run_care('optimize', '--apply', stdin=stdin)
            self.assertEqual(p.returncode, 0, p.stderr)
        self.assertEqual(self.writes(), [])

    def test_optimize_and_restore_preserve_original_and_absent_keys(self):
        p = self.run_care('optimize', '--apply', stdin='ДА\n')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertTrue(all(self.database()[key] == '0.5' for key in KEYS))
        backup = (self.state / 'animations.tsv').read_text()
        p = self.run_care('optimize', '--apply', stdin='ДА\n')
        self.assertNotEqual(p.returncode, 0)
        self.assertEqual((self.state / 'animations.tsv').read_text(), backup)
        p = self.run_care('restore')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertTrue(all(self.database()[key] == '0.5' for key in KEYS))
        p = self.run_care('restore', '--apply', stdin='ДА\n')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertEqual(self.database(), {k: v for k, v in self.initial.items() if v != 'null'})
        self.assertFalse((self.state / 'animations.tsv').exists())

    def test_failed_or_silently_denied_write_rolls_back(self):
        for variable in ['MOCK_FAIL', 'MOCK_SILENT_FAIL']:
            p = self.run_care('optimize', '--apply', stdin='ДА\n', **{variable: KEYS[1]})
            self.assertNotEqual(p.returncode, 0)
            self.assertEqual(self.database(), {k: v for k, v in self.initial.items() if v != 'null'})
            self.assertFalse((self.state / 'animations.tsv').exists())

    def test_reject_corrupt_duplicate_and_executable_backups(self):
        self.state.mkdir()
        variants = [
            'window_animation_scale\t$(touch /tmp/should-not-run)\n',
            'window_animation_scale\t1\n' * 3,
            'not_a_setting\t1\n' + ''.join(f'{k}\t1\n' for k in KEYS[1:]),
            ''.join(f'{k}\t1\textra\n' for k in KEYS),
        ]
        for content in variants:
            (self.state / 'animations.tsv').write_text(content)
            p = self.run_care('restore', '--apply', stdin='ДА\n')
            self.assertNotEqual(p.returncode, 0)
        self.assertEqual(self.writes(), [])

    def test_no_snapshot_on_unreadable_setting(self):
        db = self.initial | {KEYS[1]: 'Permission Denial'}
        (self.base / 'settings.json').write_text(json.dumps(db))
        p = self.run_care('optimize', '--apply', stdin='ДА\n')
        self.assertNotEqual(p.returncode, 0)
        self.assertEqual(self.writes(), [])
        self.assertFalse((self.state / 'animations.tsv').exists())

    def test_wrong_uid_refused(self):
        p = self.run_care('optimize', '--apply', stdin='ДА\n', MOCK_UID='10234')
        self.assertNotEqual(p.returncode, 0)
        self.assertEqual(self.writes(), [])
        self.assertFalse(self.state.exists())

    def test_lock_and_symlink_refused(self):
        self.state.mkdir()
        (self.state / 'lock').mkdir()
        p = self.run_care('audit')
        self.assertNotEqual(p.returncode, 0)
        self.assertTrue((self.state / 'lock').exists())
        (self.state / 'lock').rmdir()
        (self.state / 'animations.tsv').symlink_to(self.base / 'settings.json')
        p = self.run_care('optimize', '--apply', stdin='ДА\n')
        self.assertNotEqual(p.returncode, 0)
        self.assertEqual(self.writes(), [])
        self.assertFalse((self.state / 'lock').exists())

    def test_trim_requires_separate_consent_and_detects_zero_exit_error(self):
        p = self.run_care('trim', '--apply', stdin='нет\n')
        self.assertEqual(p.returncode, 0)
        self.assertFalse(any(c[:2] == ['pm', 'trim-caches'] for c in self.calls()))
        p = self.run_care('trim', '--apply', stdin='ДА\n', MOCK_TRIM_FAIL='1')
        self.assertNotEqual(p.returncode, 0)
        p = self.run_care('trim', '--apply', stdin='ДА\n')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertIn(['pm', 'trim-caches', '1G', 'internal'], self.calls())
        self.assertEqual(self.writes(), [])

    def test_incomplete_restore_retains_snapshot_for_retry(self):
        p = self.run_care('optimize', '--apply', stdin='ДА\n')
        self.assertEqual(p.returncode, 0, p.stderr)
        p = self.run_care('restore', '--apply', stdin='ДА\n', MOCK_RESTORE_FAIL=KEYS[0])
        self.assertNotEqual(p.returncode, 0)
        self.assertTrue((self.state / 'animations.tsv').exists())
        self.assertFalse((self.state / 'lock').exists())
        p = self.run_care('restore', '--apply', stdin='ДА\n')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertFalse((self.state / 'animations.tsv').exists())

    def test_command_timeouts_are_not_health_passes(self):
        timeout = self.bin / 'timeout'
        timeout.write_text('#!/bin/sh\nshift\n[ "$1" != dumpsys ] || exit 124\nexec "$@"\n')
        timeout.chmod(0o755)
        p = self.run_care('audit')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertIn('код 124', p.stdout)
        self.assertIn('Батарея: оценка температуры недоступна.', p.stdout)
        self.assertEqual(self.writes(), [])

    def test_hot_battery_warning_and_unique_reports(self):
        for _ in range(2):
            p = self.run_care('audit', MOCK_TEMP='430')
            self.assertEqual(p.returncode, 0, p.stderr)
            self.assertIn('43.0 °C', p.stdout)
            self.assertIn('Батарея тёплая/горячая', p.stdout)
        self.assertEqual(len(list(self.state.glob('report-*'))), 2)

    def test_invalid_arguments(self):
        for args in [('audit', '--apply'), ('optimize', '--yes'), ('unknown',), ('trim', '--apply', '--yes')]:
            p = self.run_care(*args)
            self.assertEqual(p.returncode, 2, p.stderr)
        self.assertEqual(self.calls(), [])

    def test_menu_audit_optimize_restore_exit(self):
        p = self.run_care('menu', stdin='1\n2\nДА\n3\nДА\n0\n')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertTrue(list(self.state.glob('report-*')))
        self.assertFalse((self.state / 'animations.tsv').exists())
        self.assertEqual(self.database(), {k: v for k, v in self.initial.items() if v != 'null'})

    def game_writes(self):
        return [c for c in self.calls() if c[:3] == ['cmd', 'game', 'mode']]

    def test_disabled_and_faster_animations_are_never_slowed_down(self):
        original = dict(zip(KEYS, ['0', '0.25', '1']))
        (self.base / 'settings.json').write_text(json.dumps(original))
        p = self.run_care('optimize', '--apply', stdin='ДА\n')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertEqual(self.database(), dict(zip(KEYS, ['0', '0.25', '0.5'])))
        p = self.run_care('restore', '--apply', stdin='ДА\n')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertEqual(self.database(), original)

    def test_game_report_and_frame_summary_are_read_only(self):
        for action in ['game', 'frames']:
            p = self.run_care(action, 'com.example.game')
            self.assertEqual(p.returncode, 0, p.stderr)
            self.assertTrue(list(self.state.glob(action + '-report-*')))
        self.assertIn('99th percentile: 20ms', p.stdout)
        self.assertIn('99th gpu percentile: 9ms', p.stdout)
        self.assertNotIn('private.process.name', p.stdout)
        self.assertNotIn('private.activity.name', p.stdout)
        self.assertIn('НЕ замер FPS', p.stdout)
        self.assertEqual(self.writes(), [])
        self.assertEqual(self.game_writes(), [])

    def test_empty_frames_and_permissions_do_not_claim_zero_fps(self):
        p = self.run_care('frames', 'com.example.game', MOCK_FRAMES='0')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertIn('Нет ненулевой HWUI-выборки', p.stdout)
        p = self.run_care('frames', 'com.example.game', MOCK_DUMP_FAIL='gfxinfo')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertIn('FPS и плавность не оценены', p.stdout)

    def test_bad_packages_and_monitor_lengths_are_rejected_before_android_calls(self):
        for package in ['--help', 'com.x;id', 'com.x$(id)', 'com.x/y', 'com..game', '.com.game', 'com.game.', '1com.game', 'com.1game', 'game', 'com.' + 'x' * 256]:
            for action in ['game', 'frames', 'game-boost']:
                p = self.run_care(action, package)
                self.assertEqual(p.returncode, 2, (action, package, p.stderr))
        for count in ['0', '13', '-1', '100000', '1;id', '--apply', '']:
            p = self.run_care('monitor', count)
            # An explicitly empty duration is invalid, not a default.
            self.assertEqual(p.returncode, 2, (count, p.stderr))
        self.assertEqual(self.calls(), [])

    def test_monitor_csv_is_bounded_and_records_unknown_first_cpu_sample(self):
        p = self.run_care('monitor', '3')
        self.assertEqual(p.returncode, 0, p.stderr)
        report = next(self.state.glob('monitor-*'))
        lines = report.read_text().splitlines()
        self.assertEqual(len(lines), 4)
        self.assertEqual(lines[1].split(',')[1], 'NA')
        self.assertEqual(lines[1].split(',')[3:], ['32.0', '0'])
        self.assertEqual(sum(c[0] == 'sleep' for c in self.calls()), 2)
        self.assertEqual(report.stat().st_mode & 0o777, 0o600)
        self.assertEqual(self.writes(), [])
        self.assertEqual(self.game_writes(), [])

    def test_basic_monitor_never_calls_privileged_services(self):
        p = self.run_care('monitor', '2', MOCK_UID='10234')
        self.assertEqual(p.returncode, 0, p.stderr)
        report = next((self.home / '.android-care').glob('monitor-*'))
        self.assertTrue(all(row.endswith(',NA,NA') for row in report.read_text().splitlines()[1:]))
        self.assertFalse(any(c[0] in ['settings', 'dumpsys', 'pm', 'cmd', 'am'] for c in self.calls()))

    def test_game_boost_plan_and_cancellation_do_not_write(self):
        for args, stdin in [(('game-boost', 'com.example.game'), ''),
                            (('game-boost', 'com.example.game', '--apply'), 'нет\n')]:
            p = self.run_care(*args, stdin=stdin)
            self.assertEqual(p.returncode, 0, p.stderr)
        self.assertEqual(self.game_writes(), [])
        self.assertFalse((self.state / 'game.tsv').exists())

    def test_game_mode_apply_verify_restore_and_preserve_snapshot(self):
        p = self.run_care('game-boost', 'com.example.game', '--apply', stdin='ДА\n')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertEqual(json.loads((self.base / 'game.json').read_text())['mode'], 'performance')
        snap = (self.state / 'game.tsv').read_text()
        self.assertEqual(snap, '0\tcom.example.game\tstandard\n')
        self.assertIn(['cmd', 'game', 'mode', '--user', '0', 'performance', 'com.example.game'], self.game_writes())
        p = self.run_care('game-boost', 'com.other.game', '--apply', stdin='ДА\n')
        self.assertNotEqual(p.returncode, 0)
        self.assertEqual((self.state / 'game.tsv').read_text(), snap)
        before = len(self.game_writes())
        p = self.run_care('game-restore')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertEqual(len(self.game_writes()), before)
        p = self.run_care('game-restore', '--apply', stdin='ДА\n')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertEqual(json.loads((self.base / 'game.json').read_text())['mode'], 'standard')
        self.assertFalse((self.state / 'game.tsv').exists())

    def test_game_mode_unsupported_or_unsafe_conditions_never_write(self):
        variants = [dict(MOCK_NO_GAME_SERVICE='1'), dict(MOCK_GAME_MODES='standard,battery'),
                    dict(MOCK_GAME_UNKNOWN_FORMAT='1'), dict(MOCK_THERMAL='2'),
                    dict(MOCK_THERMAL_OVERRIDE='true'), dict(MOCK_HAL_READY='false'),
                    dict(MOCK_THERMAL='6'), dict(MOCK_THERMAL='unknown'),
                    dict(MOCK_DUMP_FAIL='thermalservice'), dict(MOCK_TEMP='400'),
                    dict(MOCK_TEMP='430'), dict(MOCK_BATTERY_SIMULATED='1'), dict(MOCK_TEMP='unknown'), dict(MOCK_DUMP_FAIL='battery')]
        for env in variants:
            p = self.run_care('game-boost', 'com.example.game', '--apply', stdin='ДА\n', **env)
            self.assertNotEqual(p.returncode, 0, (env, p.stdout, p.stderr))
            self.assertFalse((self.state / 'game.tsv').exists())
        self.assertEqual(self.game_writes(), [])

    def test_game_mode_failed_and_silent_writes_attempt_rollback(self):
        for variable in ['MOCK_GAME_FAIL', 'MOCK_GAME_SILENT_FAIL']:
            p = self.run_care('game-boost', 'com.example.game', '--apply', stdin='ДА\n', **{variable: '1'})
            self.assertNotEqual(p.returncode, 0)
            self.assertFalse((self.state / 'game.tsv').exists())
            self.assertEqual(json.loads((self.base / 'game.json').read_text())['mode'], 'standard')

    def test_game_restore_for_different_user_refuses_then_can_retry(self):
        p = self.run_care('game-boost', 'com.example.game', '--apply', stdin='ДА\n')
        self.assertEqual(p.returncode, 0, p.stderr)
        n = len(self.game_writes())
        p = self.run_care('game-restore', '--apply', stdin='ДА\n', MOCK_USER='10')
        self.assertNotEqual(p.returncode, 0)
        self.assertEqual(len(self.game_writes()), n)
        self.assertTrue((self.state / 'game.tsv').exists())
        p = self.run_care('game-restore', '--apply', stdin='ДА\n')
        self.assertEqual(p.returncode, 0, p.stderr)

    def test_game_user_switch_after_confirmation_never_writes(self):
        p = self.run_care('game-boost', 'com.example.game', '--apply', stdin='ДА\n', MOCK_SWITCH_USER_AT='3')
        self.assertNotEqual(p.returncode, 0)
        self.assertIn('Пользователь изменился', p.stderr)
        self.assertFalse((self.state / 'game.tsv').exists())
        self.assertEqual(self.game_writes(), [])

    def test_audit_does_not_trust_simulated_battery_temperature(self):
        p = self.run_care('audit', MOCK_BATTERY_SIMULATED='1')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertIn('Батарея: оценка температуры недоступна.', p.stdout)
        self.assertNotIn('Температура батареи: 32.0', p.stdout)

    def test_game_user_switch_during_write_keeps_recovery_snapshot(self):
        p = self.run_care('game-boost', 'com.example.game', '--apply', stdin='ДА\n', MOCK_SWITCH_AFTER_WRITE='1')
        self.assertNotEqual(p.returncode, 0)
        self.assertTrue((self.state / 'game.tsv').exists())
        self.assertIn('game-restore --apply', p.stderr)
        (self.base / 'active-user').unlink()
        p = self.run_care('game-restore', '--apply', stdin='ДА\n')
        self.assertEqual(p.returncode, 0, p.stderr)

    def test_game_restore_error_keeps_snapshot_until_success(self):
        p = self.run_care('game-boost', 'com.example.game', '--apply', stdin='ДА\n')
        self.assertEqual(p.returncode, 0, p.stderr)
        p = self.run_care('game-restore', '--apply', stdin='ДА\n', MOCK_GAME_RESTORE_FAIL='1')
        self.assertNotEqual(p.returncode, 0)
        self.assertTrue((self.state / 'game.tsv').exists())
        self.assertFalse((self.state / 'lock').exists())
        p = self.run_care('game-restore', '--apply', stdin='ДА\n')
        self.assertEqual(p.returncode, 0, p.stderr)

    def test_game_battery_and_custom_modes_are_restored_exactly(self):
        for mode in ['battery', 'custom']:
            (self.base / 'game.json').write_text(json.dumps({'mode': mode}))
            p = self.run_care('game-boost', 'com.example.game', '--apply', stdin='ДА\n')
            self.assertEqual(p.returncode, 0, p.stderr)
            p = self.run_care('game-restore', '--apply', stdin='ДА\n')
            self.assertEqual(p.returncode, 0, p.stderr)
            self.assertEqual(json.loads((self.base / 'game.json').read_text())['mode'], mode)

    def test_already_performance_does_not_create_snapshot(self):
        (self.base / 'game.json').write_text(json.dumps({'mode': 'performance'}))
        p = self.run_care('game-boost', 'com.example.game', '--apply', stdin='ДА\n')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertFalse((self.state / 'game.tsv').exists())
        self.assertEqual(self.game_writes(), [])

    def test_new_menu_actions_work_without_hidden_writes(self):
        p = self.run_care('menu', stdin='5\ncom.example.game\n6\ncom.example.game\n8\ncom.example.game\nДА\n9\nДА\n7\n0\n')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertTrue(list(self.state.glob('game-report-*')))
        self.assertTrue(list(self.state.glob('frames-report-*')))
        self.assertTrue(list(self.state.glob('monitor-*')))
        self.assertFalse((self.state / 'game.tsv').exists())
        self.assertEqual(self.writes(), [])
        self.assertEqual(len(self.game_writes()), 2)

    def test_corrupt_game_snapshot_is_data_not_code(self):
        self.state.mkdir()
        for text in ['0\tcom.example.game\t$(id)\n', '0\tcom.example.game\tstandard\n' * 2,
                     '0\tcom.example.game;id\tstandard\n', 'bad\tcom.example.game\tstandard\n']:
            (self.state / 'game.tsv').write_text(text)
            p = self.run_care('game-restore', '--apply', stdin='ДА\n')
            self.assertNotEqual(p.returncode, 0)
        self.assertEqual(self.game_writes(), [])

    def test_basic_game_actions_never_escalate(self):
        for args in [('game', 'com.example.game'), ('frames', 'com.example.game'),
                     ('game-boost', 'com.example.game', '--apply'), ('game-restore', '--apply')]:
            p = self.run_care(*args, stdin='ДА\n', MOCK_UID='10234')
            self.assertNotEqual(p.returncode, 0)
        self.assertFalse(any(c[0] in ['settings', 'dumpsys', 'pm', 'cmd', 'am'] for c in self.calls()))

    def launcher(self, uid='10234', rish=None):
        shutil.copy(ROOT / 'run.sh', self.base / 'run.sh')
        local_bin = self.base / 'local-bin'
        local_bin.mkdir(exist_ok=True)
        (local_bin / 'id').write_text('#!/bin/sh\nprintf "%s\\n" ' + uid + '\n')
        (local_bin / 'id').chmod(0o755)
        if rish is not None:
            path = self.base / 'rish/rish'
            path.parent.mkdir(exist_ok=True)
            path.write_text(rish)
        return dict(self.env, PATH=str(local_bin) + ':/usr/bin:/bin')

    def test_launcher_preserves_interactive_input(self):
        env = self.launcher(rish='#!/bin/sh\n[ "$RISH_PRESERVE_ENV" = 0 ] || exit 20\nexec /bin/sh "$@"\n')
        p = subprocess.run(['sh', str(self.base / 'run.sh'), 'optimize', '--apply'],
                           input='ДА\n', text=True, capture_output=True, env=env, timeout=30)
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertTrue(all(self.database()[key] == '0.5' for key in KEYS))

    def test_basic_audit_never_calls_privileged_services(self):
        p = self.run_care('audit', MOCK_UID='10234')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertIn('БАЗОВЫЙ', p.stdout)
        self.assertIn('не проверены без shell-доступа', p.stdout)
        reports = list((self.home / '.android-care').glob('report-*'))
        self.assertEqual(len(reports), 1)
        self.assertEqual(reports[0].stat().st_mode & 0o777, 0o600)
        self.assertFalse(any(c[0] in ['settings', 'dumpsys', 'pm', 'am', 'cmd'] for c in self.calls()))
        self.assertFalse(self.state.exists())

    def test_basic_apply_is_blocked_for_all_actions(self):
        for action in ['optimize', 'restore', 'trim']:
            p = self.run_care(action, '--apply', stdin='ДА\n', MOCK_UID='10234')
            self.assertNotEqual(p.returncode, 0)
        self.assertFalse(any(c[0] in ['settings', 'dumpsys', 'pm', 'am', 'cmd'] for c in self.calls()))

    def test_basic_menu_cannot_escalate(self):
        p = self.run_care('menu', stdin='2\n3\n4\n1\n0\n', MOCK_UID='10234')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertEqual(p.stdout.count('Недоступно без Shizuku/rish'), 3)
        self.assertFalse(any(c[0] in ['settings', 'dumpsys', 'pm', 'am', 'cmd'] for c in self.calls()))

    def test_launcher_without_rish_runs_basic_audit(self):
        env = dict(self.launcher(), MOCK_UID='10234')
        p = subprocess.run(['sh', str(self.base / 'run.sh')], text=True,
                           capture_output=True, env=env, timeout=30)
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertIn('БАЗОВЫЙ', p.stdout)

    def test_launcher_falls_back_on_failed_rish_only_for_audit(self):
        env = dict(self.launcher(rish='#!/bin/sh\nexit 42\n'), MOCK_UID='10234')
        p = subprocess.run(['sh', str(self.base / 'run.sh')], text=True,
                           capture_output=True, env=env, timeout=30)
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertIn('БАЗОВЫЙ', p.stdout)
        for args in [('menu',), ('optimize', '--apply'), ('trim', '--apply'), ('restore', '--apply'),
                     ('game-boost', 'com.example.game', '--apply'), ('game-restore', '--apply'),
                     ('game', 'com.example.game'), ('frames', 'com.example.game'), ('monitor', '2')]:
            p = subprocess.run(['sh', str(self.base / 'run.sh'), *args], input='ДА\n',
                               text=True, capture_output=True, env=env, timeout=30)
            self.assertEqual(p.returncode, 42, p.stderr)
        self.assertEqual(self.writes(), [])

    def test_existing_shell_does_not_need_rish(self):
        env = self.launcher(uid='2000', rish='#!/bin/sh\nexit 42\n')
        p = subprocess.run(['sh', str(self.base / 'run.sh'), 'optimize'], text=True,
                           capture_output=True, env=env, timeout=30)
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertNotIn('БАЗОВЫЙ', p.stdout)

    def test_explicit_missing_rish_path_is_not_silently_ignored(self):
        env = dict(self.launcher(), RISH=str(self.base / 'missing-rish'))
        p = subprocess.run(['sh', str(self.base / 'run.sh')], text=True,
                           capture_output=True, env=env, timeout=30)
        self.assertNotEqual(p.returncode, 0)
        self.assertIn('Указанный RISH не найден', p.stderr)


if __name__ == '__main__':
    unittest.main()
