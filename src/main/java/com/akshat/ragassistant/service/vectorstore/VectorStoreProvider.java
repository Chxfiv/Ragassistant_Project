package com.akshat.ragassistant.service.vectorstore;

import java.util.List;

public interface VectorStoreProvider {

    void storeEmbedding(
            Long chunkId,
            List<Float> embedding
    );
}