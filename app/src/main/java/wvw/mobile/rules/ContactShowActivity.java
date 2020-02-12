package wvw.mobile.rules;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import wvw.mobile.rules.adapter.ContactsAdapter;
import wvw.mobile.rules.dto.Contact;
import wvw.utils.MyRequest;
import wvw.utils.wvw.utils.rdf.Namespaces;
import wvw.utils.wvw.utils.rdf.Utilite;


import static wvw.mobile.rules.MainActivity.CONTACTS_LIST;

public class ContactShowActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private TextView mTextMessage;
    private Contact contact;
    private Toolbar toolbar;
    private AppBarLayout appBar;
    private ImageView mOptionsImageMore,mImageViewEdit,mImageViewShare;
    private SearchView searchView;
    private ContactsAdapter mAdapter;
    private RecyclerView recyclerView;
    private List<Contact> contacts = new ArrayList<>();
    private List<Contact> contactsSave = new ArrayList<>();
    private String smsCmdArr[] = {"New", "Delete", "Settings", "BookMark", "Block"};
    private Model modelOntologie  ;
    private InfModel modeleInf;
    private static String FILE_NAME_DATABASE="ont.owl";
    private File owlFile = null;
    private FileOutputStream outputStream= null;
    private FileInputStream in=null;
    private final String SUPPRIMER_CHOICE="Supprimer le contact";
    private final String AJOUTER_CHOICE="Ajouter à la liste noire";
    private final String PLACER_CHOICE="Placer sur écran acceuil";
    private ProgressBar progressBarLiens;
    public static String CONTACT_SELECT="CONTACT_SELECT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_show);

        owlFile=new File(getExternalFilesDir(null),""+FILE_NAME_DATABASE);

        modelOntologie  = Utilite.readModel(owlFile);
        //modelOntologie = Utilite.readModel(getAssets(),getExternalFilesDir(null));
        modeleInf= Utilite.inference(modelOntologie,this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        appBar = (AppBarLayout) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); //cache le button pour retourne depuis le actionBar

        searchView=(SearchView)findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(this);
        progressBarLiens = findViewById(R.id.progressBarLiens);
        progressBarLiens.setVisibility(View.VISIBLE);



        try {
            Intent intent = getIntent();
            this.contact= (Contact) intent.getSerializableExtra(CONTACT_SELECT);
            this.contactsSave= (List<Contact>) intent.getSerializableExtra(CONTACTS_LIST);

            System.out.println("Contact receved");
        } catch (NullPointerException e) {
            System.out.println("Fragment Parent non trouvee");
        }

        loadNamesField(contact);
        loadOthersField(contact);

        /**
        loadAddressField(contact);
        loadEmailField(contact);
        **/
        //loadContactsField(contact); // gere par une Tache Asynchrone
        mOptionsImageMore = (ImageView) findViewById(R.id.imageViewMore);
        mImageViewEdit = (ImageView) findViewById(R.id.imageViewEdit);
        mImageViewShare = (ImageView) findViewById(R.id.imageViewShare);
        mOptionsImageMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(ContactShowActivity.this, mOptionsImageMore);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.poupup_menu, popup.getMenu());
                popup.getMenu().clear();
                popup.getMenu().add(SUPPRIMER_CHOICE);
                popup.getMenu().add(AJOUTER_CHOICE);
                popup.getMenu().add(PLACER_CHOICE);

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (String.valueOf(item.getTitle())){
                            case SUPPRIMER_CHOICE:
                                new AlertDialog.Builder(ContactShowActivity.this)
                                        .setTitle("Supprimer le contact")
                                        .setMessage("Supprimer ce contact?")
                                        .setNegativeButton("Annuler", null)
                                        .setPositiveButton("Supprimer", new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface arg0, int arg1) {
                                                //Suprimer ce contact
                                                delContact();
                                            }
                                        }).create().show();
                                break; // break is optional
                            case AJOUTER_CHOICE:

                                break;
                            case PLACER_CHOICE:

                                break;
                        }
                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        });

        mImageViewEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), ContactEditActivity.class);
                intent.putExtra(CONTACT_SELECT, contact);
                startActivity(intent);
            }
        });

        //TODO:: le contenu de la fonction a remplacer apres
        mImageViewShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        FindKnowsPersonTask task = new FindKnowsPersonTask();
        task.execute(new String[] { "Nikiss" });

    }

    private void loadOthersField(Contact contact) {

        if (contact == null) {
            return;
        }
        TextView txtNumbers01 = (TextView) findViewById(R.id.txtNumbers01);
        TextView txtEmails01 = (TextView) findViewById(R.id.txtEmail01);
        txtNumbers01.setText(contact.getPhone());
        if(contact.getEmail()!=null && !contact.getEmail().isEmpty()){
            txtEmails01.setText(contact.getEmail());
        }
    }


    private void delContact() {
        try {
            outputStream=new FileOutputStream(owlFile);

            String identifiant=contact.getId();

            Utilite.supprimerValeurProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant");
            Utilite.supprimerValeurProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom");
            Utilite.supprimerValeurProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom");
            Utilite.supprimerValeurProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1");
            Utilite.supprimerValeurProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero2");
            Utilite.supprimerValeurProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero3");
            Utilite.supprimerValeurProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance");


            if(!contact.getEmail().contains("ND")){
                Utilite.supprimerValeurProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email");
            }

            modelOntologie.write(outputStream,"N3");
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void loadNamesField(Contact contact)
    {
        if (contact == null) {
            return;
        }

        TextView displayedName = (TextView) findViewById(R.id.tvValue_1);

        //TODO:: A remplacer apres par le premier caractere du Nom
        ImageView imageView = (ImageView) findViewById(R.id.contactImage);

        if(contact.getSexe()!=null && contact.getSexe()=="Femme"){
            imageView.setImageResource(R.drawable.woman);
        }
        /**
         * If a name is missing then hide it
         */
        setViewState(displayedName, contact.getName());
        StringBuilder title = new StringBuilder();

        if(contact.getName()!=null) {title.append(contact.getName() + " ");}
        if(contact.getPrenom()!=null) {title.append(contact.getPrenom());}

        /**
         * Set activity's title to show contact first and last names
         */
        getSupportActionBar().setTitle(title.toString().trim());

        displayedName.setText(title.toString().trim());

    }

    private void loadContactsField(Contact contact) {
        if (contact == null || contact.getContactsLiens() == null) {
            return;
        }
        contacts = contact.getContactsLiens();
        recyclerView = (RecyclerView) findViewById(R.id.rvLiens);
        mAdapter = new ContactsAdapter(contacts);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        //RecyclerView rvEvent = (RecyclerView) findViewById(R.id.rvLiens);
        //rvEvent.setItemAnimator(new DefaultItemAnimator());
    }

    private boolean setViewState(TextView tv, String value)
    {
        if (value.trim().isEmpty())
        {
            tv.setVisibility(View.GONE);
            return false;
        }
        else
        {
            tv.setVisibility(View.VISIBLE);
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        /**
         * l'envoi de la liste permettra de ne pas refaire une requete dans la base de
         * donnees ,Attention le finish permet de supprimer l'activity creer
         */
        Intent intent = new Intent(this, MainActivity.class).putExtra(CONTACTS_LIST, (Serializable) contactsSave);// New activity
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

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

    private class FindKnowsPersonTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "Nikiss";

            contact=findKnowsPerson(contact);
            return response;
        }

        /**
         * Metode lancer apres recherche des sonnes
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {
            //textView.setText(result);
            loadContactsField(contact);
            progressBarLiens.setVisibility(View.INVISIBLE);
        }
    }

    private Contact findKnowsPerson(Contact selectContact) {

        List<Contact> contactList = new ArrayList<>();
        String reqDesPersonneEnRelation=Utilite.gestionRequete(MyRequest.relation, Integer.parseInt(selectContact.getId()));
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
                System.out.println("RELATION AVEC id: "+idTrouver+" "+nomTrouver+" "+prenomTrouver);
                Contact c= new Contact();
                c.setId(idTrouver);
                c.setName(nomTrouver);
                c.setPrenom(prenomTrouver);
                //c.setRelationFind("Fils de");
                String lien=findRelationType(idTrouver,selectContact.getId());
                c.setRelationFind(lien);
                if(!idTrouver.equals(contact.getId())) {
                    contactList.add(c);
                }
            }
        }finally{
            qexec1.close();}
        selectContact.setContactsLiens(contactList);
        return selectContact;
    }

    private String findRelationType(String idTrouver,String idContactSelected){
        String relationFind= " ";

        // idContactSelected aPourPere idContactTemp

        String relation=relationType(idTrouver, idContactSelected, MyRequest.pere_2);
        if(!relation.contains(""+"null")){   //relationFind="est le père de";
            relationFind="Père";
        }else {
            relation = relationType(idTrouver, idContactSelected, MyRequest.mere_2);
            if(!relation.contains(""+"null")){
                relationFind="Mère";
            }else {
                relation = relationType(idTrouver, idContactSelected, MyRequest.fille_2);
                if(!relation.contains(""+"null")){
                    relationFind="Fille";
                }else {
                    relation = relationType(idTrouver, idContactSelected, MyRequest.fils_2);
                    if(!relation.contains(""+"null")){
                        relationFind="Fils";
                    }else {
                        relation = relationType(idTrouver, idContactSelected, MyRequest.frere_2);
                        if(!relation.contains(""+"null")){
                            relationFind="Frère";
                        }else {
                            relation = relationType(idTrouver, idContactSelected, MyRequest.soeur_2);
                            if(!relation.contains(""+"null")){
                                relationFind="Soeur";
                            }else {
                                //relation = relationType(idTrouver, idContactSelected, MyRequest.soeur_2);
                                relationFind="En relation";
                            }
                        }
                    }
                }
            }
        }

        return relationFind;
    }

    public String relationType(String idTrouver,String idContactSelected,String reque){

        int id_trouver=Integer.parseInt(idTrouver);
        int id_selected=Integer.parseInt(idContactSelected);

        String req=gestionRequeteRelationType(reque, id_trouver, id_selected);
        String nom="";
        Query query=QueryFactory.create(req);
        QueryExecution qexec = QueryExecutionFactory.create(query,modeleInf);
        try  {
            ResultSet resultat=qexec.execSelect();
            while(resultat.hasNext()){

                QuerySolution soln=resultat.nextSolution();
                Literal nom_li=soln.getLiteral("nom");
                nom=""+nom_li;
                return nom;
            }
        }finally{
            qexec.close();}
        return ("null");

    }

    public String gestionRequeteRelationType(String requete,int id_trouver,int id_selected){
        return requete+" filter (xsd:integer(?identif)="+id_trouver+" && xsd:integer(?identi)="+id_selected+")}";
    }


}
