package sdcn.project.ecommerce_client_distributed;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import sdcn.project.ecommerce_client_distributed.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class signin_mailpassword extends AppCompatActivity {

    // variable took from FirebaseUI code
    private static final int RC_SIGN_IN = 123;
    // variable took from Firebase authentication
    private FirebaseAuth mAuth;
    // variable took from Firebase createAcountMethod
    private static final String TAG = "EmailPassword";
    // xml resources
    private Button btn_signIn;
    private EditText signin_mail_edittext, signin_pass_edittext;


    // [____________|| Progress Bar ||____________] [<--BEGIN]
    private ProgressDialog progressBar;
    private int progressSignInBarStatus = 0;
    // [____________|| Progress Bar ||____________] [<--BEGIN]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_mailpassword);
        // Initialize Firebase Auth // variable took from Firebase authentication
        mAuth = FirebaseAuth.getInstance();
        // btn
        btn_signIn= (Button) findViewById(R.id.signin_btn);
        // Edit Text
        signin_mail_edittext= (EditText) findViewById(R.id.mail_signin);
        signin_pass_edittext = (EditText) findViewById(R.id.password_signin);
        // OnClickListeners
        btn_signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("hola");
                if(!TextUtils.isEmpty(signin_mail_edittext.getText()) && !TextUtils.isEmpty( signin_pass_edittext.getText()))
                {
                    launchSignInProgressBar(v);
                    signIn(signin_mail_edittext.getText().toString(), signin_pass_edittext.getText().toString());
                    //startActivity(new Intent(signin_mailpassword.this, UserAuth_menu.class));
                }
            }
        });
    }

    // --------> [START auth_fui_create_intent] de FirebaseUI
    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.TwitterBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }
    // --------> [END auth_fui_create_intent]

    // -------->  onStart method [start]
    // When initializing your Activity, check to see if the user is currently signed in.
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    // -------->  onStart method [end]


    // -------->  updateUI method [start]
    private void updateUI(FirebaseUser user)
    {
        //hideProgressBar();
        if (user != null) {
            System.out.println("En metodo updateUI. User entro");

            // quit the progress bar
            progressSignInBarStatus =100;

            // launch the main menu if the authentication is successfully
            startActivity(new Intent(signin_mailpassword.this, UserAuth_menu.class));
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
            // quit the progress bar
            progressSignInBarStatus =100;

            System.out.println("En metodo updateUI. User no entro");
            /*
            mBinding.status.setText(R.string.signed_out);
            mBinding.detail.setText(null);

            mBinding.emailPasswordButtons.setVisibility(View.VISIBLE);
            mBinding.emailPasswordFields.setVisibility(View.VISIBLE);
            mBinding.signedInButtons.setVisibility(View.GONE);
            */
        }
    }
    // -------->  updateUI method [end]

    // -------->  createAccount method [start]
    //Create a new createAccount method which takes in an email address and password, validates
    // them and then creates a new user with the createUserWithEmailAndPassword method.
    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        // "validateForm" method doesn't work because it used a package "databinding" that i don't
        // have it and i don't know how to import it.
        /*
        if (!validateForm()) {
            return;
        }

         */

        //showProgressBar();

        // --------> [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(signin_mailpassword.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        //hideProgressBar();
                        // [END_EXCLUDE]
                    }
                });
        // --------> [END create_user_with_email]
    }
    // --------> createAccount method [end]


    // --------> validateForm method [start]
    // "validateForm" method doesn't work because it used a package "databinding" that i don't
    // have it and i don't know how to import it.
    /*
    private boolean validateForm() {
        boolean valid = true;

        String email = mBinding.fieldEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mBinding.fieldEmail.setError("Required.");
            valid = false;
        } else {
            mBinding.fieldEmail.setError(null);
        }

        String password = mBinding.fieldPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mBinding.fieldPassword.setError("Required.");
            valid = false;
        } else {
            mBinding.fieldPassword.setError(null);
        }

        return valid;
    }

     */
    // --------> validateForm method [end]

    // --------> signIn method [start]
    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);


        /*
        // "validateForm" method doesn't work because it used a package "databinding" that i don't
        // have it and i don't know how to import it.
        if (!validateForm()) {
            return;
        }

        showProgressBar();
        */

        // --------> [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            System.out.println("hola iniciaste sesion"); // comentario //  no entro
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // quit the progress bar
                            progressSignInBarStatus =100;
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(signin_mailpassword.this, "Error de autenticación.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                            // ### "checkForMultiFactorFailure" es un metodo no utilizado de la
                            // documentacion de Firebase para Firebase authentication
                            // [START_EXCLUDE]
                            // checkForMultiFactorFailure(task.getException());
                            // [END_EXCLUDE]
                        }

                        /*
                        // "validateForm" method doesn't work because it used a package "databinding" that i don't
                        // have it and i don't know how to import it.
                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            mBinding.status.setText(R.string.auth_failed);
                        }
                        hideProgressBar();
                        // [END_EXCLUDE]

                         */
                    }
                });
        // --------> [END sign_in_with_email]
    }
    // --------> signIn method [end]




    // <==========|| Progress bar ||==========> [BEGIN]
    public void launchSignInProgressBar(View v)
    {
        progressBar = new ProgressDialog(v.getContext());
        progressBar.setCancelable(false);
        progressBar.setMessage("Accediendo...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.show();
        progressSignInBarStatus = 0;

        new Thread(new Runnable() {
            public void run() {
                while (progressSignInBarStatus < 100) {
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

                if (progressSignInBarStatus >= 100) {

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