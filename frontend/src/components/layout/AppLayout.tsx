import { useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import {
  LayoutDashboard, BookOpen, Bot, RotateCcw,
  Menu, Brain,
} from 'lucide-react'
import { useSidebarStore } from '@/stores/sidebar'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { api } from '@/lib/api'
import { Markdown } from '@/components/ui/Markdown'

const navItems = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/topics', icon: BookOpen, label: 'Topics' },
  { to: '/revisions', icon: RotateCcw, label: 'Revisions' },
]

export function AppLayout() {
  const { collapsed, toggle } = useSidebarStore()
  const location = useLocation()
  const [isOpen, setIsOpen] = useState(false)
  const [message, setMessage] = useState('')
  const [messages, setMessages] = useState<Array<{ sender: 'user' | 'ai'; text: string }>>([])
  const [sending, setSending] = useState(false)

  const handleSend = async () => {
    if (!message.trim() || sending) return
    const userMsg = message
    setMessage('')
    setMessages(prev => [...prev, { sender: 'user', text: userMsg }])
    setSending(true)

    let topicId: number | undefined
    const match = location.pathname.match(/\/topics\/(\d+)/)
    if (match) {
      topicId = Number(match[1])
    }

    try {
      const res = await api.ai.tutor(userMsg, topicId)
      setMessages(prev => [...prev, { sender: 'ai', text: res.answer }])
    } catch (e) {
      setMessages(prev => [...prev, { sender: 'ai', text: 'Sorry, I encountered an error. Please try again.' }])
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="flex min-h-screen bg-[var(--color-background)]">
      <aside
        className={cn(
          'fixed inset-y-0 left-0 z-35 flex flex-col border-r bg-[var(--color-sidebar)] transition-all duration-300',
          collapsed ? 'w-16' : 'w-60'
        )}
      >
        <div className="flex h-14 items-center gap-2 border-b px-4">
          <Brain className="h-6 w-6 shrink-0 text-[var(--color-primary)]" />
          {!collapsed && <span className="font-semibold text-[var(--color-sidebar-foreground)]">KnowledgeOS</span>}
        </div>
        <nav className="flex-1 space-y-1 p-2">
          {navItems.map(({ to, icon: Icon, label }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-[var(--color-sidebar-accent)] text-[var(--color-primary)]'
                    : 'text-[var(--color-sidebar-foreground)] hover:bg-[var(--color-sidebar-accent)]'
                )
              }
            >
              <Icon className="h-4 w-4 shrink-0" />
              {!collapsed && <span>{label}</span>}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className={cn('flex flex-1 flex-col transition-all duration-300 mr-0', collapsed ? 'ml-16' : 'ml-60')}>
        <header className="sticky top-0 z-20 flex h-14 items-center gap-4 border-b bg-[var(--color-background)]/80 px-6 backdrop-blur">
          <Button variant="ghost" size="icon" onClick={toggle}>
            <Menu className="h-4 w-4" />
          </Button>
          <div className="flex-1" />
          <span className="text-sm text-[var(--color-muted-foreground)]">Demo User</span>
        </header>
        <main className="flex-1 p-6">
          <Outlet />
        </main>
      </div>

      {/* Floating AI Co-Pilot Toggle */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="fixed bottom-6 right-6 z-50 flex h-14 w-14 items-center justify-center rounded-full bg-[var(--color-primary)] text-white shadow-lg hover:scale-105 hover:bg-[var(--color-primary)]/90 active:scale-95 transition-all duration-200 cursor-pointer"
        title="AI Co-Pilot"
      >
        <Bot className="h-6 w-6 animate-pulse" />
      </button>

      {/* Slide-out Glassmorphic Chat Drawer */}
      <div
        className={cn(
          "fixed inset-y-0 right-0 z-40 w-96 border-l bg-background/95 backdrop-blur-md shadow-2xl transition-transform duration-300 flex flex-col",
          isOpen ? "translate-x-0" : "translate-x-full"
        )}
      >
        <div className="flex h-14 items-center justify-between border-b px-4">
          <div className="flex items-center gap-2">
            <Bot className="h-5 w-5 text-[var(--color-primary)]" />
            <span className="font-semibold text-sm">AI Co-Pilot</span>
          </div>
          <Button variant="ghost" size="sm" onClick={() => setIsOpen(false)}>Close</Button>
        </div>
        
        <div className="flex-1 overflow-y-auto p-4 space-y-4">
          {messages.length === 0 ? (
            <div className="flex flex-col items-center justify-center text-center mt-20 space-y-2">
              <Bot className="h-10 w-10 text-[var(--color-primary)] animate-bounce" />
              <p className="text-xs text-[var(--color-muted-foreground)] max-w-[200px]">
                I know context of the page you are viewing. Ask me anything!
              </p>
            </div>
          ) : (
            messages.map((m, idx) => (
              <div key={idx} className={cn("flex flex-col max-w-[80%] rounded-lg p-3 text-xs leading-relaxed", m.sender === 'user' ? "bg-[var(--color-primary)] text-white self-end ml-auto" : "bg-[var(--color-accent)] text-foreground self-start mr-auto")}>
                {m.sender === 'user' ? m.text : <Markdown content={m.text} />}
              </div>
            ))
          )}
          {sending && (
            <div className="bg-[var(--color-accent)] text-foreground self-start mr-auto max-w-[80%] rounded-lg p-3 text-xs animate-pulse">
              Thinking...
            </div>
          )}
        </div>

        <div className="border-t p-3 bg-background">
          <div className="flex gap-2">
            <input
              type="text"
              placeholder="Ask a question..."
              className="flex-1 h-9 rounded-md border bg-transparent px-3 py-1 text-xs shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-[var(--color-primary)]"
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSend()}
              disabled={sending}
            />
            <Button size="sm" onClick={handleSend} disabled={sending}>Send</Button>
          </div>
        </div>
      </div>
    </div>
  )
}
