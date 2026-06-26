import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Search, Grid, List } from 'lucide-react'
import { api } from '@/lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { cn } from '@/lib/utils'

const difficultyColor: Record<string, 'default' | 'secondary' | 'warning'> = {
  BEGINNER: 'secondary',
  INTERMEDIATE: 'default',
  ADVANCED: 'warning',
}

export function TopicsPage() {
  const [search, setSearch] = useState('')
  const [categoryId, setCategoryId] = useState<string>('')
  const [difficulty, setDifficulty] = useState('')
  const [sortBy, setSortBy] = useState('title')
  const [view, setView] = useState<'grid' | 'list'>('grid')

  const { data: categories } = useQuery({ queryKey: ['categories'], queryFn: api.categories.list })
  const { data, isLoading } = useQuery({
    queryKey: ['topics', search, categoryId, difficulty, sortBy],
    queryFn: () => api.topics.list({
      search: search || undefined,
      categoryId: categoryId || undefined,
      difficulty: difficulty || undefined,
      sortBy,
      size: 50,
    }),
  })

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Topics</h1>
        <p className="text-[var(--color-muted-foreground)]">Explore and learn new topics</p>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        <div className="relative flex-1 min-w-[200px]">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-[var(--color-muted-foreground)]" />
          <Input className="pl-9" placeholder="Search topics..." value={search} onChange={(e) => setSearch(e.target.value)} />
        </div>
        <select className="h-9 rounded-md border px-3 text-sm" value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
          <option value="">All Categories</option>
          {categories?.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <select className="h-9 rounded-md border px-3 text-sm" value={difficulty} onChange={(e) => setDifficulty(e.target.value)}>
          <option value="">All Difficulties</option>
          <option value="BEGINNER">Beginner</option>
          <option value="INTERMEDIATE">Intermediate</option>
          <option value="ADVANCED">Advanced</option>
        </select>
        <select className="h-9 rounded-md border px-3 text-sm" value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
          <option value="title">Sort by Title</option>
          <option value="difficulty">Sort by Difficulty</option>
          <option value="estimatedMinutes">Sort by Duration</option>
        </select>
        <div className="flex gap-1">
          <Button variant={view === 'grid' ? 'default' : 'outline'} size="icon" onClick={() => setView('grid')}><Grid className="h-4 w-4" /></Button>
          <Button variant={view === 'list' ? 'default' : 'outline'} size="icon" onClick={() => setView('list')}><List className="h-4 w-4" /></Button>
        </div>
      </div>

      {isLoading ? (
        <div className={cn('grid gap-4', view === 'grid' ? 'md:grid-cols-2 lg:grid-cols-3' : '')}>
          {Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} className="h-40" />)}
        </div>
      ) : (
        <div className={cn(view === 'grid' ? 'grid gap-4 md:grid-cols-2 lg:grid-cols-3' : 'space-y-3')}>
          {data?.content.map((topic) => (
            <Link key={topic.id} to={`/topics/${topic.id}`}>
              <Card className="h-full hover:shadow-md transition-shadow cursor-pointer">
                <CardHeader className={view === 'list' ? 'flex-row items-center justify-between space-y-0 pb-2' : ''}>
                  <div>
                    <CardTitle className="text-base">{topic.title}</CardTitle>
                    <CardDescription>{topic.categoryName}</CardDescription>
                  </div>
                  <div className="flex gap-2 mt-2">
                    <Badge variant={difficultyColor[topic.difficulty] || 'secondary'}>{topic.difficulty}</Badge>
                    {topic.progressStatus !== 'NOT_STARTED' && (
                      <Badge variant={topic.progressStatus === 'COMPLETED' ? 'success' : 'default'}>{topic.progressStatus}</Badge>
                    )}
                  </div>
                </CardHeader>
                <CardContent>
                  <p className="text-sm text-[var(--color-muted-foreground)] line-clamp-2">{topic.description}</p>
                  <p className="mt-2 text-xs text-[var(--color-muted-foreground)]">{topic.estimatedMinutes} min</p>
                </CardContent>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
