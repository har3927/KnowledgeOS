import { useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Clock, Play, CheckCircle, Sparkles, FileText } from 'lucide-react'
import { api } from '@/lib/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'

export function TopicDetailPage() {
  const { id } = useParams<{ id: string }>()
  const topicId = Number(id)
  const queryClient = useQueryClient()
  const [summary, setSummary] = useState<string | null>(null)
  const [quiz, setQuiz] = useState<import('@/types').Quiz | null>(null)
  const [answers, setAnswers] = useState<Record<number, string>>({})
  const [quizResult, setQuizResult] = useState<import('@/types').QuizResult | null>(null)

  const { data: topic, isLoading } = useQuery({
    queryKey: ['topic', topicId],
    queryFn: () => api.topics.get(topicId),
    enabled: !!topicId,
  })

  const startMutation = useMutation({
    mutationFn: () => api.learning.start(topicId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['topic', topicId] }),
  })

  const completeMutation = useMutation({
    mutationFn: () => api.learning.complete(topicId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['topic', topicId] })
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
    },
  })

  const handleSummary = async () => {
    const res = await api.ai.summary(topicId)
    setSummary(res.summary)
  }

  const handleQuiz = async () => {
    const q = await api.quizzes.generate(topicId)
    setQuiz(q)
    setQuizResult(null)
    setAnswers({})
  }

  const handleSubmitQuiz = async () => {
    if (!quiz) return
    const result = await api.quizzes.submit(
      quiz.id,
      Object.entries(answers).map(([questionId, answer]) => ({ questionId: Number(questionId), answer }))
    )
    setQuizResult(result)
  }

  if (isLoading) return <Skeleton className="h-96" />
  if (!topic) return <p>Topic not found</p>

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <Link to="/topics" className="inline-flex items-center gap-1 text-sm text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]">
        <ArrowLeft className="h-4 w-4" /> Back to Topics
      </Link>

      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold">{topic.title}</h1>
          <p className="text-[var(--color-muted-foreground)]">{topic.categoryName}</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Badge>{topic.difficulty}</Badge>
          <Badge variant="secondary"><Clock className="mr-1 h-3 w-3" />{topic.estimatedMinutes} min</Badge>
          <Badge variant={topic.progressStatus === 'COMPLETED' ? 'success' : 'outline'}>{topic.progressStatus}</Badge>
        </div>
      </div>

      <div className="flex flex-wrap gap-2">
        <Button onClick={() => startMutation.mutate()} disabled={startMutation.isPending}>
          <Play className="mr-1 h-4 w-4" /> Start Learning
        </Button>
        <Button variant="secondary" onClick={() => completeMutation.mutate()} disabled={completeMutation.isPending}>
          <CheckCircle className="mr-1 h-4 w-4" /> Mark Complete
        </Button>
        <Button variant="outline" onClick={handleSummary}>
          <FileText className="mr-1 h-4 w-4" /> Generate Summary
        </Button>
        <Button variant="outline" onClick={handleQuiz}>
          <Sparkles className="mr-1 h-4 w-4" /> Generate Quiz
        </Button>
      </div>

      {topic.prerequisites.length > 0 && (
        <Card>
          <CardHeader><CardTitle className="text-base">Prerequisites</CardTitle></CardHeader>
          <CardContent className="flex flex-wrap gap-2">
            {topic.prerequisites.map((p) => (
              <Link key={p.id} to={`/topics/${p.id}`}>
                <Badge variant="outline">{p.title}</Badge>
              </Link>
            ))}
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader><CardTitle>Content</CardTitle></CardHeader>
        <CardContent>
          <p className="whitespace-pre-wrap leading-relaxed">{topic.content}</p>
        </CardContent>
      </Card>

      {summary && (
        <Card>
          <CardHeader><CardTitle>AI Summary</CardTitle></CardHeader>
          <CardContent><p className="whitespace-pre-wrap text-sm">{summary}</p></CardContent>
        </Card>
      )}

      {quiz && (
        <Card>
          <CardHeader><CardTitle>{quiz.title}</CardTitle></CardHeader>
          <CardContent className="space-y-6">
            {quiz.questions.map((q) => (
              <div key={q.id} className="space-y-2">
                <p className="font-medium">{q.question}</p>
                <div className="space-y-1">
                  {q.options.map((opt) => (
                    <label key={opt} className="flex items-center gap-2 rounded-md border p-2 cursor-pointer hover:bg-[var(--color-accent)]">
                      <input
                        type="radio"
                        name={`q-${q.id}`}
                        value={opt}
                        checked={answers[q.id] === opt}
                        onChange={() => setAnswers((a) => ({ ...a, [q.id]: opt }))}
                      />
                      <span className="text-sm">{opt}</span>
                    </label>
                  ))}
                </div>
              </div>
            ))}
            {!quizResult ? (
              <Button onClick={handleSubmitQuiz}>Submit Quiz</Button>
            ) : (
              <div className="rounded-md bg-emerald-50 p-4 text-emerald-800">
                Score: {quizResult.score.toFixed(0)}% ({quizResult.correctCount}/{quizResult.totalQuestions} correct)
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  )
}
