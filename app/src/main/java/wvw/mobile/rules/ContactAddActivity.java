package wvw.mobile.rules;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.vocabulary.VCARD;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import wvw.mobile.rules.dto.Contact;
import wvw.utils.Civilite;
import wvw.utils.IOUtils;
import wvw.utils.MyRequest;
import wvw.utils.wvw.utils.rdf.Namespaces;

import static wvw.mobile.rules.HomeActivity.CONTACT_SELECT;

public class ContactAddActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,AdapterView.OnItemClickListener {

    private EditText txtNom,txtPrenom,txtTelephone,txtEmail,txtNaissance;
    private Button btnEnregistrer;
    private Spinner spinSexe, spinRelation,spinPropriete;
    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Model modelOntologie  ;
    private InfModel modeleInf;
    private static String FILE_NAME_DATABASE="ont.owl";
    private File owlFile = null;
    private FileOutputStream out= null;
    private Contact contact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_add);

        owlFile=new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),""+FILE_NAME_DATABASE);
        modelOntologie  = readModel();
        modeleInf= inference(modelOntologie );

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

        //gestion contenu relation
        ArrayAdapter <CharSequence> adapterR = ArrayAdapter.createFromResource(this,R.array.contacts_relation_types,android.R.layout.simple_spinner_item);
        adapterR.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinRelation.setAdapter(adapterR);
        spinRelation.setOnItemSelectedListener(this);
        // gestion de la date de naissance à l'aide de DataPicker
        mDisplayDate = (EditText) findViewById(R.id.input_naissance);

        mDisplayDate.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        ContactAddActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();


            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month +1;
                Log.d("selectdate","onDateSet: dd/mm/yyyy" + day + "/" + month + "/" + year);

                String date =  day + "/" + month +"/" + year;
                mDisplayDate.setText(date);

            }
        };


        spinRelation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String selectedText =  adapterView.getItemAtPosition(i).toString();
                String choix=spinRelation.getSelectedItem().toString();

                if(!(choix.contains("Choisir"))){
                    spinPropriete.setEnabled(true);//findViewById(R.id.spinner_propriete).setEnabled(true);
                    remplirCombobox();
                }else
                {
                    //findViewById(R.id.spinner_propriete).setEnabled(false);
                    spinPropriete.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    //Lecture du fichier owl
    public Model readModel() {
        //creation d'un model vide
        Model model = ModelFactory.createDefaultModel();
        // utilisation de FileManager pour trouver le fichier owl
        FileInputStream in = null;
        try {
            in= new FileInputStream(owlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // lecture du fichier RDF/XML
        model.read(in, "","N3");
        try {
            in.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return null;
        }
        return model;

    }


    //Gestion des inferences
    private InfModel inference(Model model)  {
        AssetManager asset=getAssets();

        try {
            List<Rule> rules = Rule.parseRules(IOUtils.read(asset.open("regles.jena")));
            GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
            InfModel infModel = ModelFactory.createInfModel(reasoner, model);

            return infModel;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }


    //Creation d'instance de classe
    public boolean creerInstanceDeClasse(Model model, String namespace, String nomClasse, String nomInstance) {
        Resource rs = model.getResource(namespace + nomInstance);
        if (rs == null)
            rs = model.createResource(namespace + nomInstance);
        Property p = model.getProperty(Namespaces.rdf + "type");
        Resource rs2 = model.getResource(namespace + nomClasse);
        if ((rs2 != null)&&(rs != null) && (p != null)) {
            //ajout de nouvelle valeur
            rs.addProperty(p,rs2);
            return true;
        }
        return false;
    }


    //Ajouter la valeur d'une propriete datatype d'une Instance
    public boolean ajouterValeurDataTypeProperty(Model model, String namespace, String nomInstance, String nomPropriete, Object valeur) {
        Resource rs = model.getResource(namespace + nomInstance);
        Property p = model.getProperty(namespace + nomPropriete);
        if ((rs != null) && (p != null)) {
            //ajout de nouvelle valeur
            rs.addLiteral(p, valeur);
            return true;

        }
        return false;
    }


    //Modifier la valeur d'une propriete datatype d'une Instance
    public boolean modifierValeurDataTypeProperty(Model model, String namespace, String nomInstance, String nomPropriete, Object valeur) {
        Resource rs = model.getResource(namespace + nomInstance);
        Property p = model.getProperty(namespace + nomPropriete);
        if ((rs != null) && (p != null)) {
            //suppression de l'ancienne valeur de la propriete
            rs.removeAll(p);
            //ajout de nouvelle valeur
            rs.addLiteral(p, valeur);
            return true;
        }
        return false;
    }



    //Ajouter la valeur d'une propriete objet d'une Instance
    public boolean ajouterValeurObjectProperty(Model model, String namespace, String nomInstance1, String nomPropriete, String nomInstance2) {
        Resource rs1 = model.getResource(namespace + nomInstance1);
        Resource rs2 = model.getResource(namespace + nomInstance2);
        Property p = model.getProperty(namespace + nomPropriete);
        if ((rs1 != null) && (rs2 != null) && (p != null)) {
            //ajout de nouvelle valeur
            rs1.addProperty(p,rs2);
            return true;
        }
        return false;
    }


    //Modifier la valeur d'une propriete objet d'une Instance
    public boolean modifierValeurObjectProperty(Model model, String namespace, String nomObject1, String nomPropriete, String nomObject2) {
        Resource rs1 = model.getResource(namespace + nomObject1);
        Resource rs2 = model.getResource(namespace + nomObject2);
        Property p = model.getProperty(namespace + nomPropriete);
        if ((rs1 != null) && (rs2 != null) && (p != null)) {
            //suppression des valeurs de la propriete
            rs1.removeAll(p);
            //ajout de nouvelle valeur
            rs1.addProperty(p,rs2);
            return true;
        }
        return false;
    }


    //Methode pour afficher les contacts dans le combobox
    public void remplirCombobox() {

        Query query=QueryFactory.create(MyRequest.requeteRemplirCombobox);
        QueryExecution qexec = QueryExecutionFactory.create(query,modeleInf);

        ArrayAdapter<String> adapter = new ArrayAdapter <String> (getApplicationContext(), android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

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

                adapter.add(concat);

            }
            spinPropriete.setAdapter(adapter);


        }finally{
            qexec.close();}

    }


    //Methode pour gerer les nouveaux identifiants
    private int identifiant()  {

        String requete="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
                "SELECT ?id" +
                " where {?x rdf:type ns:Personne." +
                "?x ns:identifiant ?id.}";

        Query query=QueryFactory.create(requete);
        QueryExecution qexec = QueryExecutionFactory.create(query, modeleInf );
        try  {
            int max=0;
            ResultSet resultat=qexec.execSelect();
            while(resultat.hasNext()){
                QuerySolution soln=resultat.nextSolution();

                Literal identifiant=soln.getLiteral("id");
                int id=identifiant.getInt();
                if(max<id){max=id;}
            }return max;
        }finally{
            qexec.close();}

    }


    //Methode pour gerer les identifiants existants
    public String id_existant(String str)  {

        String requete="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
                "SELECT ?id ?nom ?prenom" +
                " where {?x rdf:type ns:Personne." +
                "?x ns:identifiant ?id." +
                "?x ns:nom ?nom." +
                "?x ns:prenom ?prenom.}";

        Query query=QueryFactory.create(requete);
        QueryExecution qexec = QueryExecutionFactory.create(query,modeleInf);
        try  {
            String valeur="";
            ResultSet resultat = qexec.execSelect();
            while (resultat.hasNext()) {
                QuerySolution soln = resultat.nextSolution();

                Literal identifiant = soln.getLiteral("id");
                String id = identifiant.getString();
                Literal nom = soln.getLiteral("nom");
                String Nom = nom.getString();
                Literal prenom = soln.getLiteral("prenom");
                String Prenom = prenom.getString();

                String concat = id + " " + Nom + "  " + Prenom;
                if(concat.contains(str)){
                    valeur=id;
                }
            }return(valeur);
        }finally{
            qexec.close();}

    }


    //gestion des requetes
    public String gestionRequete(String requete,int id){
        return requete+" filter (xsd:integer(?identif)="+id+")}";}




    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        System.out.println(text);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    public void saveContact(View view){
        contact = new Contact();
        String nom = String.valueOf(txtNom.getText());// Partie declaration et initialisation de variables //
        String prenom = String.valueOf(txtPrenom.getText());
        String numero = String.valueOf(txtTelephone.getText());
        String email = String.valueOf(txtEmail.getText());
        String naissance = String.valueOf(txtNaissance.getText());
        String sexe = String.valueOf(spinSexe.getSelectedItem().toString());
        String b_relation = String.valueOf(spinRelation.getSelectedItem().toString());


        int identifiant=0;
        identifiant = identifiant()+1;
        if (nom.isEmpty() || numero.isEmpty()) {
            Toast.makeText(ContactAddActivity.this, "Impossible d'enregistrer!!", Toast.LENGTH_LONG).show();
        }else{

            if (email.isEmpty() && sexe.contains("Homme")) {
                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
            }else {
                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
            }
            // add the property
            ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
            ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", ""+nom);
            ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", ""+numero);
            contact.setId(""+identifiant);
            contact.setName(nom);
            contact.setPhone(numero);
            //verfier avant d'ajouter la propriete
            if(!prenom.isEmpty()) {
                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", "" + prenom);
                contact.setPrenom(prenom);
            }
            if(!naissance.isEmpty()){
            ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);
            contact.setBirthday(naissance);
            }
            if(!email.isEmpty()) {
                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                contact.setEmail(email);
            }

            //La personne a une relation avec une autre personne , mais on appelera proprieté= personne et relation=relation
            if (!b_relation.contains("Choisir")) {
                String choice_person=spinPropriete.getSelectedItem().toString();
                String id_person_choice=id_existant(choice_person);//identifiant de l'enregistrement existant

                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id_person_choice);
                switch(b_relation)
                {
                    case "Père de" :
                        // Statements
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "aPourEnfant", id_person_choice);
                        break; // break is optional
                    case "Mère de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "aPourEnfant", id_person_choice);
                        break; // break is optional
                    case "Frère de" :
                        //TODO:: A Revoir les noms des proprites changé id_person_choice par identifiant et vice versa
                        System.out.println("Choice frere stich case");
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+id_person_choice, "aPourFrere", ""+identifiant);
                        break;
                    case "Soeur de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+id_person_choice, "aPourSoeur", ""+identifiant);
                        break;
                    case "Oncle paternel de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+id_person_choice, "aPourOnclePaternel", ""+identifiant);
                        break;
                    case "Tante paternelle de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourTantePaternelle", ""+identifiant);
                        break;
                    case "Grand père paternel de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourGrandParentPaternel", ""+identifiant);
                        break;
                    case "Cousin paternel de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourCousinPaternel", ""+identifiant);
                        break;
                    case "Cousine paternelle de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourCousinePaternelle", ""+identifiant);
                        break;
                    case "Neveu paternel de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourNeveuPaternel", ""+identifiant);
                        break;
                    case "Nièce paternelle de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourNiecePaternelle", ""+identifiant);
                        break;
                    case "petit fils paternel de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourPetitEnfantPaternel", ""+identifiant);
                        break;
                    case "Petite fille paternelle de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourPetitEnfantPaternel", ""+identifiant);
                        break;
                    case "Epoux de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourEpoux", ""+identifiant);
                        break;
                    case "Epouse de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourEpouse", ""+identifiant);
                        break;
                    case "Ami de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourAmi", ""+identifiant);
                        break;
                    case "Collègue de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourCollegue", ""+identifiant);
                        break;
                    case "Fils de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourEnfant", ""+identifiant);
                        break;
                    case "Fille de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourEnfant", ""+identifiant);
                        break;
                    case "Oncle maternel de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourOncleMaternel", ""+identifiant);
                        break;
                    case "Tante maternelle de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourTanteMaternelle", ""+identifiant);
                        break;
                    case "Grand père maternel de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourGrandParentMaternel", ""+identifiant);
                        break;
                    case "Grande mère maternelle de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourGrandParentMaternel", ""+identifiant);
                        break;
                    case "Petit fils maternel de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourPetitEnfantMaternel", ""+identifiant);
                        break;
                    case "Petite fille maternelle de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourPetitEnfantMaternel", ""+identifiant);
                        break;
                    case "Cousin maternel" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourCousinMaternel", ""+identifiant);
                        break;
                    case "Cousine maternelle" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourCousineMaternelle", ""+identifiant);
                        break;
                    case "Neveu maternel de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourNeveuMaternel", ""+identifiant);
                        break;
                    case "Nièce maternelle de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourNieceMaternelle", ""+identifiant);
                        break;
                    case "Gendre de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourGendre", ""+identifiant);
                        break;
                    case "Beau père de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourBeauPere", ""+identifiant);
                        break;
                    case "Belle mère de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourbelleMere", ""+identifiant);
                        break;
                    case "Belle soeur de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourBelleSoeur", ""+identifiant);
                        break;
                    case "Belle fille de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourBelleFille", ""+identifiant);
                        break;
                    case "Beau frère de" :
                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id_person_choice, "aPourBeauFrere", ""+identifiant);
                        break;
                    default :
                        // Statements
                        System.out.println("AUCOUN LIEN TROUVEE");
                }

            }

            try {
                out = new FileOutputStream(owlFile);
                modelOntologie.write(out,"N3");
                Intent intent = new Intent(this, ContactShowActivity.class);
                intent.putExtra(CONTACT_SELECT, contact);
                startActivity(intent);
            } catch (FileNotFoundException e) {Toast.makeText(this, "Echec !!", Toast.LENGTH_LONG).show();e.printStackTrace();}
        }

    }

    private void initContact() {
    }


}
