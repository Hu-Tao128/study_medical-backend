package com.studymedical.backend.infrastructure.controllers;

import com.studymedical.backend.domain.entities.User;
import com.studymedical.backend.domain.entities.UserDocument;
import com.studymedical.backend.domain.services.EmbeddingNotConfiguredException;
import com.studymedical.backend.infrastructure.persistence.UserDocumentRepository;
import com.studymedical.backend.infrastructure.security.AuthenticatedUser;
import com.studymedical.backend.infrastructure.security.RoleAuthorizationService;
import com.studymedical.backend.infrastructure.services.DocumentIngestionService;
import com.studymedical.backend.infrastructure.services.DocumentQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints del RAG por usuario: subir documentos, listarlos y consultarlos con citas.
 */
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentIngestionService ingestionService;
    private final DocumentQueryService queryService;
    private final UserDocumentRepository userDocumentRepository;
    private final RoleAuthorizationService roleAuthorizationService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> upload(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestParam("file") MultipartFile file
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archivo vacío");
        }

        try {
            UserDocument document = ingestionService.ingest(
                    currentUser.getId(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getInputStream()
            );
            return ResponseEntity.ok(DocumentResponse.from(document));
        } catch (EmbeddingNotConfiguredException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo");
        }
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> list(
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);
        List<DocumentResponse> documents = userDocumentRepository.findByUserId(currentUser.getId())
                .stream()
                .map(DocumentResponse::from)
                .toList();
        return ResponseEntity.ok(documents);
    }

    @PostMapping("/{id}/query")
    public ResponseEntity<DocumentQueryResponse> query(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @PathVariable UUID id,
            @Valid @RequestBody QueryRequest request
    ) {
        User currentUser = roleAuthorizationService.requireAuthenticatedUser(principal);

        try {
            DocumentQueryService.DocumentAnswer answer = queryService.answer(
                    currentUser.getId(),
                    id,
                    request.question(),
                    request.topK()
            );
            return ResponseEntity.ok(DocumentQueryResponse.from(answer));
        } catch (EmbeddingNotConfiguredException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
        }
    }

    public record DocumentResponse(
            String id,
            String filename,
            String contentType,
            String status,
            int chunkCount,
            String errorMessage
    ) {
        static DocumentResponse from(UserDocument document) {
            return new DocumentResponse(
                    document.id().toString(),
                    document.filename(),
                    document.contentType(),
                    document.status().name(),
                    document.chunkCount(),
                    document.errorMessage()
            );
        }
    }

    public record QueryRequest(
            @NotBlank String question,
            Integer topK
    ) {
    }

    public record DocumentQueryResponse(String answer, List<Citation> citations) {
        static DocumentQueryResponse from(DocumentQueryService.DocumentAnswer result) {
            List<Citation> citations = result.citations().stream()
                    .map(citation -> new Citation(
                            citation.chunkIndex(),
                            citation.pageNumber(),
                            citation.similarity(),
                            citation.snippet()
                    ))
                    .toList();
            return new DocumentQueryResponse(result.answer(), citations);
        }

        public record Citation(int chunkIndex, Integer pageNumber, double similarity, String snippet) {
        }
    }
}
