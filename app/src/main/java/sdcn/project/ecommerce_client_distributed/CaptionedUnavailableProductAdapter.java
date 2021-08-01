package sdcn.project.ecommerce_client_distributed;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import sdcn.project.ecommerce_client_distributed.R;

import java.util.List;

public class CaptionedUnavailableProductAdapter extends
        RecyclerView.Adapter<CaptionedUnavailableProductAdapter.ViewHolder>{

    // <==========|| Attributes. ||==========> [Begin]
    final private List<String> purchasedOutSockProductNameArray;
    final private List<Integer> actualFireStoreStockProductArray;
    final private List<Integer> purchasedAmountProductOutStockArray;
    // <==========|| Attributes. ||==========> [Begin]








    // <==========|| Make the constructor. ||==========> [Begin]
    // We’ll pass the data to the adapter using its constructor.

    public CaptionedUnavailableProductAdapter(
            List<String> purchasedOutSockProductNameArray,
            List<Integer> actualFireStoreStockProductArray,
            List<Integer> purchasedAmountProductOutStockArray)
    {
        this.purchasedOutSockProductNameArray = purchasedOutSockProductNameArray;
        this.actualFireStoreStockProductArray = actualFireStoreStockProductArray;
        this.purchasedAmountProductOutStockArray = purchasedAmountProductOutStockArray;
    }

    // <==========|| Make the constructor. ||==========> [END]









    // <==================|| Define the adapter’s view holder ||==================> [BEGIN}
    // Our recycler view needs to display CardViews, so we specify that our ViewHolder contains
    // CardViews. If you want to display another type of data (views) in the recycler view, you
    // should define it here.
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        public ViewHolder(CardView v)
        {
            super(v);
            cardView = v;
        }
    }
    // <==================|| Define the adapter’s view holder ||==================> [END]








    /* <==================|| implement the getItemCount() method ||==================> [BEGIN]
 The length of the captions array equals the number of data items in the recycler view so
 that number is returned by the getItemCount override method.
 */
    @Override
    public int getItemCount() {return this.actualFireStoreStockProductArray.size();}
    // <==================|| implement the getItemCount() method ||==================> [END]








    // <==================|| Override the onCreateViewHolder() method ||==================> [BEGIN]
    // "CaptionedImagesAdapter.ViewHolder" was changed by just "ViewHolder"
    // The method "onCreateViewHolder" gets called when the recycler view needs to create a view
    // holder. (one for each card)
    @NonNull
    @Override
    public CaptionedUnavailableProductAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType)
    {
        CardView cv =
                (CardView) LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.card_captioned_unavailable_product, parent, false
                );
        return new ViewHolder(cv);
    }
    // <==================|| Override the onCreateViewHolder() method ||==================> [END]








    /*
    <==================|| Add the data to the card views ||==================> [END]
    * The recycler view calls this method (onBindViewHolder) when it wants to use (or reuse) a
      view holder for a new piece of data.
    * This method populate the CardView’s ImageView and TextView with data.
    * The parameter "holder" it's an object of type "ViewHolder", an inner class previously created,
      the inner class ViewHolder method must hold the views that the cardView contain, those are:
        - the cardView per se.
        - a delete product button.
      So the holder is used to refer to those tow previously mentioned views.
     */
    @Override
    public void onBindViewHolder(
            @NonNull CaptionedUnavailableProductAdapter.ViewHolder holder, int position
    )
    {
        System.out.println("position: "+ position);

        System.out.println("name size: "+ purchasedOutSockProductNameArray.size());
        System.out.println("purchase stock size: "+ purchasedAmountProductOutStockArray.size());
        System.out.println("actual stock size: "+ actualFireStoreStockProductArray.size());

        System.out.println("productName value: "+ purchasedOutSockProductNameArray.get(position));
        String productName = purchasedOutSockProductNameArray.get(position);

        System.out.println("purchasedAmount value: "+ purchasedAmountProductOutStockArray.get(position));
        int purchasedAmount = purchasedAmountProductOutStockArray.get(position);

        System.out.println("actualStock value: " + actualFireStoreStockProductArray.get(position) );
        int actualStock = actualFireStoreStockProductArray.get(position);

        // The cardView hold the views that shows the information of each product in the collection
        // "Productos"
        CardView cardView = holder.cardView;

        // get the views based on the ones from the card View xml
        TextView unavailableProductAdapterValue;
        unavailableProductAdapterValue =cardView.findViewById(R.id.unavailableProductAdapterValue);
        TextView txt_desiredQuantityValue;
        txt_desiredQuantityValue= cardView.findViewById(R.id.txt_desiredQuantityValue);
        TextView textView_actualStockValue;
        textView_actualStockValue=cardView.findViewById(R.id.textView_actualStockValue);

        // set the views values based on the product and quantity.
        //System.out.println("productName value: "+ productName);
        //System.out.println("purchasedAmount value: "+purchasedAmount);

        unavailableProductAdapterValue.setText(productName);
        txt_desiredQuantityValue.setText(String.valueOf(purchasedAmount));
        textView_actualStockValue.setText(String.valueOf(actualStock));
    }
    // <==================|| Add the data to the card views ||==================> [END]








}