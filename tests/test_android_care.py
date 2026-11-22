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
elif name == 'dumpsys':
    if args[0] == os.environ.get('MOCK_DUMP_FAIL'):
        print('Permission Denial'); sys.exit(1)
    if args[0] == 'deviceidle':
        print('  mState=ACTIVE\n  Whitelist user apps: private.example.app')
    elif args[0] == 'display':
        print('  mSupportedModes=[{id=1, refreshRate=120.0}]\n  uniqueId=private-display-id')
    elif args[0] == 'diskstats':
        print('Data-Free: 1234K / 9999K total = 12% free\nPackage Names: [private.example.app]')
    else:
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
        self.home = self.base / 'home'
        self.home.mkdir()
        self.env = dict(os.environ, MOCK_HOME=str(self.base), HOME=str(self.home))
        mock = self.bin / 'android-mock'
        mock.write_text(MOCK)
        mock.chmod(0o755)
        for name in ['id', 'stat', 'getprop', 'dumpsys', 'settings', 'pm']:
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
        self.assertFalse(any(c[0] in ['settings', 'dumpsys', 'pm'] for c in self.calls()))
        self.assertFalse(self.state.exists())

    def test_basic_apply_is_blocked_for_all_actions(self):
        for action in ['optimize', 'restore', 'trim']:
            p = self.run_care(action, '--apply', stdin='ДА\n', MOCK_UID='10234')
            self.assertNotEqual(p.returncode, 0)
        self.assertFalse(any(c[0] in ['settings', 'dumpsys', 'pm'] for c in self.calls()))

    def test_basic_menu_cannot_escalate(self):
        p = self.run_care('menu', stdin='2\n3\n4\n1\n0\n', MOCK_UID='10234')
        self.assertEqual(p.returncode, 0, p.stderr)
        self.assertEqual(p.stdout.count('Недоступно без Shizuku/rish'), 3)
        self.assertFalse(any(c[0] in ['settings', 'dumpsys', 'pm'] for c in self.calls()))

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
        for args in [('menu',), ('optimize', '--apply'), ('trim', '--apply'), ('restore', '--apply')]:
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
