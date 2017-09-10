package com.mario99ukdw.bakingapp.ui.fragment;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.mario99ukdw.bakingapp.R;
import com.mario99ukdw.bakingapp.schema.json.Step;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * Created by mario99ukdw on 19.08.2017.
 */

public class StepDetailFragment extends Fragment {
    private static final String LOG_TAG = StepDetailFragment.class.getSimpleName();
    public static final String ARGUMENT_VAR_NAME_STEP = "step";

    private Step step;

    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private BandwidthMeter bandwidthMeter;

    OnStepDetailClickListener onStepDetailClickListener;

    @Nullable @BindView(R.id.step_description_text_view) TextView stepDescriptionTextView;
    @BindView(R.id.player_view)  SimpleExoPlayerView simpleExoPlayerView;
    @BindView(R.id.no_video_text_view) TextView noVideoTextView;
    @Nullable @BindView(R.id.big_image_view) ImageView bigImageView;
    @Nullable @BindView(R.id.thumb_image_view) ImageView thumbImageView;

    public interface OnStepDetailClickListener {
        void OnStepPreviousClick();
        void OnStepNextClick();
    }

    public static StepDetailFragment newInstance(Step step){
        StepDetailFragment fragment = new StepDetailFragment();
        Bundle b = new Bundle();
        b.putParcelable(ARGUMENT_VAR_NAME_STEP, step);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_step_detail, container, false);
        ButterKnife.bind(this, view);

        step = getArguments().getParcelable(ARGUMENT_VAR_NAME_STEP);

        refreshUI();

        bandwidthMeter = new DefaultBandwidthMeter();
        mediaDataSourceFactory = new DefaultDataSourceFactory(this.getContext(), Util.getUserAgent(this.getContext(), "mediaPlayerSample"), (TransferListener<? super DataSource>) bandwidthMeter);

        return view;
    }

    @Optional @OnClick(R.id.previous_text_view)
    public void OnPreviousClick() {
        if (onStepDetailClickListener != null) onStepDetailClickListener.OnStepPreviousClick();
    }
    @Optional @OnClick(R.id.next_text_view)
    public void OnNextClick() {
        if (onStepDetailClickListener != null) onStepDetailClickListener.OnStepNextClick();
    }

    private Uri getVideoUri() {
        if (!TextUtils.isEmpty(step.getVideoURL())) {
            return Uri.parse(step.getVideoURL());
        } else {
            return null;
        }
    }
    private void refreshUI() {
        setUITextView();
    }
    private void setUITextView() {
        if (stepDescriptionTextView != null) {
            stepDescriptionTextView.setText(step.getDescription());
        }
    }

    private void initializePlayer() {
        Uri videoUri = getVideoUri();

        if (videoUri != null) {
            simpleExoPlayerView.setVisibility(View.VISIBLE);
            simpleExoPlayerView.requestFocus();

            TrackSelection.Factory videoTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(bandwidthMeter);
            trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
            player = ExoPlayerFactory.newSimpleInstance(this.getContext(), trackSelector);
            simpleExoPlayerView.setPlayer(player);
            player.setPlayWhenReady(true);

            DefaultExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource mediaSource = new ExtractorMediaSource(videoUri, mediaDataSourceFactory, extractorsFactory, null, null);
            player.prepare(mediaSource);

            noVideoTextView.setVisibility(View.GONE);
        } else {
            noVideoTextView.setVisibility(View.VISIBLE);
            simpleExoPlayerView.setVisibility(View.INVISIBLE);
        }
    }

    private void initializePlayerWithDelay() {
        initializePlayer();
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
            trackSelector = null;
        }
    }

    public void loadStep(Step step) {
        this.step = step;

        refreshUI();
        releasePlayer();
        initializePlayer();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayerWithDelay();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayerWithDelay();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private boolean isMultipane() {
        boolean isTablet = getContext().getResources().getBoolean(R.bool.isTablet);

        return isTablet && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public void setOnStepDetailClickListener(OnStepDetailClickListener listener) {
        onStepDetailClickListener = listener;
    }
}
