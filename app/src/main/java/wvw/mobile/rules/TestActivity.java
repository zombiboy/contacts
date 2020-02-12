package wvw.mobile.rules;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import wvw.mobile.rules.dto.Contact;
import wvw.utils.MyRequest;
import wvw.utils.wvw.utils.rdf.Utilite;

import static wvw.mobile.rules.util.Constant.FILE_ONTOLOGY_NAME;

public class TestActivity extends AppCompatActivity {

    private Model modelOntologie  ;
    private InfModel modeleInf;
    private File owlFile = null;
    EditText editText1,editText2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        owlFile=new File(getExternalFilesDir(null),""+FILE_ONTOLOGY_NAME);
        modelOntologie  = Utilite.readModel(owlFile);
        modeleInf= Utilite.inference(modelOntologie,this);

        editText1 = (EditText) findViewById(R.id.editText1);
        editText2 = (EditText) findViewById(R.id.editText2);

        getContacts();

    }

    public void getContacts(){

        int nombreContact=0;
        String nom="",prenom="",email="",numero="";

        Query query= QueryFactory.create(MyRequest.listePersonne);
        QueryExecution qexec = QueryExecutionFactory.create(query,modeleInf);

        try  {
            ResultSet resultat=qexec.execSelect();
            while(resultat.hasNext()){
                QuerySolution soln=resultat.nextSolution();
                Contact contact = new Contact();
                nombreContact++;
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
                System.out.println("ID= "+identifiant+" Nom= "+nom+" Prenom= "+prenom);
            }

            System.out.println("Nombre Contact = "+nombreContact);

        }finally{
            qexec.close();
        }

    }


    public void rechercheLien(View v){


        int nombreResult=0;
        int idContact=Integer.parseInt(editText1.getText().toString());
        System.out.println("Le contact saisi est:"+idContact);
        String requet= Utilite.gestionRequete(MyRequest.mere, idContact);//TODO:: A changer
        System.out.println("la requete est = "+requet);
        Query query1=QueryFactory.create(requet);
        QueryExecution qexec1 = QueryExecutionFactory.create(query1,modeleInf);
        try  {
            ResultSet resultat1=qexec1.execSelect();
            while(resultat1.hasNext()){
                QuerySolution soln1=resultat1.nextSolution();

                nombreResult++;
                Contact contact = new Contact();
                contact.setRelationFind(" ");
                String numero1="", email1="", prenom1="";

                Literal li_identifiant1=soln1.getLiteral("id");
                String id1=li_identifiant1.getString();
                contact.setId(id1);
                Literal li_nom1=soln1.getLiteral("nom");
                String nom1=li_nom1.getString();
                contact.setName(nom1);
                Literal li_prenom1=soln1.getLiteral("prenom");
                if(li_prenom1!=null){
                    prenom1=li_prenom1.getString();
                    contact.setPrenom(prenom1);
                }

                Literal li_numero1=soln1.getLiteral("numero");
                if(li_numero1!=null){
                    numero1=li_numero1.getString();
                    contact.setPhone(numero1);
                }
                Literal li_email1=soln1.getLiteral("email");
                if(li_email1!=null){
                    email1=li_email1.getString();
                    contact.setEmail(email1);
                }

                System.out.println("Personne trouve: Nom = "+nom1+" Prenom = "+prenom1+" Numero = "+numero1);

            }
        }finally{
            qexec1.close();
        }

        System.out.println("Nombre trouve:"+nombreResult);
    }

    public void findKnowsPerson(View v) {

        int idContact=Integer.parseInt(editText2.getText().toString()); //TODO A remplacer

        System.out.println("Le contact saisi est:"+idContact);

        System.out.println("++++++++++++++++Connaissance de la personne++++++++++++++++++++++");
        String reqDesPersonneEnRelation=Utilite.gestionRequete(MyRequest.relation, idContact);
        Query query2=QueryFactory.create(reqDesPersonneEnRelation);
        QueryExecution qexec2 = QueryExecutionFactory.create(query2,modeleInf);
        try  {
            ResultSet resultat1 = qexec2.execSelect();
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
                String lien=findRelationType(idTrouver,idContact);
                System.out.println("TYPE RELATION: "+lien);

            }
        }finally{
            qexec2.close();
        }

    }

    private String findRelationType(String idTrouver,int idContactSelected){
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

    public String relationType(String idTrouver,int idContactSelected,String reque){

        int id_trouver=Integer.parseInt(idTrouver);

        String req=gestionRequeteRelationType(reque, id_trouver, idContactSelected);
        String nom="";
        Query query3=QueryFactory.create(req);
        QueryExecution qexec3 = QueryExecutionFactory.create(query3,modeleInf);
        try  {
            ResultSet resultat=qexec3.execSelect();
            while(resultat.hasNext()){

                QuerySolution soln=resultat.nextSolution();
                Literal nom_li=soln.getLiteral("nom");
                nom=""+nom_li;
                return nom;
            }
        }finally{
            qexec3.close();}
        return ("null");

    }

    public String gestionRequeteRelationType(String requete,int id_trouver,int id_selected){
        return requete+" filter (xsd:integer(?identif)="+id_trouver+" && xsd:integer(?identi)="+id_selected+")}";
    }

}
