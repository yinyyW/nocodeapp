declare module "markdown-it" {
  interface MarkdownItOptions {
    html?: boolean
    linkify?: boolean
    typographer?: boolean
    highlight?: (str: string, lang: string) => string
  }

  interface MarkdownIt {
    render(src: string): string
    utils: {
      escapeHtml(str: string): string
    }
  }

  interface MarkdownItConstructor {
    new (options?: MarkdownItOptions): MarkdownIt
    (options?: MarkdownItOptions): MarkdownIt
  }

  const MarkdownIt: MarkdownItConstructor
  export default MarkdownIt
}
