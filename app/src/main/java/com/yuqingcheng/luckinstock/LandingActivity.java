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

        /*

        Map<String, Map<String, Integer>> jsonMap = new HashMap<>();

        jsonMap.put("YuqingPremium", new HashMap<String, Integer>());

        jsonMap.get("YuqingPremium").put("AAPL", 100);

        jsonMap.get("YuqingPremium").put("GOOG", 200);

        JSONObject jsonObject = new JSONObject(jsonMap);

        Log.i("JSON", jsonObject.toString());
        */






        if(isLogin) {
            currentUser = ParseUser.getCurrentUser();

            userName.setText(currentUser.getUsername());

        }

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
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
        Intent intent = new Intent(this, ManageBasketActivity.class);

        startActivityForResult(intent, MANAGE_BASKET);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGNUP_LOGININ){
            if(resultCode == Activity.RESULT_OK) {
                boolean loggedIn = data.getBooleanExtra("loggedIn", false);

                if(loggedIn) {
                    isLogin = true;
                    currentUser = ParseUser.getCurrentUser();
                    userName.setText(currentUser.getUsername());
                }
            }
        }
    }
}
