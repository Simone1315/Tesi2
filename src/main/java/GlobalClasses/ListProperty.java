package GlobalClasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListProperty implements Serializable {
    private String name;
    private boolean choice;
    private List<String> values;
    private List<ListProperty> nestedProperty;
    private List<String> optionalURIs;
    private List<String> optionalTooltips;

    public ListProperty(String name, String value, boolean choice)
    {
        this.name = name;
        this.values = new ArrayList<>();
        values.add(value);
        this.choice = choice;
    }

    public ListProperty(String name, String value, boolean choice,String URI, String tooltip)
    {
        this.name = name;
        this.values = new ArrayList<>();
        values.add(value);
        this.choice = choice;
        optionalURIs = new ArrayList<>();
        optionalTooltips = new ArrayList<>();
        optionalURIs.add(URI);
        optionalTooltips.add(tooltip);
    }

    public ListProperty(String name, List<ListProperty> nestedProperty)
    {
        this.name = name;
        this.nestedProperty = nestedProperty;
    }

    public String getPropertyName()
    {
        return this.name.substring(1,name.length()-1);
    }

    public List<String> getValues() {

        if(nestedProperty == null)
            return values;
        return null;
    }

    public void addValueToProperty(String value)
    {
        values.add(value);
    }

    public void addValueToProperty(String value,String URI, String tooltip)
    {
        optionalURIs.add(URI);
        optionalTooltips.add(tooltip);
        values.add(value);
    }

    public List<ListProperty> getNestedProperty() {
        return nestedProperty;
    }

    public String getValuesString()
    {
        String string = "";
            for (int i = 0; i < values.size(); i++) {
                if (i < values.size() - 1)
                    string += values.get(i) + ", ";
                else
                    string += values.get(i);
            }
            return string;
    }

    public boolean isChoice() {
        return choice;
    }

    public List<String> getOptionalTooltips() {
        return optionalTooltips;
    }

    public List<String> getOptionalURIs() {
        return optionalURIs;
    }
}
