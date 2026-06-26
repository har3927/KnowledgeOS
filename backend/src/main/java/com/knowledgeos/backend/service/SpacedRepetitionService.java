package com.knowledgeos.backend.service;

import com.knowledgeos.backend.entity.RevisionSchedule;
import com.knowledgeos.backend.entity.Topic;
import com.knowledgeos.backend.entity.User;
import com.knowledgeos.backend.repository.RevisionScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpacedRepetitionService {

    public static final int[] REVISION_INTERVALS = {1, 3, 7, 30, 90};

    private final RevisionScheduleRepository revisionRepository;

    @Transactional
    public void scheduleRevisions(User user, Topic topic) {
        LocalDate baseDate = LocalDate.now();
        int level = 1;
        if (!revisionRepository.existsByUserIdAndTopicIdAndRevisionLevel(user.getId(), topic.getId(), level)) {
            RevisionSchedule schedule = RevisionSchedule.builder()
                    .user(user)
                    .topic(topic)
                    .nextRevisionDate(baseDate.plusDays(REVISION_INTERVALS[0]))
                    .revisionLevel(level)
                    .completed(false)
                    .build();
            revisionRepository.save(schedule);
            log.info("Scheduled initial revision level {} for topic {} on {}",
                    level, topic.getId(), schedule.getNextRevisionDate());
        }
    }

    public List<RevisionSchedule> findDueToday(Long userId) {
        return revisionRepository.findDueToday(userId, LocalDate.now());
    }
}
