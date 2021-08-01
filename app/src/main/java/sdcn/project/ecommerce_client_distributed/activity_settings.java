package sdcn.project.ecommerce_client_distributed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import sdcn.project.ecommerce_client_distributed.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import lombok.SneakyThrows;

public class activity_settings extends AppCompatActivity {

    // [____________|| Firebase ||____________] [BEGIN-->]
    private FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser() ;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    // [____________|| Firebase ||____________] [<--END]



    // [____________|| progress Bar ||____________] [<--BEGIN]
    private ProgressDialog progressBarProducts;
    private int progressProductsBarStatus = 0;
    // [____________|| Variables ||____________] [END-->]


    // [____________|| Objects ||____________] [<--END]
    private User user;
    // [____________|| Objects ||____________] [<--END]

    // [____________|| Variables ||____________] [<--END]
    private LatLng latLong = new LatLng(0,0);
    private LatLng latLongUser = new LatLng(0,0);
    private MutableLiveData<LatLng> latLongListener;
    // [____________|| Variables ||____________] [<--END]

    // [____________|| Views ||____________] [BEGIN-->]
    private EditText editText_accountFirstNameValue;
    private EditText editText_accountLastNameValue;
    private TextView textView_accountMailValue;
    private EditText editText_accountPhoneValue;
    private TextView textView_accountLocationValue;

    private Button button_accountAddMap;
    private Button button_saveAccountChanges;

    private LinearLayout linearLayout_settings;
    // [____________|| Views ||____________] [<--END]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // init views
        editText_accountFirstNameValue = findViewById(R.id.editText_accountFirstNameValue);
        editText_accountLastNameValue = findViewById(R.id.editText_accountLastNameValue);
        textView_accountMailValue = findViewById(R.id.textView_accountMailValue);
        editText_accountPhoneValue = findViewById(R.id.editText_accountPhoneValue);
        textView_accountLocationValue = findViewById(R.id.textView_accountLocationValue);
        button_accountAddMap = findViewById(R.id.button_accountAddMap);
        button_saveAccountChanges = findViewById(R.id.button_saveAccountChanges);
        linearLayout_settings = findViewById(R.id.linearLayout_settings);

        // [BEGIN -->] _______________Toolbar_____________________________
        // set the toolbar on the layout as the app bar for the activity
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_onSettings);
        setSupportActionBar(myToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        // ___________________________Toolbar___________________ [<-- END]


        // launch the progress bar
        launchSettingsProgressBar(linearLayout_settings, "Obteniendo información.");
        // first get the fireStore user
        getFireStoreUser();
    }


    public void getFireStoreUser()
    {
        DocumentReference docRef;
        docRef = db
                .collection("Usuarios")
                .document(currentFirebaseUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @SneakyThrows
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    progressProductsBarStatus = 100;
                    // init the viewers
                    user = task.getResult().toObject(User.class);
                    if (user != null)
                    {
                        //latLong = new LatLng(user.getLatitude(), user.getLongitude());
                        latLongUser = new LatLng(user.getLatitude(), user.getLongitude());
                        try {
                            setViews();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        // TODO: launch a layout showing that the info have not been charge
                    }
                }
                else
                {
                    progressProductsBarStatus = 100;
                    // TODO: launch a layout showing that the info have not been charge
                    Log.w(
                            "constructProduct(): ",
                            "Error getting documents.",
                            task.getException()
                    );
                }
            }
        });
    }



    private void setViews() throws IOException
    {

        editText_accountFirstNameValue.setText(user.getFirstName());
        editText_accountLastNameValue.setText(user.getLastName());
        textView_accountMailValue.setText(user.getEmail());
        editText_accountPhoneValue.setText(user.getPhone());

        // set user address
        String userAddress = "No ha añadido ubicación.";
        if (user.getLatitude()!= 0 && user.getLongitude()!=0)
        {
            userAddress = addressGiver(new LatLng(user.getLatitude(), user.getLongitude()));
        }
        textView_accountLocationValue.setText(userAddress);

        // views listeners for the location TextView
        latLongListener = new MutableLiveData<>();
        //latLongListener.setValue(this.latLong);
        latLongListener.observe(this, new Observer<LatLng>(){
            @SneakyThrows
            @Override
            public void onChanged(LatLng latLongUpdated){
                if(latLongUpdated!=null){
                    try {
                        textView_accountLocationValue.setText(addressGiver(latLongUpdated));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //System.out.println("this.latLong: " + latLongUpdated);
                    //System.out.println("this.latLong.toString(): " + latLong.toString());
                }
            }
        });
        //

        // set actions to buttons
        setButtonsActions();
    }



    // <==========|| AddressGiver method ||==========> [BEGIN]
    // method to get the address from latitude, longitude

    public String addressGiver(LatLng mMarker) throws IOException
    {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(mMarker.latitude, mMarker.longitude, 3); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        return addresses.get(0).getAddressLine(0);
    }
    // <==========|| AddressGiver method ||==========> [END]



    private void setButtonsActions()
    {
        // add a user location from a map
        button_accountAddMap.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (latLongUser.latitude!=0 && latLongUser.longitude!=0)
                {
                    Bundle bLatLong = new Bundle();
                    bLatLong.putParcelable(
                            "bundleLatLng"
                            , new LatLng(latLongUser.latitude,latLongUser.longitude)
                    );

                    System.out.println("bundle intent");
                    Intent i = new Intent(activity_settings.this, MapsActivity.class);
                    i.putExtra("getExtraLatLng", bLatLong);
                    startActivityForResult(i,1);
                }
                else
                {
                    Intent i = new Intent(activity_settings.this, MapsActivity.class);
                    startActivityForResult(i,1);
                }
            }
        });

        // save changes button
        button_saveAccountChanges.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                System.out.println("hola desde save information");
                // call method to update user values on fireStore
                updateUserValues();
                if (latLong!=null) System.out.println("strEditText: " + latLong.toString());

            }
        });
    }

    private void updateUserValues()
    {
        // first check if name values and phone are not empty
        if (
                !editText_accountFirstNameValue.getText().toString().equals("")
                && !editText_accountLastNameValue.getText().toString().equals("")
                && !editText_accountPhoneValue.getText().toString().equals("")
        )
        {
            // * if at least one parameter have changed, then update the user values in fireStore
            // * the user object obtained from fireStore have the old values, the views fields
            // have the new values, so i compare the user object parameters with the views values.
            if (
                !editText_accountFirstNameValue.getText().toString().equals(user.getFirstName())
                || !editText_accountLastNameValue.getText().toString().equals(user.getLastName())
                || !editText_accountPhoneValue.getText().toString().equals(user.getPhone())
                || latLongUser.latitude!= user.getLatitude()
                || latLongUser.longitude!= user.getLongitude()
            )
            {
                System.out.println("update almost ready, no field empty and changes detected");
                // launch the progress bar
                launchSettingsProgressBar(linearLayout_settings, "actualizando información.");
                // update values
                DocumentReference washingtonRef;
                db
                .collection("Usuarios")
                .document(currentFirebaseUser.getUid())
                .update(
                        "firstName", editText_accountFirstNameValue.getText().toString(),
                        "lastName",editText_accountLastNameValue.getText().toString(),
                        "phone", editText_accountPhoneValue.getText().toString(),
                        "latitude",  latLong.latitude,
                        "longitude", latLong.longitude
                )
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        // update User object values
                        user.setLongitude(latLongUser.longitude);
                        user.setLatitude(latLongUser.latitude);
                        user.setFirstName(editText_accountFirstNameValue.getText().toString());
                        user.setLastName(editText_accountLastNameValue.getText().toString());
                        user.setPhone(editText_accountPhoneValue.getText().toString());

                        // end progress bar
                        progressProductsBarStatus=100;

                        // show a toast message indicating the values have been updated successfully
                        Log.d("TAG", "successfully updated!");
                        Toast.makeText(
                                activity_settings.this,
                                "Información actualizada.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        // end progress bar
                        progressProductsBarStatus=100;

                        // show a toast message indicating the update haven't been successfully
                        Log.w("TAG", "Error updating", e);
                    }
                });
            }
            else
            {
                Toast.makeText(
                        this,
                        "No hay cambios a realizar",
                        Toast.LENGTH_LONG
                ).show();
            }

        }
        else
        {
            Toast.makeText(this, "Tiene parametros vacios", Toast.LENGTH_LONG).show();
        }
    }

    // <==========|| onActivityResult method ||==========> [BEGIN]
    // "onActivityResult" method allows to get the coordinates of the user selected location
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
        {
            if(resultCode == RESULT_OK)
            {
                // get the coordinates of the user location on the "latLong" variable
                this.latLong = (LatLng)data.getExtras().getParcelable("editTextValue");
                System.out.println("strEditText: " + latLong.toString());
                // set a value to the "latLongListener" listener variable
                latLongListener.setValue(this.latLong);

                latLongUser = latLong;
                // update the long and lat user values
                // this.userLongitude = latLong.longitude;
                // this.userLatitude = latLong.latitude;
                //user.setLatitude(latLong.latitude);
                //user.setLongitude(latLong.longitude);
            }
        }
    }
    // <==========|| onActivityResult method ||==========> [END]


    // ======================================================

    public void launchSettingsProgressBar(View v, String msn)
    {
        progressBarProducts = new ProgressDialog(v.getContext());
        progressBarProducts.setCancelable(false);
        progressBarProducts.setMessage(msn);
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