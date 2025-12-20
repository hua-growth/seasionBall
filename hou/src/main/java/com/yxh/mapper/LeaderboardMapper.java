// filePath：hou/src/main/java/com/yxh/mapper/LeaderboardMapper.java
package com.yxh.mapper;

import com.yxh.vo.LeaderboardRowVO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 排行榜数据访问接口
 */
public interface LeaderboardMapper {
    /**
     * 根据赛季编码查询排行榜数据
     * @param seasonCode 赛季编码
     * @return 排行榜行数据列表
     */
    List<LeaderboardRowVO> selectLeaderboardBySeason(@Param("seasonCode") String seasonCode);
}