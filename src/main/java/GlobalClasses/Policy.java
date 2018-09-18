package GlobalClasses;

import java.io.Serializable;

public class Policy implements Serializable {

    private String URI;
    private String name;

    public Policy(String URI, String name)
    {
        this.URI = URI;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getURI() {
        return URI;
    }
}
