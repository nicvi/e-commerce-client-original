package sdcn.project.ecommerce_client_distributed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import sdcn.project.ecommerce_client_distributed.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

// TODO * the activity_search widget works with the keyboard but not touching the activity_search icon [DONE]


public class SearchableActivity extends AppCompatActivity {


    // [____________|| Firestore ||____________] [<--BEGIN]
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // [____________|| Firestore ||____________] [END-->]


    // [____________|| Viewers. ||____________] [<--BEGIN]
    private Button btnBack_query, btnNext_query;
    // i make a view RecyclerView  and bind it with the xml file that contains the recyclerView.
    private RecyclerView recyclerView_query;
    private TextView textView_noQueryResults, textView_query;
    // [____________|| Viewers. ||____________] [END-->]


    // [____________|| Variables ||____________] [BEGIN-->]
    private String strQuery; // bundle
    String noQueryMsn = "No hay resultados para: ";
    // [____________|| Variables ||____________] [<--END]


    // [____________|| progress Bar ||____________] [<--BEGIN]
    private ProgressDialog progressBarQuery;
    private int progressProductsBarStatus = 0;
    // [____________|| progress Bar ||____________] [END-->]


    // [____________|| Pagination ||____________] [<--BEGIN]
    // TODO private int totalPages = Paginator.TOTAL_NUM_ITEMS / Paginator.ITEMS_PER_PAGE;
    private final int productsPerPage = 6;
    private long totalProducts;
    private DocumentSnapshot lastVisibleNext, lastVisiblePrevious;
    private int currentPage = 0;
    // [____________|| Pagination ||____________] [END-->]


    // [____________|| Product Object Variables (captionedImageAdapter) ||____________] [<--BEGIN]
    private Producto[] productArray;
    private String[] documentID;
    // [____________|| Product Object Variables (captionedImageAdapter) ||____________] [END-->]


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        // [BEGIN -->] _______________ VIEWS _____________________________ [BEGIN -->]
        btnBack_query = findViewById(R.id.btnBack_query);
        btnNext_query = findViewById(R.id.btnNext_query);
        textView_noQueryResults = (TextView) findViewById(R.id.textView_noQueryResults);
        recyclerView_query = findViewById(R.id.recyclerView_query);
        textView_query =  findViewById(R.id.textView_query);


        textView_noQueryResults.setVisibility(View.GONE);
        btnNext_query.setEnabled(false);
        btnBack_query.setEnabled(false);
        // [<-- END] ___________________________ VIEWS ___________________ [<-- END]


        // [BEGIN -->] _______________Toolbar_____________________________
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_searchable);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        // ___________________________Toolbar___________________ [<-- END]


        // get the query from the searchView of the previous activity
        handleIntent(getIntent());

        // buttons actions
        setViewsActions();

    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent)
    {
        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            strQuery = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(strQuery);// the query is retrieved and passed to a local doMySearch() method where the actual activity_search operation is done.
        }
    }


    // doMySearch method do the activity_search and find the match of the items searched
    public void doMySearch(String strQuery)
    {
        LaunchProgressQueryBar(recyclerView_query);
        getTotalProducts();

    }


    private void setViewsActions()
    {
        //getTotalProducts();
        // buttons actions
        btnNext_query.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                btnNext_query.setEnabled(false);
                paginateNextQuery();
                // launch the progress bar until the next page is loaded
                LaunchProgressQueryBar(recyclerView_query);
            }
        });

        btnBack_query.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                btnBack_query.setEnabled(false);
                paginatePreviousQuery();
                // launch the progress bar until the next page is loaded
                LaunchProgressQueryBar(recyclerView_query);
            }
        });
    }



    private void getTotalProducts()
    {
        // i get the number of products
        db
                .collection("Productos")
                .whereArrayContains("caseSearch", strQuery)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots)
                    {
                        // if query is not empty
                        if (!documentSnapshots.isEmpty())
                        {
                            totalProducts = documentSnapshots.size();
                            //System.out.println("total products on category: "+ documentSnapshots.size());

                            paginateFirstQuery();
                        }
                        // no products of this category available
                        else
                        {
                            // hide the recyclerView and show the no products textView
                            setViewsVisibility(false);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        // hide the recyclerView and show the no products textView
                        setViewsVisibility(false);
                        Log.w("onFailure", "accessing next document", e);
                    }
                });
    }



    public void paginateFirstQuery()
    {
        currentPage = 0;

        System.out.println("paginateFirstQuery");
        // Construct query for first 25 cities, ordered by population

        db.collection("Productos")
                .whereArrayContains("caseSearch", strQuery)
                .orderBy("name") // to implement a second condition i have to create compound
                // query on the server side
                .limit(productsPerPage)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots)
                    {
                        // paging first page
                        paging(documentSnapshots.size());

                        // Construct a new query starting at this document,
                        // get the next 6 products.
                        lastVisibleNext = documentSnapshots.getDocuments()
                                .get(documentSnapshots.size() -1);
                        // set values for the last document read
                        lastVisiblePrevious = documentSnapshots.getDocuments()
                                .get(0);

                        // call a method for get the Products from fireStore
                        fillArrayProducts(documentSnapshots, documentSnapshots.size());

                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        // hide the recyclerView and show the no products textView
                        setViewsVisibility(false);
                        Log.w("onFailure", "accessing next document", e);
                    }
                });;
    }


    public void paginateNextQuery()
    {
        db.collection("Productos")
                .whereArrayContains("caseSearch", strQuery)
                .orderBy("name")
                .startAfter(lastVisibleNext)
                .limit(productsPerPage)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots)
                    {
                        // enable btn next, paging next pages
                        currentPage+=1;
                        paging(documentSnapshots.size());
                        // update the lastVisible document
                        lastVisibleNext = documentSnapshots.getDocuments()
                                .get(documentSnapshots.size() -1);
                        lastVisiblePrevious = documentSnapshots.getDocuments()
                                .get(0);

                        // call a method for get the Products from fireStore
                        fillArrayProducts(documentSnapshots, documentSnapshots.size());
                        final long endTime = System.currentTimeMillis();

                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        // hide the recyclerView and show the no products textView
                        setViewsVisibility(false);
                        Log.w("onFailure", "accessing next document", e);
                    }
                });
    }

    private void paginatePreviousQuery()
    {

        db.collection("Productos")
                .whereArrayContains("caseSearch", strQuery)
                .orderBy("name")
                .endBefore(lastVisiblePrevious)
                .limitToLast(productsPerPage)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots)
                    {
                        // enable btn next, paging next pages
                        currentPage-=1;
                        paging(documentSnapshots.size());
                        // update the lastVisible document
                        lastVisibleNext = documentSnapshots.getDocuments()
                                .get(documentSnapshots.size() -1);
                        lastVisiblePrevious = documentSnapshots.getDocuments()
                                .get(0);

                        // call a method for get the Products from fireStore
                        fillArrayProducts(documentSnapshots, documentSnapshots.size());

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        // hide the recyclerView and show the no products textView
                        setViewsVisibility(false);
                        Log.w("onFailure", "accessing next document", e);
                    }
                });

    }

    public void paging (int documentSnapshotsSize)
    {
        // enable btn next
        boolean fullPage = documentSnapshotsSize<productsPerPage;
        boolean firstPage = currentPage == 0;

        int value = 0;
        if (fullPage) value|= 0x01;
        if (firstPage) value|= 0x02;

        switch (value)
        {
            case 0:
                // fullPage false and firstPage false
                System.out.println("case 0 (a)");
                if (totalProducts>documentSnapshotsSize*(currentPage+1))
                {
                    System.out.println("case 0 (a), n page");
                    btnNext_query.setEnabled(true);
                    btnBack_query.setEnabled(true);
                }
                else{
                    System.out.println("case 0 (a), n page y last page");
                    btnNext_query.setEnabled(false);
                    btnBack_query.setEnabled(true);
                }
                break;
            case 1:
                // fullPage true ^ firstPage false
                System.out.println("case 1 (c), last page");
                btnNext_query.setEnabled(false);
                btnBack_query.setEnabled(true);
                break;
            case 2:
                // fullPage false ^ firstPage true
                System.out.println("case 2 (b)");
                if (totalProducts>documentSnapshotsSize)
                {
                    System.out.println("case 2 (b), (full) 1rst of n pages");
                    btnNext_query.setEnabled(true);
                    btnBack_query.setEnabled(false);
                }
                else{
                    System.out.println("case 2 (b), (full) 1rst and last page");
                    btnNext_query.setEnabled(false);
                    btnBack_query.setEnabled(false);
                }
                break;
            case 3:
                // fullPage true and firstPage true
                System.out.println("case 3 (d), first an last page");
                btnNext_query.setEnabled(false);
                btnBack_query.setEnabled(false);
                break;
            default:
                throw new RuntimeException("Something strange happens, is fullPage?: "+ fullPage+
                        "is firstPage?: " + firstPage);
        }
    }



    public void fillArrayProducts(
            QuerySnapshot firestoneProductsArray,
            int productsPerPage)
    {

        // fill the recyclerView with products if there are at least one product
        if (totalProducts>0){

            // show the recyclerView and hide the no products textView
            setViewsVisibility(true );

            productArray= new Producto[productsPerPage];
            documentID = new String[productsPerPage];
            for (int i = 0; i < productsPerPage; i++)
            {
                // here fill the arrays
                Producto product =
                        firestoneProductsArray
                                .getDocuments()
                                .get( i)
                                .toObject(Producto.class);

                documentID[i] = firestoneProductsArray.getDocuments().get(i).getId();
                assert product != null;
                productArray[i]= product;
            }
            toImageAdapter();

        }
        else
        {
            // hide the recyclerView and show the no products textView
            setViewsVisibility(false);

            Toast.makeText(
                    getApplicationContext(),
                    "No hay productos para mostrar.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }



    private void setViewsVisibility (boolean visibly)
    {
        if (visibly)
        {
            textView_noQueryResults.setVisibility(View.GONE);
            recyclerView_query.setVisibility(View.VISIBLE);

            String strQueryAvailable = "Resultados para: " + strQuery ;
            textView_query.setText(strQueryAvailable);
        }
        else
        {
            Log.w("onFailure", "products not loading");
            recyclerView_query.setAdapter(null);
            recyclerView_query.setVisibility(View.GONE);
            textView_noQueryResults.setText(noQueryMsn+=  strQuery);
            textView_noQueryResults.setVisibility(View.VISIBLE);
        }
        progressProductsBarStatus = 100;
    }




    // [BEGIN-->] < =================== || toImageAdapter method || ================== > [BEGIN-->]
    // toImageAdapter method creates a CaptionImagesAdapter object that will initialize all the
    // cardViews in the RecyclerView
    public void toImageAdapter()
    {
        // create an CaptionImagesAdapter object and then i'll pass the arrays with the products
        // information that i want yo show in  the RecyclerView
        // The CaptionImagesAdapter only receive as arguments arrays with the elements to be
        // displayed.
        CaptionedImagesAdapter Adapter = new CaptionedImagesAdapter(
                documentID,
                productArray
        );

        recyclerView_query.setAdapter(Adapter);
        // LinearLayoutManager is used to show the cardview in a list way
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView_query.setLayoutManager(layoutManager);

        // * the listener will launch the EditActivity activity when the cardView within the
        //   recyclerView is touched
        // * "startActivity" implements the Listener onClick() method.
        //   It starts PizzaDetailActivity, passing it the product the user chose.

        Adapter.setListener(new CaptionedImagesAdapter.Listener()
        {
            public void onClick(Producto product, String ProductDocID)
            {
                // create a bundle to wrap a Producto object
                Bundle bundleProduct = new Bundle();
                bundleProduct.putParcelable("bundleProduct", product);

                // put an extra value (docID) in the bundle
                bundleProduct.putString("bundleStrDocID", ProductDocID);

                // i initialize the intent, to be ready to send the Producto object
                Intent intentProduct;
                intentProduct = new Intent(
                        SearchableActivity.this, selectedProduct.class);
                intentProduct.putExtra("intentGetProduct", bundleProduct);
                startActivity(intentProduct);
            }
        });

    }
    // [<-- END] < =================== || toImageAdapter method || ================== > [<-- END]






    // [BEGIN-->] < =============== || LaunchProgressQueryBar method || =============== > [BEGIN-->]
    public void LaunchProgressQueryBar(View v)
    {
        progressBarQuery = new ProgressDialog(v.getContext());
        progressBarQuery.setCancelable(false);
        progressBarQuery.setMessage("Buscando productos...");
        progressBarQuery.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBarQuery.setProgress(0);
        progressBarQuery.setMax(100);
        progressBarQuery.show();
        progressProductsBarStatus = 0;

        new Thread(new Runnable() {
            public void run() {
                while (progressProductsBarStatus < 100)
                {

                }

                if (progressProductsBarStatus >= 100) {

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    progressBarQuery.dismiss();
                }
            }
        }).start();
    }
    // [<-- END] < =============== || LaunchProgressQueryBar method || =============== > [<-- END]



    // [BEGIN-->] <==========|| Action Bar override methods ||==========> [BEGIN-->]
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
            System.out.println("action_home");
            startActivity(new Intent(this, UserAuth_menu.class));
            return true;
        }
        else if (item.getItemId() == R.id.action_cart)
        {
            System.out.println("action_cart");
            startActivity(new Intent(this, BuyNowActivity.class));
            return  true;
        }
        else if (item.getItemId() == R.id.action_search)
        {
            System.out.println("action_search");
            noQueryMsn = "No hay resultados para: ";
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
        System.out.println("super.onOptionsItemSelected");
        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }
    //[<-- END] <==========|| Action Bar override methods ||==========> [<-- END]
}
