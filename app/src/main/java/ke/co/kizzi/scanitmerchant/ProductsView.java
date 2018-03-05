package ke.co.kizzi.scanitmerchant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ke.co.kizzi.scanitmerchant.adapter.AlertDialogManager;
import ke.co.kizzi.scanitmerchant.adapter.ProductsRecyclerAdapter;
import ke.co.kizzi.scanitmerchant.adapter.Variables;

public class ProductsView extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
        RecyclerView recyclerView;
        ArrayList<String> listName = new ArrayList();
        ArrayList<String> listPrice = new ArrayList();
        ArrayList<String> listDescription = new ArrayList();
        ArrayList<String> listID = new ArrayList();

        //create sessions to store/retrieve selections
        SharedPreferences sharedpreferences;

        SharedPreferences.Editor editor;
public static final String USERPREFERENCES = "UserDetails" ;

// Progress dialog
private ProgressDialog pDialog;
private static Variables address = new Variables();
// API urls
private static String URL_PRODUCTS = address.getAddress()+"/products";

        AlertDialogManager alert = new AlertDialogManager();

        SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_view);

        sharedpreferences = getSharedPreferences(USERPREFERENCES,
                Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        recyclerView= (RecyclerView) findViewById(R.id.recyclerProducts);

        swipeRefreshLayout.setOnRefreshListener(this);
        if(isNetworkAvailable()){
            new getProducts().execute();
        }else{
            Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onRefresh() {
        if(isNetworkAvailable()){
            new getProducts().execute();
        }else{
            Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
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


    /**
     * Async task class to get json by making HTTP call
     * */
    private class getProducts extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ProductsView.this);
            pDialog.setMessage("loading..");
            pDialog.setCancelable(true);
            pDialog.show();

        }
        @Override
        protected Void doInBackground(Void... arg0){
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("api_token", sharedpreferences.getString("token",null)));
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String json = sh.makeServiceCall(URL_PRODUCTS, ServiceHandler.GET,params);

            //shows the response that we got from the http request on the logcat
            Log.d("my_Response: ", "> " + json);

            listName.clear();
            listID.clear();
            listPrice.clear();
            listDescription.clear();

            if (json != null) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // stopping swipe refresh
                        swipeRefreshLayout.setRefreshing(false);
                        pDialog.dismiss();
                    }
                });

                try {
                    JSONObject jsonObj = new JSONObject(json);
                    if (jsonObj != null) {

                        String status = jsonObj.getString("status");
                        String id="";
                        String name="";
                        String price="";
                        String description="";

                        if(status.equals("success")){
                            final JSONArray dataArr = jsonObj.getJSONArray("products");

                            if(dataArr.length()<1){
                                // Existing data
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),"No products at the moment",Toast.LENGTH_SHORT).show();
                                        pDialog.dismiss();
                                    }
                                });
                            }
                            for(int i =0; i<dataArr.length();i++){
                                JSONObject catObj = (JSONObject) dataArr.get(i);

                                id = catObj.getString("id");
                                name = catObj.getString("name");
                                price = catObj.getString("price");
                                description = catObj.getString("description");

                                listName.add(name);
                                listID.add(id);
                                listPrice.add(price);
                                listDescription.add(description);

                            }
                            // Existing data
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pDialog.dismiss();

                                    ProductsRecyclerAdapter adapter=new ProductsRecyclerAdapter(ProductsView.this,listPrice.toArray(new String[listPrice.size()]),
                                            listName.toArray(new String[listName.size()]),listDescription.toArray(new String[listDescription.size()]));
                                    recyclerView.setAdapter(adapter);
                                    recyclerView.setHasFixedSize(true);
                                    recyclerView.setLayoutManager(new LinearLayoutManager(ProductsView.this));
                                }
                            });
                        }else{
                            listName.clear();
                            listID.clear();
                            listPrice.clear();
                            listDescription.clear();

                            // Existing data
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alert.showAlertDialog(
                                            ProductsView.this,
                                            "Sorry",
                                            "Please try again",
                                            false);
                                    pDialog.dismiss();
                                }
                            });
                        }

                    } else {
                        // Existing data
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                alert.showAlertDialog(
                                        ProductsView.this,
                                        "Failed",
                                        "Failed",
                                        false);
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
                                ProductsView.this,
                                "Error",
                                "No internet connection",
                                false);
                        pDialog.dismiss();
                        alert.notify();
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
        }

    }

}
