package sdcn.project.ecommerce_client_distributed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import sdcn.project.ecommerce_client_distributed.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import lombok.SneakyThrows;

public class signup_mailpassword extends AppCompatActivity {

    // [____________|| Variables ||____________] [<--BEGIN]

    // Google maps variables
    MutableLiveData<LatLng> latLongListener;
    private LatLng latLong = null;
    double userLongitude = 0, userLatitude = 0;

    // Firebase variables
    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private static final String TAG = "EmailPassword";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Objects
    User user;
    // [____________|| Variables ||____________] [END-->]



    // [____________|| Viewers. ||____________] [<--BEGIN]
    private Button btn_signUp, btn_addLocation;
    private EditText signup_mail_edittext,
            signup_pass_edittext,
            editText_userLastName,
            editText_userFirstName,
            editText_userPhone;
    private TextView textView_location;
    // [____________|| Viewers. ||____________] [END-->]



    // [____________|| Progress Bar ||____________] [<--BEGIN]
    private ProgressDialog progressBar;
    private int progressSignUpBarStatus = 0;
    // [____________|| Progress Bar ||____________] [<--BEGIN]



    // <==========|| onCreate method ||==========> [BEGIN]
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_mailpassword);

        // Initialize Firebase Auth // variable took from Firebase authentication
        mAuth = FirebaseAuth.getInstance();
        // [____________|| Viewers. ||____________] [<--BEGIN]
        // btn
        btn_signUp = (Button) findViewById(R.id.signup_btn);
        btn_addLocation = (Button) findViewById(R.id.button_addMap);
        // Edit Text
        editText_userFirstName = (EditText) findViewById(R.id.editText_userFirstName);
        editText_userLastName = (EditText) findViewById(R.id.editText_userLastName);
        signup_mail_edittext= (EditText) findViewById(R.id.email_signup);
        signup_pass_edittext = (EditText) findViewById(R.id.password_signup);
        editText_userPhone = (EditText) findViewById(R.id.editText_userPhone);
        textView_location = (TextView) findViewById(R.id.textView_location);
        // [____________|| Viewers. ||____________] [END-->]

        // [____________||  Listeners. ||____________] [<--BEGIN]
        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("hola desde signup button");
                User userBuilt= createUser();
                if (userBuilt !=null)
                {
                    launchSingUpProgressBar(v);
                    createAccount(userBuilt);
                }
            }
        });

        btn_addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("hola desde add location");
                Intent i = new Intent(signup_mailpassword.this, MapsActivity.class);
                startActivityForResult(i, 1);
                //startActivity(new Intent(signup_mailpassword.this, MapsActivity.class));
            }
        });

        latLongListener = new MutableLiveData<>();
        latLongListener.setValue(this.latLong);
        latLongListener.observe(this, new Observer<LatLng>(){
            @SneakyThrows
            @Override
                public void onChanged(LatLng latLongUpdated){
                if(latLongUpdated!=null){
                    try {
                        textView_location.setText(AddressGiver(latLongUpdated));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //System.out.println("this.latLong: " + latLongUpdated);
                    //System.out.println("this.latLong.toString(): " + latLong.toString());
                }
            }
        });
        // [____________||  Listeners. ||____________] [END-->]

    }
    // <==========|| onCreate method ||==========> [END]




    // <==========|| AddressGiver method ||==========> [BEGIN]
    public String AddressGiver(LatLng mMarker) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(mMarker.latitude, mMarker.longitude, 3); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        return addresses.get(0).getAddressLine(0);
    }
    // <==========|| AddressGiver method ||==========> [END]




    // <==========|| createUser method ||==========> [BEGIN]
    public User createUser(){

        if(
                !TextUtils.isEmpty(editText_userFirstName.getText()) &&
                        !TextUtils.isEmpty(editText_userLastName.getText()) &&
                        !TextUtils.isEmpty(signup_mail_edittext.getText()) &&
                        !TextUtils.isEmpty(editText_userPhone.getText()) &&
                        !TextUtils.isEmpty( signup_pass_edittext.getText())
        )
        {
            if (editText_userPhone.getText().toString().length()>=9)
            {
                // Constructor order: FirstName, LastName, email, password, phone, latitude, longitude
                return new User(
                        editText_userFirstName.getText().toString(),
                        editText_userLastName.getText().toString(),
                        signup_mail_edittext.getText().toString(),
                        editText_userPhone.getText().toString(),
                        "",
                        this.userLatitude,
                        this.userLongitude);
            }
            else
            {
                Toast.makeText(
                        signup_mailpassword.this,
                        "Número de celular no valido.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
        else
        {
            //Log.d("Al crear producto","Los parametros de productos son null");
            Toast.makeText(
                    signup_mailpassword.this,
                    "Complete los parámetros para terminar el registro.",
                    Toast.LENGTH_LONG
            ).show();
        }
        return null;
    }
    // <==========|| createUser method ||==========> [END]




    // <==========|| onStart method ||==========> [BEGIN]
    // When initializing your Activity, check to see if the user is currently signed in.
    @Override
    public void onStart()
    {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    // <==========|| onStart method ||==========> [END]




    // <==========|| updateUI method ||==========> [BEGIN]
    private void updateUI(FirebaseUser user) {
        //hideProgressBar();
        if (user != null) {
            System.out.println("En metodo updateUI. User no es null");
            /*
            mBinding.status.setText(getString(R.string.emailpassword_status_fmt,
                    user.getEmail(), user.isEmailVerified()));
            mBinding.detail.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            mBinding.emailPasswordButtons.setVisibility(View.GONE);
            mBinding.emailPasswordFields.setVisibility(View.GONE);
            mBinding.signedInButtons.setVisibility(View.VISIBLE);

            if (user.isEmailVerified()) {
                mBinding.verifyEmailButton.setVisibility(View.GONE);
            } else {
                mBinding.verifyEmailButton.setVisibility(View.VISIBLE);
            }

             */
        } else {
            System.out.println("En metodo updateUI. User es null");
            /*
            mBinding.status.setText(R.string.signed_out);
            mBinding.detail.setText(null);

            mBinding.emailPasswordButtons.setVisibility(View.VISIBLE);
            mBinding.emailPasswordFields.setVisibility(View.VISIBLE);
            mBinding.signedInButtons.setVisibility(View.GONE);
            */
        }
    }
    // <==========|| updateUI method ||==========> [END]




    // <==========|| createAccount method ||==========> [BEGIN]
    // "createAccount" method takes an previously email address and password validated,
    // then creates a new user account with those values
    private void createAccount(final User user)
    {
        Log.d(TAG, "createAccount:" + user.toString());
        mAuth.createUserWithEmailAndPassword(user.getEmail(), signup_pass_edittext.getText().toString())
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail: success");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        // "addUserToFireStore" method creates a new User fireStore database
                        mAuth.getUid();
                        firebaseUser.getUid();
                        System.out.println("UID mAthu : mAuth.getUid()" + mAuth.getUid() +" - "
                                +"UID firebaseUser : "+ firebaseUser.getUid());
                        addUserToFireStore(user, mAuth.getUid());
                        updateUI(firebaseUser);
                    } else
                    {
                        // quit the progress bar view
                        progressSignUpBarStatus= 100;
                        // If sign in fails, display a message to the user.
                        if (signup_pass_edittext.getText().toString().length()<6)
                        {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(
                                    signup_mailpassword.this,
                                    "Error de registro, compruebe tamaño de contraseña.",
                                    Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(
                                    signup_mailpassword.this,
                                    "Error al registrase. Quizás ya este registrado",
                                    Toast.LENGTH_LONG).show();
                        }

                        //updateUI(null);
                    }
                }
            });
    }
    // <==========|| createAccount method ||==========> [END]




    // <==========|| addUserToFireStore method ||==========> [BEGIN]
    private void addUserToFireStore(User user, final String documentID)
    {
        user.setDocumentID(documentID);
        System.out.println("addUserToFireStore: " + user.toString());

        db.collection("Usuarios")
                .document(documentID)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(
                                TAG,
                                "DocumentSnapshot written with ID: " + documentID
                        );
                        // quit the progress bar view
                        progressSignUpBarStatus= 100;
                        // i launch the next activity from here because then i'll make sure that
                        // the user data is send to the fireStore database before the activity is
                        // launched and do not stop the connection to the fireStore database
                        startActivity(
                            new Intent(signup_mailpassword.this,UserAuth_menu.class)
                        );
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // quit the progress bar view
                        progressSignUpBarStatus= 100;
                        System.out.println("addUserToFireStore: OnFailureListener");
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }
    // <==========|| addUserToFireStore method ||==========> [END]



    // <==========|| onActivityResult method ||==========> [BEGIN]
    // "onActivityResult" method allows to get the coordinates of the user selected location
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                //String strEditText = data.getStringExtra("editTextValue");
                this.latLong = (LatLng)data.getExtras().getParcelable("editTextValue");
                System.out.println("strEditText: " + latLong.toString());
                // update the LatLng value
                latLongListener.setValue(this.latLong);
                // update the long and lat user values
                this.userLongitude = latLong.longitude;
                this.userLatitude = latLong.latitude;
            }
        }
    }
    // <==========|| onActivityResult method ||==========> [END]



    // <==========|| Progress bar ||==========> [BEGIN]
    private void launchSingUpProgressBar(View v)
    {
        progressBar = new ProgressDialog(v.getContext());
        progressBar.setCancelable(false);
        progressBar.setMessage("Registrando...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();
        progressSignUpBarStatus = 0;

        new Thread(new Runnable() {
            public void run() {
                while (progressSignUpBarStatus < 100) {
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

                if (progressSignUpBarStatus >= 100) {

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
    // <==========|| Progress bar ||==========> [END]
}