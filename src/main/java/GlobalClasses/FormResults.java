package GlobalClasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FormResults implements Serializable {
    List<Result> results;

    public FormResults()
    {
        results = new ArrayList<>();
    }

    public List<Result> getResults() {
        return results;
    }

    public void addResult(String newURI, List<String> values, String differentSubject)
    {
        results.add(new Result(newURI,values,differentSubject));
    }



    public void printResult()
    {
        for (Result r : results)
        {
            r.printResults();
        }
    }


}
