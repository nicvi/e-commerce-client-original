package sdcn.project.ecommerce_client_distributed;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

// [TODO] the idea of this class doesn't work to well cause the  fireStore method works asyncronously
public class Down_Up_loader extends AppCompatActivity {

    // [____________|| Parameters ||____________] [<--BEGIN]
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Producto product = new Producto ();
    // [____________|| Parameters ||____________] [<--END]


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    public Down_Up_loader() { }


    public void createProduct(String documentID)
    {
        //final Producto[] product = {null};
        System.out.println("documentID: "+ documentID);
        DocumentReference docRef;
        docRef = db.collection("Productos")
                .document(documentID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                System.out.println("productFromLoaderClass Previous if ");
                if(task.isSuccessful()){
                    // i obtain the product
                    //product = task.getResult().toObject(Producto.class);
                    setProduct(task.getResult().toObject(Producto.class));

                    System.out.println("product obtained: " + product);
                }
                else
                {
                    System.out.println("product not obtained: " + product);
                    Log.w(
                            "constructProduct(): ",
                            "Error getting documents.",
                            task.getException()
                    );
                }
                System.out.println("product obtained, after if-else: " + product);
            }
        });
        System.out.println("product obtained, at the edge of the method: " + product);
        //return product[0];
    }


    public FirebaseFirestore getDb() {
        return db;
    }

    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }

    public Producto getProduct() {
        return product;
    }

    public void setProduct(Producto product) {
        this.product = product;
    }
}
