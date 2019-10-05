package wvw.mobile.rules;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;

import java.io.File;
import java.io.FileOutputStream;

import wvw.mobile.rules.dto.Contact;
import wvw.utils.wvw.utils.rdf.Utilite;

import static wvw.mobile.rules.ContactListActivity.CONTACT_SELECT;

public class ContactEditActivity extends AppCompatActivity {

    private EditText txtNom,txtPrenom,txtTelephone,txtEmail,txtNaissance;
    private Button btnEnregistrer;
    private Spinner spinSexe, spinRelation,spinPropriete;
    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Model modelOntologie  ;
    private InfModel modeleInf;
    private static String FILE_NAME_DATABASE="ont.owl";
    private File owlFile = null;
    private FileOutputStream outputStream= null;
    private Contact contact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);
        owlFile=new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),""+FILE_NAME_DATABASE);

        modelOntologie = Utilite.readModel(getAssets(),getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS));
        modeleInf = Utilite.inference(modelOntologie,getAssets());

        //Data to Spinner
        spinSexe =findViewById(R.id.spinner_sexe);
        spinRelation = findViewById(R.id.spinner_relation);
        spinPropriete= findViewById(R.id.spinner_propriete);
        txtNom=findViewById(R.id.input_nom);
        txtPrenom=(EditText)findViewById(R.id.input_prenom);
        txtTelephone=(EditText)findViewById(R.id.input_telephone);
        txtEmail=(EditText)findViewById(R.id.input_email);
        txtNaissance =(EditText) findViewById(R.id.input_naissance);
        btnEnregistrer= findViewById(R.id.btn_enregistrer);

        //Recuperer les donnes a modifier

        try {
            Intent intent = getIntent();
            if (intent.hasExtra(CONTACT_SELECT)){ // vérifie qu'une valeur est associée à la clé “edittext”
                this.contact= (Contact) intent.getSerializableExtra(CONTACT_SELECT);
            }

        } catch (NullPointerException e) {
            System.out.println("Fragment Parent non trouvee");
        }

        initialiseInput();

    }

    private void initialiseInput() {
        txtNom.setText(this.contact.getName());
        txtTelephone.setText(this.contact.getPhone());
        if(contact.getPrenom()!=null)txtPrenom.setText(contact.getPrenom());
        if(contact.getEmail()!=null) txtEmail.setText(contact.getEmail());
        if(contact.getBirthday()!=null) txtNaissance.setText(contact.getBirthday());
        if(contact.getSexe() != null){
            if(contact.getSexe().contentEquals("Homme")){
                //spinSexe.setSelection(1);
                System.out.println();
            }else{
                //spinSexe.setSelection(2);
            }
        }


    }
}
