package com.feedbacks.FeedbackSystem.controller;

import com.feedbacks.FeedbackSystem.DTO.ApiResponse;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.requestDTOs.InstitutionRequestDTO;
import com.feedbacks.FeedbackSystem.DTO.EntityDTO.responseDTOs.InstitutionResponseDTO;
import com.feedbacks.FeedbackSystem.service.interfaces.InstitutionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/institutions")
@Slf4j
public class InstitutionController {

    private final InstitutionService institutionService;

    public InstitutionController(InstitutionService institutionService) {
        this.institutionService = institutionService;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<InstitutionResponseDTO>> createInstitution(@Valid @RequestBody InstitutionRequestDTO dto) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Institution created successfully",
                        institutionService.createInstitution(dto)
                )
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{institutionId}")
    public ResponseEntity<ApiResponse<InstitutionResponseDTO>> getInstitution(@PathVariable Integer institutionId) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Institution fetched successfully",
                        institutionService.getInstitutionById(institutionId)
                )
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<InstitutionResponseDTO>>> getAllInstitutions() {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "All institutions fetched successfully",
                        institutionService.getAllInstitutions()
                )
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<ApiResponse<InstitutionResponseDTO>> updateInstitution(
            @Valid @RequestBody InstitutionRequestDTO dto) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "Institution updated successfully",
                        institutionService.updateInstitution(dto)
                )
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteInstitution() {
        institutionService.deleteInstitution();
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "Institution deleted successfully",
                        null
                )
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<InstitutionResponseDTO>>> search(@RequestParam String name) {
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true, "Institutions search results fetched",
                        institutionService.searchInstitutions(name)
                )
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{institutionId}/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> stats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", institutionService.totalUsers());
        stats.put("totalCourses", institutionService.totalCourses());
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Institution statistics fetched",
                        stats
                )
        );
    }
}
