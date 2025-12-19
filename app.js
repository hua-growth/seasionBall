const { createApp, ref, onMounted, onUnmounted } = Vue;

createApp({
    setup() {
        // 弹窗相关数据
        const showVideoModal = ref(false);
        const currentVideoUrl = ref('');
        const currentVideoTitle = ref('');
        
        // 其他数据
        const seasons = ref([]);
        const selectedSeason = ref('v3');
        const leaderboard = ref([]);
        const currentSeason = ref(null);
        const loading = ref(false);
        const error = ref('');
        
        // 引用通知元素
        const notification = ref(null);
        
        // API基础URL
        const API_BASE = 'http://localhost:8080/api';
        
        // 方法
        const loadSeasons = async () => {
            try {
                loading.value = true;
                const response = await axios.get(`${API_BASE}/seasons`);
                
                if (response.data.code === 200) {
                    seasons.value = response.data.data;
                    
                    // 如果没有选择赛季，选择第一个
                    if (seasons.value.length > 0 && !selectedSeason.value) {
                        selectedSeason.value = seasons.value[0].code;
                    }
                    
                    // 加载排行榜
                    await loadLeaderboard();
                } else {
                    error.value = '加载赛季数据失败: ' + response.data.message;
                }
            } catch (err) {
                console.error('加载赛季数据出错:', err);
                error.value = '加载数据失败，请检查网络连接或稍后重试';
            } finally {
                loading.value = false;
            }
        };
        
        const loadLeaderboard = async () => {
            try {
                loading.value = true;
                error.value = '';
                
                const response = await axios.get(`${API_BASE}/leaderboard`, {
                    params: {
                        seasonCode: selectedSeason.value
                    }
                });
                
                if (response.data.code === 200) {
                    const data = response.data.data;
                    currentSeason.value = data.season;
                    leaderboard.value = data.leaderboard;
                } else {
                    error.value = '加载排行榜数据失败: ' + response.data.message;
                    leaderboard.value = [];
                }
            } catch (err) {
                console.error('加载排行榜数据出错:', err);
                error.value = '加载排行榜失败，请检查网络连接';
                leaderboard.value = [];
            } finally {
                loading.value = false;
            }
        };
        
        const calculateHitRate = (entry) => {
            if (!entry.totalScore || entry.totalScore === 0) return 0;
            const rate = (entry.hitCount / entry.totalScore * 100);
            return rate.toFixed(1);
        };
        
        const getRankClass = (rank) => {
            if (rank === 1) return 'rank-1';
            if (rank === 2) return 'rank-2';
            if (rank === 3) return 'rank-3';
            return 'rank-other';
        };
        
        const getRankDisplay = (rank) => {
            return rank;
        };
        
        // 打开视频弹窗
        const openVideoModal = (entry) => {
            // 假设视频URL存储在entry的videoUrl属性中
            // 如果没有videoUrl，可以根据封面URL构造或者使用默认视频
            if (entry.videoUrl) {
                currentVideoUrl.value = entry.videoUrl;
            } else {
                // 如果API没有返回videoUrl，这里可以构造一个示例视频
                // 或者使用封面URL的变体
                const baseUrl = entry.videoCoverUrl || '';
                // 尝试多种可能的视频格式
                const possibleExtensions = ['.mp4', '.mov', '.avi', '.webm', '.mkv'];
                let videoUrl = '';
                
                for (const ext of possibleExtensions) {
                    if (baseUrl.includes('.jpg')) {
                        videoUrl = baseUrl.replace('.jpg', ext);
                        break;
                    } else if (baseUrl.includes('.jpeg')) {
                        videoUrl = baseUrl.replace('.jpeg', ext);
                        break;
                    } else if (baseUrl.includes('.png')) {
                        videoUrl = baseUrl.replace('.png', ext);
                        break;
                    }
                }
                
                // 如果没有找到合适的替换，使用默认视频
                if (!videoUrl) {
                    videoUrl = 'https://download.like-sports.com/leaderboard/v1/sample-video.mp4';
                }
                
                currentVideoUrl.value = videoUrl;
            }
            
            currentVideoTitle.value = entry.nickname;
            showVideoModal.value = true;
        };
        
        // 关闭视频弹窗
        const closeVideoModal = () => {
            showVideoModal.value = false;
            currentVideoUrl.value = '';
            currentVideoTitle.value = '';
        };
        
        // 显示通知
        const showNotification = (message) => {
            if (notification.value) {
                notification.value.textContent = message;
                notification.value.classList.add('show');
                
                setTimeout(() => {
                    notification.value.classList.remove('show');
                }, 3000);
            }
        };
        
        // 复制到剪贴板
        const copyToClipboard = (text) => {
            if (navigator.clipboard && window.isSecureContext) {
                navigator.clipboard.writeText(text)
                    .then(() => {
                        showNotification('已复制链接到剪贴板');
                    })
                    .catch(err => {
                        console.error('复制失败:', err);
                        fallbackCopyTextToClipboard(text);
                    });
            } else {
                fallbackCopyTextToClipboard(text);
            }
        };
        
        // 降级复制方法
        const fallbackCopyTextToClipboard = (text) => {
            const textArea = document.createElement('textarea');
            textArea.value = text;
            textArea.style.position = 'fixed';
            textArea.style.left = '-999999px';
            textArea.style.top = '-999999px';
            document.body.appendChild(textArea);
            textArea.focus();
            textArea.select();
            
            try {
                document.execCommand('copy');
                showNotification('已复制链接到剪贴板');
            } catch (err) {
                console.error('复制失败:', err);
                showNotification('复制失败，请手动复制链接');
            }
            
            document.body.removeChild(textArea);
        };
        
        // 下载iOS应用
        const downloadiOS = () => {
            const appStoreUrl = 'https://apps.apple.com/app/like-sports/id6742750297';
            const isWeChat = /MicroMessenger/i.test(navigator.userAgent);
            const isIOS = /iPhone|iPad|iPod/i.test(navigator.userAgent);
            const isMobile = /iPhone|iPad|iPod|Android/i.test(navigator.userAgent);
            
            if (isWeChat && isIOS) {
                // 在微信 iOS 中，无法直接跳转，需要引导用户
                copyToClipboard(appStoreUrl);
                showNotification('已复制 App Store 链接，请点击右上角"..."选择"在浏览器中打开"，然后粘贴链接即可下载');
            } else if (isMobile) {
                // 非微信环境下的移动设备，直接跳转
                window.location.href = appStoreUrl;
            } else {
                // 桌面浏览器，新窗口打开
                window.open(appStoreUrl, '_blank');
            }
        };
        
        // 下载Android应用
        const downloadAndroid = () => {
            const downloadUrl = 'https://download.like-sports.com/app/official.apk';
            copyToClipboard(downloadUrl);
            showNotification('已复制下载链接，请打开手机浏览器下载安装');
        };
        
        // 键盘事件处理
        const handleKeydown = (event) => {
            if (event.key === 'Escape' && showVideoModal.value) {
                closeVideoModal();
            }
        };
        
        // 生命周期
        onMounted(() => {
            loadSeasons();
            window.addEventListener('keydown', handleKeydown);
        });
        
        // 清理
        onUnmounted(() => {
            window.removeEventListener('keydown', handleKeydown);
        });
        
        return {
            // 弹窗相关
            showVideoModal,
            currentVideoUrl,
            currentVideoTitle,
            openVideoModal,
            closeVideoModal,
            
            // 排行榜相关
            seasons,
            selectedSeason,
            leaderboard,
            currentSeason,
            loading,
            error,
            loadLeaderboard,
            calculateHitRate,
            getRankClass,
            getRankDisplay,
            
            // 下载相关
            downloadiOS,
            downloadAndroid,
            
            // 引用
            notification
        };
    }
}).mount('#app');