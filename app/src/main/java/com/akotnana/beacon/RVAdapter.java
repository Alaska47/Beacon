package com.akotnana.beacon;

/**
 * Created by anees on 8/6/2016.
 */
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.BeaconViewHolder> {

    public static class BeaconViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView BeaconTitle;
        TextView BeaconTags;
        ImageView BeaconPhoto;

        BeaconViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            BeaconTitle = (TextView)itemView.findViewById(R.id.beacon_title);
            BeaconTags = (TextView)itemView.findViewById(R.id.beacon_tags);
            BeaconPhoto = (ImageView)itemView.findViewById(R.id.beacon_photo);
        }
    }

    List<Beacon> beacons;

    RVAdapter(List<Beacon> beacons){
        this.beacons = beacons;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public BeaconViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        BeaconViewHolder pvh = new BeaconViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(BeaconViewHolder BeaconViewHolder, int i) {
        BeaconViewHolder.BeaconTitle.setText(beacons.get(i).title);
        BeaconViewHolder.BeaconTags.setText(beacons.get(i).tags);
        BeaconViewHolder.BeaconPhoto.setImageResource(beacons.get(i).photoId);
    }

    @Override
    public int getItemCount() {
        return beacons.size();
    }
}
