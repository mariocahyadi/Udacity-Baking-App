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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mario99ukdw on 07.09.2017.
 */

public class IngredientListFactory implements RemoteViewsService.RemoteViewsFactory {
    private final static String LOG_TAG = IngredientListFactory.class.getSimpleName();

    private Context context;
    private int appWidgetId;
    private ArrayList<Ingredient> ingredients = new ArrayList<>();

    public IngredientListFactory(Context context, Intent intent) {
        this.context = context;
        Bundle bundle = intent.getBundleExtra(RecipeWidget.EXTRA_VAR_NAME);

        appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        ingredients = bundle.getParcelableArrayList(RecipeWidget.EXTRA_INGREDIENTS);
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
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return ingredients.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.list_item_ingredient_widget);
        Ingredient ingredient = ingredients.get(position);

        rv.setTextViewText(R.id.ingredient_text_view, ingredient.getFormatedText());
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
