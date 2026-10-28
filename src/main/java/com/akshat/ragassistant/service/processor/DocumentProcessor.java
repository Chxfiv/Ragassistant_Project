package com.akshat.ragassistant.service.processor;

import java.io.IOException;

public interface DocumentProcessor {

    String extractText(String filePath)
            throws IOException;
}