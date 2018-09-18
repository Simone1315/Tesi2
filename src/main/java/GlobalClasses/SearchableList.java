package GlobalClasses;

import Utilities.ConstValues;
import org.apache.jena.rdf.model.Statement;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class SearchableList implements Serializable {

    transient TDBDataset TDBDataset;
    transient TDBDataset assetOnthology;
    List<ListElement> allAssets;
    List<String> allNames;

    //subject is the subclass of asset we want to retrieve. If null, all assets will be retrieved
    public SearchableList(String subject, TDBDataset dataset, TDBDataset assetOnthology,TDBDataset policyDataset, String namedModel, String onthologyNamedMoodel, String caller)
    {
        this.TDBDataset = dataset;
        this.assetOnthology  = assetOnthology;

        List<Statement> choiceStatements = assetOnthology.getStatements(onthologyNamedMoodel, null, ConstValues.choice, null);
        List<String> choiceURI = new ArrayList<>();

        for(Statement s : choiceStatements)
        {
            if(s.getObject().toString().equals(caller))
                choiceURI.add(s.getSubject().toString());
        }

        allAssets = new ArrayList<>();
        List<String> subclasses = getAllSubclasses(onthologyNamedMoodel);
        List<String> allIDs = getAllIDs(namedModel,subclasses, subject);
        List<String> nestedProperties = getNestedProperties(onthologyNamedMoodel);
        List<Statement> statements = TDBDataset.getStatements(namedModel,null,null,null);

        for(String id: allIDs)
            allAssets.add(new ListElement(id));
        for(Statement s : statements) {
            System.out.println(s);
            String ID = s.getSubject().toString();
            if (allIDs.contains(ID)) {
                ListElement a = findAssetWithID(ID);
                String propertyURI = s.getPredicate().toString();
                boolean choice = false;
                if(choiceURI.contains(propertyURI))
                    choice = true;
                if(!nestedProperties.contains(propertyURI)) {
                    if (!propertyURI.equals(ConstValues.rdfType)) {
                        String propertyName = assetOnthology.getStatements(onthologyNamedMoodel, propertyURI, ConstValues.hasHumanReadableName, null).get(0).getObject().toString();
                        if(propertyURI.equals(ConstValues.hasPolicy))
                        {
                            List<Statement> policyStatement = policyDataset.getStatements("createdPoliciesModel",s.getObject().toString(), ConstValues.description,null);
                            List<Statement> policyStatementLabel = policyDataset.getStatements("createdPoliciesModel",s.getObject().toString(), ConstValues.hasHumanReadableName,null);

                            if(a.findProperty(propertyName) != null) {
                                a.findProperty(propertyName).addValueToProperty(policyStatement.get(0).getObject().toString(),s.getObject().toString(),policyStatementLabel.get(0).getObject().toString());
                            }
                            else
                            {
                                ListProperty policyProperty = new ListProperty(propertyName,policyStatement.get(0).getObject().toString(),choice,s.getObject().toString(),policyStatementLabel.get(0).getObject().toString());
                                a.addProperty(policyProperty);
                            }
                        }
                        else {
                            if (propertyURI.equals(ConstValues.nameResource))
                                a.setName(s.getObject().toString());

                            if (a.findProperty(propertyName) != null) {
                                a.findProperty(propertyName).addValueToProperty(s.getObject().toString());
                            } else {
                                ListProperty newProperty = new ListProperty(propertyName, s.getObject().toString(),choice);
                                a.addProperty(newProperty);
                            }
                        }
                    }
                }
                else
                {
                    List<ListProperty> assetProperties = manageNestedProperty(a, nestedProperties, propertyURI,s.getObject().toString(),namedModel,onthologyNamedMoodel);
                    String propertyName = assetOnthology.getStatements(onthologyNamedMoodel, propertyURI, ConstValues.hasHumanReadableName, null).get(0).getObject().toString();
                    ListProperty baseProperty = new ListProperty(propertyName,assetProperties);
                    a.addProperty(baseProperty);
                }
            }

        }

        java.util.Collections.sort(allAssets);
        allNames = new ArrayList<>();


        for(ListElement a: allAssets) {
            allNames.add(a.getName());
        }




    }

    public SearchableList(TDBDataset dataset, TDBDataset assetOnthology, String namedModel,String onthologyNamedMoodel, String userID)
    {
        this.TDBDataset = dataset;
        this.assetOnthology  = assetOnthology;


        allAssets = new ArrayList<>();
        List<String> allIDs = new ArrayList<>();
        allIDs.add(userID);
        List<String> nestedProperties = getNestedProperties(onthologyNamedMoodel);
        List<Statement> statements = TDBDataset.getStatements(namedModel,userID,null,null);
        for(String id: allIDs)
            allAssets.add(new ListElement(id));
        for(Statement s : statements) {
            System.out.println(s);
            String ID = s.getSubject().toString();
            if (allIDs.contains(ID)) {
                ListElement a = findAssetWithID(ID);
                String propertyURI = s.getPredicate().toString();
                if(!nestedProperties.contains(propertyURI)) {

                     if (!propertyURI.equals(ConstValues.rdfType) && !propertyURI.equals(ConstValues.hasPassword)) {
                        String propertyName = assetOnthology.getStatements(onthologyNamedMoodel, propertyURI, ConstValues.hasHumanReadableName, null).get(0).getObject().toString();


                        if (propertyURI.equals(ConstValues.nameResource))
                            a.setName(s.getObject().toString());

                        if (a.findProperty(propertyName) != null) {
                            a.findProperty(propertyName).addValueToProperty(s.getObject().toString());
                        } else {
                            ListProperty newProperty = new ListProperty(propertyName, s.getObject().toString(), false);
                            a.addProperty(newProperty);
                        }
                    }
                }
                else
                {
                    List<ListProperty> assetProperties = manageNestedProperty(a, nestedProperties, propertyURI,s.getObject().toString(),namedModel,onthologyNamedMoodel);
                    String propertyName = assetOnthology.getStatements(ConstValues.userNameModel, propertyURI, ConstValues.hasHumanReadableName, null).get(0).getObject().toString();
                    ListProperty baseProperty = new ListProperty(propertyName,assetProperties);
                    a.addProperty(baseProperty);
                }
            }

        }

        allNames = new ArrayList<>();

        for(ListElement a: allAssets)
            allNames.add(a.getName());


    }

    private ListElement findAssetWithID(String ID)
    {
        for(ListElement a : allAssets)
        {
            if(a.getId().equals(ID))
                return a;
        }
        return null;
    }

    public List<String> getAllNames() {
        return allNames;
    }

    public ListElement getAsset(int index)
    {
        if(allAssets.size() > 0)
            return allAssets.get(index);
        else return null;
    }

    private List<String> getAllSubclasses(String namedModel)
    {
        List<String> subclasses = new ArrayList<>();
        List<Statement> statements = assetOnthology.getStatements(namedModel,null,ConstValues.subclassOf,null);
        for(Statement s: statements)
        {
            subclasses.add(s.getSubject().toString());
        }

        return subclasses;
    }

    private List<String> getAllIDs(String namedModel, List<String> subclasses, String subject)
    {
        List<String> allID = new ArrayList<>();
        if(subject == null) {
            for (String s : subclasses) {
                List<Statement> statements = TDBDataset.getStatements(namedModel, null, ConstValues.rdfType, s);
                for (Statement st : statements) {
                    allID.add(st.getSubject().toString());
                }
            }
        }
        else
        {
            List<Statement> statements = TDBDataset.getStatements(namedModel, null, ConstValues.rdfType, subject);
            for (Statement st : statements) {
                allID.add(st.getSubject().toString());
            }
        }

        return allID;
    }

    private List<String> getNestedProperties(String onthologyNameModel )
    {
        List<String> properties1 = new ArrayList<>();
        List<Statement> statements1 = assetOnthology.getStatements(onthologyNameModel,null,ConstValues.range,null);
        List<String> basicTypes = ConstValues.basicTypes;

        for(Statement s : statements1)
        {
            if(!basicTypes.contains(s.getObject().toString()))
            {
                properties1.add(s.getSubject().toString());
            }
        }

        return properties1;
    }

    private List<ListProperty> manageNestedProperty(ListElement asset, List<String> nestedProperties, String propertyURI, String objectValue, String namedModel, String onthologyNamedModel)
    {
        if (!propertyURI.equals(ConstValues.rdfType)) {
            List<Statement> nestedStatements = TDBDataset.getStatements(namedModel, objectValue, null, null);
            List<ListProperty> properties = new ArrayList<>();

            for(Statement s: nestedStatements)
            {
                String propertyName = assetOnthology.getStatements(onthologyNamedModel, s.getPredicate().toString(), ConstValues.hasHumanReadableName, null).get(0).getObject().toString();
                if(nestedProperties.contains(s.getPredicate().toString()))
                {
                    ListProperty nestedProperty = new ListProperty(propertyName,manageNestedProperty(asset,nestedProperties,s.getPredicate().toString(), s.getObject().toString(),namedModel, onthologyNamedModel));
                    properties.add(nestedProperty);
                }
                else
                {
                    if(findPropertyWithName(properties,propertyName) != null)
                    {
                        findPropertyWithName(properties,propertyName).addValueToProperty(s.getObject().toString());
                    }
                    else {
                        ListProperty property = new ListProperty(propertyName, s.getObject().toString(), false);
                        properties.add(property);
                    }
                }
            }
            return properties;
        }
        return null;
    }

    private ListProperty findPropertyWithName(List<ListProperty> properties, String name)
    {
        for(ListProperty p : properties)
        {
            if(p.getPropertyName().equals(name.substring(1,name.length()-1)))
            {
                return p;
            }
            if(p.getNestedProperty() != null)
                return findPropertyWithName(p.getNestedProperty(), name);
        }
        return null;
    }


    public ListElement findAssetWithName(String name)
    {
        for(ListElement a : allAssets)
        {
            if(a.getId().equals(name))
                return a;
        }
        return null;
    }
}
