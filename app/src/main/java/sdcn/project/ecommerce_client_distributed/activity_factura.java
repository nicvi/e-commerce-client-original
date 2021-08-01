package sdcn.project.ecommerce_client_distributed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import sdcn.project.ecommerce_client_distributed.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class activity_factura extends AppCompatActivity
{

    private PagingDataAdapter BillPagingDataAdapter;
    private RecyclerView recyclerView;
    private TextView textView_noBillsMessage;
    private LinearLayout linearLayout_onFacturaActivity;

    // [____________|| Firestore ||____________] [<--BEGIN]
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    // [____________|| Variables ||____________] [<--BEGIN]
    long totalProducts;

    // [____________|| Progress Bar ||____________] [<--BEGIN]
    private ProgressDialog progressBar;
    private int progressBillBarStatus = 0;

    private Handler progressBarbHandler = new Handler();
    private boolean shouldAllowBack= true;
    // [____________|| Progress Bar ||____________] [<--BEGIN]

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factura);

        // initialize the recyclerView
        linearLayout_onFacturaActivity = findViewById(R.id.linearLayout_onFacturaActivity);
        recyclerView = findViewById(R.id.RecyclerView_facturas);
        recyclerView.setVisibility(View.GONE);
        textView_noBillsMessage=findViewById(R.id.textView_noBillsMessage);

        // [BEGIN -->] _______________Toolbar_____________________________
        // set the toolbar on the layout as the app bar for the activity
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_factura);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        // ___________________________Toolbar___________________ [<-- END]

        LaunchProgressBarAndBuy(textView_noBillsMessage);


        // get numbers
        getProductsNumber();

        receivedDataFromPreviousActivity();

    }



    public void getProductsNumber()
    {
        CollectionReference docRef = db
                .collection("Usuarios")
                .document(currentFirebaseUser.getUid())
                .collection("Compras");
        // i get the number of products
        docRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task)
            {
                if (task.isSuccessful())
                {
                    if (!task.getResult().isEmpty())
                    {
                        System.out.println("Bill collection is NOT empty");
                        textView_noBillsMessage.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        // Do something
                        //LaunchProgressBarAndBuy(textView_noBillsMessage);
                        launchRecyclerView ();
                    }
                    else
                    {
                        System.out.println("Bill collection IS empty");

                        recyclerView.setVisibility(View.GONE);
                        textView_noBillsMessage.setVisibility(View.VISIBLE);

                        Log.d("TAG", "Error getting documents: ", task.getException());

                        progressBillBarStatus = 100;
                    }
                }
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Bill collection IS empty on fail");
                Log.w("onFailure", "Error deleting document", e);
            }
        });;
    }



    public void launchRecyclerView ()
    {
        // Create new MoviesAdapter object and provide
        CaptionedBillAdapter billsAdapter = new CaptionedBillAdapter(new BillComparator());


        // Create ViewModel
        BillViewModel billViewModel=new ViewModelProvider(this).get(BillViewModel.class);


        // set the adapter to the recyclerView
        recyclerView.setAdapter(billsAdapter);


        // Subscribe to to paging data
        billViewModel.pagingDataFlow.subscribe(billPagingData -> {
            // submit new data to recyclerview adapter
            billsAdapter.submitData(getLifecycle(), billPagingData);
        });


        /*
        // set adapter
        recyclerView.setAdapter(
                // concat movies adapter with header and footer loading view
                // This will show end user a progress bar while pages are being requested from server
                billsAdapter.withLoadStateFooter(
                        // Pass footer load state adapter.
                        // When we will scroll down and next page request will be sent
                        // while we get response form server Progress bar will show to end user
                        // If request success Progress bar will hide and next page of movies
                        // will be shown to end user or if request will fail error message and
                        // retry button will be shown to resend the request
                        new BillsLoadStateAdapter(v -> {
                            billsAdapter.retry();
                        })));
         */
        // LinearLayoutManager is used to show the cardview in a list way

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        progressBillBarStatus = 100;
        
        // set adapter listeners
        billsAdapter.setListenerAccessBill(new CaptionedBillAdapter.ListenerAccessBill() {
            @Override
            public void onClickAccessBill(Bill currentBill) {
                // TODO pass the bill object the next activity
                // Cause i'm deleting a product, i'll send a Bill object wrapped in
                // a bundle object over an intent, to the BuyNowActivity.
                Bundle bundleBill = new Bundle();
                bundleBill.putParcelable("optionsBill", currentBill);
                // i initialize the intent, to be ready to send the Compra object
                Intent intent;
                intent = new Intent(activity_factura.this, BillSelected.class);
                intent.putExtra("bundleBill", bundleBill);
                startActivityForResult(intent, 1);
            }
        });

        billsAdapter.setListenerDeleteBill(new CaptionedBillAdapter.ListenerDeleteBill() {
            @Override
            public void onLongClickRemove(String billId) {
                // call delete function
                deleteBill(billId);

                //
                progressBillBarStatus = 0;
                LaunchProgressBarAndBuy(textView_noBillsMessage);
                getProductsNumber();

            }

            @Override
            public void onClickDelete() {
                // show toast message
                Toast.makeText(
                        getApplicationContext(),
                        "Mantenga pulsado el botón para eliminar factura." ,
                        Toast.LENGTH_LONG)
                        .show();
            }
        });


    }






    public void LaunchProgressBarAndBuy(View v)
    {
        progressBar = new ProgressDialog(v.getContext());
        progressBar.setCancelable(false);
        progressBar.setMessage("Obteniendo facturas...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();
        progressBillBarStatus = 0;

        new Thread(new Runnable() {
            public void run() {
                while (progressBillBarStatus < 100) {
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

                if (progressBillBarStatus >= 100) {

                    try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                    
                    progressBar.dismiss();
                }
            }
        }).start();
    }



    public void deleteBill(String billId)
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();;

        db.collection("Usuarios")
                .document(this.currentFirebaseUser.getUid())
                .collection("Compras").document(billId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Se elimino factura",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("onFailure", "Error deleting document", e);
                    }
                });
    }


    public void receivedDataFromPreviousActivity()
    {
        String caller = getIntent().getStringExtra("billDeleted");
        if (  caller!=null  && caller.equals("BillSelected"))
        {
            shouldAllowBack= false;
        }
    }


    @Override
    public void onBackPressed()
    {
        if (shouldAllowBack)
        {
            super.onBackPressed();
        }
        else
        {
            shouldAllowBack=true;
            Intent intent = new Intent(this,UserAuth_menu.class);
            startActivity(intent);
        }
    }




    // [BEGIN-->] <==========|| Action Bar override methods ||==========>
    // inflate the app bar with the toolbar wanted model
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // R.menu.menu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
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
}