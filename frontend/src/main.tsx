import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { AppLayout } from '@/components/layout/AppLayout'
import { DashboardPage } from '@/pages/DashboardPage'
import { TopicsPage } from '@/pages/TopicsPage'
import { TopicDetailPage } from '@/pages/TopicDetailPage'
import { LearningPathsPage } from '@/pages/LearningPathsPage'
import { TutorPage } from '@/pages/TutorPage'
import { RevisionsPage } from '@/pages/RevisionsPage'
import { KnowledgeGraphPage } from '@/pages/KnowledgeGraphPage'
import { ProgressPage } from '@/pages/ProgressPage'
import { SettingsPage } from '@/pages/SettingsPage'
import './index.css'

const queryClient = new QueryClient({
  defaultOptions: { queries: { staleTime: 30_000, retry: 1 } },
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route element={<AppLayout />}>
            <Route index element={<DashboardPage />} />
            <Route path="topics" element={<TopicsPage />} />
            <Route path="topics/:id" element={<TopicDetailPage />} />
            <Route path="learning-paths" element={<LearningPathsPage />} />
            <Route path="tutor" element={<TutorPage />} />
            <Route path="revisions" element={<RevisionsPage />} />
            <Route path="graph" element={<KnowledgeGraphPage />} />
            <Route path="progress" element={<ProgressPage />} />
            <Route path="settings" element={<SettingsPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>
)
