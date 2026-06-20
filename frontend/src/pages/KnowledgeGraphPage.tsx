import { useEffect, useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  ReactFlow, Background, Controls, MiniMap,
  useNodesState, useEdgesState, type Node, type Edge,
} from '@xyflow/react'
import '@xyflow/react/dist/style.css'
import { api } from '@/lib/api'
import { Skeleton } from '@/components/ui/skeleton'

const statusColors: Record<string, string> = {
  COMPLETED: '#10b981',
  IN_PROGRESS: '#6366f1',
  NOT_STARTED: '#94a3b8',
}

export function KnowledgeGraphPage() {
  const { data, isLoading } = useQuery({ queryKey: ['graph'], queryFn: api.graph.get })

  const initialNodes: Node[] = useMemo(() => {
    if (!data) return []
    const cols = 6
    return data.nodes.map((n, i) => ({
      id: n.id,
      data: { label: n.label, category: n.category, difficulty: n.difficulty, status: n.status },
      position: { x: (i % cols) * 220, y: Math.floor(i / cols) * 100 },
      style: {
        background: statusColors[n.status] || '#94a3b8',
        color: 'white',
        border: 'none',
        borderRadius: 8,
        padding: '8px 12px',
        fontSize: 11,
        width: 180,
      },
    }))
  }, [data])

  const initialEdges: Edge[] = useMemo(() => {
    if (!data) return []
    return data.edges.map((e) => ({
      id: e.id,
      source: e.source,
      target: e.target,
      animated: true,
      style: { stroke: '#6366f1' },
    }))
  }, [data])

  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes)
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges)

  useEffect(() => {
    setNodes(initialNodes)
    setEdges(initialEdges)
  }, [initialNodes, initialEdges, setNodes, setEdges])

  if (isLoading) return <Skeleton className="h-[600px]" />

  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-2xl font-bold">Knowledge Graph</h1>
        <p className="text-[var(--color-muted-foreground)]">Visual map of topics and prerequisites</p>
      </div>
      <div className="flex gap-4 text-xs">
        <span className="flex items-center gap-1"><span className="h-3 w-3 rounded-full bg-emerald-500" /> Completed</span>
        <span className="flex items-center gap-1"><span className="h-3 w-3 rounded-full bg-indigo-500" /> In Progress</span>
        <span className="flex items-center gap-1"><span className="h-3 w-3 rounded-full bg-slate-400" /> Not Started</span>
      </div>
      <div className="h-[600px] rounded-lg border">
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          fitView
        >
          <Background />
          <Controls />
          <MiniMap />
        </ReactFlow>
      </div>
    </div>
  )
}
