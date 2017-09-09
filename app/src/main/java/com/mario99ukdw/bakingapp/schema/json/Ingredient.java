
package com.mario99ukdw.bakingapp.schema.json;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Ingredient implements Parcelable {
    public Ingredient() {}

    @SerializedName("quantity")
    @Expose
    private Double quantity;
    @SerializedName("measure")
    @Expose
    private String measure;
    @SerializedName("ingredient")
    @Expose
    private String ingredient;

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    /* based on https://en.wikibooks.org/wiki/Cookbook:Units_of_measurement */
    public static String getMeasureText(String measure) {
        switch (measure) {
            case "CUP": return "cup";
            case "TBLSP": return "tablespoon";
            case "TSP": return "teaspoon";
            case "K": return "kilo";
            case "G": return "gram";
            case "OZ": return "ounce";
            default: return measure;
        }
    }
    public String getFormatedText() {
        NumberFormat nf = new DecimalFormat("##.#");
        return String.format("%s %s %s", nf.format(quantity), getMeasureText(measure), ingredient);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(quantity);
        dest.writeString(measure);
        dest.writeString(ingredient);
    }

    private Ingredient(Parcel in){
        this.quantity = in.readDouble();
        this.measure = in.readString();
        this.ingredient = in.readString();
    }
    public static final Parcelable.Creator<Ingredient> CREATOR = new Parcelable.Creator<Ingredient>() {

        @Override
        public Ingredient createFromParcel(Parcel source) {
            return new Ingredient(source);
        }

        @Override
        public Ingredient[] newArray(int size) {
            return new Ingredient[size];
        }
    };
}
