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

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.MyViewHolder> implements FastScroller.SectionIndexer{

    private List<Contact> contactList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, phone;

        public MyViewHolder(View view) {
            super(view);

            name = (TextView) view.findViewById(R.id.name);
            phone = (TextView) view.findViewById(R.id.phone);
        }
    }


    public ContactAdapter(List<Contact> contacts) {

        this.contactList = contacts;
    }

    @Override
    public ContactAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.liste_contacts, parent, false);

        return new ContactAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ContactAdapter.MyViewHolder holder, int position) {
        Contact contact = contactList.get(position);

        holder.name.setText(contact.getName());
        holder.phone.setText(contact.getPhone());


        //holder.id.setText(""+Chapitre.getId()); // cela permet de caster le int en String
        holder.itemView.setTag(contact);
        //if ((position % 2) == 0) {
        //holder.itemView.setBackgroundResource(R.color.buttonAccentColor);

        /**
         * Ici on pourra mettre tous type de modification concernant l'affichage d'un  itemview
         * qui dans notre cas est un
         */

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

    @Override
    public String getSectionText(int position) {
        //return getItem(position).getIndex();
        return contactList.get(position).getName().substring(0,1);
    }

}