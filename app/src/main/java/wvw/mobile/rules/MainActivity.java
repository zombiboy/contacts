package wvw.mobile.rules;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.vocabulary.VCARD;

import java.io.IOException;
import java.util.List;

import wvw.utils.IOUtils;
import wvw.utils.wvw.utils.rdf.Namespaces;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final Model model = setup();
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                reason(model);
                Intent intent = new Intent(MainActivity.this, ContactListActivity.class);
                startActivity(intent);
            }
        });

        printPizzaTypes(model);

    /**
        // some definitions
        String personURI    = "http://somewhere/JohnSmith";
        String givenName    = "John";
        String familyName   = "Smith";
        String fullName     = givenName + " " + familyName;

// create an empty Model
        Model model = ModelFactory.createDefaultModel();

// create the resource
//   and add the properties cascading style
        Resource johnSmith
                = model.createResource(personURI)
                .addProperty(VCARD.FN, fullName)
                .addProperty(VCARD.N,
                        model.createResource()
                                .addProperty(VCARD.Given, givenName)
                                .addProperty(VCARD.Family, familyName));
        // list the statements in the Model
        StmtIterator iter = model.listStatements();

// print out the predicate, subject and object of each statement
        while (iter.hasNext()) {
            Statement stmt      = iter.nextStatement();  // get next statement
            Resource  subject   = stmt.getSubject();     // get the subject
            Property predicate = stmt.getPredicate();   // get the predicate
            RDFNode   object    = stmt.getObject();      // get the object

            System.out.print(subject.toString());
            System.out.print(" " + predicate.toString() + " ");
            if (object instanceof Resource) {
                System.out.print(object.toString());
            } else {
                // object is a literal
                System.out.print(" \"" + object.toString() + "\"");
            }

            System.out.println(" .");
        }

        //model.write(System.out);
        //model.write(System.out, "N-TRIPLES");**/
    }

    // create Model & load data
    private Model setup() {
        AssetManager assMan = getAssets();

        Model model = ModelFactory.createDefaultModel();
        try {
            // load OWL2 RL axioms
            model.read(assMan.open("axioms.nt"), "", "N-TRIPLE");

            // load ontology + dataset
            model.read(assMan.open("pizza.owl"), "", "N3");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return model;
    }

    // perform reasoning using OWL2 RL ruleset

    private void reason(Model model) {
        Log.d("android-rules","> reasoning");

        AssetManager assMan = getAssets();
        try {
            // load & parse rules
            List<Rule> rules = Rule.parseRules(IOUtils.read(assMan.open("owl2rl.jena")));

            // create inf model
            GenericRuleReasoner reasoner = new GenericRuleReasoner(rules);
            InfModel infModel = ModelFactory.createInfModel(reasoner, model);

            printPizzaTypes(infModel);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // print DominosMargheritaPizza types

    private void printPizzaTypes(Model model) {
        StmtIterator stmtIt = model.listStatements(
                model.createResource(Namespaces.pza + "DominosMargheritaPizza"),
                model.createProperty(Namespaces.rdf + "type"),
                (RDFNode) null);

        StringBuffer buffer = new StringBuffer();
        while (stmtIt.hasNext()) {
            Statement stmt = stmtIt.nextStatement();

            Resource type = (Resource) stmt.getObject();
            if (type.toString().startsWith(Namespaces.pza))
                buffer.append(type).append("\n");
        }

        String str = buffer.toString();
        if (str.trim().equals(""))
            str = "no pizza types .. yet!";

        TextView tv = (TextView) findViewById(R.id.pizzaTypes);
        tv.setText(str);
    }
}
