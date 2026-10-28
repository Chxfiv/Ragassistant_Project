package com.akshat.ragassistant.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import com.akshat.ragassistant.dto.DocumentResponse;
import com.akshat.ragassistant.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(
        name = "Document APIs",
        description = "Upload and manage PDF documents"
)
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "Get all uploaded documents")
    @GetMapping
    public List<DocumentResponse> getAllDocuments() {
        return documentService.getAllDocuments();
    }


    @Operation(
            summary = "Upload a PDF",
            description = "Uploads a PDF, extracts text, creates chunks, generates embeddings and stores them in Qdrant."
    )
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public String uploadFile(
            @RequestParam("file") MultipartFile file) {

        return documentService.uploadFile(file);
    }

}