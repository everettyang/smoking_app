package edu.tamu.geoinnovation.fpx.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import edu.tamu.geoinnovation.fpx.Modules.SensorModule;
import edu.tamu.geoinnovation.fpx.R;


public class SensorDataAdapter extends RecyclerView.Adapter<SensorDataAdapter.ViewHolder>  {
    private static final String TAG = "SensorDataAdapter";
    private ArrayList<SensorModule> mDataset;
    private static ClickListener clickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView timeStamp;
        public TextView sensorType;
        public TextView sensorValue;

        public ViewHolder(View v) {
            super(v);
            timeStamp = (TextView) v.findViewById(R.id.adapter_timestamp);
            sensorType = (TextView) v.findViewById(R.id.adapter_sensor_type);
            sensorValue = (TextView) v.findViewById(R.id.adapter_sensor_value);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public SensorDataAdapter() {
        this.mDataset = new ArrayList<>();
    }

    public SensorDataAdapter(ArrayList<SensorModule> response) {
        this.mDataset = response;
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        SensorDataAdapter.clickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_events, parent, false);

        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
//        SensorModule event = mDataset.get(position);
//        holder.time.setText(event.Time);
//        holder.name.setText(event.EventName);
//        StringBuilder tagsConcat = new StringBuilder();
//        if (event.LocationRoom.length() > 0) {
//            tagsConcat.append(event.LocationBuilding).append(" | ").append(event.LocationRoom);
//        } else {
//            tagsConcat.append(event.LocationBuilding);
//        }
//
//        holder.loc.setText(tagsConcat.toString());
    }



    @Override
    public int getItemCount() {
        return mDataset.size();
    }


}
