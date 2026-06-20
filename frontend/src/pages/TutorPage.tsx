import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Send, Bot } from 'lucide-react'
import { api } from '@/lib/api'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'

export function TutorPage() {
  const [question, setQuestion] = useState('')
  const [loading, setLoading] = useState(false)
  const [latestAnswer, setLatestAnswer] = useState<string | null>(null)

  const { data: history, refetch } = useQuery({
    queryKey: ['conversations'],
    queryFn: api.ai.conversations,
  })

  const handleAsk = async () => {
    if (!question.trim()) return
    setLoading(true)
    try {
      const res = await api.ai.tutor(question)
      setLatestAnswer(res.answer)
      setQuestion('')
      refetch()
    } finally {
      setLoading(false)
    }
  }

  const suggestions = [
    'Explain Java Streams with examples',
    'What should I learn after Spring Boot?',
    'How do I prepare for system design interviews?',
    'Give me career advice for a backend developer',
  ]

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold">AI Tutor</h1>
        <p className="text-[var(--color-muted-foreground)]">Ask anything about your learning journey</p>
      </div>

      <Card>
        <CardContent className="p-4">
          <div className="flex gap-2">
            <Input
              placeholder="Ask your question..."
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleAsk()}
            />
            <Button onClick={handleAsk} disabled={loading}>
              <Send className="h-4 w-4" />
            </Button>
          </div>
          <div className="mt-3 flex flex-wrap gap-2">
            {suggestions.map((s) => (
              <button key={s} onClick={() => setQuestion(s)} className="rounded-full border px-3 py-1 text-xs hover:bg-[var(--color-accent)] transition-colors">
                {s}
              </button>
            ))}
          </div>
        </CardContent>
      </Card>

      {latestAnswer && (
        <Card>
          <CardHeader><CardTitle className="flex items-center gap-2 text-base"><Bot className="h-4 w-4" /> Latest Response</CardTitle></CardHeader>
          <CardContent><p className="whitespace-pre-wrap text-sm leading-relaxed">{latestAnswer}</p></CardContent>
        </Card>
      )}

      <div className="space-y-3">
        <h2 className="text-lg font-semibold">Conversation History</h2>
        {!history ? (
          <Skeleton className="h-32" />
        ) : history.length === 0 ? (
          <p className="text-sm text-[var(--color-muted-foreground)]">No conversations yet. Ask your first question!</p>
        ) : (
          history.map((c) => (
            <Card key={c.id}>
              <CardContent className="space-y-2 p-4">
                <p className="text-sm font-medium">{c.prompt}</p>
                <p className="text-sm text-[var(--color-muted-foreground)] whitespace-pre-wrap">{c.response}</p>
                <p className="text-xs text-[var(--color-muted-foreground)]">{new Date(c.createdAt).toLocaleString()}</p>
              </CardContent>
            </Card>
          ))
        )}
      </div>
    </div>
  )
}
