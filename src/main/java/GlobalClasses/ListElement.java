package GlobalClasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ListElement implements Serializable, Comparable {

    private String id;
    private String name;
    private List<ListProperty> properties;

    public ListElement(String id)
    {
        this.id = id;
        properties = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public List<ListProperty> getProperties() {
        return properties;
    }

    public void addProperty(ListProperty property)
    {
        properties.add(property);
    }

    public ListProperty findProperty(String name)
    {
        for(ListProperty p : properties)
        {
            if(p.getPropertyName().equals(name.substring(1,name.length()-1)))
                return p;
        }
        return null;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Object l) {
        return name.toLowerCase().compareTo(((ListElement)l).getName().toLowerCase());
    }
}
