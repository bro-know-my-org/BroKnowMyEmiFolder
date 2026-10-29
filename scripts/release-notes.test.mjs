import assert from 'node:assert/strict'
import { execFileSync, spawnSync } from 'node:child_process'
import { mkdtempSync, readFileSync, rmSync, writeFileSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { join } from 'node:path'
import { test } from 'node:test'
import { fileURLToPath } from 'node:url'

const script = fileURLToPath(new URL('./generate-release-notes.mjs', import.meta.url))

function fixture(t) {
  const cwd = mkdtempSync(join(tmpdir(), 'bkmef-release-'))
  t.after(() => rmSync(cwd, { recursive: true, force: true }))
  const fixtureEnv = {
    ...Object.fromEntries(Object.entries(process.env).filter(([name]) => !name.startsWith('GIT_'))),
    GIT_CONFIG_NOSYSTEM: '1', GIT_CONFIG_GLOBAL: '/dev/null'
  }
  const git = (...args) => execFileSync('git', args, { cwd, encoding: 'utf8', env: fixtureEnv }).trim()
  git('init', '--quiet')
  git('config', 'user.name', 'Release Test')
  git('config', 'user.email', 'release@example.com')
  const commit = message => git('commit', '--quiet', '--allow-empty', '-m', message)
  const generate = (env = {}) => {
    const result = spawnSync(process.execPath, [script], {
      cwd, encoding: 'utf8', env: {
        ...fixtureEnv, RELEASE_VERSION: '0.0.7+1.21.1', RELEASE_NOTES: '',
        GITHUB_REPOSITORY: 'example/mod', GITHUB_SERVER_URL: 'https://github.com', ...env
      }
    })
    assert.equal(result.status, 0, result.stderr)
    return readFileSync(join(cwd, 'CHANGELOG_RELEASE.md'), 'utf8')
  }
  return { cwd, git, commit, generate }
}

test('manual notes override changelog and preserve multiline Markdown without Git history', t => {
  const { cwd, generate } = fixture(t)
  writeFileSync(join(cwd, 'CHANGELOG.md'), '## [0.0.7+1.21.1]\nChangelog text\n')
  assert.equal(generate({ RELEASE_NOTES: '## Highlights\n\n- Manual fix\n' }), '## Highlights\n\n- Manual fix\n')
})

test('matching changelog version excludes unreleased and older entries and preserves code fences', t => {
  const { cwd, generate } = fixture(t)
  writeFileSync(join(cwd, 'CHANGELOG.md'), [
    '# Changelog', '## [Unreleased]', '- Future change',
    '## [v0.0.7+1.21.1] - 2026-10-29', '### Fixed', '- Current fix',
    '```md', '```example', '## Example heading', '```', '## [0.0.6+1.21.1]', '- Old fix'
  ].join('\r\n'))
  assert.equal(generate({ RELEASE_NOTES: '  ' }), '### Fixed\n- Current fix\n```md\n```example\n## Example heading\n```\n')
  writeFileSync(join(cwd, 'CHANGELOG.md'), '## v0.0.7+1.21.1 - 2026-10-29\n- Plain heading\n')
  assert.equal(generate(), '- Plain heading\n')
})

test('inline backticks and indented code do not hide version headings', t => {
  const { cwd, generate } = fixture(t)
  writeFileSync(join(cwd, 'CHANGELOG.md'), [
    '# Changelog', '```example```', '\t```indented code',
    '  ## [0.0.7+1.21.1]', '- Current fix', '```inline example```',
    '   ## [0.0.6+1.21.1]', '- Old fix'
  ].join('\n'))
  assert.equal(generate(), '- Current fix\n```inline example```\n')
})

test('any level-two heading ends the selected version section, including empty headings', t => {
  const { cwd, generate } = fixture(t)
  for (const boundary of ['##', '##   ', '  ##\t', '## Other section']) {
    writeFileSync(join(cwd, 'CHANGELOG.md'), `## [0.0.7+1.21.1]\n- Current fix\n${boundary}\nUnrelated content`)
    assert.equal(generate(), '- Current fix\n')
  }
})

test('automatic notes include direct commits and PR subjects since the previous tag, including reruns', t => {
  const { cwd, git, commit, generate } = fixture(t)
  commit('feat: previous release')
  git('tag', 'v0.0.6+1.21.1')
  commit('fix: direct <T> & [search]')
  commit('feat: improve search (#42)')
  git('tag', 'v0.0.7+1.21.1')
  // A changelog that has no current entry must fall back to the commit range.
  writeFileSync(join(cwd, 'CHANGELOG.md'), '## [0.0.6+1.21.1]\n- Previous release\n')
  const notes = generate()
  assert.match(notes, /fix: direct &lt;T&gt; &amp; \\\[search\\\]/)
  assert.match(notes, /feat: improve search \(#42\)/)
  assert.doesNotMatch(notes, /feat: previous release/)
  assert.match(notes, /https:\/\/github.com\/example\/mod\/commit\/[a-f0-9]{40}/)
  assert.ok(notes.includes(`/compare/v0.0.6%2B1.21.1...${git('rev-parse', 'HEAD')}`))
  assert.equal(generate(), notes)
})

test('first release includes all commits and does not select an unrelated branch tag', t => {
  const { git, commit, generate } = fixture(t)
  commit('feat: first feature')
  const branch = git('branch', '--show-current')
  git('checkout', '--quiet', '-b', 'unrelated')
  commit('feat: unrelated change')
  git('tag', 'v9.0.0')
  git('checkout', '--quiet', branch)
  git('tag', 'v0.0.7+1.21.1')
  commit('fix: selected first-release checkout')
  const notes = generate()
  assert.match(notes, /feat: first feature/)
  assert.match(notes, /fix: selected first-release checkout/)
  assert.doesNotMatch(notes, /unrelated change|compare\//)
  assert.ok(notes.includes(`/commits/${git('rev-parse', 'HEAD')}`))
})

test('manual release without an existing tag stops at the checkout and handles an empty range', t => {
  const { git, commit, generate } = fixture(t)
  commit('feat: old feature')
  git('tag', 'v0.0.6+1.21.1')
  assert.match(generate(), /No new commits since the previous release/)
  commit('fix: unreleased fix')
  const notes = generate()
  assert.match(notes, /fix: unreleased fix/)
  assert.doesNotMatch(notes, /feat: old feature/)
})

test('manual dispatch from a newer checkout describes the code actually being built', t => {
  const { git, commit, generate } = fixture(t)
  commit('feat: previous release')
  git('tag', 'v0.0.6+1.21.1')
  commit('fix: current release')
  git('tag', 'v0.0.7+1.21.1')
  commit('fix: selected checkout')
  const notes = generate()
  assert.match(notes, /fix: current release/)
  assert.match(notes, /fix: selected checkout/)
  assert.doesNotMatch(notes, /feat: previous release/)
  assert.ok(notes.includes(`/compare/v0.0.6%2B1.21.1...${git('rev-parse', 'HEAD')}`))
})

test('large first-release history is not limited by the child-process stdout buffer', t => {
  const { commit, generate } = fixture(t)
  for (let index = 0; index < 12; index++) {
    commit(`fix: large history ${index} ${'x'.repeat(100_000)}`)
  }
  const notes = generate()
  assert.ok(Buffer.byteLength(notes) > 1024 * 1024)
  assert.equal(notes.split('\n').filter(line => line.startsWith('- fix:')).length, 12)
  assert.match(notes, /fix: large history 0 /)
  assert.match(notes, /fix: large history 11 /)
})
