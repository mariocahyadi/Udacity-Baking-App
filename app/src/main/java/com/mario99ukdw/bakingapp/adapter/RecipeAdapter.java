package com.mario99ukdw.bakingapp.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mario99ukdw.bakingapp.R;
import com.mario99ukdw.bakingapp.schema.json.Recipe;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mario99ukdw on 09.08.2017.
 */

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    List<Recipe> mRecipes;
    Context mContext;
    int resource_item_list = 0;

    public RecipeAdapter(List<Recipe> recipes, Context context) {
        mRecipes = recipes;
        mContext = context;
    }
    public void setLayout(int resourceId) {
        resource_item_list = resourceId;
    }

    @Override
    public int getItemCount() {
        return mRecipes.size();
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v;
        if (resource_item_list > 0) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(resource_item_list, viewGroup, false);
        } else {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recipe_item_list, viewGroup, false);
        }
        RecipeViewHolder pvh = new RecipeViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder viewHolder, int position) {
        Recipe recipe = mRecipes.get(position);
        viewHolder.nameTextView.setText(mRecipes.get(position).getName());

        //recipe.setImage("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQVwZTCCVWZ06f-2X-N1GsHMlgT9MwD7HuC1T5SoadzJFEA896-UA");
        if (viewHolder.imageView != null && mContext != null) {
            viewHolder.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            if (!TextUtils.isEmpty(recipe.getImage())) {
                Picasso.with(mContext)
                        .load(recipe.getImage())
                        .placeholder(R.drawable.no_image_available_md)
                        .error(R.drawable.no_image_available_md)      // Image to load when something goes wrong
                        .resize(200, 0)
                        .into(viewHolder.imageView);
                Log.d("RecipeAdapter", "load image " + recipe.getImage());
            } else {
                // load default image
            }
        }
        //viewHolder.imageView.setImageResource(mRecipes.get(position).getImage());
    }
    public Recipe getItem(int position) {
        return mRecipes.get(position);
    }


    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        @Nullable @BindView(R.id.card_view) CardView cardView;
        @BindView(R.id.name_text_view) TextView nameTextView;
        @Nullable @BindView(R.id.image_view) ImageView imageView;

        RecipeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
