package com.yxh.mapper;

import com.yxh.entity.LeaderboardEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LeaderboardEntryMapper {
    List<LeaderboardEntry> findBySeasonCode(@Param("seasonCode") String seasonCode);
    List<LeaderboardEntry> findBySeasonId(@Param("seasonId") Long seasonId);

    LeaderboardEntry findBySeasonIdAndUserId(@Param("seasonId") Long seasonId, @Param("userId") Long userId);

    LeaderboardEntry findById(@Param("id") Long id);

    void insert(LeaderboardEntry entry);

    void update(LeaderboardEntry entry);

    List<LeaderboardEntry> findAllBySeasonId(@Param("seasonId") Long seasonId);

    void updateRankManual(@Param("id") Long id, @Param("rankManual") Integer rankManual);

    void deleteById(@Param("id") Long id);

    void deleteBySeasonId(@Param("seasonId") Long seasonId);
}