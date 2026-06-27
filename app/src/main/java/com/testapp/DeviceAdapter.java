package com.testapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.json.JSONObject;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.VH> {
    private List<JSONObject> devices;

    public DeviceAdapter(List<JSONObject> devices) {
        this.devices = devices;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH h, int pos) {
        JSONObject dev = devices.get(pos);
        try {
            h.id.setText(dev.getString("id"));
            h.model.setText(dev.getString("model") + " | Android " + dev.optString("android", "??"));
            h.last.setText("Last: " + dev.optString("last_seen", "unknown"));
        } catch (Exception e) {}
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void update(List<JSONObject> newDevices) {
        devices = newDevices;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView id, model, last;
        VH(View v) {
            super(v);
            id = v.findViewById(R.id.deviceId);
            model = v.findViewById(R.id.deviceModel);
            last = v.findViewById(R.id.deviceLastSeen);
        }
    }
}
