const BASE = (import.meta as any).env?.VITE_API_URL || '/api'

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    ...options,
  })
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }))
    throw new Error(err.message || 'Request failed')
  }
  if (res.status === 204) return undefined as T
  return res.json()
}

export const api = {
  categories: {
    list: () => request<import('@/types').Category[]>('/categories'),
  },
  topics: {
    list: (params: Record<string, string | number | undefined> = {}) => {
      const qs = new URLSearchParams()
      Object.entries(params).forEach(([k, v]) => {
        if (v !== undefined && v !== '') qs.set(k, String(v))
      })
      return request<import('@/types').PageResponse<import('@/types').Topic>>(`/topics?${qs}`)
    },
    get: (id: number) => request<import('@/types').Topic>(`/topics/${id}`),
    generateRandom: (categoryHint?: string) => {
      const qs = new URLSearchParams()
      if (categoryHint) qs.set('categoryHint', categoryHint)
      return request<import('@/types').Topic>(`/topics/generate-random?${qs}`, { method: 'POST' })
    },
  },
  learning: {
    start: (topicId: number) => request<import('@/types').Progress>(`/learning/topics/${topicId}/start`, { method: 'POST' }),
    complete: (topicId: number, data?: { warmUpText?: string; quizScore?: number; feynmanSubmission?: string; feynmanScore?: number; feynmanFeedback?: string }) =>
      request<import('@/types').Progress>(`/learning/topics/${topicId}/complete`, {
        method: 'POST',
        body: data ? JSON.stringify(data) : undefined,
      }),
    progress: () => request<import('@/types').Progress[]>('/learning/progress'),
    dashboard: () => request<import('@/types').Dashboard>('/learning/dashboard'),
    summary: () => request<import('@/types').ProgressSummary>('/learning/progress/summary'),
  },
  revisions: {
    due: () => request<import('@/types').Revision[]>('/revisions/due'),
    upcoming: () => request<import('@/types').Revision[]>('/revisions/upcoming'),
    completed: () => request<import('@/types').Revision[]>('/revisions/completed'),
    complete: (id: number, rating?: string) => request<import('@/types').Revision>(`/revisions/${id}/complete?rating=${rating || ''}`, { method: 'POST' }),
  },
  quizzes: {
    generate: (topicId: number) => request<import('@/types').Quiz>(`/quizzes/generate/${topicId}`, { method: 'POST' }),
    get: (id: number) => request<import('@/types').Quiz>(`/quizzes/${id}`),
    submit: (id: number, answers: { questionId: number; answer: string }[]) =>
      request<import('@/types').QuizResult>(`/quizzes/${id}/submit`, {
        method: 'POST',
        body: JSON.stringify({ answers }),
      }),
  },
  ai: {
    tutor: (question: string, topicId?: number) =>
      request<{ answer: string; conversationId: number }>('/ai/tutor', {
        method: 'POST',
        body: JSON.stringify({ question, topicId }),
      }),
    conversations: () => request<import('@/types').Conversation[]>('/ai/conversations'),
    learningPath: (goal: string, level: string) =>
      request<{ path: string }>('/ai/learning-path', {
        method: 'POST',
        body: JSON.stringify({ goal, level }),
      }),
    summary: (topicId: number) => request<{ summary: string }>(`/ai/topics/${topicId}/summary`),
    explanation: (topicId: number) => request<{ explanation: string }>(`/ai/topics/${topicId}/explanation`),
    quiz: (topicId: number) => request<import('@/types').Quiz>(`/ai/quiz/${topicId}`, { method: 'POST' }),
    feynmanEval: (topicId: number, explanation: string) =>
      request<{ feedback: string; score: number }>(`/ai/topics/${topicId}/feynman-eval`, {
        method: 'POST',
        body: JSON.stringify({ explanation }),
      }),
  },
  graph: {
    get: () => request<import('@/types').Graph>('/graph'),
  },
  learningPaths: {
    list: () => request<import('@/types').LearningPath[]>('/learning-paths'),
  },
}
