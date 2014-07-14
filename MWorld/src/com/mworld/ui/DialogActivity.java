package com.mworld.ui;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.annotation.view.ViewInject;
import net.tsz.afinal.http.AjaxCallBack;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mworld.utils.PreUtils;
import com.weibo.api.Oauth2API;
import com.weibo.api.UsersAPI;
import com.weibo.entities.User;

public class DialogActivity extends Activity {

	@ViewInject(id = R.id.llayout01, click = "click")
	private LinearLayout layout01;
	@ViewInject(id = R.id.llayout02, click = "click")
	private LinearLayout layout02;
	@ViewInject(id = R.id.llayout03, click = "click")
	private LinearLayout layout03;
	@ViewInject(id = R.id.llayout04, click = "click")
	private LinearLayout layout04;
	@ViewInject(id = R.id.llayout05, click = "click")
	private LinearLayout layout05;

	@ViewInject(id = R.id.head_icon)
	ImageView mHeadIcon;
	@ViewInject(id = R.id.head_name)
	TextView mHeadName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dialog);
		FinalActivity.initInjectedView(this);
		hasUser();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		finish();
		return true;
	}

	public void click(View view) {
		switch (view.getId()) {

		case R.id.llayout01:
			Intent intent = new Intent(this, DisplayActivity.class);
			intent.putExtra("type", 3);
			intent.putExtra("uid", MainActivity.sUser.id);
			startActivity(intent);
			break;
		case R.id.llayout05:
			Intent loginIntent = new Intent(this, LoginActivity.class);
			startActivity(loginIntent);
			PreUtils.logOut(this);
			Oauth2API.revokeOauth2(MainActivity.sAccessToken,
					new AjaxCallBack<String>() {
					});
			finish();
			MainActivity.instance.finish();
			break;
		default:
			break;
		}
		finish();
	}

	private void hasUser() {
		if (null != MainActivity.sUser) {
			setHeadIcon();
		} else {
			new UsersAPI(MainActivity.sAccessToken).show(
					Long.parseLong(MainActivity.sAccessToken.uid),
					new AjaxCallBack<String>() {

						@Override
						public void onFailure(Throwable t, int errorNo,
								String strMsg) {
							super.onFailure(t, errorNo, strMsg);
						}

						@Override
						public void onSuccess(String jsonString) {
							super.onSuccess(jsonString);
							MainActivity.sUser = User.parse(jsonString);
							setHeadIcon();
						}

					});
		}
	}

	private void setHeadIcon() {
		FinalBitmap.create(this).display(mHeadIcon,
				MainActivity.sUser.avatar_large);
		mHeadName.setText(MainActivity.sUser.screen_name);
	}
}