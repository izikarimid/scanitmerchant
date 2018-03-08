package ke.co.kizzi.scanitmerchant;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ke.co.kizzi.scanitmerchant.adapter.AlertDialogManager;
import ke.co.kizzi.scanitmerchant.adapter.Variables;

public class HomeView extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    ImageView btnScan;
    String barcode = "";

    //create sessions to store/retrieve selections
    SharedPreferences sharedpreferences;

    SharedPreferences.Editor editor;
    public static final String USERPREFERENCES = "UserDetails" ;

    ProgressDialog loading = null;
    private static Variables address = new Variables();
    // API urls
    private static String URL_LOGOUT = address.getAddress() + "/logout";
    private  static String URL_SCAN_PRODUCT = address.getAddress() + "/checkproduct";

    ProgressDialog pDialog;
    AlertDialogManager alert = new AlertDialogManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedpreferences = getSharedPreferences(USERPREFERENCES,
                Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        btnScan = (ImageView) findViewById(R.id.btnScan);

        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanBarcode();
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.products) {
            Intent i =new Intent(HomeView.this, ProductsView.class);
            startActivity(i);
        } else if (id == R.id.logout) {
            //logout
            if(sharedpreferences.contains("token")){

                if(isNetworkAvailable()){
                    new logout().execute();
                }else{
                    Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
                }

            }else{
                Intent i = new Intent(HomeView.this,SignInView.class);
                startActivity(i);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    /**
     * Async task class to get json by making HTTP call
     * */
    private class logout extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(HomeView.this);
            pDialog.setMessage("signing out...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("api_token", sharedpreferences.getString("token",null)));

            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
            // Making a request to url and getting response
            String json = sh.makeServiceCall(URL_LOGOUT, ServiceHandler.POST,params);

            //shows the response that we got from the http request on the logcat
            Log.d("Response: ", "> " + json + sharedpreferences.getString("token",null));
            //result = jsonStr;
            if (json != null) {
                try {
                    JSONObject jsonObj = new JSONObject(json);
                    if (jsonObj != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                editor.remove("token");
                                editor.remove("name");
                                editor.remove("email");
                                editor.commit();
                                Intent i = new Intent(HomeView.this,SignInView.class);
                                startActivity(i);
                                HomeView.this.finish();
                                pDialog.dismiss();
                            }
                        });

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
                                HomeView.this,
                                "Failed",
                                "No internet connection",
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



    //product bar code mode
    public void scanBarcode() {
        if (ContextCompat.checkSelfPermission(HomeView.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeView.this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }else{
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            new IntentIntegrator(HomeView.this).initiateScan();
        }
    }



    // Get the results:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if (data != null) {
                if(data.getStringExtra("SCAN_RESULT") != null){

                    //get the extras that are returned from the intent
                    barcode = data.getStringExtra("SCAN_RESULT");
                    String format = data.getStringExtra("SCAN_RESULT_FORMAT");
                    Toast.makeText(getApplicationContext(),barcode,Toast.LENGTH_SHORT).show();
                    Log.d("barcode",barcode);
                    if(barcode.length() < 1){
                        Toast.makeText(getApplicationContext(),"Please scan a valid barcode",Toast.LENGTH_SHORT).show();
                    }else if(!isNetworkAvailable()){
                        Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
                    }else{
                        new checkProduct().execute();
                    }

                }
            } else if (resultCode == RESULT_CANCELED) {

                // Handle cancel
            }
            //}
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    /**
     * Async task class to get json by making HTTP call
     * */
    private class checkProduct extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(HomeView.this);
            pDialog.setMessage("checking...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();
            // Making a request to url and getting response
            String json = sh.makeServiceCall(URL_SCAN_PRODUCT+"/"+barcode+"?api_token="+sharedpreferences.getString("token",null), ServiceHandler.GET,null);

            //shows the response that we got from the http request on the logcat
            Log.d("Response: ", "> " + json);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (pDialog.isShowing())
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

                            Boolean product_exists = jsonObj.getBoolean("product_exists");

                            if(product_exists){

                                final JSONObject productObj = jsonObj.getJSONObject("product");
                               // Toast.makeText(getApplicationContext(),productObj+"",Toast.LENGTH_SHORT).show();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                        if (pDialog.isShowing())
                                            pDialog.dismiss();
                                            updateDialog();;
                                            String name = productObj.getString("name");
//                                            String student_status = studentObj.getString("status");
//                                            String registration_date = studentObj.getString("created_at");
//                                            String email = studentObj.getString("email");
//                                            String id_number = studentObj.getString("id_number");
//
//                                            Bundle bundle = new Bundle();
//                                            bundle.putString("name",name);
//                                            bundle.putString("student_status",student_status);
//                                            bundle.putString("registration_date",registration_date);
//                                            bundle.putString("email",email);
//                                            bundle.putString("id_number",id_number);
//                                            bundle.putString("qr_code",qr_code);
//                                            Intent i = new Intent(HomeView.this,AddProductView.class);
//                                            i.putExtras(bundle);
//                                            startActivity(i);

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }


                                    }
                                });

                            }else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Product doesnt exist proceed to creating one", Toast.LENGTH_SHORT).show();
                                        Log.d("product_not_there", "product_not_there");
                                        Bundle bundle = new Bundle();
                                        bundle.putString("barcode", barcode);
                                        Intent i = new Intent(HomeView.this, AddProductView.class);
                                        i.putExtras(bundle);
                                        startActivity(i);
                                    }
                                });

                            }




                        }
                        else if(status.equals("error")){
                            final String message = jsonObj.getString("message");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alert.showAlertDialog(
                                            HomeView.this,
                                            "Failed",
                                            message,
                                            false);
                                    if (pDialog.isShowing())
                                        pDialog.dismiss();
                                }
                            });

                        }

                        else {
                            // Existing data
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alert.showAlertDialog(
                                            HomeView.this,
                                            "Failed",
                                            "Invalid student number provided",
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
                                HomeView.this,
                                "Error",
                                "No internet connection",
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


    public void updateDialog(){
        // custom dialog
        final Dialog dialog = new Dialog(HomeView.this);
        dialog.setContentView(R.layout.update_product_dialog);
        dialog.setTitle("Update Product");

        final EditText txtQuantity = (EditText) dialog.findViewById(R.id.txtQuantity);
        Button btnSubmit = (Button) dialog.findViewById(R.id.btnSubmit);
        // if button is clicked, close the custom dialog
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtQuantity.getText().toString().trim().length() > 0 ){
                    //reasons = txtReason.getText().toString().trim();
                    //new updateProduct().execute();
                }else{
                    Toast.makeText(getApplicationContext(),"Please input the quantity",Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        dialog.show();

    }


}
