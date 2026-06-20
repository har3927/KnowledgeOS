package com.knowledgeos.backend.controller;

import com.knowledgeos.backend.dto.Dtos;
import com.knowledgeos.backend.service.KnowledgeGraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/graph")
@RequiredArgsConstructor
@Tag(name = "Knowledge Graph")
public class KnowledgeGraphController {

    private final KnowledgeGraphService graphService;

    @GetMapping
    @Operation(summary = "Get knowledge graph nodes and edges")
    public Dtos.GraphDto getGraph() {
        return graphService.getGraph();
    }

    @GetMapping("/nodes")
    @Operation(summary = "Get graph nodes")
    public Dtos.GraphDto getNodes() {
        return graphService.getGraph();
    }

    @GetMapping("/edges")
    @Operation(summary = "Get graph edges")
    public Dtos.GraphDto getEdges() {
        return graphService.getGraph();
    }
}
