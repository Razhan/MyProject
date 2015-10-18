package com.ef.bite.dataacces.mode.httpMode;

import java.util.List;

public class HttpDashboard extends HttpBaseMessage {

	public DashboardData data;

    public static class DashboardData {
		public int inbox_count;
		public List<Lesson> new_lessons;
		public int new_lesson_count;
		public int new_lesson_unlocking_seconds;
		public List<Lesson> new_rehearsals;
		public int new_rehearsal_count;
		public int new_rehearsal_unlocking_seconds;
		public int master_count;
		public int phrase_count;
		public List<friend> rank_friends;
        public boolean unlock_enabled;
        public int recording_like_count;
        public List<BannersEntity> banners;
    }

	public static class Lesson {
		public String course_id;
		public String course_name;
		public String course_package_url;
		public int course_version;
		public int rehearsal_status;
		public String order;
	}

	public static class friend{
		public String bella_id;
		public String avatar_url;
		public String alias;
		public String given_name;
		public String family_name;
		public int rank;
		public int level;
		public int score;
		public String level_progress;
		public int friend_count;

	}

    public static class BannersEntity {
        /**
         * image_url : http://bella-image.oss-cn-qingdao.aliyuncs.com/banner/dashboard_banner_default.jpg
         * target_url : http://www.englishtown.com
         */

        private String image_url;
        private String target_url;

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

        public void setTarget_url(String target_url) {
            this.target_url = target_url;
        }

        public String getImage_url() {
            return image_url;
        }

        public String getTarget_url() {
            return target_url;
        }
    }
}

