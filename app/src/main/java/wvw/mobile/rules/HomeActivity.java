package wvw.mobile.rules;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import wvw.mobile.rules.dto.Contact;
import wvw.mobile.rules.fragment.ContactListFragment;
import wvw.mobile.rules.fragment.RelationFragment;
import wvw.utils.MyRequest;
import wvw.utils.wvw.utils.rdf.Utilite;

import static wvw.mobile.rules.ContactListActivity.CONTACT_SELECT;

public class HomeActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    public static String CONTACT_SELECT="CONTACT_SELECT";
    public static String CONTACTS_ST_LIST="contact_string_list";
    public static String CONTACTS_LIST="contacts_list";

    private Model modelOntologie  ;
    private InfModel modeleInf;
    private static String FILE_NAME_DATABASE="ont.owl";
    private File owlFile = null;
    private List<Contact> contacts = new ArrayList<>();
    private List<String> contactsSequences = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        /**
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setVisibility(View.INVISIBLE);
         **/
        owlFile=new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),""+FILE_NAME_DATABASE);
        modelOntologie  = Utilite.readModel(owlFile);
        modeleInf= Utilite.inference(modelOntologie,getAssets());
        getContacts();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), ContactAddActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        HomeActivity.ViewPagerAdapter adapter = new HomeActivity.ViewPagerAdapter(getSupportFragmentManager());
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

    }

}
