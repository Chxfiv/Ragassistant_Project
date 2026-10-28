package com.akshat.ragassistant.service;
import com.akshat.ragassistant.dto.Citation;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.akshat.ragassistant.dto.ChatResponse;
import com.akshat.ragassistant.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private static final Logger log =
            LoggerFactory.getLogger(ChatService.class);
    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;
    private final DocumentRepository documentRepository;


    public ChatResponse askQuestion(String question, Long documentId) {


                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new RuntimeException("No document found with id: " + documentId));

        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(5)
                        .build()
        );

        if(documents == null || documents.isEmpty()){
            return new ChatResponse(
                    "No relevant information found.",
                    List.of()
            );
        }

        log.info("Retrieved {} relevant chunks.", documents.size());

        for (Document doc : documents) {
            System.out.println("--------------------------------");
            System.out.println(doc.getText());
        }
        List<Citation> citations = new ArrayList<>();

        StringBuilder context = new StringBuilder();

        for (Document document : documents) {

            context.append(document.getText())
                    .append("\n\n");

            citations.add(

                    new Citation(

                            Long.parseLong(
                                    document.getMetadata()
                                            .get("chunkId")
                                            .toString()
                            ),

                            document.getMetadata()
                                    .get("fileName")
                                    .toString(),

                            Integer.parseInt(
                                    document.getMetadata()
                                            .get("chunkIndex")
                                            .toString()
                            )

                    )

            );

        }

        String prompt = """
                You are an AI assistant.

                Answer ONLY using the context provided below.

                If the answer is not present in the context, say:
                "I don't know based on the uploaded documents."

                Context:
                %s

                Question:
                %s
                """.formatted(context, question);

        String answer = chatClientBuilder.build()
                .prompt(prompt)
                .call()
                .content();

        if (answer == null) {
            answer = "No answer generated.";
        }

        return new ChatResponse(
                answer,
                citations
        );
    }

 }