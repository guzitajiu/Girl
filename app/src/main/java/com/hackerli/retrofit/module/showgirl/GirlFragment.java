package com.hackerli.retrofit.module.showgirl;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hackerli.retrofit.BaseFragment;
import com.hackerli.retrofit.R;
import com.hackerli.retrofit.data.entity.Girl;
import com.hackerli.retrofit.module.showgirl.adapter.GirlAdapter;
import com.hackerli.retrofit.module.showgirl.adapter.GirlOnClickListener;
import com.hackerli.retrofit.module.showgirl.viewgirlphoto.GirlPhotoFragment;
import com.hackerli.retrofit.util.SnackBarUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

/**
 * Created by CoXier on 2016/5/2.
 */
public class GirlFragment extends BaseFragment implements GirlOnClickListener, GirlContract.View {

    @Bind(R.id.recl)
    RecyclerView recyclerView;
    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    private int page = 1;
    private int limit = 10;
    private List<Girl> mGirls = new ArrayList<>();
    private GirlAdapter mGirlAdapter;
    private GirlContract.Presenter mPresenter = new GirlPresenter(this);

    private boolean mIsFirstTouchedBottom = true;
    private boolean mIsFirstCreated = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_girl, container, false);
        ButterKnife.bind(this, view);

        setRecyclerView();
        setSwipeRefreshLayout();

        // 进入之后先加载，故refresh
        swipeRefreshLayout.measure(View.MEASURED_SIZE_MASK, View.MEASURED_HEIGHT_STATE_SHIFT);
        if (mIsFirstCreated) {
            onRefresh();
        }
        swipeRefreshLayout.setOnRefreshListener(this);
        return view;
    }

    @Override
    public void setRecyclerView() {
        mGirlAdapter = new GirlAdapter(this, mGirls);
        recyclerView.setAdapter(new ScaleInAnimationAdapter(mGirlAdapter));
        StaggeredGridLayoutManager gridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addOnScrollListener(getOnBottomListener(gridLayoutManager));
    }

    @Override
    public void setSwipeRefreshLayout() {
        swipeRefreshLayout.setColorSchemeResources(R.color.refresh_process1, R.color.refresh_process2, R.color.refresh_process3);

    }

    @Override
    public void showMore(@Nullable List list) {
        int size = mGirls.size();
        for (Object girl : list) {
            mGirls.add((Girl) girl);
        }
        if (recyclerView!=null) {
            recyclerView.requestLayout();
        }
        if (mGirls.size() - size == 10) {
            page++;
        }
    }

    @Override
    public void finishRefresh() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showSnackBar() {
        SnackBarUtil.showSnackBar(recyclerView, this);
    }

    @Override
    public void onRefresh() {
        if (page <= limit) {
            swipeRefreshLayout.setRefreshing(true);
            mPresenter.loadMore(page);
            mIsFirstCreated = false;
        }else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void viewGirlPhoto(Girl girl) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        GirlPhotoFragment fragment = GirlPhotoFragment.newInstance(girl.getUrl(), girl.getDesc());
        fragment.show(fragmentManager, "fragment_girl_photo");
        fragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.Dialog_FullScreen);
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    /**
     * The code segment is I learn from Meizhi created by drakeet
     * Meizhi is under the terms of the GNU General Public License as published by
     * the Free Software Foundation, either version 3 of the License, or
     * (at your option) any later version.
     */
    RecyclerView.OnScrollListener getOnBottomListener(final StaggeredGridLayoutManager layoutManager) {
        RecyclerView.OnScrollListener bottomListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int[] lastVisiblePositions = new int[2];
                lastVisiblePositions = layoutManager.findLastCompletelyVisibleItemPositions(lastVisiblePositions);
                int right = lastVisiblePositions[1];
                boolean isBottom = right > mGirlAdapter.getItemCount() - 7;
                if (isBottom && !swipeRefreshLayout.isRefreshing()) {
                    if (!mIsFirstTouchedBottom) {
                            onRefresh();
                    } else mIsFirstTouchedBottom = false;
                }
            }
        };
        return bottomListener;
    }


    @Override
    public void setPresenter(GirlContract.Presenter presenter) {
        mPresenter = presenter;
    }


}
