package com.yxh.service.impl;

import com.yxh.dto.SeasonDTO;
import com.yxh.entity.Season;
import com.yxh.mapper.SeasonMapper;
import com.yxh.service.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeasonServiceImpl implements SeasonService {
    
    @Autowired
    private SeasonMapper seasonMapper;
    
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
    
    private SeasonDTO convertToDTO(Season season) {
        SeasonDTO dto = new SeasonDTO();
        dto.setId(season.getId());
        dto.setCode(season.getCode());
        dto.setName(season.getName());
        return dto;
    }
}