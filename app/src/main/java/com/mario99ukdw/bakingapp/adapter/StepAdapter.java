package com.mario99ukdw.bakingapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mario99ukdw.bakingapp.R;
import com.mario99ukdw.bakingapp.schema.json.Step;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mario99ukdw on 17.08.2017.
 */

public class StepAdapter extends RecyclerView.Adapter<StepAdapter.StepViewHolder> {
    private int selectedPosition = -1;

    private List<Step> mSteps;
    private Context mContext;

    public StepAdapter(List<Step> steps, Context context) {
        mSteps = steps;
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return mSteps.size();
    }

    @Override
    public StepAdapter.StepViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.step_item_list, viewGroup, false);
        return new StepAdapter.StepViewHolder(v);
    }

    @Override
    public void onBindViewHolder(StepAdapter.StepViewHolder viewHolder, int position) {
        Step step = mSteps.get(position);
        String stepText = step.getId() == 0 ? mContext.getResources().getString(R.string.text_step_intro) :
                mContext.getResources().getString(R.string.text_step_number, position);

        viewHolder.itemView.setSelected(selectedPosition == position);

        viewHolder.stepCaptionTextView.setText(stepText);
        viewHolder.shortDescriptionTextView.setText(step.getShortDescription());

        viewHolder.cardView.setCardBackgroundColor(selectedPosition == position ? Color.GREEN : Color.LTGRAY);

        //step.setThumbnailURL("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQVwZTCCVWZ06f-2X-N1GsHMlgT9MwD7HuC1T5SoadzJFEA896-UA");
        if (viewHolder.thumbImageView != null && mContext != null) {
            if (!TextUtils.isEmpty(step.getThumbnailURL())) {
                viewHolder.thumbImageView.setVisibility(View.VISIBLE);
                viewHolder.thumbImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                Picasso.with(mContext)
                        //.load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ4QLjk73zLFkqV1Kev_kt5U9MWO3UynIuDusup2SwiyYYjTmVHw8cbds0")
                        .load(step.getThumbnailURL())
                        //.placeholder(R.drawable.no_image_available_md)
                        //.error(R.drawable.no_image_available_md)      // Image to load when something goes wrong
                        .resize(250, 0)
                        .into(viewHolder.thumbImageView);
            } else {
                viewHolder.thumbImageView.setVisibility(View.GONE);
            }
        }
    }
    public Step getItem(int position) {
        return mSteps.get(position);
    }

    public void setSelectedPosition(int position) {
        notifyItemChanged(selectedPosition);
        selectedPosition = position;
        notifyItemChanged(selectedPosition);
    }


    class StepViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.card_view) CardView cardView;
        @BindView(R.id.step_caption_text_view) TextView stepCaptionTextView;
        @BindView(R.id.short_description_text_view) TextView shortDescriptionTextView;
        @Nullable @BindView(R.id.thumb_image_view) ImageView thumbImageView;

        StepViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

                    setSelectedPosition(getAdapterPosition());
                }
            });
        }
    }
}
