package com.yuqingcheng.luckinstock;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.List;

public class SignupLoginActivity extends AppCompatActivity implements View.OnClickListener {

    EditText username;
    EditText password;
    Button signupLoginin;
    ImageButton avatar;
    boolean onSignup;
    TextView changeSignupMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_login);

        username = (EditText) findViewById(R.id.loginUsername);
        password = (EditText) findViewById(R.id.loginPassword);
        avatar = (ImageButton) findViewById(R.id.avatar);
        signupLoginin = (Button) findViewById(R.id.signupLoginin);
        changeSignupMode = (TextView) findViewById(R.id.changeSignupMode);
        changeSignupMode.setOnClickListener(this);
        onSignup = true;

        getWindow().setBackgroundDrawableResource(R.drawable.background_3);


    }

    public void handleSignupLoginin(View view) {
        String username = this.username.getText().toString();
        String password = this.password.getText().toString();
        if(username.length() == 0 || password.length() == 0) {
            Toast.makeText(getApplicationContext(),
                    "Username and password cannot be blank.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(onSignup){
            if(username.length() > 10) {
                Toast.makeText(getApplicationContext(),
                        "Username should be no more than 12 characters.", Toast.LENGTH_SHORT).show();
                return;
            }
            if(usernameFormCheck(username)) {
                if(password.length() < 10 || password.length() > 20) {
                    Toast.makeText(getApplicationContext(),
                            "Password should be no less than 10 and no more than 20 characters.", Toast.LENGTH_SHORT).show();
                    return;
                }
                ParseQuery<ParseUser> query = ParseUser.getQuery();
                query.whereEqualTo("username", username);
                query.setLimit(1);
                query.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        if(e == null) {
                            if(objects.size() > 0) {
                                Log.i("Find existing username:", "yes");
                                Toast.makeText(getApplicationContext(),
                                        "Username already exists, please try another.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }else{
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });

                if(passwordFormCheck(password)) {

                    ParseUser user = new ParseUser();
                    user.setUsername(username);
                    user.setPassword(password);
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null) {
                                Toast.makeText(getApplicationContext(), "Successful Sign Up!", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent();
                                intent.putExtra("loggedIn", true);
                                setResult(RESULT_OK, intent);
                                finish();
                            }else{
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    });
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Password should contain at least a letter and a number, and is case-sensitive.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }else{
                Toast.makeText(getApplicationContext(),
                        "Username should only contain letters and/or numbers, and cannot start with number.", Toast.LENGTH_SHORT).show();
                return;
            }

        }else{

            ParseUser.logOut();
            try {
                ParseUser.logIn(username, password);
                Toast.makeText(getApplicationContext(), "Successful Log In!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra("loggedIn", true);
                setResult(RESULT_OK, intent);
                finish();
            }catch(Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.changeSignupMode) {
            onSignup = !onSignup;
        }

        if(onSignup) {
            signupLoginin.setText("Sign Up");
            changeSignupMode.setText("Have an account? Login in here.");

        }else{
            signupLoginin.setText("Log In");
            changeSignupMode.setText("Don't have an account? Sign up here.");
        }
    }

    private boolean usernameFormCheck(String username) {
        if(! isLetter(username.charAt(0))) return false;

        for(char c : username.toCharArray()) {
            if(! isLetter(c) &&  !isNumeric(c)) return false;
        }

        return true;

    }

    private boolean passwordFormCheck(String password) {
        int numLetter = 0;
        int numNumber = 0;

        for(char c : password.toCharArray()) {
            if(isNumeric(c)) numNumber++;
            if(isLetter(c)) numLetter++;
        }

        return numLetter > 0 && numNumber >0;
    }

    private boolean isLetter(char c) {
        return c <= 'z' && c >= 'a' || c <= 'Z' && c >= 'A';
    }

    private boolean isNumeric(char c) {
        return c <= '9' && c >= '0';
    }
}
