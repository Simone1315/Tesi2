package UserServer;

import Utilities.ConstValues;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;

public class TDBUserLoader {

    private Dataset dataset;

    public TDBUserLoader(Dataset dataset)
    {
        this.dataset = dataset;
    }

    public void loadData()
    {
        addStatement( ConstValues.userNameModel, ConstValues.vCardhasEmail, ConstValues.isTotal,  ConstValues.vCardName );
        addStatement( ConstValues.userNameModel, ConstValues.vCardhasEmail, ConstValues.range,  ConstValues.text );
        addStatement( ConstValues.userNameModel, ConstValues.vCardhasEmail, ConstValues.rdfType,  ConstValues.functionalProperty );
        addStatement( ConstValues.userNameModel, ConstValues.vCardhasEmail, ConstValues.mandatoryFor, ConstValues.userManager );
        addStatement( ConstValues.userNameModel, ConstValues.vCardhasEmail, ConstValues.hasHumanReadableName, "E-mail");

        addStatement( ConstValues.userNameModel, ConstValues.vCardIndividual, ConstValues.subclassOf,  ConstValues.vCardName );
        addStatement( ConstValues.userNameModel, ConstValues.vCardOrganization, ConstValues.subclassOf, ConstValues.vCardName );
        addStatement( ConstValues.userNameModel, ConstValues.vCardIndividual, ConstValues.hasHumanReadableName, "Individual");
        addStatement( ConstValues.userNameModel, ConstValues.vCardOrganization, ConstValues.hasHumanReadableName, "Organization");

        addStatement( ConstValues.userNameModel, ConstValues.vCardFormattedName, ConstValues.isTotal, ConstValues.vCardIndividual);
        addStatement( ConstValues.userNameModel, ConstValues.vCardFormattedName, ConstValues.range, ConstValues.text);
        addStatement( ConstValues.userNameModel, ConstValues.vCardFormattedName, ConstValues.rdfType,  ConstValues.functionalProperty );
        addStatement( ConstValues.userNameModel, ConstValues.vCardFormattedName, ConstValues.mandatoryFor, ConstValues.userManager );
        addStatement( ConstValues.userNameModel, ConstValues.vCardFormattedName, ConstValues.hasHumanReadableName, "Full Name");


        addStatement( ConstValues.userNameModel, ConstValues.vCardNickName, ConstValues.isTotal, ConstValues.vCardIndividual);
        addStatement( ConstValues.userNameModel, ConstValues.vCardNickName, ConstValues.range, ConstValues.text);
        addStatement( ConstValues.userNameModel, ConstValues.vCardNickName, ConstValues.rdfType,  ConstValues.functionalProperty );
        addStatement( ConstValues.userNameModel, ConstValues.vCardNickName, ConstValues.mandatoryFor, ConstValues.userManager );
        addStatement( ConstValues.userNameModel, ConstValues.vCardNickName, ConstValues.hasHumanReadableName, "Nickname");


        addStatement( ConstValues.userNameModel, ConstValues.vCardOrganizationName, ConstValues.isTotal, ConstValues.vCardOrganization);
        addStatement( ConstValues.userNameModel, ConstValues.vCardOrganizationName, ConstValues.range, ConstValues.text);
        addStatement( ConstValues.userNameModel, ConstValues.vCardOrganizationName, ConstValues.rdfType,  ConstValues.functionalProperty );
        addStatement( ConstValues.userNameModel, ConstValues.vCardOrganizationName, ConstValues.mandatoryFor, ConstValues.userManager );
        addStatement( ConstValues.userNameModel, ConstValues.vCardOrganizationName, ConstValues.hasHumanReadableName, "Organization Name");

        addStatement( ConstValues.userNameModel, ConstValues.hasPassword, ConstValues.isTotal, ConstValues.vCardName);
        addStatement( ConstValues.userNameModel, ConstValues.hasPassword, ConstValues.range, ConstValues.passwordRange);
        addStatement( ConstValues.userNameModel, ConstValues.hasPassword, ConstValues.rdfType,  ConstValues.functionalProperty);
        addStatement( ConstValues.userNameModel, ConstValues.hasPassword, ConstValues.mandatoryFor, ConstValues.userManager );
        addStatement( ConstValues.userNameModel, ConstValues.hasPassword, ConstValues.hasHumanReadableName, "Password" );

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

