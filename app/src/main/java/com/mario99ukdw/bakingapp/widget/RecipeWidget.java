package com.mario99ukdw.bakingapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.mario99ukdw.bakingapp.DetailActivity;
import com.mario99ukdw.bakingapp.R;
import com.mario99ukdw.bakingapp.provider.RecipeContract;
import com.mario99ukdw.bakingapp.provider.RecipeProvider;
import com.mario99ukdw.bakingapp.schema.json.Ingredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class RecipeWidget extends AppWidgetProvider {
    private static final String LOG_TAG = RecipeWidget.class.getSimpleName();

    public static final String EXTRA_VAR_NAME = "recipe.data";
    public static final String EXTRA_INGREDIENTS = "ingredients";

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.recipe_widget);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int recipeId = prefs.getInt("widget_recipe_id_" + appWidgetId, 0);
        String recipeName = prefs.getString("widget_recipe_name_" + appWidgetId, context.getString(R.string.text_placeholder_name));
        Log.d(LOG_TAG, "widgetId : " + appWidgetId + ", recipeId:" + recipeId);

        views.setTextViewText(R.id.recipe_name_text_view, recipeName);

        // Load ingredients
        ArrayList<Ingredient> ingredients = new ArrayList<>();
        String[] projection = new String[]{
                RecipeContract.IngredientsColumn.COL_QUANTITY, // 0
                RecipeContract.IngredientsColumn.COL_MEASURE, // 1
                RecipeContract.IngredientsColumn.COL_INGREDIENT, // 2
        };
        Cursor cursor = context.getContentResolver().query(RecipeProvider.getIngredientContentUri(recipeId), projection, null, null, null);
        if (cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setQuantity(cursor.getDouble(0));
                ingredient.setMeasure(cursor.getString(1));
                ingredient.setIngredient(cursor.getString(2));
                ingredients.add(ingredient);
            }
        }
        cursor.close();

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA_INGREDIENTS, ingredients);
        bundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

        Intent intent = new Intent(context, RecipeWidgetService.class);

        intent.putExtra(EXTRA_VAR_NAME, bundle);
        //intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //intent.putParcelableArrayListExtra(EXTRA_INGREDIENTS, ingredients);
        intent.setData(Uri.fromParts("content", String.valueOf(appWidgetId), null)); // https://stackoverflow.com/questions/11350287/ongetviewfactory-only-called-once-for-multiple-widgets
        views.setRemoteAdapter(R.id.ingredient_list_view, intent);

//        Intent startActivityIntent = new Intent(context, DetailActivity.class);
//        PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        views.setPendingIntentTemplate(R.id.ingredient_list_view, startActivityPendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.ingredient_list_view);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
            Log.d(LOG_TAG, "updateAppWidget for id " + appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled

    }

}

