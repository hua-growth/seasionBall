package com.yxh.service.impl;

import com.yxh.dto.AddLeaderboardEntryDTO;
import com.yxh.dto.LeaderboardDTO;
import com.yxh.entity.LeaderboardEntry;
import com.yxh.entity.Season;
import com.yxh.entity.UserProfile;
import com.yxh.mapper.LeaderboardEntryMapper;
import com.yxh.mapper.SeasonMapper;
import com.yxh.mapper.UserProfileMapper;
import com.yxh.service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    @Autowired
    private LeaderboardEntryMapper leaderboardEntryMapper;

    @Autowired
    private SeasonMapper seasonMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Override
    @Transactional
    public List<LeaderboardDTO> getLeaderboardBySeasonCode(String seasonCode) {
        // 先获取赛季信息
        Season season = seasonMapper.findByCode(seasonCode);
        if (season == null) {
            return List.of();
        }

        // 重新排序并更新排名，确保排名是最新的
        reorderLeaderboard(season.getId());

        // 然后查询已更新排名的数据
        List<LeaderboardEntry> entries = leaderboardEntryMapper.findBySeasonCode(seasonCode);
        return entries.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<LeaderboardDTO> getLeaderboardBySeasonId(Long seasonId) {
        // 重新排序并更新排名，确保排名是最新的
        reorderLeaderboard(seasonId);

        // 然后查询已更新排名的数据
        List<LeaderboardEntry> entries = leaderboardEntryMapper.findBySeasonId(seasonId);
        return entries.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String addOrUpdateLeaderboardEntry(AddLeaderboardEntryDTO dto) {
        // 1. 验证赛季代码
        Season season = seasonMapper.findByCode(dto.getSeasonCode());
        if (season == null) {
            throw new RuntimeException("赛季代码不存在: " + dto.getSeasonCode());
        }

        // 2. 验证必填字段
        if (dto.getNickname() == null || dto.getNickname().trim().isEmpty()) {
            throw new RuntimeException("用户昵称不能为空");
        }
        if (dto.getTotalScore() == null || dto.getTotalScore() < 0) {
            throw new RuntimeException("总出手数必须大于等于0");
        }
        if (dto.getHitCount() == null || dto.getHitCount() < 0) {
            throw new RuntimeException("命中数必须大于等于0");
        }
        if (dto.getHitCount() > dto.getTotalScore()) {
            throw new RuntimeException("命中数不能大于总出手数");
        }

        // 3. 查找或创建用户
        UserProfile user = userProfileMapper.findByNickname(dto.getNickname());
        if (user == null) {
            user = new UserProfile();
            user.setNickname(dto.getNickname());
            user.setAvatarUrl(dto.getAvatarUrl());
            userProfileMapper.insert(user);
        } else {
            // 如果提供了头像URL，更新用户头像
            if (dto.getAvatarUrl() != null && !dto.getAvatarUrl().trim().isEmpty()) {
                user.setAvatarUrl(dto.getAvatarUrl());
                userProfileMapper.update(user);
            }
        }

        // 4. 查找或创建排行榜条目
        LeaderboardEntry entry = leaderboardEntryMapper.findBySeasonIdAndUserId(season.getId(), user.getId());
        boolean isNew = (entry == null);

        if (entry == null) {
            entry = new LeaderboardEntry();
            entry.setSeasonId(season.getId());
            entry.setUserId(user.getId());
        }

        // 5. 更新排行榜条目数据
        entry.setTotalScore(dto.getTotalScore());
        entry.setHitCount(dto.getHitCount());
        if (dto.getVideoCoverUrl() != null) {
            entry.setVideoCoverUrl(dto.getVideoCoverUrl());
        }
        if (dto.getVideoUrl() != null) {
            entry.setVideoUrl(dto.getVideoUrl());
        }

        if (isNew) {
            leaderboardEntryMapper.insert(entry);
        } else {
            leaderboardEntryMapper.update(entry);
        }

        // 6. 重新排序该赛季的所有条目（按照命中个数降序，命中个数相同时按总出手数升序）
        reorderLeaderboard(season.getId());

        return isNew ? "成功添加排行榜条目" : "成功更新排行榜条目";
    }

    @Override
    @Transactional
    public String deleteLeaderboardEntry(Long entryId) {
        // 1. 查找条目
        LeaderboardEntry entry = leaderboardEntryMapper.findById(entryId);
        if (entry == null) {
            throw new IllegalArgumentException("排行榜条目不存在");
        }

        Long seasonId = entry.getSeasonId();

        // 2. 删除条目
        leaderboardEntryMapper.deleteById(entryId);

        // 3. 重新排序该赛季的所有条目
        reorderLeaderboard(seasonId);

        return "成功删除排行榜条目";
    }

    /**
     * 重新排序指定赛季的排行榜
     * 排序规则：按命中个数（hit_count）降序，命中个数相同时按总出手数（total_score）升序（命中率高的在前）
     * 排名规则：只要命中个数相同，排名就相同（并列排名）
     */
    private void reorderLeaderboard(Long seasonId) {
        // 获取该赛季的所有条目，按命中个数降序、总出手数升序排序（命中率高的在前）
        List<LeaderboardEntry> entries = leaderboardEntryMapper.findAllBySeasonId(seasonId);

        // 如果列表为空，直接返回
        if (entries == null || entries.isEmpty()) {
            return;
        }

        // 按照命中个数降序，命中个数相同时按总出手数升序排序（命中率高的在前，仅用于显示顺序，不影响排名）
        entries.sort((e1, e2) -> {
            int hitCountCompare = Integer.compare(e2.getHitCount(), e1.getHitCount()); // 降序
            if (hitCountCompare != 0) {
                return hitCountCompare;
            }
            // 命中个数相同时，按总出手数升序（投的少的命中率高，排在前面）
            return Integer.compare(e1.getTotalScore(), e2.getTotalScore());
        });

        // 分配排名（处理并列排名）：只要命中个数相同，排名就相同
        // 排名规则：当命中个数不同时，排名 = 当前索引位置（从1开始）
        // 例如：如果有3个人并列第3名，下一个排名应该是第6名（跳过前面5个位置）
        int index = 0;
        int currentRank = 0;
        Integer previousHitCount = null;

        for (LeaderboardEntry entry : entries) {
            index++; // 当前索引位置（从1开始）

            // 如果是第一个条目，或者命中数与上一个不同，排名等于当前索引位置
            if (previousHitCount == null || !previousHitCount.equals(entry.getHitCount())) {
                // 排名等于当前索引位置（跳过前面所有并列的人数）
                currentRank = index;
            }
            // 否则，排名与上一个相同（并列排名，因为命中个数相同）

            // 更新排名
            entry.setRankManual(currentRank);
            leaderboardEntryMapper.updateRankManual(entry.getId(), currentRank);

            previousHitCount = entry.getHitCount();
        }
    }

    private LeaderboardDTO convertToDTO(LeaderboardEntry entry) {
        LeaderboardDTO dto = new LeaderboardDTO();
        Season season = seasonMapper.findById(entry.getSeasonId());
        UserProfile user = userProfileMapper.findById(entry.getUserId());

        dto.setId(entry.getId());
        dto.setSeasonId(entry.getSeasonId());
        dto.setSeasonName(season != null ? season.getName() : null);
        dto.setUserId(entry.getUserId());
        dto.setNickname(user != null ? user.getNickname() : null);
        dto.setAvatarUrl(user != null ? user.getAvatarUrl() : null);
        dto.setTotalScore(entry.getTotalScore());
        dto.setHitCount(entry.getHitCount());
        dto.setVideoCoverUrl(entry.getVideoCoverUrl());
        dto.setRankManual(entry.getRankManual());
        dto.setVideoUrl(entry.getVideoUrl());

        return dto;
    }
}