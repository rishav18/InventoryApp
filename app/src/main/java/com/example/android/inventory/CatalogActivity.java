package com.example.android.inventory;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.inventory.data.ProductContract.ProductEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;
    private ListView mProductsListView;
    private ProductsCursorAdapter mCursorAdapter;
    private View noProductTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        mProductsListView = (ListView) findViewById(R.id.list_view);
        mCursorAdapter = new ProductsCursorAdapter(this, null);
        mProductsListView.setAdapter(mCursorAdapter);
        mProductsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intentDetails = new Intent(CatalogActivity.this, DetailActivity.class);
                Uri currentPetUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);
                intentDetails.setData(currentPetUri);
                startActivity(intentDetails);
            }
        });
        noProductTextView = (TextView) findViewById(R.id.no_product_text_view);
        mProductsListView.setEmptyView(noProductTextView);
        getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,  // Parent activity context
                ProductEntry.CONTENT_URI,   // Table to query
                null, // Projection to return
                null,  // No selection clause
                null,  // No selection arguments
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.e("Catalog Activity", rowsDeleted + " rows deleted from database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_all_delete:
                deleteAllProducts();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(CatalogActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
