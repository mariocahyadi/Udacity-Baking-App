package com.mario99ukdw.bakingapp;

import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.mario99ukdw.bakingapp.provider.RecipeContract;
import com.mario99ukdw.bakingapp.provider.RecipeProvider;
import com.mario99ukdw.bakingapp.schema.json.Recipe;
import com.mario99ukdw.bakingapp.schema.json.Step;
import com.mario99ukdw.bakingapp.ui.fragment.StepDetailFragment;
import com.mario99ukdw.bakingapp.ui.fragment.StepListFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements StepListFragment.OnStepClickListener, StepDetailFragment.OnStepDetailListener {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    public static final String STATE_VAR_NAME_RECIPE = "recipe";
    public static final String STATE_VAR_NAME_STEP_POSITION = "step_position";

    private int stepPosition = -1;
    private Recipe recipe;

    @Nullable @BindView(R.id.single_pane_layout) RelativeLayout singlePaneLayout;
    @BindView(R.id.left_panel) FrameLayout leftPanel;
    @BindView(R.id.right_panel) FrameLayout rightPanel;

    StepDetailFragment stepDetailFragment;
    StepListFragment stepListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // handle fragment creation by this method, https://stackoverflow.com/questions/13305861/fool-proof-way-to-handle-fragment-on-orientation-change
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        Log.d(LOG_TAG, "onCreate is called");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            recipe = extras.getParcelable(MainActivity.EXTRA_VAR_NAME_RECIPE);

            if (recipe == null) {
                finish();
            } else {
                List<Step> steps = loadStepsFromLocalData(recipe.getId());
                recipe.setSteps(steps);

                getSupportActionBar().setTitle(recipe.getName());

                Step step = Step.createEmptyStep();
                if (savedInstanceState != null) {
                    stepPosition = savedInstanceState.getInt(STATE_VAR_NAME_STEP_POSITION, 0);
                }

                if (singlePaneLayout == null && stepPosition < 0) stepPosition = 0;
                if (stepPosition >= 0 && stepPosition < steps.size()) step = steps.get(stepPosition);

                stepListFragment = (StepListFragment) getSupportFragmentManager().findFragmentById(R.id.left_panel);
                if (stepListFragment == null) {
                    stepListFragment = StepListFragment.newInstance(recipe);
                    getSupportFragmentManager().beginTransaction()
                        .add(R.id.left_panel, stepListFragment)
                        .commit();
                }
                stepListFragment.setOnStepClickListener(this);

                stepDetailFragment = (StepDetailFragment) getSupportFragmentManager().findFragmentById(R.id.right_panel);
                if (stepDetailFragment == null) {
                    stepDetailFragment = StepDetailFragment.newInstance(step);
                    getSupportFragmentManager().beginTransaction()
                        .add(R.id.right_panel, stepDetailFragment)
                        .commit();
                }
                stepDetailFragment.setOnStepDetailListener(this);

                if (singlePaneLayout != null) {
                    if (step.getId() >= 0) {
                        showStepDetailFragment();
                    } else {
                        hideStepDetailFragment();
                    }
                } else {
                    showMultipaneFragment();
                }

            }
        } else {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(STATE_VAR_NAME_STEP_POSITION, stepPosition);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (singlePaneLayout != null && rightPanel.getVisibility() == View.VISIBLE) {
            hideStepDetailFragment();
            stepListFragment.clearStepSelection();
            stepPosition = -1;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void OnStepClick(Step step) {
        Log.d(LOG_TAG, "Step clicked " + step.getDescription());

        stepPosition = step.getId();
        loadStepDetail(step);
        Log.d(LOG_TAG, "OnStepClicked " + step.getDescription());

        if (singlePaneLayout != null) {
            showStepDetailFragment();
            Log.d(LOG_TAG, "OnStepClicked show detail fragment");
        }
    }

    @Override
    public void OnStepPreviousClick() {
        if (stepPosition > 0) {
            stepPosition--;
            try {
                Step step = recipe.getSteps().get(stepPosition);
                loadStepDetail(step);
                Log.d(LOG_TAG, "OnStepPreviousClick with stepPosition " + stepPosition);
            } catch(ArrayIndexOutOfBoundsException ex) {
                // handle error here
                stepPosition++;
            }
        }
    }

    @Override
    public void OnStepNextClick() {
        if (stepPosition < recipe.getSteps().size() - 1) {
            stepPosition++;
            try {
                Step step = recipe.getSteps().get(stepPosition);
                loadStepDetail(step);
                Log.d(LOG_TAG, "OnStepNextClick with stepPosition " + stepPosition);
            } catch(ArrayIndexOutOfBoundsException ex) {
                // handle error here
                stepPosition--;
            }
        }
    }

    private void loadStepDetail(Step step) {
        if (stepDetailFragment != null) {
            stepDetailFragment.loadStep(step);
        }
    }

    /**
     *  load list from content provider into grid view
     */
    protected List<Step> loadStepsFromLocalData(int recipeId) {
        List<Step> steps = new ArrayList<>();

        String[] projection = new String[]{
                RecipeContract.StepsColumn.COL_STEP_NUMBER, // 0
                RecipeContract.StepsColumn.COL_SHORT_DESCRIPTION, // 1
                RecipeContract.StepsColumn.COL_DESCRIPTION, // 2
                RecipeContract.StepsColumn.COL_VIDEO_URL, // 3
                RecipeContract.StepsColumn.COL_THUMBNAIL_URL, // 4
        };

        final Cursor cursor = getContentResolver().query(RecipeProvider.getStepContentUri(recipeId),projection,null,null,null);
        Log.d(LOG_TAG, "query step count:" + cursor.getCount() );
        if (cursor.getCount()!=0) {
            while(cursor.moveToNext()) {
                Step step = new Step();
                step.setId(cursor.getInt(0));
                step.setShortDescription(cursor.getString(1));
                step.setDescription(cursor.getString(2));
                step.setVideoURL(cursor.getString(3));
                step.setThumbnailURL(cursor.getString(4));

                steps.add(step);
            }
        }

        return steps;
    }

    private void showMultipaneFragment() {
        leftPanel.setVisibility(View.VISIBLE);
        rightPanel.setVisibility(View.VISIBLE);
        stepListFragment.setStepPosition(stepPosition);
        stepDetailFragment.setStep(recipe.getSteps().get(stepPosition));
    }
    private void showStepDetailFragment() {
        leftPanel.setVisibility(View.GONE);
        rightPanel.setVisibility(View.VISIBLE);
        stepDetailFragment.setStep(recipe.getSteps().get(stepPosition));
    }
    private void hideStepDetailFragment() {
        leftPanel.setVisibility(View.VISIBLE);
        rightPanel.setVisibility(View.GONE);
        stepListFragment.setStepPosition(-1);
        stepDetailFragment.setStep(Step.createEmptyStep());
    }
}
