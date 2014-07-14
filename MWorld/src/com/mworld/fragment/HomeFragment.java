package com.mworld.fragment;

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
import com.mworld.adapter.StatusListAdapter;
import com.mworld.handler.StatusLoadHandler;
import com.mworld.handler.StatusRefHandler;
import com.mworld.ui.R;
import com.weibo.api.StatusesAPI;
import com.weibo.entities.Status;

public class HomeFragment extends BaseFragment {

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAPI = new StatusesAPI(mAccessToken);
		mArrayList = new ArrayList<Status>();
		mAdapter = new StatusListAdapter(getActivity(), mArrayList);
	}

	@SuppressLint("InflateParams")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_home, null);
		mList = (PullToRefreshListView) view.findViewById(R.id.home_timeline);
		mList.setAdapter(mAdapter);
		((StatusesAPI) mAPI).friendsTimeline(since_id, 0, 20, 1, false, 0,
				false, new StatusRefHandler(this));
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		mList.setOnRefreshListener(new OnRefreshListener<ListView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				Log.i("Home", "refresh");
				((StatusesAPI) mAPI).friendsTimeline(since_id, 0, 20, 1, false,
						0, false, new StatusRefHandler(HomeFragment.this));
			}
		});
		mList.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

			@Override
			public void onLastItemVisible() {
				if (isLoading) {
					Toast.makeText(getActivity(), "不要着急，正在加载",
							Toast.LENGTH_SHORT).show();
					mList.onRefreshComplete();
				} else {
					Toast.makeText(getActivity(), "加载中...", Toast.LENGTH_SHORT)
							.show();
					((StatusesAPI) mAPI).friendsTimeline(0, init_id, 20,
							page++, false, 0, false, new StatusLoadHandler(
									HomeFragment.this));
					isLoading = true;
				}
			}
		});

	}

}