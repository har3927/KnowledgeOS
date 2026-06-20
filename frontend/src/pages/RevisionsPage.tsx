import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { api } from '@/lib/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'

export function RevisionsPage() {
  const queryClient = useQueryClient()

  const { data: due, isLoading: dueLoading } = useQuery({ queryKey: ['revisions-due'], queryFn: api.revisions.due })
  const { data: upcoming, isLoading: upLoading } = useQuery({ queryKey: ['revisions-upcoming'], queryFn: api.revisions.upcoming })
  const { data: completed, isLoading: compLoading } = useQuery({ queryKey: ['revisions-completed'], queryFn: api.revisions.completed })

  const completeMutation = useMutation({
    mutationFn: api.revisions.complete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['revisions-due'] })
      queryClient.invalidateQueries({ queryKey: ['revisions-completed'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })

  const RevisionList = ({ items, showComplete }: { items: import('@/types').Revision[]; showComplete?: boolean }) => (
    <div className="space-y-2">
      {items.length === 0 ? (
        <p className="text-sm text-[var(--color-muted-foreground)]">None</p>
      ) : (
        items.map((r) => (
          <div key={r.id} className="flex items-center justify-between rounded-md border p-3">
            <div>
              <Link to={`/topics/${r.topicId}`} className="font-medium hover:underline">{r.topicTitle}</Link>
              <p className="text-xs text-[var(--color-muted-foreground)]">{r.nextRevisionDate} · Level {r.revisionLevel}</p>
            </div>
            {showComplete && (
              <Button size="sm" onClick={() => completeMutation.mutate(r.id)} disabled={completeMutation.isPending}>
                Complete
              </Button>
            )}
            {!showComplete && <Badge variant="secondary">Level {r.revisionLevel}</Badge>}
          </div>
        ))
      )}
    </div>
  )

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Revisions</h1>
        <p className="text-[var(--color-muted-foreground)]">Spaced repetition schedule</p>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        <Card>
          <CardHeader><CardTitle className="text-base">Due Today</CardTitle></CardHeader>
          <CardContent>{dueLoading ? <Skeleton className="h-20" /> : <RevisionList items={due || []} showComplete />}</CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle className="text-base">Upcoming</CardTitle></CardHeader>
          <CardContent>{upLoading ? <Skeleton className="h-20" /> : <RevisionList items={upcoming || []} />}</CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle className="text-base">Completed</CardTitle></CardHeader>
          <CardContent>{compLoading ? <Skeleton className="h-20" /> : <RevisionList items={completed || []} />}</CardContent>
        </Card>
      </div>
    </div>
  )
}
