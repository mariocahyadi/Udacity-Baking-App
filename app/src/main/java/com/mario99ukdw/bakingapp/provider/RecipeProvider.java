package com.mario99ukdw.bakingapp.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by mario99ukdw on 16.08.2017.
 */

public class RecipeProvider extends ContentProvider {
    static final String LOG_TAG = RecipeProvider.class.getSimpleName();

    static final String PROVIDER_NAME = "com.mario99ukdw.bakingapp.provider";
    static final String AUTHORITY_URL = "content://" + PROVIDER_NAME + "/";

    static final String RECIPE_URL_PATH = "recipes";
    static final String INGREDIENT_URL_PATH = "recipes/#/ingredients";
    static final String INGREDIENT_ALL_URL_PATH = "recipes/ingredients";
    static final String STEP_URL_PATH = "recipes/#/steps";
    static final String STEP_ALL_URL_PATH = "recipes/steps";

    static final String RECIPE_URL = AUTHORITY_URL + RECIPE_URL_PATH;
    static final String INGREDIENT_URL = AUTHORITY_URL + INGREDIENT_URL_PATH;
    static final String INGREDIENT_ALL_URL = AUTHORITY_URL + INGREDIENT_ALL_URL_PATH;
    static final String STEP_URL = AUTHORITY_URL + STEP_URL_PATH;
    static final String STEP_ALL_URL = AUTHORITY_URL + STEP_ALL_URL_PATH;

    // get CONTENT_URI
    public static final Uri RECIPE_CONTENT_URI = Uri.parse(RECIPE_URL);
    public static Uri getIngredientContentUri(int recipeId) {
        return Uri.parse(INGREDIENT_URL.replace("#", String.valueOf(recipeId)));
    }
    public static Uri getStepContentUri(int recipeId) {
        return Uri.parse(STEP_URL.replace("#", String.valueOf(recipeId)));
    }
    public static final Uri INGREDIENT_ALL_CONTENT_URI = Uri.parse(INGREDIENT_ALL_URL);
    public static final Uri STEP_ALL_CONTENT_URI = Uri.parse(STEP_ALL_URL);

    static final int URI_ID_RECIPES = 1;
    static final int URI_ID_RECIPES_ID = 2;
    static final int URI_ID_INGREDIENTS = 3;
    static final int URI_ID_INGREDIENTS_ALL = 4;
    static final int URI_ID_STEPS = 5;
    static final int URI_ID_STEPS_ALL = 6;
    static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, RECIPE_URL_PATH, URI_ID_RECIPES);
        uriMatcher.addURI(PROVIDER_NAME, RECIPE_URL_PATH + "/#", URI_ID_RECIPES_ID);
        uriMatcher.addURI(PROVIDER_NAME, INGREDIENT_URL_PATH, URI_ID_INGREDIENTS);
        uriMatcher.addURI(PROVIDER_NAME, INGREDIENT_ALL_URL_PATH, URI_ID_INGREDIENTS_ALL);
        uriMatcher.addURI(PROVIDER_NAME, STEP_URL_PATH, URI_ID_STEPS);
        uriMatcher.addURI(PROVIDER_NAME, STEP_ALL_URL_PATH, URI_ID_STEPS_ALL);
    }

    private RecipeDbHelper mRecipeDbHelper;

    private SQLiteDatabase getDb(boolean writeable) {
        return writeable ? mRecipeDbHelper.getWritableDatabase() : mRecipeDbHelper.getReadableDatabase();
    }

    @Override
    public boolean onCreate() {
        Log.d(LOG_TAG, "onCreate");
        Context context = getContext();
        mRecipeDbHelper = new RecipeDbHelper(context);

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = getDb(false);

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String recipeId;
        switch (uriMatcher.match(uri)) {
            case URI_ID_RECIPES:
                qb.setTables(RecipeContract.RecipesColumn.TABLE_NAME);
                Log.d(LOG_TAG,"query uriMatcher " + URI_ID_RECIPES);
                break;
            case URI_ID_RECIPES_ID:
                qb.setTables(RecipeContract.RecipesColumn.TABLE_NAME);
                recipeId = uri.getLastPathSegment();
                Log.d(LOG_TAG, "query uriMatcher " + URI_ID_RECIPES_ID + " With ID=" + recipeId);
                qb.appendWhere(RecipeContract.RecipesColumn.COL_ID + "=" + recipeId);
                break;
            case URI_ID_INGREDIENTS:
                qb.setTables(RecipeContract.IngredientsColumn.TABLE_NAME);
                recipeId = uri.getPathSegments().get(1);
                Log.d(LOG_TAG,"query uriMatcher " + URI_ID_INGREDIENTS + " With ID=" + recipeId);
                qb.appendWhere(RecipeContract.IngredientsColumn.COL_RECIPE_ID + "=" + recipeId);
                break;
            case URI_ID_STEPS:
                qb.setTables(RecipeContract.StepsColumn.TABLE_NAME);
                recipeId = uri.getPathSegments().get(1);
                Log.d(LOG_TAG,"query uriMatcher " + URI_ID_STEPS + " With ID=" + recipeId);
                qb.appendWhere(RecipeContract.StepsColumn.COL_RECIPE_ID + "=" + recipeId);
                break;
        }
        Cursor cursor = qb.query (
                db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = getDb(true);
        long rowID = 0;

        switch (uriMatcher.match(uri)) {
            case URI_ID_RECIPES:
                rowID = db.insert(RecipeContract.RecipesColumn.TABLE_NAME, "", values);
                Log.d(LOG_TAG, "insert uriMatcher " + URI_ID_RECIPES + " with rowID: " + rowID);

                if (rowID > 0) {
                    Uri _uri = ContentUris.withAppendedId(RECIPE_CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(_uri, null);
                    return _uri;
                }
                break;
            case URI_ID_INGREDIENTS:
                rowID = db.insert(RecipeContract.IngredientsColumn.TABLE_NAME, "", values);
                Log.d(LOG_TAG, "insert uriMatcher " + URI_ID_INGREDIENTS + " with rowID: " + rowID);
                return null;
            case URI_ID_STEPS:
                rowID = db.insert(RecipeContract.StepsColumn.TABLE_NAME, "", values);
                Log.d(LOG_TAG, "insert uriMatcher " + URI_ID_STEPS + " with rowID: " + rowID);
                return null;
        }
        try {
            throw new SQLException("Failed to add new record into " + uri);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = getDb(true);
        int count = 0;
        String recipeId;

        switch (uriMatcher.match(uri)) {
            case URI_ID_RECIPES:
                count = db.delete(RecipeContract.RecipesColumn.TABLE_NAME, selection, selectionArgs);
                Log.d(LOG_TAG, "delete uriMatcher " + URI_ID_RECIPES + " total : " + count);
                break;
            case URI_ID_RECIPES_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(RecipeContract.RecipesColumn.TABLE_NAME, RecipeContract.RecipesColumn.COL_ID + " = " + id +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                Log.d(LOG_TAG, "delete uriMatcher " + URI_ID_RECIPES_ID + " by id=" + id + " total : " + count);
                break;
            case URI_ID_INGREDIENTS:
                recipeId = uri.getPathSegments().get(1);
                count = db.delete(RecipeContract.IngredientsColumn.TABLE_NAME, RecipeContract.IngredientsColumn.COL_RECIPE_ID + " = " + recipeId +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                Log.d(LOG_TAG, "delete uriMatcher " + URI_ID_INGREDIENTS + " by recipe_id=" + recipeId + " total : " + count);
                break;
            case URI_ID_INGREDIENTS_ALL:
                // delete all
                count = db.delete(RecipeContract.IngredientsColumn.TABLE_NAME, selection, selectionArgs);
                Log.d(LOG_TAG, "delete all uriMatcher " + URI_ID_INGREDIENTS_ALL + " total : " + count);
                break;
            case URI_ID_STEPS:
                recipeId = uri.getPathSegments().get(1);
                count = db.delete(RecipeContract.IngredientsColumn.TABLE_NAME, RecipeContract.IngredientsColumn.COL_RECIPE_ID + " = " + recipeId +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                Log.d(LOG_TAG, "delete uriMatcher " + URI_ID_STEPS + " by recipe_id=" + recipeId + " total : " + count);
                break;
            case URI_ID_STEPS_ALL:
                // delete all
                count = db.delete(RecipeContract.StepsColumn.TABLE_NAME, selection, selectionArgs);
                Log.d(LOG_TAG, "delete all uriMatcher " + URI_ID_STEPS_ALL + " total : " + count);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        // not implemented
        Log.d(LOG_TAG, "update called");
        return 0;
    }
}
