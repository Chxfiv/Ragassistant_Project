package com.akshat.ragassistant.service;
import com.akshat.ragassistant.dto.DocumentResponse;
import com.akshat.ragassistant.service.chunking.ChunkingStrategy;
import com.akshat.ragassistant.service.embedding.EmbeddingProvider;
import com.akshat.ragassistant.service.processor.DocumentProcessor;
import com.akshat.ragassistant.entity.Chunk;
import com.akshat.ragassistant.entity.Document;
import com.akshat.ragassistant.repository.ChunkRepository;
import com.akshat.ragassistant.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class
DocumentService {

    private static final Logger log =
            LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final DocumentProcessor documentProcessor;
    private final ChunkingStrategy chunkingStrategy;
    private final ChunkRepository chunkRepository;
    private final VectorStore vectorStore;

    public List<DocumentResponse> getAllDocuments() {

        return documentRepository.findAll()
                .stream()
                .map(document -> new DocumentResponse(
                        document.getId(),
                        document.getFileName()
                ))
                .toList();
    }

    public Document saveDocument(String fileName) {

        Document document = new Document();

        document.setFileName(fileName);

        return documentRepository.save(document);
    }


    public String uploadFile(MultipartFile file) {

        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException(
                        "Please select a non-empty PDF file.");
            }

            String originalFilename = file.getOriginalFilename();

            if (originalFilename == null
                    || !originalFilename.toLowerCase()
                    .endsWith(".pdf")) {
                throw new IllegalArgumentException(
                        "Only PDF files are supported.");
            }

            log.info("UPLOAD STEP 1: Saving PDF");
            Path filePath = saveUploadedFile(file);

            log.info("UPLOAD STEP 2: Extracting PDF text");
            List<String> chunks = extractAndChunk(filePath);

            if (chunks == null || chunks.isEmpty()) {
                throw new IllegalStateException(
                        "No chunks were created from the PDF.");
            }

            log.info("UPLOAD STEP 3: Saving document");
            Document document = createDocument(file);

            log.info("UPLOAD STEP 4: Saving chunks and preparing vectors");
            List<org.springframework.ai.document.Document> vectorDocuments =
                    saveChunksAndPrepareVectors(document, chunks);

            if (vectorDocuments.isEmpty()) {
                throw new IllegalStateException(
                        "No vector documents were created.");
            }

            log.info("UPLOAD STEP 5: Uploading vectors to Qdrant");
            uploadVectors(vectorDocuments);

            log.info("UPLOAD COMPLETE: documentId={}", document.getId());
            return "File uploaded successfully. Document ID: "
                    + document.getId();

        } catch (Exception e) {
            log.error("PDF UPLOAD FAILED", e);
            throw new RuntimeException("PDF upload failed: "
                    + e.getMessage(), e);
        }
    }

    private Path saveUploadedFile(MultipartFile file) throws IOException {

        Path uploadPath = Paths.get("uploads");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(file.getOriginalFilename());

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        log.info("PDF saved successfully.");

        return filePath;
    }


    private List<String> extractAndChunk(Path filePath) throws IOException{

        String text =
                documentProcessor.extractText(filePath.toString());

        log.info("PDF text extracted successfully.");

        List<String> chunks =
                chunkingStrategy.chunk(text, 2000);

        log.info("Total chunks created: {}", chunks.size());

        return chunks;
    }


    private Document createDocument(MultipartFile file) {

        Document document = new Document();

        document.setFileName(file.getOriginalFilename());

        return documentRepository.save(document);
    }


    private List<org.springframework.ai.document.Document> saveChunksAndPrepareVectors(
            Document document,
            List<String> chunks) {

        int chunkIndex = 0;

        List<org.springframework.ai.document.Document> vectorDocuments =
                new ArrayList<>();

        for (String chunkText : chunks) {

            log.info("Processing chunk {}", chunkIndex);

            Chunk chunk = new Chunk();

            chunk.setContent(chunkText);

            chunk.setChunkIndex(chunkIndex);

            chunk.setDocument(document);

            chunkRepository.save(chunk);

            org.springframework.ai.document.Document vectorDocument =
                    new org.springframework.ai.document.Document(
                            chunk.getContent()
                    );

            vectorDocument.getMetadata().put(
                    "chunkId",
                    chunk.getId()
            );

            vectorDocument.getMetadata().put(
                    "documentId",
                    document.getId()
            );

            vectorDocument.getMetadata().put(
                    "fileName",
                    document.getFileName()
            );

            vectorDocument.getMetadata().put(
                    "chunkIndex",
                    chunk.getChunkIndex()
            );

            vectorDocuments.add(vectorDocument);

            chunkIndex++;
        }

        log.debug("First chunk: {}", chunks.get(0));

        return vectorDocuments;
    }

    private void uploadVectors(
            List<org.springframework.ai.document.Document> vectorDocuments) {

        log.info("Uploading vectors to Qdrant...");

        long start = System.currentTimeMillis();

        vectorStore.add(vectorDocuments);

        long end = System.currentTimeMillis();

        log.info("Uploaded {} vectors to Qdrant.", vectorDocuments.size());

        log.info("Upload completed in {} ms", end - start);
    }


}