package sdcn.project.ecommerce_client_distributed


import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

// TODO if you have an "error: cannot find symbol" while trying to execute a Kotlin class in a Java
//  project It looks like your kotlin code isn't compiled during build process.
//  I have this problem trying to call a object "FirestorePagingSource" that was defined in
//  a kotlin class.

class FirestorePagingSource(
        private val db: FirebaseFirestore,
        private val currentFirebaseUser: FirebaseUser
) : PagingSource<QuerySnapshot, Bill>() {

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, Bill>
    {
        return try {
            // Step 1
            val currentPage =
                    params.key ?: db.collection("Usuarios")
                    .document(this.currentFirebaseUser.uid)
                    .collection("Compras")
                    .orderBy("billDate", Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .await()

            // Step 2
            val lastDocumentSnapshot = currentPage.documents[currentPage.size() - 1]

            // Step 3
            val nextPage =
                    db.collection("Usuarios")
                    .document(this.currentFirebaseUser.uid)
                    .collection("Compras")
                    .orderBy("billDate", Query.Direction.DESCENDING)
                    .limit(10)
                    .startAfter(lastDocumentSnapshot)
                    .get()
                    .await()


            //currentPage.toObjects(Bill::class.java).isEmpty()

            println("hello from kotlin, all well")

            println("currentPage.isEmpty: " + currentPage.isEmpty)

            // Step 4
            LoadResult.Page(
                    data = currentPage.toObjects(Bill::class.java),
                    prevKey = null,
                    nextKey = nextPage
            )
        }
        catch (e: Exception)
        {
            println("hello from kotlin, all not well")
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<QuerySnapshot, Bill>): QuerySnapshot? {
        TODO("Not yet implemented")
    }
}