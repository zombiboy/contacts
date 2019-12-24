package wvw.mobile.rules;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;

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

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import wvw.mobile.rules.adapter.ContactAdapter;
import wvw.mobile.rules.dto.Contact;
import wvw.mobile.rules.util.RecyclerTouchListener;
import wvw.utils.MyRequest;
import wvw.utils.wvw.utils.rdf.Utilite;
import xyz.danoz.recyclerviewfastscroller.sectionindicator.title.SectionTitleIndicator;
import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

import static wvw.mobile.rules.ContactShowActivity.CONTACT_SELECT;
import static wvw.utils.MyRequest.requeteRemplirCombobox;


public class ContactListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    private Model modelOntologie  ;
    private InfModel modeleInf;
    private static String FILE_NAME_DATABASE="ont.owl";
    private File owlFile = null;
    private SearchView searchView;
    private ContactAdapter mAdapter;
    private List<Contact> contacts = new ArrayList<>();
    private FastScrollRecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        owlFile=new File(getExternalFilesDir(null),""+FILE_NAME_DATABASE);
        //owlFile=new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),""+FILE_NAME_DATABASE);
        modelOntologie  = Utilite.readModel(owlFile);
        modeleInf= Utilite.inference(modelOntologie,getAssets());

        recyclerView = (FastScrollRecyclerView) findViewById(R.id.recycler_view);

        searchView=(SearchView)findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(this);
        //ajouterContact();
        getContacts();

        mAdapter = new ContactAdapter(contacts);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), ContactAddActivity.class);
                startActivity(intent);
            }
        });

        //remplirCombobox();
        //getContacts();

        /**
        if(!isfileExistInExternalDir()){
            copyAssets();
        }
         **/
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {

                Contact contact = contacts.get(position);
                contact=findKnowsPerson(contact);
                //TODO:contact show
                Intent intent = new Intent(getBaseContext(), ContactShowActivity.class);
                intent.putExtra(CONTACT_SELECT, contact);
                startActivity(intent);

            }

            @Override
            public void onLongClick(View view, final int position) {

                //Rubrique rubrique = rubriqueList.get(position);

            }
        }));

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
                c.setRelationFind("Ami de");
                contactList.add(c);
            }
        }finally{
            qexec1.close();}
            selectContact.setContactsLiens(contactList);
        return selectContact;
    }


    public void remplirCombobox() {

        Query query=QueryFactory.create(requeteRemplirCombobox);
        QueryExecution qexec = QueryExecutionFactory.create(query,modeleInf);

        try  {
            ResultSet resultat=qexec.execSelect();
            while(resultat.hasNext()){
                QuerySolution soln=resultat.nextSolution();

                Literal identifiant=soln.getLiteral("id");
                String id=identifiant.getString();
                Literal nom=soln.getLiteral("nom");
                String Nom=nom.getString();
                Literal prenom=soln.getLiteral("prenom");
                String Prenom=prenom.getString();
                String concat=id+" "+Nom+"  "+Prenom;

                System.out.println("ID="+id+" Nom="+Nom+" Prenom"+Prenom);
            }


        }finally{
            qexec.close();}

    }

    //Methode pour afficher tous les contacts
    public void getContacts(){
        String nom="",prenom="",email="",numero="";

        Query query= QueryFactory.create(MyRequest.listePersonne);
        QueryExecution qexec = QueryExecutionFactory.create(query,modeleInf);

        try  {
            ResultSet resultat=qexec.execSelect();
            while(resultat.hasNext()){
                QuerySolution soln=resultat.nextSolution();
                Contact contact = new Contact();

                Literal li_identifiant=soln.getLiteral("id");
                String identifiant=li_identifiant.getString();
                Literal li_nom=soln.getLiteral("nom");
                if(li_nom!=null){
                    nom=li_nom.getString();
                    contact.setName(nom);
                }
                Literal li_prenom=soln.getLiteral("prenom");
                if(li_prenom!=null){
                    prenom=li_prenom.getString();
                    contact.setPrenom(prenom);
                }
                Literal li_numero=soln.getLiteral("numero1");
                if(li_numero!=null){
                    numero=li_numero.getString();
                    contact.setPhone(numero);
                }
                Literal li_email=soln.getLiteral("email");
                if(li_email!=null){
                    email=li_email.getString();
                    contact.setEmail(email);
                }
                System.out.println("ID="+identifiant+" Nom="+nom+" Prenom"+prenom);
                contact.setId(identifiant);
                contacts.add(contact);

            }


        }finally{
            qexec.close();
        }

    }

    private boolean isfileExistInExternalDir(){

        File outFile = new File(getExternalFilesDir(null), "ont.owl");
        return outFile.exists();
    }

    private void copyAssets() {
        AssetManager assetManager = getAssets();
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
                File outFile = new File(getExternalFilesDir(null), filename);
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
        Intent intent = new Intent(getBaseContext(), ContactEditActivity.class);
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



    private void ajouterContact() {
        // Ajoute les contacts à la list
        contacts.add(new Contact("Tony","(91)878547"));
        contacts.add(new Contact("Kate","(91)888547"));
        contacts.add(new Contact("Caty","(91)978547"));
        contacts.add(new Contact("Mitchell","(91)278547"));
        contacts.add(new Contact("Kabore","(91)7649394"));
        contacts.add(new Contact("SALI","(31)438439"));
        contacts.add(new Contact("Frank","(91)978547"));
        contacts.add(new Contact("Issa","(91)38493"));
        contacts.add(new Contact("Salif","(91)647399"));
        contacts.add(new Contact("Rahim","(91)094366"));
        contacts.add(new Contact("Brahima","(91)98436"));
        contacts.add(new Contact("Aidara","(91)7764783"));
        contacts.add(new Contact("Boli","(91)878547"));
        contacts.add(new Contact("Sakande","(91)888547"));
        contacts.add(new Contact("Maxime","(91)978547"));
        contacts.add(new Contact("Arley","(91)278547"));
        contacts.add(new Contact("Makula","(91)7649394"));
        contacts.add(new Contact("Zida","(31)438439"));
        contacts.add(new Contact("Kassoum","(91)978547"));
        contacts.add(new Contact("NONU","(91)38493"));
        contacts.add(new Contact("IDPE","(91)647399"));
        contacts.add(new Contact("BUISA","(91)094366"));
        contacts.add(new Contact("PEPDEU","(91)98436"));
        contacts.add(new Contact("Aidara","(91)7764783"));
        int SIZE = 1000;
        for (int i = 0; i < SIZE; i++) {
            contacts.add(new Contact("GEN"+i+"U"+(SIZE-i),i+"7764783"  ));
        }

    }

}
