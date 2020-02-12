package wvw.utils.wvw.utils.rdf;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import wvw.utils.IOUtils;

public class Utilite {

    public static Model readModel(AssetManager asset, File parent) {
        //creation d'un model vide
        Model model = ModelFactory.createDefaultModel();

        FileInputStream in = null;
        try {
            File file=new File(parent,"ont.owl");
            in= new FileInputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // lecture du fichier RDF/XML
        model.read(in, "","N3");
        try {
            in.close();
        } catch (IOException e) {
            return null;
        }
        return model;

    }

    public static Model readModel(File owlFile) {
        Model model = ModelFactory.createDefaultModel();
        FileInputStream in = null;
        try {
            in= new FileInputStream(owlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        model.read(in, "","N3");
        try {
            in.close();
        } catch (IOException e) {
            return null;
        }
        return model;
    }

    public static Model readModelWithAsset(AssetManager asset, File parent) {
        //creation d'un model vide
        Model model = ModelFactory.createDefaultModel();

        Context context=null;
        // utilisation de FileManager pour trouver le fichier owl
        InputStream in = null;
        try {
            in = asset.open("ont.owl");
            //File file=new File(parent,"ont.owl");
            //in= new FileInputStream(file);
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


    public static InfModel inference(Model model, AssetManager asset)  {

        List<Rule> rules = null;
        try {
            rules = Rule.parseRules(IOUtils.read(asset.open("regles.jena")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        InfModel infModel = ModelFactory.createInfModel(reasoner, model);
        return infModel;

    }

    public static InfModel inference(Model model, Context context)  {
        File rfile =new File(context.getExternalFilesDir(null),"regles.jena");
        List rules = Rule.rulesFromURL(rfile.getAbsolutePath());
        GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
        reasoner.setDerivationLogging(true);
        reasoner.setOWLTranslation(true);               // not needed in RDFS case
        reasoner.setTransitiveClosureCaching(true);
        InfModel inf = ModelFactory.createInfModel(reasoner, model);
        return inf;
    }

    /**
     * La methode permet de filter les resultats en fonction d'un identifiant
     *  Semblable de where en Sql
     * @param requete
     * @param id
     * @return
     */
    public static String gestionRequete(String requete, int id){
        return requete+" filter (xsd:integer(?identif)="+id+")}";
    }


        //gestion des requetes
    public String gestionRequeteRelationType(String requete, int id, int ident){
        return requete+" filter (xsd:integer(?identif)="+id+" && xsd:integer(?identi)="+ident+")}";}

    //Supprimer toutes les valeurs d'une propriete d'une Instance
    public static boolean supprimerValeurProperty(Model model, String namespace, String nomObjet, String nomPropriete) {
        Resource rs = model.getResource(namespace + nomObjet);
        Property p = model.getProperty(namespace + nomPropriete);
        if ((rs != null) && (p != null)) {
            //suppression des valeurs de la propriete
            rs.removeAll(p);
            return true;
        }
        return false;
    }

    //Modifier la valeur d'une propriete objet d'une Instance
    public static boolean modifierValeurObjectProperty(Model model, String namespace, String nomObject1, String nomPropriete, String nomObject2) {
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
    //Modifier la valeur d'une propriete datatype d'une Instance
    public static boolean modifierValeurDataTypeProperty(Model model, String namespace, String nomInstance, String nomPropriete, Object valeur) {
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


}
