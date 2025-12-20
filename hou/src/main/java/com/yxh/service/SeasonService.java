// filePath：hou/src/main/java/com/yxh/service/SeasonService.java
package com.yxh.service;

import com.yxh.dto.SeasonDTO;
import java.util.List;

public interface SeasonService {
    List<SeasonDTO> getAllSeasons();
    SeasonDTO getSeasonByCode(String code);

    /**
     * 新增一个赛季（榜单）
     * @param dto 赛季数据（只需要 code 和 name）
     * @return 新增后的赛季信息
     */
    SeasonDTO createSeason(SeasonDTO dto);

    /**
     * 删除赛季（榜单），同时删除该赛季下的所有排行榜条目
     * @param seasonId 赛季ID
     * @return 操作结果消息
     */
    String deleteSeason(Long seasonId);
}