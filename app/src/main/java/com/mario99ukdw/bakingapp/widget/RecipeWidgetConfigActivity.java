package com.mario99ukdw.bakingapp.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.mario99ukdw.bakingapp.DetailActivity;
import com.mario99ukdw.bakingapp.MainActivity;
import com.mario99ukdw.bakingapp.R;
import com.mario99ukdw.bakingapp.adapter.RecipeAdapter;
import com.mario99ukdw.bakingapp.provider.RecipeContract;
import com.mario99ukdw.bakingapp.provider.RecipeProvider;
import com.mario99ukdw.bakingapp.schema.json.Recipe;
import com.mario99ukdw.bakingapp.ui.fragment.RecipeListFragment;
import com.mario99ukdw.bakingapp.ui.view.RecyclerItemClickListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecipeWidgetConfigActivity extends AppCompatActivity {
    private static final String LOG_TAG = RecipeWidgetConfigActivity.class.getSimpleName();

    @BindView(R.id.recipe_recycler_view) RecyclerView mRecipeRecyclerView;

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_widget_config);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle(getString(R.string.text_select_recipe_for_widget));

        setResult(RESULT_CANCELED);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        Log.d(LOG_TAG, "AppWidgetId : " + mAppWidgetId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getApplicationContext());
        mRecipeRecyclerView.setLayoutManager(layoutManager);

        mRecipeRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this.getApplicationContext(), mRecipeRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Recipe recipe = ((RecipeAdapter) mRecipeRecyclerView.getAdapter()).getItem(position);

                        handleOnRecipeSelected(recipe);
                    }
                })
        );
        loadRecipesFromLocalData();
    }

    /**
     *  load list from content provider into grid view
     */
    protected void loadRecipesFromLocalData() {
        ArrayList<Recipe> recipes = new ArrayList<>();
        String[] projection = new String[]{
                RecipeContract.RecipesColumn.COL_ID, // 0
                RecipeContract.RecipesColumn.COL_NAME, // 1
                RecipeContract.RecipesColumn.COL_IMAGE, // 2
        };
        final Cursor cursor = getContentResolver().query(RecipeProvider.RECIPE_CONTENT_URI,projection,null,null,null);
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
    }

    /**
     *  load list into grid view
     */
    private void loadRecipeListToRecyclerView(ArrayList<Recipe> recipes) {
        RecipeAdapter adapter = new RecipeAdapter(recipes, this.getApplicationContext());
        adapter.setLayout(R.layout.recipe_item_list_simple);
        mRecipeRecyclerView.setAdapter(adapter);
    }

    private void setPreferencesSetting(Recipe recipe) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();

        editor.putInt("widget_recipe_id_" + mAppWidgetId, recipe.getId());
        editor.putString("widget_recipe_name_" + mAppWidgetId, recipe.getName());
        editor.apply();

        Log.d(LOG_TAG, "Save recipe id : " + recipe.getId());
    }

    private void handleOnRecipeSelected(Recipe recipe) {
        setPreferencesSetting(recipe);

        Intent intent = new Intent(this, RecipeWidget.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");

        int[] ids = {mAppWidgetId};
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}
