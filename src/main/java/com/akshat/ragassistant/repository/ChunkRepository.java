package com.akshat.ragassistant.repository;

import com.akshat.ragassistant.entity.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChunkRepository
        extends JpaRepository<Chunk, Long> {
}
