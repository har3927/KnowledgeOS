package com.knowledgeos.backend.controller;

import com.knowledgeos.backend.dto.Dtos;
import com.knowledgeos.backend.service.RevisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/revisions")
@RequiredArgsConstructor
@Tag(name = "Revisions")
public class RevisionController {

    private final RevisionService revisionService;

    @GetMapping("/due")
    @Operation(summary = "Get revisions due today")
    public List<Dtos.RevisionDto> getDueToday() {
        return revisionService.getDueToday();
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming revisions")
    public List<Dtos.RevisionDto> getUpcoming() {
        return revisionService.getUpcoming();
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed revisions")
    public List<Dtos.RevisionDto> getCompleted() {
        return revisionService.getCompleted();
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete a revision")
    public Dtos.RevisionDto completeRevision(@PathVariable Long id) {
        return revisionService.completeRevision(id);
    }
}
