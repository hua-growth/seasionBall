package com.yxh.dto;

import lombok.Data;

@Data
public class LeaderboardDTO {
    private Long id;
    private Long seasonId;
    private String seasonName;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Integer totalScore;
    private Integer hitCount;
    private String videoCoverUrl;
    private Integer rankManual;
    private Double hitRate;
    private String videoUrl;

    public Double getHitRate() {
        if (totalScore == null || totalScore == 0) return 0.0;
        return (double) hitCount / totalScore * 100;
    }
}