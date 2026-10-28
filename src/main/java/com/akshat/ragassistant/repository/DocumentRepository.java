package com.akshat.ragassistant.repository;

import com.akshat.ragassistant.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository
        extends JpaRepository<Document, Long> {
}