import { defineConfig } from 'vitepress'

export default defineConfig({
  lang: 'en-US',
  title: 'Bro Know My EMI Folder',
  description: 'Define collapsible EMI index groups with KubeJS.',
  base: process.env.DOCS_BASE || '/',
  themeConfig: {
    siteTitle: 'BKMEF',
    nav: [
      { text: 'Getting Started', link: '/getting-started' },
      { text: 'API Reference', link: '/api/kubejs' },
      { text: 'Changelog', link: 'https://github.com/bro-know-my-org/BroKnowMyEmiFolder/releases' }
    ],
    sidebar: [
      { text: 'Getting Started', link: '/getting-started' },
      { text: 'KubeJS Guide', link: '/kubejs-api' },
      { text: 'Examples', link: '/examples' },
      { text: 'API Reference', link: '/api/kubejs' }
    ],
    search: { provider: 'local' },
    socialLinks: [{ icon: 'github', link: 'https://github.com/bro-know-my-org/BroKnowMyEmiFolder' }]
  }
})
