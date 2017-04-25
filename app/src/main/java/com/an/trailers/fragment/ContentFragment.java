package com.an.trailers.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.an.trailers.Constants;
import com.an.trailers.R;
import com.an.trailers.adapter.CommonPagerAdapter;
import com.an.trailers.callback.MovieResponseListener;
import com.an.trailers.model.Movie;
import com.an.trailers.service.RESTExecutorService;
import com.an.trailers.service.VolleyTask;
import com.an.trailers.utils.BaseUtils;
import com.an.trailers.views.CustPagerTransformer;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;
import java.util.List;


public class ContentFragment extends BaseFragment implements MovieResponseListener {

    private ViewPager viewPager;
    private ViewPager viewPagerBackground;
    private View overlayView;
    private View containerView;
    private CommonPagerAdapter adapter;

    private String url;
    private int currentPage = 1;
    private long totalPages;
    private List<CommonFragment> fragments = new ArrayList<>();
    private List<Movie> movies = new ArrayList<>();

    private View emptyContainer;

    public static ContentFragment newInstance(String method) {
        ContentFragment contentFragment = new ContentFragment();
        Bundle bundle = new Bundle();
        bundle.putString(String.class.getName(), method);
        contentFragment.setArguments(bundle);
        return contentFragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.url = getArguments().getString(String.class.getName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        overlayView = rootView.findViewById(R.id.overlay);
        containerView = rootView.findViewById(R.id.fr_container);
        viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        viewPagerBackground = (ViewPager) rootView.findViewById(R.id.viewPagerBackground);
        emptyContainer = rootView.findViewById(R.id.emptyContainer);

        fillViewPager();

        if(url == null) fetchFavMovies();
        else RESTExecutorService.submit(new VolleyTask(activity, null, String.valueOf(currentPage), url, this));

        return rootView;
    }

    private void fillViewPager() {
        viewPager.setPageTransformer(false, new CustPagerTransformer(activity));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int index = 0;
            private int pos = 0;

            @Override
            public void onPageScrolled(final int position, float positionOffset, int positionOffsetPixels) {
                if(movies.isEmpty()) return;
                if(pos > 0) return;
                String imageUrl = String.format(Constants.IMAGE_URL, movies.get(position).getPosterPath());
                Glide.with(activity).load(imageUrl).asBitmap().into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap loadedImage, GlideAnimation<? super Bitmap> glideAnimation) {
                        bindBackgroundImage(loadedImage, containerView, overlayView);
                        pos++;
                    }
                });
            }

            @Override
            public void onPageSelected(int position) {
                index = position;
                if(url != null && currentPage <= totalPages && (position+2) == adapter.getCount()) {
                    RESTExecutorService.submit(new VolleyTask(activity, null, String.valueOf(currentPage), url, ContentFragment.this));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    viewPagerBackground.setCurrentItem(index);
                }
            }
        });
    }

    @Override
    public void onMoviesResponse(List<Movie> movies, int currentPage, long totalPages) {
        if(movies == null || movies.isEmpty()) {
            handleEmptyResponse();
        } else {
            this.movies.addAll(movies);
            this.totalPages = totalPages;
            this.currentPage = currentPage + 1;
            if (this.movies.size() == movies.size()) {
                handleFirstMovieResponse();
            } else handleMovieRepsonse(movies);
        }
    }

    private void handleFirstMovieResponse() {
        for (Movie movie : movies) {
            if(movie.getPosterPath() != null)
                fragments.add(CommonFragment.newInstance(movie));
        }
        adapter = new CommonPagerAdapter(getChildFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
    }

    private void handleMovieRepsonse(List<Movie> movieList) {
        for (Movie movie : movieList) {
            if(movie.getPosterPath() != null)
                adapter.addFragment(CommonFragment.newInstance(movie));
        }
        adapter.notifyDataSetChanged();
    }

    private void handleEmptyResponse() {
        emptyContainer.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.GONE);
    }

    private void fetchFavMovies() {
        this.movies = BaseUtils.getFavMovies();
        if(movies == null || movies.isEmpty()) {
            handleEmptyResponse();
        } else handleFirstMovieResponse();
    }

    @Override
    public void bindImage(Bitmap loadedImage) {
        bindBackgroundImage(loadedImage, containerView, overlayView);
    }
}