package sn.devbrik.backend.brique.controller;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import sn.devbrik.backend.brique.dto.LangageDto;
import sn.devbrik.backend.brique.repository.LangageRepository;

import java.util.List;

/**
 * GET /api/langages → public (SecurityConfig : permitAll).
 */
@RestController
@RequestMapping("/api/langages")
public class LangageController {

    private final LangageRepository repo;

    public LangageController(LangageRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<LangageDto> lister() {
        return repo.findAll(Sort.by("nom")).stream().map(LangageDto::de).toList();
    }
}
