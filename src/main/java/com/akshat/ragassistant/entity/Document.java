package com.akshat.ragassistant.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @OneToMany(
            mappedBy = "document",
            cascade = CascadeType.ALL
    )
    private List<Chunk> chunks;
}