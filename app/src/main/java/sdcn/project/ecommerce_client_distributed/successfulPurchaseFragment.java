package sdcn.project.ecommerce_client_distributed;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import sdcn.project.ecommerce_client_distributed.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link successfulPurchaseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class successfulPurchaseFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private List<Producto> purchaseProductArray;
    private List<Integer> purchaseQuantityArray;


    public successfulPurchaseFragment(
            List<Producto> purchaseProductArray,
            List<Integer> purchaseQuantityArray
    )
    {
        // Required empty public constructor
        this.purchaseProductArray = purchaseProductArray;
        this.purchaseQuantityArray = purchaseQuantityArray;
    }


    public successfulPurchaseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment successfulPurchaseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static successfulPurchaseFragment newInstance(String param1, String param2) {
        successfulPurchaseFragment fragment = new successfulPurchaseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_successful_purchase, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Fragment me=this;

        // action to click on the RelativeLayout (outside the inner layout) and remove the fragment
        // from the activity
        RelativeLayout layout1 =  view.findViewById(R.id.RelativeLayout_successfulPurchase);
        layout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do something
                System.out.println("RelativeLayout click");

                getFragmentManager()
                        .beginTransaction()
                        .remove(me)
                        .commit();
            }
        });

        // set Txt views values
        TextView textView_billPriceFragment = view.findViewById(R.id.textView_billPriceFragment);

        TextView textView_totalBillProductsFragment =
                view.findViewById(R.id.textView_totalBillProductsFragment);

        String[] arrayValues = getTotalProducts();

        String strTotalProductsBill = "Total ( "+ arrayValues[0] +" items)";
        String strBillPrice = "$ "+ arrayValues[1] ;

        textView_billPriceFragment.setText(strBillPrice);
        textView_totalBillProductsFragment.setText(strTotalProductsBill);

        // set RecyclerView
        RecyclerView RecyclerView_successfulPurchase =
                view.findViewById(R.id.RecyclerView_successfulPurchase);

        // create an trolleyPurchase Adapter to fill the RecyclerView
        CaptionedTrolleyPurchasedAdapter Adapter = new CaptionedTrolleyPurchasedAdapter(
                purchaseProductArray,
                purchaseQuantityArray
        );

        //Set the adapter to the recyclerView
        RecyclerView_successfulPurchase.setAdapter(Adapter);


        // LinearLayoutManager is used to show the cardView in a list way
        LinearLayoutManager layoutManager=
            new LinearLayoutManager(requireActivity().getApplicationContext());
        RecyclerView_successfulPurchase.setLayoutManager(layoutManager);

    }

    public String[] getTotalProducts()
    {
        int totalQuantityProducts = 0;
        float totalPrice = 0;
        String [] array =new String[2];
        int flag = 0;

        for (int quantity : purchaseQuantityArray)
        {
            totalQuantityProducts += quantity;

            if (purchaseProductArray.get(flag).isOferta())
            {
                totalPrice += quantity * purchaseProductArray.get(flag).getPrecioNuevo();
            }
            else
            {
                totalPrice += quantity * purchaseProductArray.get(flag).getPrecio();
            }

            flag+=1;
        }

        // give the format of two decimals for the price
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setDecimalFormatSymbols(symbols);

        array[0]= String.valueOf(totalQuantityProducts);
        array[1]= decimalFormat.format(totalPrice);
        return array;
    }



}