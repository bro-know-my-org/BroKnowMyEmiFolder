import { mkdir, readFile, writeFile } from 'node:fs/promises'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const entities = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '{': '&#123;', '}': '&#125;', '|': '&#124;' }
const prose = text => text.replace(/[&<>{}|]/g, char => entities[char]).replaceAll('\n', '<br>')

export function renderApi(api, sourceRef = 'HEAD') {
  if (api.schemaVersion !== 1) throw new Error(`Unsupported API schema: ${api.schemaVersion}`)
  const source = `https://github.com/bro-know-my-org/BroKnowMyEmiFolder/blob/${encodeURIComponent(sourceRef)}/${api.source}`
  const sections = [
    '<!-- Generated from @Info/@Param. Do not edit. -->',
    '# KubeJS API Reference',
    'Call these methods inside a `RecipeViewerEvents.fold(event => { ... })` callback.',
    prose(api.description)
  ]
  for (const method of api.methods) {
    const signature = `event.${method.name}(${method.parameters.map(p => p.name).join(', ')})`
    sections.push(`## ${signature}`, `\`\`\`js\n${signature}\n\`\`\``, prose(method.description))
    if (method.parameters.length) {
      sections.push('| Parameter | Description |\n| --- | --- |\n' + method.parameters
        .map(p => `| \`${p.name}\` | ${prose(p.description)} |`).join('\n'))
    }
    sections.push(`[View source](${source}#L${method.line})`)
  }
  return sections.join('\n\n') + '\n'
}

if (process.argv[1] && fileURLToPath(import.meta.url) === resolve(process.argv[1])) {
  const api = JSON.parse(await readFile(new URL('../build/docs/kubejs-api.json', import.meta.url), 'utf8'))
  const output = fileURLToPath(new URL('../docs/api/kubejs.md', import.meta.url))
  await mkdir(dirname(output), { recursive: true })
  await writeFile(output, renderApi(api, process.env.GITHUB_SHA || 'HEAD'))
  console.log(`Generated ${api.methods.length} method overloads → docs/api/kubejs.md`)
}
