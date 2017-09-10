package com.mario99ukdw.bakingapp.ui.fragment;

import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mario99ukdw.bakingapp.MainActivity;
import com.mario99ukdw.bakingapp.R;
import com.mario99ukdw.bakingapp.adapter.StepAdapter;
import com.mario99ukdw.bakingapp.provider.RecipeContract;
import com.mario99ukdw.bakingapp.provider.RecipeProvider;
import com.mario99ukdw.bakingapp.schema.json.Ingredient;
import com.mario99ukdw.bakingapp.schema.json.Recipe;
import com.mario99ukdw.bakingapp.schema.json.Step;
import com.mario99ukdw.bakingapp.ui.view.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by mario99ukdw on 17.08.2017.
 */

public class StepListFragment extends Fragment {
    private static final String LOG_TAG = RecipeListFragment.class.getSimpleName();
    private static final String STATE_VAR_NAME_SCROLL_Y = "scroll_y";

    Recipe recipe;
    ArrayList<Ingredient> ingredients;

    OnStepClickListener onStepClickListener;

    @BindView(R.id.step_recycler_view) RecyclerView mStepRecyclerView;
    @BindView(R.id.content_scroll_view) ScrollView mContentScrollView;
    @BindView(R.id.no_data_text_view) TextView mNoDataTextView;
    @BindView(R.id.ingredient_text_view) TextView mIngredientTextView;

    public interface OnStepClickListener {
        void OnStepClick(Step step);
    }

    public static StepListFragment newInstance(Recipe recipe){
        StepListFragment fragment = new StepListFragment();
        Bundle b = new Bundle();
        b.putParcelable("recipe", recipe);
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
        View view = inflater.inflate(R.layout.fragment_step_list, container, false);
        ButterKnife.bind(this, view);

        if (savedInstanceState != null) {
            float scroll_y = savedInstanceState.getFloat(STATE_VAR_NAME_SCROLL_Y);
            mContentScrollView.setY(scroll_y);
            Log.d(LOG_TAG, "scroll view set Y = " + scroll_y);
        }

        mStepRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this.getContext(), mStepRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                    if (onStepClickListener != null) {
                        Step step = ((StepAdapter) mStepRecyclerView.getAdapter()).getItem(position);
                        onStepClickListener.OnStepClick(step);
                    }
                    }
                })
        );
        // set empty adapter first while loading data from server
        mStepRecyclerView.setAdapter(new StepAdapter(new ArrayList<Step>(), this.getContext()));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
        mStepRecyclerView.setLayoutManager(layoutManager);

        Recipe recipe = getArguments().getParcelable(MainActivity.EXTRA_VAR_NAME_RECIPE);

        loadStepListToRecyclerView(recipe.getSteps());
        loadIngredientsFromLocalData(recipe.getId());

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        float scroll_y = mContentScrollView.getY();
        outState.putFloat(STATE_VAR_NAME_SCROLL_Y, scroll_y);
        Log.d(LOG_TAG, "scroll view save Y = " + scroll_y);

        super.onSaveInstanceState(outState);
    }

    /**
     *  load list from content provider into grid view
     */
    protected void loadIngredientsFromLocalData(int recipeId) {
        ingredients = new ArrayList<>();

        String[] projection = new String[]{
                RecipeContract.IngredientsColumn.COL_QUANTITY, // 0
                RecipeContract.IngredientsColumn.COL_MEASURE, // 1
                RecipeContract.IngredientsColumn.COL_INGREDIENT, // 2
        };

        final Cursor cursor = getActivity().getContentResolver().query(RecipeProvider.getIngredientContentUri(recipeId),projection,null,null,null);
        Log.d(LOG_TAG, "query ingredient count:" + cursor.getCount() );
        if (cursor.getCount()!=0) {
            while(cursor.moveToNext()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setQuantity(cursor.getDouble(0));
                ingredient.setMeasure(cursor.getString(1));
                ingredient.setIngredient(cursor.getString(2));

                ingredients.add(ingredient);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (Ingredient ingredient : ingredients) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(ingredient.getFormatedText());
            Log.d(LOG_TAG, "ingredient formated text : " + ingredient.getFormatedText());
        }
        Log.d(LOG_TAG, "ingredient formated text : " + sb.toString());

        mIngredientTextView.setText(sb.toString());
    }

    /**
     *  load list into grid view
     */
    private void loadStepListToRecyclerView(List<Step> steps) {
        StepAdapter adapter = new StepAdapter(steps, this.getContext());
        mStepRecyclerView.setAdapter(adapter);

        if (isMultipane() && steps.size() > 0) adapter.setSelectedPosition(0);

        mNoDataTextView.setVisibility(steps.size() == 0 ? View.VISIBLE : View.GONE);
        mContentScrollView.setVisibility(steps.size() > 0 ? View.VISIBLE : View.GONE);
    }

    public void setOnStepClickListener(OnStepClickListener listener) {
        onStepClickListener = listener;
    }

    private boolean isMultipane() {
        boolean isTablet = getContext().getResources().getBoolean(R.bool.isTablet);

        return isTablet && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
}
