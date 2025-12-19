package com.yxh.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeaderboardEntry {
    private Long id;
    private Long seasonId;
    private Long userId;
    private Integer totalScore;
    private Integer hitCount;
    private String videoCoverUrl;
    private Integer rankManual;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String videoUrl;
}