package com.mario99ukdw.bakingapp.ui.fragment;

import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
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

    private static final String STATE_VAR_NAME_STEP = "step";
    private static final String STATE_VAR_NAME_PLAY_WHEN_READY = "play_when_ready";
    private static final String STATE_VAR_NAME_PLAYER_LAST_POSITION = "player_last_position";

    private Step step;

    private DataSource.Factory mediaDataSourceFactory;
    private SimpleExoPlayer player;
    private DefaultTrackSelector trackSelector;
    private BandwidthMeter bandwidthMeter;

    private boolean playWhenReady = true;
    private long playerLastPosition = 0;
    private boolean isVisible = true;

    OnStepDetailListener onStepDetailListener;

    @Nullable @BindView(R.id.step_description_text_view) TextView stepDescriptionTextView;
    @BindView(R.id.player_view)  SimpleExoPlayerView simpleExoPlayerView;
    @BindView(R.id.no_video_text_view) TextView noVideoTextView;
    @Nullable @BindView(R.id.big_image_view) ImageView bigImageView;
    @Nullable @BindView(R.id.thumb_image_view) ImageView thumbImageView;

    public interface OnStepDetailListener {
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

        if (step == null) step = getArguments().getParcelable(ARGUMENT_VAR_NAME_STEP);

        bandwidthMeter = new DefaultBandwidthMeter();
        mediaDataSourceFactory = new DefaultDataSourceFactory(this.getContext(), Util.getUserAgent(this.getContext(), "mediaPlayerSample"), (TransferListener<? super DataSource>) bandwidthMeter);

        Log.d(LOG_TAG, "onCreateView is called");
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playerLastPosition = player.getCurrentPosition();
        }
        outState.putBoolean(STATE_VAR_NAME_PLAY_WHEN_READY, playWhenReady);
        outState.putLong(STATE_VAR_NAME_PLAYER_LAST_POSITION, playerLastPosition);

        Log.d(LOG_TAG, "onSaveInstanceState is called");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            this.playWhenReady = savedInstanceState.getBoolean(STATE_VAR_NAME_PLAY_WHEN_READY, true);
            this.playerLastPosition = savedInstanceState.getLong(STATE_VAR_NAME_PLAYER_LAST_POSITION, 0);
        }

        refreshUI();

        Log.d(LOG_TAG, "onActivityCreated is called");
    }

    @Optional @OnClick(R.id.previous_text_view)
    public void OnPreviousClick() {
        if (onStepDetailListener != null) onStepDetailListener.OnStepPreviousClick();
    }
    @Optional @OnClick(R.id.next_text_view)
    public void OnNextClick() {
        if (onStepDetailListener != null) onStepDetailListener.OnStepNextClick();
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
            player.seekTo(playerLastPosition);
            player.setPlayWhenReady(playWhenReady);

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
            playWhenReady = player.getPlayWhenReady();
            playerLastPosition = player.getCurrentPosition();
            player.setPlayWhenReady(false);

            player.release();
            player = null;
            trackSelector = null;
        }
    }

    public void setStep(Step step) {
        if (simpleExoPlayerView != null) {
            loadStep(step, false);
        } else{
            this.step = step;
        }
    }
    public void loadStep(Step step) {
        loadStep(step, true);
    }

    public void loadStep(Step step, boolean resetState) {
        this.step = step;
        Log.d(LOG_TAG, "loadStep is called");

        refreshUI();
        releasePlayer();

        if (resetState) {
            playWhenReady = true;
            playerLastPosition = 0;
        }
        initializePlayer();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayerWithDelay();
        }
        Log.d(LOG_TAG, "onStart is called");
    }

    @Override
    public void onResume() {
        super.onResume();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayerWithDelay();
        }
        Log.d(LOG_TAG, "onResume is called");
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

    public void setOnStepDetailListener(OnStepDetailListener listener) {
        onStepDetailListener = listener;
    }
}
