package com.bstech.voicechanger.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;
import java.util.List;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.model.Record;
import com.bstech.voicechanger.utils.Utils;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {
    private List<Record> recordList;
    private Context context;
    private OnClick callback;

    public RecordAdapter(List<Record> recordList, Context context, OnClick callback) {
        this.recordList = recordList;
        this.context = context;
        this.callback = callback;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_record, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Record record = recordList.get(position);

        String dateString = DateFormat.format("dd/MM/yyyy HH:mm ", new Date(record.getDateTime())).toString();

        holder.tvNameRecord.setText(record.getTitle());
        holder.tvDuration.setText(Utils.convertMillisecond(record.getDuration()));
        holder.tvSize.setText(record.getSize());
        holder.tvTimeAdded.setText(dateString);

    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }


    public interface OnClick {
        void onClick(int index);

        void onOptionClick(int index);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNameRecord, tvTimeAdded, tvDuration, tvSize;
        private ImageView ivMore;

        public ViewHolder(View itemView) {
            super(itemView);

            ivMore = itemView.findViewById(R.id.iv_more);
            tvSize = itemView.findViewById(R.id.tv_file_size);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvNameRecord = itemView.findViewById(R.id.tv_name_song);
            tvTimeAdded = itemView.findViewById(R.id.tv_time_added);

            ivMore.setOnClickListener(view -> callback.onOptionClick(getAdapterPosition()));
            itemView.setOnClickListener(view -> callback.onClick(getAdapterPosition()));

        }
    }
}
