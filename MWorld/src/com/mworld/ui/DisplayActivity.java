package com.mworld.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.tsz.afinal.FinalBitmap;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mworld.utils.PreUtils;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.weibo.api.FriendshipsAPI;
import com.weibo.api.UsersAPI;
import com.weibo.entities.AccessToken;
import com.weibo.entities.User;
import com.weibo.entities.UsersList;

public class DisplayActivity extends Activity {

	private AccessToken mAccessToken = null;
	private PullToRefreshListView mList;
	private UsersAPI mUsersAPI;
	private FriendshipsAPI mFriendshipsAPI;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display);
		initComponents();
		switch (getIntent().getIntExtra("type", 0)) {
		case 3:
			profile();
			break;
		case 5:
			friends();
			break;
		case 6:
			fans();
			break;
		default:

		}
	}

	private void initComponents() {
		mAccessToken = PreUtils.readAccessToken(DisplayActivity.this);

		mUsersAPI = new UsersAPI(mAccessToken);
		mFriendshipsAPI = new FriendshipsAPI(mAccessToken);
	}

	private void profile() {
		long uid = getIntent().getLongExtra("uid", 0);
		if (uid == MainActivity.sUser.id) {
			User user = MainActivity.sUser;
			FinalBitmap.create(DisplayActivity.this)
					.display(((ImageView) findViewById(R.id.my_icon)),
							user.avatar_large);

			TextView myName = (TextView) findViewById(R.id.my_name);
			myName.setText(user.screen_name);

			TextView myFriends = (TextView) findViewById(R.id.my_friends);
			myFriends.setText("关注：" + user.friends_count);
			myFriends.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// Intent intent = new Intent(
					// DisplayActivity.this,
					// DisplayActivity.class);
					// intent.putExtra("type", 5);
					// intent.putExtra("uid", getIntent()
					// .getStringExtra("uid"));
					// startActivity(intent);
				}

			});

			TextView myFans = (TextView) findViewById(R.id.my_fans);
			myFans.setText("粉丝：" + user.followers_count);
			myFans.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// Intent intent = new Intent(
					// DisplayActivity.this,
					// DisplayActivity.class);
					// intent.putExtra("type", 6);
					// intent.putExtra("uid", getIntent()
					// .getStringExtra("uid"));
					// startActivity(intent);
				}

			});

			TextView myStatus = (TextView) findViewById(R.id.my_status);
			myStatus.setText("微博：" + user.statuses_count);
			myStatus.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// Intent intent = new Intent(
					// DisplayActivity.this,
					// DisplayActivity.class);
					// intent.putExtra("type", 4);
					// intent.putExtra("uid", getIntent()
					// .getStringExtra("uid"));
					// startActivity(intent);
				}

			});
		} else {
			mUsersAPI.show(uid, new RequestListener() {

				@Override
				public void onComplete(String jsonString) {
					User user = User.parse(jsonString);
					FinalBitmap.create(DisplayActivity.this).display(
							((ImageView) findViewById(R.id.my_icon)),
							user.avatar_large);

					TextView myName = (TextView) findViewById(R.id.my_name);
					myName.setText(user.screen_name);

					TextView myFriends = (TextView) findViewById(R.id.my_friends);
					myFriends.setText("关注：" + user.friends_count);

					TextView myFans = (TextView) findViewById(R.id.my_fans);
					myFans.setText("粉丝：" + user.followers_count);

					TextView myStatus = (TextView) findViewById(R.id.my_status);
					myStatus.setText("微博：" + user.statuses_count);

				}

				@Override
				public void onWeiboException(WeiboException arg0) {

				}

			});
		}
	}

	private void friends() {
		mFriendshipsAPI.friends(
				Long.parseLong(getIntent().getStringExtra("uid")), 20, 0,
				false, new UserHandler());
	}

	private void fans() {
		mFriendshipsAPI.followers(
				Long.parseLong(getIntent().getStringExtra("uid")), 20, 0,
				false, new UserHandler());
	}

	public class UserHandler implements RequestListener {

		@Override
		public void onComplete(String jsonString) {
			Log.i("--------------------------", "回调");
			UsersList userList = new UsersList();
			try {
				userList = UsersList.parse(jsonString);
			} catch (Exception e) {

				e.printStackTrace();
			}
			if (userList.usersList == null) {
				Toast.makeText(DisplayActivity.this, "没有获取到关注/粉丝",
						Toast.LENGTH_SHORT).show();
				return;
			}
			final List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
			for (User user : userList.usersList) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("user_avatar", user.avatar_large + "\n" + user.id);
				map.put("user_name", user.screen_name);

				data.add(map);
			}
			String[] from = new String[] { "user_avatar", "user_name" };
			int[] to = new int[] { R.id.icon, R.id.name };
			SimpleAdapter adapter = new SimpleAdapter(DisplayActivity.this,
					data, R.layout.user_list_item, from, to);
			mList.setAdapter(adapter);
			adapter.setViewBinder(new ViewBinder() {

				@Override
				public boolean setViewValue(View view, Object data,
						String textRepresentation) {

					if (view instanceof ImageView) {
						ImageView iv = (ImageView) view;
						final String[] tokens = ((String) data).split("\n");
						FinalBitmap.create(DisplayActivity.this).display(iv,
								tokens[0]);
						view.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {

								Intent intent = new Intent(
										DisplayActivity.this,
										DisplayActivity.class);
								intent.putExtra("type", 3);
								intent.putExtra("uid", tokens[1]);
								startActivity(intent);
							}
						});
						return true;
					} else
						return false;
				}

			});

		}

		@Override
		public void onWeiboException(WeiboException e) {
			Log.d("-------------------", e.getMessage());
		}

	}
}
