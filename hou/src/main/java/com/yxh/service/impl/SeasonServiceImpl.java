package com.yxh.service.impl;

import com.yxh.dto.SeasonDTO;
import com.yxh.entity.Season;
import com.yxh.mapper.LeaderboardEntryMapper;
import com.yxh.mapper.SeasonMapper;
import com.yxh.service.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeasonServiceImpl implements SeasonService {

    @Autowired
    private SeasonMapper seasonMapper;

    @Autowired
    private LeaderboardEntryMapper leaderboardEntryMapper;

    @Override
    public List<SeasonDTO> getAllSeasons() {
        return seasonMapper.findAllOrdered()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SeasonDTO getSeasonByCode(String code) {
        Season season = seasonMapper.findByCode(code);
        return season != null ? convertToDTO(season) : null;
    }

    @Override
    public SeasonDTO createSeason(SeasonDTO dto) {
        // 简单校验
        if (dto == null) {
            throw new IllegalArgumentException("赛季数据不能为空");
        }
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("赛季编码不能为空");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("赛季名称不能为空");
        }

        // 编码唯一性校验
        Season exists = seasonMapper.findByCode(dto.getCode().trim());
        if (exists != null) {
            throw new IllegalArgumentException("赛季编码已存在: " + dto.getCode());
        }

        Season season = new Season();
        season.setCode(dto.getCode().trim());
        season.setName(dto.getName().trim());
        season.setCreatedAt(LocalDateTime.now());
        season.setUpdatedAt(LocalDateTime.now());

        seasonMapper.insert(season);

        return convertToDTO(season);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public String deleteSeason(Long seasonId) {
        // 1. 验证赛季是否存在
        Season season = seasonMapper.findById(seasonId);
        if (season == null) {
            throw new IllegalArgumentException("赛季不存在");
        }

        // 2. 删除该赛季下的所有排行榜条目
        leaderboardEntryMapper.deleteBySeasonId(seasonId);

        // 3. 删除赛季
        int deleted = seasonMapper.deleteById(seasonId);
        if (deleted == 0) {
            throw new IllegalArgumentException("删除失败，赛季可能不存在");
        }

        return "成功删除赛季及其所有排行榜条目";
    }

    private SeasonDTO convertToDTO(Season season) {
        SeasonDTO dto = new SeasonDTO();
        dto.setId(season.getId());
        dto.setCode(season.getCode());
        dto.setName(season.getName());
        return dto;
    }
}