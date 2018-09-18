package GlobalClasses;

import java.io.Serializable;

public class Subclass implements Serializable {

    private String URI;
    private String name;

    public Subclass(String URI, String name)
    {
        this.URI = URI;
        this.name = name;
    }

    public String getURI(){return this.URI;}

    public String getName()
    {
        return this.name;
    }


}
