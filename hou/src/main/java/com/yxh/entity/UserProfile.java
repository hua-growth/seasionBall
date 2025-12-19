package com.yxh.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfile {
    private Long id;
    private String nickname;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}