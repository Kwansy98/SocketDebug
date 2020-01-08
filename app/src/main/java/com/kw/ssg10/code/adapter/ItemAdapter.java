package com.kw.ssg10.code.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kw.ssg10.R;
import com.kw.ssg10.code.session.task.LogTask;
import com.kw.ssg10.code.session.task.ReceiveStringTask;
import com.kw.ssg10.code.session.task.SendStringTask;
import com.kw.ssg10.code.session.task.SessionTask;
import com.kw.ssg10.code.session.task.UploadFileTask;

import java.util.ArrayList;


public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "kwdebug";

    private ArrayList<SessionTask> tasks;

    public ItemAdapter(ArrayList<SessionTask> tasks) {
        this.tasks = tasks;
    }

    private static final int TYPE_RECV_STRING = 0;
    private static final int TYPE_SEND_STRING = 1;
    private static final int TYPE_LOG = 2;
    private static final int TYPE_DOWNLOAD_FILE = 3;
    private static final int TYPE_UPLOAD_FILE = 4;
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case TYPE_RECV_STRING:
                return new ViewHolderTextGet(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_recv_text, viewGroup, false));
            case TYPE_SEND_STRING:
                return new ViewHolderTextSend(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_send_text, viewGroup, false));
            case TYPE_LOG:
                return new ViewHolderTextLog(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_log_text, viewGroup, false));
            case TYPE_UPLOAD_FILE:
                return new ViewHolderFileSend(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_send_file, viewGroup, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int pos) {
        switch (viewHolder.getItemViewType()) {
            case TYPE_RECV_STRING:
                ViewHolderTextGet viewHolderTextGet = (ViewHolderTextGet) viewHolder;
                viewHolderTextGet.tvGet.setText(tasks.get(pos).showText());
                break;
            case TYPE_SEND_STRING:
                ViewHolderTextSend viewHolderTextSend = (ViewHolderTextSend) viewHolder;
                viewHolderTextSend.tvSend.setText(tasks.get(pos).showText());
                break;
            case TYPE_LOG:
                ViewHolderTextLog viewHolderTextLog = (ViewHolderTextLog) viewHolder;
                viewHolderTextLog.tvLog.setText(tasks.get(pos).showText());
                break;
            case TYPE_UPLOAD_FILE:
                ViewHolderFileSend viewHolderFileSend = (ViewHolderFileSend) viewHolder;
                viewHolderFileSend.tvFileSend.setText(tasks.get(pos).showText());
                break;
            default:

        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class ViewHolderTextGet extends RecyclerView.ViewHolder {
        TextView tvGet;
        public ViewHolderTextGet(@NonNull View itemView) {
            super(itemView);
            tvGet = itemView.findViewById(R.id.tv_text_get);
        }
    }

    class ViewHolderTextSend extends RecyclerView.ViewHolder {
        TextView tvSend;
        public ViewHolderTextSend(@NonNull View itemView) {
            super(itemView);
            tvSend = itemView.findViewById(R.id.tv_text_send);
        }
    }

    class ViewHolderTextLog extends RecyclerView.ViewHolder {
        TextView tvLog;
        public ViewHolderTextLog(@NonNull View itemView) {
            super(itemView);
            tvLog = itemView.findViewById(R.id.tv_text_log);
        }
    }

    class ViewHolderFileSend extends RecyclerView.ViewHolder {
        TextView tvFileSend;
        public ViewHolderFileSend(@NonNull View itemView) {
            super(itemView);
            tvFileSend = itemView.findViewById(R.id.tv_file_send);
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (tasks.get(position) instanceof ReceiveStringTask) return TYPE_RECV_STRING;
        if (tasks.get(position) instanceof SendStringTask) return TYPE_SEND_STRING;
        if (tasks.get(position) instanceof LogTask) return TYPE_LOG;
        if (tasks.get(position) instanceof UploadFileTask) return TYPE_UPLOAD_FILE;
        else return -1;
    }


}
