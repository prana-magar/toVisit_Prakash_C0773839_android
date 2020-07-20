package com.example.tovisit_prakash_c0773839_android;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.example.tovisit_prakash_c0773839_android.adapters.PlaceAdapter;
import com.example.tovisit_prakash_c0773839_android.database.LocalCacheManager;
import com.example.tovisit_prakash_c0773839_android.database.Place;
import com.example.tovisit_prakash_c0773839_android.interfaces.DeletePlaceInterface;
import com.example.tovisit_prakash_c0773839_android.interfaces.MainInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Objects;

import static androidx.core.view.ViewCompat.getTransitionName;

public class MainActivity extends AppCompatActivity implements DeletePlaceInterface, MainInterface {



    FloatingActionButton btn;
    RecyclerView placeRv;

    PlaceAdapter adapter;
    List<Place> placeList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.fabAddPlace);
        placeRv = findViewById(R.id.rvPlaces);

        setTitle("Fav Places");


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                startActivity(intent);
            }
        });


        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT| ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                LocalCacheManager.getInstance(MainActivity.this).deletePlace(MainActivity.this,adapter.getPlaceAt(viewHolder.getAdapterPosition()));
                Toast.makeText(MainActivity.this,"Deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(placeRv);

        placeRv.setLayoutManager(new LinearLayoutManager(this));


    }



    private void loadPlaces(){

        //Call Method to get Notes
        LocalCacheManager.getInstance(this).getPlaces(this);


    }
    @Override
    protected void onResume() {
        super.onResume();
        loadPlaces();
    }

    @Override
    public void onPlaceDeleted() {
        loadPlaces();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        SearchView searchView = (SearchView) menu.findItem( R.id.action_search).getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }



    @Override
    public void onPlacesLoaded(final List<Place> places) {
        placeList = places;

        if(placeList.size() == 0){
            onDataNotAvailable();
        }else {
            adapter = new PlaceAdapter(this, places);
            adapter.setOnItemClickListner(new PlaceAdapter.OnItemClickListner() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onItemClick(Place place, View view) {
                    Intent intent = new Intent(MainActivity.this,DetailActivity.class);
                    intent.putExtra("placeObj",place);
                    intent.putExtra("transition_name", getTransitionName(view));

                    ActivityOptionsCompat options;
                    options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            MainActivity.this,
                            view,
                            Objects.requireNonNull(getTransitionName(view)));

                    startActivity(intent, options.toBundle());
                }
            });
            placeRv.setAdapter(adapter);
        }
    }

    @Override
    public void onPlaceAdded() {

    }

    @Override
    public void onDataNotAvailable() {

    }
}