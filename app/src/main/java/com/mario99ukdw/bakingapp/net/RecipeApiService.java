package com.mario99ukdw.bakingapp.net;

import com.mario99ukdw.bakingapp.schema.json.Recipe;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by mario99ukdw on 09.08.2017.
 */

public interface RecipeApiService {

    /*
    Retrofit get annotation with our URL
    And our method that will return us the List of ContactList
    */
    @GET("topher/2017/May/59121517_baking/baking.json")
    Call<List<Recipe>> getRecipes();
}