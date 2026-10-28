package com.akshat.ragassistant.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.akshat.ragassistant.dto.ChatRequest;
import com.akshat.ragassistant.dto.ChatResponse;
import com.akshat.ragassistant.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(
        name = "Chat APIs",
        description = "Ask questions about uploaded PDF documents"
)
public class ChatController {

    private final ChatService chatService;

    @Operation(
            summary = "Ask a question",
            description = "Performs semantic search on the uploaded document and generates an answer using Llama 3.2."
    )
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request){

        return chatService.askQuestion(
                request.getQuestion(),
                request.getDocumentId()
        );
    }

}