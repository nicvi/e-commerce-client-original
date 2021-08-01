package sdcn.project.ecommerce_client_distributed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import sdcn.project.ecommerce_client_distributed.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

// TODO Note: You should not use the com.android.support and com.google.android.material dependencies in your app at the same time.

// TODO  Ensure you are using AppCompatActivity


// TODO, falta:
    // TODO * expand the activity_search widget to a size that allows another item to stay there
    // TODO * remove the app name from the app bar
    // TODO * put the app icons in the right side of tha app bar
    // TODO * [THIS] implement the activity_search action, use the searchable activity


public class UserAuth_menu extends AppCompatActivity
{

    // <=================|| Firebase User basic ||=================> BEGIN
    private FirebaseUser currentFirebaseUser;

    // [____________|| Firestore ||____________] [<--BEGIN]
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // <=================|| Views ||=================> BEGIN
    private TextView txtUsr;
    private Button signout;
    private ImageButton IMBTN_productos, IMBTN_carrito, IMBTN_BILL, IMBTN_SETTINGS;
    private ImageView imageUser;

    // <=================|| Objects ||=================> BEGIN
    private Compra purchase;
    private User userObject;

    // <=================|| Variables ||=================> BEGIN
    private static final String TAG = "on UserAuth_Menu";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_auth_menu);

        // [BEGIN -->] _______________Toolbar_____________________________
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_userAuthMenu);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        // ___________________________Toolbar___________________ [<-- END]





        // <=================|| BOTON productos ||=================> BEGIN
        IMBTN_productos = (ImageButton) findViewById(R.id.btn_productos);
        IMBTN_productos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ChooserCategoryActivity
                // activity_productos
                startActivity(
                        new Intent(
                            UserAuth_menu.this,
                                ChooserCategoryActivity.class
                        )
                );
            }
        });

        // TODO CAMBIA SearchableActivity -> activity_carrito.class
        // <=================|| BOTON carrito ||=================> BEGIN
        IMBTN_carrito = (ImageButton) findViewById(R.id.btn_carrito);
        IMBTN_carrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserAuth_menu.this, BuyNowActivity.class));
            }
        });
        // <=================|| BOTON factura ||=================> BEGIN
        IMBTN_BILL = (ImageButton) findViewById(R.id.btn_bill);
        IMBTN_BILL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserAuth_menu.this,activity_factura.class));
            }
        });
        // <=================|| BOTON settings ||=================> BEGIN
        IMBTN_SETTINGS = (ImageButton) findViewById(R.id.btn_settings);
        IMBTN_SETTINGS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO -> TestActivity - activity_settings
                startActivity(new Intent(UserAuth_menu.this, activity_settings.class));
            }
        });

        // <=================|| TextView User Name ||=================> BEGIN
        txtUsr = (TextView) findViewById(R.id.txt_usr);
        // set as default the client name as "Cliente"
        txtUsr.setText("Cliente");

        currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentFirebaseUser != null) {
            getFireStoreUsrObject();
            //txtUsr.setText(currentFirebaseUser.getEmail());
        } else {
            txtUsr.setText("Cliente");
        }
        // <=================|| IMAGEN ||=================> BEGIN

        imageUser = (ImageView) findViewById(R.id.imageView_usr);
        imageUser.setImageResource(R.drawable.man_avatar);

        // <=================|| BOTON Cerrar sesion ||=================> BEGIN
        signout = (Button) findViewById(R.id.signout_btn);
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(UserAuth_menu.this, MainActivity.class));
            }
        });

        // call the method receiveDataFromPreviousActivity to know if i would launch an fragment
        // with a message indicating if the purchase was or it wasn't successful
        receiveDataFromPreviousActivity();

    }

    public void getFireStoreUsrObject()
    {
        DocumentReference docRef;
        docRef = db.collection("Usuarios").document(currentFirebaseUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if (task.isSuccessful())
                {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists())
                    {
                        userObject = document.toObject(User.class);
                        txtUsr.setText(userObject.getFirstName());
                    }
                    else
                    {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    txtUsr.setText("Cliente");
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    public void receiveDataFromPreviousActivity()
    {
        // <=================|| CALLER from buyNowActivity ||=================> BEGIN
        // if the expected CALLER (in this case a string value from the BuyNowActivity activity)
        // is equal to "BuyNowActivity", then a not successful message in a fragment will appear

        String caller = getIntent().getStringExtra("notSuccessPurchase");
        if (  caller!=null  && caller.equals("BuyNowActivity"))
        {
            launchSuccessPurchaseMessage();
        }
        // if by other hand the purchase was successfully then i'll maybe receive a bundle with a
        // Compra object wrapped in it.
        else
        {
            // getting the purchase object from the previous activity, from the BuyNowActivity.
            Bundle bundleSuccessReceived =
                    getIntent().getBundleExtra("callerSuccessPurchaseBundle");

            System.out.println("bundle received from userAuth: " + bundleSuccessReceived);

            if (bundleSuccessReceived!=null)
            {
                // So if b.isEmpty() == True, then no object was sent via a bundle
                if (!bundleSuccessReceived.isEmpty()) {
                    purchase = bundleSuccessReceived.getParcelable("purchaseObject");

                    // call a method that will launch the fragment
                    launchSuccessPurchaseMessage();
                }
            }
        }
    }







    // [BEGIN-->] <==========|| Action Bar override methods ||==========>

    // This method:
    // * Bind the corresponding menu to the activity's toolbar of the xml file from menu folder.
    // * Activate the activity_search in the searchable view
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the options menu from XML
        getMenuInflater().inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);

    }

    // set actions to the app bar buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_home)
        {
            return true;
        }
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
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }
    // <==========|| Action Bar override methods ||==========> [<-- END]

    public void launchSuccessPurchaseMessage()
    {
        getSupportFragmentManager()
            .beginTransaction()
            .add(
                    R.id.fragment_container_UserMenu,
                    new successfulPurchaseFragment(
                        purchase.getPurchaseProductArray(), purchase.getPurchaseQuantityArray()))
            .commit();
    }


    @Override
    public void onBackPressed() {
        // doing nothing here, not calling the super, will disable the back button action.
    }

}