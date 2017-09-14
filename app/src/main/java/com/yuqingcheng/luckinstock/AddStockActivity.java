package com.yuqingcheng.luckinstock;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.yuqingcheng.luckinstock.model.trader.MyStockAnalyzer;
import com.yuqingcheng.luckinstock.model.trader.StockAnalyzer;

public class AddStockActivity extends AppCompatActivity {

    StockAnalyzer analyzer;

    EditText stockName;

    EditText fromDate;

    EditText toDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock);

        this.analyzer = new MyStockAnalyzer();

        stockName = (EditText) findViewById(R.id.stockName);

        fromDate = (EditText) findViewById(R.id.fromDate);

        toDate = (EditText) findViewById(R.id.toDate);

        try{
            Intent intent = getIntent();
            String symbol = intent.getStringExtra("symbol");
            if(symbol.length() > 0) {
                stockName.setText(symbol);
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

        intent.putExtra("result", stockSymbol + "," + fromDateStr + "," + toDateStr+","+name);
        setResult(RESULT_OK, intent);

        finish();

    }

    public void goBack(View view) {
        Intent intent = new Intent();

        setResult(RESULT_CANCELED, intent);

        finish();
    }


}
