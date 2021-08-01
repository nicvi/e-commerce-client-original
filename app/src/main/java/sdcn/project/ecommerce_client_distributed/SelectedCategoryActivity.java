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

public class SelectedCategoryActivity extends AppCompatActivity
{

    // [____________|| Firestore ||____________] [<--BEGIN]
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // [____________|| Firestore ||____________] [END-->]


    // [____________|| Viewers. ||____________] [<--BEGIN]
    private Button btnBack, btnNext;
    // i make a view RecyclerView  and bind it with the xml file that contains the recyclerView.
    private RecyclerView recyclerView_selectedCategory;
    private TextView textView_noProductsMsn ;
    // [____________|| Viewers. ||____________] [END-->]


    // [____________|| Variables ||____________] [BEGIN-->]
    private String strSelectedCategory; // bundle
    // [____________|| Variables ||____________] [<--END]


    // [____________|| progress Bar ||____________] [<--BEGIN]
    private ProgressDialog progressBarProducts;
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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_category);

        // [BEGIN -->] _______________ VIEWS _____________________________ [BEGIN -->]
        btnBack = findViewById(R.id.button_selectedCategoryBack);
        btnNext = findViewById(R.id.button_selectedCategoryNext);
        textView_noProductsMsn = findViewById(R.id.textView_noProductsMsn);
        recyclerView_selectedCategory = findViewById(R.id.recyclerView_selectedCategory);

        textView_noProductsMsn.setVisibility(View.GONE);
        btnNext.setEnabled(false);
        btnBack.setEnabled(false);
        // [<-- END] ___________________________ VIEWS ___________________ [<-- END]




        // [BEGIN -->] _______________ Toolbar _____________________________ [BEGIN -->]
        // set the toolbar on the layout as the app bar for the activity
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_selectedCategory);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        // [<-- END] ___________________________ Toolbar ___________________ [<-- END]




        // [BEGIN -->] _______________ GET BUNDLE _____________________________ [BEGIN -->]
        Bundle categoryBundle = getIntent().getBundleExtra("categoryGetExtra");
        if (categoryBundle!=null)
        {
            if (!categoryBundle.isEmpty())
            {
                strSelectedCategory = categoryBundle.getString("bundleCategory");
                LaunchProgressSelectedCategoryBar(recyclerView_selectedCategory);
                getTotalProducts();

                btnNext.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v){
                        btnNext.setEnabled(false);
                        paginateNextQuery();
                        // launch the progress bar until the next page is loaded
                        LaunchProgressSelectedCategoryBar(recyclerView_selectedCategory);
                    }
                });

                btnBack.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        btnBack.setEnabled(false);
                        paginatePreviousQuery();
                        // launch the progress bar until the next page is loaded
                        LaunchProgressSelectedCategoryBar(recyclerView_selectedCategory);
                    }
                });
            }
        }
        // [END -->] _______________ GET BUNDLE _____________________________ [END -->]


    }

    private void getTotalProducts()
    {
        // i get the number of products
        db
        .collection("Productos")
        .whereEqualTo("categoria", strSelectedCategory)
        .get()
        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots)
            {
                // if query is not empty
                if (!documentSnapshots.isEmpty())
                {
                    //progressProductsBarStatus = 100;
                    totalProducts = documentSnapshots.size();
                    //System.out.println("total products on category: "+ documentSnapshots.size());

                    paginateFirstQuery();
                }
                // no products of this category available
                else
                {
                    recyclerView_selectedCategory.setVisibility(View.GONE);
                    textView_noProductsMsn.setVisibility(View.VISIBLE);
                    progressProductsBarStatus = 100;
                    Log.w("document is Empty", "accessing next document");
                }
            }
        })
        .addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                recyclerView_selectedCategory.setVisibility(View.GONE);
                textView_noProductsMsn.setVisibility(View.VISIBLE);
                progressProductsBarStatus = 100;
                Log.w("onFailure", "accessing next document", e);
            }
        });
    }



    // ======================================================

    public void LaunchProgressSelectedCategoryBar(View v)
    {
        progressBarProducts = new ProgressDialog(v.getContext());
        progressBarProducts.setCancelable(false);
        progressBarProducts.setMessage("Obteniendo productos...");
        progressBarProducts.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBarProducts.setProgress(0);
        progressBarProducts.setMax(100);
        progressBarProducts.show();
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

                    progressBarProducts.dismiss();
                }
            }
        }).start();
    }

    // ======================================================

    public void paginateFirstQuery()
    {
        currentPage = 0;

        System.out.println("paginateFirstQuery");
        // Construct query for first 25 cities, ordered by population

        db.collection("Productos")
            .whereEqualTo("categoria", strSelectedCategory)
            .orderBy("name")
            .limit(productsPerPage)
            .get()
            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
            {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots)
                {
                    // ...

                    // paging first page
                    paging(documentSnapshots.size());

                    //System.out.println(
                    // "paginateFirstQuery firstQuerySize: "+documentSnapshots.size());

                    // set values for the last document read it and the first one
                    lastVisibleNext = documentSnapshots.getDocuments()
                            .get(documentSnapshots.size() -1);
                    lastVisiblePrevious = documentSnapshots.getDocuments()
                            .get(0);

                    // call a method for get the Products from fireStore
                    fillArrayProducts(documentSnapshots, documentSnapshots.size());
                    // Construct a new query starting at this document,
                    // get the next 25 cities.

                    // Use the query for pagination
                    // ...
                }
            })
            .addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    System.out.println("paginateFirstQuery onFailure ");

                    progressProductsBarStatus = 100;
                    recyclerView_selectedCategory.setVisibility(View.GONE);
                    textView_noProductsMsn.setVisibility(View.VISIBLE);

                    Log.w("onFailure", "accessing next document", e);
                }
            });;
    }


    public void paginateNextQuery()
    {
        db.collection("Productos")
            .whereEqualTo("categoria", strSelectedCategory)
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
                        progressProductsBarStatus = 100;
                        recyclerView_selectedCategory.setVisibility(View.GONE);
                        textView_noProductsMsn.setVisibility(View.VISIBLE);
                        Log.w("onFailure", "accessing next document", e);
                    }
                });
    }

    public void paginatePreviousQuery()
    {

        db.collection("Productos")
            .whereEqualTo("categoria", strSelectedCategory)
            .orderBy("name")
            .endBefore(lastVisiblePrevious)
            .limitToLast(productsPerPage).get()
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
                        progressProductsBarStatus = 100;
                        recyclerView_selectedCategory.setVisibility(View.GONE);
                        textView_noProductsMsn.setVisibility(View.VISIBLE);
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
                    btnNext.setEnabled(true);
                    btnBack.setEnabled(true);
                }
                else{
                    System.out.println("case 0 (a), n page y last page");
                    btnNext.setEnabled(false);
                    btnBack.setEnabled(true);
                }
                break;
            case 1:
                // fullPage true ^ firstPage false
                System.out.println("case 1 (c), last page");
                btnNext.setEnabled(false);
                btnBack.setEnabled(true);
                break;
            case 2:
                // fullPage false ^ firstPage true
                System.out.println("case 2 (b)");
                if (totalProducts>documentSnapshotsSize)
                {
                    System.out.println("case 2 (b), (full) 1rst of n pages");
                    btnNext.setEnabled(true);
                    btnBack.setEnabled(false);
                }
                else{
                    System.out.println("case 2 (b), (full) 1rst and last page");
                    btnNext.setEnabled(false);
                    btnBack.setEnabled(false);
                }
                break;
            case 3:
                // fullPage true and firstPage true
                System.out.println("case 3 (d), first an last page");
                btnNext.setEnabled(false);
                btnBack.setEnabled(false);
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
        // loading the progress bar
        progressProductsBarStatus = 100;

        if (totalProducts>0){
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
            recyclerView_selectedCategory.setVisibility(View.GONE);
            textView_noProductsMsn.setVisibility(View.VISIBLE);

            Log.w("totalProducts <0", "filling recyclerView");
            Toast.makeText(
                    getApplicationContext(),
                    "No hay productos para mostrar..",
                    Toast.LENGTH_LONG
            ).show();
        }
    }






    // <==========|| toImageAdapter method creates a CaptionImagesAdapter
    // object that will initialize all the cardViews in the RecyclerView ||==========> [BEGIN]
    public void toImageAdapter()
    {
        // end the progress bar because all the products of the page have been load
        progressProductsBarStatus=100;
        // create an CaptionImagesAdapter object and then i'll pass the arrays with the products
        // information that i want yo show in  the RecyclerView
        // The CaptionImagesAdapter only receive as arguments arrays with the elements to be
        // displayed.
        CaptionedImagesAdapter Adapter = new CaptionedImagesAdapter(
                documentID,
                productArray
        );

        recyclerView_selectedCategory.setAdapter(Adapter);
        // LinearLayoutManager is used to show the cardview in a list way
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView_selectedCategory.setLayoutManager(layoutManager);

        // data_download set as true will allow to do paging
        //data_download = true;

        // I enable the buttons
        //toggleButtons();

        // * the listener will launch the EditActivity activity when the cardView within the
        //   recyclerView is touched
        // * "startActivity" implements the Listener onClick() method.
        //   It starts PizzaDetailActivity, passing it the product the user chose.

        Adapter.setListener(new CaptionedImagesAdapter.Listener()
        {
            public void onClick(Producto product, String ProductDocID) {
                /*Intent intent = new Intent(
                        SelectedCategoryActivity.this,
                        selectedProduct.class
                );
                intent.putExtra(selectedProduct.EXTRA_PRODUCT_ATTRIBUTES, Producto);
                startActivity(intent);
                 */

                // create a bundle to wrap a Producto object
                Bundle bundleProduct = new Bundle();
                bundleProduct.putParcelable("bundleProduct", product);

                // put an extra value (docID) in the bundle
                bundleProduct.putString("bundleStrDocID", ProductDocID);

                // i initialize the intent, to be ready to send the Producto object
                Intent intentProduct;
                intentProduct = new Intent(
                        SelectedCategoryActivity.this, selectedProduct.class);
                intentProduct.putExtra("intentGetProduct", bundleProduct);
                startActivity(intentProduct);
            }
        });

    }
    // <==========|| toImageAdapter method creates a CaptionImagesAdapter
    // object that will initialize all the cardViews in the RecyclerView ||==========> [END]







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