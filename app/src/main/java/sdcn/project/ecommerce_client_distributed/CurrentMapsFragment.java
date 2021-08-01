package sdcn.project.ecommerce_client_distributed;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import sdcn.project.ecommerce_client_distributed.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CurrentMapsFragment extends Fragment {

    private LatLng latLong;
    private static final int DEFAULT_ZOOM = 16;
    private TextView TextView_address;
    private Button btn__confirmLocation, btn_changeLocation;

    ///////////////////////////////////
    public interface onSomeEventListener {
        public void someEvent( LatLng latLong);
    }

    onSomeEventListener someEventListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            someEventListener = (onSomeEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement onSomeEventListener");
        }
    }

    ////////////////////////////

    public CurrentMapsFragment (){

    }

    public CurrentMapsFragment (LatLng latLong){
        this.latLong = latLong;
    }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            LatLng mMarker = latLong;
            googleMap.addMarker(new MarkerOptions().position(mMarker).title("My Marker"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarker,DEFAULT_ZOOM));
        }
    };

    private void AddressGiver(LatLng mMarker) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());

        addresses = geocoder.getFromLocation(mMarker.latitude, mMarker.longitude, 3); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0);

        TextView_address.setText(address);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                                        .findFragmentById(R.id.map_currentPlace);
        if (mapFragment != null)
        {
            mapFragment.getMapAsync(callback);
        }

        // Initializing the views of the xml fragment
        TextView_address = (TextView) view.findViewById(R.id.textView_addressSelected);
        btn_changeLocation = (Button) view.findViewById(R.id.button_changeLocation);
        btn__confirmLocation = (Button) view.findViewById(R.id.button_confirmLocation);

        // "AddressGiver" method gives the address of the marker place selected by the user
        try {
            AddressGiver(latLong);
        } catch (IOException e) {
            e.printStackTrace();
        }

        btn_changeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("btn_changeLocation");
                requireActivity().getSupportFragmentManager().beginTransaction().remove(CurrentMapsFragment.this).commit();


            }
        });

        btn__confirmLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                someEventListener.someEvent( latLong);
                requireActivity().onBackPressed();
            }
        });

    }
}