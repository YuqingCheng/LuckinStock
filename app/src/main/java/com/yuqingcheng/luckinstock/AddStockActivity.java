package com.yuqingcheng.luckinstock;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.yuqingcheng.luckinstock.model.trader.MyStockAnalyzer;
import com.yuqingcheng.luckinstock.model.trader.StockAnalyzer;

public class AddStockActivity extends AppCompatActivity {

    StockAnalyzer analyzer;

    EditText stockName;

    EditText fromDate;

    EditText toDate;

    Switch maSwitch;

    CheckBox maCheck50;

    CheckBox maCheck200;

    final String MA_50_CHECKED = "50_CHECKED";
    final String MA_200_CHECKED = "200_CHECKED";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock);

        this.analyzer = new MyStockAnalyzer();

        stockName = (EditText) findViewById(R.id.stockName);

        fromDate = (EditText) findViewById(R.id.fromDate);

        toDate = (EditText) findViewById(R.id.toDate);

        maSwitch = (Switch) findViewById(R.id.maSwitch);

        maCheck50 = (CheckBox) findViewById(R.id.check50);

        maCheck200 = (CheckBox) findViewById(R.id.check200);

        maSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked){
                    maCheck50.setVisibility(View.VISIBLE);
                    maCheck200.setVisibility(View.VISIBLE);
                }else{
                    maCheck50.setChecked(false);
                    maCheck200.setChecked(false);
                    maCheck50.setVisibility(View.INVISIBLE);
                    maCheck200.setVisibility(View.INVISIBLE);
                }
            }
        });

        try{
            Intent intent = getIntent();
            String symbol = intent.getStringExtra("symbol");
            String fromDate = intent.getStringExtra("fromDate");
            String toDate = intent.getStringExtra("toDate");
            String movingAverage = intent.getStringExtra("movingAverage");
            if(symbol != null && symbol.length() > 0) {
                stockName.setText(symbol);
            }
            if(fromDate != null && fromDate.length() > 0) {
                this.fromDate.setText(fromDate);
            }
            if(toDate != null && toDate.length() > 0) {
                this.toDate.setText(toDate);
            }
            if(movingAverage != null && movingAverage.length() > 0) {
                maSwitch.setChecked(true);
                if(movingAverage.equals(MA_50_CHECKED+MA_200_CHECKED)) {
                    maCheck50.setChecked(true);
                    maCheck200.setChecked(true);
                }else if(movingAverage.equals(MA_50_CHECKED)) {
                    maCheck50.setChecked(true);
                }else{
                    maCheck200.setChecked(true);
                }
            }
        }catch(Exception e) {
            //do nothing.
        }

    }

    public class GetStockName extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                return analyzer.getStockName(strings[0]);
            }catch(IllegalArgumentException e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);

            if(res.length() == 0) {
                Toast toast;

                toast = Toast.makeText(getApplicationContext(),
                        "Cannot find input stock symbol, please try again.", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    public class CheckDateRange extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {

            boolean res = analyzer.isValidDateRange(strings[0], strings[1]);
            Log.i("date range valid:", ""+res);
            return res;
        }

        @Override
        protected void onPostExecute(Boolean isValidDateRange) {
            super.onPostExecute(isValidDateRange);

            if(! isValidDateRange) {
                Toast toast;

                toast = Toast.makeText(getApplicationContext(),
                        "Invalid format of date or input date is non-bussiness day, please try again.", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    /**
     * when clicking add stock button, if input date and stock name are valid, add corresponding
     * curve to display, otherwise toast out error message.
     *
     * @param view
     */

    public void addStock(View view) {

        String stockSymbol = stockName.getText().toString().toUpperCase();

        String fromDateStr = fromDate.getText().toString();

        String toDateStr = toDate.getText().toString();

        String name = "";

        Toast toast;

        GetStockName checkStockSymbolThread = new GetStockName();
        CheckDateRange checkDateRangeThread = new CheckDateRange();

        try{
            name = checkStockSymbolThread.execute(stockSymbol).get();

            if(name.length() == 0) return;

            try{
                boolean isValidDateRange = checkDateRangeThread.execute(fromDateStr, toDateStr).get();

                if(!isValidDateRange) return;

            }catch(Exception e) {
                toast = Toast.makeText(getApplicationContext(),
                        "Network error, please try again.", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

        } catch(Exception e) {
            toast = Toast.makeText(getApplicationContext(),
                    "Network error, please try again.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        Intent intent = new Intent();
        StringBuffer maSb = new StringBuffer("");
        if(maCheck50.isChecked()) maSb.append(MA_50_CHECKED);
        if(maCheck200.isChecked()) maSb.append(MA_200_CHECKED);

        intent.putExtra("symbol", stockSymbol);
        intent.putExtra("fromDate", fromDateStr);
        intent.putExtra("toDate", toDateStr);
        intent.putExtra("name", name);
        intent.putExtra("movingAverage", maSb.toString());
        setResult(RESULT_OK, intent);

        finish();

    }

    public void goBack(View view) {
        Intent intent = new Intent();

        setResult(RESULT_CANCELED, intent);

        finish();
    }


}
