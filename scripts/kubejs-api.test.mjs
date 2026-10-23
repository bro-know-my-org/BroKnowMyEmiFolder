import assert from 'node:assert/strict'
import { spawnSync } from 'node:child_process'
import { mkdtempSync, readFileSync, rmSync, writeFileSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { join } from 'node:path'
import { test } from 'node:test'
import { renderApi } from './generate-kubejs-api.mjs'

test('real API preserves overloads and hides injected Rhino Context', () => {
  const api = JSON.parse(readFileSync('build/docs/kubejs-api.json', 'utf8'))
  assert.equal(api.methods.filter(m => m.name === 'fold').length, 3)
  assert.ok(api.methods.every(m => m.parameters.every(p => p.name !== 'cx' && p.description)))
  assert.match(renderApi(api), /event\.fold\(id, name, filter, options\)/)
})

test('AST export handles text blocks, rejects broken documentation, and renders safe tables', () => {
  const directory = mkdtempSync(join(tmpdir(), 'bkmef-docs-'))
  const source = join(directory, 'Fixture.java')
  const output = join(directory, 'api.json')
  const java = process.env.KUBEJS_DOCS_JAVA || (process.env.JAVA_HOME ? join(process.env.JAVA_HOME, 'bin', 'java') : 'java')
  const fixture = `import dev.latvian.mods.rhino.Context;
    @Info("""
    Fixture <T> with {{ braces }} and "quotes".
    Second line.
    """)
    interface Fixture {
      @Info(value = "Defines a group.", params = {
        @Param(name = "name", value = "Text | component"),
        @Param(name = "filter", value = "{ mod: string }")
      })
      void fold(Context cx, Object name, Object filter);
      private void helper() {}
      static void utility() {}
    }`
  const run = text => {
    writeFileSync(source, text)
    return spawnSync(java, ['-cp', 'build/classes/java/apiDocs', 'ExportKubeJsApi', source, output, 'Fixture.java'],
      { encoding: 'utf8' })
  }
  try {
    const result = run(fixture)
    assert.equal(result.status, 0, result.stderr || result.error?.message)
    const first = readFileSync(output, 'utf8')
    const api = JSON.parse(first)
    assert.equal(api.methods.length, 1)
    assert.deepEqual(api.methods[0].parameters.map(p => p.name), ['name', 'filter'])
    assert.match(api.description, /Second line\./)
    assert.equal(run(fixture).status, 0)
    assert.equal(readFileSync(output, 'utf8'), first)
    const markdown = renderApi(api)
    assert.match(markdown, /Text &#124; component/)
    assert.match(markdown, /&#123;&#123; braces &#125;&#125;/)
    assert.match(markdown, /&lt;T&gt;/)
    const shorthand = run(`@Info("Fixture") interface Fixture {
      @Info(value = "Defines a group.", params = @Param(name = "name", value = "Display name"))
      void fold(Object name);
    }`)
    assert.equal(shorthand.status, 0, shorthand.stderr)
    assert.equal(JSON.parse(readFileSync(output, 'utf8')).methods[0].parameters[0].description, 'Display name')
    for (const [text, error] of [
      [fixture.replace('Object filter', 'Object missing'), /Missing @Param/],
      [fixture.replace(', Object filter', ''), /Stale @Param/],
      [fixture.slice(0, -1), /Invalid Java source/]
    ]) {
      const failed = run(text)
      assert.notEqual(failed.status, 0)
      assert.match(failed.stderr, error)
    }
    assert.throws(() => renderApi({ schemaVersion: 2 }), /Unsupported API schema/)
  } finally {
    rmSync(directory, { recursive: true, force: true })
  }
})
