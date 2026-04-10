// store/settings.js 或用 Vuex
import { getSettings } from '@/api/settings';

export default {
    state: {
        siteName: '宠物服务系统',
        siteLogo: '',
        siteDesc: '',
        copyright: '',
        icp: '',
        servicePhone: '',
        serviceEmail: '',
        maintenanceMode: false
    },

    async loadSettings() {
        const res = await getSettings();
        if (res.code === 200) {
            Object.assign(this.state, res.data.basic);
            // 更新页面标题
            document.title = this.state.siteName;
        }
    }
};