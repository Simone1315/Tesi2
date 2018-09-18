package Utilities;

import org.apache.jena.vocabulary.XSD;

import java.util.ArrayList;
import java.util.List;

public class ConstValues {

    public static final  String assetNameModel = "ListElement";
    public static final  String userNameModel = "Users";

    public static final String schemaURI = "http://schema.org/";
    public static final String RRDFSURI = "http://example.org/";
    public static final String rdfURI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public static final String rdfsURI = "http://www.w3.org/2000/01/rdf-schema#";
    public static final String vCardURI = "http://www.w3.org/2006/vcard/ns#";
    public static final String XSDURI = "http://www.w3.org/2001/XMLSchema#";

    public static final String CreativeWork = schemaURI + "CreativeWork";
    public static final String nameResource = schemaURI + "name";



    public static final String rdfType = rdfURI + "type";
    public static final String subclassOf = rdfsURI + "subClassOf";
    public static final String range = rdfsURI + "range";

    public static final String isTotal = RRDFSURI + "isTotal";
    public static final String mandatoryFor = RRDFSURI + "mandatoryFor";

    public static final String functionalProperty = RRDFSURI + "functionalProperty";


    public static final String assetManager = RRDFSURI + "assetManager";
    public static final String userManager = RRDFSURI + "userManager";
    public static final String hasHumanReadableName =  rdfsURI+ "label";
    public static final String description = schemaURI + "description";

    //vCard constant values

    public static final String vCardName = vCardURI + "Kind";
    public static final String vCardhasEmail = vCardURI +"hasEmail";
    public static final String vCardFormattedName = vCardURI + "fn";
    public static final String vCardNickName = vCardURI + "nickname";
    public static final String vCardIndividual = vCardURI + "Individual";
    public static final String vCardOrganization = vCardURI + "Organization";
    public static final String vCardOrganizationName = vCardURI + "hasOrganizationName";
    public static final String hasPassword = RRDFSURI + "hasPassword";



    public static final String text = schemaURI + "Text";
    public static final String nonNegativeInteger = XSDURI + "nonNegativeInteger";
    public static final String passwordRange = schemaURI + "accessCode";
    public static final String date = schemaURI + "Date";
    public static final String policy = RRDFSURI + "Policy";
    public static final String hasPolicy = RRDFSURI + "hasPolicy";
    public static final String choice = RRDFSURI +"choice";
    public static final String alreadyDefined = RRDFSURI + "alreadyDefined";

    public static final String navigationManager = RRDFSURI +"navigationManager";


    public static final List<String> basicTypes = new ArrayList<String>();
    static {
        basicTypes.add(passwordRange);
        basicTypes.add(nonNegativeInteger);
        basicTypes.add(text);
        basicTypes.add(date);
        basicTypes.add(policy );
    }


}
