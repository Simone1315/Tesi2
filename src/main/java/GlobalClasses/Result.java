package GlobalClasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Result implements Serializable {
    private String URI;
    private List<String> values;
    private String differentSubject;

    public Result(String newUri, List<String> values, String differentSubject)
    {
        URI = newUri;
        this.values = values;
        this.differentSubject = differentSubject;
    }

    public String getURI() {
        return URI;
    }

    public List<String> getValues() {
        return values;
    }

    public void printResults()
    {
        System.out.println(URI);

        for(String s : values)
        {
            System.out.println(s);
        }
    }

    public String getDifferentSubject() {
        return differentSubject;
    }
}
