package wvw.mobile.rules;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wvw.mobile.rules.dto.Contact;
import wvw.utils.MyRequest;
import wvw.utils.wvw.utils.rdf.Utilite;

import static wvw.mobile.rules.MainActivity.CONTACTS_LIST;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private View mContentView;
    private Model modelOntologie  ;
    private InfModel modeleInf;
    private static String FILE_NAME_DATABASE="ont.owl";
    private File owlFile = null;
    private List<Contact> contacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        mContentView = findViewById(R.id.progress_bar);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        progressBar = findViewById(R.id.progress_bar);
        progressBar.getProgressDrawable().setColorFilter(
                Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);

        SplashActivity.LoadContactTask task = new SplashActivity.LoadContactTask();
        task.execute(new String[] { "Nikiss" });

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

    private void copyDatabase() {
        AssetManager assetManager = getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(FILE_NAME_DATABASE);
            File outFile = new File(getExternalFilesDir(null), FILE_NAME_DATABASE);
            out = new FileOutputStream(outFile);
            copyFile(in, out);
        } catch(IOException e) {
            Log.e("tag", "Failed to copy asset file: " + FILE_NAME_DATABASE, e);
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

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    private class LoadContactTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "Nikiss";

            if(!isDatabasefileExist()){
                copyAssets();
                //copyDatabase();
            }
            owlFile=new File(getExternalFilesDir(null),""+FILE_NAME_DATABASE);
            //owlFile=new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),""+FILE_NAME_DATABASE);
            modelOntologie  = Utilite.readModel(owlFile);

            //modeleInf= Utilite.inference(modelOntologie,getAssets());
            modeleInf= Utilite.inference(modelOntologie,getApplicationContext());
            System.out.println("Resolu");
            getContacts();
            return response;
        }

        /**
         * Metode lancer apres recherche des sonnes
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {
            startActivity(new Intent(getBaseContext(),MainActivity.class).putExtra(CONTACTS_LIST, (Serializable) contacts));
            finish();
        }
    }
}
