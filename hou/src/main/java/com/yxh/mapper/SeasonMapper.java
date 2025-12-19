package com.yxh.mapper;

import com.yxh.entity.Season;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SeasonMapper {
    List<Season> findAllOrdered();
    Season findByCode(@Param("code") String code);

    Season findById(Long seasonId);
}