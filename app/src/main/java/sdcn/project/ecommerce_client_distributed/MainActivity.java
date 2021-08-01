package sdcn.project.ecommerce_client_distributed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import sdcn.project.ecommerce_client_distributed.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// TODO Note: You should not use the com.android.support and com.google.android.material dependencies in your app at the same time.

public class MainActivity extends AppCompatActivity
{
    private Button signin_btn, signup_btn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize Firebase Auth // variable took from Firebase authentication
        mAuth = FirebaseAuth.getInstance();

        // incializo y lanzo  activity [comienzo]
        signin_btn = (Button) findViewById(R.id.signin_btn);
        signup_btn= (Button) findViewById(R.id.signup_btn);

        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("hola signin");
                ChooserActivity.user_enter= "signin";
                startActivity(new Intent(MainActivity.this, signin_mailpassword.class));
            }
        });
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("hola signin");
                ChooserActivity.user_enter= "signup";
                startActivity(new Intent (MainActivity.this, signup_mailpassword.class));
            }
        });
        // incializo y lanzo  activity [finalizo]
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    // -------->  updateUI method [start]
    private void updateUI(FirebaseUser user) {
        //hideProgressBar();
        if (user != null) {
            System.out.println("En metodo updateUI. User no entro desde MainActivity");
            startActivity(new Intent(MainActivity.this, UserAuth_menu.class));
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

}