const { createApp, ref, computed, onMounted } = Vue;

// 管理后台（仅用于简单维护榜单，不做真实权限控制）
createApp({
    setup() {
        const API_BASE = 'http://localhost:8080/api';

        // 赛季 & 榜单数据
        const seasons = ref([]);
        const selectedSeasonCode = ref('');
        const leaderboard = ref([]);

        // 表单数据
        const newSeason = ref({
            code: '',
            name: ''
        });

        const newEntry = ref({
            seasonCode: '',
            nickname: '',
            avatarUrl: '',
            totalScore: null,
            hitCount: null,
            videoCoverUrl: '',
            videoUrl: ''
        });

        // 状态
        const loadingSeasons = ref(false);
        const loadingEntries = ref(false);
        const saving = ref(false);
        const error = ref('');
        const success = ref('');

        const currentSeasonName = computed(() => {
            const s = seasons.value.find(item => item.code === selectedSeasonCode.value);
            return s ? s.name : '';
        });

        const clearMessageLater = () => {
            if (!success.value && !error.value) return;
            setTimeout(() => {
                success.value = '';
                error.value = '';
            }, 3000);
        };

        const loadSeasons = async () => {
            try {
                loadingSeasons.value = true;
                const resp = await axios.get(`${API_BASE}/seasons`);
                if (resp.data.code === 200) {
                    seasons.value = resp.data.data || [];
                    if (!selectedSeasonCode.value && seasons.value.length > 0) {
                        selectedSeasonCode.value = seasons.value[0].code;
                        newEntry.value.seasonCode = selectedSeasonCode.value;
                        await loadLeaderboard();
                    }
                } else {
                    error.value = '加载赛季失败：' + (resp.data.message || '');
                }
            } catch (e) {
                console.error(e);
                error.value = '加载赛季失败，请检查后端服务';
            } finally {
                loadingSeasons.value = false;
                clearMessageLater();
            }
        };

        const loadLeaderboard = async () => {
            if (!selectedSeasonCode.value) {
                leaderboard.value = [];
                return;
            }
            try {
                loadingEntries.value = true;
                const resp = await axios.get(`${API_BASE}/leaderboard`, {
                    params: { seasonCode: selectedSeasonCode.value }
                });
                if (resp.data.code === 200 && resp.data.data) {
                    leaderboard.value = resp.data.data.leaderboard || [];
                } else {
                    error.value = '加载排行榜失败：' + (resp.data.message || '');
                    leaderboard.value = [];
                }
            } catch (e) {
                console.error(e);
                error.value = '加载排行榜失败，请检查后端服务';
                leaderboard.value = [];
            } finally {
                loadingEntries.value = false;
                clearMessageLater();
            }
        };

        const selectSeason = async (code) => {
            if (selectedSeasonCode.value === code) return;
            selectedSeasonCode.value = code;
            newEntry.value.seasonCode = code;
            await loadLeaderboard();
        };

        const createSeason = async () => {
            if (!newSeason.value.code || !newSeason.value.name) {
                error.value = '请完整填写赛季编码和名称';
                clearMessageLater();
                return;
            }
            try {
                saving.value = true;
                error.value = '';
                success.value = '';
                const payload = {
                    code: newSeason.value.code.trim(),
                    name: newSeason.value.name.trim()
                };
                const resp = await axios.post(`${API_BASE}/seasons`, payload);
                if (resp.data.code === 200) {
                    success.value = '新赛季创建成功';
                    newSeason.value.code = '';
                    newSeason.value.name = '';
                    await loadSeasons();
                } else {
                    error.value = resp.data.message || '创建赛季失败';
                }
            } catch (e) {
                console.error(e);
                error.value = (e.response && e.response.data && e.response.data.message) || '创建赛季失败';
            } finally {
                saving.value = false;
                clearMessageLater();
            }
        };

        const saveEntry = async () => {
            if (!selectedSeasonCode.value) {
                error.value = '请先选择一个赛季';
                clearMessageLater();
                return;
            }
            if (!newEntry.value.nickname || newEntry.value.totalScore == null || newEntry.value.hitCount == null) {
                error.value = '请至少填写昵称、总出手数和命中数';
                clearMessageLater();
                return;
            }

            try {
                saving.value = true;
                error.value = '';
                success.value = '';
                const payload = {
                    seasonCode: selectedSeasonCode.value,
                    nickname: newEntry.value.nickname.trim(),
                    avatarUrl: newEntry.value.avatarUrl || null,
                    totalScore: Number(newEntry.value.totalScore),
                    hitCount: Number(newEntry.value.hitCount),
                    videoCoverUrl: newEntry.value.videoCoverUrl || null,
                    videoUrl: newEntry.value.videoUrl || null
                };

                const resp = await axios.post(`${API_BASE}/leaderboard/entry`, payload);
                if (resp.data.code === 200) {
                    success.value = resp.data.data || '保存成功';
                    // 清理除赛季代码外的表单
                    newEntry.value.nickname = '';
                    newEntry.value.avatarUrl = '';
                    newEntry.value.totalScore = null;
                    newEntry.value.hitCount = null;
                    newEntry.value.videoCoverUrl = '';
                    newEntry.value.videoUrl = '';
                    await loadLeaderboard();
                } else {
                    error.value = resp.data.message || '保存失败';
                }
            } catch (e) {
                console.error(e);
                const msg = e.response && e.response.data && e.response.data.message;
                error.value = msg || '保存失败，请检查填写内容';
            } finally {
                saving.value = false;
                clearMessageLater();
            }
        };

        const calculateHitRate = (entry) => {
            if (!entry.totalScore || entry.totalScore === 0) return '0.0';
            const rate = (entry.hitCount / entry.totalScore) * 100;
            return rate.toFixed(1);
        };

        const deleteSeason = async (seasonId, seasonName) => {
            if (!confirm(`确定要删除赛季"${seasonName}"吗？\n\n此操作将同时删除该赛季下的所有排行榜条目，且无法恢复！`)) {
                return;
            }
            try {
                saving.value = true;
                error.value = '';
                success.value = '';
                const resp = await axios.delete(`${API_BASE}/seasons/${seasonId}`);
                if (resp.data.code === 200) {
                    success.value = resp.data.data || '删除成功';
                    // 如果删除的是当前选中的赛季，清空选择
                    const deletedSeason = seasons.value.find(s => s.id === seasonId);
                    if (deletedSeason && deletedSeason.code === selectedSeasonCode.value) {
                        selectedSeasonCode.value = '';
                        leaderboard.value = [];
                    }
                    await loadSeasons();
                } else {
                    error.value = resp.data.message || '删除失败';
                }
            } catch (e) {
                console.error(e);
                const msg = e.response && e.response.data && e.response.data.message;
                error.value = msg || '删除失败，请稍后重试';
            } finally {
                saving.value = false;
                clearMessageLater();
            }
        };

        const deleteEntry = async (entryId, nickname) => {
            if (!confirm(`确定要删除"${nickname}"的排行榜记录吗？\n\n此操作无法恢复！`)) {
                return;
            }
            try {
                saving.value = true;
                error.value = '';
                success.value = '';
                const resp = await axios.delete(`${API_BASE}/leaderboard/entry/${entryId}`);
                if (resp.data.code === 200) {
                    success.value = resp.data.data || '删除成功';
                    await loadLeaderboard();
                } else {
                    error.value = resp.data.message || '删除失败';
                }
            } catch (e) {
                console.error(e);
                const msg = e.response && e.response.data && e.response.data.message;
                error.value = msg || '删除失败，请稍后重试';
            } finally {
                saving.value = false;
                clearMessageLater();
            }
        };

        onMounted(async () => {
            await loadSeasons();
        });

        return {
            seasons,
            selectedSeasonCode,
            leaderboard,
            newSeason,
            newEntry,
            loadingSeasons,
            loadingEntries,
            saving,
            error,
            success,
            currentSeasonName,
            loadSeasons,
            loadLeaderboard,
            selectSeason,
            createSeason,
            saveEntry,
            calculateHitRate,
            deleteSeason,
            deleteEntry
        };
    }
}).mount('#admin-app');