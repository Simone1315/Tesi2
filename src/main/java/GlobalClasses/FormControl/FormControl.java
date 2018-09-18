package GlobalClasses.FormControl;

import GlobalClasses.FormResults;
import javafx.scene.layout.VBox;

import java.util.List;

public interface FormControl {

    VBox getVBox();
    void putResults(FormResults formResults);
    String getURI();
    String getName();
    boolean filledIn();
    void Alert();
    void removeAlert();
}
