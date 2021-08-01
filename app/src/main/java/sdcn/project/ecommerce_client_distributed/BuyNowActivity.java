package sdcn.project.ecommerce_client_distributed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import sdcn.project.ecommerce_client_distributed.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.Integer.parseInt;

public class BuyNowActivity extends AppCompatActivity
{

    // We’ll use this constant to pass the ID of the pizza as extra information in the intent.
    public static final String EXTRA_PRODUCT_ATTRIBUTES = "purchaseID";

    // firebase objects
    private FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;

    // objects
    private Bill bill;
    private User user;
    private Compra purchase;

    // bill
    private boolean isOnTrolley=false;

    // Variables
    private static final String TAG = "bill Uploaded FireStore";
    private boolean uploadIt;
    private List<Boolean> uploadItArrayList;// = new ArrayList<>();
    private List<String> purchasedOutSockProductNameArray = new ArrayList<>();
    private List<Integer> actualFireStoreStockProductArray = new ArrayList<>();
    private List<Integer> purchasedAmountProductOutStockArray = new ArrayList<>();
    private int stockValuesArray[];
    private int counter;

    // Progress bar variables
    private ProgressDialog progressBar;
    private int progressBarStatus = 0;
    private Handler progressBarbHandler = new Handler();
    private int checkProgressTrolleyBarStatus=0;
    private Handler CheckTrolleyProgressBarHandler = new Handler();

    // views
    private TextView textView_purchaseTotalValue;
    private Button button_BuyPurchase, button_doNotBuyPurchase;
    private TextInputEditText TextInputEditText_purchaseName;
    private RadioGroup RadioGroup_purchaseOptions;
    private TextView textView_subtotalTrolleyField;
    private TextView textView_productsList_title;
    private TextView textView_purchaseMethod_title;
    private TextView textView_nameAdvice_title;
    private TextView textView_productName_title;


    // [____________|| Firestore ||____________] [<--BEGIN]
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference fireStoreBillID;

    //fragments
    //Bundle savedInstanceStateCopy;

    // i make a view RecyclerView  and bind it with the xml file that contains the recyclerView.
    private RecyclerView productoRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_now);

        // Declare the viewers
        textView_purchaseTotalValue = findViewById(R.id.textView_purchaseTotalValue);
        TextInputEditText_purchaseName = findViewById(R.id.TextInputEditText_purchaseName);
        RadioGroup_purchaseOptions = (RadioGroup)findViewById(R.id.RadioGroup_purchaseOptions);
        textView_subtotalTrolleyField = findViewById(R.id.textView_subtotalTrolleyField);
        button_BuyPurchase = findViewById(R.id.button_BuyPurchase);
        button_doNotBuyPurchase = findViewById(R.id.button_doNotBuyPurchase);
        // i make a view RecyclerView  and bind it with the xml file that contains the recyclerView
        productoRecycler = (RecyclerView) findViewById(R.id.RecyclerView_trolley);
        // just title views
        textView_productsList_title =  findViewById(R.id.textView_productsList_title);
        textView_purchaseMethod_title=  findViewById(R.id.textView_purchaseMethod_title);
        textView_nameAdvice_title=  findViewById(R.id.textView_nameAdvice_title);
        textView_productName_title =   findViewById(R.id.textView_productName_title);

        // set visibility GONE until check if there are products to purchase
        setVisibility(false);


        // [BEGIN -->] _______________Toolbar_____________________________
        // set the toolbar on the layout as the app bar for the activity
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_buyNow);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        // ___________________________Toolbar___________________ [<-- END]


        // launch the first progress bar
        LaunchFirstProgressBar(productoRecycler);

        // initialize the bill Object

        // If get the product from the "selectedProduct" activity then ill store it in a Bundle var
        Bundle b = getIntent().getBundleExtra("bundle");


        // i have to find if this activity is being open after "selectedProduct" activity, if it is
        //  the case  b.isEmpty() == false.
        // That means the client want to purchase just one product, and not a trolley with
        // more than one product.
        // So if b.isEmpty() == true, then i proceed to retrieve the "Bill" object passed by the
        // "selectedProduct" activity
        if (b!=null)
        {
            // change visibility to VISIBLE cause there is a products to purchase
            setVisibility(true);
            checkProgressTrolleyBarStatus = 100;
            if (!b.isEmpty()) purchase =b.getParcelable("options");

            bill = new Bill
                    (
                            "",
                            "",
                            "",
                            "",
                            null,
                            false,
                            0,
                            purchase
                    );
            setViewers();
            isOnTrolley=false;
        }
        // if b.isEmpty() == true, then the client want to purchase a trolley and i have to retrieve
        // the trolley products in other way, using FireStore
        else
        {
            // download the trolley
            getFireStoreBill();
        }
    }

    public void setVisibility(boolean visibility)
    {
        if (visibility)
        {
            textView_purchaseTotalValue.setVisibility(View.VISIBLE);
            TextInputEditText_purchaseName.setVisibility(View.VISIBLE);
            RadioGroup_purchaseOptions.setVisibility(View.VISIBLE);
            textView_subtotalTrolleyField.setVisibility(View.VISIBLE);
            button_BuyPurchase.setVisibility(View.VISIBLE);
            button_doNotBuyPurchase.setVisibility(View.VISIBLE);
            productoRecycler.setVisibility(View.VISIBLE);
            textView_productsList_title.setVisibility(View.VISIBLE);
            textView_purchaseMethod_title.setVisibility(View.VISIBLE);
            textView_nameAdvice_title.setVisibility(View.VISIBLE);
            textView_productName_title.setVisibility(View.VISIBLE);
        }
        else
        {
            textView_purchaseTotalValue.setVisibility(View.GONE);
            TextInputEditText_purchaseName.setVisibility(View.GONE);
            RadioGroup_purchaseOptions.setVisibility(View.GONE);
            textView_subtotalTrolleyField.setVisibility(View.GONE);
            button_BuyPurchase.setVisibility(View.GONE);
            button_doNotBuyPurchase.setVisibility(View.GONE);
            productoRecycler.setVisibility(View.GONE);
            textView_productsList_title.setVisibility(View.GONE);
            textView_purchaseMethod_title.setVisibility(View.GONE);
            textView_nameAdvice_title.setVisibility(View.GONE);
            textView_productName_title.setVisibility(View.GONE);
        }

    }

    // <==========|| Get the bill from fireStore ||========================>[BEGIN]
    public void getFireStoreBill()
    {
        db.collection("Usuarios")
                .document(currentFirebaseUser.getUid())
                .collection("Carrito")
                .document(currentFirebaseUser.getEmail()+"Trolley")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
        {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                // if a Factura already exists then download it and the Producto to it
                if (documentSnapshot.exists())
                {
                    // stop the first progress bar
                    checkProgressTrolleyBarStatus=100;
                    // change visibility to VISIBLE cause there is at least a product to purchase
                    setVisibility(true);

                    //setContentView(R.layout.activity_buy_now);
                    // get the bill
                    bill=documentSnapshot.toObject(Bill.class);
                    assert bill != null;
                    purchase = bill.getCompra();

                    // set the boolean value to know if the bill come from a FireStore trolley
                    isOnTrolley = true;

                    // launch view;
                    setViewers();
                }
                else
                {
                    checkProgressTrolleyBarStatus=100;
                    // Change the layout if the fireStore Facturas collection is empty
                    setContentView(R.layout.activity_empty_trolley);

                    // [BEGIN -->] _______________Toolbar_____________________________
                    // set the toolbar on the layout as the app bar for the activity
                    Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_buyNow);
                    setSupportActionBar(myToolbar);
                    Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
                    // ___________________________Toolbar___________________ [<-- END]
                }
            }
        });
    }
    // <==========|| Get the bill from fireStore ||========================>[END]


    public void setViewers()
    {

        uploadItArrayList= new ArrayList<>(
                Collections.nCopies( purchase.getPurchaseQuantityArray().size(), true)
        );
        // set the viewers values, and fill the purchase method
        initViewers();
        // Set action to the buttons
        button_BuyPurchase.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View V)
            {
                boolean noPurchaseMethodSelected = TextUtils.isEmpty(bill.getPurchasedMethod());
                if (purchase.getPurchaseDocumentIDArray().size()!=0)
                {
                    if (noPurchaseMethodSelected)
                    {
                        Toast.makeText(
                                getApplicationContext(),
                                "Método de compra no elegido, eliga método de compra",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                    // store the bill in fireStore
                    else
                    {
                        // set a default value for the following variables.
                        purchasedOutSockProductNameArray.clear();
                        actualFireStoreStockProductArray.clear();
                        purchasedAmountProductOutStockArray.clear();
                        stockValuesArray = new int[purchase.getPurchaseProductArray().size()];
                        uploadIt= true;
                        counter=0;

                        // Launch the progress bar and start the purchase methods
                        LaunchProgressBarAndBuy(V);
                        //createAndCheckBill();
                    }
                }
                else
                {
                    Toast.makeText(
                            getApplicationContext(),
                            bill.getPurchasedMethod(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });

        button_doNotBuyPurchase.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View V)
            {

                // if the client was in the way to buy a trolley but chose not to buy it by
                // selecting the delete button, then the client trolley on fireStore must be
                // delete it.
                if (isOnTrolley)
                {
                    // That is done with the deleteTrolley() method
                    deleteTrolley();
                }

                // I create an intent to send a value to "UserAuth_menu" activity to advise
                // that the product was not purchased
                Intent intent;
                intent = new Intent(BuyNowActivity.this, UserAuth_menu.class);
                intent.putExtra("notSuccessPurchase", "BuyNowActivity");
                startActivity(intent);
            }
        });
    }



    // <==========|| Init Viewers ||========================>[BEGIN]
    public void initViewers()
    {
        // set the price of the purchase
        /*float totalPrice = 0;
        int totalProducts = 0;
        for (int quantity :  purchase.getPurchaseQuantityArray() )
        {
            totalProducts+=quantity;
            for (Producto productPurchased : purchase.getPurchaseProductArray())
            {
                totalPrice = quantity*productPurchased.getPrecio();
            }
        }

         */
        ArrayList<String> priceArray = getTotalPriceValues();
        String totalPriceString = priceArray.get(0);
        String totalProducts = priceArray.get(1);
        textView_purchaseTotalValue.setText(totalPriceString);

        // TextView_totalItems
        String TextView_totalItems_Text  ="Total (" + totalProducts + " items)";
        textView_subtotalTrolleyField.setText(TextView_totalItems_Text);

        // Get the purchase method
        RadioGroup_purchaseOptions.clearCheck();
        RadioGroup_purchaseOptions.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener()
                {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        RadioButton radioButton =  group.findViewById(checkedId);
                        bill.setPurchasedMethod(radioButton.getText().toString());
                    }
                }
        );

        // set the product list with the function "toImageAdapter"
        toImageAdapter();
    }

    // <==========|| Init Viewers ||========================>[END]




    public ArrayList<String> getTotalPriceValues()
    {
        ArrayList<String> returned = new ArrayList<>();
        float totalPrice = 0;
        int totalProducts = 0;

        for (int flag = 0 ; flag<purchase.getPurchaseQuantityArray().size(); flag++)
        {
            totalProducts+=purchase.getPurchaseQuantityArray().get(flag);

            // if the product is in offer
            if (purchase.getPurchaseProductArray().get(flag).isOferta()){
                // totalPrice store the price for all the quantity of purchase products
                totalPrice +=
                    purchase.getPurchaseQuantityArray().get(flag) *
                    purchase.getPurchaseProductArray().get(flag).getPrecioNuevo();
            }
            // if the product is NOT in offer
            else
            {
                // totalPrice store the price for all the quantity of purchase products
                totalPrice +=
                    purchase.getPurchaseQuantityArray().get(flag) *
                    purchase.getPurchaseProductArray().get(flag).getPrecio();
            }


        }
        // give the format of two decimals for the price
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setDecimalFormatSymbols(symbols);

        returned.add("$ "+ decimalFormat.format(totalPrice));
        returned.add(String.valueOf(totalProducts));
        return returned;
    }

    /*
       public ArrayList<String> getTotalPriceValues()
    {
        ArrayList<String> returned = new ArrayList<>();
        String totalPrice = "0";
        int totalProducts = 0;
        for (int quantity :  purchase.getPurchaseQuantityArray() )
        {
            totalProducts+=quantity;
            for (Producto productPurchased : purchase.getPurchaseProductArray())
            {
                if (productPurchased.isOferta()){
                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
                    totalPrice = decimalFormat.format(
                            quantity*productPurchased.getPrecioNuevo());
                }
                else{
                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
                    totalPrice = decimalFormat.format(
                            quantity*productPurchased.getPrecio());
                }
            }
        }
        returned.add("$ "+totalPrice);
        returned.add(String.valueOf(totalProducts));
        return returned;
    }
    */

    /*
        public ArrayList<String> getTotalPriceValues(
            List<Integer> PurchaseQuantityArray,
            List<Producto> PurchaseProductArray)
    {
        ArrayList<String> returned = new ArrayList<>();
        float totalPrice = 0;
        int totalProducts = 0;
        for (int quantity :  PurchaseQuantityArray )
        {
            totalProducts+=quantity;
            for (Producto productPurchased : PurchaseProductArray)
            {
                totalPrice = quantity*productPurchased.getPrecio();
            }
        }
        returned.add("$ "+totalPrice);
        returned.add(String.valueOf(totalProducts));
        return returned;
    }
    * */






    // <==========|| toImageAdapter method creates a CaptionImagesAdapter
    // object that will initialize all the cardViews in the RecyclerView ||==========> [BEGIN]
    public void toImageAdapter()
    {
        // create an CaptionImagesAdapter object and then i'll pass the arrays with the products
        // information that i want yo show in  the RecyclerView
        // The CaptionImagesAdapter only receive as arguments arrays with the elements to be
        // displayed.
        CaptionedPurchaseAdapter Adapter = new CaptionedPurchaseAdapter(
                purchase.getPurchaseProductArray(),
                purchase.getPurchaseQuantityArray(),
                uploadItArrayList
        );

        productoRecycler.setAdapter(Adapter);
        // LinearLayoutManager is used to show the cardView in a list way
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        productoRecycler.setLayoutManager(layoutManager);

        // <==========|| Set Listeners ||==========>

        // active the interactive buttons in each product on the captionedPurchaseAdapter
        // * the listener will launch the EditActivity activity when the cardView within the
        //   recyclerView is touched
        // * "startActivity" implements the Listener onClick() method.
        //   It starts PizzaDetailActivity, passing it the product the user chose.

        Adapter.setListenerRemove(new CaptionedPurchaseAdapter.ListenerRemove()
        {
            @Override
            public void onLongClickRemove(int position)
            {
                // remove the product from all the arrays
                removeProduct(position);

            }

            @Override

            public void onClickDelete() {
                Toast.makeText(
                        getApplicationContext(),
                        "Mantenga pulsado para remover producto.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        // Increase products purchased on the cardView
        Adapter.setListenerButtonIncrease(new CaptionedPurchaseAdapter.ListenerButtonIncrease()
        {
            @Override
            public void onClickButtonIncrease(
                    TextView TextView_quantityPurchased,
                    TextView textView_availableProducts,
                    int position
            )
            {
                //------->
                int productStock = parseInt( textView_availableProducts.getText().toString());
                int purchased =
                        parseInt( TextView_quantityPurchased.getText().toString());
                // condition that stop the purchase increment if there are no more product available
                if (purchased+1 <= productStock)
                {
                    purchased+=1;
                    TextView_quantityPurchased.setText(String.valueOf(purchased));

                    // upgrade the purchase Quantity array
                    purchase.getPurchaseQuantityArray().set(position, purchased);

                    // Obtain the new values
                    ArrayList<String> priceArrayValues = getTotalPriceValues();
                    String newTotalPrice = priceArrayValues.get(0);
                    String newTotalProducts = priceArrayValues.get(1);

                    // set the new txt view values
                    textView_purchaseTotalValue.setText(newTotalPrice);
                    String newTextView_totalItems_Text  ="Total (" + newTotalProducts + " items)";
                    textView_subtotalTrolleyField.setText(newTextView_totalItems_Text);

                    // set the new bill price
                    // bill.setBillPrice(Float.parseFloat(newTotalPrice.split("$")[1]));
                }
                else
                {
                    Toast.makeText(
                            getApplicationContext(),
                            "No hay mas productos disponibles. purchased: " + purchased,
                            Toast.LENGTH_LONG
                    ).show();
                }
                //------->
            }
        });

        // Decrease products purchased on the cardView
        Adapter.setListenerButtonDecrease(new CaptionedPurchaseAdapter.ListenerButtonDecrease()
        {
            @Override
            public void onClickButtonDecrease(
                    TextView TextView_quantityPurchased,
                    int position
            )
            {
                //------->
                int purchased =
                        parseInt( TextView_quantityPurchased.getText().toString());
                // condition that stop the purchase decrement if there 0 products
                if (purchased-1 >= 0)
                {
                    purchased-=1;
                    TextView_quantityPurchased.setText(String.valueOf(purchased));

                    // upgrade the purchase Quantity array
                    purchase.getPurchaseQuantityArray().set(position, purchased);

                    // Obtain the new values
                    ArrayList<String> priceArrayValues = getTotalPriceValues();
                    String newTotalPrice = priceArrayValues.get(0);
                    String newTotalProducts = priceArrayValues.get(1);

                    // set the new txt view values
                    textView_purchaseTotalValue.setText(newTotalPrice);
                    String newTextView_totalItems_Text  ="Total (" + newTotalProducts + " items)";
                    textView_subtotalTrolleyField.setText(newTextView_totalItems_Text);

                    // set the new bill price
                    // bill.setBillPrice(Float.parseFloat(newTotalPrice.split("$")[1]));
                }
                else
                {
                    Toast.makeText(
                            getApplicationContext(),
                            "No tiene elegido productos. purchased: " + purchased,
                            Toast.LENGTH_LONG
                    ).show();
                }
                //------->
            }
        });

        // <==========|| Set Listeners ||==========>

    }
    // <==========|| toImageAdapter method creates a CaptionImagesAdapter
    // object that will initialize all the cardViews in the RecyclerView ||==========> [END]

    public void removeProduct(int position)
    {
        // remove the product from all the arrays
        purchase.getPurchaseProductArray().remove(position);
        purchase.getPurchaseQuantityArray().remove(position);
        purchase.getPurchaseDocumentIDArray().remove(position);
        uploadItArrayList.remove(position);

        // Obtain the new values of total price and items quantity
        ArrayList<String> priceArrayValues = getTotalPriceValues();
        String newTotalPrice = priceArrayValues.get(0);
        String newTotalProducts = priceArrayValues.get(1);

        // set the new values of total price and items quantity
        textView_purchaseTotalValue.setText(newTotalPrice);
        String newTextView_totalItems_Text  ="Total (" + newTotalProducts + " items)";
        textView_subtotalTrolleyField.setText(newTextView_totalItems_Text);

        // Reload the arrays
        toImageAdapter();

    }




    public void LaunchFirstProgressBar(View v)
    {

        progressBar = new ProgressDialog(v.getContext());
        progressBar.setCancelable(false);
        progressBar.setMessage("Obteniendo facturas...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();
        checkProgressTrolleyBarStatus = 0;


        new Thread(new Runnable() {
            public void run() {
                while (checkProgressTrolleyBarStatus < 100) {
                    // call the method that will be waited to finish it


                    // TODO why do i call Thread.sleep(1000), it is because this was the original
                    //  format of the code that i findon internet?
                    /*
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                     */

                    /*
                    progressBarbHandler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressBillBarStatus);
                        }
                    });

                     */
                }

                if (checkProgressTrolleyBarStatus >= 100)
                {
                    progressBar.dismiss();
                }
            }
        }).start();
    }






    public void LaunchProgressBarAndBuy(View v)
    {
        progressBar = new ProgressDialog(v.getContext());
        progressBar.setCancelable(false);
        progressBar.setMessage("Realizando compra...puede llevar tiempo");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();
        progressBarStatus = 0;

        new Thread(new Runnable() {
            public void run() {
                while (progressBarStatus < 100) {
                    // call the method that will be waited to finish it


                    // TODO why do i call Thread.sleep(1000), it is because this was the original
                    //  format of the code that i findon internet?
                    /*
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                     */

                    progressBarbHandler.post(new Runnable() {
                        public void run() {
                            progressBar.setProgress(progressBarStatus);
                        }
                    });
                }

                if (progressBarStatus >= 100) {
                    /*
                    try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                     */
                    progressBar.dismiss();
                }
            }
        }).start();
        fillBillValues();
    }

    public void fillBillValues()
    {
        // obtain the bill fireStore ID
        fireStoreBillID = db
                .collection("Usuarios").document(currentFirebaseUser.getUid())
                .collection("Compras").document();

        // Set the bill parameters
        if (Objects.requireNonNull(TextInputEditText_purchaseName.getText()).toString().length()!=0)
        {
            bill.setBillName(TextInputEditText_purchaseName.getText().toString());
        }
        else
        {
            bill.setBillName(fireStoreBillID.getId());
        }

        bill.setBillID(fireStoreBillID.getId());
        bill.setUserID(currentFirebaseUser.getUid());
        bill.setBillDate(java.util.Calendar.getInstance().getTime());
        bill.setPickedUp(false);
        String strPurchasePrice=textView_purchaseTotalValue.getText().toString().split(" ")[1];
        //String strDUTPrice = strCOMAPrice.split(",")[0]+"."+strCOMAPrice.split(",")[1];
        bill.setBillPrice(Float.parseFloat(strPurchasePrice));
        bill.setCompra(purchase);

        // upload it to fireStore
        selectLocalProductIDToCompare();
    }

    public void selectLocalProductIDToCompare()
    {
        int flag=0;
        System.out.println("getPurchaseDocumentIDArray: "+purchase.getPurchaseDocumentIDArray().size());
        System.out.println("getPurchaseDocID: "+purchase.getPurchaseDocumentIDArray().get(0));

        for(String docIDFireStoreProduct: purchase.getPurchaseDocumentIDArray())
        {
            System.out.println("docIDFireStoreProduct: "+docIDFireStoreProduct);
            getFireStoreProduct(docIDFireStoreProduct, flag);
            flag+=1;
        }
    }

    public void getFireStoreProduct(
            String docIDFireStoreProduct,
            final int flag
    )
    {
        System.out.println("docIDFireStoreProduct: "+docIDFireStoreProduct);

        DocumentReference docRef;
        docRef = db.collection("Productos").document(docIDFireStoreProduct);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
            if (task.isSuccessful())
            {
                DocumentSnapshot document = task.getResult();
                if (document.exists())
                {

                    //Producto fireStoreActualProduct = document.toObject(Producto.class);
                    purchaseIfStockAvailable(
                        Objects.requireNonNull(document.toObject(Producto.class)).getName()
                        ,Objects.requireNonNull(document.toObject(Producto.class))
                                    .getCantidad_disponible()
                        ,flag
                    );
                }
                else
                {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
            }
        });
    }

    // "flag" indicates a product index in the array, this variable synchronous the elements of
    // the; purchase product array, purchase docID array and purchase quantity array.
    public void purchaseIfStockAvailable
    (
            String productNameFireStore,
            int actualFireStoreStockProductValue,
            int flag
    )
    {
        // outCounter is an outsider counter which indicates when the stock of all the products
        // have been checked
        counter += 1;
        // fill the array with the actual stock values
        stockValuesArray[flag]= actualFireStoreStockProductValue;
        // if the actual stock of the current iterated product meets demand then it enters the
        // first filter

        System.out.println( "counter outside if: " + counter);
        /*
        System.out.println( "products Quantity: " + purchase.getPurchaseProductArray().size());
        System.out.println( "stockValuesArray["+flag+"]: " + actualFireStoreStockProductValue);
        System.out.println( "item: " + (flag+1));
        System.out.println( "Product name: " + purchase.getPurchaseProductArray().get(flag));
         */

        if (
                actualFireStoreStockProductValue >= purchase.getPurchaseQuantityArray().get(flag)
        )
        {
            uploadItArrayList.set(flag, true);

            System.out.println("almost Ready to be purchased");
            if ((counter) == purchase.getPurchaseProductArray().size() && uploadIt)
            {
                System.out.println("Ready to be purchased");

                // if there are stock for the last product of the array then ill upload the bill
                // and upgrade the stock value for each purchased product
                // upload the bill to fireStore, with the previous ID created for the bill
                uploadBillToFireStore();

                for (int i = 0; i< purchase.getPurchaseDocumentIDArray().size(); i++)
                {
                    // upload the bill to fireStore
                    //uploadBillToFireStore();

                    // upgrade value of the stock field of the fireStore product,
                    // after make the purchase.

                    /*
                    System.out.println("purchase.getPurchaseProductArray()().get(index "+i+"): "
                    +purchase.getPurchaseProductArray().get(i));

                    System.out.println( "Actual stockValuesArray[index "+i+"]: "
                    + stockValuesArray[i]);

                    System.out.println( "purchase.getPurchaseQuantityArray().get(index "+i+"): "
                     + purchase.getPurchaseQuantityArray().get(i));

                    System.out.println( "actual stock (index "+i+"): "
                    + ( stockValuesArray[i] -purchase.getPurchaseQuantityArray().get(i)) );
                     */


                    updateProductStock(
                            purchase.getPurchaseDocumentIDArray().get(i),
                            stockValuesArray[i] -
                            purchase.getPurchaseQuantityArray().get(i),
                            i
                    );
                }
            }
            System.out.println("almost Ready to be purchased");
        }
        // if the actual iterated product stock doesn't meet the demand then execute the else
        else
        {

            uploadItArrayList.set(flag, false);

            System.out.println("not ready to purchase");
            // if there are at least one product out of stock then i'll not upload the bill, and
            // the "uploadIt" will change to false. This variable helps to indicates if the bill
            // is or isn't upload it.
            uploadIt = false;
            System.out.println("after --> uploadIt = false");

            // i create three arrays and fill those with the info of the products out of stock and
            // the purchased quantity of those products.
            // The arrays values will be need it for load a RecyclerView.
            purchasedOutSockProductNameArray.add(productNameFireStore);
            actualFireStoreStockProductArray.add(actualFireStoreStockProductValue);
            purchasedAmountProductOutStockArray.add(purchase.getPurchaseQuantityArray().get(flag));

            //upgrade the actual stock value of the product
            purchase.getPurchaseProductArray().get(flag)
                    .setCantidad_disponible(actualFireStoreStockProductValue);
        }

        // The next "if" happens if there are at least one out of stock products
        if (!uploadIt && counter == purchase.getPurchaseProductArray().size())
        {
            System.out.println("ready to show an out of stock alert");


            // upload the whole progress bar, so the loading bar will be close
            progressBarStatus=100;

            // show a toast alerting that the purchase was not made cause at least one product was
            // out of stock.
            Toast.makeText(getApplicationContext(),
                    "No hay suficiente productos",
                    Toast.LENGTH_LONG).show();

            // reload the captioned purchase  adapter with upgraded products stock value
            toImageAdapter();

            // call a method that shows a fragment with the out of stock products
            unavailabilityProducts();

        }
    }

    public void uploadBillToFireStore()
    {
        // upload Bill to "Usuarios" collection, after check there are stock for all the bill products
        db.collection("Usuarios").document(currentFirebaseUser.getUid())
                .collection("Compras").document(fireStoreBillID.getId())
                .set(bill)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        // send the notification to the admin app
                        //getUserToSendNotification();

                        // i launch the next activity from here because then i'll make sure that
                        // the user data is send to the fireStore database before the activity is
                        // launched and do not stop the connection to the fireStore database

                        // almost finish the progress bar
                        progressBarStatus+=99;

                        Log.d(TAG,"bill written on Users with ID: " + fireStoreBillID);


                        // Cause i chose to buy the purchase, i'll send a Compra object wrapped in
                        // a bundle object over an intent, to the UserAuth_menu Activity.
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("purchaseObject", purchase);

                        // i initialize the intent, to be ready to send the Compra object
                        Intent intent;
                        intent = new Intent(BuyNowActivity.this, UserAuth_menu.class);
                        intent.putExtra("callerSuccessPurchaseBundle", bundle);
                        startActivityForResult(intent, 1);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

        // upload Bill to "Factura/NoRecogido" collection, after check there are stock for all
        // the bill products
        db.collection("Facturas").document("9JLKeuCuSqH9ZUzDh61X")
                .collection("NoRecogido").document(fireStoreBillID.getId())
                .set(bill)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // finish the progress bar
                        progressBarStatus+=1;

                        Log.d(TAG,"bill written on Bills with ID: " + fireStoreBillID.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

        // call method that deletes the trolley document from fireStore because it was purchased
        deleteTrolley();

    }

    public void deleteTrolley()
    {
        // delete the trolley document from fireStore because it was purchased
        db.collection("Usuarios")
                .document(currentFirebaseUser.getUid())
                .collection("Carrito")
                .document(currentFirebaseUser.getEmail()+"Trolley")
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "trolley successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting trolley document", e);
                    }
                });
    }

    public void updateProductStock(String docIDProduct,  int stockValue, int i )
    {
        DocumentReference washingtonRef;
        washingtonRef = db.collection("Productos").document(docIDProduct);

        System.out.println( " stock on updateProductStock method (index "+i+"): " + stockValue);
        // Set the "stock" field of the product of the 'docIDProduct'
        washingtonRef
                .update("cantidad_disponible", stockValue)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Product Stock successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating ProductsStock", e);
                    }
                });
    }

    public void unavailabilityProducts()
    {
        // show a little window (fragment) with the list of unavailable products

        /*
        // send data to the fragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment newFragment = UnavailableProductsFragment.newInstance("From Arguments","2nd");
        ft.add(R.id.fragment_container_BuyNow, newFragment);
        ft.commit();
         */

        getSupportFragmentManager()
            .beginTransaction()
            .add(
                R.id.fragment_container_BuyNow,
                new UnavailableProductsFragment(
                        purchasedOutSockProductNameArray,
                        actualFireStoreStockProductArray,
                        purchasedAmountProductOutStockArray))
        .commit();

        /*
        getSupportFragmentManager()
        .beginTransaction()
        .add(
            R.id.fragment_container,
            new CurrentMapsFragment(markerLatLng))
        .commit();
        * */

    }


    // [BEGIN-->] <==========|| Action Bar override methods ||==========>
    // inflate the app bar with the toolbar wanted model
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // set actions to the app bar buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_home) {
            startActivity(new Intent(this, UserAuth_menu.class));
            return true;
        }// If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        else if (item.getItemId() == R.id.action_cart)
        {
            //startActivity(new Intent(this, BuyNowActivity.class));
            return  true;
        }
        else if (item.getItemId() == R.id.action_search)
        {
            System.out.println("action_search");
            // SearchManager connects the Search View with the searchable configuration
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            // declare and initialize the activity_search view
            final SearchView searchView = (SearchView) item.getActionView();
            // a componentName allow me to save, in this case, the activity_search query in a SEARCH_SERVICE
            //      then i'll retrieve the query from Intent.ACTION_SEARCH and SearchManager.QUERY set in
            //      the SearchableActivity class
            // i bind the componentName of this activity to the SearchableActivity
            ComponentName componentName = new ComponentName(this, SearchableActivity.class);
            // i associate the searchView text with the component name
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
            // setSubmitButtonEnabled shows a interactive button to activity_search
            searchView.setSubmitButtonEnabled(true);
            searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
            return  true;
        }
        return super.onOptionsItemSelected(item);
    }
    // <==========|| Action Bar override methods ||==========> [<-- END]





    // <========================================|| FireStore Cloud Messages ||========================================> [BEGIN -->]
    private void getUserToSendNotification()
    {

        System.out.println("getUserToSendNotification method");
        db
                .collection("Usuarios")
                .document(bill.getUserID())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            System.out.println("task isSuccessful");
                            DocumentSnapshot document= task.getResult();
                            if (document.exists())
                            {
                                System.out.println("User exists");
                                // launch the notification
                                user = document.toObject(User.class);
                                //settingUpNotification();
                            }
                            else
                            {
                                System.out.println("User doesn't exists");
                                Log.d("TAG", "Error getting User: ",
                                        task.getException());
                            }
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("User document on fail");
                Log.w("onFailure", "Error getting user document", e);
            }
        });
    }


    private String getBillDate(String date)
    {
        String[] dateTimeArray = date.split(" ");

        String month = dateTimeArray[1];
        String day = dateTimeArray[2];
        String time = dateTimeArray[3];
        String year = dateTimeArray[dateTimeArray.length-1];

        switch(month.toLowerCase())
        {
            case "jan":
                month= "Enero";
                break;
            case "apr":
                month = "Abr";
                break;
            case "feb":
                month = "Febrero";
                break;
            default:
                break;
        }

        return  month + " " +day + ", " +year + " - " +time;
    }



    //______________________________________________________ TODO --------------------------

    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAAJ0SQU0:APA91bEtjtq8mutolkQcIGOe7mKKuKXbYp8RXVBZbriWIRpTiRaUELT14JA1E6pjE2uHjuGeBLpTXBJw48F2w70U5zvyBFworVQsZ-4mXM4YrNoBzXcDNtQmZ8FweaElKFIbGKICqJE-";
    final private String contentType = "application/json";
    final String TAG1 = "NOTIFICATION TAG";

    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;

    private void settingUpNotification()
    {
        TOPIC = "/topics/userABC"; //topic must match with what the receiver subscribed to
        NOTIFICATION_TITLE = "Hay una nueva compra";
        NOTIFICATION_MESSAGE = "Nueva compra de: " +
                                user.getFirstName() +
                                ". " + getBillDate(String.valueOf(bill.getBillDate()));

        JSONObject notification = new JSONObject();
        JSONObject notifcationBody = new JSONObject();
        try {
            System.out.println("settingUpNotification method");

            notifcationBody.put("title", NOTIFICATION_TITLE);
            notifcationBody.put("message", NOTIFICATION_MESSAGE);

            notification.put("to", TOPIC);
            notification.put("data", notifcationBody);
        } catch (JSONException e) {
            Log.e(TAG1, "onCreate: " + e.getMessage() );
        }
        sendNotification(notification);
    }



    private void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG1, "onResponse: " + response.toString());
                        Toast.makeText(BuyNowActivity.this, "done notification", Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(BuyNowActivity.this, "Request error", Toast.LENGTH_LONG).show();
                        Log.i(TAG1, "onErrorResponse: Didn't work");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    // <========================================|| FireStore Cloud Messages ||========================================> [<---- END]


}