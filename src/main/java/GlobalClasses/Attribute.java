package GlobalClasses;

import java.io.Serializable;

public class Attribute implements Serializable {

    private String URI;
    private String name;
    private boolean isMandatory;
    private boolean isMultiple;
    private String type;
    private boolean hasNesting;
    private String parent;
    private boolean alreadyDefined;

    public Attribute(String URI, String name, boolean isMandatory, boolean isMultiple, String type, String parent, boolean hasNesting, boolean alreadyDefined)
    {
        this.URI = URI;
        this.name = name;
        this.isMandatory = isMandatory;
        this.isMultiple = isMultiple;
        this.type = type;
        this.parent = parent;
        this.hasNesting = hasNesting;
        this.alreadyDefined = alreadyDefined;
    }



    public String getURI(){return this.URI;}

    public String getName()
    {
        return this.name;
    }

    public boolean getMandatory()
    {
        return this.isMandatory;
    }

    public boolean getIsMultiple()
    {
        return this.isMultiple;
    }

    public String getType(){return this.type; }

    public boolean HasNesting() {
        return hasNesting;
    }

    public String getParent() {
        return parent;
    }

    public boolean isAlreadyDefined() {
        return alreadyDefined;
    }
}
