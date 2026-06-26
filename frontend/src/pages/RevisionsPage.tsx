import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { api } from '@/lib/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { CheckCircle2, Eye } from 'lucide-react'
import { cn } from '@/lib/utils'

export function RevisionsPage() {
  const queryClient = useQueryClient()
  const [activeIdx, setActiveIdx] = useState(0)
  const [isFlipped, setIsFlipped] = useState(false)
  const [summaryText, setSummaryText] = useState('')
  const [loadingSummary, setLoadingSummary] = useState(false)

  const { data: due, isLoading: dueLoading } = useQuery({ queryKey: ['revisions-due'], queryFn: api.revisions.due })
  const { data: upcoming, isLoading: upLoading } = useQuery({ queryKey: ['revisions-upcoming'], queryFn: api.revisions.upcoming })
  const { data: completed, isLoading: compLoading } = useQuery({ queryKey: ['revisions-completed'], queryFn: api.revisions.completed })

  const dueLength = due?.length || 0

  const completeMutation = useMutation({
    mutationFn: ({ id, rating }: { id: number; rating: string }) => api.revisions.complete(id, rating),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['revisions-due'] })
      queryClient.invalidateQueries({ queryKey: ['revisions-completed'] })
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      setIsFlipped(false)
      setSummaryText('')
    },
  })

  const handleFlip = async (topicId: number) => {
    if (!isFlipped) {
      setIsFlipped(true)
      if (!summaryText) {
        setLoadingSummary(true)
        try {
          const res = await api.ai.summary(topicId)
          setSummaryText(res.summary)
        } catch (e) {
          setSummaryText('Failed to load topic summary.')
        } finally {
          setLoadingSummary(false)
        }
      }
    } else {
      setIsFlipped(false)
    }
  }

  const handleRate = (id: number, rating: string) => {
    completeMutation.mutate({ id, rating })
    if (activeIdx >= dueLength - 1) {
      setActiveIdx(0)
    }
  }

  const activeCard = due && due.length > 0 ? due[activeIdx] : null

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Revisions Deck</h1>
        <p className="text-[var(--color-muted-foreground)] text-sm">Spaced repetition deck powered by Leitner system</p>
      </div>

      <div className="grid gap-6 lg:grid-cols-3">
        {/* Active Study Deck Column */}
        <div className="lg:col-span-2 space-y-4">
          <Card className="border-indigo-100 bg-indigo-50/10">
            <CardHeader className="border-b">
              <CardTitle className="text-base flex items-center justify-between">
                <span>Active Review Deck</span>
                {due && due.length > 0 && (
                  <Badge className="bg-[var(--color-primary)] text-white">
                    {activeIdx + 1} of {due.length} cards due
                  </Badge>
                )}
              </CardTitle>
            </CardHeader>
            <CardContent className="p-6 flex flex-col items-center justify-center space-y-6 min-h-[420px]">
              {dueLoading ? (
                <Skeleton className="w-full h-80" />
              ) : !activeCard ? (
                <div className="text-center py-10 space-y-3">
                  <CheckCircle2 className="h-12 w-12 text-emerald-500 mx-auto animate-bounce" />
                  <h3 className="font-bold text-lg">Deck Completed!</h3>
                  <p className="text-xs text-[var(--color-muted-foreground)] max-w-xs">
                    You have finished all revisions due today. Keep up the streak to lock concepts in!
                  </p>
                </div>
              ) : (
                <div className="w-full flex flex-col items-center space-y-6">
                  {/* Card Container with Flip Animation */}
                  <div
                    className="w-full max-w-md h-80 relative cursor-pointer [perspective:1000px]"
                    onClick={() => handleFlip(activeCard.topicId)}
                  >
                    <div
                      className={cn(
                        "w-full h-full rounded-2xl border transition-all duration-500 [transform-style:preserve-3d]",
                        isFlipped ? "[transform:rotateY(180deg)]" : ""
                      )}
                    >
                      {/* Card Front */}
                      <div className="absolute inset-0 w-full h-full rounded-2xl bg-gradient-to-br from-indigo-600 to-indigo-800 text-white flex flex-col items-center justify-center p-8 [backface-visibility:hidden] shadow-lg">
                        <Badge className="bg-white/20 text-white border-none text-xs">Level {activeCard.revisionLevel}</Badge>
                        <h2 className="text-2xl font-black mt-6 text-center leading-tight tracking-wide">{activeCard.topicTitle}</h2>
                        <span className="text-xs text-white/60 mt-10 flex items-center gap-1">
                          <Eye className="h-3 w-3" /> Click card to flip and view summary
                        </span>
                      </div>

                      {/* Card Back */}
                      <div className="absolute inset-0 w-full h-full rounded-2xl bg-card text-card-foreground border border-indigo-100 flex flex-col p-6 [backface-visibility:hidden] [transform:rotateY(180deg)] shadow-lg overflow-y-auto">
                        <div className="flex items-center justify-between border-b pb-2 mb-3">
                          <span className="text-xs font-bold text-[var(--color-primary)]">AI Lesson Summary</span>
                          <Badge variant="outline">Level {activeCard.revisionLevel}</Badge>
                        </div>
                        {loadingSummary ? (
                          <div className="space-y-2 mt-4">
                            <Skeleton className="h-4 w-full" />
                            <Skeleton className="h-4 w-[90%]" />
                            <Skeleton className="h-4 w-[95%]" />
                            <Skeleton className="h-4 w-[80%]" />
                          </div>
                        ) : (
                          <p className="text-xs leading-relaxed whitespace-pre-wrap text-[var(--color-muted-foreground)]">
                            {summaryText || "Flip card to load summary context."}
                          </p>
                        )}
                        <span className="text-xs text-indigo-400 mt-auto text-center pt-2">Click to view front face</span>
                      </div>
                    </div>
                  </div>

                  {/* Leitner Scoring buttons (visible when flipped) */}
                  {isFlipped && (
                    <div className="flex items-center justify-center gap-2 w-full max-w-md animate-fadeIn">
                      <Button
                        className="bg-red-500 hover:bg-red-600 text-white text-xs flex-1 h-10 cursor-pointer"
                        onClick={() => handleRate(activeCard.id, 'again')}
                        disabled={completeMutation.isPending}
                      >
                        Again (Level 1)
                      </Button>
                      <Button
                        className="bg-indigo-600 hover:bg-indigo-700 text-white text-xs flex-1 h-10 cursor-pointer"
                        onClick={() => handleRate(activeCard.id, 'good')}
                        disabled={completeMutation.isPending}
                      >
                        Good (+Level)
                      </Button>
                      <Button
                        className="bg-emerald-600 hover:bg-emerald-700 text-white text-xs flex-1 h-10 cursor-pointer"
                        onClick={() => handleRate(activeCard.id, 'easy')}
                        disabled={completeMutation.isPending}
                      >
                        Easy (Double Spacing)
                      </Button>
                    </div>
                  )}

                  {/* Navigation controls if multiple due */}
                  {dueLength > 1 && (
                    <div className="flex gap-4">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => {
                          setActiveIdx((i) => (i === 0 ? dueLength - 1 : i - 1))
                          setIsFlipped(false)
                          setSummaryText('')
                        }}
                      >
                        Prev Card
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => {
                          setActiveIdx((i) => (i === dueLength - 1 ? 0 : i + 1))
                          setIsFlipped(false)
                          setSummaryText('')
                        }}
                      >
                        Next Card
                      </Button>
                    </div>
                  )}
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Sidebar Lists Column (Upcoming / Completed) */}
        <div className="space-y-4">
          <Card>
            <CardHeader className="bg-[var(--color-accent)]/20 border-b">
              <CardTitle className="text-sm font-semibold">Upcoming Schedules</CardTitle>
            </CardHeader>
            <CardContent className="p-4 overflow-y-auto max-h-[220px]">
              {upLoading ? (
                <Skeleton className="h-10 w-full" />
              ) : !upcoming || upcoming.length === 0 ? (
                <p className="text-xs text-[var(--color-muted-foreground)]">No upcoming revisions scheduled.</p>
              ) : (
                <div className="space-y-2">
                  {upcoming.map((r) => (
                    <div key={r.id} className="flex items-center justify-between border-b pb-2 text-xs">
                      <div>
                        <Link to={`/topics/${r.topicId}`} className="font-semibold hover:underline">
                          {r.topicTitle}
                        </Link>
                        <p className="text-[10px] text-[var(--color-muted-foreground)]">Due: {r.nextRevisionDate}</p>
                      </div>
                      <Badge variant="secondary">Level {r.revisionLevel}</Badge>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="bg-[var(--color-accent)]/20 border-b">
              <CardTitle className="text-sm font-semibold">Archived / Completed</CardTitle>
            </CardHeader>
            <CardContent className="p-4 overflow-y-auto max-h-[220px]">
              {compLoading ? (
                <Skeleton className="h-10 w-full" />
              ) : !completed || completed.length === 0 ? (
                <p className="text-xs text-[var(--color-muted-foreground)]">No completed revision milestones.</p>
              ) : (
                <div className="space-y-2">
                  {completed.map((r) => (
                    <div key={r.id} className="flex items-center justify-between border-b pb-2 text-xs">
                      <div>
                        <Link to={`/topics/${r.topicId}`} className="font-semibold hover:underline">
                          {r.topicTitle}
                        </Link>
                        <p className="text-[10px] text-[var(--color-muted-foreground)]">Archived Level {r.revisionLevel}</p>
                      </div>
                      <Badge variant="success">Completed</Badge>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
