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

import sdcn.project.ecommerce_client_distributed.R;

import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UnavailableProductsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UnavailableProductsFragment extends Fragment {

    CharSequence mLabel;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private List<String> purchasedOutSockProductNameArray;
    private List<Integer> actualFireStoreStockProductArray;
    private List<Integer> purchasedAmountProductOutStockArray;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public UnavailableProductsFragment()
    {
        // Required empty public constructor
    }

    public UnavailableProductsFragment(
            List<String> purchasedOutSockProductNameArray,
            List<Integer> actualFireStoreStockProductArray,
            List<Integer> purchasedAmountProductOutStockArray)
    {
        this.purchasedOutSockProductNameArray=purchasedOutSockProductNameArray;
        this.actualFireStoreStockProductArray=actualFireStoreStockProductArray;
        this.purchasedAmountProductOutStockArray=purchasedAmountProductOutStockArray;
    // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UnavailableProductsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UnavailableProductsFragment newInstance(String param1, String param2)
    {
        UnavailableProductsFragment fragment = new UnavailableProductsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    )
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_unavailable_products,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Fragment me=this;

        // action to click on the RelativeLayout (outside the inner layout) and remove the fragment
        // from the activity
        RelativeLayout layout1 = (RelativeLayout) view.findViewById(R.id.RelLayout_noStockProducts);
        layout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do something
                System.out.println("RelativeLayout click");

                getFragmentManager()
                        .beginTransaction()
                        .remove(me)
                        .commit();
                // getActivity().getFragmentManager().popBackStack(); // it does nothing

                // getActivity().onBackPressed(); //  it takes me back

                // getExitTransition (); // do nothing
            }
        });


        System.out.println("parm value: ");
        // set a recyclerView

        RecyclerView RecyclerView_unavailableProduct;
        RecyclerView_unavailableProduct = view.findViewById(R.id.RecyclerView_unavailableProduct);


        CaptionedUnavailableProductAdapter Adapter = new CaptionedUnavailableProductAdapter(
                purchasedOutSockProductNameArray,
                actualFireStoreStockProductArray,
                purchasedAmountProductOutStockArray
        );

        RecyclerView_unavailableProduct.setAdapter(Adapter);
        // LinearLayoutManager is used to show the cardView in a list way
        LinearLayoutManager layoutManager;
        layoutManager=
            new LinearLayoutManager(Objects.requireNonNull(getActivity()).getApplicationContext());
        RecyclerView_unavailableProduct.setLayoutManager(layoutManager);

    }
}