package com.ef.bite.business.task;

import java.io.File;

import android.content.Context;

import cn.trinea.android.common.util.PreferencesUtils;

import com.ef.bite.AppConst;
import com.ef.bite.business.CourseServerAPI;
import com.ef.bite.business.LoginServerAPI;
import com.ef.bite.dataacces.mode.httpMode.HttpAppResourceRequest;
import com.ef.bite.dataacces.mode.httpMode.HttpAppResourceResponse;
import com.ef.bite.utils.*;
import org.json.JSONException;
import org.json.JSONObject;

public class GetGlobalLanguageTask extends
        BaseAsyncTask<HttpAppResourceRequest, Void, Boolean> {
    private Context context;

    public GetGlobalLanguageTask(Context context,
                                 PostExecuting<Boolean> executing) {
        super(context, executing);
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(HttpAppResourceRequest... params) {
        LoginServerAPI loginServerAPI = new LoginServerAPI(this.context);
        HttpAppResourceResponse httpAppResourceResponse = loginServerAPI
                .getAppResource(params[0]);
        // 通过url开始下载文件
        boolean isDiffLang = !StringUtils.isEquals(getLangCode(),params[0].system_culture);
        CourseServerAPI api = new CourseServerAPI(this.context);
        FileStorage downloadStorage = new FileStorage(this.context,
                AppConst.CacheKeys.Storage_DownloadChunk);
        FileStorage languageStorage = new FileStorage(this.context,
                AppConst.CacheKeys.Storage_Language);

        if (httpAppResourceResponse != null && (isDiffLang||httpAppResourceResponse.data.has_update)) {
            try {
                PreferencesUtils.putString(context,
                        AppLanguageHelper.App_Res_Culture,
                        AppLanguageHelper.getSystemLaunguage(context));

                // 分配时间戳为文件名
                String key = KeyGenerator.getKeyFromDateTime();
                // 创建文件
                File saveFile = new File(downloadStorage.getStorageFolder(), key);
                api.downloadCourses(saveFile.getAbsolutePath(),
                        httpAppResourceResponse.data.package_url);
                if (downloadStorage.get(key) == null) { // 下载失败
                    LogManager
                            .definedLog("DownloadCoursesTask -> Fail to download course!");
                }

                // 解压课程
                if (!ZipUtil.decompress(saveFile.getAbsolutePath(),
                        languageStorage.getStorageFolder(), "utf-8")) {
                    LogManager
                            .definedLog("DownloadCoursesTask -> Fail to decrompress the download course!");
                }
                AppLanguageHelper.loadLanguageFromStorage(mContext, languageStorage.getStorageFolder());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;


    }

    private String getLangCode(){
        try {
            JSONObject jsonObject =new JSONObject(PreferencesUtils.getString(context,
                    AppLanguageHelper.Translation, "")) ;
            if(jsonObject==null){
                return "";
            }
            return jsonObject.optString("language_code");
        } catch (JSONException e) {
            return "";
        }
    }

}
