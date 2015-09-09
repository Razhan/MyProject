package com.ef.bite.business;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.ef.bite.AppConst;
import com.ef.bite.dataacces.mode.LoginMode;
import com.ef.bite.dataacces.mode.httpMode.HttpAppResourceRequest;
import com.ef.bite.dataacces.mode.httpMode.HttpAppResourceResponse;
import com.ef.bite.dataacces.mode.httpMode.HttpBaseMessage;
import com.ef.bite.dataacces.mode.httpMode.HttpLogin;
import com.ef.bite.dataacces.mode.httpMode.HttpServerAddress;
import com.ef.bite.model.ConfigModel;
import com.ef.bite.utils.HttpRestfulClient;
import com.ef.bite.utils.JsonSerializeHelper;
import com.ef.bite.utils.TraceHelper;

public class LoginServerAPI extends BaseServerAPI {
	private Context context;

	public LoginServerAPI(Context context) {
		super(context);
		this.context = context;
	}

	public HttpLogin login(LoginMode loginMode) {
		try {
			String usermodeString = JsonSerializeHelper
					.JsonSerializer(loginMode);
			String loginMesString = HttpRestfulClient.JsonPost(
					AppConst.EFAPIs.BaseAddress + "2/login", usermodeString,
					headerMap);
			Log.i("loginMesString", loginMesString);
			HttpLogin httpLogin = (HttpLogin) JsonSerializeHelper
					.JsonDeserialize(loginMesString, HttpLogin.class);
			return httpLogin;
		} catch (Exception ex) {
			ex.printStackTrace();
			TraceHelper.tracingErrorLog(this.context, ex.getMessage());
			return null;
		}
	}

	public HttpBaseMessage Logout(String uid) {
		try {
			HttpBaseMessage logoutMessage = (HttpBaseMessage) HttpRestfulClient
					.Get(AppConst.EFAPIs.BaseAddress + "logout/" + uid,
							HttpBaseMessage.class);
			return logoutMessage;
		} catch (Exception ex) {
			ex.printStackTrace();
			TraceHelper.tracingErrorLog(this.context, ex.getMessage());
			return null;
		}
	}

	/**
	 * 通过邮箱和密码对注册EF账号
	 * 
	 * @param email
	 * @param password
	 * @return
	 */
	public HttpBaseMessage registerUser(Object phone, Object password,
			Object firstname, Object lastname, Object is_call_allowed,
			Object real_phone, Object ageindex) {
		try {
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("username", phone);
			jsonObj.put("phone", phone);
			jsonObj.put("password", password);
			jsonObj.put("first_name", firstname);
			jsonObj.put("last_name", lastname);
			jsonObj.put("alias", firstname);
			jsonObj.put("is_call_allowed", is_call_allowed);
			jsonObj.put("real_phone", real_phone);
			jsonObj.put("age", ageindex);
			String registInfoMessage = HttpRestfulClient.JsonPost(
					AppConst.EFAPIs.BaseAddress + "register",
					jsonObj.toString(), headerMap);
			HttpBaseMessage registInfoMessage2 = (HttpBaseMessage) JsonSerializeHelper
					.JsonDeserialize(registInfoMessage, HttpBaseMessage.class);
			return registInfoMessage2;
		} catch (Exception ex) {
			ex.printStackTrace();
			TraceHelper.tracingErrorLog(this.context, ex.getMessage());
			return null;
		}
	}

	/**
	 * 根据版本获得Server API调用的地址
	 */
	public HttpServerAddress getServerAddress(String app_version,
			Context context) {
		String country = context.getResources().getConfiguration().locale
				.getCountry().toLowerCase();
		try {
			// 正式API
			if (country.equals("cn")) {
				HttpServerAddress address = (HttpServerAddress) HttpRestfulClient
						.Get(AppConst.EFAPIs.ETHost + "android/" + app_version,
								HttpServerAddress.class);
				return address;
			} else {
				HttpServerAddress address = (HttpServerAddress) HttpRestfulClient
						.Get(AppConst.EFAPIs.HK_ETHost + "android/"
								+ app_version, HttpServerAddress.class);
				return address;
			}

			// 调试API
			// HttpServerAddress address = (HttpServerAddress) HttpRestfulClient
			// .Get(AppConst.EFAPIs.ETHost + "android/" + app_version,
			// HttpServerAddress.class);
			// return address;
		} catch (Exception ex) {
			ex.printStackTrace();
			TraceHelper.tracingErrorLog(this.context, ex.getMessage());
			return null;
		}
	}

	public HttpServerAddress getServerAddressCN(String app_version,
			Context context) {
		try {
			HttpServerAddress address = (HttpServerAddress) HttpRestfulClient
					.Get(AppConst.EFAPIs.ETHost + "android/" + app_version,
							HttpServerAddress.class);
			return address;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public HttpServerAddress getServerAddressCOM(String app_version,
			Context context) {
		try {
			HttpServerAddress address = (HttpServerAddress) HttpRestfulClient
					.Get(AppConst.EFAPIs.HK_ETHost + "android/" + app_version,
							HttpServerAddress.class);
			return address;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public HttpAppResourceResponse getAppResource(
			HttpAppResourceRequest appResourceRequest) {
		try {
			String resource = JsonSerializeHelper
					.JsonSerializer(appResourceRequest);
			String language = HttpRestfulClient.JsonPost(
					AppConst.EFAPIs.BaseAddress + "appresource", resource,
					headerMap);
			HttpAppResourceResponse httpAppResourceResponse = (HttpAppResourceResponse) JsonSerializeHelper
					.JsonDeserialize(language, HttpAppResourceResponse.class);
			Log.i("language", language);
			return httpAppResourceResponse;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}
	}
}
