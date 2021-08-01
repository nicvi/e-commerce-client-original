package sdcn.project.ecommerce_client_distributed;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import sdcn.project.ecommerce_client_distributed.R;

import java.text.DecimalFormat;
import java.util.List;

import static java.lang.Integer.parseInt;

public class CaptionedPurchaseAdapter extends
        RecyclerView.Adapter<CaptionedPurchaseAdapter.ViewHolder>
{





    // <==========|| Attributes. ||==========> [Begin]
    private final List<Producto> purchaseProductArray;
    private final List<Integer> purchaseQuantityArray;
    private List<Boolean> uploadItArrayList;
    // <==========|| Attributes. ||==========> [Begin]





    // <==========|| Make the constructor. ||==========> [Begin]
    // We’ll pass the data to the adapter using its constructor.
    public CaptionedPurchaseAdapter(
            List<Producto> purchaseProductArray,
            List<Integer> purchaseQuantityArray,
            List<Boolean> uploadItArrayList
    )
    {
        this.purchaseProductArray = purchaseProductArray;
        this.purchaseQuantityArray = purchaseQuantityArray;
        this.uploadItArrayList = uploadItArrayList;
    }
    // <==========|| Make the constructor. ||==========> [END]







    // <==========|| Interfaces. ||==========> [BEGIN]
    interface ListenerRemove
    {
        void onLongClickRemove(int position );
        void onClickDelete();
    }

    interface ListenerButtonDecrease
    {
        void onClickButtonDecrease(
            TextView TextView_quantityPurchased,
            int position
        );
    }

    interface ListenerButtonIncrease
    {
        void onClickButtonIncrease(
            TextView TextView_quantityPurchased,
            TextView textView_availableProducts,
            int position
        );
    }
    // <==========|| Interface. ||==========> [END]






    // <==========|| Add the listeners as a private variable. ||==========> [BEGIN]
    private ListenerRemove listenerRemove;
    private ListenerButtonIncrease listenerButtonIncrease;
    private ListenerButtonDecrease listenerButtonDecrease;
    // <==========|| Add the listener as a private variable. ||==========> [END]








    // <==================|| Setting Listeners setters ||==================> [BEGIN]
    public void setListenerRemove(CaptionedPurchaseAdapter.ListenerRemove listenerRemove)
    {
        this.listenerRemove = listenerRemove;
    }

    public void setListenerButtonIncrease(
            CaptionedPurchaseAdapter.ListenerButtonIncrease listenerButtonIncrease
    )
    {
        this.listenerButtonIncrease = listenerButtonIncrease;
    }

    public void setListenerButtonDecrease(
            CaptionedPurchaseAdapter.ListenerButtonDecrease listenerButtonDecrease
    )
    {
        this.listenerButtonDecrease = listenerButtonDecrease;
    }
    // <==================|| Setting Listeners ||==================> [END]








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






    // <==================|| implement the getItemCount() method ||==================> [BEGIN]

    // The length of the captions array equals the number of data items in the recycler view so
    // that number is returned by the getItemCount override method.
    @Override
    public int getItemCount() {return this.purchaseProductArray.size();   }
    // <==================|| implement the getItemCount() method ||==================> [END]





    // <==================|| Override the onCreateViewHolder() method ||==================> [BEGIN]
    // "CaptionedImagesAdapter.ViewHolder" was changed by just "ViewHolder"
    // The method "onCreateViewHolder" gets called when the recycler view needs to create a view
    // holder. (one for each card)
    @NonNull
    @Override
    public CaptionedPurchaseAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType)
    {
        // "card_captioned_image" is the LayoutInflator to turn the layout into a CardView.
        // This is nearly identical to code you've already seen in the onCreateView() of fragments.
        CardView cv =
                (CardView) LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.card_captioned_image_trolley, parent, false
                );

        return new ViewHolder(cv);
    }
    // <==================|| Override the onCreateViewHolder() method ||==================> [END]






    /*
    <==================|| Add the data to the card views ||==================> [BEGIN]
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
            @NonNull CaptionedPurchaseAdapter.ViewHolder holder,
            final int position
    )
    {
        //final Down_Up_loader down_up_loader = new Down_Up_loader ();

        final Producto product = purchaseProductArray.get(position);

        int quantity = purchaseQuantityArray.get(position);

        // The cardView hold the views that shows the information of each product in the collection
        // "Productos"
        CardView cardView = holder.cardView;

        setImagesViewHolder(cardView, product, position );

    }
    // <==================|| Add the data to the card views ||==================> [END]




    // public void getFireStoreProduct(){ }


    public void setImagesViewHolder(CardView cardView, Producto product, int position){
        // instance an ImageView and bind it with the one in the xml file
        ImageView imageView = (ImageView)cardView.findViewById(R.id.imageView_productToBuy);
        System.out.println("product In adapter: "+ product.toString());
        // Create a drawable with each images element (Image ID) in the imageIds array.
        getImageGlide(  imageView,  product.getUrl().get(0),  cardView);
        // A ContentDescription allows you to associate a textual description to the view.
        // For example it associate an Image of an Apple with the text "apple" that is store
        // and coincide it with array's element from "captions"
        imageView.setContentDescription(product.getName());

        // CardView UI, text views
        cardViewSetTextUIViews(cardView, product, position);

        // call method that handle buttons
        buttons(cardView,position );
    }



    // <==================|| CardView UI, text views  ||==================> [BEGIN]
    public void cardViewSetTextUIViews(
            CardView cardView,
            Producto product,
            int position
    )
    {
        String productName;
        float productPrice, productPriceNew;
        boolean productOffer;
        int cantidadDisponible, maxLength;


        maxLength = 47;
        cantidadDisponible= product.getCantidad_disponible();
        productOffer = product.isOferta();
        productName = product.getName();
        productPrice  = product.getPrecio();
        productPriceNew = product.getPrecioNuevo();

        if (!uploadItArrayList.get(position))
        {
            System.out.println("Color.parseColor(\"#BAF8BF62\"): " + Color.parseColor("#BAF8BF62"));
            LinearLayout linearLayout_btnCardPurchaseNow =
                    cardView.findViewById(R.id.linearLayout_btnCardPurchaseNow);

            linearLayout_btnCardPurchaseNow
                    .setBackgroundColor(Color.parseColor("#BAF8BF62"));

            LinearLayout linearLayout_availabilityCardPurchaseNow =
                    cardView.findViewById(R.id.linearLayout_availabilityCardPurchaseNow);

            linearLayout_availabilityCardPurchaseNow
                    .setBackgroundColor(Color.parseColor("#BAF8BF62"));
        }

        /* BASIC TEMPLATE  ------> [BEGIN] */

        // ill show the product name according with the number of characters allowed, if the product
        //  name uses more characters than the allowed then ill add some ellipsis to the product
        //  name.
        // create the TextView para el nombre del producto
        TextView textView = (TextView)cardView.findViewById(R.id.txt_productToBuy);
        //
        if (productName.length() > maxLength)
        {
            productName = productName.substring(0,maxLength-4) + "....";
        }

        // Display the product name in the TextView.
        textView.setText(productName);

        // if there are an offer price, i'll show the offer price and the no offer price on CardView
        TextView textviewNewPrice =
                (TextView) cardView.findViewById(R.id.textView_previousPriceToBuy);
        // TODO [______________________ Product offered [TRUE] ___________________________] [BEGIN]
        if (productOffer)
        {
            // TODO [______________________ previous price [ENABLE] ______________________________]
            textviewNewPrice.
                    setPaintFlags(textviewNewPrice.getPaintFlags()| Paint.STRIKE_THRU_TEXT_FLAG);
            String txtPrecioAnterior = "$ "+ String.valueOf(productPrice);
            textviewNewPrice.
                    setText( txtPrecioAnterior );

            // TODO [_______________________ Actual price [ENABLE] ______________________________]
            // create the TextView para el precion del producto
            TextView textView_precio = (TextView)cardView.findViewById(R.id.textView_actualPriceToBuy);
            String txtPrecio = "$ "+ String.valueOf(productPriceNew);
            // Display the caption in the TextView.
            textView_precio.setText(txtPrecio);

            // TODO [_______________________ off price [ENABLE] _______________________________]
            TextView textView_off_price = (TextView)cardView.findViewById(R.id.textView_offPriceToBuy);
            float productOffPriceFloat;
            productOffPriceFloat =
                    ((productPrice - productPriceNew) / productPrice )*100;

            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            String productOffPriceStr = decimalFormat.format(productOffPriceFloat) + "% descuento";
            textView_off_price.setText(productOffPriceStr);
        }
        // TODO [______________________ Product offered [TRUE] ___________________________] [END]

        // TODO [______________________ Product offered [FALSE] __________________________] [BEGIN]
        else{
            // TODO [______________________ previous price [DISABLE] ____________________________]
            textviewNewPrice.setVisibility(View.INVISIBLE);

            // TODO [_______________________ off price [DISABLE] _____________________________]
            TextView textView_off_price = (TextView)cardView.findViewById(R.id.textView_offPriceToBuy);
            textView_off_price.setVisibility(View.INVISIBLE);

            // TODO [_______________________ Actual price [ENABLE] ____________________________]
            // create the TextView para el precion del producto
            TextView textView_precio = (TextView)cardView.findViewById(R.id.textView_actualPriceToBuy);
            String txtPrecio = "$ "+ String.valueOf(productPrice);
            // Display the caption in the TextView.
            textView_precio.setText(String.valueOf(txtPrecio));
        }
        // TODO [______________________ Product offered [FALSE] __________________________] [END]

        // TODO [_______________________ Availability [ENABLE] ____________________________]
        TextView textView_availability =
                (TextView)cardView.findViewById(R.id.textView_availabilityToBuy);
        if (cantidadDisponible <= 0)
        {
            textView_availability.setText(R.string.unavailableTag);
            textView_availability.setTextColor(Color.parseColor("#981C1C"));


        }
        /* BASIC TEMPLATE  [END] <------ */

    }
    // <==================|| CardView UI format  ||==================> [END]

    // <==================|| buttons ||==================> [END]
    // Recycler view that respond to clicks, for this a listener was added.
    public void buttons(
            CardView cardView,
            final int position
            )
    {

        int quantityCount = purchaseQuantityArray.get(position);

        final TextView TextView_quantityPurchased = (TextView)
                cardView.findViewById(R.id.TextView_quantityPurchased);
        TextView_quantityPurchased.setText(String.valueOf(quantityCount));

        final TextView textView_availableProducts  =
                cardView.findViewById(R.id.textView_availableProducts);
        textView_availableProducts.setText(String.valueOf(purchaseProductArray.get(position).getCantidad_disponible()));

        if (purchaseProductArray.get(position).getCantidad_disponible()<=0)
        {
            textView_availableProducts.setVisibility(View.GONE);
        }


        // [___________|| set a purchased Amount of products ||____________] [<--BEGIN]
        // increase the amount of purchased products

        ImageButton imageButton_increaseToBuyNow = (ImageButton)
                cardView.findViewById(R.id.imageButton_increaseToBuyNow);
        ImageButton imageButton_decreaseToBuyNow = (ImageButton)
                cardView.findViewById(R.id.imageButton_decreaseToBuyNow);


        // Recycler view that respond to clicks, for this a listener was added.
        imageButton_increaseToBuyNow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View V)
            {
                if (listenerButtonIncrease!=null){
                    listenerButtonIncrease.onClickButtonIncrease(
                            TextView_quantityPurchased,
                            textView_availableProducts,
                            position
                    );
                }
            }
        });
        // increase the amount of purchased products
        imageButton_decreaseToBuyNow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View V)
            {
                if (listenerButtonDecrease!=null){
                    listenerButtonDecrease.onClickButtonDecrease(
                            TextView_quantityPurchased,
                            position
                    );
                }

            }
        });
        // [___________|| set a purchased Amount of products ||____________] [<--END]


        // REMOVE BUTTON
        Button button_deleteProduct = cardView.findViewById(R.id.button_deleteProduct);
        button_deleteProduct.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v){
                if (listenerRemove!= null)
                {
                    listenerRemove.onLongClickRemove(
                            position
                            /*
                            product.getName(),
                            product.getUrl().get(0)

                             */
                    );
                }
                return true;
            }
        });
        button_deleteProduct.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (listenerRemove!= null)
                {
                    listenerRemove.onClickDelete();
                }
            }
        });

    }



    // <====|| Use glide to bind the image from firebase database with the cardView ||=======> [END]
    // the context could be, the activity or a explicit view as a cardView in this scenario.
    public void getImageGlide( ImageView imageView, String url_image, CardView cardView){
        Glide.with(cardView.getContext())
                .load(url_image)
                .into(imageView);
    }
    // <======|| Use glide to bind the image from firebase database with the cardView ||=====> [END]


}
