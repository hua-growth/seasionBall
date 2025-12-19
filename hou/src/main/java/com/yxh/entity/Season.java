package com.yxh.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Season {
    private Long id;
    private String code;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}