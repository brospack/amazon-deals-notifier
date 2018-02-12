package com.nautilusapps.amazondealsnotifier;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

/**
 * Allows the user to add an Amazon item to his list, by specifying its title and URL.<br>
 * Can be called specifying the initial value of the URL field.
 */
public class AddItemActivity extends AppCompatActivity {

    public static final String AMAZON_URL_REGEX = "\\b(((ht|f)tp(s?)://|~/|/)|www.)(\\w+:\\w+@)?(([-\\w]+\\.)+(com|[a-z]{2}))(:[\\d]{1,5})?(((/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|/)+|\\?|#)?((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b";

    private EditText mTitleEditText, mUrlEditText;
    private TextInputLayout mTitleTextInputLayout, mUrlTextInputLayout;
    private ProgressBar mProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_add_item_activity));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_close);
        }

        this.mTitleEditText = findViewById(R.id.edit_text_add_item_title);
        this.mUrlEditText = findViewById(R.id.edit_text_add_item_url);
        this.mTitleTextInputLayout = findViewById(R.id.text_input_layout_add_item_title);
        this.mUrlTextInputLayout = findViewById(R.id.text_input_layout_add_item_url);
        this.mProgressBar = findViewById(R.id.progress_bar_add_item_activity);

        setInitialValues();

        // If the title field is focused, show an hint message belows the field:
        mTitleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mTitleTextInputLayout.setError(getString(R.string.msg_hint_title));
                } else {
                    mTitleTextInputLayout.setError(null);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_add_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.item_add) {
            actionAdd();
        } else if (id == android.R.id.home) {
            actionClose();
        }

        return super.onOptionsItemSelected(item);

    }

    /**
     * Gets the initial values from the extended data from an {@code Intent} and if available fills
     * the fields with these values.
     */
    private void setInitialValues() {

        String url;
        Bundle extras = getIntent().getExtras();

        url = (extras == null) ? null : extras.getString("EXTRA_URL");

        if (url != null) {
            mUrlEditText.setText(url);
        }

    }



    /**
     * Validates the input fields and in case of success tries to add the Amazon item to the
     * database. While the item data is being scraped, shows an indeterminate progress bar.<br>
     * If the data has been scraped correctly calls {@link MainActivity}, otherwise shows an error
     * message.
     */
    @SuppressLint("StaticFieldLeak")
    private void actionAdd() {

        if (validateInput()) {

            mProgressBar.setVisibility(View.VISIBLE);

            new AsyncTask<Void, Void, Void>() {

                private boolean successful;

                @Override
                protected Void doInBackground(Void... params) {
                    successful = addItem();
                    return null;
                }

                @Override
                protected void onPostExecute(Void param) {

                    super.onPostExecute(param);

                    mProgressBar.setVisibility(View.GONE);

                    if (successful) {
                        actionClose();
                    } else {
                        Snackbar.make(findViewById(android.R.id.content),
                                getString(R.string.error_add_item_unsuccessful),
                                Snackbar.LENGTH_LONG).show();
                    }

                }

            }.execute();

        }

    }

    /**
     * Calls {@link MainActivity}.
     */
    private void actionClose() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    /**
     * Checks if each field contains a valid value and eventually shows an error message below the
     * first invalid field.
     * @return  {@code false} if at least a field contains an invalid value, {@code true} otherwise.
     */
    private boolean validateInput() {

        String url = mUrlEditText.getText().toString();

        // Check if the URL field is empty:
        if (mUrlEditText.getText().toString().trim().matches("")) {
            mUrlTextInputLayout.setError(getString(R.string.error_required_url));
            mUrlEditText.requestFocus();
            return false;
        }

        // Check if the URL value is valid:
        if (!url.matches(AMAZON_URL_REGEX)) {
            mUrlTextInputLayout.setError(getString(R.string.error_invalid_url));
            mUrlEditText.requestFocus();
            return false;
        }

        return true;

    }

    /**
     * Gets the item title and URL from their fields and tries to scrape the data for the
     * corresponding Amazon item. If the lookup is successful adds the item to the database and
     * returns {@code true}. Otherwise returns {@code false}.<br>
     * @return  {@code false} if the lookup was unsuccessful, {@code true} otherwise.<br>
     *          Returns {@code false} if the URL field is empty.
     */
    private boolean addItem() {

        String url = mUrlEditText.getText().toString();
        String title = mTitleEditText.getText().toString();

        if (url.trim().matches("")) return false;

        AmazonItemLookUp amazonItemLookUp = new AmazonItemLookUp(new String[]{url});
        AmazonItem amazonItem = amazonItemLookUp.getAmazonItems()[0];

        // If the look up failed:
        if (amazonItem == null) {
            return false;
        }

        // If there there is a specified title:
        if (!mTitleEditText.getText().toString().trim().matches("")) amazonItem.title = title;

        DBHandler dbHandler = new DBHandler(getApplicationContext());
        if (dbHandler.addItem(amazonItem) == -1L) return false;

        return true;

    }

}
