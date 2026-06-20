import { NavLink, Outlet } from 'react-router-dom'
import {
  LayoutDashboard, BookOpen, Route, Bot, RotateCcw,
  Network, TrendingUp, Settings, Menu, Brain,
} from 'lucide-react'
import { useSidebarStore } from '@/stores/sidebar'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'

const navItems = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/topics', icon: BookOpen, label: 'Topics' },
  { to: '/learning-paths', icon: Route, label: 'Learning Paths' },
  { to: '/tutor', icon: Bot, label: 'AI Tutor' },
  { to: '/revisions', icon: RotateCcw, label: 'Revisions' },
  { to: '/graph', icon: Network, label: 'Knowledge Graph' },
  { to: '/progress', icon: TrendingUp, label: 'Progress' },
  { to: '/settings', icon: Settings, label: 'Settings' },
]

export function AppLayout() {
  const { collapsed, toggle } = useSidebarStore()

  return (
    <div className="flex min-h-screen">
      <aside
        className={cn(
          'fixed inset-y-0 left-0 z-30 flex flex-col border-r bg-[var(--color-sidebar)] transition-all duration-300',
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

      <div className={cn('flex flex-1 flex-col transition-all duration-300', collapsed ? 'ml-16' : 'ml-60')}>
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
    </div>
  )
}
