package GlobalClasses;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;

import java.util.ArrayList;
import java.util.List;

public class TDBDataset {

    private Dataset dataset;

    public TDBDataset(String directory)
    {
        dataset  = TDBFactory.createDataset( directory );
    }


    public  void addStatementToTDB(String modelName, String subject, String property, String object )
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

    public  List<Statement> getStatements(String modelName, String subject, String property, String object )
    {
        List<Statement> results = new ArrayList<Statement>();

        Model model = null;

        dataset.begin( ReadWrite.READ );
        try
        {
            model = dataset.getNamedModel( modelName );

            Selector selector = new SimpleSelector(
                    ( subject != null ) ? model.createResource( subject ) : (Resource) null,
                    ( property != null ) ? model.createProperty( property ) : (Property) null,
                    ( object != null ) ? model.createResource( object ) : (RDFNode) null
            );

            StmtIterator it = model.listStatements( selector );
            {
                while( it.hasNext() )
                {
                    Statement stmt = it.next();
                    results.add( stmt );
                }
            }

            dataset.commit();
        }
        finally
        {
            //if( model != null ) model.close();
            dataset.end();
        }

        return results;
    }

    public Dataset getDataset()
    {
        return dataset;
    }
}
