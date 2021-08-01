package sdcn.project.ecommerce_client_distributed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class BillSelected extends AppCompatActivity
{

    // Bill object
    private Bill bill;
    // views
    private TextView textView_billName;
    private TextView textView_billTotalPrice;
    private TextView textView_billDate;
    private TextView textView_billHour;
    private TextView textView_billPurchaseMethod;
    private TextView textView_billTotalProducts;
    private TextView textView_billPickedUp;
    private RecyclerView recyclerView_selectedBill;
    private Button button_deleteSelectedBill;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_selected);

        // If get the product from the "selectedProduct" activity then ill store it in a Bundle var
        Bundle b = getIntent().getBundleExtra("bundleBill");
        bill =b.getParcelable("optionsBill");

        // init views
        textView_billName = findViewById(R.id.textView_billName);
        textView_billTotalPrice = findViewById(R.id.textView_billTotalPrice);
        textView_billDate = findViewById(R.id.textView_billDate);
        textView_billHour = findViewById(R.id.textView_billHour);
        textView_billPurchaseMethod = findViewById(R.id.textView_billPurchaseMethod);
        textView_billTotalProducts = findViewById(R.id.textView_billTotalProducts);
        textView_billPickedUp = findViewById(R.id.textView_billPickedUp);

        recyclerView_selectedBill =(RecyclerView) findViewById(R.id.recyclerView_selectedBill);
        button_deleteSelectedBill = findViewById(R.id.button_deleteSelectedBill);


        // [BEGIN -->] _______________Toolbar_____________________________
        // set the toolbar on the layout as the app bar for the activity
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_billSelected);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        // ___________________________Toolbar___________________ [<-- END]


        //setViews
        if (bill!= null)
        {
            setViews();
            setSelectedBillAdapter();
            deleteSelectedBill();
        }
    }

    public void setViews()
    {
        textView_billName.setText(bill.getBillName());
        // bill price
        String billPrice = "$ " + bill.getBillPrice();
        textView_billTotalPrice.setText(billPrice);

        // is picked Up
        String pickedUp="";
        if (bill.isPickedUp())
        {
            pickedUp = "Si";
        }
        else
        {
            pickedUp = "No";
            textView_billPickedUp.setTextColor(Color.parseColor("#981C1C"));
        }
        textView_billPickedUp.setText(pickedUp);

        // date
        String[] dateTimeArray = bill.getBillDate().toString().split(" ");

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

        String date = month + " " +day + ", " +year  ;

        textView_billDate.setText(date);
        textView_billHour.setText(time);

        // other views
        textView_billPurchaseMethod.setText(bill.getPurchasedMethod());

        // total products value
        textView_billTotalProducts.setText(String.valueOf(getTotalProducts()));
    }

    public int getTotalProducts()
    {
        int totalQuantityProducts = 0;


        for (int quantity : bill.getCompra().getPurchaseQuantityArray())
        {
            totalQuantityProducts += quantity;
        }

        return totalQuantityProducts;
    }

    public void setSelectedBillAdapter()
    {
        // create an CaptionSelectedBillAdapter object and then i'll pass the arrays with the
        // products information that i want yo show in  the RecyclerView.
        // The CaptionImagesAdapter only receive as arguments arrays with the elements to be
        // displayed.
        CaptionSelectedBillAdapter billSelectedAdapter = new CaptionSelectedBillAdapter(
                bill.getCompra().getPurchaseProductArray(),
                bill.getCompra().getPurchaseQuantityArray()
        );

        recyclerView_selectedBill.setAdapter(billSelectedAdapter);
        // LinearLayoutManager is used to show the cardView in a list way
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView_selectedBill.setLayoutManager(layoutManager);
    }

    public void deleteSelectedBill()
    {
        button_deleteSelectedBill.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v) {
                deleteBillFromFiresStore();
                // go back to the activity_factura
                Intent billIntent;
                billIntent = new Intent(BillSelected.this, activity_factura.class);
                billIntent.putExtra("billDeleted", "BillSelected");
                startActivity(billIntent);
            }
        });
    }


    public void deleteBillFromFiresStore()
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();;

        db.collection("Usuarios")
                .document(bill.getUserID())
                .collection("Compras").document(bill.getBillID())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(
                                getApplicationContext(),
                                "Se elimino factura: " + bill.getBillName(),
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