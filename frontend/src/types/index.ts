export interface Category {
  id: number
  name: string
  description: string
}

export interface Topic {
  id: number
  categoryId: number
  categoryName: string
  title: string
  description: string
  difficulty: string
  estimatedMinutes: number
  content: string
  createdAt: string
  prerequisites: TopicSummary[]
  progressStatus: string
}

export interface TopicSummary {
  id: number
  title: string
  difficulty: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface Progress {
  id: number
  topicId: number
  topicTitle: string
  categoryName: string
  status: string
  score: number | null
  startedAt: string | null
  completedAt: string | null
}

export interface Dashboard {
  topicsCompleted: number
  topicsInProgress: number
  revisionsDue: number
  currentStreak: number
  todayRecommendation: Recommendation | null
  dueRevisions: Revision[]
  continueLearning: Progress[]
  categoryProgress: CategoryProgress[]
  weeklyActivity: WeeklyActivity[]
}

export interface CategoryProgress {
  categoryName: string
  completed: number
  total: number
  percentage: number
}

export interface WeeklyActivity {
  day: string
  topicsCompleted: number
}

export interface Recommendation {
  topicId?: number
  topicTitle?: string
  categoryName?: string
  difficulty?: string
  reason?: string
}

export interface Revision {
  id: number
  topicId: number
  topicTitle: string
  nextRevisionDate: string
  revisionLevel: number
  completed: boolean
}

export interface LearningPath {
  id: number
  name: string
  description: string
  topics: LearningPathTopic[]
  progressPercentage: number
}

export interface LearningPathTopic {
  topicId: number
  title: string
  sequenceNo: number
  status: string
}

export interface Quiz {
  id: number
  topicId: number
  title: string
  generatedByAi: boolean
  questions: QuizQuestion[]
}

export interface QuizQuestion {
  id: number
  question: string
  options: string[]
  answer?: string
}

export interface QuizResult {
  attemptId: number
  score: number
  correctCount: number
  totalQuestions: number
}

export interface Conversation {
  id: number
  prompt: string
  response: string
  createdAt: string
}

export interface GraphNode {
  id: string
  label: string
  category: string
  difficulty: string
  status: string
}

export interface GraphEdge {
  id: string
  source: string
  target: string
}

export interface Graph {
  nodes: GraphNode[]
  edges: GraphEdge[]
}

export interface ProgressSummary {
  completionPercentage: number
  totalTopics: number
  completedTopics: number
  inProgressTopics: number
  currentStreak: number
  categoryBreakdown: CategoryProgress[]
  quizPerformance: { quizTitle: string; score: number; attemptedAt: string }[]
}
