package com.akshat.ragassistant.service.embedding;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OllamaEmbeddingProvider
        implements EmbeddingProvider {

    private final EmbeddingModel embeddingModel;

    @Override
    public List<Float> generateEmbedding(String text) {

        float[] embedding =
                embeddingModel.embed(text);

        List<Float> result =
                new ArrayList<>();

        for(float value : embedding){
            result.add(value);
        }

        return result;
    }
}