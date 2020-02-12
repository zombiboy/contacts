package wvw.mobile.rules;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;

import java.io.File;

import wvw.utils.wvw.utils.rdf.Utilite;

import static wvw.mobile.rules.util.Constant.FILE_ONTOLOGY_NAME;

public class TestActivity extends AppCompatActivity {

    private Model modelOntologie  ;
    private InfModel modeleInf;
    private File owlFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        owlFile=new File(getExternalFilesDir(null),""+FILE_ONTOLOGY_NAME);
        modelOntologie  = Utilite.readModel(owlFile);
        modeleInf= Utilite.inference(modelOntologie,this);

    }
}
