package sdcn.project.ecommerce_client_distributed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import sdcn.project.ecommerce_client_distributed.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.lang.Integer.parseInt;

public class selectedProduct extends AppCompatActivity {

    // We’ll use this constant to pass the ID of the pizza as extra information in the intent.
    public static final String EXTRA_PRODUCT_ATTRIBUTES = "productID";

    // [____________|| Variables ||____________] [<--BEGIN]
    private String documentID;
    private Producto product;

    // [____________|| Views ||____________] [<--BEGIN]
    private Button btn_buyNow, button_addToCart;
    private TextView
            textView_productName,
            textView_actualPrice,
            textView_previousPrice,
            textView_offer,
            textView_productUnitValue,
            textView_ProductStockValue;
    private EditText editText_purchasedAmount;
    private ImageButton imageButton_increaseProduct, imageButton_decreaseProduct;
    private ImageView imageView_product_buyProduct;

    // [____________|| FireStore ||____________] [<--BEGIN]
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;

    // [____________|| Purchase arguments ||____________] [<--BEGIN]
    List<String> arrayIDs;
    List<Producto> arrayProducts;
    List<Integer> arrayQuantityProduct;
    // Query first;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_product);

        // init the Views
        btn_buyNow = (Button) findViewById(R.id.button_buyNow);
        button_addToCart = findViewById(R.id.button_addToCart);

        imageButton_increaseProduct = findViewById(R.id.imageButton_increaseProduct);
        imageButton_decreaseProduct= findViewById(R.id.imageButton_decreaseProduct);

        editText_purchasedAmount = findViewById(R.id.editText_purchasedAmount);
        textView_productName= findViewById(R.id.textView_productName);
        textView_actualPrice= findViewById(R.id.textView_actualPrice);
        textView_previousPrice= findViewById(R.id.textView_previousPrice);
        textView_offer= findViewById(R.id.textView_offer);
        textView_productUnitValue = findViewById(R.id.textView_productUnitValue);
        textView_ProductStockValue = findViewById(R.id.textView_ProductStockValue);
        imageView_product_buyProduct = findViewById(R.id.imageView_product_buyProduct);


        // [BEGIN -->] _______________Toolbar_____________________________
        // set the toolbar on the layout as the app bar for the activity
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_selectedProduct);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        // ___________________________Toolbar___________________ [<-- END]


        // unable some views to make sure no interaction while the product is not downloaded from
        // fireStore

        btn_buyNow.setEnabled(false);
        button_addToCart.setEnabled(false);
        editText_purchasedAmount.setEnabled(false);


        // If get the product from the "previous" activity then ill store it in a Bundle var
        Bundle bundleProduct = getIntent().getBundleExtra("intentGetProduct");

        if (bundleProduct!=null)
        {
            // change visibility to VISIBLE cause there is a products to purchase

            if (!bundleProduct.isEmpty())
            {
                product = bundleProduct.getParcelable("bundleProduct");
                documentID = bundleProduct.getString("bundleStrDocID");
                setViewsValues();
            }
            else
            {
                Toast.makeText(
                        this, "Error al mostrar el producto", Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Toast.makeText(
                    this, "Error al cargar el producto", Toast.LENGTH_LONG).show();
        }


    }

    public void createProduct(String documentID)
    {
        DocumentReference docRef;
        docRef = db.collection("Productos")
                .document(documentID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task){

                if(task.isSuccessful()){
                    // i obtain the product
                    product =task.getResult().toObject(Producto.class);
                    setViewsValues();
                }
                else
                {
                    Log.w(
                            "constructProduct(): ",
                            "Error getting documents.",
                            task.getException()
                    );
                }
            }
        });
    }

    public void setViewsValues()
    {
        btn_buyNow.setEnabled(true);
        button_addToCart.setEnabled(true);
        editText_purchasedAmount.setEnabled(true);

        textView_productName.setText(product.getName());
        // [____________|| Check if there is an offer ||____________] [<--BEGIN]
        if (product.isOferta())
        {
            String actualPrice = "$ "+ product.getPrecio();
            textView_actualPrice.setText(actualPrice);

            String previousPrice = "$ "+ product.getPrecioNuevo();
            textView_previousPrice.
                    setPaintFlags(
                            textView_previousPrice.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG
                    );
            textView_previousPrice.setText(previousPrice);

            float productOffPriceFloat =
                    ((product.getPrecio() - product.getPrecioNuevo()) / product.getPrecio() )*100;
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            String productOffPriceStr = decimalFormat.format(productOffPriceFloat) + "% descuento";
            textView_offer.setText(productOffPriceStr);
        }
        else
        {
            String actualPrice =  "$ "+product.getPrecio();
            textView_actualPrice.setText(actualPrice);
            textView_previousPrice.setVisibility(View.INVISIBLE);
            textView_offer.setVisibility(View.INVISIBLE);
        }
        // [____________|| Check if there is an offer ||____________] [<--END]

        textView_productUnitValue.setText(product.getUnidad());
        textView_ProductStockValue.setText(String.valueOf(product.getCantidad_disponible()));

        // [____________|| set the product image ||____________] [<--BEGIN]
        Glide.with(this)
                .load(product.getUrl().get(0))
                .into(imageView_product_buyProduct);
        // [_____________|| set the product image ||_____________] [<--END]

        // [____________|| Set actions if there are not available products ||___________] [<--BEGIN]
        if (product.getCantidad_disponible()==0)
        {
            btn_buyNow.setEnabled(false);
            button_addToCart.setEnabled(false);
            editText_purchasedAmount.setEnabled(false);

            textView_ProductStockValue.setText(R.string.unavailableTag);
            textView_ProductStockValue.setTextColor(Color.parseColor("#981C1C"));

            Toast.makeText(
                    getApplicationContext(),
                    "No hay productos disponibles.",
                    Toast.LENGTH_LONG
            ).show();
        }
        // [____________|| Set actions if there are not available products ||_____________] [<--END]


        // [___|| set a purchased Amount of products with the textEditor ||___] [<--BEGIN]
        editText_purchasedAmount.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(""))
                    if (
                            Integer.parseInt(s.toString()) >
                            product.getCantidad_disponible()
                    ) {
                        editText_purchasedAmount.setText("0");
                        Toast.makeText(
                                getApplicationContext(),
                                "Cantidad no diponible, no hay mas productos disponibles.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
            }
        });
        // [___|| set a purchased Amount of products with the textEditor ||___] [<--END]


        // [___________|| set a purchased Amount of products using buttons ||_________] [<--BEGIN]
        // increase the amount of purchased products
        imageButton_increaseProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                if (!editText_purchasedAmount.getText().toString().equals("")) {
                    int productStock = product.getCantidad_disponible();
                    //parseInt( textView_ProductStockValue.getText().toString());
                    int purchased =
                            parseInt(editText_purchasedAmount.getText().toString());
                    // condition that stop the purchase increment if there are no more product available
                    if (purchased + 1 <= productStock) {
                        purchased += 1;
                        editText_purchasedAmount.setText(String.valueOf(purchased));
                    } else {
                        Toast.makeText(
                                getApplicationContext(),
                                "No hay mas productos disponibles.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
                else{
                    Toast.makeText(
                            getApplicationContext(),
                            "Coloque una cantidad.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
        // increase the amount of purchased products
        imageButton_decreaseProduct.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View V)
            {
                if (!editText_purchasedAmount.getText().toString().equals(""))
                {
                    int purchased =
                            parseInt(editText_purchasedAmount.getText().toString());
                    // condition that stop the purchase decrement if there 0 products
                    if (purchased - 1 >= 0)
                    {
                        purchased -= 1;
                        editText_purchasedAmount.setText(String.valueOf(purchased));
                    }
                    else
                    {
                        Toast.makeText(
                                getApplicationContext(),
                                "No tiene elegido productos.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
                else{
                    Toast.makeText(
                            getApplicationContext(),
                            "Coloque una cantidad.",
                            Toast.LENGTH_LONG
                    ).show();
                }

            }
        });
        // [___________|| set a purchased Amount of products ||____________] [<--END]


        // [___________|| purchased button actions ||____________] [<--BEGIN]
        btn_buyNow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                if(!editText_purchasedAmount.getText().toString().equals(""))
                {
                    /*
                    int productAmount = parseInt(editText_purchasedAmount.getText().toString());
                    int productsPurchased =
                            parseInt(editText_purchasedAmount.getText().toString());
                    if (
                            productAmount != 0 &&
                            productsPurchased != 0 &&
                            productsPurchased <=
                            productAmount)
                     */
                    if (parseInt(editText_purchasedAmount.getText().toString()) != 0)
                    {
                            /*
                            Intent intent = new Intent(
                                    selectedProduct.this,
                                    BuyNowActivity.class
                            );

                            intent.putExtra(BuyNowActivity.EXTRA_PRODUCT_ATTRIBUTES, purchase);
                            selectedProduct.this.startActivity(intent);
                            */

                        // Cause i'm buying just one product, ill send a Compra object wrapped in
                        // a bundle object over an intent, to the BuyNowActivity.
                        Bundle b = new Bundle();
                        b.putParcelable("options", createPurchase());

                        // i initialize the intent, to be ready to send the Compra object
                        Intent intent;
                        intent = new Intent(selectedProduct.this, BuyNowActivity.class);
                        intent.putExtra("bundle", b);
                        startActivityForResult(intent, 1);
                    } else {
                        Toast.makeText(
                                getApplicationContext(),
                                "No hay productos que comprar.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
                else{
                    Toast.makeText(
                            getApplicationContext(),
                            "Cantidad a comprar no colocada.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });

        button_addToCart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!editText_purchasedAmount.getText().toString().equals(""))
                {
                    /*
                    float productAmountTrolley =
                            parseInt( textView_ProductStockValue.getText().toString());
                    float productsPurchased =
                            Float.parseFloat( editText_purchasedAmount.getText().toString());
                    if
                    (
                            productAmountTrolley!=0 &&
                            productsPurchased!=0 &&
                            productsPurchased <= productAmountTrolley
                    )
                     */
                    if (parseInt(editText_purchasedAmount.getText().toString()) != 0)
                    {
                        // call the method checkTrolleyExistence to check if a trolley exists
                        checkTrolleyExistence();
                    }
                    else
                    {
                        Toast.makeText(
                                getApplicationContext(),
                                "No hay productos para agregar al carrito.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
                else{
                    Toast.makeText(
                            getApplicationContext(),
                            "Cantidad a comprar no colocada.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
        // [___________|| purchased button actions ||____________] [<--END]

        // [___________|| call external functions for purchase the product ||__________] [<--BEGIN]

        // [____________|| call external functions for purchase the product ||___________] [<--END]
    }

    public Compra createPurchase()
    {
        // initialize the arraysList that will be arguments for a Compra object
        arrayIDs = new ArrayList<>();
        arrayProducts = new ArrayList<>();
        arrayQuantityProduct = new ArrayList<>();

        // add elements to the arraysList
        arrayIDs.add(documentID);
        arrayProducts.add(product);
        arrayQuantityProduct.add(Integer.valueOf(editText_purchasedAmount.getText().toString()));

        // create the Compra object
        return new Compra(
                arrayIDs,
                arrayProducts,
                arrayQuantityProduct
        );
    }

    public void checkTrolleyExistence()
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
                            addProductToFireStoreCart(
                                    Objects.requireNonNull(
                                            documentSnapshot.toObject(Bill.class)).getCompra()
                            );
                        }
                        // if a Factura doesn't exists then create one and upload it
                        else
                        {
                            createUploadTrolley();
                        }
                    }
        });
    }

    public void addProductToFireStoreCart(Compra purchase)
    {
        if (!purchase.getPurchaseDocumentIDArray().contains(documentID))
        {
            // Update the arrays from the purchase object previously retrieved from fireStore.
            // Those array contains the products in the cart, the for now the bill has no other
            // meaningful data.
            purchase.getPurchaseDocumentIDArray().add(documentID);
            purchase.getPurchaseProductArray().add(product);
            purchase.getPurchaseQuantityArray().
                    add(Integer.valueOf(editText_purchasedAmount.getText().toString()));

            Log.d("Purchase: ", purchase.toString());

            // Get a reference of the fireStore document where the bill is store
            DocumentReference updateFireStoreBill = db.collection("Usuarios")
                    .document(currentFirebaseUser.getUid())
                    .collection("Carrito")
                    .document(currentFirebaseUser.getEmail()+"Trolley");

            // upload the "compra" field with the updated purchase object with updated arrays
            updateFireStoreBill
                    .update("compra", purchase)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(
                                    getApplicationContext()
                                    , product.getName() +" añadido al carrito"
                                    , Toast.LENGTH_LONG)
                                    .show();

                            Log.d("Carrito: ", "compra successfully updated!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(
                                    getApplicationContext()
                                    , "Error, producto no añadido al carrito"
                                    , Toast.LENGTH_LONG)
                                    .show();

                            Log.w("Carrito", "error updating bill", e);
                        }
                    });
        }
        else
        {
            Toast.makeText(
                    getApplicationContext()
                    , "Prodcuto ya agregado al carrito"
                    , Toast.LENGTH_LONG)
                    .show();
        }

    }

    public void createUploadTrolley ()
    {
        Bill bill = new Bill(
                ""
                , ""
                , ""
                , ""
                , null
                , false
                , 0
                , createPurchase()
                );

        DocumentReference addFireStoreBill = db.collection("Usuarios")
                .document(currentFirebaseUser.getUid())
                .collection("Carrito")
                .document(currentFirebaseUser.getEmail()+"Trolley");

        addFireStoreBill.set(bill)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(
                                getApplicationContext()
                                , "Producto añadido al carrito"
                                , Toast.LENGTH_LONG)
                                .show();
                        Log.d("Carrito", "Bill successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(
                                getApplicationContext()
                                , "Error, producto no añadido al carrito"
                                , Toast.LENGTH_LONG)
                                .show();

                        Log.w("Bill", "Error writing bill", e);
                    }
                });
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
            startActivity(new Intent(this, BuyNowActivity.class));
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

}