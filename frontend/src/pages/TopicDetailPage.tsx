import { useState, useEffect, useMemo } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  ArrowLeft, Clock, CheckCircle, Sparkles, BookOpen,
  Bot, Trophy, ChevronLeft, ChevronRight, RefreshCw, AlertCircle
} from 'lucide-react'
import { api } from '@/lib/api'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Markdown } from '@/components/ui/Markdown'

export function TopicDetailPage() {
  const { id } = useParams<{ id: string }>()
  const topicId = Number(id)
  const queryClient = useQueryClient()
  const navigate = useNavigate()

  // Stepper state: 0 = Warm Up, 1 = Study Lesson, 2 = Quiz Quest, 3 = Feynman Sandbox
  const [currentStep, setCurrentStep] = useState(0)

  // Step 1: Slides state
  const [activeSlide, setActiveSlide] = useState(0)

  // Step 2: Quiz state
  const [quiz, setQuiz] = useState<any>(null)
  const [loadingQuiz, setLoadingQuiz] = useState(false)
  const [currentQuestionIdx, setCurrentQuestionIdx] = useState(0)
  const [answeredQuestions, setAnsweredQuestions] = useState<Record<number, string>>({})

  // Step 3: Feynman Sandbox state
  const [warmUpText, setWarmUpText] = useState('')
  const [feynmanText, setFeynmanText] = useState('')
  const [evaluating, setEvaluating] = useState(false)
  const [evaluationResult, setEvaluationResult] = useState<{ feedback: string; score: number } | null>(null)

  const { data: topic, isLoading } = useQuery({
    queryKey: ['topic', topicId],
    queryFn: () => api.topics.get(topicId),
    enabled: !!topicId,
  })

  const startMutation = useMutation({
    mutationFn: () => api.learning.start(topicId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['topic', topicId] })
      setCurrentStep(1) // Advance to lesson slides
    },
  })

  const completeMutation = useMutation({
    mutationFn: (data: { warmUpText?: string; quizScore?: number; feynmanSubmission?: string; feynmanScore?: number; feynmanFeedback?: string }) =>
      api.learning.complete(topicId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['topic', topicId] })
      queryClient.invalidateQueries({ queryKey: ['dashboard'] })
      navigate('/')
    },
  })

  // Chunk topic content into slides based on markdown headings
  const slides = useMemo(() => {
    if (!topic || !topic.content) return []
    const parts = topic.content.split(/(?=##+ )/)
    if (parts.length <= 1) {
      // Split by double newlines into 3 pages if no headers exist
      const paragraphs = topic.content.split('\n\n').filter(Boolean)
      const chunkSize = Math.max(1, Math.ceil(paragraphs.length / 3))
      const result: string[] = []
      for (let i = 0; i < paragraphs.length; i += chunkSize) {
        result.push(paragraphs.slice(i, i + chunkSize).join('\n\n'))
      }
      return result.map((content, idx) => ({
        title: idx === 0 ? "Overview" : idx === 1 ? "Deep Dive" : "Real-World Application",
        content
      }))
    }
    return parts.map((part) => {
      const match = part.match(/##+\s+(.*)/)
      const title = match ? match[1].trim() : "General Concepts"
      const content = part.replace(/##+\s+.*/, '').trim()
      return { title, content }
    })
  }, [topic])

  // Trigger quiz load when entering quiz step
  useEffect(() => {
    if (currentStep === 2 && !quiz) {
      loadQuiz()
    }
  }, [currentStep])

  const loadQuiz = async () => {
    setLoadingQuiz(true)
    try {
      const q = await api.quizzes.generate(topicId)
      setQuiz(q)
      setCurrentQuestionIdx(0)
      setAnsweredQuestions({})
    } catch (e) {
      console.error("Failed to load quiz", e)
    } finally {
      setLoadingQuiz(false)
    }
  }

  const handleWarmUpSubmit = () => {
    if (!warmUpText.trim()) return
    startMutation.mutate()
  }

  const handleFeynmanSubmit = async () => {
    if (!feynmanText.trim() || evaluating) return
    setEvaluating(true)
    try {
      const res = await api.ai.feynmanEval(topicId, feynmanText)
      setEvaluationResult(res)
    } catch (e) {
      console.error(e)
    } finally {
      setEvaluating(false)
    }
  }

  const getMedal = (score: number) => {
    if (score >= 90) return { name: 'Gold Medal', color: 'text-amber-500 bg-amber-50 border-amber-200' }
    if (score >= 75) return { name: 'Silver Medal', color: 'text-slate-400 bg-slate-50 border-slate-200' }
    if (score >= 50) return { name: 'Bronze Medal', color: 'text-amber-700 bg-amber-50 border-amber-200' }
    return { name: 'Participant Badge', color: 'text-indigo-500 bg-indigo-50 border-indigo-200' }
  }

  const calculateQuizScore = () => {
    if (!quiz || !quiz.questions) return 0
    let correct = 0
    quiz.questions.forEach((q: any, idx: number) => {
      if (answeredQuestions[idx] === q.answer) {
        correct++
      }
    })
    return quiz.questions.length > 0 ? (correct * 100) / quiz.questions.length : 0
  }

  if (isLoading) return <Skeleton className="h-96" />
  if (!topic) return <p>Topic not found</p>

  const stepsList = ['Warm Up', 'Lesson Slides', 'Quiz Challenge', 'Feynman Sandbox']

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <Link to="/topics" className="inline-flex items-center gap-1 text-sm text-[var(--color-muted-foreground)] hover:text-[var(--color-foreground)]">
        <ArrowLeft className="h-4 w-4" /> Back to Topics
      </Link>

      {/* Header Info */}
      <div className="flex flex-wrap items-start justify-between gap-4 border-b pb-4">
        <div>
          <h1 className="text-2xl font-bold flex items-center gap-2">
            {topic.title}
          </h1>
          <p className="text-sm text-[var(--color-muted-foreground)]">{topic.categoryName} · {topic.difficulty}</p>
        </div>
        <div className="flex items-center gap-2">
          <Badge variant="secondary"><Clock className="mr-1 h-3 w-3" />{topic.estimatedMinutes} min</Badge>
          <Badge variant={topic.progressStatus === 'COMPLETED' ? 'success' : 'outline'}>{topic.progressStatus}</Badge>
        </div>
      </div>

      {/* Interactive Quest Stepper Header */}
      <div className="grid grid-cols-4 gap-2 text-center text-xs font-semibold">
        {stepsList.map((step, idx) => (
          <div
            key={step}
            className={`border-b-2 pb-2 transition-colors duration-200 ${
              currentStep === idx
                ? 'border-[var(--color-primary)] text-[var(--color-primary)] font-bold'
                : currentStep > idx
                ? 'border-emerald-500 text-emerald-600'
                : 'border-muted text-[var(--color-muted-foreground)]'
            }`}
          >
            {idx + 1}. {step}
          </div>
        ))}
      </div>

      {/* STEP 0: Warm Up */}
      {currentStep === 0 && (
        <Card className="border-indigo-100 bg-indigo-50/20">
          <CardHeader>
            <CardTitle className="text-base flex items-center gap-2 text-indigo-900">
              <Sparkles className="h-5 w-5 text-indigo-500" />
              Step 1: Priming Your Brain
            </CardTitle>
            <CardDescription className="text-indigo-800/80">
              Before reading the lesson, take a guess: what do you think this concept is about, or what do you hope to learn? Active guessing primes your brain to absorb details.
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <textarea
              className="w-full min-h-[100px] rounded-lg border bg-background p-3 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]"
              placeholder="Type your initial thoughts here (e.g. 'I think generics allows writing class schemas without locking the parameters type'...) "
              value={warmUpText}
              onChange={(e) => setWarmUpText(e.target.value)}
            />
            <div className="flex justify-end">
              <Button onClick={handleWarmUpSubmit} disabled={!warmUpText.trim() || startMutation.isPending}>
                Start Lesson Quest <ChevronRight className="ml-1 h-4 w-4" />
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* STEP 1: Lesson Slides */}
      {currentStep === 1 && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-[var(--color-muted-foreground)]">
              Slide {activeSlide + 1} of {slides.length}
            </h3>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setActiveSlide((s) => Math.max(0, s - 1))}
                disabled={activeSlide === 0}
              >
                <ChevronLeft className="h-4 w-4" /> Prev
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setActiveSlide((s) => Math.min(slides.length - 1, s + 1))}
                disabled={activeSlide === slides.length - 1}
              >
                Next <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </div>

          <Card className="min-h-[300px]">
            <CardHeader className="bg-[var(--color-accent)]/30 border-b">
              <CardTitle className="text-base font-bold flex items-center gap-2">
                <BookOpen className="h-4 w-4 text-[var(--color-primary)]" />
                {slides[activeSlide]?.title}
              </CardTitle>
            </CardHeader>
            <CardContent className="p-6">
              <Markdown content={slides[activeSlide]?.content} />
            </CardContent>
          </Card>

          <div className="flex justify-between items-center">
            <Button variant="ghost" onClick={() => setCurrentStep(0)}>
              <ChevronLeft className="mr-1 h-4 w-4" /> Warm Up
            </Button>
            {activeSlide === slides.length - 1 ? (
              <Button onClick={() => setCurrentStep(2)} className="bg-emerald-600 hover:bg-emerald-700">
                Unlock Quiz Quest <ChevronRight className="ml-1 h-4 w-4" />
              </Button>
            ) : (
              <Button onClick={() => setActiveSlide((s) => s + 1)}>
                Next Slide <ChevronRight className="ml-1 h-4 w-4" />
              </Button>
            )}
          </div>
        </div>
      )}

      {/* STEP 2: Quiz Quest */}
      {currentStep === 2 && (
        <div className="space-y-4">
          {loadingQuiz && <Skeleton className="h-72" />}
          
          {!loadingQuiz && quiz && (
            <Card>
              <CardHeader className="border-b">
                <CardTitle className="text-base flex items-center justify-between">
                  <span>Question {currentQuestionIdx + 1} of {quiz.questions.length}</span>
                  <Badge variant="outline" className="text-emerald-600 border-emerald-200 bg-emerald-50">Quiz Quest</Badge>
                </CardTitle>
              </CardHeader>
              <CardContent className="p-6 space-y-6">
                {/* Render Question */}
                <p className="font-semibold text-base">{quiz.questions[currentQuestionIdx]?.question}</p>

                {/* Render Options */}
                <div className="grid gap-2">
                  {quiz.questions[currentQuestionIdx]?.options.map((opt: string) => {
                    const isCorrectOption = opt === quiz.questions[currentQuestionIdx]?.answer
                    const isSelected = answeredQuestions[currentQuestionIdx] === opt
                    const hasAnswered = answeredQuestions[currentQuestionIdx] !== undefined

                    let btnClass = "border text-left p-3 rounded-lg text-sm transition-all duration-200 cursor-pointer "
                    if (hasAnswered) {
                      if (isCorrectOption) {
                        btnClass += "bg-emerald-500 text-white border-emerald-500"
                      } else if (isSelected) {
                        btnClass += "bg-red-500 text-white border-red-500"
                      } else {
                        btnClass += "opacity-50 border-muted"
                      }
                    } else {
                      btnClass += "hover:bg-[var(--color-accent)] border-muted"
                    }

                    return (
                      <button
                        key={opt}
                        className={btnClass}
                        disabled={hasAnswered}
                        onClick={() => setAnsweredQuestions((prev) => ({ ...prev, [currentQuestionIdx]: opt }))}
                      >
                        {opt}
                      </button>
                    )
                  })}
                </div>

                {/* Explanation Card */}
                {answeredQuestions[currentQuestionIdx] !== undefined && (
                  <div className="rounded-lg bg-indigo-50 border border-indigo-100 p-4 text-indigo-900 flex items-start gap-2 animate-fadeIn">
                    <Bot className="h-5 w-5 text-indigo-500 shrink-0 mt-0.5" />
                    <div className="space-y-1">
                      <p className="text-xs font-semibold text-indigo-900">Tutor Feedback</p>
                      <p className="text-xs leading-relaxed">
                        {answeredQuestions[currentQuestionIdx] === quiz.questions[currentQuestionIdx]?.answer
                          ? "Correct! Excellent grasp of the concept."
                          : `Incorrect. The correct answer was "${quiz.questions[currentQuestionIdx]?.answer}". Check your notes and slides again.`}
                      </p>
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          )}

          <div className="flex justify-between items-center">
            <Button variant="ghost" onClick={() => setCurrentStep(1)}>
              <ChevronLeft className="mr-1 h-4 w-4" /> Lesson Slides
            </Button>
            
            {answeredQuestions[currentQuestionIdx] !== undefined && (
              currentQuestionIdx < (quiz?.questions?.length - 1) ? (
                <Button onClick={() => setCurrentQuestionIdx((i) => i + 1)}>
                  Next Question <ChevronRight className="ml-1 h-4 w-4" />
                </Button>
              ) : (
                <Button onClick={() => setCurrentStep(3)} className="bg-emerald-600 hover:bg-emerald-700">
                  Go to Feynman Sandbox <ChevronRight className="ml-1 h-4 w-4" />
                </Button>
              )
            )}
          </div>
        </div>
      )}

      {/* STEP 3: Feynman Sandbox */}
      {currentStep === 3 && (
        <Card className="border-amber-100 bg-amber-50/10">
          <CardHeader>
            <CardTitle className="text-base flex items-center gap-2 text-amber-900">
              <Trophy className="h-5 w-5 text-amber-500" />
              Step 4: The Feynman Sandbox
            </CardTitle>
            <CardDescription className="text-amber-800/80">
              Complete the quest by explaining the concept in your own words. Explain it simply — as if you were teaching it to a 5-year-old. The AI tutor will grade and score your explanation!
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <textarea
              className="w-full min-h-[120px] rounded-lg border bg-background p-3 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]"
              placeholder="Explain it in your own words..."
              value={feynmanText}
              onChange={(e) => setFeynmanText(e.target.value)}
              disabled={evaluating || !!evaluationResult}
            />

            {!evaluationResult ? (
              <div className="flex justify-end gap-2">
                <Button variant="outline" onClick={() => setCurrentStep(2)}>
                  <ChevronLeft className="mr-1 h-4 w-4" /> Quiz Quest
                </Button>
                <Button onClick={handleFeynmanSubmit} disabled={!feynmanText.trim() || evaluating}>
                  {evaluating ? 'AI Evaluating...' : 'Submit to Tutor'}
                </Button>
              </div>
            ) : (
              <div className="space-y-4 border-t pt-4">
                {/* Result score board */}
                <div className="flex items-center gap-4">
                  <div className={`border rounded-lg p-3 flex flex-col items-center justify-center min-w-[100px] ${getMedal(evaluationResult.score).color}`}>
                    <Trophy className="h-6 w-6" />
                    <span className="text-xs font-bold mt-1 text-center">{getMedal(evaluationResult.score).name}</span>
                  </div>
                  <div>
                    <p className="text-sm font-semibold">Tutor Score</p>
                    <p className="text-3xl font-extrabold text-[var(--color-primary)]">{evaluationResult.score}/100</p>
                  </div>
                </div>

                {/* AI feedback text */}
                <div className="rounded-lg bg-[var(--color-accent)]/30 p-4 border text-xs leading-relaxed space-y-2">
                  <p className="font-semibold text-sm flex items-center gap-1"><AlertCircle className="h-4 w-4 text-[var(--color-primary)]" /> Tutor Critique</p>
                  <Markdown content={evaluationResult.feedback} />
                </div>

                <div className="flex justify-end gap-2">
                  <Button variant="outline" onClick={() => setEvaluationResult(null)} disabled={completeMutation.isPending}>
                    <RefreshCw className="mr-1 h-3 w-3" /> Try Again
                  </Button>
                  <Button
                    onClick={() =>
                      completeMutation.mutate({
                        warmUpText,
                        quizScore: calculateQuizScore(),
                        feynmanSubmission: feynmanText,
                        feynmanScore: evaluationResult?.score ?? 0,
                        feynmanFeedback: evaluationResult?.feedback ?? '',
                      })
                    }
                    disabled={completeMutation.isPending}
                  >
                    Complete Quest & Save <CheckCircle className="ml-1 h-4 w-4" />
                  </Button>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  )
}
