package sdcn.project.ecommerce_client_distributed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import sdcn.project.ecommerce_client_distributed.R;

//import com.google.firebase.quickstart.auth.R;


public class ChooserActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static String user_enter;

    private static final Class[] CLASSES_login = new Class[]{
            signin_mailpassword.class,
            /*
            GoogleSignInActivity.class,
            FacebookLoginActivity.class,
            PasswordlessActivity.class,
            PhoneAuthActivity.class,
            AnonymousAuthActivity.class,
            FirebaseUIActivity.class,
            CustomAuthActivity.class,
            GenericIdpActivity.class,
            MultiFactorActivity.class,
             */
    };

    private static final Class[] CLASSES_signup = new Class[]{
            signup_mailpassword.class,
            /*
            GoogleSignInActivity.class,
            FacebookLoginActivity.class,
            PasswordlessActivity.class,
            PhoneAuthActivity.class,
            AnonymousAuthActivity.class,
            FirebaseUIActivity.class,
            CustomAuthActivity.class,
            GenericIdpActivity.class,
            MultiFactorActivity.class,
             */
    };

    private static final int[] DESCRIPTION_IDS = new int[] {
            R.string.desc_emailpassword,
            //R.string.desc_firebase_ui,
            /*
            R.string.desc_google_sign_in,
            R.string.desc_facebook_login,
            R.string.desc_passwordless,
            R.string.desc_phone_auth,
            R.string.desc_anonymous_auth,
            R.string.desc_custom_auth,
            R.string.desc_generic_idp,
            R.string.desc_multi_factor,
            */
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooser);

        // Set up ListView and Adapter
        ListView listView = findViewById(R.id.listView);
        MyArrayAdapter adapter;
        //System.out.println("user_enter: "+user_enter);
        if (user_enter.equals("signin")){
            System.out.println(user_enter);
            adapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_2, CLASSES_login, "Iniciar sesión");
        }
        else {
            System.out.println(user_enter);
            adapter = new MyArrayAdapter(this, android.R.layout.simple_list_item_2, CLASSES_signup, "Registrarse");
        }
        adapter.setDescriptionIds(DESCRIPTION_IDS);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    // <============================|| onItemClick method ||============================> [BEGIN]
    // this method add a clickable listener on each listView item.
    // Each list view item comes from the arrays "CLASSES_login" or "CLASSES_signUp".
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Class clicked;
        if (user_enter.equals("signin")){
            clicked = CLASSES_login[position];
        }
        else{
            clicked = CLASSES_signup[position];
        }
        startActivity(new Intent(this, clicked));
    }
    // <============================|| onItemClick method ||============================> [END]





    public static class MyArrayAdapter extends ArrayAdapter<Class> {

        private Context mContext;
        private Class[] mClasses;
        private int[] mDescriptionIds;
        private String authDescription;

        public MyArrayAdapter(Context context, int resource, Class[] objects, String authDescription) {
            super(context, resource, objects);

            mContext = context;
            mClasses = objects;
            this.authDescription = authDescription;
        }




        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(android.R.layout.simple_list_item_2, null);
            }

            ((TextView) view.findViewById(android.R.id.text1)).setText(authDescription);
            ((TextView) view.findViewById(android.R.id.text2)).setText(mDescriptionIds[position]);

            return view;
        }

        public void setDescriptionIds(int[] descriptionIds) {
            mDescriptionIds = descriptionIds;
        }
    }
}