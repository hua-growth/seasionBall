package com.yxh.dto;

import lombok.Data;

/**
 * 添加排行榜条目请求DTO
 */
@Data
public class AddLeaderboardEntryDTO {
    /**
     * 赛季代码（如：v1, v2, v3）
     */
    private String seasonCode;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户头像URL（可选）
     */
    private String avatarUrl;
    
    /**
     * 总出手数
     */
    private Integer totalScore;
    
    /**
     * 命中数
     */
    private Integer hitCount;
    
    /**
     * 视频封面URL（可选）
     */
    private String videoCoverUrl;
    
    /**
     * 视频播放URL（可选）
     */
    private String videoUrl;
}

