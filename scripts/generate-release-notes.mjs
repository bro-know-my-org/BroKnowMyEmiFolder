import { execFileSync, spawn } from 'node:child_process'
import { existsSync, readFileSync, writeFileSync } from 'node:fs'
import { createInterface } from 'node:readline'

const version = process.env.RELEASE_VERSION?.replace(/^v/, '')
if (!version) throw new Error('RELEASE_VERSION is required')

const tag = `v${version}`
const git = (...args) => execFileSync('git', args, { encoding: 'utf8' }).trim()

// Accept Keep a Changelog headings: ## [0.0.7+1.21.1] - YYYY-MM-DD,
// as well as unbracketed versions or versions prefixed with v.
function changelogNotes() {
  if (!existsSync('CHANGELOG.md')) return ''
  const lines = readFileSync('CHANGELOG.md', 'utf8').split(/\r?\n/)
  const notes = []
  let collecting = false
  let fence = ''
  for (const line of lines) {
    const delimiter = line.match(/^ {0,3}(`{3,}|~{3,})/)
    if (delimiter) {
      const rest = line.slice(delimiter[0].length)
      if (!fence) {
        if (delimiter[1][0] !== '`' || !rest.includes('`')) fence = delimiter[1]
      }
      else if (delimiter[1][0] === fence[0] && delimiter[1].length >= fence.length
        && /^[ \t]*$/.test(rest)) fence = ''
    }
    const heading = !fence && line.match(/^ {0,3}##(?:[ \t]+(.*)|$)/)
    if (heading) {
      if (collecting) break
      const headingVersion = (heading[1] || '').match(/^(?:\[v?([^\]]+)\]|v?(\S+))/)
      collecting = headingVersion && (headingVersion[1] || headingVersion[2]) === version
      continue
    }
    if (collecting) notes.push(line)
  }
  return notes.join('\n').trim()
}

async function commitNotes() {
  const repository = process.env.GITHUB_REPOSITORY
  if (!repository) throw new Error('GITHUB_REPOSITORY is required for commit notes')
  const url = `${process.env.GITHUB_SERVER_URL || 'https://github.com'}/${repository}`
  // Notes describe the checkout being built, including manual dispatches.
  // Exclude this release's tag so tag-triggered reruns keep the original range.
  const tags = git('tag', '--merged', 'HEAD', '--list', 'v*').split('\n').filter(value => value && value !== tag)
  const previousTag = tags.length
    ? git('describe', '--tags', '--abbrev=0', '--match=v*', `--exclude=${tag}`, 'HEAD')
    : ''
  const range = previousTag ? `${previousTag}..HEAD` : 'HEAD'
  const log = spawn('git', ['log', '--reverse', '--format=%H%x09%s', range, '--'], {
    stdio: ['ignore', 'pipe', 'inherit']
  })
  const completed = new Promise((resolve, reject) => {
    log.once('error', reject)
    log.once('close', (code, signal) => {
      if (code === 0) resolve()
      else reject(new Error(`git log failed: ${signal || code}`))
    })
  })
  const entries = []
  const readEntries = async () => {
    for await (const commit of createInterface({ input: log.stdout, crlfDelay: Infinity })) {
      const [sha, ...subject] = commit.split('\t')
      const title = subject.join('\t').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/[\\`*_[\]]/g, '\\$&')
      entries.push(`- ${title} ([${sha.slice(0, 7)}](${url}/commit/${sha}))`)
    }
  }
  await Promise.all([completed, readEntries()])
  if (!entries.length) entries.push('No new commits since the previous release.')
  const head = git('rev-parse', 'HEAD')
  const history = previousTag
    ? `${url}/compare/${encodeURIComponent(previousTag)}...${head}`
    : `${url}/commits/${head}`
  return `## What's Changed\n\n${entries.join('\n')}\n\n**Full Changelog**: ${history}`
}

const manualNotes = process.env.RELEASE_NOTES || ''
// Manual notes take precedence, then the matching version in CHANGELOG.md.
// The commit fallback includes direct commits, merge commits and squash PRs.
const notes = manualNotes.trim() ? manualNotes : changelogNotes() || await commitNotes()
writeFileSync('CHANGELOG_RELEASE.md', `${notes.trimEnd()}\n`)
