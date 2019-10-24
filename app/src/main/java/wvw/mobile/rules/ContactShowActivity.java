package wvw.mobile.rules;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.l4digital.fastscroll.FastScrollRecyclerView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import wvw.mobile.rules.adapter.ContactAdapter;
import wvw.mobile.rules.adapter.ContactsAdapter;
import wvw.mobile.rules.dto.Contact;
import wvw.utils.wvw.utils.rdf.Namespaces;
import wvw.utils.wvw.utils.rdf.Utilite;

import static wvw.mobile.rules.ContactListActivity.CONTACT_SELECT;

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
    private String smsCmdArr[] = {"New", "Delete", "Settings", "BookMark", "Block"};
    private Model modelOntologie  ;
    private InfModel modeleInf;
    private static String FILE_NAME_DATABASE="ont.owl";
    private File owlFile = null;
    private FileOutputStream outputStream= null;
    private FileInputStream in=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_show);

        owlFile=new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),""+FILE_NAME_DATABASE);
        modelOntologie = Utilite.readModel(getAssets(),getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS));
        modeleInf = Utilite.inference(modelOntologie,getAssets());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        appBar = (AppBarLayout) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        searchView=(SearchView)findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(this);



        try {
            Intent intent = getIntent();
            this.contact= (Contact) intent.getSerializableExtra(CONTACT_SELECT);
            System.out.println("Contact receved");
        } catch (NullPointerException e) {
            System.out.println("Fragment Parent non trouvee");
        }

        loadNamesField(contact);
        /**loadNumbersField(contact);
        loadAddressField(contact);
        loadEmailField(contact);
        **/
        loadContactsField(contact);
        mOptionsImageMore = (ImageView) findViewById(R.id.imageViewMore);
        mImageViewEdit = (ImageView) findViewById(R.id.imageViewEdit);
        mImageViewShare = (ImageView) findViewById(R.id.imageViewShare);
        mOptionsImageMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.popup, null);
                final PopupWindow popupWindow = new PopupWindow(popupView,
                        WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

                popupWindow.showAsDropDown(mOptionsImageMore);
                // Create SimpleAdapter that will be used by short message list view.
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
            }
        });


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
            Intent intent = new Intent(getBaseContext(), HomeActivity.class);
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
        TextView firstName = (TextView) findViewById(R.id.tvValue_2);
        TextView lastName = (TextView) findViewById(R.id.tvValue_3);
        //TODO:: A remplacer apres par le premier caractere du Nom
        ImageView imageView = (ImageView) findViewById(R.id.contactImage);

        /**
         * If a name is missing then hide it
         */
        setViewState(displayedName, contact.getName());
        boolean fName = setViewState(firstName, contact.getName());
        boolean lName = setViewState(lastName, contact.getPrenom());

        StringBuilder title = new StringBuilder();

        if(fName) {title.append(contact.getName() + " ");}
        if(lName) {title.append(contact.getPrenom());}

        /**
         * Set activity's title to show contact first and last names
         */
        getSupportActionBar().setTitle(title.toString().trim());

        displayedName.setText(title.toString().trim());
        firstName.setText(contact.getName());
        lastName.setText(contact.getPrenom());

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
        startActivity(new Intent(getBaseContext(),HomeActivity.class));
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
        System.out.println("LE query est::"+query);
        return filteredModelList;
    }

}
