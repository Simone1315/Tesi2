package GlobalClasses.FormControl;

import GlobalClasses.FormResults;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class AlreadyFilledForm implements FormControl{

    private String URI;
    private List<String> values;

    public AlreadyFilledForm(String URI, String range)
    {
        values = new ArrayList<>();
        this.URI = URI;
        fillValues(range);
    }

    private void fillValues(String range)
    {
        values.add("CIAOOOOOO");
    }

    @Override
    public VBox getVBox() {
        return null;
    }

    @Override
    public void putResults(FormResults formResults) {
            formResults.addResult(URI,values,null);
    }

    @Override
    public String getURI() {
        return URI;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean filledIn() {
        return true;
    }

    @Override
    public void Alert() {

    }

    @Override
    public void removeAlert() {

    }
}
