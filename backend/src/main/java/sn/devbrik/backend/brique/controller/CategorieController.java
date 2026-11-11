package sn.devbrik.backend.brique.controller;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import sn.devbrik.backend.brique.dto.CategorieDto;
import sn.devbrik.backend.brique.repository.CategorieRepository;

import java.util.List;

/**
 * GET /api/categories → public (SecurityConfig : permitAll).
 */
@RestController
@RequestMapping("/api/categories")
public class CategorieController {

    private final CategorieRepository repo;

    public CategorieController(CategorieRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<CategorieDto> lister() {
        return repo.findAll(Sort.by("nom")).stream().map(CategorieDto::de).toList();
    }
}
