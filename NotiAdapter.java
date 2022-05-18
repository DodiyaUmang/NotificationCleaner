package com.heaven.notificationcleaner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NotiAdapter extends RecyclerView.Adapter<NotiAdapter.NotiViewHolder> {
    ArrayList<NotiModel> notiList;
    Context context;

    public NotiAdapter(ArrayList<NotiModel> notiList, Context context) {
        this.notiList = notiList;
        this.context = context;
    }

    @NonNull
    @Override
    public NotiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_noti, parent, false);
        NotiViewHolder holder = new NotiViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull NotiAdapter.NotiViewHolder holder, int position) {
        holder.tv_pkg.setText(notiList.get(position).getPkg());
        holder.tv_text.setText(notiList.get(position).getText());
        holder.tv_title.setText(notiList.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return notiList.size();
    }

    public class NotiViewHolder extends RecyclerView.ViewHolder {
        TextView tv_pkg,tv_text,tv_title;
        public NotiViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_pkg = itemView.findViewById(R.id.tv_pkg);
            tv_title = itemView.findViewById(R.id.tv_title);
            tv_text = itemView.findViewById(R.id.tv_text);
        }
    }
}
