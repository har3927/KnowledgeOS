import React from 'react'

interface MarkdownProps {
  content: string
}

export function Markdown({ content }: MarkdownProps) {
  if (!content) return null

  // Split by code blocks first
  const parts = content.split(/(```[\s\S]*?```)/g)

  return (
    <div className="space-y-3 text-xs leading-relaxed text-[var(--color-foreground)]">
      {parts.map((part, idx) => {
        if (part.startsWith('```')) {
          // Code block
          const lines = part.split('\n')
          const firstLine = lines[0]
          const lang = firstLine.replace('```', '').trim()
          const codeContent = lines
            .slice(1, lines.length - (lines[lines.length - 1] === '```' ? 1 : 0))
            .join('\n')
          return (
            <div key={idx} className="my-3 overflow-hidden rounded-lg border border-slate-700 bg-slate-900 shadow-md">
              {lang && (
                <div className="flex items-center justify-between border-b border-slate-800 bg-slate-950 px-3 py-1.5 text-[9px] font-semibold uppercase tracking-wider text-slate-400">
                  <span>{lang}</span>
                </div>
              )}
              <pre className="overflow-x-auto p-3 font-mono text-[10px] text-slate-100">
                <code>{codeContent}</code>
              </pre>
            </div>
          )
        } else {
          // Regular text
          const lines = part.split('\n')
          const elements: React.ReactNode[] = []
          let currentList: React.ReactNode[] = []

          const renderLineText = (lineText: string) => {
            // Split by inline code first
            const codeParts = lineText.split(/(`[^`]+`)/g)
            return codeParts.map((cp, cidx) => {
              if (cp.startsWith('`') && cp.endsWith('`')) {
                return (
                  <code
                    key={cidx}
                    className="mx-0.5 rounded bg-indigo-50/80 dark:bg-indigo-950/40 px-1.5 py-0.5 font-mono text-[10px] font-medium text-indigo-600 dark:text-indigo-300 border border-indigo-100/50 dark:border-indigo-900/30"
                  >
                    {cp.substring(1, cp.length - 1)}
                  </code>
                )
              }
              // Parse bold text
              const boldParts = cp.split(/(\*\*[^*]+\*\*)/g)
              return boldParts.map((bp, bidx) => {
                if (bp.startsWith('**') && bp.endsWith('**')) {
                  return (
                    <strong key={bidx} className="font-extrabold text-[var(--color-primary)]">
                      {bp.substring(2, bp.length - 2)}
                    </strong>
                  )
                }
                return bp
              })
            })
          }

          const flushList = (key: number) => {
            if (currentList.length > 0) {
              elements.push(
                <ul key={`ul-${key}`} className="my-2 list-none pl-1 space-y-1.5">
                  {currentList}
                </ul>
              )
              currentList = []
            }
          }

          lines.forEach((line, lidx) => {
            const trimmed = line.trim()

            // Headings
            if (trimmed.startsWith('###')) {
              flushList(lidx)
              elements.push(
                <h4 key={lidx} className="mt-3 text-xs font-bold text-[var(--color-foreground)]">
                  {renderLineText(trimmed.replace(/^###\s*/, ''))}
                </h4>
              )
            } else if (trimmed.startsWith('##')) {
              flushList(lidx)
              elements.push(
                <h3 key={lidx} className="mt-4 text-sm font-bold text-[var(--color-foreground)]">
                  {renderLineText(trimmed.replace(/^##\s*/, ''))}
                </h3>
              )
            } else if (trimmed.startsWith('#')) {
              flushList(lidx)
              elements.push(
                <h2 key={lidx} className="mt-5 text-base font-extrabold text-[var(--color-foreground)]">
                  {renderLineText(trimmed.replace(/^#\s*/, ''))}
                </h2>
              )
            }
            // List items starting with '-' or '*'
            else if (trimmed.startsWith('-') || trimmed.startsWith('*')) {
              const itemText = trimmed.replace(/^[-*]\s*/, '')
              currentList.push(
                <li key={lidx} className="flex items-start gap-2 text-[11px] leading-relaxed text-[var(--color-muted-foreground)]">
                  <span className="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-[var(--color-primary)]" />
                  <span className="flex-1">{renderLineText(itemText)}</span>
                </li>
              )
            }
            // Numbered lists
            else if (/^\d+\.\s/.test(trimmed)) {
              flushList(lidx)
              elements.push(
                <div key={lidx} className="my-1.5 flex items-start gap-2 pl-1 text-[11px] leading-relaxed text-[var(--color-muted-foreground)]">
                  <span className="font-semibold text-[var(--color-primary)] select-none">{trimmed.match(/^(\d+\.)/)?.[0]}</span>
                  <span className="flex-1">{renderLineText(trimmed.replace(/^\d+\.\s*/, ''))}</span>
                </div>
              )
            }
            // Paragraph
            else if (trimmed.length > 0) {
              flushList(lidx)
              elements.push(
                <p key={lidx} className="my-1.5 text-[11px] leading-relaxed text-[var(--color-foreground)] opacity-95">
                  {renderLineText(line)}
                </p>
              )
            } else {
              flushList(lidx)
            }
          })

          flushList(lines.length)
          return <React.Fragment key={idx}>{elements}</React.Fragment>
        }
      })}
    </div>
  )
}
