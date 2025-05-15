package com.example.sprintly_app_smd_finale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private static List<Contact> contactList;
    private static ContactsAdapter instance;
    Context context;
    private final chat_interface chatting_interface;

    public ContactsAdapter(Context context, List<Contact> contactList, chat_interface chatting_interface) {
        this.context = context;
        this.chatting_interface = chatting_interface;
        ContactsAdapter.contactList = contactList; // Now it's static
        instance = this;
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        public TextView contactName;
        public ImageView imgview;
        public TextView numView;

        public ContactViewHolder(View view, chat_interface chatting_interface) {
            super(view);
            contactName = view.findViewById(R.id.contactName);
            imgview = view.findViewById(R.id.contactImage);
            numView = view.findViewById(R.id.contactNum);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (chatting_interface != null) {
                        int position = getAbsoluteAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            chatting_interface.on_item_click(position);
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view, chatting_interface);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.contactName.setText(contactList.get(position).getName());
        holder.imgview.setImageResource(contactList.get(position).getImage());
        holder.numView.setText(contactList.get(position).getNumber());
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    // ✅ Static method to update list and refresh the adapter
    public static void updateList(List<Contact> filteredContacts) {
        if (contactList != null) {
            contactList.clear();
            contactList.addAll(filteredContacts);
            if (instance != null) {
                instance.notifyDataSetChanged();
            }
        }
    }
}
