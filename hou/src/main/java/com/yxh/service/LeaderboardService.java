// filePath：hou/src/main/java/com/yxh/service/LeaderboardService.java
package com.yxh.service;

import com.yxh.dto.AddLeaderboardEntryDTO;
import com.yxh.dto.LeaderboardDTO;
import java.util.List;

public interface LeaderboardService {
    List<LeaderboardDTO> getLeaderboardBySeasonCode(String seasonCode);
    List<LeaderboardDTO> getLeaderboardBySeasonId(Long seasonId);

    /**
     * 添加或更新排行榜条目，并自动按照命中个数排序
     * @param dto 排行榜条目数据
     * @return 操作结果消息
     */
    String addOrUpdateLeaderboardEntry(AddLeaderboardEntryDTO dto);

    /**
     * 删除排行榜条目
     * @param entryId 条目ID
     * @return 操作结果消息
     */
    String deleteLeaderboardEntry(Long entryId);
}