package com.mario99ukdw.bakingapp.ui.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mario99ukdw.bakingapp.DetailActivity;
import com.mario99ukdw.bakingapp.MainActivity;
import com.mario99ukdw.bakingapp.R;
import com.mario99ukdw.bakingapp.adapter.RecipeAdapter;
import com.mario99ukdw.bakingapp.net.NetworkUtil;
import com.mario99ukdw.bakingapp.net.RecipeApiService;
import com.mario99ukdw.bakingapp.net.RecipeRetroClient;
import com.mario99ukdw.bakingapp.provider.RecipeContract;
import com.mario99ukdw.bakingapp.provider.RecipeProvider;
import com.mario99ukdw.bakingapp.schema.json.Ingredient;
import com.mario99ukdw.bakingapp.schema.json.Recipe;
import com.mario99ukdw.bakingapp.schema.json.Step;
import com.mario99ukdw.bakingapp.test.SimpleIdlingResource;
import com.mario99ukdw.bakingapp.ui.view.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by mario99ukdw on 13.08.2017.
 */

public class RecipeListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String LOG_TAG = RecipeListFragment.class.getSimpleName();
    private static final String LOG_TAG_ACTIVITY = "Activity";

    final static String VAR_NAME_RECIPE_ARRAY_LIST = "recipeArrayList";
    final static String VAR_NAME_RECIPE_FIRST_VISIBLE_POSITION = "firstVisiblePosition";

    ArrayList<Recipe> recipes;

    @BindView(R.id.recipe_recycler_view) RecyclerView mRecipeRecyclerView;
    @BindView(R.id.content_scroll_view) NestedScrollView mContentScrollView;
    @BindView(R.id.no_data_text_view) TextView mNoDataTextView;
    @SuppressWarnings("WeakerAccess") @BindView(R.id.swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;

    protected ProgressDialog mProgressDialog;

    @VisibleForTesting
    private SimpleIdlingResource getIdlingResource() {
        MainActivity mainActivity = (MainActivity) getActivity();
        return mainActivity.getIdlingResource();
    }

    @VisibleForTesting
    private void setIdlingResourceIdleState(boolean state) {
        SimpleIdlingResource idlingResource = getIdlingResource();
        if (idlingResource != null) idlingResource.setIdleState(state);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);
        ButterKnife.bind(this, view);

        swipeRefreshLayout.setOnRefreshListener(this);

        mRecipeRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this.getContext(), mRecipeRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Recipe recipe = ((RecipeAdapter) mRecipeRecyclerView.getAdapter()).getItem(position);

                        Intent intent = new Intent(getActivity(), DetailActivity.class);
                        Bundle extras = new Bundle();
                        extras.putParcelable(MainActivity.EXTRA_VAR_NAME_RECIPE, recipe);
                        intent.putExtras(extras);
                        startActivity(intent);
                    }
                })
        );

        setRecyclerViewLayoutManager();

        setIdlingResourceIdleState(false);

        Log.d(LOG_TAG_ACTIVITY, "onCreateView is called (fragment)");

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        restoreInstanceState(savedInstanceState);

        Log.d(LOG_TAG_ACTIVITY, "onActivityCreated is called (fragment)");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);

        if (recipes != null) {
            int firstVisiblePosition = 0;
            if (isTablet) {
                GridLayoutManager layoutManager = (GridLayoutManager) mRecipeRecyclerView.getLayoutManager();
                firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
            } else {
                LinearLayoutManager layoutManager = (LinearLayoutManager) mRecipeRecyclerView.getLayoutManager();
                firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
            }

            outState.putParcelableArrayList(VAR_NAME_RECIPE_ARRAY_LIST, recipes);
            outState.putInt(VAR_NAME_RECIPE_FIRST_VISIBLE_POSITION, firstVisiblePosition);

            Log.d(LOG_TAG_ACTIVITY, "onSaveInstanceState : recipes is saved");
        }

        Log.d(LOG_TAG_ACTIVITY, "onSaveInstanceState is called (fragment)");
    }

    /**
     *  call when came back from another activity or minimize at background
     */
    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            // set empty adapter first while loading data from server
            mRecipeRecyclerView.setAdapter(new RecipeAdapter(new ArrayList<Recipe>(), this.getContext()));

            loadRecipesFromServer();
        } else {
            // Get data from local resources
            recipes = savedInstanceState.getParcelableArrayList(VAR_NAME_RECIPE_ARRAY_LIST);

            if (recipes != null) {
                loadRecipeListToRecyclerView(recipes);
                Log.d(LOG_TAG_ACTIVITY, "onCreate : recipes is loaded from state");
            } else {
                loadRecipesFromServer();
            }
        }
    }

    protected void setRecyclerViewLayoutManager() {
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);

        if (isTablet) {
            int column_numbers = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 4 : 6;

            GridLayoutManager layoutManager = new GridLayoutManager(this.getContext(), column_numbers);
            mRecipeRecyclerView.setLayoutManager(layoutManager);
        } else {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext());
            mRecipeRecyclerView.setLayoutManager(layoutManager);
        }
    }

    /**
     *  show progress dialog
     */
    protected void showProgressDialog(String text) {
        mProgressDialog = new ProgressDialog(this.getContext());
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // STYLE_HORIZONTAL
        // http://stackoverflow.com/questions/19655715/progress-dialog-is-closed-when-touch-on-screen
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                // DO SOME STUFF HERE
            }
        });

        mProgressDialog.setMessage(text);
        mProgressDialog.show();
    }

    /**
     *  hide progress dialog
     */
    protected void hideProgressDialog() {
        if (mProgressDialog != null) mProgressDialog.cancel();
    }

    /**
     *  save recipes into local data using content provider
     */
    protected void saveRecipes(List<Recipe> recipes) {
        getActivity().getContentResolver().delete(RecipeProvider.RECIPE_CONTENT_URI, null, null);
        getActivity().getContentResolver().delete(RecipeProvider.INGREDIENT_ALL_CONTENT_URI, null, null);
        getActivity().getContentResolver().delete(RecipeProvider.STEP_ALL_CONTENT_URI, null, null);

        for (Recipe recipe : recipes) {
            ContentValues values = new ContentValues();
            values.put(RecipeContract.RecipesColumn.COL_ID, recipe.getId());
            values.put(RecipeContract.RecipesColumn.COL_NAME, recipe.getName());
            values.put(RecipeContract.RecipesColumn.COL_SERVINGS, recipe.getServings());
            values.put(RecipeContract.RecipesColumn.COL_IMAGE, recipe.getImage());

            Uri uri = getActivity().getContentResolver().insert(RecipeProvider.RECIPE_CONTENT_URI, values);
            Log.d(LOG_TAG, "insert recipe name=" + recipe.getName());

            List<Ingredient> ingredients = recipe.getIngredients();
            if (ingredients.size() > 0) {
                ContentValues[] ingredientValues = new ContentValues[ingredients.size()];
                int i = 0;
                for (Ingredient ingredient : ingredients) {
                    ingredientValues[i] = new ContentValues();
                    ingredientValues[i].put(RecipeContract.IngredientsColumn.COL_QUANTITY, ingredient.getQuantity());
                    ingredientValues[i].put(RecipeContract.IngredientsColumn.COL_MEASURE, ingredient.getMeasure());
                    ingredientValues[i].put(RecipeContract.IngredientsColumn.COL_INGREDIENT, ingredient.getIngredient());
                    ingredientValues[i].put(RecipeContract.IngredientsColumn.COL_RECIPE_ID, recipe.getId());
                    Log.d(LOG_TAG, "insert ingredient name=" + ingredient.getIngredient());

                    i++;
                }

                getActivity().getContentResolver().bulkInsert(RecipeProvider.getIngredientContentUri(recipe.getId()), ingredientValues);
                Log.d(LOG_TAG, "bulk insert ingredient for recipe=" + recipe.getName());
            } else {
                Log.d(LOG_TAG, "nothing to insert for ingredient");

            }

            List<Step> steps = recipe.getSteps();
            if (steps.size() > 0) {
                ContentValues[] stepValues = new ContentValues[steps.size()];
                int i = 0;
                for (Step step : steps) {
                    stepValues[i] = new ContentValues();
                    stepValues[i].put(RecipeContract.StepsColumn.COL_STEP_NUMBER, step.getId());
                    stepValues[i].put(RecipeContract.StepsColumn.COL_SHORT_DESCRIPTION, step.getShortDescription());
                    stepValues[i].put(RecipeContract.StepsColumn.COL_DESCRIPTION, step.getDescription());
                    stepValues[i].put(RecipeContract.StepsColumn.COL_VIDEO_URL, step.getVideoURL());
                    stepValues[i].put(RecipeContract.StepsColumn.COL_THUMBNAIL_URL, step.getThumbnailURL());
                    stepValues[i].put(RecipeContract.StepsColumn.COL_RECIPE_ID, recipe.getId());
                    Log.d(LOG_TAG, "insert step number=" + step.getId());

                    i++;
                }

                getActivity().getContentResolver().bulkInsert(RecipeProvider.getStepContentUri(recipe.getId()), stepValues);
                Log.d(LOG_TAG, "bulk insert step for recipe=" + recipe.getName());
            } else {
                Log.d(LOG_TAG, "nothing to insert for step");

            }
        }

        getActivity().getContentResolver().notifyChange(RecipeProvider.RECIPE_CONTENT_URI, null);

        loadRecipesFromLocalData();
    }

    /**
     *  load list from server into grid view
     */
    private void loadRecipesFromServer() {
        if (NetworkUtil.isNetworkAvailable(getContext())) {
            swipeRefreshLayout.setRefreshing(true);
            //showProgressDialog(getResources().getString(R.string.message_please_wait));

            RecipeApiService api = RecipeRetroClient.getApiService();

            Call<List<Recipe>> call = api.getRecipes();
            Log.d(LOG_TAG, "Load recipes from url: " + call.request().url());

            call.enqueue(new Callback<List<Recipe>>() {
                @Override
                public void onResponse(Call<List<Recipe>> call, Response<List<Recipe>> response) {
                    if(response.isSuccessful()) {
                        Log.d(LOG_TAG, "Load recipes from server is success");
                        saveRecipes(response.body());
                    } else {
                        Log.d(LOG_TAG, "Load recipes from server is not success");
                        //Toast.makeText(getActivity(), R.string.toast_load_data_from_server_failed, Toast.LENGTH_LONG).show();
                        showSnackbarMessage(getString(R.string.toast_load_data_from_server_failed));
                        loadRecipesFromLocalData();
                    }
                    hideProgressDialog();
                }

                @Override
                public void onFailure(Call<List<Recipe>> call, Throwable t) {
                    Log.d(LOG_TAG, "Load recipes from server is failed with message: " + t.getMessage());
                    //Toast.makeText(getActivity(), R.string.toast_load_data_from_server_failed, Toast.LENGTH_LONG).show();
                    showSnackbarMessage(getString(R.string.toast_load_data_from_server_failed));
                    loadRecipesFromLocalData();
                    t.printStackTrace();
                    hideProgressDialog();
                }
            });
        } else {
            //Toast.makeText(getActivity(), R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
            showSnackbarMessage(getString(R.string.toast_no_connectivity));
            loadRecipesFromLocalData();
        }
    }

    /**
     *  load list from content provider into grid view
     */
    protected void loadRecipesFromLocalData() {
        swipeRefreshLayout.setRefreshing(false);

        recipes = new ArrayList<>();
        String[] projection = new String[]{
                RecipeContract.RecipesColumn.COL_ID, // 0
                RecipeContract.RecipesColumn.COL_NAME, // 1
                RecipeContract.RecipesColumn.COL_IMAGE, // 2
        };
        final Cursor cursor = getActivity().getContentResolver().query(RecipeProvider.RECIPE_CONTENT_URI,projection,null,null,null);
        Log.d(LOG_TAG, "query recipe count:" + cursor.getCount() );
        if (cursor.getCount()!=0) {
            while(cursor.moveToNext()) {
                Recipe recipe = new Recipe();
                recipe.setId(cursor.getInt(0));
                recipe.setName(cursor.getString(1));
                recipe.setImage(cursor.getString(2));
                recipes.add(recipe);
            }
        }

        loadRecipeListToRecyclerView(recipes);

        setIdlingResourceIdleState(true);
    }

    /**
     *  load list into grid view
     */
    private void loadRecipeListToRecyclerView(ArrayList<Recipe> recipes) {
        RecipeAdapter adapter = new RecipeAdapter(recipes, this.getContext());
        mRecipeRecyclerView.setAdapter(adapter);

        mNoDataTextView.setVisibility(recipes.size() == 0 ? View.VISIBLE : View.GONE);
        mContentScrollView.setVisibility(recipes.size() > 0 ? View.VISIBLE : View.GONE);
    }

    public void showSnackbarMessage(String text) {
        Snackbar.make(mContentScrollView, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.d(LOG_TAG_ACTIVITY, "onAttach is called (fragment)");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG_ACTIVITY, "onCreate is called (fragment)");
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(LOG_TAG_ACTIVITY, "onStart is called (fragment)");
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(LOG_TAG_ACTIVITY, "onResume is called (fragment)");
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(LOG_TAG_ACTIVITY, "onPause is called (fragment)");
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(LOG_TAG_ACTIVITY, "onStop is called (fragment)");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d(LOG_TAG_ACTIVITY, "onDestroyView is called (fragment)");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(LOG_TAG_ACTIVITY, "onDestroy is called (fragment)");
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Log.d(LOG_TAG_ACTIVITY, "onDetach is called (fragment)");
    }

    @Override
    public void onRefresh() {
        loadRecipesFromServer();
    }
}

