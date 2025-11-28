package cz.cvut.fel.ear.controller;

import cz.cvut.fel.ear.dto.CategoryCreationDTO;
import cz.cvut.fel.ear.dto.CategoryDTO;
import cz.cvut.fel.ear.model.Category;
import cz.cvut.fel.ear.service.BoardGameService;
import cz.cvut.fel.ear.service.CategoryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCategory(@RequestBody CategoryCreationDTO categoryCreationDTO) {
        Long id = categoryService.addCategory(categoryCreationDTO.name());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Location", "api/categories/" + id);
        return new ResponseEntity<>("New category created",responseHeaders, HttpStatus.CREATED);

    }

    @PutMapping("/{categoryId}/games/{boardGameId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addBoardGameToCategory(@PathVariable Long categoryId, @PathVariable Long boardGameId) {
        categoryService.addBoardGameToCategory(boardGameId, categoryId);
        return new ResponseEntity<>("Board Game added to Category", HttpStatus.OK);
    }

    @DeleteMapping("/{categoryId}/games/{boardGameId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeBoardGameFromCategory(@PathVariable Long categoryId, @PathVariable Long boardGameId) {
        categoryService.removeGameFromCategory(boardGameId, categoryId);
        return new ResponseEntity<>("Board Game removed from Category", HttpStatus.NO_CONTENT);
    }

    @GetMapping("/")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> new CategoryDTO(category.getId(), category.getName()))
                .toList();

        return new ResponseEntity<>(categoryDTOs, HttpStatus.OK);
    }
}
