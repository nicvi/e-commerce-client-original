package sdcn.project.ecommerce_client_distributed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import sdcn.project.ecommerce_client_distributed.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChooserCategoryActivity extends AppCompatActivity {

    // [____________|| Firestore ||____________] [<--BEGIN]
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
    // [____________|| Firestore ||____________] [END-->]


    // [____________|| Variables ||____________] [END-->]
    private List<String> arrayCategories =  new ArrayList<String>();
    // [____________|| Variables ||____________] [END-->]


    // [____________|| Views ||____________] [BEGIN-->]
    private ListView listView_fireStoreCategories;
    private Button button_AllCategories;
    // [____________|| Views ||____________] [END-->]


    // [____________|| progress Bar ||____________] [<--BEGIN]
    private ProgressDialog progressBarProducts;
    private int progressProductsBarStatus = 0;
    // [____________|| Variables ||____________] [END-->]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser_category);

        button_AllCategories = findViewById(R.id.button_AllCategories);
        listView_fireStoreCategories = (ListView) findViewById(R.id.listView_fireStoreCategories);

        button_AllCategories.setVisibility(View.GONE);

        // [BEGIN -->] _______________Toolbar_____________________________
        // set the toolbar on the layout as the app bar for the activity
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_ChooserCategory);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        // ___________________________Toolbar___________________ [<-- END]

        launchCategoriesProgressBar(listView_fireStoreCategories);

        getCategoriesFireStore();

    }

    private void getCategoriesFireStore()
    {

        db
        .collection("Categorias")
        .orderBy("Categoria")
        .get()
        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
        {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshots)
            {
                System.out.println("documentSnapshots size: " + documentSnapshots.size());
                if (!documentSnapshots.isEmpty())
                {
                    System.out.println("documentSnapshots is NOT empty");
                    for(DocumentSnapshot category : documentSnapshots)
                    {
                        arrayCategories.add(
                                Objects.requireNonNull(category.get("Categoria")).toString()
                        );
                        System.out.println(
                                Objects.requireNonNull(category.get("Categoria")).toString()
                        );
                    }

                    progressProductsBarStatus = 100;

                    setViews();
                }
                else
                {
                    System.out.println("documentSnapshots IS empty");

                    progressProductsBarStatus = 100;
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressProductsBarStatus = 100;

                Log.w("onFailure", "accessing next document", e);
            }
        });
    }





    private void setViews()
    {
        //button_AllCategories = findViewById(R.id.button_AllCategories);
        button_AllCategories.setVisibility(View.VISIBLE);

        button_AllCategories.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(
                        ChooserCategoryActivity.this, activity_productos.class)
                );
            }
        });

        populateListView();
    }




    private void populateListView()
    {
        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                arrayCategories );

        // set the adapter to the listView
        listView_fireStoreCategories.setAdapter(arrayAdapter);
        setActionListView();
    }





    private void setActionListView()
    {
        listView_fireStoreCategories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle chooserBundle = new Bundle ();
                chooserBundle.putString("bundleCategory", (String) parent.getItemAtPosition(position));

                Intent categoryIntent  = new Intent (
                        ChooserCategoryActivity.this, SelectedCategoryActivity.class);

                categoryIntent.putExtra("categoryGetExtra", chooserBundle);
                startActivity(categoryIntent);
            }
        });
    }




    private void launchCategoriesProgressBar(View v)
    {
        progressBarProducts = new ProgressDialog(v.getContext());
        progressBarProducts.setCancelable(false);
        progressBarProducts.setMessage("Obteniendo información...");
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





    // [BEGIN-->] <==========|| Action Bar override methods ||==========>
    // inflate the app bar with the toolbar wanted model
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // R.menu.mymenu is a reference to an xml file named menu.xml which should be inside your
        // res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // set actions to the app bar buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_home)
        {
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