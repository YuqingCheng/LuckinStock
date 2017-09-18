package com.yuqingcheng.luckinstock;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;
import com.yuqingcheng.luckinstock.model.trader.MyStockAnalyzer;
import com.yuqingcheng.luckinstock.model.trader.StockAnalyzer;
import com.yuqingcheng.luckinstock.util.DateParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EditBasketActivity extends AppCompatActivity {
    String basketName;
    int date;
    TextView basketHeader;
    EditText basketNameInput;
    EditText dateInput;
    Button submit;
    StockAnalyzer analyzer;
    JSONArray basketNamesJSONArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_basket);
        Intent intent = getIntent();
        basketName = "";
        date = -1;
        basketHeader = (TextView) findViewById(R.id.basketHeaderTextView);
        basketNameInput = (EditText) findViewById(R.id.basketName);
        dateInput = (EditText) findViewById(R.id.date);
        submit = (Button) findViewById(R.id.submit);
        analyzer = new MyStockAnalyzer();
        try{
            basketName = intent.getStringExtra("basketName");
            date = intent.getIntExtra("date", -1);
            basketNamesJSONArray = new JSONObject(intent.getStringExtra("basketNamesJSON")).getJSONArray("names");

        }catch(Exception e){
            e.printStackTrace();
        }

        if(basketName.length() > 0) {
            basketHeader.setText("Edit Basket");
            basketNameInput.setText(basketName);
            dateInput.setText(""+date);
        }
    }

    public void submit(View view) {
        String name = basketNameInput.getText().toString();
        String date = dateInput.getText().toString();
        if(name.length() > 0) {
            for(int i = 0; basketNamesJSONArray != null && i < basketNamesJSONArray.length(); i++) {
                try {
                    if (name.equals(basketNamesJSONArray.get(i)) && !basketName.equals(name)) {
                        Toast.makeText(getApplicationContext(), "basket name already exists.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }

            try{
                int dateAsInteger = DateParser.parseDateToInteger(date);
                int currentDate = Integer.valueOf(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                if(dateAsInteger > currentDate) {
                    Toast.makeText(getApplicationContext(), "Please enter a past date", Toast.LENGTH_SHORT);
                    return;
                }

                Intent intent = new Intent();
                intent.putExtra("basketName", name);
                intent.putExtra("date", dateAsInteger);
                intent.putExtra("formerBasketName", basketName);
                setResult(RESULT_OK, intent);
                finish();

            }catch(Exception e) {
                Toast.makeText(getApplicationContext(), "Please make sure input date is in right format.", Toast.LENGTH_SHORT).show();
                return;
            }

        }else{
            Toast.makeText(getApplicationContext(), "Basket name cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }

    }
}
