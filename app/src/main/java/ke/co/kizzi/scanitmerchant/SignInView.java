package ke.co.kizzi.scanitmerchant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ke.co.kizzi.scanitmerchant.adapter.AlertDialogManager;
import ke.co.kizzi.scanitmerchant.adapter.Variables;

public class SignInView extends AppCompatActivity {
    EditText txtUsername,txtPassword;
    Button btnLogin;
    TextView textView2,textView3;

    ProgressDialog loading = null;
    private static Variables address = new Variables();
    // API urls
    private static String URL_LOGIN =address.getAddress()+"/login";
    ProgressDialog pDialog;
    AlertDialogManager alert = new AlertDialogManager();

    //create sessions to store/retrieve selections
    SharedPreferences sharedpreferences;

    SharedPreferences.Editor editor;
    public static final String USERPREFERENCES = "UserDetails" ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in_view);

        //declare shared preferences
        sharedpreferences = getSharedPreferences(USERPREFERENCES,
                Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);

        txtUsername = (EditText) findViewById(R.id.txtUsername);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        btnLogin = (Button)findViewById(R.id.btnLogin);

        // Font path
        String fontPath = "myfonts/AdventPro-Regular.ttf";
        // Loading Font Face
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
//
//        // Applying font
        txtPassword.setTypeface(tf);
        txtUsername.setTypeface(tf);
        btnLogin.setTypeface(tf);

        textView2.setTypeface(tf);
        textView3.setTypeface(tf);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtUsername.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"Enter Username/Email",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(txtPassword.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"Enter Password",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(isNetworkAvailable()){
                    new login().execute();
                }else{
                    Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });


    }

    /**
     * Async task class to get json by making HTTP call
     * */
    private class login extends AsyncTask<Void, Void, Void> {
        String username = txtUsername.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SignInView.this);
            pDialog.setMessage("signing in...");
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("email", username));
            params.add(new BasicNameValuePair("password", password));

            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
            // Making a request to url and getting response
            String json = sh.makeServiceCall(URL_LOGIN, ServiceHandler.POST,params);

            //shows the response that we got from the http request on the logcat
            Log.d("Response: ", "> " + json + URL_LOGIN);
            //result = jsonStr;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pDialog.dismiss();
                }
            });
            if (json != null) {
                try {
                    JSONObject jsonObj = new JSONObject(json);
                    String token = null;
                    if (jsonObj != null) {
                        String status = jsonObj.get("status").toString();
                        if (status.equals("success")) {
                            // Existing data
                            token = jsonObj.get("token").toString();

                            editor.putString("token", token);

                            JSONObject userObj = jsonObj.getJSONObject("user");
                            String name = userObj.get("name").toString();
                            String email = userObj.get("email").toString();

                            editor.putString("name", name);
                            editor.putString("email", email);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    editor.commit();
                                }
                            });

                            //go to home page
                            Intent i = new Intent(SignInView.this,HomeView.class);
                            startActivity(i);
                            SignInView.this.finish();
                        } else {
                            final String message = jsonObj.get("message").toString();
                            // Existing data
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alert.showAlertDialog(
                                            SignInView.this,
                                            "Failed",
                                            message,
                                            false);
                                    pDialog.dismiss();
                                }
                            });
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                // Error in connection
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        alert.showAlertDialog(
                                SignInView.this,
                                "Failed",
                                "No Internet Connection",
                                false);
                        pDialog.dismiss();
                    }
                });

            }
            return null;
        }
        protected void onPostExecute(Void result) {
            // dismiss the dialog once done
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            //add intent
        }
    }



    // Private class isNetworkAvailable
    private boolean isNetworkAvailable() {
        // Using ConnectivityManager to check for Network Connection
        ConnectivityManager connectivityManager = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    @Override
    public void onBackPressed (){
        moveTaskToBack(true);
        SignInView.this.finish();
        System.exit(0);
    }

}
