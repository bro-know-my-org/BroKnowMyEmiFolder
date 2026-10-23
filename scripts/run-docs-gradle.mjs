import { spawnSync } from 'node:child_process'
import { fileURLToPath } from 'node:url'

const windows = process.platform === 'win32'
const tasks = process.argv.slice(2)
const result = spawnSync(windows ? 'gradlew.bat' : 'sh',
  windows ? ['--console=plain', ...tasks] : ['gradlew', '--console=plain', ...tasks], {
    cwd: fileURLToPath(new URL('../', import.meta.url)),
    stdio: 'inherit',
    shell: windows
  })
if (result.error) throw result.error
process.exit(result.status ?? 1)
