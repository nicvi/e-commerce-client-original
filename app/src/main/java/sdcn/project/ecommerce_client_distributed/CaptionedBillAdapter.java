package sdcn.project.ecommerce_client_distributed;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import sdcn.project.ecommerce_client_distributed.R;

import org.jetbrains.annotations.NotNull;

public class CaptionedBillAdapter extends
        PagingDataAdapter<Bill, CaptionedBillAdapter.BillViewHolder>
{
    // Define Loading ViewType
    public static final int LOADING_ITEM = 0;
    // Define Movie ViewType
    public static final int BILL_ITEM = 1;

    //<==================|| DiffUtil.ItemCallback ||==================> [BEGIN]
    public CaptionedBillAdapter(@NotNull DiffUtil.ItemCallback<Bill> diffCallback)
    {
        super(diffCallback);
    }
    //<==================|| DiffUtil.ItemCallback ||==================> [END]






    @Override
    public int getItemViewType(int position) {
        // set ViewType
        return position == getItemCount() ? BILL_ITEM : LOADING_ITEM;
    }




    // <==================|| Define the adapter’s view holder ||==================> [BEGIN}
    // Our recycler view needs to display CardViews, so we specify that our ViewHolder contains
    // CardViews. If you want to display another type of data (views) in the recycler view, you
    // should define it here.
    public static class BillViewHolder extends RecyclerView.ViewHolder
    {
        // Define movie_item layout view binding
        private CardView cardView;


        public BillViewHolder(@NonNull CardView v)
        {
            super(v);
            // init binding
            this.cardView = v;
        }
    }
    // <==================|| Define the adapter’s view holder ||==================> [END]






    // <==================|| Override the onCreateViewHolder() method ||==================> [BEGIN]
    // "CaptionedImagesAdapter.ViewHolder" was changed by just "ViewHolder"
    // The method "onCreateViewHolder" gets called when the recycler view needs to create a view
    // holder. (one for each card)
    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Return MovieViewHolder
        return new BillViewHolder(
                (CardView) LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.card_captioned_info_bills, parent, false
                )
        );
    }
    //<==================|| Override onCreateViewHolder ||==================> [END]







    // <==========|| Interfaces. ||==========> [BEGIN]
    interface ListenerDeleteBill
    {
        // onLongClickRemove remove the bill
        void onLongClickRemove(String billId );
        // show a message indicating that the button should be held down
        void onClickDelete();
    }



    interface ListenerAccessBill
    {
        // access the bill
        void onClickAccessBill(Bill currentBill);
    }
    // <==========|| Interface. ||==========> [END]






    // <==========|| Add the listeners as a private variable. ||==========> [BEGIN]
    private ListenerDeleteBill listenerDeleteBill;
    private ListenerAccessBill listenerAccessBill;
    // <==========|| Add the listener as a private variable. ||==========> [END]







    // <==================|| Setting Listeners setters ||==================> [BEGIN]
    public void setListenerDeleteBill(ListenerDeleteBill listenerDeleteBill) {
        this.listenerDeleteBill = listenerDeleteBill;
    }

    public void setListenerAccessBill(ListenerAccessBill listenerAccessBill) {
        this.listenerAccessBill = listenerAccessBill;
    }
    // <==================|| Setting Listeners setters ||==================> [BEGIN]








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
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        //  TODO here
        System.out.println("currentBill: "+ getItem(position));

        // Get current movie
        Bill currentBill = getItem(position);

        System.out.println("currentBill: "+ currentBill);
        // Check for null
        if (currentBill != null) {
            // Set Views values
            // TODO, COMPLETE IT WITH VIEWS
            // The cardView hold the views that shows the information of each product in the collection
            // "Facturas"
            CardView cardView = holder.cardView;

            TextView textView_BoughtNameValueBills =
                    cardView.findViewById(R.id.textView_BoughtNameValueBills);
            textView_BoughtNameValueBills.setText(currentBill.getBillName());

            TextView textView_priceValueBills =
                    cardView.findViewById(R.id.textView_priceValueBills);
            String billPrice = "$ " + currentBill.getBillPrice();
            textView_priceValueBills.setText(billPrice);


            TextView textView_shippedValueBills =
                    cardView.findViewById(R.id.textView_shippedValueBills);
            String pickedUp="";
            if (currentBill.isPickedUp())
            {
                pickedUp = "Si";
            }
            else
            {
                pickedUp = "No";
                textView_shippedValueBills.setTextColor(Color.parseColor("#981C1C"));
            }
            textView_shippedValueBills.setText(pickedUp);

            TextView textView_billInfoDateValue =
                    cardView.findViewById(R.id.textView_billInfoDateValue);
            String[] dateTimeArray = currentBill.getBillDate().toString().split(" ");

            String month = dateTimeArray[1];
            String day = dateTimeArray[2];
            String time = dateTimeArray[3];
            String year = dateTimeArray[dateTimeArray.length-1];

            switch(month.toLowerCase())
            {
                case "jan":
                    month= "Enero";
                    break;
                case "apr":
                    month = "Abr";
                    break;
                case "feb":
                    month = "Febrero";
                    break;
                default:
                    break;
            }

            String dateTime = month + " " +day + ", " +year + " - " +time;

            textView_billInfoDateValue.setText(dateTime);


            // [___________|| set a purchased Amount of products ||____________] [<--BEGIN]
            // increase the amount of purchased products

            ImageButton imageButton_deleteBill = (ImageButton)
                    cardView.findViewById(R.id.imageButton_deleteBill);

            // Recycler view that respond to clicks, for this a listener was added.
            imageButton_deleteBill.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View V)
                {
                    if (listenerDeleteBill!=null)
                    {
                        listenerDeleteBill.onClickDelete();
                    }
                }
            });
            // increase the amount of purchased products
            imageButton_deleteBill.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View V)
                {
                    if (listenerDeleteBill!=null)
                    {
                        listenerDeleteBill.onLongClickRemove(currentBill.getBillID());
                    }
                    return true;
                }
            });


            cardView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if (listenerAccessBill!= null){
                        listenerAccessBill.onClickAccessBill(currentBill);
                    }
                }
            });
            // [___________|| set a purchased Amount of products ||____________] [<--END]
        }
        else{
            System.out.println("bill vacio");
        }
    }
    //<==================|| Add the data to the card views ||==================> [END]
}