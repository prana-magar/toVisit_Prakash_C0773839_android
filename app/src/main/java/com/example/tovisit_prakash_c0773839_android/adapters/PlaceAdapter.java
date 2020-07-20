package com.example.tovisit_prakash_c0773839_android.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;


import com.example.tovisit_prakash_c0773839_android.R;
import com.example.tovisit_prakash_c0773839_android.database.Place;

import java.util.ArrayList;
import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.NotesViewHolder> implements Filterable {

    Context context;
    List<Place> placeList = new ArrayList<>();
    private List<Place> placeListFull = new ArrayList<>();

    private OnItemClickListner listner;


    public PlaceAdapter(Context context, List<Place> placeList) {
        this.context = context;
        this.placeList = placeList;
        placeListFull = new ArrayList<>(placeList);

    }

    public Place getPlaceAt(int position){
        return placeList.get(position);
    }

    @Override
    public NotesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.row_place,parent,false);
        NotesViewHolder nvh = new NotesViewHolder(v);
        return nvh;
    }

    @SuppressLint({"SetTextI18n", "ResourceAsColor"})
    @Override
    public void onBindViewHolder(NotesViewHolder holder, int position) {
        holder.tvPlaceName.setText(placeList.get(position).getPlaceName());
        System.out.println(placeList.get(position).getLatCord() );

        if(placeList.get(position).getVisited()){
            holder.tvPlaceAddress.setText("Visited");
        }
        else{
            holder.tvPlaceAddress.setText("");
        }

        if(placeList.get(position).getVisited()){
            holder.itemView.setBackgroundColor(R.color.quantum_cyan100);
        }
        ViewCompat.setTransitionName(holder.tvPlaceName, Integer.toString(placeList.get(position).getId()));
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    @Override
    public Filter getFilter() {
        return noteListFilter;
    }

    private Filter noteListFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<Place> filterList = new ArrayList<>();
            if(charSequence == null || charSequence.length() == 0){
                filterList.addAll(placeListFull);
            }
            else{
                String filterPattern = charSequence.toString().trim().toLowerCase();
                for(Place place: placeListFull){
                    if(place.getPlaceName().toLowerCase().contains(filterPattern) ){
                        filterList.add(place);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filterList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {


            placeList.clear();
            placeList.addAll((List)filterResults.values);
            notifyDataSetChanged();
        }
    };


    public class NotesViewHolder extends RecyclerView.ViewHolder{

        TextView tvPlaceName,tvPlaceAddress;
        public NotesViewHolder(View itemView) {
            super(itemView);

            tvPlaceName = itemView.findViewById(R.id.tvPlaceName);
            tvPlaceAddress = itemView.findViewById(R.id.tvPlaceAddress);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position  =  getAdapterPosition();

                    if(listner != null && position != RecyclerView.NO_POSITION){
                        listner.onItemClick(placeList.get(position),tvPlaceName);
                    }

                }
            });

        }
    }

    public  interface  OnItemClickListner {
        void onItemClick(Place contact, View view);
    }

    public void setOnItemClickListner(OnItemClickListner onItemClickListner){
        this.listner = onItemClickListner;
    }

}
