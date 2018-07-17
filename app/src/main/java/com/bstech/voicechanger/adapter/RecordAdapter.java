package com.bstech.voicechanger.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bstech.voicechanger.R;
import com.bstech.voicechanger.fragment.StudioFragment;
import com.bstech.voicechanger.model.Record;
import com.bstech.voicechanger.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {
    private List<Record> recordList;
    private StudioFragment context;
    private OnClick callback;

    public RecordAdapter(List<Record> recordList, StudioFragment context, OnClick callback) {
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
        holder.checkBox.setChecked(recordList.get(position).getCheck());
        // action mode
        if (context.isActionMode) {
            if (context.isSelectAll) {
                holder.checkBox.setChecked(recordList.get(position).getCheck());
                Log.e("xxx",recordList.get(position).getCheck()+"_____");
            }
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.ivMore.setVisibility(View.GONE);
        } else {
            holder.checkBox.setVisibility(View.GONE);
            holder.ivMore.setVisibility(View.VISIBLE);
        }
    }

    public void setFilter(List<Record> list) {
        recordList = new ArrayList<>();
        recordList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }


    public interface OnClick {
        void onClick(int index, boolean check);

        void onOptionClick(int index);

        boolean onLongClick(int index, boolean check);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNameRecord, tvTimeAdded, tvDuration, tvSize;
        private ImageView ivMore;
        private CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);

            ivMore = itemView.findViewById(R.id.iv_more);
            tvSize = itemView.findViewById(R.id.tv_file_size);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvNameRecord = itemView.findViewById(R.id.tv_name_song);
            tvTimeAdded = itemView.findViewById(R.id.tv_time_added);
            checkBox = itemView.findViewById(R.id.checkbox);

            checkBox.setOnCheckedChangeListener((compoundButton, b) -> {

                if (context.isActionMode) {
                    Record record = recordList.get(getAdapterPosition());
                    if (b) {

                        recordList.get(getAdapterPosition()).setCheck(true);

                        boolean isAll = true;

                        for (Record record1 : recordList) {
                            if (!record1.getCheck()) {
                                isAll = false;
                            }
                        }

                        if (isAll) context.isSelectAll = true;

                    } else {
                        context.isSelectAll = false;
                        recordList.get(getAdapterPosition()).setCheck(false);
                    }
                    context.prepareSelection(checkBox, getAdapterPosition());
                }
            });

            ivMore.setOnClickListener(view -> callback.onOptionClick(getAdapterPosition()));
            itemView.setOnClickListener(view -> {
                if (context.isActionMode) {

                    Record record = recordList.get(getAdapterPosition());

                    context.isSelectAll = false;

                    if (record.getCheck()) {
                        record.setCheck(false);
                        checkBox.setChecked(record.getCheck());
                    } else {
                        record.setCheck(true);
                        checkBox.setChecked(record.getCheck());
                    }

                    context.prepareSelection(checkBox, getAdapterPosition());

                } else {
                    callback.onClick(getAdapterPosition(), false);
                }
                context.isSelectAll = false;
            });
            itemView.setOnLongClickListener(view -> {
                if (context.isActionMode) {
                    Record record = recordList.get(getAdapterPosition());
                    context.isSelectAll = false;
                    if (record.getCheck()) {
                        record.setCheck(false);
                        checkBox.setChecked(record.getCheck());
                    } else {
                        record.setCheck(true);
                        checkBox.setChecked(record.getCheck());
                    }
                    context.prepareSelection(checkBox, getAdapterPosition());
                    context.isSelectAll = false;
                } else {
                    callback.onLongClick(getAdapterPosition(), false);
                }

                return true;
            });

        }
    }
}
