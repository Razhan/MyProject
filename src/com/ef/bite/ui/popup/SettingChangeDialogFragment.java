package com.ef.bite.ui.popup;

import android.app.Activity;
import com.ef.bite.R;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.ef.bite.AppConst;
import com.ef.bite.business.task.PostExecuting;
import com.ef.bite.business.task.PostProfileTask;
import com.ef.bite.business.task.UpdateUserProfile;
import com.ef.bite.dataacces.mode.httpMode.HttpBaseMessage;
import com.ef.bite.dataacces.mode.httpMode.HttpProfile;
import com.ef.bite.utils.JsonSerializeHelper;

import org.json.JSONObject;

/**
 * 设置项值修改的dialog
 * 
 * @author Allen.Zhu
 * 
 */
public class SettingChangeDialogFragment extends BaseDialogFragment {

	public final static int SETTINGS_NICKNAME = 0;
	public final static int SETTINGS_FIRST_NAME = 1;
	public final static int SETTINGS_LAST_NAME = 2;
	public final static int SETTINGS_EMAIL = 3;
	public final static int SETTINGS_PHONE = 4;
	public final static int SETTINGS_LOCATION = 5;

	private String mTitle;
	private String mItemValue;
	private AfterItemChangeListener mChangeListener;
	private int mChangeItemID;

	public SettingChangeDialogFragment(Activity activity, int itemId,
			String itemName, String itemValue,
			AfterItemChangeListener changeListener) {
		super(activity);
		mTitle = JsonSerializeHelper.JsonLanguageDeserialize(mActivity,
				"settings_edit_item_title") + "\"" + itemName + "\"";
		mItemValue = itemValue;
		mChangeListener = changeListener;
		mChangeItemID = itemId;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mBuilder.setTitle(mTitle);
		final EditText edit = new EditText(mActivity);
		edit.setText(mItemValue);
		this.mBuilder.setView(edit);
		mBuilder.setPositiveButton(
				JsonSerializeHelper.JsonLanguageDeserialize(mActivity,
						"settings_logout_dialog_ok"),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						boolean is_to_update = true;
						final String text = edit.getText().toString().trim();
						if (text == null || text.isEmpty()) {
							Toast.makeText(
									mActivity,
									JsonSerializeHelper
											.JsonLanguageDeserialize(mActivity,
													"settings_item_value_null"),
									Toast.LENGTH_SHORT).show();
							return;
						}

                        JSONObject jsonObj = new JSONObject();

                        try{
                            jsonObj.put("bella_id", AppConst.CurrUserInfo.UserId);

                            if (mChangeItemID == SETTINGS_NICKNAME) {
                                jsonObj.put("alias", text);

                            } else if (mChangeItemID == SETTINGS_FIRST_NAME) {
                                jsonObj.put("given_name", text);

                            } else if (mChangeItemID == SETTINGS_LAST_NAME) {
                                jsonObj.put("family_name", text);

                            } else if (mChangeItemID == SETTINGS_PHONE) {
                                jsonObj.put("phone", text);

                            } else
                                is_to_update = false;

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

						if (is_to_update) {
                            UpdateUserProfile task = new UpdateUserProfile(
									mActivity,
									new PostExecuting<HttpBaseMessage>() {
										@Override
										public void executing(
												HttpBaseMessage result) {
											if (result != null
													&& result.status != null) {
												if (result.status.equals("0")) {
													if (mChangeListener != null)
														mChangeListener
																.afterChange(text);
												} else
													Toast.makeText(mActivity,
															result.message,
															Toast.LENGTH_SHORT)
															.show();
											} else
												Toast.makeText(
														mActivity,
														JsonSerializeHelper
																.JsonLanguageDeserialize(
																		mActivity,
																		"settings_item_fail_to_edit"),
														Toast.LENGTH_SHORT)
														.show();
										}
									});
							task.execute(jsonObj);
						} else
							Toast.makeText(
									mActivity,
									JsonSerializeHelper
											.JsonLanguageDeserialize(mActivity,
													"settings_select_wrong_item"),
									Toast.LENGTH_SHORT).show();
					}
				}).setNegativeButton(
                JsonSerializeHelper.JsonLanguageDeserialize(mActivity,
                        "settings_logout_dialog_cancel"),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
		return mBuilder.create();
	}

	public interface AfterItemChangeListener {
		void afterChange(String changed);
	}

}
