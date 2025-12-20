// filePath：hou/src/main/java/com/yxh/controller/LeaderboardController.java
package com.yxh.controller;

import com.yxh.dto.AddLeaderboardEntryDTO;
import com.yxh.dto.LeaderboardDTO;
import com.yxh.dto.SeasonDTO;
import com.yxh.service.LeaderboardService;
import com.yxh.service.SeasonService;
import com.yxh.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 排行榜控制器
 * 处理排行榜相关的HTTP请求
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LeaderboardController {

    @Autowired
    private LeaderboardService leaderboardService;

    @Autowired
    private SeasonService seasonService;

    /**
     * 获取所有赛季列表
     * @return 赛季列表数据
     */
    @GetMapping("/seasons")
    public ResponseEntity<Result<List<SeasonDTO>>> getAllSeasons() {
        List<SeasonDTO> seasons = seasonService.getAllSeasons();
        return ResponseEntity.ok(Result.success(seasons));
    }

    /**
     * 新增一个赛季（榜单）
     * @param dto 赛季数据
     * @return 新增后的赛季信息
     */
    @PostMapping("/seasons")
    public ResponseEntity<Result<SeasonDTO>> createSeason(@RequestBody SeasonDTO dto) {
        try {
            SeasonDTO created = seasonService.createSeason(dto);
            return ResponseEntity.ok(Result.success(created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error("服务器错误: " + e.getMessage()));
        }
    }

    /**
     * 根据赛季编码获取排行榜数据
     * @param seasonCode 赛季编码，默认值为"v3"
     * @return 包含赛季信息和排行榜数据的结果
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<Result<Map<String, Object>>> getLeaderboard(
            @RequestParam(value = "seasonCode", defaultValue = "v3") String seasonCode) {
        try {
            List<LeaderboardDTO> leaderboard = leaderboardService.getLeaderboardBySeasonCode(seasonCode);
            SeasonDTO season = seasonService.getSeasonByCode(seasonCode);

            Map<String, Object> data = new HashMap<>();
            data.put("season", season);
            data.put("leaderboard", leaderboard);

            return ResponseEntity.ok(Result.success(data));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error("服务器错误: " + e.getMessage()));
        }
    }

    /**
     * 根据赛季ID获取排行榜数据
     * @param seasonId 赛季ID
     * @return 排行榜数据列表
     */
    @GetMapping("/leaderboard/{seasonId}")
    public ResponseEntity<Result<List<LeaderboardDTO>>> getLeaderboardBySeasonId(
            @PathVariable Long seasonId) {
        try {
            List<LeaderboardDTO> leaderboard = leaderboardService.getLeaderboardBySeasonId(seasonId);
            return ResponseEntity.ok(Result.success(leaderboard));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error("服务器错误: " + e.getMessage()));
        }
    }

    /**
     * 添加或更新排行榜条目
     * 数据会自动按照命中个数排序并更新排名
     * @param dto 排行榜条目数据
     * @return 操作结果
     */
    @PostMapping("/leaderboard/entry")
    public ResponseEntity<Result<String>> addOrUpdateLeaderboardEntry(
            @RequestBody AddLeaderboardEntryDTO dto) {
        try {
            String message = leaderboardService.addOrUpdateLeaderboardEntry(dto);
            return ResponseEntity.ok(Result.success(message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error("服务器错误: " + e.getMessage()));
        }
    }

    /**
     * 删除排行榜条目
     * @param entryId 条目ID
     * @return 操作结果
     */
    @DeleteMapping("/leaderboard/entry/{entryId}")
    public ResponseEntity<Result<String>> deleteLeaderboardEntry(
            @PathVariable Long entryId) {
        try {
            String message = leaderboardService.deleteLeaderboardEntry(entryId);
            return ResponseEntity.ok(Result.success(message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error("服务器错误: " + e.getMessage()));
        }
    }

    /**
     * 删除赛季（榜单）
     * @param seasonId 赛季ID
     * @return 操作结果
     */
    @DeleteMapping("/seasons/{seasonId}")
    public ResponseEntity<Result<String>> deleteSeason(
            @PathVariable Long seasonId) {
        try {
            String message = seasonService.deleteSeason(seasonId);
            return ResponseEntity.ok(Result.success(message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Result.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Result.error("服务器错误: " + e.getMessage()));
        }
    }
}