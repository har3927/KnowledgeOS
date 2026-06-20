package com.knowledgeos.backend.service;

import com.knowledgeos.backend.config.AppProperties;
import com.knowledgeos.backend.entity.User;
import com.knowledgeos.backend.exception.ResourceNotFoundException;
import com.knowledgeos.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserContextService {

    private final UserRepository userRepository;
    private final AppProperties appProperties;

    public User getCurrentUser() {
        return userRepository.findById(appProperties.getDefaultUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Default user not found"));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
