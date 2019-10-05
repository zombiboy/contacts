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

import wvw.utils.Civilite;
import wvw.utils.IOUtils;
import wvw.utils.MyRequest;
import wvw.utils.wvw.utils.rdf.Namespaces;

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


    public void saveContact2(View view){
        int identifiant=0;
        identifiant = identifiant()+1;

        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
        // add the property

        ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", "KABORE");
        ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
        ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", "KALIFA");
        ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", "78787878");

        try {
            out = new FileOutputStream(owlFile);
            modelOntologie.write(out,"N3");
            Intent intent = new Intent(this, ContactShowActivity.class);
            startActivity(intent);
        } catch (FileNotFoundException e) {Toast.makeText(this, "Echec !!", Toast.LENGTH_LONG).show();e.printStackTrace();}

    }

    public void saveContact(View view) {
        String nom = String.valueOf(txtNom.getText());// Partie declaration et initialisation de variables //
        String prenom = String.valueOf(txtPrenom.getText());
        String numero = String.valueOf(txtTelephone.getText());
        String email = String.valueOf(txtEmail.getText());
        String naissance = String.valueOf(txtNaissance.getText());
        String sexe = String.valueOf(spinSexe.getSelectedItem().toString());
        String b_relation = String.valueOf(spinRelation.getSelectedItem().toString());


        int identifiant=0;

        identifiant = identifiant()+1;

        /**
         * Partie bloc de contrôle
         */

        try {

            if (nom.isEmpty() | prenom.isEmpty() | numero.isEmpty()) {
                Toast.makeText(ContactAddActivity.this, "Impossible d'enregistrer!!", Toast.LENGTH_LONG).show();
            } else {
                /**
                 * Enregistrement d'un homme
                 */

                //Enregistrement homme sans email ni relation sociale
                if (email.isEmpty() && sexe.contains("Homme") && b_relation.contains("Choisir")) {


                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                    ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                    ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                    ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                    ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                    ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);


                    out = new FileOutputStream(owlFile);
                    modelOntologie.write(out,"N3");
                    //TODO:: Redirigé vers le conatct creer
                    Toast.makeText(ContactAddActivity.this, "Enregistrer avec succès!!  ", Toast.LENGTH_LONG).show();

                }

                else {
                    //Enregistrement homme avec email et sans relation sociale
                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Choisir")) {

                        // Toast.makeText(ContactAddActivity.this, "Enregistrer avec succès!!", Toast.LENGTH_LONG).show();
                        creerInstanceDeClasse( modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                        ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                        ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                        ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                        ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                        ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                        ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);

                        out = new FileOutputStream(owlFile);
                        modelOntologie.write(out,"N3");
                        Toast.makeText(ContactAddActivity.this, "Enregistrer avec succès!!  ", Toast.LENGTH_LONG).show();

                    } else {
                        /**
                         * Enregistrement d'une femme
                         */

                        //Enregistrement femme sans email ni relation sociale
                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Choisir")) {

                            // Toast.makeText(ContactAddActivity.this, "Enregistrer avec succès!!", Toast.LENGTH_LONG).show();
                            creerInstanceDeClasse( modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                            ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                            ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                            ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                            ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                            ajouterValeurDataTypeProperty( modelOntologie, Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);

                            out = new FileOutputStream(owlFile);
                            modelOntologie.write(out,"N3");
                            Toast.makeText(ContactAddActivity.this, "Enregistrer avec succès!!  ", Toast.LENGTH_LONG).show();

                        } else {
                            //Enregistrement femme avec email et sans relation sociale
                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Choisir")) {

                                //   Toast.makeText(ContactAddActivity.this, "Enregistrer avec succès!!", Toast.LENGTH_LONG).show();
                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);

                                out = new FileOutputStream(owlFile);
                                modelOntologie.write(out,"N3");
                                Toast.makeText(ContactAddActivity.this, "Enregistrer avec succès!!  ", Toast.LENGTH_LONG).show();


                            }

                            else{

                                String choix_relation=spinPropriete.getSelectedItem().toString();
                                String id=id_existant(choix_relation);//identifiant de l'enregistrement existant
                                //TODO::ALGO a revoir en recuperant directement l ID depuis la liste deroulante

                                /**
                                 * Enregistrement d'un père
                                 */


                                //Enregistrement père sans email
                                if(!(nom.isEmpty()&&prenom.isEmpty()&&numero.isEmpty())&&email.isEmpty()&&sexe.contains("Homme")&&b_relation.contains("Père")){

                                    //  Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "aPourEnfant", id);
                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                }else{
                                    //Enregistrement père avec email
                                    if(!(nom.isEmpty()&&prenom.isEmpty()&&numero.isEmpty()&&email.isEmpty())&&sexe.contains("Homme")&&b_relation.contains("Père")){

                                        //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email", email);
                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "aPourEnfant", id);
                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);
                                        System.out.println("SaveToDaron"+identifiant);

                                    }else{
                                        /**
                                         * Enregistrement d'une mère
                                         */

                                        //Enregistrement mère sans email
                                        if(!(nom.isEmpty()&&prenom.isEmpty()&&numero.isEmpty())&&email.isEmpty()&&sexe.contains("Femme")&&b_relation.contains("Mère")){

                                            //       Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                            creerInstanceDeClasse(modeleInf,Namespaces.nspacePerson, "femme", ""+identifiant);
                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "aPourEnfant", id);
                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                        }else{
                                            //Enregistrement mère avec email
                                            if(!(nom.isEmpty()&&prenom.isEmpty()&&numero.isEmpty()&&email.isEmpty())&&sexe.contains("Femme")&&b_relation.contains("Mère")){

                                                //      Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "aPourEnfant", id);
                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                            }else{
                                                //==========================
                                                //Enregistrement d'un frère//
                                                //===========================

                                                //Enregistrement frère sans email
                                                if(!(nom.isEmpty()&&prenom.isEmpty()&&numero.isEmpty())&&email.isEmpty()&&sexe.contains("Homme")&&b_relation.contains("Frère")){

                                                    //       Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                    ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourFrere", ""+identifiant);
                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                }else{
                                                    //Enregistrement frère avec email
                                                    if(!(nom.isEmpty()&&prenom.isEmpty()&&numero.isEmpty()&&email.isEmpty())&&sexe.contains("Homme")&&b_relation.contains("Frère")){

                                                        //       Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourFrere", ""+identifiant);
                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                    }else{
                                                        //==========================
                                                        //Enregistrement d'une soeur//
                                                        //===========================

                                                        //Enregistrement soeur sans email
                                                        if(!(nom.isEmpty()&&prenom.isEmpty()&&numero.isEmpty())&&email.isEmpty()&&sexe.contains("Femme")&&b_relation.contains("Soeur")){

                                                            //      Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourSoeur", ""+identifiant);
                                                            ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                        }else{
                                                            //Enregistrement soeur avec email
                                                            if(!(nom.isEmpty()&&prenom.isEmpty()&&numero.isEmpty()&&email.isEmpty())&&sexe.contains("Femme")&&b_relation.contains("Soeur")){

                                                                //        Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourSoeur", ""+identifiant);
                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                            }else{
                                                                //====================================
                                                                //Enregistrement d'un oncle paternel//
                                                                //==================================

                                                                //Enregistrement oncle paternel sans email
                                                                if(!(nom.isEmpty()&&prenom.isEmpty()&&numero.isEmpty())&&email.isEmpty()&&sexe.contains("Homme")&&b_relation.contains("Oncle paternel")){

                                                                    //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourOnclePaternel", ""+identifiant);
                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                }else{
                                                                    //Enregistrement oncle paternel avec email
                                                                    if(!(nom.isEmpty()&&prenom.isEmpty()&&numero.isEmpty()&&email.isEmpty())&&sexe.contains("Homme")&&b_relation.contains("Oncle paternel")){

                                                                        //        Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                        ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, id, "aPourOnclePaternel", ""+identifiant);
                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                    }else{
                                                                        //=======================================
                                                                        //Enregistrement d'une tante paternelle//
                                                                        //=====================================

                                                                        //Enregistrement tante paternelle sans email
                                                                        if(!(nom.isEmpty()&&prenom.isEmpty()&&numero.isEmpty())&&email.isEmpty()&&sexe.contains("Femme")&&b_relation.contains("Tante paternelle")){

                                                                            //      Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                            ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourTantePaternelle", ""+identifiant);
                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                        }else{
                                                                            //Enregistrement tante paternelle avec email
                                                                            if(!(nom.isEmpty()&&prenom.isEmpty()&&numero.isEmpty()&&email.isEmpty())&&sexe.contains("Femme")&&b_relation.contains("Tante paternelle")){

                                                                                //       Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourTantePaternelle", ""+identifiant);
                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                            }else{

                                                                                //=========================================
                                                                                //Enregistrement d'un grand père paternel//
                                                                                //=======================================

                                                                                //Enregistrement grand père paternel sans email

                                                                                if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Homme") && b_relation.contains("Grand père paternel")) {

                                                                                    //      Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourGrandParentPaternel", ""+identifiant);
                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                }else{

                                                                                    //Enregistrement grand père paternel avec email
                                                                                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Grand père paternel")) {

                                                                                        //         Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourGrandParentPaternel", ""+identifiant);
                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                    }else{

                                                                                        //==============================================
                                                                                        //Enregistrement d'une grande mère paternelle //
                                                                                        //=============================================

                                                                                        //Enregistrement grand mère paternelle sans email

                                                                                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Grande mère paternelle")) {

                                                                                            //    Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                                            ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                            ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourGrandParentPaternel", ""+identifiant);
                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                        }else{

                                                                                            //Enregistrement grand mère avec email
                                                                                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Grande mère paternelle")) {

                                                                                                //    Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourGrandParentPaternel", ""+identifiant);
                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                            }else{

                                                                                                //=====================================
                                                                                                //Enregistrement d'un cousin paternel//
                                                                                                //====================================

                                                                                                //Enregistrement cousin paternel sans email
                                                                                                if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Homme") && b_relation.contains("Cousin paternel")) {

                                                                                                    //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                    ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourCousinPaternel", ""+identifiant);
                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                } else {

                                                                                                    //Enregistrement cousin paternel avec email
                                                                                                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Cousin paternel")) {

                                                                                                        //        Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourCousinPaternel", ""+identifiant);
                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                    } else {

                                                                                                        //=========================================
                                                                                                        //Enregistrement d'une cousine paternelle//
                                                                                                        //======================================

                                                                                                        //Enregistrement cousine paternelle sans email
                                                                                                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Cousine paternelle")) {

                                                                                                            //           Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourCousinePaternelle", ""+identifiant);
                                                                                                            ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                        } else {

                                                                                                            //Enregistrement cousine paternelle avec email
                                                                                                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Cousine paternelle")) {

                                                                                                                //  Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                ajouterValeurObjectProperty(modeleInf,Namespaces.nspacePerson, id, "aPourCousinePaternelle", ""+identifiant);
                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                            }else{


                                                                                                                //====================================
                                                                                                                //Enregistrement d'un neveu paternel//
                                                                                                                //===================================
                                                                                                                //Enregistrement neveu paternel sans email
                                                                                                                if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Homme") && b_relation.contains("Neveu paternel")) {

                                                                                                                    //    Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourNeveuPaternel", ""+identifiant);
                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                } else {

                                                                                                                    //Enregistrement neveu paternel avec email
                                                                                                                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Neveu paternel")) {

                                                                                                                        //  Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                        ajouterValeurObjectProperty(modeleInf, Namespaces.nspacePerson, id, "aPourNeveuPaternel", ""+identifiant);
                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                    } else {

                                                                                                                        //=======================================
                                                                                                                        //Enregistrement d'une niece paternelle//
                                                                                                                        //=====================================
                                                                                                                        //Enregistrement niece paternelle sans email
                                                                                                                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Nièce paternelle")) {

                                                                                                                            //   Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourNiecePaternelle", ""+identifiant);
                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                        } else {

                                                                                                                            //Enregistrement niece paternelle avec email
                                                                                                                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Nièce paternelle")) {

                                                                                                                                //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourNiecePaternelle", ""+identifiant);
                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                            }else{

                                                                                                                                //=========================================
                                                                                                                                //Enregistrement d'un petit fils paternel//
                                                                                                                                //=======================================

                                                                                                                                //Enregistrement petit fils paternel sans email
                                                                                                                                if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Homme") && b_relation.contains("petit fils paternel")) {

                                                                                                                                    //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                    ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, id, "aPourPetitEnfantPaternel", ""+identifiant);
                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                } else {

                                                                                                                                    //Enregistrement petit fils paternel avec email
                                                                                                                                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Petit fils paternel")) {

                                                                                                                                        //    Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                        creerInstanceDeClasse(modeleInf,Namespaces.nspacePerson, "homme", ""+identifiant);
                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourPetitEnfantPaternel", ""+identifiant);
                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                    } else {

                                                                                                                                        //==============================================
                                                                                                                                        //Enregistrement d'une petite fille paternelle//
                                                                                                                                        //============================================

                                                                                                                                        //Enregistrement petite fille paternelle sans email
                                                                                                                                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Petite fille paternelle")) {

                                                                                                                                            // Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                            ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, id, "aPourPetitEnfantPaternel", ""+identifiant);
                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                        } else {

                                                                                                                                            //Enregistrement petite fille paternelle avec email
                                                                                                                                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Petite fille paternelle")) {

                                                                                                                                                //   Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourPetitEnfantPaternel", ""+identifiant);
                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                            }else{

                                                                                                                                                //============================
                                                                                                                                                //Enregistrement d'un epoux //
                                                                                                                                                //===========================
                                                                                                                                                //Enregistrement epoux sans email
                                                                                                                                                if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Homme") && b_relation.contains("Epoux")) {

                                                                                                                                                    //   Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourEpoux", ""+identifiant);
                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                                } else {

                                                                                                                                                    //Enregistrement epoux avec email
                                                                                                                                                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Epoux")) {

                                                                                                                                                        //    Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson,""+identifiant, "email", email);
                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourEpoux", ""+identifiant);
                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                                    } else {

                                                                                                                                                        //==============================
                                                                                                                                                        //Enregistrement d'une epouse //
                                                                                                                                                        //=============================

                                                                                                                                                        //Enregistrement epouse sans email
                                                                                                                                                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Epouse")) {

                                                                                                                                                            //       Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourEpouse", ""+identifiant);
                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                                        } else {

                                                                                                                                                            //Enregistrement epouse avec email
                                                                                                                                                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Epouse")) {

                                                                                                                                                                //        Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourEpouse", ""+identifiant);
                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                                            }else{



                                                                                                                                                                //==========================
                                                                                                                                                                //Enregistrement d'un ami //
                                                                                                                                                                //==========================

                                                                                                                                                                //Enregistrement ami sans email
                                                                                                                                                                if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Homme") && b_relation.contains("Ami")) {

                                                                                                                                                                    //        Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourAmi", ""+identifiant);
                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                                                } else {

                                                                                                                                                                    //Enregistrement ami avec email
                                                                                                                                                                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Ami")) {

                                                                                                                                                                        //      Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourAmi", ""+identifiant);
                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                                                    } else {

                                                                                                                                                                        //==============================
                                                                                                                                                                        //Enregistrement d'une amie //
                                                                                                                                                                        //=============================
                                                                                                                                                                        //Enregistrement amie sans email
                                                                                                                                                                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Ami")) {

                                                                                                                                                                            //       Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                            creerInstanceDeClasse(modeleInf,Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourAmi", ""+identifiant);
                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                                                        } else {

                                                                                                                                                                            //Enregistrement amie avec email
                                                                                                                                                                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Ami")) {

                                                                                                                                                                                //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourAmi", ""+identifiant);
                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                                                            }else{



                                                                                                                                                                                //===============================
                                                                                                                                                                                //Enregistrement d'un Collegue //
                                                                                                                                                                                //==============================

                                                                                                                                                                                //Enregistrement Collegue sans email
                                                                                                                                                                                if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Homme") && b_relation.contains("Collègue")) {

                                                                                                                                                                                    //       Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourCollegue", ""+identifiant);
                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                                                                } else {

                                                                                                                                                                                    //Enregistrement Collegue avec email
                                                                                                                                                                                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Collègue")) {

                                                                                                                                                                                        //      Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", ""+identifiant);
                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourCollegue", ""+identifiant);
                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                                                                    } else {

                                                                                                                                                                                        //==============================
                                                                                                                                                                                        //Enregistrement d'une Collegue //
                                                                                                                                                                                        //=============================

                                                                                                                                                                                        //Enregistrement Collegue sans email
                                                                                                                                                                                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Collègue")) {

                                                                                                                                                                                            //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourCollegue", ""+identifiant);
                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                                                                        } else {

                                                                                                                                                                                            //Enregistrement Collegue avec email
                                                                                                                                                                                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Collègue")) {

                                                                                                                                                                                                //    Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", ""+identifiant);
                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "nom", nom);
                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "identifiant", identifiant);
                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "email", email);
                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "prenom", prenom);
                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "numero1", numero);
                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourCollegue", ""+identifiant);
                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                                                                            }else{
                                                                                                                                                                                                //===========================
                                                                                                                                                                                                //Enregistrement d'un fils //
                                                                                                                                                                                                //==========================

                                                                                                                                                                                                //Enregistrement fils sans email
                                                                                                                                                                                                if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Homme") && b_relation.contains("Fils")) {

                                                                                                                                                                                                    //  Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourEnfant", "" + identifiant);
                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                                                                                }else{


                                                                                                                                                                                                    //Enregistrement fils avec email
                                                                                                                                                                                                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Fils")) {

                                                                                                                                                                                                        //    Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourEnfant", "" + identifiant);
                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                                                                                    }else{


                                                                                                                                                                                                        //==============================
                                                                                                                                                                                                        //Enregistrement d'une fille //
                                                                                                                                                                                                        //=============================
                                                                                                                                                                                                        //Enregistrement fille sans email
                                                                                                                                                                                                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Fille")) {

                                                                                                                                                                                                            //       Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourEnfant", "" + identifiant);
                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                                                                                        }else{


                                                                                                                                                                                                            //Enregistrement fille avec email
                                                                                                                                                                                                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Fille")) {

                                                                                                                                                                                                                //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourEnfant", "" + identifiant);
                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "estEnRelation", id);

                                                                                                                                                                                                            }else{


                                                                                                                                                                                                                //=====================================
                                                                                                                                                                                                                //Enregistrement d'un oncle maternel //
                                                                                                                                                                                                                //====================================
                                                                                                                                                                                                                //Enregistrement oncle maternel sans email
                                                                                                                                                                                                                if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Homme") && b_relation.contains("Oncle maternel")) {

                                                                                                                                                                                                                    //   Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourOncleMaternel", "" + identifiant);
                                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                } else {

                                                                                                                                                                                                                    //Enregistrement oncle maternel avec email
                                                                                                                                                                                                                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Oncle maternel")) {

                                                                                                                                                                                                                        //   Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourOncleMaternel", "" + identifiant);
                                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                    } else {

                                                                                                                                                                                                                        //========================================
                                                                                                                                                                                                                        //Enregistrement d'une tante maternelle //
                                                                                                                                                                                                                        //======================================
                                                                                                                                                                                                                        //Enregistrement tante maternelle sans email
                                                                                                                                                                                                                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Tante maternelle")) {

                                                                                                                                                                                                                            //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourTanteMaternelle", "" + identifiant);
                                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                        } else {

                                                                                                                                                                                                                            //Enregistrement tante maternelle avec email
                                                                                                                                                                                                                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Tante maternelle")) {

                                                                                                                                                                                                                                //    Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourTanteMaternelle", "" + identifiant);
                                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                            }else{


                                                                                                                                                                                                                                //==========================================
                                                                                                                                                                                                                                //Enregistrement d'un grand père maternel //
                                                                                                                                                                                                                                //========================================
                                                                                                                                                                                                                                //Enregistrement grand père maternel sans email
                                                                                                                                                                                                                                if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Homme") && b_relation.contains("Grand père maternel")) {

                                                                                                                                                                                                                                    //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, id, "aPourGrandParentMaternel", "" + identifiant);
                                                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                } else {

                                                                                                                                                                                                                                    //Enregistrement grand père maternel avec email
                                                                                                                                                                                                                                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Grand père maternel")) {

                                                                                                                                                                                                                                        //      Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourGrandParentMaternel", "" + identifiant);
                                                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                    } else {

                                                                                                                                                                                                                                        //=============================================
                                                                                                                                                                                                                                        //Enregistrement d'une grande mère maternelle //
                                                                                                                                                                                                                                        //============================================
                                                                                                                                                                                                                                        //Enregistrement grande mère maternelle sans email
                                                                                                                                                                                                                                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Grande mère maternelle")) {

                                                                                                                                                                                                                                            //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourGrandParentMaternel", "" + identifiant);
                                                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                        } else {

                                                                                                                                                                                                                                            //Enregistrement grande mère maternelle avec email
                                                                                                                                                                                                                                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Grande mère maternelle")) {

                                                                                                                                                                                                                                                //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourGrandParentMaternel", "" + identifiant);
                                                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                            }else{


                                                                                                                                                                                                                                                //==========================================
                                                                                                                                                                                                                                                //Enregistrement d'un petit fils maternel //
                                                                                                                                                                                                                                                //=========================================
                                                                                                                                                                                                                                                //Enregistrement petit fils maternel sans email
                                                                                                                                                                                                                                                if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Homme") && b_relation.contains("Petit fils maternel")) {

                                                                                                                                                                                                                                                    //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourPetitEnfantMaternel", ""+identifiant);
                                                                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                } else {

                                                                                                                                                                                                                                                    //Enregistrement petit fils maternel avec email
                                                                                                                                                                                                                                                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Petit fils maternel")) {

                                                                                                                                                                                                                                                        //       Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson,id, "aPourPetitEnfantMaternel", ""+identifiant);
                                                                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                    } else {

                                                                                                                                                                                                                                                        //==============================================
                                                                                                                                                                                                                                                        //Enregistrement d'une petite fille maternelle //
                                                                                                                                                                                                                                                        //=============================================
                                                                                                                                                                                                                                                        //Enregistrement petite fille maternelle sans email
                                                                                                                                                                                                                                                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Petite fille maternelle")) {

                                                                                                                                                                                                                                                            //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, id, "aPourPetitEnfantMaternel", "" + identifiant);
                                                                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                        } else {

                                                                                                                                                                                                                                                            //Enregistrement petite fille maternelle avec email
                                                                                                                                                                                                                                                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Petite fille maternelle")) {

                                                                                                                                                                                                                                                                //      Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourPetitEnfantMaternel", "" + identifiant);
                                                                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                            }else{


                                                                                                                                                                                                                                                                //=======================================
                                                                                                                                                                                                                                                                //Enregistrement d'un cousin maternel //
                                                                                                                                                                                                                                                                //=====================================
                                                                                                                                                                                                                                                                //Enregistrement cousin maternel sans email
                                                                                                                                                                                                                                                                if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Homme") && b_relation.contains("Cousin maternel")) {

                                                                                                                                                                                                                                                                    //    Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourCousinMaternel", "" + identifiant);
                                                                                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                } else {

                                                                                                                                                                                                                                                                    //Enregistrement cousin maternel avec email
                                                                                                                                                                                                                                                                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Cousin maternel")) {

                                                                                                                                                                                                                                                                        //      Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourCousinMaternel", "" + identifiant);
                                                                                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                    } else {

                                                                                                                                                                                                                                                                        //==========================================
                                                                                                                                                                                                                                                                        //Enregistrement d'une cousine maternelle //
                                                                                                                                                                                                                                                                        //=========================================
                                                                                                                                                                                                                                                                        //Enregistrement cousine maternelle sans email
                                                                                                                                                                                                                                                                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Cousine maternelle")) {

                                                                                                                                                                                                                                                                            //         Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourCousineMaternelle", "" + identifiant);
                                                                                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                        } else {

                                                                                                                                                                                                                                                                            //Enregistrement cousine maternelle avec email
                                                                                                                                                                                                                                                                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Cousine maternelle")) {

                                                                                                                                                                                                                                                                                //      Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourCousineMaternelle", "" + identifiant);
                                                                                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                            }else{


                                                                                                                                                                                                                                                                                //=====================================
                                                                                                                                                                                                                                                                                //Enregistrement d'un neveu maternel //
                                                                                                                                                                                                                                                                                //====================================
                                                                                                                                                                                                                                                                                //Enregistrement neveu maternel sans email
                                                                                                                                                                                                                                                                                if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Homme") && b_relation.contains("Neveu maternel")) {

                                                                                                                                                                                                                                                                                    //  Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourNeveuMaternel", "" + identifiant);
                                                                                                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                                } else {

                                                                                                                                                                                                                                                                                    //Enregistrement neveu maternel avec email
                                                                                                                                                                                                                                                                                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Neveu maternel")) {

                                                                                                                                                                                                                                                                                        //  Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourNeveuMaternel", "" + identifiant);
                                                                                                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                                    } else {

                                                                                                                                                                                                                                                                                        //========================================
                                                                                                                                                                                                                                                                                        //Enregistrement d'une nièce maternelle //
                                                                                                                                                                                                                                                                                        //=======================================
                                                                                                                                                                                                                                                                                        //Enregistrement nièce maternelle sans email
                                                                                                                                                                                                                                                                                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Nièce maternelle")) {

                                                                                                                                                                                                                                                                                            //  Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourNieceMaternelle", "" + identifiant);
                                                                                                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                                        } else {

                                                                                                                                                                                                                                                                                            //Enregistrement nièce maternelle avec email
                                                                                                                                                                                                                                                                                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Nièce maternelle")) {

                                                                                                                                                                                                                                                                                                //    Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, ""+identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourNieceMaternelle", "" + identifiant);
                                                                                                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                                            } else{

                                                                                                                                                                                                                                                                                                //=====================================
                                                                                                                                                                                                                                                                                                //Enregistrement d'un gendre          //
                                                                                                                                                                                                                                                                                                //====================================
                                                                                                                                                                                                                                                                                                //Enregistrement d'un gendre sans email
                                                                                                                                                                                                                                                                                                if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Homme") && b_relation.contains("Gendre")) {

                                                                                                                                                                                                                                                                                                    //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourGendre", "" + identifiant);
                                                                                                                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                                                } else {

                                                                                                                                                                                                                                                                                                    //Enregistrement d'un gendre avec email
                                                                                                                                                                                                                                                                                                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Gendre")) {

                                                                                                                                                                                                                                                                                                        //   Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, id, "aPourGendre", "" + identifiant);
                                                                                                                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                                                    }else{

                                                                                                                                                                                                                                                                                                        //=====================================
                                                                                                                                                                                                                                                                                                        //Enregistrement d'un Beau père        //
                                                                                                                                                                                                                                                                                                        //====================================
                                                                                                                                                                                                                                                                                                        //Enregistrement d'un Beau père sans email
                                                                                                                                                                                                                                                                                                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Homme") && b_relation.contains("Beau père")) {

                                                                                                                                                                                                                                                                                                            //   Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourBeauPere", "" + identifiant);
                                                                                                                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                                                        } else {

                                                                                                                                                                                                                                                                                                            //Enregistrement d'un Beau père avec email
                                                                                                                                                                                                                                                                                                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Beau père")) {

                                                                                                                                                                                                                                                                                                                //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourBeauPere", "" + identifiant);
                                                                                                                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                                                            }else{

                                                                                                                                                                                                                                                                                                                //=====================================
                                                                                                                                                                                                                                                                                                                //Enregistrement d'une Belle mère     //
                                                                                                                                                                                                                                                                                                                //====================================
                                                                                                                                                                                                                                                                                                                //Enregistrement d'une Belle mère sans email
                                                                                                                                                                                                                                                                                                                if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Belle mère")) {

                                                                                                                                                                                                                                                                                                                    //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, id, "aPourbelleMere", "" + identifiant);
                                                                                                                                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                                                                } else {

                                                                                                                                                                                                                                                                                                                    //Enregistrement d'une Belle mère avec email
                                                                                                                                                                                                                                                                                                                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Belle mère")) {

                                                                                                                                                                                                                                                                                                                        //      Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                                                        creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourBelleMere", "" + identifiant);
                                                                                                                                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                                                                    }else{

                                                                                                                                                                                                                                                                                                                        //=====================================
                                                                                                                                                                                                                                                                                                                        //Enregistrement d'une Belle soeur    //
                                                                                                                                                                                                                                                                                                                        //====================================
                                                                                                                                                                                                                                                                                                                        //Enregistrement d'une Belle soeur sans email
                                                                                                                                                                                                                                                                                                                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Belle soeur")) {

                                                                                                                                                                                                                                                                                                                            //       Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourBelleSoeur", "" + identifiant);
                                                                                                                                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                                                                        } else {

                                                                                                                                                                                                                                                                                                                            //Enregistrement d'une Belle soeur avec email
                                                                                                                                                                                                                                                                                                                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Belle soeur")) {

                                                                                                                                                                                                                                                                                                                                //      Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                                                                creerInstanceDeClasse(modeleInf,Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourBelleSoeur", "" + identifiant);
                                                                                                                                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                                                                            }else{

                                                                                                                                                                                                                                                                                                                                //=====================================
                                                                                                                                                                                                                                                                                                                                //Enregistrement d'une Belle fille   //
                                                                                                                                                                                                                                                                                                                                //====================================
                                                                                                                                                                                                                                                                                                                                //Enregistrement d'une Belle fille sans email
                                                                                                                                                                                                                                                                                                                                if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Femme") && b_relation.contains("Belle fille")) {

                                                                                                                                                                                                                                                                                                                                    //        Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                                                                    creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                                                                    ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourBelleFille", "" + identifiant);
                                                                                                                                                                                                                                                                                                                                    ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                                                                                } else {

                                                                                                                                                                                                                                                                                                                                    //Enregistrement d'une Belle fille avec email
                                                                                                                                                                                                                                                                                                                                    if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Femme") && b_relation.contains("Belle fille")) {

                                                                                                                                                                                                                                                                                                                                        //     Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                                                                        creerInstanceDeClasse(modeleInf,Namespaces.nspacePerson, "femme", "" + identifiant);
                                                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                                                                        ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, id, "aPourBelleFille", "" + identifiant);
                                                                                                                                                                                                                                                                                                                                        ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                                                                                    }else{

                                                                                                                                                                                                                                                                                                                                        //=====================================
                                                                                                                                                                                                                                                                                                                                        //Enregistrement d'un Beau frère      //
                                                                                                                                                                                                                                                                                                                                        //====================================
                                                                                                                                                                                                                                                                                                                                        //Enregistrement d'un Beau frère sans email
                                                                                                                                                                                                                                                                                                                                        if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty()) && email.isEmpty() && sexe.contains("Homme") && b_relation.contains("Beau frère")) {

                                                                                                                                                                                                                                                                                                                                            //      Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                                                                            creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                                                                            ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourBeauFrere", "" + identifiant);
                                                                                                                                                                                                                                                                                                                                            ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                                                                                        } else {

                                                                                                                                                                                                                                                                                                                                            //Enregistrement d'un Beau frère avec email
                                                                                                                                                                                                                                                                                                                                            if (!(nom.isEmpty() && prenom.isEmpty() && numero.isEmpty() && email.isEmpty()) && sexe.contains("Homme") && b_relation.contains("Beau frère")) {

                                                                                                                                                                                                                                                                                                                                                //      Toast.makeText(ContactAddActivity.this,"Enregistrer avec succès!!",Toast.LENGTH_LONG).show();
                                                                                                                                                                                                                                                                                                                                                creerInstanceDeClasse(modeleInf, Namespaces.nspacePerson, "homme", "" + identifiant);
                                                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "nom", nom);
                                                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "identifiant", identifiant);
                                                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "email", email);
                                                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "prenom", prenom);
                                                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "numero1", numero);
                                                                                                                                                                                                                                                                                                                                                ajouterValeurDataTypeProperty(modelOntologie, Namespaces.nspacePerson, "" + identifiant, "dateNaissance", naissance);

                                                                                                                                                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie, Namespaces.nspacePerson, id, "aPourBeauFrere", "" + identifiant);
                                                                                                                                                                                                                                                                                                                                                ajouterValeurObjectProperty(modelOntologie,Namespaces.nspacePerson, "" + identifiant, "estEnRelation", id);

                                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                                            }
                                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                                            }

                                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                                            }

                                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                                }
                                                                                                                                                                                                                                            }

                                                                                                                                                                                                                                        }
                                                                                                                                                                                                                                    }
                                                                                                                                                                                                                                }
                                                                                                                                                                                                                            }
                                                                                                                                                                                                                        }
                                                                                                                                                                                                                    }
                                                                                                                                                                                                                }
                                                                                                                                                                                                            }
                                                                                                                                                                                                        }
                                                                                                                                                                                                    }
                                                                                                                                                                                                }
                                                                                                                                                                                            }
                                                                                                                                                                                        }

                                                                                                                                                                                    }
                                                                                                                                                                                }
                                                                                                                                                                            }
                                                                                                                                                                        }

                                                                                                                                                                    }
                                                                                                                                                                }
                                                                                                                                                            }
                                                                                                                                                        }

                                                                                                                                                    }
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                        }

                                                                                                                                    }
                                                                                                                                }
                                                                                                                            }
                                                                                                                        }

                                                                                                                    }
                                                                                                                }
                                                                                                            }

                                                                                                        }

                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }

                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    out = new FileOutputStream(owlFile);
                    modelOntologie.write(out,"N3");
                    System.out.println("Enregistrement a la fin");
                    Toast.makeText(ContactAddActivity.this, "Enregistrer avec succès!!  ", Toast.LENGTH_LONG).show();
                }
            }

        }
        catch(Exception e)

        {
            e.printStackTrace();
        }

    }
}
