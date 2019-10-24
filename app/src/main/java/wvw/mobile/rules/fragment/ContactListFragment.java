package wvw.mobile.rules.fragment;


import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import java.util.ArrayList;
import java.util.List;

import wvw.mobile.rules.ContactEditActivity;
import wvw.mobile.rules.ContactShowActivity;
import wvw.mobile.rules.R;
import wvw.mobile.rules.adapter.ContactAdapter;
import wvw.mobile.rules.dto.Contact;
import wvw.mobile.rules.util.RecyclerTouchListener;
import wvw.utils.MyRequest;
import wvw.utils.wvw.utils.rdf.Utilite;

import static wvw.mobile.rules.HomeActivity.CONTACTS_LIST;
import static wvw.mobile.rules.HomeActivity.CONTACT_SELECT;
import static wvw.utils.MyRequest.requeteRemplirCombobox;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactListFragment extends Fragment implements SearchView.OnQueryTextListener {
    private Model modelOntologie  ;
    private InfModel modeleInf;
    private static String FILE_NAME_DATABASE="ont.owl";
    private File owlFile = null;
    private SearchView searchView;
    private ContactAdapter mAdapter;
    private List<Contact> contacts = new ArrayList<>();
    private FastScrollRecyclerView recyclerView;

    //private static final String ARG_PARAM2 = "param2";


    public ContactListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.contacts = (List<Contact>) getArguments().getSerializable(CONTACTS_LIST);
        }
        owlFile=new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),""+FILE_NAME_DATABASE);
        modelOntologie  = Utilite.readModel(owlFile);
        modeleInf= Utilite.inference(modelOntologie,getContext().getAssets());

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
                contact=findKnowsPerson(contact);
                //TODO:contact show
                Intent intent = new Intent(getContext(), ContactShowActivity.class);
                intent.putExtra(CONTACT_SELECT, contact);
                startActivity(intent);


            }

            @Override
            public void onLongClick(View view, final int position) {

                //Rubrique rubrique = rubriqueList.get(position);

            }
        }));

        return view;
    }

    private Contact findKnowsPerson(Contact selectContact) {

        List<Contact> contactList = new ArrayList<>();
        String reqDesPersonneEnRelation=Utilite.gestionRequete(MyRequest.relation, selectContact.getId());
        Query query1=QueryFactory.create(reqDesPersonneEnRelation);
        QueryExecution qexec1 = QueryExecutionFactory.create(query1,modeleInf);
        try  {
            ResultSet resultat1 = qexec1.execSelect();
            while (resultat1.hasNext()) {
                QuerySolution soln1 = resultat1.nextSolution();

                Literal iden = soln1.getLiteral("id");
                String idTrouver = iden.getString();
                Literal nom = soln1.getLiteral("nom");
                String nomTrouver = nom.getString();
                Literal prenom = soln1.getLiteral("prenom");
                String prenomTrouver = prenom.getString();
                //TODO:: ADD
                System.out.println("RELATION AVEC "+idTrouver+" "+nomTrouver+" "+prenomTrouver);
                Contact c= new Contact();
                c.setId(idTrouver);
                c.setName(nomTrouver);
                c.setPrenom(prenomTrouver);
                c.setRelationFind("Cousin de");
                contactList.add(c);
            }
        }finally{
            qexec1.close();}
        selectContact.setContactsLiens(contactList);
        return selectContact;
    }




    private boolean isfileExistInExternalDir(){

        File outFile = new File(getContext().getExternalFilesDir(null), "ont.owl");
        return outFile.exists();
    }

    private void copyAssets() {
        AssetManager assetManager = getContext().getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(getContext().getExternalFilesDir(null), filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public void editContact(){
        Intent intent = new Intent(getContext(), ContactEditActivity.class);
        String sessionId="OHD";
        intent.putExtra("EXTRA_SESSION_ID", sessionId);
        startActivity(intent);
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
