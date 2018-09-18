package GlobalClasses.FormControl;

import GlobalClasses.FormResults;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

public class MultipleForm implements FormControl {

    private VBox myVBox;
    private String name;
    private String URI;
    private List<TextField> textField;
    private TextField firstTextField;
    private Label nameLabel;
    private HBox myHBox;
    private String parent;

    public MultipleForm(String name, String URI, boolean isMandatory, float padding, String parent)
    {
        this.name = name;
        this.parent = parent;
        name = name.substring(1, name.length() -1);
        this.URI = URI;
        myVBox = new VBox(5);
        myHBox =  new HBox(20);
        Button addButton = new Button();
        addButton.setText("+");
        if(isMandatory)
            nameLabel = new Label(name + " (*)");
        else
            nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Verdana", FontWeight.BOLD,14));
        textField = new ArrayList<>();
        firstTextField = new TextField();
        textField.add(firstTextField);
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                textField.get(textField.size()-1).getStyleClass().remove("error");
                if(!textField.get(textField.size()-1).getText().trim().isEmpty()) {
                    textField.get(textField.size() - 1).setDisable(true);
                    HBox newHbox = new HBox(20);
                    TextField newInput = new TextField();

                    textField.add(newInput);
                    myHBox.getChildren().remove(addButton);
                    newHbox.getChildren().addAll(newInput, addButton);
                    myVBox.getChildren().add(newHbox);
                }
                else
                {
                    textField.get(textField.size()-1).getStyleClass().add("error");
                }
            }
        });
        myHBox.getChildren().addAll(firstTextField,addButton);
        myVBox.getChildren().addAll(nameLabel,myHBox);
        myVBox.setPadding(new Insets(0,0,0,padding));
    }

    @Override
    public VBox getVBox() {
        return myVBox;
    }

    @Override
    public void putResults(FormResults formResults) {
        List<String> values = new ArrayList<>();
        for(TextField t : textField) {
            if(!t.getText().trim().isEmpty())
                values.add(t.getText());
        }
        if(values.size() > 0) {

            formResults.addResult(URI, values,parent);
        }
        }

    public String getName()
    {
        return this.name;
    }

    public String getURI()
    {
        return this.URI;
    }

    public boolean filledIn()
    {
        boolean filled = false;
        for(TextField t : textField)
        {
            if(!t.getText().trim().isEmpty())
                filled = true;
        }

        return filled;
    }

    public void Alert()
    {
        firstTextField.getStyleClass().add("error");
    }

    public void removeAlert()
    {
        firstTextField.getStyleClass().remove("error");

    }
}