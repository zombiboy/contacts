package wvw.utils.wvw.utils.rdf;

/**
 * Created by William on 16/03/2018.
 */

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class Namespaces {
    protected static final String uri = "http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#";
    private static Model m = ModelFactory.createDefaultModel();
    public static final Resource ORGPROPERTIES;
    public static final Resource ADRTYPES;
    public static final Resource NOM;

    public static final String pza = "https://pizza.org/ontology#";
    public static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String nspacePerson="http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#";

    public Namespaces() {
    }

    public static String getURI() {
        return "http://www.w3.org/2001/vcard-rdf/3.0#";
    }

    static {
        ORGPROPERTIES = m.createResource("http://www.w3.org/2001/vcard-rdf/3.0#ORGPROPERTIES");
        ADRTYPES = m.createResource("http://www.w3.org/2001/vcard-rdf/3.0#ADRTYPES");
        NOM = m.createResource("http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#NOM");
    }
    public static class Contact{
        public static String NOM="nom";
        public static String PRENOM="prenom";
        public static String PHONE="numero1";
        public static String PHONE_2="numero2";
        public static String PHONE_3="numero3";
        public static String SEXE = "sexe";
        public static String EMAIL= "email";
        public static String IDENTIFIANT ="identifiant";
        public static String DATE_NAISSANCE="dateNaissance";

        public static class Property{
            public static final String A_POUR_ENFANT="aPourEnfant";
            public static final String A_POUR_FRERE="aPourFrere";
            public static final String A_POUR_SOEUR="aPourSoeur";

        }

        public static class Liens{
            public static final String PERE_DE="Père de";
            public static final String MERE_DE="Mère de";
            public static final String FRERE_DE="Frère de";
            public static final String SOEUR_DE="Soeur de";
            public static final String EPOUX_DE="Epoux de";
            public static final String EPOUSE_DE="Epouse de";
            public static final String FILS_DE="Fils de";
            public static final String FILLE_DE="Fille de";
            public static final String AMI_DE="Ami de";
            public static final String COLLEGUE_DE="Collègue de";
            public static final String ONCLE_DE="Oncle de";
            public static final String TANTE_DE="Tante de";
            public static final String NEVEU_DE="Neveu de";
            public static final String NIECE_DE="Nièce de";
            public static final String GRAND_PARENT_DE="Grand parent de";
            public static final String PETIT_ENFANT_DE="Petit enfant de";
            public static final String COUSIN_DE="Cousin de";
            public static final String COUSINE_DE="Cousine de";
            public static final String BEAU_PARENT_DE="Beau parent de";
            public static final String BEAU_FRERE_DE="Brother in law of";
            public static final String BELLE_SOEUR_DE="Belle soeur de";
            public static final String GENDRE_DE="Gendre de";
            public static final String BELLE_FILLE_DE="Belle fille de";
        }

    }


}
