package theperfectsquare.counterandchrono;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import theperfectsquare.counterandchrono.contentprovider.CategoriesContentProvider;
import theperfectsquare.counterandchrono.contentprovider.ResultsContentProvider;
import theperfectsquare.counterandchrono.database.CategoriesTable;
import theperfectsquare.counterandchrono.database.ResultsTable;


public class CounterActivity extends Activity {

    private EditText mTitleText;
    private TextView mCounterInt;
    private Uri CategoryUri;
    private Uri ResultsUri;
    private int count= 0;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_counter);

        mTitleText = (EditText) findViewById(R.id.title_editable);
        mCounterInt = (TextView) findViewById(R.id.counter);
        Button increase = (Button) findViewById(R.id.increase_button);
        final Button decrease = (Button) findViewById(R.id.decrease_button);

        Bundle extras = getIntent().getExtras();

        // check from the saved Instance
        CategoryUri = (bundle == null) ? null : (Uri) bundle
                .getParcelable(CategoriesContentProvider.CONTENT_ITEM_TYPE);
        ResultsUri = (bundle == null) ? null : (Uri) bundle
                .getParcelable(ResultsContentProvider.CONTENT_ITEM_TYPE);

        // Or passed from the other activity
        if (extras != null) {
            CategoryUri = extras
                    .getParcelable(CategoriesContentProvider.CONTENT_ITEM_TYPE);
            ResultsUri = extras
                    .getParcelable(ResultsContentProvider.CONTENT_ITEM_TYPE);
            fillData(CategoryUri, ResultsUri);
            count = Integer.parseInt(mCounterInt.getText().toString());
        }
        increase.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                count ++;
                mCounterInt.setText(Integer.toString(count));
            }

        });
        decrease.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                count --;
                mCounterInt.setText(Integer.toString(count));
            }

        });
        decrease.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                count = 0;
                mCounterInt.setText(Integer.toString(count));
                return false;
            }
        });
        decrease.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // on release
                    decrease.setBackgroundColor(Color.WHITE);
                    decrease.setTextColor(Color.BLACK);
                    decrease.setText("-");
                }else{
                    // on touch
                    decrease.setBackgroundColor(Color.RED);
                    decrease.setTextColor(Color.WHITE);
                    decrease.setText("Hold to Reset");
                }
                return false;
            }
        });
    }

    private void fillData(Uri categoryUri, Uri resultsUri) {
        Log.i("Category URI", "Category URI " + categoryUri);
        Log.i("Results URI", "Results URI " + resultsUri);

        String[] categoryProjection = { CategoriesTable.COLUMN_TYPE, CategoriesTable.COLUMN_NAME};
        Cursor categoryCursor = getContentResolver().query(categoryUri, categoryProjection, null, null, null);

        String[] resultsProjection = { ResultsTable.COLUMN_ID, ResultsTable.COLUMN_RESULT, ResultsTable.COLUMN_CATEGORY_ID};
        Cursor resultsCursor = getContentResolver().query(resultsUri, resultsProjection, null, null, null);

        if (categoryCursor != null && resultsCursor != null) {
            categoryCursor.moveToFirst();
            mTitleText.setText(categoryCursor.getString(categoryCursor
                    .getColumnIndexOrThrow(CategoriesTable.COLUMN_NAME)));
            // always close the cursor
            categoryCursor.close();

            resultsCursor.moveToFirst();
            mCounterInt.setText(resultsCursor.getString(resultsCursor
                    .getColumnIndexOrThrow(ResultsTable.COLUMN_RESULT)));

            // always close the cursor
            resultsCursor.close();
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putParcelable(CategoriesContentProvider.CONTENT_ITEM_TYPE, CategoryUri);
        outState.putParcelable(ResultsContentProvider.CONTENT_ITEM_TYPE, ResultsUri);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    private void saveState() {
        String summary = mTitleText.getText().toString();
        String data = mCounterInt.getText().toString();
        Calendar cal = Calendar.getInstance();
        int dateInSeconds = (int)((cal.getTimeInMillis()+cal.getTimeZone().getOffset(cal.getTimeInMillis()))/1000);
        // only save if either summary or description
        // is available

        //category values
        ContentValues categoryValues = new ContentValues();
        categoryValues.put(CategoriesTable.COLUMN_NAME, summary);
        categoryValues.put(CategoriesTable.COLUMN_TYPE, "counter");

        //results values
        ContentValues resultsValues = new ContentValues();
        resultsValues.put(ResultsTable.COLUMN_RESULT,data);
        resultsValues.put(ResultsTable.COLUMN_DATE,dateInSeconds);

        if (CategoryUri == null) {
            // New cagetory which means zero data or new data
            CategoryUri = getContentResolver().insert(CategoriesContentProvider.CONTENT_URI, categoryValues);
            //gets the new id from the given gategory uri
            resultsValues.put(ResultsTable.COLUMN_CATEGORY_ID, CategoryUri.getLastPathSegment());
            ResultsUri = getContentResolver().insert(ResultsContentProvider.CONTENT_URI, resultsValues);
        } else {
            // Update category
            getContentResolver().update(CategoryUri, categoryValues, null, null);
            getContentResolver().update(ResultsUri, resultsValues, null, null);
        }
    }

    private void makeToast() {
        Toast.makeText(CounterActivity.this, "Please maintain a summary",
                Toast.LENGTH_LONG).show();
    }
}
