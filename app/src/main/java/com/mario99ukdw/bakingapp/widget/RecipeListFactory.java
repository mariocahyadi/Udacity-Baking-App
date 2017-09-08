package com.mario99ukdw.bakingapp.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.mario99ukdw.bakingapp.DetailActivity;
import com.mario99ukdw.bakingapp.MainActivity;
import com.mario99ukdw.bakingapp.R;
import com.mario99ukdw.bakingapp.provider.RecipeContract;
import com.mario99ukdw.bakingapp.provider.RecipeProvider;
import com.mario99ukdw.bakingapp.schema.json.Recipe;

/**
 * Created by mario99ukdw on 02.09.2017.
 */

public class RecipeListFactory implements RemoteViewsService.RemoteViewsFactory {
    private final static String LOG_TAG = RecipeListFactory.class.getSimpleName();

    private final static int POSITION_RECIPE_ID = 0;
    private final static int POSITION_RECIPE_NAME = 1;

    private Context context;
    private Cursor cursor;
    private int widgetId;

    public RecipeListFactory(Context context, Intent intent) {
        this.context = context;
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        if (cursor != null) {
            cursor.close();
        }
        String[] projection = new String[]{
                RecipeContract.RecipesColumn.COL_ID, // 0
                RecipeContract.RecipesColumn.COL_NAME, // 1
                RecipeContract.RecipesColumn.COL_IMAGE, // 2
        };
        cursor = context.getContentResolver().query(RecipeProvider.RECIPE_CONTENT_URI, projection, null, null, null);
        Log.d(LOG_TAG, "Total : " + cursor.getCount());
    }

    @Override
    public void onDestroy() {
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.list_item_recipe_widget);
        if (cursor.moveToPosition(position)) {
            rv.setTextViewText(R.id.recipe_name_text_view, cursor.getString(POSITION_RECIPE_NAME));

            Recipe recipe = new Recipe();
            recipe.setId(cursor.getInt(0));
            recipe.setName(cursor.getString(1));
            recipe.setImage(cursor.getString(2));

            Intent fillInIntent = new Intent();
            Bundle extras = new Bundle();
            extras.putParcelable(MainActivity.EXTRA_VAR_NAME_RECIPE, recipe);

            fillInIntent.putExtras(extras);
            rv.setOnClickFillInIntent(R.id.item_recipe_linear_layout, fillInIntent);
        }
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
