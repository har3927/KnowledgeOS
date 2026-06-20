import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { BookOpen, CheckCircle, Clock, Flame, RotateCcw } from 'lucide-react'
import { api } from '@/lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts'

const COLORS = ['#6366f1', '#8b5cf6', '#a78bfa', '#c4b5fd', '#ddd6fe']

function StatCard({ title, value, icon: Icon, color }: { title: string; value: number; icon: React.ElementType; color: string }) {
  return (
    <Card>
      <CardContent className="flex items-center gap-4 p-6">
        <div className={`rounded-lg p-3 ${color}`}>
          <Icon className="h-5 w-5 text-white" />
        </div>
        <div>
          <p className="text-sm text-[var(--color-muted-foreground)]">{title}</p>
          <p className="text-2xl font-bold">{value}</p>
        </div>
      </CardContent>
    </Card>
  )
}

export function DashboardPage() {
  const { data, isLoading } = useQuery({ queryKey: ['dashboard'], queryFn: api.learning.dashboard })

  if (isLoading) {
    return (
      <div className="space-y-6">
        <Skeleton className="h-8 w-48" />
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-24" />)}
        </div>
      </div>
    )
  }

  if (!data) return null

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <p className="text-[var(--color-muted-foreground)]">Your learning overview</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard title="Topics Completed" value={data.topicsCompleted} icon={CheckCircle} color="bg-emerald-500" />
        <StatCard title="In Progress" value={data.topicsInProgress} icon={Clock} color="bg-blue-500" />
        <StatCard title="Revisions Due" value={data.revisionsDue} icon={RotateCcw} color="bg-amber-500" />
        <StatCard title="Learning Streak" value={data.currentStreak} icon={Flame} color="bg-orange-500" />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Today&apos;s Recommendation</CardTitle>
            <CardDescription>Your personalized learning suggestion</CardDescription>
          </CardHeader>
          <CardContent>
            {data.todayRecommendation?.topicId ? (
              <div className="space-y-3">
                <div className="flex items-center gap-2">
                  <BookOpen className="h-5 w-5 text-[var(--color-primary)]" />
                  <span className="font-medium">{data.todayRecommendation.topicTitle}</span>
                  <Badge variant="secondary">{data.todayRecommendation.difficulty}</Badge>
                </div>
                <p className="text-sm text-[var(--color-muted-foreground)]">{data.todayRecommendation.reason}</p>
                <Button asChild>
                  <Link to={`/topics/${data.todayRecommendation.topicId}`}>Start Learning</Link>
                </Button>
              </div>
            ) : (
              <p className="text-sm text-[var(--color-muted-foreground)]">{data.todayRecommendation?.reason || 'No recommendations yet'}</p>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Due Revisions</CardTitle>
            <CardDescription>Topics to review today</CardDescription>
          </CardHeader>
          <CardContent>
            {data.dueRevisions.length === 0 ? (
              <p className="text-sm text-[var(--color-muted-foreground)]">No revisions due today</p>
            ) : (
              <ul className="space-y-2">
                {data.dueRevisions.map((r) => (
                  <li key={r.id} className="flex items-center justify-between rounded-md border p-3">
                    <span className="text-sm font-medium">{r.topicTitle}</span>
                    <Badge variant="warning">Level {r.revisionLevel}</Badge>
                  </li>
                ))}
              </ul>
            )}
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Continue Learning</CardTitle>
        </CardHeader>
        <CardContent>
          {data.continueLearning.length === 0 ? (
            <p className="text-sm text-[var(--color-muted-foreground)]">No topics in progress. Browse topics to get started!</p>
          ) : (
            <div className="grid gap-3 md:grid-cols-2">
              {data.continueLearning.map((p) => (
                <Link key={p.id} to={`/topics/${p.topicId}`} className="flex items-center justify-between rounded-md border p-4 hover:bg-[var(--color-accent)] transition-colors">
                  <div>
                    <p className="font-medium">{p.topicTitle}</p>
                    <p className="text-xs text-[var(--color-muted-foreground)]">{p.categoryName}</p>
                  </div>
                  <Badge variant="secondary">{p.status}</Badge>
                </Link>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader><CardTitle>Category Progress</CardTitle></CardHeader>
          <CardContent className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={data.categoryProgress.filter(c => c.total > 0)} dataKey="completed" nameKey="categoryName" cx="50%" cy="50%" outerRadius={80} label>
                  {data.categoryProgress.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle>Weekly Activity</CardTitle></CardHeader>
          <CardContent className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={data.weeklyActivity}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="day" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="topicsCompleted" fill="#6366f1" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
