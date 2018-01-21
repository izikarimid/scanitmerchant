package ke.co.kizzi.scanitmerchant;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class SplashScreenView extends AppCompatActivity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1000;
    //shared preference used to manage user sessions
    SharedPreferences sharedpreferences;
    public static final String USERPREFERENCES = "UserDetails" ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_view);

        //check if user has already logged in
        sharedpreferences = getSharedPreferences(USERPREFERENCES,
                Context.MODE_PRIVATE);

        new Handler().postDelayed(new Runnable() {

		/*
			 * Showing splash screen with a timer. This will be useful when you
			 * want to show case your app logo / company*/


            @Override
            public void run() {
                Intent i;
              //  if (sharedpreferences.contains("token")) {
                    i = new Intent(SplashScreenView.this, HomeView.class);
                    startActivity(i);
//                } else {
//                    i = new Intent(SplashScreenView.this, SignInView.class);
//                    startActivity(i);
//                }

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public void onBackPressed (){
        System.exit(0);
    }
}
