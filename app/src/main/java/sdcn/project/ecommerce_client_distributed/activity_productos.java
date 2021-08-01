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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

// TODO This activity show the products in the recyclerView

// * just download the data from fireStore take from 1.5 to 6.2 seconds, without showing them on the
//      recyclerView, and without considering the time that take to download the image and then
//      displayed them.
public class activity_productos extends AppCompatActivity
{



    // [____________|| Viewers. ||____________] [<--BEGIN]
    private Button btnAnterior, btnSiguiente;
    // i make a view RecyclerView  and bind it with the xml file that contains the recyclerView.
    private RecyclerView productoRecycler;
    private TextView textView_noProductsResult;
    // [____________|| Viewers. ||____________] [END-->]



    // [____________|| Product Object Variables (captionedImageAdapter) ||____________] [<--BEGIN]
    private Producto[] productArray;
    private String[] documentID;
    // [____________|| Product Object Variables (captionedImageAdapter) ||____________] [END-->]



    // [____________|| Firestore ||____________] [<--BEGIN]
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentSnapshot lastVisibleNext, lastVisiblePrevious;
    private Query firstQuery;
    // [____________|| Firestore ||____________] [END-->]



    // [____________|| Pagination ||____________] [<--BEGIN]
    private Paginator paginator ;
    // TODO private int totalPages = Paginator.TOTAL_NUM_ITEMS / Paginator.ITEMS_PER_PAGE;
    private int totalPages;
    private int productsPerPages=5; // old variable, used in methods unused
    private int total_Products; // old variable, used in methods unused
    private long totalProducts;
    private final int productsPerPage = 6;
    private int currentPage = 0;

    private boolean data_download= false;
    // [____________|| Pagination ||____________] [END-->]



    // [____________|| Variables ||____________] [<--BEGIN]
    // si currentPage lo cambio a 1, entonces no le debo sumar el +1 al currentPage
    // que se encuenta en el case 0 del metodo paging.

    private long startTime;
    private long startTime2;
    private long startTime3;
    // [____________|| Variables ||____________] [END-->]



    // [____________|| progress Bar ||____________] [<--BEGIN]
    private ProgressDialog progressBarProducts;
    private int progressProductsBarStatus = 0;
    // [____________|| Variables ||____________] [END-->]



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos);


        // [____________|| Viewers. ||____________] [BEGIN]
        btnAnterior= (Button) findViewById(R.id.btnAnterior);
        btnSiguiente= (Button) findViewById(R.id.btnSiguiente);
        btnAnterior.setEnabled(false);
        btnSiguiente.setEnabled(false);
        // i make a view RecyclerView  and bind it with the xml file that contains the recyclerView
        productoRecycler = (RecyclerView) findViewById(R.id.product_recycler);
        textView_noProductsResult = findViewById(R.id.textView_noProductsResult);

        textView_noProductsResult.setVisibility(View.GONE);
        // [____________|| Viewers. ||____________] [ END] ------>>>>>>


        // [BEGIN -->] _______________Toolbar_____________________________
        // set the toolbar on the layout as the app bar for the activity
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_productos);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        // ___________________________Toolbar___________________ [<-- END]

        // launch the progress bar until the first page is loaded
        LaunchProgressBarAndBuy(productoRecycler);

        startTime = System.currentTimeMillis();

        // first i call the "getProductsNumber" method which get me the total number of products,
        //      this method also call the method "paginateFirstQuery" which retrieve the information of
        //      the first "productsPerPage" number of products.
        getProductsNumber();

        btnSiguiente.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                btnSiguiente.setEnabled(false);
                paginateNextQuery();
                // launch the progress bar until the next page is loaded
                LaunchProgressBarAndBuy(productoRecycler);
            }
        });

        btnAnterior.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                btnAnterior.setEnabled(false);
                paginatePreviousQuery();
                // launch the progress bar until the next page is loaded
                LaunchProgressBarAndBuy(productoRecycler);
            }
        });
    }

    public void getProductsNumber()
    {
        DocumentReference docRef = db
                .collection("CantidadProductos")
                .document("metaData");
        // i get the number of products
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task){
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()){
                        totalProducts = document.getLong("totalProductos");
                        // Do something
                        paginateFirstQuery();
                    }
                    else
                    {
                        progressProductsBarStatus=100;
                        Log.d("Error", "No such document");
                    }
                }else
                {
                    progressProductsBarStatus=100;
                    Log.d("Error", "get failed with ");
                }
            }
        });
    }



    // ======================================================

    public void LaunchProgressBarAndBuy(View v)
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
                    // call the method that will be waited to finish it


                    // TODO why do i call Thread.sleep(1000), it is because this was the original
                    //  format of the code that i found on internet?
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





    public void updateProductsNumber(String action)
    {

        DocumentReference docRef = db
                .collection("CantidadProductos")
                .document("metaData");

        // i update the number of products, i decrease or increase it.
        if (action.equals("updateAdd")){
            totalProducts+=1;
            docRef
                    .update("totalProductos", totalProducts)
                    .addOnSuccessListener(new OnSuccessListener<Void>(){
                        @Override
                        public void onSuccess(Void avoid){
                            // products number  was successfully updated --> i'll reload the recyclerView
                            Log.d("UpdatedAdd: ", "Total products increased successfully!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener(){
                        @Override
                        public void onFailure(@NonNull Exception e){
                            Log.w("Updated", "Error updated document", e);
                        }
                    });

        }
        else if(action.equals("updateMinus")){
            totalProducts-=1;
            docRef
                    .update("totalProductos", totalProducts)
                    .addOnSuccessListener(new OnSuccessListener<Void>(){
                        @Override
                        public void onSuccess (Void avoid){
                            //paginate first Query again;
                            paginateFirstQuery();
                            Log.d("UpdatedMinus", "Total products decreased successfully!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener(){
                        @Override
                        public void onFailure(@NonNull Exception e){
                            Log.w("updatedMinus: ", "Error Updated document", e);
                        }
                    });
        }

    }

    public void paginateActualQuery()
    {
        startTime2 = System.currentTimeMillis();

        Query next = db.collection("Productos")
                .orderBy("name")
                .startAt(lastVisiblePrevious)
                .limit(productsPerPage);

        next.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots)
                    {
                        // enable btn next, paging next pages
                        paging(documentSnapshots.size());
                        // update the lastVisible document
                        lastVisiblePrevious = documentSnapshots.getDocuments()
                                .get(0);

                        // call a method for get the Products from fireStore
                        fillArrayProducts(documentSnapshots, documentSnapshots.size());
                        final long endTime = System.currentTimeMillis();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("onFailure", "accessing next document", e);
                    }
                });
    }


    public void paginateNextQuery()
    {
        startTime2 = System.currentTimeMillis();

        Query next = db.collection("Productos")
                .orderBy("name")
                .startAfter(lastVisibleNext)
                .limit(productsPerPage);

        next.get()
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
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("onFailure", "accessing next document", e);
                    }
                });
    }




    public void paginatePreviousQuery(){
        startTime3 = System.currentTimeMillis();

        Query next = db
                .collection("Productos")
                .orderBy("name")
                .endBefore(lastVisiblePrevious)
                .limitToLast(productsPerPage);

        db
        .collection("Productos")
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
                    final long endTime = System.currentTimeMillis();

                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("onFailure", "accessing next document", e);
                }
            });

    }




    public void paginateFirstQuery()
    {
        currentPage = 0;
        // Construct query for first 25 cities, ordered by population
        firstQuery = db.collection("Productos")
                .orderBy("name")
                .limit(productsPerPage);

        firstQuery.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots) {
                        // ...

                        // paging first page
                        paging(documentSnapshots.size());

                        // set values for the last document read it and the first one
                        lastVisibleNext = documentSnapshots.getDocuments()
                                .get(documentSnapshots.size() -1);
                        lastVisiblePrevious = documentSnapshots.getDocuments()
                                .get(0);

                        // call a method for get the Products from fireStore
                        fillArrayProducts(documentSnapshots, documentSnapshots.size());
                        final long endTime = System.currentTimeMillis();
                        // Construct a new query starting at this document,
                        // get the next 25 cities.

                        // Use the query for pagination
                        // ...
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
                    btnSiguiente.setEnabled(true);
                    btnAnterior.setEnabled(true);
                }
                else{
                    System.out.println("case 0 (a), n page y last page");
                    btnSiguiente.setEnabled(false);
                    btnAnterior.setEnabled(true);
                }
                break;
            case 1:
                // fullPage true ^ firstPage false
                System.out.println("case 1 (c), last page");
                btnSiguiente.setEnabled(false);
                btnAnterior.setEnabled(true);
                break;
            case 2:
                // fullPage false ^ firstPage true
                System.out.println("case 2 (b)");
                if (totalProducts>documentSnapshotsSize)
                {
                    System.out.println("case 2 (b), (full) 1rst of n pages");
                    btnSiguiente.setEnabled(true);
                    btnAnterior.setEnabled(false);
                }
                else{
                    System.out.println("case 2 (b), (full) 1rst and last page");
                    btnSiguiente.setEnabled(false);
                    btnAnterior.setEnabled(false);
                }
                break;
            case 3:
                // fullPage true and firstPage true
                System.out.println("case 3 (d), first an last page");
                btnSiguiente.setEnabled(false);
                btnAnterior.setEnabled(false);
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
            productoRecycler.setAdapter(null);
            progressProductsBarStatus=100;
            Toast.makeText(
                    getApplicationContext(),
                    "No hay productos para mostrar.",
                    Toast.LENGTH_SHORT
            ).show();
            textView_noProductsResult.setVisibility(View.VISIBLE);
        }
    }


    // -------------------------------------------------------------------------------------------->

    // <==========|| constructProduct method ||==========> [BEGIN]
    // constructProduct method gets the information of each document on the "Producto" collection
    public void constructProduct(String collection){
        Task<QuerySnapshot> docRef;
        docRef = db.collection(collection)
                .get();
        docRef.addOnCompleteListener(new OnCompleteListener< QuerySnapshot>(){
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task){

                if(task.isSuccessful()){
                    total_Products= Objects.requireNonNull(task.getResult()).size();
                    totalPages=total_Products/productsPerPages;
                    // TODO here down i comment a uncomment commentary
                    //pagingOld(task.getResult(), total_Products, productsPerPages);
                }
                else{
                    Log.w(
                            "constructProduct(): ",
                            "Error getting documents.",
                            task.getException()
                    );
                }
            }
        });
    }
    // <==========|| constructProduct method ||==========> [END]





    // <==========|| paging method ||==========> [BEGIN]
    public void pagingOld(
            QuerySnapshot FirestoneProductList,
            int totalProducts,
            int productPerPage)
    {
        // if "totalProducts=0" then there are no products in the firestone collection "Productos"
        // then there are products
        if(totalProducts>0) {
            // First i assume that are as many products in fireStore as productPerPage
            // each array must be reset and re-initialize each time "paging" method is called.
            // each array size must be the "productPerPage" int value.
            productArray= new Producto[productPerPage];
            documentID = new String[productPerPage];

            // Basics paging variables
            int ITEMS_REMAINING=totalProducts % productPerPage;
            int LAST_PAGE=totalProducts/productPerPage;

            // "i" is the item index from all the lists used for add on it all the products field
            // obtained from the "Products" collection query snapshot list.

            //int i =0 ;

            // entrego una lista con una coleccion personalizada, osea no con todos los productos,
            // sino con los productos correspendientes de la pagina.
            int startItem=currentPage*productPerPage;

            // if there are items remaining then those will be show in a new page ("extra page").
            if (currentPage == LAST_PAGE && ITEMS_REMAINING > 0)
            {
                // initialize again the documentID array and the productArray array
                productArray= new Producto[ITEMS_REMAINING];
                documentID = new String[ITEMS_REMAINING];

                for (int i = 0; i < ITEMS_REMAINING; i++)
                {
                    // here fill the arrays
                    Producto product =
                            FirestoneProductList
                                    .getDocuments()
                                    .get(startItem + i)
                                    .toObject(Producto.class);

                    documentID[i] = FirestoneProductList.getDocuments().get(i).getId();
                    assert product != null;
                    productArray[i]= product;
                }
            }
            // else: totalItems <= productPerPage
            else
            {
                for (int i = 0; i < productPerPage; i++)
                {
                    // here fill the arrays
                    Producto product =
                            FirestoneProductList.
                                    getDocuments().
                                    get(startItem + i).
                                    toObject(Producto.class);
                    documentID[i] = FirestoneProductList.getDocuments().get(i).getId();
                    assert product != null;
                    productArray[i]= product;
                }
            }
            toImageAdapter();
        }
        // If there are no elements to download from firebase, because totalProducts = 0, then the
        // recyclerView will show nothing.
        else
        {
            progressProductsBarStatus=100;
            productoRecycler.setAdapter(null);
            Toast.makeText(
                    getApplicationContext(),
                    "No hay productos para mostrar.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
    // <==========|| paging method ||==========> [END]




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

        productoRecycler.setAdapter(Adapter);
        // LinearLayoutManager is used to show the cardview in a list way
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        productoRecycler.setLayoutManager(layoutManager);

        // data_download set as true will allow to do paging
        //data_download = true;

        // I enable the buttons
        //toggleButtons();

        // * the listener will launch the EditActivity activity when the cardView within the
        //   recyclerView is touched
        // * "startActivity" implements the Listener onClick() method.
        //   It starts PizzaDetailActivity, passing it the product the user chose.

        Adapter.setListener(new CaptionedImagesAdapter.Listener() {
            public void onClick(Producto product, String ProductDocID) {
                /*
                Intent intent = new Intent(
                        activity_productos.this,
                        selectedProduct.class
                );
                intent.putExtra(selectedProduct.EXTRA_PRODUCT_ATTRIBUTES, product);
                activity_productos.this.startActivity(intent);
                 */

                // create a bundle to wrap a Producto object
                Bundle bundleProduct = new Bundle();
                bundleProduct.putParcelable("bundleProduct", product);

                // put an extra value (docID) in the bundle
                bundleProduct.putString("bundleStrDocID", ProductDocID);


                // i initialize the intent, to be ready to send the Producto object
                Intent intentProduct;
                intentProduct = new Intent(activity_productos.this, selectedProduct.class);
                intentProduct.putExtra("intentGetProduct", bundleProduct);
                startActivity(intentProduct);
            }
        });

    }
    // <==========|| toImageAdapter method creates a CaptionImagesAdapter
    // object that will initialize all the cardViews in the RecyclerView ||==========> [END]




    // <==========|| toggleButtons enable or disable the next or previous button depending if it is on the first or last page ||==========> [BEGIN]
    private void toggleButtons() {
        if (currentPage == totalPages) {
            btnSiguiente.setEnabled(false);
            btnAnterior.setEnabled(true);
        } else if (currentPage == 0) {
            btnAnterior.setEnabled(false);
            btnSiguiente.setEnabled(true);
        } else if (currentPage >= 1 && currentPage <= totalPages) {
            btnSiguiente.setEnabled(true);
            btnAnterior.setEnabled(true);
        }
    }
    // <==========|| toggleButtons enable or disable the next or previous button depending if it is on the first or last page ||==========> [END]






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