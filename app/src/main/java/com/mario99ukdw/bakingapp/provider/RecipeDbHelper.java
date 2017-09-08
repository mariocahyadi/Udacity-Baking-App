package com.mario99ukdw.bakingapp.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by mario99ukdw on 15.08.2017.
 */

public class RecipeDbHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = RecipeDbHelper.class.getName();

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "bakingapp.db";

    private static final String CREATE_TABLE_RECIPES = "create table " + RecipeContract.RecipesColumn.TABLE_NAME
            + "(" + RecipeContract.RecipesColumn.COL_ID + " integer unique, "
            + RecipeContract.RecipesColumn.COL_NAME + " text not null,"
            + RecipeContract.RecipesColumn.COL_SERVINGS + " integer not null, "
            + RecipeContract.RecipesColumn.COL_IMAGE + " text );";

    private static final String CREATE_TABLE_INGREDIENT = "create table " + RecipeContract.IngredientsColumn.TABLE_NAME
            + "(" + RecipeContract.IngredientsColumn.COL_QUANTITY + " double, "
            + RecipeContract.IngredientsColumn.COL_MEASURE + " text not null,"
            + RecipeContract.IngredientsColumn.COL_INGREDIENT + " text not null, "
            + RecipeContract.IngredientsColumn.COL_RECIPE_ID + " integer not null );";

    private static final String CREATE_TABLE_STEP = "create table " + RecipeContract.StepsColumn.TABLE_NAME
            + "(" + RecipeContract.StepsColumn.COL_STEP_NUMBER + " integer not null, "
            + RecipeContract.StepsColumn.COL_SHORT_DESCRIPTION + " text not null,"
            + RecipeContract.StepsColumn.COL_DESCRIPTION + " text not null, "
            + RecipeContract.StepsColumn.COL_VIDEO_URL + " text not null, "
            + RecipeContract.StepsColumn.COL_THUMBNAIL_URL + " text not null, "
            + RecipeContract.StepsColumn.COL_RECIPE_ID + " integer not null );";

    private SQLiteDatabase mDB;

    public RecipeDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.mDB = getWritableDatabase();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, String.format("%s  onCreate", DB_NAME));
        db.execSQL(CREATE_TABLE_RECIPES);
        db.execSQL(CREATE_TABLE_INGREDIENT);
        db.execSQL(CREATE_TABLE_STEP);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, String.format("%s onUpgrade", DB_NAME));
        db.execSQL("DROP TABLE IF EXISTS " + RecipeContract.RecipesColumn.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RecipeContract.IngredientsColumn.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RecipeContract.StepsColumn.TABLE_NAME);
        onCreate(db);
    }
}
