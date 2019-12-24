package wvw.mobile.rules.fragment;


import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.l4digital.fastscroll.FastScrollRecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import wvw.mobile.rules.ContactEditActivity;
import wvw.mobile.rules.ContactListActivity;
import wvw.mobile.rules.ContactShowActivity;
import wvw.mobile.rules.R;
import wvw.mobile.rules.adapter.ContactAdapter;
import wvw.mobile.rules.dto.Contact;
import wvw.mobile.rules.util.RecyclerTouchListener;
import wvw.utils.MyRequest;
import wvw.utils.wvw.utils.rdf.Utilite;

import static wvw.mobile.rules.ContactShowActivity.CONTACT_SELECT;
import static wvw.mobile.rules.HomeActivity.CONTACTS_LIST;
import static wvw.utils.MyRequest.requeteRemplirCombobox;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactListFragment extends Fragment implements SearchView.OnQueryTextListener {

    private SearchView searchView;
    private ContactAdapter mAdapter;
    private List<Contact> contacts = new ArrayList<>();
    private FastScrollRecyclerView recyclerView;
    private BottomSheetDialog mBottomSheetDialog;



    public ContactListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.contacts = (List<Contact>) getArguments().getSerializable(CONTACTS_LIST);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        recyclerView = (FastScrollRecyclerView) view.findViewById(R.id.recycler_view);

        searchView=(SearchView)view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(this);

        mAdapter = new ContactAdapter(contacts);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {

                Contact contact = contacts.get(position);
                //contact=findKnowsPerson(contact); //Prend beaucoup de temps avant de venir
                Intent intent = new Intent(getContext(), ContactShowActivity.class);
                intent.putExtra(CONTACT_SELECT, contact);
                intent.putExtra(CONTACTS_LIST, (Serializable) contacts);
                startActivity(intent);


            }

            @Override
            public void onLongClick(View view, final int position) {

                final Contact contact = contacts.get(position);
                mBottomSheetDialog = new BottomSheetDialog(getActivity());
                View sheetView = getActivity().getLayoutInflater().inflate(R.layout.dialog_op_bottom_sheet, null);
                mBottomSheetDialog.setContentView(sheetView);
                TextView opTitile = sheetView.findViewById(R.id.title_op_bottom_shett_title);
                LinearLayout edit = (LinearLayout) sheetView.findViewById(R.id.op_bottom_sheet_edit);
                LinearLayout delete = (LinearLayout) sheetView.findViewById(R.id.op_bottom_sheet_delete);
                opTitile.setText(contact.getName());

                edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Edit code here;
                        Intent intent = new Intent(getContext(), ContactEditActivity.class);
                        intent.putExtra(CONTACT_SELECT, contact);
                        startActivity(intent);
                        mBottomSheetDialog.dismiss();
                    }
                });

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Delete code here;
                        mBottomSheetDialog.dismiss();
                    }
                });
                mBottomSheetDialog.show();



            }
        }));

        return view;
    }



    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Contact> filteredModelList = filter(contacts, newText);
        mAdapter.setFilter(filteredModelList);
        return false;
    }

    private List<Contact> filter(List<Contact> models, String query) {
        query = query.toLowerCase();
        final List<Contact> filteredModelList = new ArrayList<>();
        for (Contact model : models) {
            final String text = model.getName().toLowerCase();
            final String textp = model.getPhone().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }else if (textp.contains(query)){
                filteredModelList.add(model);
            }
        }
        System.out.println("LE query est::"+query);
        return filteredModelList;
    }


}
