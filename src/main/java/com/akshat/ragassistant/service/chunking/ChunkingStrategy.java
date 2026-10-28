package com.akshat.ragassistant.service.chunking;

import java.util.List;

public interface ChunkingStrategy {

    List<String> chunk(
            String text,
            int chunkSize
    );
}