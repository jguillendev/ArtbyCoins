package com.ti.jguillendev.artbycoins;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.ti.jguillendev.utils.IabHelper;
import com.ti.jguillendev.utils.IabResult;
import com.ti.jguillendev.utils.InAppPurchaseSimpleProduct;
import com.ti.jguillendev.utils.SkuPurchaseResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivityRespaldo extends AppCompatActivity {


    // youtube:       https://www.youtube.com/watch?v=mnA0gaQWtAM
    // documentacion: https://developer.android.com/google/play/billing/billing_integrate?hl=es
    /*
     * PRODUCTOS HARCODEADOS POR PRUEBAS
     */

    String premiumUpgradePrice = "";
    String superCoin100 = "";
    String superCoin200 = "";
    String superCoin500 = "";

    String SkuStoreAction ="SaveSku";

    IabHelper mHelper;
    IInAppBillingService mService;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    ArrayList<InAppPurchaseSimpleProduct> availablePurchaseProducts = null;
    private RecyclerView _recyclerView;
    private RecyclerView.LayoutManager _rvLayoutManager;
    private AppPurchaseProductsRecyclerViewAdapter _rvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //SOLO PARA PRUEBAS
                /*
                if(SkuStoreAction == "SaveSku"){
                    storeSetSkuPurchaseToken("best_super_coin1000","ABCDE_PURCHASED_TOKEN_TEST");
                    SkuStoreAction = "ReadSku";
                    Snackbar.make(view,"SavedSku", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else if(SkuStoreAction == "ReadSku"){

                    String tokenSku = storeGetSkuPurchasedToken("best_super_coin1000");
                    SkuStoreAction = "ReadSku";
                    Snackbar.make(view,"GetSku: " + tokenSku, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                */


                try {
                    getProducts();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        });

        fillExampleList();
        buildRecyclerView();

        /*
        Button btnComprar = (Button) findViewById(R.id.btnComprar);
        btnComprar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Empezo la compra! Yea", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                try {
                    getProducts();
                    //showToastInline("SUCCESS","86","getProducts()","success");
                } catch (JSONException e) {
                    e.printStackTrace();
                    showToastInline("JSONException","87","getProducts()","ERROR");
                } catch (RemoteException e) {
                    e.printStackTrace();
                    showToastInline("RemoteException","90","getProducts()","ERROR");
                }
            }
        });
        */

        //in-app purchase code
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiFaClfQW6nuoUn7BLN+FIj7MLXqcMqfUUbwrcu72KCt3Gx/1Ps0HrO3pn2W9iDmahldHbncmF04rFPzXvmotULhFi1pkc38XUjVFevWRoRvBMeXC+XwpeRlbT4hq5pahavXDb5HjN0AhGxupAHlvshkCJeIyohy/425ri1EqgcRIbYTQlfPC80P1zdJ2/vpvpN1XvgJquJqoqGI7oV0Yc3vASqb/wTt7I2MUwBghf4ACIq87LPEhMIsJcmT/RLXeSAgoC1oefs5ZcvAXIdJv4z9uyOzZwKSRmiZBcWGdaghz8W1Imx3ZzTyb4JuduTLTdO5mXjB0ydcsMfgVuf15gwIDAQAB";
        this.mHelper = new IabHelper(this, base64EncodedPublicKey);
        this.mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            @Override
            public void onIabSetupFinished(IabResult result) {
                if(!result.isSuccess()) {
                    Toast.makeText(getBaseContext(),"IAB is NOT fully setup", Toast.LENGTH_LONG).show();
                }
                Toast.makeText(getBaseContext(),"IAB is fully setup", Toast.LENGTH_LONG).show();
            }
        });

        Intent serviceIntent =  new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

    }

    private void fillExampleList(){

        availablePurchaseProducts = new ArrayList<>();


        /*
        * PRODUCTOS DE PRUEBA
        * https://developer.android.com/google/play/billing/billing_testing#billing-testing-test
        * android.test.purchased         //COMPRADO
        * android.test.canceled          //ERROR, TARJETA NO VALIDA, CANCELADA POR EL USUARIO
        * android.test.refunded          //REMBOLSO DEL DINERO
        * android.test.item_unavailable  //PRODUCTO NO ENCONTRADO
        *
        * */

        //TEST PURCHASED
        InAppPurchaseSimpleProduct testPURCHASED =
                new InAppPurchaseSimpleProduct(R.drawable.ic_android_black,"android.test.purchased","5.00");
        String testPurchasedToken = storeGetSkuPurchasedToken("android.test.purchased");
        if(testPurchasedToken != null) {
            testPURCHASED.setPurchasedText();
            testPURCHASED.setAsPurchased(true);
        }
        availablePurchaseProducts.add(testPURCHASED);

        //TEST CANCELED
        InAppPurchaseSimpleProduct testCANCELED =
                new InAppPurchaseSimpleProduct(R.drawable.ic_android_black,"android.test.canceled","5.00");
        String testCANCELEDToken = storeGetSkuPurchasedToken("android.test.canceled");
        if(testPurchasedToken != null) {
            testCANCELED.setPurchasedText();
            testCANCELED.setAsPurchased(true);
        }
        availablePurchaseProducts.add(testCANCELED);

        //TEST REFUNDED
        InAppPurchaseSimpleProduct testREFUNDED =
                new InAppPurchaseSimpleProduct(R.drawable.ic_android_black,"android.test.refunded","5.00");
        String testREFUNDEDToken = storeGetSkuPurchasedToken("android.test.refunded");
        if(testPurchasedToken != null) {
            testREFUNDED.setPurchasedText();
            testREFUNDED.setAsPurchased(true);
        }
        availablePurchaseProducts.add(testREFUNDED);

        //best_super_coin005
        InAppPurchaseSimpleProduct sp005 = new InAppPurchaseSimpleProduct(R.drawable.ic_android_black,"best_super_coin005","5.00");
        String sp005Token =storeGetSkuPurchasedToken("best_super_coin005");
        if(sp005Token != null) {
            sp005.setPurchasedText();
            sp005.setAsPurchased(true);
        }
        availablePurchaseProducts.add(sp005);


        //best_super_coin010
        InAppPurchaseSimpleProduct sp010 = new InAppPurchaseSimpleProduct(R.drawable.ic_android_black,"best_super_coin010","10.00");
        String sp010Token =storeGetSkuPurchasedToken("best_super_coin010");
        if(sp010Token != null) {
            sp010.setPurchasedText();
            sp010.setAsPurchased(true);
        }
        availablePurchaseProducts.add(sp010);


        //best_super_coin050
        InAppPurchaseSimpleProduct sp050 = new InAppPurchaseSimpleProduct(R.drawable.ic_android_black,"best_super_coin050","50.00");
        String sp050Token =storeGetSkuPurchasedToken("best_super_coin050");
        if(sp050Token != null) {
            sp050.setPurchasedText();
            sp050.setAsPurchased(true);
        }
        availablePurchaseProducts.add(sp050);

        //best_super_coin100
        InAppPurchaseSimpleProduct sp100 = new InAppPurchaseSimpleProduct(R.drawable.ic_android_black,"best_super_coin100","100.00");
        String sp100Token =storeGetSkuPurchasedToken("best_super_coin100");
        if(sp100Token != null) {
            sp100.setPurchasedText();
            sp100.setAsPurchased(true);
        }
        availablePurchaseProducts.add(sp100);

        //best_super_coin200
        InAppPurchaseSimpleProduct sp200 = new InAppPurchaseSimpleProduct(R.drawable.ic_android_black,"best_super_coin200","200.00");
        String sp200Token =storeGetSkuPurchasedToken("best_super_coin200");
        if(sp200Token != null) {
            sp200.setPurchasedText();
            sp200.setAsPurchased(true);
        }
        availablePurchaseProducts.add(sp200);

        //best_super_coin500
        InAppPurchaseSimpleProduct sp500 = new InAppPurchaseSimpleProduct(R.drawable.ic_android_black,"best_super_coin500","500.00");
        String sp500Token =storeGetSkuPurchasedToken("best_super_coin500");
        if(sp500Token != null) {
            sp500.setPurchasedText();
            sp500.setAsPurchased(true);
        }
        availablePurchaseProducts.add(sp500);

        //best_super_coin1000
        InAppPurchaseSimpleProduct sp1000 = new InAppPurchaseSimpleProduct(R.drawable.ic_android_black,"best_super_coin1000","1000.00");
        String sp1000Token =storeGetSkuPurchasedToken("best_super_coin1000");
        if(sp1000Token != null) {
            sp1000.setPurchasedText();
            sp1000.setAsPurchased(true);
        }
        availablePurchaseProducts.add(sp1000);



    }

    private void buildRecyclerView(){

        _recyclerView = findViewById(R.id.productsRecyclerView);
        _recyclerView.setHasFixedSize(true);
        _rvLayoutManager = new LinearLayoutManager(this);
        _rvAdapter = new AppPurchaseProductsRecyclerViewAdapter(availablePurchaseProducts);

        _recyclerView.setLayoutManager(_rvLayoutManager);
        _recyclerView.setAdapter(_rvAdapter);


        _rvAdapter.setOnAppPurchaseProductLongTapListener(new AppPurchaseProductsRecyclerViewAdapter.OnItemLongTapListener() {
            @Override
            public void onItemLongClicked(int position) {
                try
                {

                    InAppPurchaseSimpleProduct avp = availablePurchaseProducts.get(position);
                    //PRUEBAS PRUEBAS PRUEBAS
                    //storeSetSkuPurchaseToken(avp.getpSku(),null);
                    //avp.setNotPurchased();


                    //CONSUMIR LA COMPRA
                    if(avp.getpPurchased() == true)
                        consumePurchaseNow(avp.getpSku(),avp.getpTokenPurchase());
                }
                catch (Exception ex){
                    showToastInline("EXCEPTION","261","CONSUME_PURCHASE","ERROR CONSUMIENDO LA COMPRA!");
                }
            }
        });


        _rvAdapter.setOnAppPurchaseProductTapListener(new AppPurchaseProductsRecyclerViewAdapter.OnItemTapListener() {
            @Override
            public void onItemClicked(int position) {

                try {

                    InAppPurchaseSimpleProduct p = availablePurchaseProducts.get(position);
                    if(p.getpPurchased() == false)
                        createBuyIntent(p.getpSku());

                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    private void showToastInline(String tipo, String line, String action, String msg){
        Toast.makeText(getBaseContext(),tipo+"! ln:"+line+" action: "+action+" msg: "+msg,Toast.LENGTH_LONG).show();
    }

    private void getProducts() throws JSONException, RemoteException {

        ArrayList<String> skuList = new ArrayList<String>();
        skuList.add("premiumUpgrade");
        skuList.add("best_super_coin005");
        skuList.add("best_super_coin010");
        //skuList.add("best_super_coin100");
        //skuList.add("best_super_coin200");
        //skuList.add("best_super_coin500");
        //skuList.add("best_super_coin1000");

        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList("ITEM_ID_LIST",skuList);
        Bundle skuDetails = mService.getSkuDetails(3,getPackageName(),"inapp", querySkus);

        //LISTING PRODUCT DETAILS
        if(skuDetails != null){
            int response = skuDetails.getInt("RESPONSE_CODE");
            if(response == 0){
                ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                for(String thisResponse : responseList){

                    JSONObject xob = new JSONObject(thisResponse);
                    String sku = xob.getString("productId");
                    String price = xob.getString("price");
                    if(sku.equals("premiumUpgrade"))
                        premiumUpgradePrice = price;
                    if(sku.equals("best_super_coin100"))
                        superCoin100= price;
                    if(sku.equals("best_super_coin200"))
                        superCoin200= price;
                    if(sku.equals("best_super_coin500")){
                        superCoin500= price;
                    }
                    Log.w("READING_SKUS","SKU:"+ sku + " PRICE:" +price);
                }
                showToastInline("SKU_DETAILS_END","161","getSkuDetails()","END_SUCCESS");
            }

        }

    }


    private void createBuyIntent(String sku) throws RemoteException, IntentSender.SendIntentException {
        /*
         * La string developerPayload se usa para especificar cualquier
         * argumento adicional que desees que Google Play envíe junto con la información de compra.
         *
         * */
        String DEVELOPER_PAYLOAD_STRING_EXAMPLE = "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ";
        Bundle buyIntentBundle =
                mService.getBuyIntent(
                        3,
                        getPackageName(),
                        sku,
                        "inapp",
                        DEVELOPER_PAYLOAD_STRING_EXAMPLE);

        //STARTING THE PURCHASE FLOW
        PendingIntent pendingIntent =
                buyIntentBundle.getParcelable("BUY_INTENT");


        //Para completar la operación de compra
        startIntentSenderForResult(
                pendingIntent.getIntentSender(), 1001,
                new Intent(),
                Integer.valueOf(0),
                Integer.valueOf(0),
                Integer.valueOf(0));

                 /*
                 *   La información de compra del pedido es una string en formato JSON que se
                 *   asigna a la clave INAPP_PURCHASE_DATA en el Intent de respuesta.
                 *   Por ejemplo:
                 *      '{
                           "orderId":"GPA.1234-5678-9012-34567",
                           "packageName":"com.example.app",
                           "productId":"exampleSku",
                           "purchaseTime":1345678900000,
                           "purchaseState":0,
                           "developerPayload":"bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ",
                           "purchaseToken":"opaque-token-up-to-1000-characters"
                         }'
                 * */


    }


    private void getPurchaseHistory() throws RemoteException {

        Bundle ownedItems = mService.
                getPurchases(3, getPackageName(), "inapp", null);

        int response = ownedItems.getInt("RESPONSE_CODE");
        if (response == 0) {
            ArrayList<String> ownedSkus =
                    ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
            ArrayList<String>  purchaseDataList =
                    ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
            ArrayList<String>  signatureList =
                    ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
            String continuationToken =
                    ownedItems.getString("INAPP_CONTINUATION_TOKEN");

            for (int i = 0; i < purchaseDataList.size(); ++i) {
                String purchaseData = purchaseDataList.get(i);
                String signature = signatureList.get(i);
                String sku = ownedSkus.get(i);

                // do something with this purchase information
                // e.g. display the updated list of products owned by user
            }

            // if continuationToken != null, call getPurchases again
            // and pass in the token to retrieve more items
        }

    }

    private void consumePurchaseNow(String sku, String purchasedToken) throws RemoteException {
        /*
         * Advertencia: No llames al método consumePurchase en el subproceso principal.
         * Si lo haces, se desencadenará una solicitud de red que podría bloquear tu subproceso principal.
         * Como alternativa, crea un subproceso separado y llama al método consumePurchase desde ese subproceso.
         * */

        try
        {

            int response = mService.consumePurchase(3, getPackageName(), purchasedToken);
            storeSetSkuPurchaseToken(sku,null);
            showToastInline("SUCCESS","406","CONSUMED_PURCHASE","RESULT:" +response);

            //obtener la posicion de un item
            int position = getItemPositionBySku(sku);

            //NOTIFICAR AL ADAPTER QUE SE ACTUALIZO UN ITEM
            availablePurchaseProducts.get(position).setNotPurchased();
            availablePurchaseProducts.get(position).setAsPurchased(false);
            _rvAdapter.notifyItemChanged(position);

        }
        catch (Exception ex){
            showToastInline("EXCEPTION","409","CONSUME_PURCHASE","");
        }
    }

    private int getItemPositionBySku(String sku){

        boolean encontrado=false;
        int position = 0;
        do{
            if(availablePurchaseProducts.get(position).getpSku() == sku)
                encontrado = true;
            else
                position = position + 1;

        }while (encontrado == false);
        encontrado = true;

        return position;
    }

    private void storeSetSkuPurchaseToken(String sku, String token){

        SharedPreferences sPreferences =
                getSharedPreferences("Purchased_Skus", MODE_PRIVATE); //MODE_PRIVATE: solo esta app tiene acceso.
        SharedPreferences.Editor spEditor = sPreferences.edit();
        if(token != null)
            spEditor.putString(sku,token);
        if(token == null)
            spEditor.remove(sku);

        spEditor.apply();
    }

    private String storeGetSkuPurchasedToken(String sku){

        SharedPreferences sPreferences = getSharedPreferences("Purchased_Skus", MODE_PRIVATE); //MODE_PRIVATE: solo esta app tiene acceso.
        return sPreferences.getString(sku,null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1001)
        {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {

                try {

                    JSONObject jo = new JSONObject(purchaseData);
                    String _productId = jo.getString("productId");
                    String _purchaseToken = jo.getString("purchaseToken");


                    //Adquiriendo datos complementarios
                    String _orderId = jo.getString("orderId");
                    String _purchaseTime = jo.getString("purchaseTime");
                    String _purchaseState = jo.getString("purchaseState");
                    String _packageName = jo.getString("packageName");

                    //actualizando la lista tan pronto como es posible
                    //obtener la posicion de un item
                    int position = getItemPositionBySku(_productId);
                    //NOTIFICAR AL ADAPTER QUE SE ACTUALIZO UN ITEM
                    availablePurchaseProducts.get(position).setPurchasedText();
                    availablePurchaseProducts.get(position).setAsPurchased(true);
                    _rvAdapter.notifyItemChanged(position);

                    Toast.makeText(getBaseContext(),"Compraste: " + _productId + "!",Toast.LENGTH_LONG).show();

                    //guardando el TOKEN tan pronto como es posible
                    storeSetSkuPurchaseToken(_productId,_purchaseToken);

                    SkuPurchaseResult purchaseResult
                            = new SkuPurchaseResult(_orderId,_productId,_purchaseTime,_purchaseState, _packageName);
                    purchaseResult.setPurchaseToken(_purchaseToken);

                }
                catch (JSONException e) {
                    Toast.makeText(getBaseContext(),"Failed to parse purchase data.",Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }

            }
        }
        else{
            showToastInline("COMPRA","","","No se realizo la compra");
        }

        //UNICAMENTE PARA PRUEBAS
        /*
            int position = getItemPositionBySku("best_super_coin005");
            //NOTIFICAR AL ADAPTER QUE SE ACTUALIZO UN ITEM
            availablePurchaseProducts.get(position).setPurchasedText();
            availablePurchaseProducts.get(position).setAsPurchased(true);
            _rvAdapter.notifyItemChanged(position);
        */
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(this.mHelper != null)
            this.mHelper.dispose();
        this.mHelper = null;

        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
