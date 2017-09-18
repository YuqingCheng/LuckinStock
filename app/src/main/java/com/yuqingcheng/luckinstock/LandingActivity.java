package com.yuqingcheng.luckinstock;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Text;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LandingActivity extends AppCompatActivity {

    final static int MANAGE_BASKET = 0;
    final static int SIGNUP_LOGININ = 1;

    boolean isLogin;
    ImageButton avatar;
    TextView userName;
    Button addBasket;
    Button strategySimulation;
    ParseUser currentUser;
    String basketJSONStr;
    String basketDateJSONstr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Add your initialization code here
        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("b7a99f74025d9cb49814bd66e1a994cada65cd02")
                .clientKey("778f44aa0e52007bff7943bf2beb38e224f7e8b7")
                .server("http://ec2-52-15-197-86.us-east-2.compute.amazonaws.com:80/parse/")
                .build()
        );

        ParseUser.logOut();

        isLogin = ParseUser.getCurrentUser() != null;

        avatar = (ImageButton) findViewById(R.id.avatar);

        userName = (TextView) findViewById(R.id.userName);

        basketJSONStr = "";

        basketDateJSONstr = "";

        /*

        Map<String, Map<String, Integer>> jsonMap = new HashMap<>();

        jsonMap.put("YuqingPremium", new HashMap<String, Integer>());

        jsonMap.get("YuqingPremium").put("AAPL", 100);

        jsonMap.get("YuqingPremium").put("GOOG", 200);

        JSONObject jsonObject = new JSONObject(jsonMap);

        Log.i("JSON", jsonObject.toString());
        */

        loginRefresh();

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }

    /**
     * refresh in-class field data to up-to-date login-in status.
     */
    private void loginRefresh() {
        if(isLogin) {
            currentUser = ParseUser.getCurrentUser();
            basketJSONStr = currentUser.getString("baskets");
            basketDateJSONstr = currentUser.getString("basketDates");
            userName.setText(currentUser.getUsername());
        }else{
            currentUser = null;
            basketJSONStr = "";
            basketDateJSONstr = "";
            userName.setText("Hi, visitor!\nClick avatar to sign up/log in");
        }
    }

    public void toAnalysis(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void toSignupLoginin(View view) {
        if(!isLogin) {
            Intent intent = new Intent(this, SignupLoginActivity.class);
            startActivityForResult(intent, SIGNUP_LOGININ);
        }
    }

    public void manageBaskets(View view) {

        if(isLogin) {
            Intent intent = new Intent(this, ManageBasketActivity.class);
            intent.putExtra("baskets", basketJSONStr);
            intent.putExtra("basketDates", basketDateJSONstr);
            startActivityForResult(intent, MANAGE_BASKET);
        }else{
            Toast.makeText(this, "Please log in to continue.", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGNUP_LOGININ){
            if(resultCode == Activity.RESULT_OK) {
                isLogin = data.getBooleanExtra("loggedIn", false);
                loginRefresh();
            }
        }else if(requestCode == MANAGE_BASKET) {
            if(resultCode == RESULT_OK){
                try{
                    basketJSONStr = data.getStringExtra("basketJSONStr");
                    basketDateJSONstr = data.getStringExtra("basketDateJSONStr");
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
