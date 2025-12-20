package com.yxh.vo;

import lombok.Data;

/**
 * 排行榜行数据VO（用于MyBatis查询结果映射）
 */
@Data
public class LeaderboardRowVO {
    private Integer rank;         // 排名（对应xml中的rank_val）
    private String nickname;      // 用户昵称
    private Integer totalScore;   // 总分数
    private Integer finishCount;  // 命中次数（对应xml中的hit_count）
    private String avatarUrl;     // 头像URL
    private String videoCoverUrl; // 视频封面URL
    private String videoUrl;      // 视频播放地址（对应xml中的video_url）
}
