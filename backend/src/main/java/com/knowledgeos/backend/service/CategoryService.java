package com.knowledgeos.backend.service;

import com.knowledgeos.backend.dto.Dtos;
import com.knowledgeos.backend.entity.Category;
import com.knowledgeos.backend.exception.ResourceNotFoundException;
import com.knowledgeos.backend.mapper.EntityMapper;
import com.knowledgeos.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final EntityMapper mapper;

    public List<Dtos.CategoryDto> getAll() {
        return categoryRepository.findAll().stream()
                .map(mapper::toCategoryDto)
                .toList();
    }

    @Transactional
    public Dtos.CategoryDto create(Dtos.CategoryCreateRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return mapper.toCategoryDto(categoryRepository.save(category));
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }
}
