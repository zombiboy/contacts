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
    public static class Person{
        public static String NOM="NOM";
        public static String PRENOM="PRENOM";

    }
}
