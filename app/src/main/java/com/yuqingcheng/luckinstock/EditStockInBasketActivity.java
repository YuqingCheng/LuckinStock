package com.yuqingcheng.luckinstock;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yuqingcheng.luckinstock.model.trader.MyStockAnalyzer;
import com.yuqingcheng.luckinstock.model.trader.StockAnalyzer;

public class EditStockInBasketActivity extends AppCompatActivity {

    TextView basketName;

    EditText stockSymbol;

    EditText numShares;

    Button submit;

    StockAnalyzer analyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_stock_in_basket);
        basketName = (TextView) findViewById(R.id.basketName);
        stockSymbol = (EditText) findViewById(R.id.stockSymbol);
        numShares = (EditText) findViewById(R.id.numShares);
        submit = (Button) findViewById(R.id.submit);
        analyzer = new MyStockAnalyzer();

        Intent intent = getIntent();

        try{
            String basketNameStr = intent.getStringExtra("basketName");
            basketName.setText(basketNameStr);
            String stockSymbolStr = intent.getStringExtra("stockSymbol");
            stockSymbol.setText(stockSymbolStr);
            String numSharesStr = intent.getStringExtra("numShares");
            numShares.setText(numSharesStr);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void submit(View view) {
        String stockSymbolStr = stockSymbol.getText().toString();
        String numSharesStr = numShares.getText().toString();
        GetStockName getStockNameTask = new GetStockName();

        try{
            String stockName = getStockNameTask.execute(stockSymbolStr).get();

            if(stockName.length() > 0) {
                try{
                    int numSharesInt = Integer.valueOf(numSharesStr);

                    if(numSharesInt > 0 && numSharesInt <= 1000) {
                        Intent intent = new Intent();
                        intent.putExtra("basketName", basketName.getText().toString());
                        intent.putExtra("stockSymbol", stockSymbolStr);
                        intent.putExtra("numShares", numSharesInt);

                        setResult(RESULT_OK, intent);
                        finish();
                    } else{
                        Toast.makeText(getApplicationContext(), "Number of shares should be integer from 1 - 1000", Toast.LENGTH_SHORT);
                        return;
                    }
                }catch(Exception e) {
                    Toast.makeText(getApplicationContext(), "Number of shares should be integer from 1 - 1000", Toast.LENGTH_SHORT);
                    return;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            return;
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
}
