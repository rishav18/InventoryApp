package com.example.android.inventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventory.data.ProductContract.ProductEntry;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int PRODUCT_LOADER = 1;
    TextView mNameTextView;
    Button mIncreaseButton;
    Button mOrderSupplyButton;
    Button mDecreaseButton;
    TextView mCountTextView;
    TextView mPriceTextView;
    ImageView mImageView;
    private Uri mCurrentProductUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mNameTextView = (TextView) findViewById(R.id.product_name);
        mCountTextView = (TextView) findViewById(R.id.product_availability);
        mPriceTextView = (TextView) findViewById(R.id.product_price);
        mDecreaseButton = (Button) findViewById(R.id.decrease_count_button);
        mIncreaseButton = (Button) findViewById(R.id.increase_count_button);
        mOrderSupplyButton = (Button) findViewById(R.id.supply_order_button);
        mImageView = (ImageView) findViewById(R.id.product_image_detail);
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        if (mCurrentProductUri != null) {
            setTitle(R.string.detail_view_title);
            getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        }
    }

    private int adjustAvailability(Uri itemUri, int newCount) {
        if (newCount < 0) return 0;

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_COUNT, newCount);
        int numRowsUpdated = getContentResolver().update(itemUri, values, null, null);
        return numRowsUpdated;
    }

    private int deleteProduct(Uri itemUri) {
        return getContentResolver().delete(itemUri, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,   // Parent activity context
                mCurrentProductUri,        // Table to query
                null,     // Projection to return
                null,            // No selection clause
                null,            // No selection arguments
                null             // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int countColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_COUNT);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
            final String name = cursor.getString(nameColumnIndex);
            final int count = cursor.getInt(countColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            byte[] imageByteArray = cursor.getBlob(imageColumnIndex);
            mNameTextView.setText(name);
            mCountTextView.setText(Integer.toString(count));
            mPriceTextView.setText("Rs" + String.format("%.2f", price));
            Bitmap bmp = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
            mImageView.setImageBitmap(bmp);
            mDecreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adjustAvailability(mCurrentProductUri, count - 1);
                }
            });
            mIncreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adjustAvailability(mCurrentProductUri, count + 1);
                }
            });
            mOrderSupplyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    composeEmail(new String[]{"xyz@gmail.com"}, "Supply Products  for " + name);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct(mCurrentProductUri);
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void composeEmail(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
