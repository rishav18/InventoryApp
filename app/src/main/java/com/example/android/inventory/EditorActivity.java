package com.example.android.inventory;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract.ProductEntry;

import java.io.ByteArrayOutputStream;

public class EditorActivity extends AppCompatActivity {
    private static final int PRODUCT_LOADER = 1;
    private static final int FILE_SELECT_CODE = 2;
    EditText mNameEditText;
    EditText mCountEditText;
    EditText mPriceEditText;
    Button mImageButton;
    ImageView mImageView;
    private Uri mCurrentProductUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        mNameEditText = (EditText) findViewById(R.id.edit_text_product_name);
        mCountEditText = (EditText) findViewById(R.id.edit_text_product_count);
        mPriceEditText = (EditText) findViewById(R.id.edit_text_product_price);
        mImageButton = (Button) findViewById(R.id.product_image);
        mImageView = (ImageView) findViewById(R.id.image_view);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonImageClick();
            }
        });
        mPriceEditText.setFilters(new InputFilter[]{
                new DigitsKeyListener(Boolean.FALSE, Boolean.TRUE) {
                    int beforeDecimal = 5, afterDecimal = 2;

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        String temp = mPriceEditText.getText() + source.toString();
                        if (temp.equals(".")) {
                            return "0.";
                        } else if (temp.toString().indexOf(".") == -1) {
                            // no decimal point placed yet
                            if (temp.length() > beforeDecimal) {
                                return "";
                            }
                        } else {
                            temp = temp.substring(temp.indexOf(".") + 1);
                            if (temp.length() > afterDecimal) {
                                return "";
                            }
                        }
                        return super.filter(source, start, end, dest, dstart, dend);
                    }
                }
        });
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        if (mCurrentProductUri == null) {
            setTitle(R.string.editor_title_add);
        }
    }

    private void buttonImageClick() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), FILE_SELECT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri imageUri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                    mImageView = (ImageView) findViewById(R.id.image_view);
                    mImageView.setImageBitmap(bitmap);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String countString = mCountEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(countString)
                || TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, "Please fill out all values", Toast.LENGTH_SHORT).show();
            return;
        }
        int count = Integer.valueOf(countString);
        double price = Double.valueOf(priceString);
        if (count < 0) {
            Toast.makeText(this, "Please input a real number for the count field.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (price < 0.0) {
            Toast.makeText(this, "Please input a real price.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mImageView.getDrawable() == null) {
            Toast.makeText(this, "Please upload an image.", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap imageBitMap = ((BitmapDrawable) mImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        imageBitMap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] imageByteArray = bos.toByteArray();
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_COUNT, count);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageByteArray);
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
        if (newUri == null) {
            Toast.makeText(this, getString(R.string.editor_insert_failed), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.editor_insert_successful), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
