package com.mario99ukdw.bakingapp.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.mario99ukdw.bakingapp.MainActivity;
import com.mario99ukdw.bakingapp.R;
import com.mario99ukdw.bakingapp.provider.RecipeContract;
import com.mario99ukdw.bakingapp.provider.RecipeProvider;
import com.mario99ukdw.bakingapp.schema.json.Ingredient;
import com.mario99ukdw.bakingapp.schema.json.Recipe;

/**
 * Created by mario99ukdw on 07.09.2017.
 */

public class IngredientListFactory implements RemoteViewsService.RemoteViewsFactory {
    private final static String LOG_TAG = IngredientListFactory.class.getSimpleName();

    private Context context;
    private Cursor cursor;
    private int appWidgetId;

    public IngredientListFactory(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        Log.d(LOG_TAG, "Constructor " + appWidgetId);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int recipeId = prefs.getInt("widget_recipe_id_" + appWidgetId, 0);
        Log.d(LOG_TAG, "widgetId : " + appWidgetId + ", recipeId:" + recipeId);

        if (cursor != null) {
            cursor.close();
        }
        String[] projection = new String[]{
                RecipeContract.IngredientsColumn.COL_QUANTITY, // 0
                RecipeContract.IngredientsColumn.COL_MEASURE, // 1
                RecipeContract.IngredientsColumn.COL_INGREDIENT, // 2
        };
        cursor = context.getContentResolver().query(RecipeProvider.getIngredientContentUri(recipeId), projection, null, null, null);
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
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.list_item_ingredient_widget);
        if (cursor.moveToPosition(position)) {
            Ingredient ingredient = new Ingredient();
            ingredient.setQuantity(cursor.getDouble(0));
            ingredient.setMeasure(cursor.getString(1));
            ingredient.setIngredient(cursor.getString(2));

            rv.setTextViewText(R.id.ingredient_text_view, ingredient.getFormatedText());
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
