import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export function SettingsPage() {
  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Settings</h1>
        <p className="text-[var(--color-muted-foreground)]">Configure your learning preferences</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Profile</CardTitle>
          <CardDescription>Your account information</CardDescription>
        </CardHeader>
        <CardContent className="space-y-2 text-sm">
          <p><span className="text-[var(--color-muted-foreground)]">Name:</span> Demo User</p>
          <p><span className="text-[var(--color-muted-foreground)]">Email:</span> demo@knowledgeos.dev</p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>AI Configuration</CardTitle>
          <CardDescription>Local LLM via Ollama (default model: qwen2.5:7b)</CardDescription>
        </CardHeader>
        <CardContent className="space-y-2 text-sm text-[var(--color-muted-foreground)]">
          <p>Ensure Ollama is running, then pull the model:</p>
          <code className="block rounded bg-[var(--color-muted)] px-2 py-1">ollama pull qwen2.5:7b</code>
          <p>Optional env vars: <code className="rounded bg-[var(--color-muted)] px-1">OLLAMA_MODEL</code>, <code className="rounded bg-[var(--color-muted)] px-1">OLLAMA_BASE_URL</code></p>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Spaced Repetition</CardTitle>
          <CardDescription>Revision intervals: Day 1, 3, 7, 30, 90</CardDescription>
        </CardHeader>
        <CardContent className="text-sm text-[var(--color-muted-foreground)]">
          Revisions are automatically scheduled when you complete a topic.
        </CardContent>
      </Card>
    </div>
  )
}
