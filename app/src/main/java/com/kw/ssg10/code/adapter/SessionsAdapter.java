package com.kw.ssg10.code.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kw.ssg10.R;
import com.kw.ssg10.code.utils.SessionConfig;

import java.util.ArrayList;

// 主界面会话列表适配器

public class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.ViewHolder> {
    private static final String TAG = "kwdebug";
    private ArrayList<SessionConfig> sessions;
    private OnItemClickListener listenerRoot, listenerShare, listenerEdit;
    private OnItemLongClickListener longClickListenerDelete;

    public SessionsAdapter(ArrayList<SessionConfig> sessions, OnItemClickListener l1, OnItemClickListener l2, OnItemClickListener l3, OnItemLongClickListener l4) {
        this.sessions = sessions;
        listenerRoot = l1;
        listenerShare = l2;
        listenerEdit = l3;
        longClickListenerDelete = l4;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_session, viewGroup, false));
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        String host;
        if (sessions.get(i).getCSMode() == 0) {
            host = sessions.get(i).getServerHost() + ":" + sessions.get(i).getServerPort();
        } else {
            host = sessions.get(i).getListenHost() + ":" + sessions.get(i).getListenPort();
        }
        viewHolder.tvHost.setText(host);
        // 点击事件
        viewHolder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenerRoot.onClick(i);
            }
        });
        viewHolder.ivShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenerShare.onClick(i);
            }
        });
        viewHolder.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listenerEdit.onClick(i);
            }
        });

        // 长按事件
        viewHolder.root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickListenerDelete.onLongClick(i, v);
                return true; // 屏蔽普通点击事件
            }
        });
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout root;
        TextView tvHost;
        ImageView ivShare, ivEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.layout_item_session_root);
            tvHost = itemView.findViewById(R.id.tv_host);
            ivShare = itemView.findViewById(R.id.iv_share);
            ivEdit = itemView.findViewById(R.id.iv_edit);
        }
    }

    public interface OnItemClickListener {
        void onClick(int pos);
    }

    public interface OnItemLongClickListener {
        void onLongClick(int pos, View v);
    }
}
