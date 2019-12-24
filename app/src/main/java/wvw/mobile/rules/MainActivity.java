package wvw.mobile.rules;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import wvw.mobile.rules.dto.Contact;
import wvw.mobile.rules.fragment.ContactListFragment;
import wvw.mobile.rules.fragment.RelationFragment;
import wvw.utils.MyRequest;
import wvw.utils.wvw.utils.rdf.Utilite;

import static wvw.mobile.rules.ContactShowActivity.CONTACT_SELECT;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    public static String CONTACTS_ST_LIST="contact_string_list";
    public static String CONTACTS_LIST="contacts_list";

    private Model modelOntologie  ;
    private InfModel modeleInf;
    private static String FILE_NAME_DATABASE="ont.owl";
    private File owlFile = null;
    private List<Contact> contacts = new ArrayList<>();
    private List<String> contactsSequences = new ArrayList<String>();
    private ImageView ImageViewSetting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        /**
         Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
         setSupportActionBar(toolbar);
         toolbar.setVisibility(View.INVISIBLE);
         **/
        Log.d("MAINNN","YEAH MAIN");
        if(!isDatabasefileExist()){
            copyAssets();
        }
        owlFile=new File(getExternalFilesDir(null),""+FILE_NAME_DATABASE);
        //owlFile=new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),""+FILE_NAME_DATABASE);
        modelOntologie  = Utilite.readModel(owlFile);
        modeleInf= Utilite.inference(modelOntologie,getAssets());

        /**
         * Permet de ne pas avoir a faire une requete en quittant d'autres vue
         */
        try {
            Intent intent = getIntent();
            List<Contact> contactsSave = (List<Contact>) intent.getSerializableExtra(CONTACTS_LIST);
            if(contactsSave!=null) {
                this.contacts = contactsSave;
                System.out.println("Data recevd successfuly");
            }else {
                getContacts();
            }

        } catch (NullPointerException e) {
            //lancer au demarrage de l'application
            getContacts();
        }

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        ImageViewSetting = findViewById(R.id.imag_setting_popup);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), ContactAddActivity.class);
                startActivity(intent);
            }
        });

        ImageViewSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(MainActivity.this, ImageViewSetting);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.poupup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        return true;
                    }
                });
                popup.show(); //showing popup menu
            }
        }); //closing the setOnClickListener method
    }

    private void setupViewPager(ViewPager viewPager) {
        MainActivity.ViewPagerAdapter adapter = new MainActivity.ViewPagerAdapter(getSupportFragmentManager());
        //envoye exercice au fragment
        Bundle bundle = new Bundle();
        bundle.putSerializable(CONTACT_SELECT, new Contact());
        bundle.putSerializable(CONTACTS_LIST, (Serializable) contacts);
        bundle.putSerializable(CONTACTS_ST_LIST, (Serializable) contactsSequences);
        ContactListFragment contactListFragment= new ContactListFragment();
        RelationFragment relationFragment = new RelationFragment();
        contactListFragment.setArguments(bundle);
        relationFragment.setArguments(bundle);
        adapter.addFragment(contactListFragment, "Contacts");
        adapter.addFragment(relationFragment, "Relations");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

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
                contactsSequences.add(identifiant+" "+nom+"  "+prenom);

            }


        }finally{
            qexec.close();
        }

        //tri par Nom
        Collections.sort(contacts, Contact.ComparatorName);

    }

    private boolean isDatabasefileExist(){

        File outFile = new File(getExternalFilesDir(null), FILE_NAME_DATABASE);
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



}
