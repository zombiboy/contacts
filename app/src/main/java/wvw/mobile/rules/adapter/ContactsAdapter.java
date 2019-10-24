package wvw.mobile.rules.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.l4digital.fastscroll.FastScroller;

import java.util.ArrayList;
import java.util.List;

import wvw.mobile.rules.R;
import wvw.mobile.rules.dto.Contact;
import wvw.mobile.rules.util.RoundLetterView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.MyViewHolder> {
    private List<Contact> contactList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private RoundLetterView vRoundLetterView;
        public TextView name, lien;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            lien = (TextView) view.findViewById(R.id.lien);
            this.vRoundLetterView = (RoundLetterView) view.findViewById(R.id.vRoundLetterView);
        }
    }
    public ContactsAdapter(List<Contact> contacts) {

        this.contactList = contacts;
    }

    @Override
    public ContactsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.liste_contacts_2, parent, false);

        return new ContactsAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ContactsAdapter.MyViewHolder holder, int position) {
        Contact contact = contactList.get(position);

        StringBuilder fullName = new StringBuilder("");
        fullName.append(contact.getName());
        if(contact.getPrenom() !=null) {
            fullName.append(" ");
            fullName.append(contact.getPrenom());
        }
        holder.name.setText(fullName);
        holder.lien.setText(contact.getRelationFind());
        holder.vRoundLetterView.setTitleText(String.valueOf(contact.getName().charAt(0)));
        holder.vRoundLetterView.setBackgroundColor(contact.getBackgroundColor());
        holder.itemView.setTag(contact);

    }

    public void setFilter(List<Contact> contacts) {
        contactList = new ArrayList<>();
        contactList.addAll(contacts);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }


}
