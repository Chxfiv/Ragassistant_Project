package com.akshat.ragassistant.service.embedding;

import java.util.List;

public interface EmbeddingProvider {

    List<Float> generateEmbedding(
            String text
    );
}
