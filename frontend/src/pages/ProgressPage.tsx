import { useQuery } from '@tanstack/react-query'
import { Flame } from 'lucide-react'
import { api } from '@/lib/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'

export function ProgressPage() {
  const { data, isLoading } = useQuery({ queryKey: ['progress-summary'], queryFn: api.learning.summary })

  if (isLoading) return <Skeleton className="h-96" />
  if (!data) return null

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Progress</h1>
        <p className="text-[var(--color-muted-foreground)]">Track your learning journey</p>
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardContent className="p-6 text-center">
            <p className="text-3xl font-bold text-[var(--color-primary)]">{data.completionPercentage.toFixed(0)}%</p>
            <p className="text-sm text-[var(--color-muted-foreground)]">Completion</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6 text-center">
            <p className="text-3xl font-bold">{data.completedTopics}</p>
            <p className="text-sm text-[var(--color-muted-foreground)]">Completed</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6 text-center">
            <p className="text-3xl font-bold">{data.inProgressTopics}</p>
            <p className="text-sm text-[var(--color-muted-foreground)]">In Progress</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-6 text-center">
            <p className="text-3xl font-bold flex items-center justify-center gap-1">
              <Flame className="h-6 w-6 text-orange-500" />{data.currentStreak}
            </p>
            <p className="text-sm text-[var(--color-muted-foreground)]">Day Streak</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader><CardTitle>Category Breakdown</CardTitle></CardHeader>
        <CardContent className="h-72">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={data.categoryBreakdown}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="categoryName" tick={{ fontSize: 11 }} />
              <YAxis />
              <Tooltip />
              <Bar dataKey="completed" fill="#6366f1" name="Completed" radius={[4, 4, 0, 0]} />
              <Bar dataKey="total" fill="#e2e8f0" name="Total" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>
    </div>
  )
}
