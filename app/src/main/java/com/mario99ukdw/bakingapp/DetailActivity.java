package com.mario99ukdw.bakingapp;

import android.content.res.Configuration;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.mario99ukdw.bakingapp.provider.RecipeContract;
import com.mario99ukdw.bakingapp.provider.RecipeProvider;
import com.mario99ukdw.bakingapp.schema.json.Recipe;
import com.mario99ukdw.bakingapp.schema.json.Step;
import com.mario99ukdw.bakingapp.ui.fragment.StepListFragment;
import com.mario99ukdw.bakingapp.ui.fragment.StepDetailFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements StepListFragment.OnStepClickListener, StepDetailFragment.OnStepDetailClickListener {
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    public static final String INTENT_PARCEL_NAME_RECIPE_ID = "recipe_id";

    private int stepPosition = 0;
    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

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

                if (isMultipane()) {
                    FragmentManager fm = getSupportFragmentManager();
                    for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                        fm.popBackStack();
                    }

                    StepListFragment stepListFragment  = (StepListFragment) getSupportFragmentManager().findFragmentById(R.id.left_panel);
                    if (stepListFragment == null) {
                        stepListFragment = StepListFragment.newInstance(recipe);
                        stepListFragment.setOnStepClickListener(this);
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.left_panel, stepListFragment)
                                .commit();
                    } else {
                        stepListFragment.setOnStepClickListener(this);
                    }

                    StepDetailFragment stepDetailFragment  = (StepDetailFragment) getSupportFragmentManager().findFragmentById(R.id.right_panel);
                    if (stepDetailFragment == null) {
                        stepDetailFragment = StepDetailFragment.newInstance(steps.get(0));
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.right_panel, stepDetailFragment)
                                .commit();
                    }
                } else {
                    StepListFragment stepListFragment  = (StepListFragment) getSupportFragmentManager().findFragmentById(R.id.container1);
                    if (stepListFragment == null) {
                        stepListFragment = StepListFragment.newInstance(recipe);
                        stepListFragment.setOnStepClickListener(this);
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.container1, stepListFragment)
                                .commit();
                    } else {
                        stepListFragment.setOnStepClickListener(this);
                    }
                }
            }
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

    @Override
    public void OnStepClick(Step step) {
        Log.d(LOG_TAG, "Step clicked " + step.getDescription());

        stepPosition = step.getId();
        if (isMultipane()) {
            loadStepDetail(step);
            Log.d(LOG_TAG, "OnStepClicked multipane " + step.getDescription());
        } else {
            StepDetailFragment stepDetailFragment = StepDetailFragment.newInstance(step);
            stepDetailFragment.setOnStepDetailClickListener(this);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container1, stepDetailFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private boolean isMultipane() {
        boolean isTablet = getApplicationContext().getResources().getBoolean(R.bool.isTablet);

        return isTablet && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    public void OnStepPreviousClick() {
        if (stepPosition > 0) {
            stepPosition--;
            try {
                Step step = recipe.getSteps().get(stepPosition);
                loadStepDetail(step);
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
            } catch(ArrayIndexOutOfBoundsException ex) {
                // handle error here
                stepPosition--;
            }
        }
    }

    private void loadStepDetail(Step step) {
        StepDetailFragment stepDetailFragment = (StepDetailFragment) getSupportFragmentManager().findFragmentById(isMultipane() ? R.id.right_panel : R.id.container1);
        if (stepDetailFragment != null) {
            stepDetailFragment.loadStep(step);
        }
    }
}
