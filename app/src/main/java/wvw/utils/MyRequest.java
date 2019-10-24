package wvw.utils;

public class MyRequest {
    public static String requete="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?id." +
            "?x ns:nom ?nom." +
            "?x ns:prenom ?prenom." +
            "?x ns:numero1 ?numero.}";


    public static String pere="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourPere ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}";


    public static String mere="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourMere ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}";


    public static String fils="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourFils ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}";


    public static String fille="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourFille ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}";


    public static String frere="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourFrere ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}";


    public static String soeur="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourSoeur ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}";


    public static String epoux="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourEpoux ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";


    public static String epouse="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourEpouse ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";


    public static String oncle="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourOncle ?y." +
            "?y ns:identifiant ?id."+
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";


    public static String tante="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourTante ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";


    public static String neveu="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourNeveu ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";


    public static String niece="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourNiece ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";


    public static String grandParent="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourGrandParent ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";


    public static String petitEnfant="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourPetitEnfant ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";

    public static String ami="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourAmi ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";


    public static String collegue="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourCollegue ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";


    public static String cousin="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourCousin ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";


    public static String cousine="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourCousine ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";



    public static String beauParent="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourBeauParent ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";



    public static String gendre="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourGendre ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";




    public static String beauFrere="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourBeauFrere ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";




    public static String belleSoeur="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourBelleSoeur ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";




    public static String belleFille="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?identif." +
            "?x ns:aPourBelleFille ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom." +
            "?y ns:numero1 ?numero.optional{?y ns:email ?email}}";




    public static String homme="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?nom ?prenom ?numero1 ?numero2 ?numero3 ?email ?dateNaissance" +
            " where {?x rdf:type ns:homme." +
            "?x ns:nom ?nom." +
            "?x ns:prenom ?prenom." +
            "?x ns:numero1 ?numero1." +
            "?x ns:dateNaissance ?dateNaissance." +
            "optional{?x ns:numero2 ?numero2.}" +
            "optional{?x ns:numero3 ?numero3.}"
            + "optional{?x ns:email ?email.}"+
            "?x ns:identifiant ?identif";



   public static String femme="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?nom ?prenom ?numero1 ?numero2 ?numero3 ?email ?dateNaissance" +
            " where {?x rdf:type ns:femme." +
            "?x ns:nom ?nom." +
            "?x ns:prenom ?prenom." +
            "?x ns:numero1 ?numero1." +
            "?x ns:dateNaissance ?dateNaissance." +
            "optional{?x ns:numero2 ?numero2.}" +
            "optional{?x ns:numero3 ?numero3.}"
            + "optional{?x ns:email ?email.}"+
            "?x ns:identifiant ?identif";



   public static String numero="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?numero1 ?numero2 ?numero3" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:numero1 ?numero1." +
            "optional{?x ns:numero2 ?numero2." +
            "?x ns:numero3 ?numero3.}"+
            "?x ns:identifiant ?identif}";


public static String relation="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:estEnRelation ?y." +
            "?y ns:identifiant ?id." +
            "?y ns:nom ?nom." +
            "?y ns:prenom ?prenom."+
            "?x ns:identifiant ?identif";


 public static String listePersonne="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero1 ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?id." +
            "?x ns:nom ?nom." +
            "?x ns:prenom ?prenom." +
            "?x ns:numero1 ?numero1."
            + "optional{?x ns:email ?email.}}";
   public static String RechercheContact="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom ?numero1 ?numero2 ?numero3 ?email" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?id." +
            "?x ns:nom ?nom." +
            "?x ns:prenom ?prenom." +
            "?x ns:numero1 ?numero1."
            + "optional{?x ns:numero2 ?numero2.}"
            + "optional{?x ns:numero3 ?numero3.}"
            + "optional{?x ns:email ?email.}}";


   public static String requeteRemplirCombobox="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?id ?nom ?prenom" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:identifiant ?id." +
            "?x ns:nom ?nom." +
            "?x ns:prenom ?prenom.}";

   public static String contact="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
            "PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
            "PREFIX ns:<http://www.semanticweb.org/amed/ontologies/2017/4/untitled-ontology-48#>" +
            "SELECT ?numero1 ?numero2 ?numero3" +
            " where {?x rdf:type ns:Personne." +
            "?x ns:numero1 ?numero1." +
            "optional{?x ns:numero2 ?numero2.}" +
            "optional{?x ns:numero3 ?numero3.}"+
            "?x ns:identifiant ?identif}";
}
