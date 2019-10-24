package wvw.mobile.rules.fragment;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.view.View.OnClickListener;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import wvw.mobile.rules.ContactShowActivity;
import wvw.mobile.rules.R;
import wvw.mobile.rules.adapter.ContactAdapter;
import wvw.mobile.rules.adapter.ContactsAdapter;
import wvw.mobile.rules.dto.Contact;
import wvw.mobile.rules.util.RecyclerTouchListener;
import wvw.utils.MyRequest;
import wvw.utils.wvw.utils.rdf.Namespaces;
import wvw.utils.wvw.utils.rdf.Utilite;

import static wvw.mobile.rules.HomeActivity.CONTACTS_LIST;
import static wvw.mobile.rules.HomeActivity.CONTACTS_ST_LIST;
import static wvw.mobile.rules.HomeActivity.CONTACT_SELECT;

/**
 * A simple {@link Fragment} subclass.
 */
public class RelationFragment extends Fragment implements SearchView.OnQueryTextListener , AdapterView.OnItemSelectedListener{

    private Model modelOntologie  ;
    private InfModel modeleInf;
    private static String FILE_NAME_DATABASE="ont.owl";
    private File owlFile = null;
    private SearchView searchView;
    private ContactsAdapter mAdapter;
    private RecyclerView recyclerView;
    private String[] optionsLiens;

    private String selectedLien;
    private String selectedContact;
    private ImageView imageViewFind;
    private List<String> contactsSequences = new ArrayList<String>();
    private List<Contact> contactsFind = new ArrayList<>();
    private List<Contact> contacts = new ArrayList<>();
    private Button button;
    private Contact contactSelected;


    public RelationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.contactsSequences = (List<String>) getArguments().getSerializable(CONTACTS_ST_LIST);
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
        View view = inflater.inflate(R.layout.fragment_relation, container, false);
        Spinner spinnerLiens = (Spinner) view.findViewById(R.id.spinnerLiens);
        Spinner spinnerContacts = (Spinner) view.findViewById(R.id.spinnerContacts);

        searchView=(SearchView)view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(this);
        button = (Button) view.findViewById(R.id.buttonShowCustomDialog);


        recyclerView = (RecyclerView) view.findViewById(R.id.rvLiens);
        mAdapter = new ContactsAdapter(contactsFind);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.contacts_relation_types, android.R.layout.simple_spinner_item);
        // Specify layout to be used when list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Applying the adapter to our spinner
        spinnerLiens.setAdapter(adapter);

        ArrayAdapter adapterContact = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, contactsSequences);
        // Specify layout to be used when list of choices appears
        adapterContact.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Applying the adapter to our spinner
        spinnerContacts.setAdapter(adapterContact);

        spinnerLiens.setOnItemSelectedListener(this);

        spinnerContacts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedContact = contactsSequences.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        imageViewFind = view.findViewById(R.id.imageViewFind);

        optionsLiens = RelationFragment.this.getResources().getStringArray(R.array.contacts_relation_types);

        imageViewFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:: Afficher le recycleview Correspondant

                if(selectedLien!=null && !selectedLien.isEmpty() && selectedContact!=null && !selectedContact.isEmpty())
                {
                    findLinks();
                }
            }
        });


        // add button listener
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // custom dialog
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.custom_find_dialog);
                dialog.setTitle("Title...");

                // set the custom dialog components - text, image and button
                RecyclerView recyclerViewDialog =  dialog.findViewById(R.id.rvContactDialog);


                final ContactAdapter mAdapterDialog = new ContactAdapter(contacts);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
                recyclerViewDialog.setLayoutManager(mLayoutManager);
                recyclerViewDialog.setItemAnimator(new DefaultItemAnimator());
                recyclerViewDialog.setAdapter(mAdapterDialog);
                recyclerViewDialog.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerViewDialog, new RecyclerTouchListener.ClickListener() {
                    @Override
                    public void onClick(View view, final int position) {
                        contactSelected = contacts.get(position);
                        dialog.dismiss();
                    }

                    @Override
                    public void onLongClick(View view, final int position) {
                        dialog.dismiss();
                    }
                }));

                TextView text = (TextView) dialog.findViewById(R.id.text);
                SearchView searchViewDialog=dialog.findViewById(R.id.search_view_dialog);
                searchViewDialog.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        final List<Contact> filteredModelList = filterSansLien(contacts, newText);
                        mAdapterDialog.setFilter(filteredModelList);
                        return false;
                    }
                });
                text.setText("Selectionner contact!");

                Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
                // if button is clicked, close the custom dialog
                dialogButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        return view;
    }


    private void findLinks() {

        switch(selectedLien) {
            case "Père de":
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.pere);
                break; // break is optional
            case Namespaces.Contact.Liens.MERE_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.mere);
                break; // break is optional
            case Namespaces.Contact.Liens.FRERE_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.frere);
                break;
            case Namespaces.Contact.Liens.SOEUR_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.soeur);
                break;
            case Namespaces.Contact.Liens.EPOUX_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.epoux);
                break;
            case Namespaces.Contact.Liens.EPOUSE_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.epouse);
                break;
            case Namespaces.Contact.Liens.FILS_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.fils);
                break;
            case Namespaces.Contact.Liens.FILLE_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.fille);
                break;
            case Namespaces.Contact.Liens.AMI_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.ami);
                break;
            case Namespaces.Contact.Liens.COLLEGUE_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.collegue);
                break;
            case Namespaces.Contact.Liens.ONCLE_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.oncle);
                break;
            case Namespaces.Contact.Liens.TANTE_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.tante);
                break;
            case Namespaces.Contact.Liens.NEVEU_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.neveu);
                break;
            case Namespaces.Contact.Liens.NIECE_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.niece);
                break;
            case Namespaces.Contact.Liens.GRAND_PARENT_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.grandParent);
                break;
            case Namespaces.Contact.Liens.PETIT_ENFANT_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.petitEnfant);
                break;
            case Namespaces.Contact.Liens.COUSIN_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.cousin);
                break;
            case Namespaces.Contact.Liens.COUSINE_DE:
                rechercheLien(selectedContact, MyRequest.requete, MyRequest.cousine);
                break;
        }
    }

    public void rechercheLien(String lien,String requete1,String requete2){
        contactsFind.clear();
        Query query=QueryFactory.create(requete1);
        QueryExecution qexec = QueryExecutionFactory.create(query,modeleInf);
        try  {
            //++++++++++++++++++++DEBUT BLOC A SUPPRIMER+++++++++++++++//
            ResultSet resultat=qexec.execSelect();
            while(resultat.hasNext()){
                QuerySolution soln=resultat.nextSolution();

                Literal identifiant=soln.getLiteral("id");
                String id=identifiant.getString();
                Literal li_nom=soln.getLiteral("nom");
                String nom=li_nom.getString();
                Literal li_prenom=soln.getLiteral("prenom");
                String prenom=li_prenom.getString();
                Literal li_numero=soln.getLiteral("numero");
                String numero=li_numero.getString();

                //++++++++++++++++++++FIN BLOC A SUPPRIMER+++++++++++++++//
                // ICI je devrai passer le parametre lien val directement
                if((id+" "+nom+"  "+prenom+" "+numero).contains(lien)){
                    int val=Integer.parseInt(id);

                    //String requet=gestionRequete(requete2, val+"");
                    String requet= Utilite.gestionRequete(requete2, String.valueOf(val));

                    Query query1=QueryFactory.create(requet);
                    QueryExecution qexec1 = QueryExecutionFactory.create(query1,modeleInf);
                    try  {
                        ResultSet resultat1=qexec1.execSelect();
                        while(resultat1.hasNext()){
                            QuerySolution soln1=resultat1.nextSolution();

                            Contact contact = new Contact();

                            //contact.setRelationFind(selectedLien);
                            contact.setRelationFind(" ");
                            String numero1="", email1="", prenom1="";

                            Literal li_identifiant1=soln1.getLiteral("id");
                            String id1=li_identifiant1.getString();
                            contact.setId(id1);
                            Literal li_nom1=soln1.getLiteral("nom");
                            String nom1=li_nom1.getString();
                            contact.setName(nom1);
                            Literal li_prenom1=soln1.getLiteral("prenom");
                            if(li_prenom1!=null){
                                prenom1=li_prenom1.getString();
                                contact.setPrenom(prenom1);
                            }

                            Literal li_numero1=soln1.getLiteral("numero1");
                            if(li_numero1!=null){
                                 numero1=li_numero1.getString();
                                contact.setPhone(numero1);
                            }
                            Literal li_email1=soln1.getLiteral("email");
                            if(li_email1!=null){
                                email1=li_email1.getString();
                                contact.setEmail(email1);
                            }
                            contactsFind.add(contact);
                            System.out.println("UNE PERSONNE"+ id1+nom1+prenom1+numero1);

                        }
                    }finally{
                        qexec1.close();
                    }
                }
            }
        }finally{
            qexec.close();}
        mAdapter.notifyDataSetChanged();
    }

    //gestion des requetes
    public String gestionRequete(String requete,String id){
        return requete+" filter (xsd:integer(?identif)="+id+")}";
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        selectedLien = optionsLiens[position];

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }



    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<Contact> filteredModelList = filter(contactsFind, newText);
        mAdapter.setFilter(filteredModelList);
        return false;
    }

    private List<Contact> filter(List<Contact> models, String query) {
        query = query.toLowerCase();
        final List<Contact> filteredModelList = new ArrayList<>();
        for (Contact model : models) {
            final String text = model.getName().toLowerCase();
            //final String textp = model.getPhone().toLowerCase();
            final String textp = model.getPrenom().toLowerCase();
            final String textr = model.getRelationFind().toLowerCase();

            if (text.contains(query)) {
                filteredModelList.add(model);
            }else if (textp.contains(query)){
                filteredModelList.add(model);
            }else if(textr.contains(query)){
                filteredModelList.add(model);
            }

        }
        return filteredModelList;
    }

    private List<Contact> filterSansLien(List<Contact> models, String query) {
        query = query.toLowerCase();
        final List<Contact> filteredModelList = new ArrayList<>();
        for (Contact model : models) {
            final String text = model.getName().toLowerCase();
            final String textn = model.getPhone().toLowerCase();
            final String textp = model.getPrenom().toLowerCase();

            if (text.contains(query)) {
                filteredModelList.add(model);
            }else if (textp.contains(query)){
                filteredModelList.add(model);
            }else if(textn.contains(query)){
                filteredModelList.add(model);
            }

        }
        return filteredModelList;
    }

}