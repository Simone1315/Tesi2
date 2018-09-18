package AssetServer;

import Utilities.ConstValues;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;

public class TDBLoader {

    private Dataset dataset;
    private String nameModel;

    public TDBLoader(Dataset dataset, String nameModel)
    {
        this.dataset = dataset;
        this.nameModel = nameModel;

    }

    public void loadData(String fileDirectory)
    {

        StreamRDF destination = new StreamRDF() {
            public void start() {

            }

            public void triple(Triple triple) {
                    System.out.println(triple.getSubject().toString());
                    System.out.println(triple.getPredicate().toString());
                    System.out.println(triple.getObject().toString());
                    addStatement(nameModel, triple.getSubject().toString(),triple.getPredicate().toString(),triple.getObject().toString());
            }

            public void quad(Quad quad) {

            }

            public void base(String base) {

            }

            public void prefix(String prefix, String iri) {

            }

            public void finish() {

            }
        };
        RDFParser.source(fileDirectory).parse(destination);


    }


    private  void addStatement( String modelName, String subject, String property, String object )
    {
        Model model = null;

        dataset.begin( ReadWrite.WRITE );
        try
        {
            model = dataset.getNamedModel( modelName );

            Statement stmt = model.createStatement
                    (
                            model.createResource( subject ),
                            model.createProperty( property ),
                            model.createResource( object )
                    );

            model.add( stmt );
            dataset.commit();
        }
        finally
        {
            //if( model != null ) model.close();
            dataset.end();
        }
    }
}
