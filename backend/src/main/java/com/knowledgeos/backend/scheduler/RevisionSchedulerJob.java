package com.knowledgeos.backend.scheduler;

import com.knowledgeos.backend.config.AppProperties;
import com.knowledgeos.backend.entity.RevisionSchedule;
import com.knowledgeos.backend.service.SpacedRepetitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevisionSchedulerJob {

    private final SpacedRepetitionService spacedRepetitionService;
    private final AppProperties appProperties;

    @Scheduled(cron = "0 0 8 * * *")
    public void processDueRevisions() {
        Long userId = appProperties.getDefaultUserId();
        List<RevisionSchedule> due = spacedRepetitionService.findDueToday(userId);
        log.info("Daily revision check: {} revisions due for user {}", due.size(), userId);
        due.forEach(r -> log.info("Revision due: topic {} level {} on {}",
                r.getTopic().getTitle(), r.getRevisionLevel(), r.getNextRevisionDate()));
    }
}
