package sdcn.project.ecommerce_client_distributed;

import androidx.annotation.NonNull;
import androidx.paging.PagingState;
import androidx.paging.rxjava2.RxPagingSource;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import io.reactivex.Single;

public class FireStorePagingSource extends RxPagingSource<QuerySnapshot, Bill> {
    // <=================|| Variables ||=================> [BEGIN]
    @NonNull
    private FirebaseFirestore fireStoreDatabase; // TODO here--> "FirebaseFirestore"
    @NonNull
    private FirebaseUser currentFirebaseUser;

    private QuerySnapshot documentSnapshotsRetrieved;
    private QuerySnapshot nextDocumentSnapshotsRetrieved;
    private List<Bill> billList;

    private DocumentSnapshot lastVisibleNext;
    // <=================|| Variables ||=================> [END]






    // <=================|| Constructor ||=================> [BEGIN]
    FireStorePagingSource(
            @NonNull FirebaseFirestore fireStoreDatabase, // TODO here--> "FirebaseFirestore"
            @NonNull FirebaseUser currentFirebaseUser)
    {
        this.fireStoreDatabase = fireStoreDatabase;
        this.currentFirebaseUser = currentFirebaseUser;
    }
    // <=================|| Constructor ||=================> [END]






    // <=================|| loadSingle ||=================> [BEGIN]

    /*
    * The LoadParams object contains information about the load operation TO BE performed.
    * This includes the key TO BE loaded and the number of items to be loaded.
    *
    * (LoadParams will send a "signal")
    * */

    @NotNull
    @Override
    public Single<LoadResult<QuerySnapshot, Bill>> loadSingle
    (@NotNull LoadParams<QuerySnapshot> params)
    {
        // Start refresh at page 1 if undefined.

        nextDocumentSnapshotsRetrieved = null;
        nextDocumentSnapshotsRetrieved = params.getKey();
        if (nextDocumentSnapshotsRetrieved == null)
        {
            fireStoreDatabase.collection("Usuarios")
                .document(this.currentFirebaseUser.getUid())
                .collection("Compras")
                .limit(10)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
                {
                    @Override
                    public void onSuccess(QuerySnapshot documentSnapshots)
                    {
                        // make sure that the bill list is empty.
                        billList.clear();

                        nextDocumentSnapshotsRetrieved = documentSnapshots;

                        // Loop through the documentSnapshots list and turn each element into a
                        // Bill object then store them into an Bill List.
                        for (int i = 0; i < nextDocumentSnapshotsRetrieved.size(); i++)
                        {
                            // here fill the Bill list
                            Bill bill =
                                    nextDocumentSnapshotsRetrieved
                                            .getDocuments()
                                            .get( i)
                                            .toObject(Bill.class);

                            billList.add(bill);
                        }
                        // set the last visible docuement
                        lastVisibleNext = nextDocumentSnapshotsRetrieved.getDocuments()
                                .get(nextDocumentSnapshotsRetrieved.size() -1);
                    }
                });
        }
        // ======================================================================
        // make sure that the bill list is empty.
        billList.clear();
        // Loop through the documentSnapshots list and turn each element into a
        // Bill object then store them into an Bill List.
        for (int i = 0; i < nextDocumentSnapshotsRetrieved.size(); i++)
        {
            // here fill the Bill list
            Bill bill =
                    nextDocumentSnapshotsRetrieved
                            .getDocuments()
                            .get( i)
                            .toObject(Bill.class);

            billList.add(bill);
        }
        // set the last visible docuement
        lastVisibleNext = nextDocumentSnapshotsRetrieved.getDocuments()
                .get(nextDocumentSnapshotsRetrieved.size() -1);
        // ======================================================================


        // return a Bill object retrieved from a fireStore call ??

        return  null;



    }
    // <=================|| loadSingle ||=================> [END]






    // <=================|| getRefreshKey ||=================> [BEGIN]
    @Nullable
    @Override
    public QuerySnapshot getRefreshKey(@NotNull PagingState<QuerySnapshot, Bill> pagingState) {
        return null;
    }
    // <=================|| getRefreshKey ||=================> [END]





    // <=================|| toLoadResult ||=================> [BEGIN]
    /*
    * The LoadResult object contains the result of the load operation.
    *  LoadResult is a sealed class that takes one of two forms, depending on whether the load()
    *  call succeeded:
    *       - If the load is successful, return a LoadResult.Page object.
    *       - If the load is not successful, return a LoadResult.Error object.
    *
    * (LoadParams will retrieve a "signal")
    * */
    private LoadResult<QuerySnapshot, Bill> toLoadResult(
            @NonNull List<Bill>  listBill, @NonNull QuerySnapshot querySnapshot)
    { // TODO here--> "SearchBillResponse"

        //return null;

        fireStoreDatabase.collection("Usuarios")
            .document(this.currentFirebaseUser.getUid())
            .collection("Compras")
            .limit(10)
            .startAfter()
            .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>()
            {
                @Override
                public void onSuccess(QuerySnapshot documentSnapshots)
                {
                    // make sure that the bill list is empty.
                    billList.clear();
                    // Loop through the documentSnapshots list and turn each element into a Bill object
                    // then store them into an Bill List
                    for (int i = 0; i < documentSnapshots.size(); i++)
                    {
                        // here fill the Bill list
                        Bill bill =
                                documentSnapshots
                                        .getDocuments()
                                        .get( i)
                                        .toObject(Bill.class);

                        billList.add(bill);
                    }

                    lastVisibleNext = documentSnapshots.getDocuments()
                            .get(documentSnapshots.size() -1);
                }
            });



        return new LoadResult.Page(

            listBill,
            null, // Only paging forward.
            nextDocumentSnapshotsRetrieved // TODO here--> "getNextPageNumber"
            );

    }
    // <=================|| toLoadResult ||=================> [END]

}
