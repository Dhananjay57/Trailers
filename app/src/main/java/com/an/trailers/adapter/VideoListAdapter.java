package com.an.trailers.adapter;


import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.an.trailers.Constants;
import com.an.trailers.R;
import com.an.trailers.databinding.VideoListItemBinding;
import com.an.trailers.model.Video;
import com.an.trailers.utils.BaseUtils;
import com.an.trailers.utils.NavigationUtils;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;

import java.util.Collections;
import java.util.List;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.CustomViewHolder> {

    private int screenWidth;
    private int screenHeight;
    private final double ASPECT_RATIO_WIDTH = 61.1;
    private final double ASPECT_RATIO_HEIGHT = 20.27;

    private Context context;
    private List<Video> videoList;
    public VideoListAdapter(Context context, List<Video> videoList) {
        this.context = context;
        this.videoList = videoList;
        this.screenWidth = BaseUtils.getScreenWidth(context);
        this.screenHeight = BaseUtils.getScreenHeight(context);
    }

    public VideoListAdapter(Context context) {
        this.context = context;
        this.videoList = Collections.emptyList();
        this.screenWidth = BaseUtils.getScreenWidth(context);
        this.screenHeight = BaseUtils.getScreenHeight(context);
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        VideoListItemBinding itemBinding = VideoListItemBinding.inflate(layoutInflater, parent, false);
        CustomViewHolder viewHolder = new CustomViewHolder(itemBinding);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {


        holder.binding.youtubeThumbnail.initialize(Constants.YOUTUBE_API_KEY, new YouTubeThumbnailView.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
                final Video item = getItem(position);
                youTubeThumbnailLoader.setVideo(item.getKey());
                youTubeThumbnailView.setImageBitmap(null);


                youTubeThumbnailLoader.setOnThumbnailLoadedListener(new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {
                    @Override
                    public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
                        youTubeThumbnailView.setVisibility(View.VISIBLE);
                        holder.binding.vidFrame.setVisibility(View.VISIBLE);
                        holder.binding.btnPlay.setImageResource(R.drawable.ic_play);
                        youTubeThumbnailLoader.release();
                    }

                    @Override
                    public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {

                    }
                });
            }

            @Override
            public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {
                //write something for failure
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public Video getItem(int position) {
        return videoList.get(position);
    }

    public void setVideos(List<Video> videos) {
        this.videoList = videos;
        notifyDataSetChanged();
    }


    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final VideoListItemBinding binding;
        public CustomViewHolder(VideoListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            ViewGroup.LayoutParams lp = binding.youtubeThumbnail.getLayoutParams();

            Double width = Math.ceil((ASPECT_RATIO_WIDTH * screenWidth)/100);
            Double height = Math.ceil((ASPECT_RATIO_HEIGHT * screenHeight)/100);
            lp.width = width.intValue();
            lp.height = height.intValue();
            binding.youtubeThumbnail.setLayoutParams(lp);

            binding.youtubeThumbnail.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            NavigationUtils.redirectToVideoScreen(context, getItem(getLayoutPosition()).getKey());
        }
    }
}
