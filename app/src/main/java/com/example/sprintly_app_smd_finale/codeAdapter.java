package com.example.sprintly_app_smd_finale;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class codeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;
    private Context context;

    public codeAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Message.TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_message, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bot_message, parent, false);
            return new BotMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof UserMessageViewHolder) {
            UserMessageViewHolder userHolder = (UserMessageViewHolder) holder;
            userHolder.userMessageText.setText(message.getContent());
        } else if (holder instanceof BotMessageViewHolder) {
            BotMessageViewHolder botHolder = (BotMessageViewHolder) holder;
            botHolder.botMessageText.setText(message.getContent());

            if (message.hasCodeBlock()) {
                botHolder.codeBlockContainer.setVisibility(View.VISIBLE);
                botHolder.codeBlockText.setText(message.getCodeBlock());

                botHolder.copyCodeButton.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Code", message.getCodeBlock());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show();
                });
            } else {
                botHolder.codeBlockContainer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView userMessageText;

        UserMessageViewHolder(View itemView) {
            super(itemView);
            userMessageText = itemView.findViewById(R.id.userMessageText);
        }
    }

    static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        TextView botMessageText;
        TextView codeBlockText;
        LinearLayout codeBlockContainer;
        Button copyCodeButton;

        BotMessageViewHolder(View itemView) {
            super(itemView);
            botMessageText = itemView.findViewById(R.id.botMessageText);
            codeBlockText = itemView.findViewById(R.id.codeBlockText);
            codeBlockContainer = itemView.findViewById(R.id.codeBlockContainer);
            copyCodeButton = itemView.findViewById(R.id.copyCodeButton);
        }
    }
}