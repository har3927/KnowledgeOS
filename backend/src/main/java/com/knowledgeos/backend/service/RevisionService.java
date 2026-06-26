package com.knowledgeos.backend.service;

import com.knowledgeos.backend.dto.Dtos;
import com.knowledgeos.backend.entity.RevisionSchedule;
import com.knowledgeos.backend.exception.ResourceNotFoundException;
import com.knowledgeos.backend.mapper.EntityMapper;
import com.knowledgeos.backend.repository.RevisionScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RevisionService {

    private final RevisionScheduleRepository revisionRepository;
    private final UserContextService userContext;
    private final EntityMapper mapper;

    public List<Dtos.RevisionDto> getDueToday() {
        return revisionRepository.findDueToday(userContext.getCurrentUserId(), LocalDate.now()).stream()
                .map(mapper::toRevisionDto)
                .toList();
    }

    public List<Dtos.RevisionDto> getUpcoming() {
        return revisionRepository.findUpcoming(userContext.getCurrentUserId(), LocalDate.now()).stream()
                .map(mapper::toRevisionDto)
                .toList();
    }

    public List<Dtos.RevisionDto> getCompleted() {
        return revisionRepository.findCompleted(userContext.getCurrentUserId()).stream()
                .map(mapper::toRevisionDto)
                .toList();
    }

    @Transactional
    public Dtos.RevisionDto completeRevision(Long revisionId, String rating) {
        RevisionSchedule revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Revision not found: " + revisionId));
        revision.setCompleted(true);
        RevisionSchedule saved = revisionRepository.save(revision);

        int currentLevel = revision.getRevisionLevel();
        if (currentLevel < SpacedRepetitionService.REVISION_INTERVALS.length) {
            int nextLevel = currentLevel + 1;
            int days = SpacedRepetitionService.REVISION_INTERVALS[nextLevel - 1];

            if ("again".equalsIgnoreCase(rating)) {
                nextLevel = 1;
                days = 1;
            } else if ("easy".equalsIgnoreCase(rating)) {
                days = days * 2;
            }

            // Check if it doesn't already exist to avoid duplication
            if (!revisionRepository.existsByUserIdAndTopicIdAndRevisionLevel(
                    revision.getUser().getId(), revision.getTopic().getId(), nextLevel)) {
                RevisionSchedule nextRevision = RevisionSchedule.builder()
                        .user(revision.getUser())
                        .topic(revision.getTopic())
                        .nextRevisionDate(LocalDate.now().plusDays(days))
                        .revisionLevel(nextLevel)
                        .completed(false)
                        .build();
                revisionRepository.save(nextRevision);
            }
        }

        return mapper.toRevisionDto(saved);
    }
}
