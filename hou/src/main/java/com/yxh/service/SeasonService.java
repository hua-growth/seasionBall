// filePath：hou/src/main/java/com/yxh/service/SeasonService.java
package com.yxh.service;

import com.yxh.dto.SeasonDTO;
import java.util.List;

public interface SeasonService {
    List<SeasonDTO> getAllSeasons();
    SeasonDTO getSeasonByCode(String code);
}