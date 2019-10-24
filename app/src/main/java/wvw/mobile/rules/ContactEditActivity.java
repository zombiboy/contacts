package wvw.mobile.rules;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import wvw.mobile.rules.dto.Contact;
import wvw.utils.wvw.utils.rdf.Namespaces;
import wvw.utils.wvw.utils.rdf.Utilite;

import static wvw.mobile.rules.ContactListActivity.CONTACT_SELECT;

public class ContactEditActivity extends AppCompatActivity {

    private EditText txtNom,txtPrenom,txtTelephone,txtEmail,txtNaissance;
    private EditText txtPhone2,txtPhone3;
    private String nom,prenom,telephone,email,naissance,phone2,phone3,sexe;
    private Button btnEnregistrer;
    private Spinner spinSexe;
    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Model modelOntologie  ;
    private InfModel modeleInf;
    private static String FILE_NAME_DATABASE="ont.owl";
    private File owlFile = null;
    private FileOutputStream outputStream= null;
    private FileInputStream in=null;
    private Contact contact;

    //TODO:: voir les multiples numeros et aussi

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);
        owlFile=new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),""+FILE_NAME_DATABASE);
        modelOntologie = Utilite.readModel(getAssets(),getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS));
        modeleInf = Utilite.inference(modelOntologie,getAssets());

        //Data to Spinner
        spinSexe =findViewById(R.id.spinner_sexe);
        txtNom=findViewById(R.id.input_nom);
        txtPrenom=(EditText)findViewById(R.id.input_prenom);
        txtTelephone=(EditText)findViewById(R.id.input_telephone);
        txtPhone2=(EditText)findViewById(R.id.input_phone2);
        txtPhone3=(EditText)findViewById(R.id.input_phone3);

        txtEmail=(EditText)findViewById(R.id.input_email);
        txtNaissance =(EditText) findViewById(R.id.input_naissance);
        btnEnregistrer= findViewById(R.id.btn_enregistrer);
        btnEnregistrer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveContact(v);
            }
        });

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

    /**
     * la methode de lire un fichier dans le format N3
     */
    private void readFileTON3() {
        try {
            in=new FileInputStream(owlFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        modelOntologie.read(in, null,"N3");

        modelOntologie.write(System.out);
    }

    private void initialiseInput() {
        txtNom.setText(this.contact.getName());
        txtTelephone.setText(this.contact.getPhone());
        if(contact.getPhone2()!=null)txtPhone2.setText(contact.getPhone2());
        if(contact.getPhone3()!=null)txtPhone3.setText(contact.getPhone3());
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

    public void saveContact(View view) {
        try {
            outputStream=new FileOutputStream(owlFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        this.nom = txtNom.getText().toString();
        this.prenom = txtPrenom.getText().toString();
        this.telephone = txtTelephone.getText().toString();
        this.phone2 = txtPhone2.getText().toString();
        this.phone3 = txtPhone3.getText().toString();
        this.email = txtEmail.getText().toString();
        this.sexe = spinSexe.getSelectedItem().toString();
        this.naissance = txtNaissance.getText().toString() ;
        String identifiant = contact.getId();


        if ( nom.isEmpty() || telephone.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Echec de modification!!", Toast.LENGTH_SHORT).show();
        }else{

            initialiseContactAttrib();
            //TODO: Ne supprime pas un attribut si la personne efface les anciens informations
            if(nom != null && !nom.isEmpty())
            Utilite.modifierValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, ""+Namespaces.Contact.NOM,this.nom);
            if(prenom != null && !prenom.isEmpty())
            Utilite.modifierValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, ""+Namespaces.Contact.PRENOM, this.prenom);
            if(telephone != null && !telephone.isEmpty())
            Utilite.modifierValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, ""+Namespaces.Contact.PHONE, this.telephone);
            if(phone2 != null && !phone2.isEmpty())
            Utilite.modifierValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, ""+Namespaces.Contact.PHONE_2, this.phone2);
            if(phone3 != null && !phone3.isEmpty())
            Utilite.modifierValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, ""+Namespaces.Contact.PHONE_3, this.phone3);
            if(email != null && !email.isEmpty())
            Utilite.modifierValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, ""+Namespaces.Contact.EMAIL, this.email);
            if(naissance != null && !naissance.isEmpty())
            Utilite.modifierValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, ""+Namespaces.Contact.DATE_NAISSANCE, this.naissance);

            modelOntologie.write(outputStream,"N3");
            Intent intent = new Intent(getBaseContext(), ContactShowActivity.class);
            intent.putExtra(CONTACT_SELECT, this.contact);
            startActivity(intent);

        }
    }

    private void initialiseContactAttrib() {
        contact.setName(nom);
        contact.setPrenom(prenom);
        contact.setEmail(email);
        contact.setPhone(telephone);
        contact.setPhone2(phone2);
        contact.setPhone3(phone3);
        //contact.setSexe(sexe);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer les modifications")
                .setMessage("Annuler les modifications ?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        modelOntologie.close();
                        ContactEditActivity.super.onBackPressed();
                    }
                }).create().show();
    }

}
