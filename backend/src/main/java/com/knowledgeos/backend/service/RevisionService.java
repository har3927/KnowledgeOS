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
    public Dtos.RevisionDto completeRevision(Long revisionId) {
        RevisionSchedule revision = revisionRepository.findById(revisionId)
                .orElseThrow(() -> new ResourceNotFoundException("Revision not found: " + revisionId));
        revision.setCompleted(true);
        return mapper.toRevisionDto(revisionRepository.save(revision));
    }
}
