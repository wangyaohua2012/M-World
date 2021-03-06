package com.mworld.ui.fragment;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.mworld.R;
import com.mworld.ui.adapter.StatusListAdapter;
import com.mworld.ui.handler.StatusLoadHandler;
import com.mworld.ui.handler.StatusRefHandler;
import com.mworld.weibo.api.StatusAPI;
import com.mworld.weibo.entities.Account;
import com.mworld.weibo.entities.Status;
import com.mworld.weibo.entities.User;

public class MentionsFragment extends BaseFragment {

	@SuppressWarnings("unused")
	private Account mAccount;

	@SuppressWarnings("unused")
	private User mUser;

	private String mToken;

	public static MentionsFragment newInstance(Account account, User user,
			String token) {
		MentionsFragment fragment = new MentionsFragment(account, user, token);
		fragment.setArguments(new Bundle());
		return fragment;
	}

	public MentionsFragment(Account account, User user, String token) {
		mAccount = account;
		mUser = user;
		mToken = token;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAPI = new StatusAPI(mToken);
		mArrayList = new ArrayList<Status>();
		mAdapter = new StatusListAdapter(getActivity(), mArrayList);
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_at, null);
		mList = (PullToRefreshListView) view.findViewById(R.id.at_timeline);
		mList.setAdapter(mAdapter);
		((StatusAPI) mAPI).mentions(since_id, 0, 20, 1,
				StatusAPI.AUTHOR_FILTER_ALL, StatusAPI.SRC_FILTER_ALL,
				StatusAPI.TYPE_FILTER_ALL, false, new StatusRefHandler(this));
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		mList.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				Log.i("At", "refresh");
				((StatusAPI) mAPI).mentions(since_id, 0, 20, 1,
						StatusAPI.AUTHOR_FILTER_ALL, StatusAPI.SRC_FILTER_ALL,
						StatusAPI.TYPE_FILTER_ALL, false, new StatusRefHandler(
								MentionsFragment.this));
			}
		});
		mList.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

			@Override
			public void onLastItemVisible() {
				Toast.makeText(getActivity(), "正在加载微博", Toast.LENGTH_SHORT)
						.show();
				((StatusAPI) mAPI).mentions(0, init_id, 20, page++,
						StatusAPI.AUTHOR_FILTER_ALL, StatusAPI.SRC_FILTER_ALL,
						StatusAPI.TYPE_FILTER_ALL, false,
						new StatusLoadHandler(MentionsFragment.this));
			}
		});
	}

}
