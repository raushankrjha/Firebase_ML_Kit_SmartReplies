package com.example.lcomlkitauto.ui;

import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lcomlkitauto.R;
import com.example.lcomlkitauto.model.Message;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages = new ArrayList<>();
    private boolean simulateRemoteUser;

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout messageContainer;
        AppCompatTextView message;
        AppCompatImageView avatar;

        MessageViewHolder(View view) {
            super(view);

            messageContainer = (LinearLayout) view;
            message = view.findViewById(R.id.message);
            avatar = view.findViewById(R.id.avatar);
        }
    }

    public MessageAdapter() {
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public void setSimulateRemoteUser(boolean simulateRemoteUser) {
        this.simulateRemoteUser = simulateRemoteUser;
        notifyDataSetChanged();
    }

    public boolean getSimulateRemoteUser() {
        return simulateRemoteUser;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        GradientDrawable drawable = (GradientDrawable) holder.message.getBackground();

        boolean receivedMessage = !message.isLocalUser() && !simulateRemoteUser
                || message.isLocalUser() && simulateRemoteUser;

        if(simulateRemoteUser) {
            holder.avatar.setImageResource(R.drawable.avatar1);
        } else  {
            holder.avatar.setImageResource(R.drawable.avatar2);
        }

        if (receivedMessage) {
            holder.avatar.setVisibility(View.VISIBLE);
            holder.messageContainer.setGravity(Gravity.START);
            drawable.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.grey));
            holder.message.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.default_text_color));
        } else {
            holder.avatar.setVisibility(View.GONE);
            holder.messageContainer.setGravity(Gravity.END);
            drawable.setColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.orange));
            holder.message.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
        }

        holder.message.setText(message.getContent());
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }
}
