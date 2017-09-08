package com.mario99ukdw.bakingapp.provider;

import android.provider.BaseColumns;

/**
 * Created by mario99ukdw on 15.08.2017.
 */

public class RecipeContract {
    /* Inner class that defines table contents */
    public static abstract class RecipesColumn implements BaseColumns {

        public static final String TABLE_NAME = "recipes_table";
        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
        public static final String COL_SERVINGS = "servings";
        public static final String COL_IMAGE = "image";
    }

    public static abstract class IngredientsColumn implements BaseColumns {

        public static final String TABLE_NAME = "ingredients_table";
        public static final String COL_QUANTITY = "quantity";
        public static final String COL_MEASURE = "measure";
        public static final String COL_INGREDIENT = "ingredient";
        public static final String COL_RECIPE_ID = "recipe_id";
    }

    public static abstract class StepsColumn implements BaseColumns {

        public static final String TABLE_NAME = "steps_table";
        public static final String COL_STEP_NUMBER = "step_number";
        public static final String COL_SHORT_DESCRIPTION = "short_description";
        public static final String COL_DESCRIPTION = "description";
        public static final String COL_VIDEO_URL = "video_url";
        public static final String COL_THUMBNAIL_URL = "thumbnail_url";
        public static final String COL_RECIPE_ID = "recipe_id";
    }
}
