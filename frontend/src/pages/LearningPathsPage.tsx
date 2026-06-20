import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { Sparkles } from 'lucide-react'
import { api } from '@/lib/api'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'

export function LearningPathsPage() {
  const { data: paths, isLoading } = useQuery({ queryKey: ['learning-paths'], queryFn: api.learningPaths.list })
  const [goal, setGoal] = useState('')
  const [level, setLevel] = useState('intermediate')
  const [aiPath, setAiPath] = useState<string | null>(null)
  const [generating, setGenerating] = useState(false)

  const handleGenerate = async () => {
    if (!goal.trim()) return
    setGenerating(true)
    try {
      const res = await api.ai.learningPath(goal, level)
      setAiPath(res.path)
    } finally {
      setGenerating(false)
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Learning Paths</h1>
        <p className="text-[var(--color-muted-foreground)]">Structured journeys through topics</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2"><Sparkles className="h-5 w-5" /> AI Generated Path</CardTitle>
          <CardDescription>Describe your learning goal and get a personalized path</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <Input placeholder="e.g. Become a senior Java developer" value={goal} onChange={(e) => setGoal(e.target.value)} />
          <select className="h-9 rounded-md border px-3 text-sm" value={level} onChange={(e) => setLevel(e.target.value)}>
            <option value="beginner">Beginner</option>
            <option value="intermediate">Intermediate</option>
            <option value="advanced">Advanced</option>
          </select>
          <Button onClick={handleGenerate} disabled={generating}>{generating ? 'Generating...' : 'Generate Path'}</Button>
          {aiPath && <div className="rounded-md border p-4 text-sm whitespace-pre-wrap">{aiPath}</div>}
        </CardContent>
      </Card>

      {isLoading ? (
        <div className="grid gap-4 md:grid-cols-2">{Array.from({ length: 4 }).map((_, i) => <Skeleton key={i} className="h-48" />)}</div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {paths?.map((path) => (
            <Card key={path.id}>
              <CardHeader>
                <div className="flex items-center justify-between">
                  <CardTitle>{path.name}</CardTitle>
                  <Badge>{path.progressPercentage.toFixed(0)}%</Badge>
                </div>
                <CardDescription>{path.description}</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="mb-3 h-2 rounded-full bg-[var(--color-muted)]">
                  <div className="h-2 rounded-full bg-[var(--color-primary)]" style={{ width: `${path.progressPercentage}%` }} />
                </div>
                <ol className="space-y-2">
                  {path.topics.map((t) => (
                    <li key={t.topicId} className="flex items-center gap-2 text-sm">
                      <span className="flex h-6 w-6 items-center justify-center rounded-full bg-[var(--color-muted)] text-xs">{t.sequenceNo}</span>
                      <Link to={`/topics/${t.topicId}`} className="hover:underline">{t.title}</Link>
                      <Badge variant={t.status === 'COMPLETED' ? 'success' : 'outline'} className="ml-auto text-xs">{t.status}</Badge>
                    </li>
                  ))}
                </ol>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
