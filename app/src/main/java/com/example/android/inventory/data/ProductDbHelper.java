package com.example.android.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProductDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "INVENTORY.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String BLOB_TYPE = " BLOB";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME + " (" +
            ProductContract.ProductEntry._ID + INT_TYPE + " PRIMARY KEY AUTOINCREMENT," +
            ProductContract.ProductEntry.COLUMN_PRODUCT_NAME + TEXT_TYPE + " NOT NULL" + COMMA_SEP +
            ProductContract.ProductEntry.COLUMN_PRODUCT_COUNT + INT_TYPE + " NOT NULL DEFAULT 0" + COMMA_SEP +
            ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE + REAL_TYPE + " NOT NULL DEFAULT 0.00" + COMMA_SEP +
            ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE + BLOB_TYPE +
            ")";
    private static final String SQL_DELETE_PRODUCTS_TABLE =
            "DROP TABLE IF EXISTS " + ProductContract.ProductEntry.TABLE_NAME;

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL(SQL_DELETE_PRODUCTS_TABLE);
            onCreate(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
