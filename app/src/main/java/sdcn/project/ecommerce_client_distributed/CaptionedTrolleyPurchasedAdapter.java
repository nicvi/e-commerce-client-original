package sdcn.project.ecommerce_client_distributed;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import sdcn.project.ecommerce_client_distributed.R;

import java.util.List;

public class CaptionedTrolleyPurchasedAdapter extends
        RecyclerView.Adapter<CaptionedTrolleyPurchasedAdapter.ViewHolder>{

    // <==================|| Arguments ||==================> [BEGIN]
    private final List<Producto> purchaseProductArray;
    private final List<Integer> purchaseQuantityArray;
    // <==================|| Arguments ||==================> [BEGIN]











    // <==========|| Make the constructor. ||==========> [Begin]
    // We’ll pass the data to the adapter using its constructor.
    public CaptionedTrolleyPurchasedAdapter(
            List<Producto> purchaseProductArray
            , List<Integer> purchaseQuantityArray
    )
    {
        this.purchaseProductArray = purchaseProductArray;
        this.purchaseQuantityArray = purchaseQuantityArray;
    }
    // <==========|| Make the constructor. ||==========> [END]








    // <==================|| Define the adapter’s view holder ||==================> [BEGIN}
    // Our recycler view needs to display CardViews, so we specify that our ViewHolder contains
    // CardViews. If you want to display another type of data (views) in the recycler view, you
    // should define it here.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        public ViewHolder(CardView v) {
            super(v);
            cardView=v;
        }
    }
    // <==================|| Define the adapter’s view holder ||==================> [END]









    // <==================|| Override the onCreateViewHolder() method ||==================> [BEGIN]
    // "CaptionedImagesAdapter.ViewHolder" was changed by just "ViewHolder"
    // The method "onCreateViewHolder" gets called when the recycler view needs to create a view
    // holder. (one for each card)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // "card_captioned_image" is the LayoutInflator to turn the layout into a CardView.
        // This is nearly identical to code you've already seen in the onCreateView() of fragments.
        CardView cv =
                (CardView) LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.card_captioned_purchased_trolley, parent, false
                );

        return new ViewHolder(cv);
    }
    // <==================|| Override the onCreateViewHolder() method ||==================> [END]










    // <==================|| implement the getItemCount() method ||==================> [BEGIN]
    // The length of the captions array equals the number of data items in the recycler view so
    // that number is returned by the getItemCount override method.
    @Override
    public int getItemCount() { return this.purchaseProductArray.size();    }
    // <==================|| implement the getItemCount() method ||==================> [END]











    /*<==================|| Add the data to the card views ||==================> [BEGIN]
    * The recycler view calls this method (onBindViewHolder) when it wants to use (or reuse) a
      view holder for a new piece of data.
    * This method populate the CardView’s ImageView and TextView with data.
    * The parameter "holder" it's an object of type "ViewHolder", an inner class previously created,
      the inner class ViewHolder method must hold the views that the cardView contain, those are:
        - the cardView per se.
        - a delete product button.
      So the holder is used to refer to those tow previously mentioned views.*/
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Producto product = purchaseProductArray.get(position);

        int quantityPurchased = purchaseQuantityArray.get(position);

        // The cardView hold the views that shows the information of each product in the collection
        // "Productos"
        CardView cardView = holder.cardView;

        // init viewers
        TextView textView_productNameOnBill;
        textView_productNameOnBill=cardView.findViewById(R.id.textView_productNameOnBill);

        TextView textView_productCostOnBill;
        textView_productCostOnBill=cardView.findViewById(R.id.textView_productCostOnBill);

        TextView textView_productAmongOnBill;
        textView_productAmongOnBill=cardView.findViewById(R.id.textView_productAmongOnBill);

        TextView textView_productUnitOnBill;
        textView_productUnitOnBill=cardView.findViewById(R.id.textView_productUnitOnBill);

        // set Viewers
        textView_productNameOnBill.setText(product.getName());

        String productCost = "$ "+product.getPrecio();
        textView_productCostOnBill.setText(productCost);

        textView_productAmongOnBill.setText(String.valueOf(quantityPurchased));
        textView_productUnitOnBill.setText(product.getUnidad());
    }
    //<==================|| Add the data to the card views ||==================> [END]
}
