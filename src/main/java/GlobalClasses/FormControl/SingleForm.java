package GlobalClasses.FormControl;

import GlobalClasses.Attribute;
import GlobalClasses.FormResults;
import GlobalClasses.Policy;
import GlobalClasses.TDBDataset;
import Utilities.ConstValues;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.apache.jena.base.Sys;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Statement;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.zip.CheckedOutputStream;

public class SingleForm implements FormControl {

    private VBox myVBox;
    private String name;
    private String URI;
    private Node textField;
    private Label nameLabel;
    private HBox myHBox;
    private boolean hasNested;
    private String type;
    private List<FormControl> mandatoryFields;
    private List<Attribute> attributes;
    private float padding;
    private List<FormControl> nestedForms;
    private String parent;
    private String ID;
    private List<Policy> policies;
    private List<CheckBox> allCheckboxes;



    public SingleForm(String name, String URI, boolean isMandatory, String type, boolean hasNested, String parent, List<Attribute> allAttributes, List<FormControl> mandatoryFields, float padding, List<Policy> optionalArgument)
    {
        this.name = name;
        name = name.substring(1, name.length() -1);
        this.URI = URI;
        this.type= type;
        this.mandatoryFields = mandatoryFields;
        this.attributes = allAttributes;
        this.padding = padding;
        this.hasNested = hasNested;
        this.parent = parent;
        myVBox = new VBox(10);
        myHBox =  new HBox(20);
        nestedForms = new ArrayList<>();
        allCheckboxes = new ArrayList<>();
        policies = optionalArgument;

        if(hasNested)
            nameLabel = new Label(name + ":");
        else if(isMandatory)
            nameLabel = new Label(name + " (*)");
        else
            nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Verdana", FontWeight.BOLD,14));
        if(type.equals(ConstValues.schemaURI + "Text"))
            textField = new TextField();
        else if(type.equals(ConstValues.passwordRange))
            textField =  new PasswordField();
        else if(type.equals(ConstValues.nonNegativeInteger)) {
            textField = createSpinner();
        }
        else if(type.equals(ConstValues.date))
        {
            textField = new DatePicker();
            ((DatePicker)textField).setValue(LocalDate.now());
            ((DatePicker)textField).setEditable(false);
        }
        else if(type.equals(ConstValues.policy))
        {
            List<String> allNames = new ArrayList<>();
            VBox checkVBox = new VBox(3);
            for(Policy a : optionalArgument)
            {
                CheckBox cb = new CheckBox(a.getName());
                allCheckboxes.add(cb);
                checkVBox.getChildren().add(cb);
            }
            System.out.println(allNames);

            textField = checkVBox;
        }
        else
        {

            VBox newVBox = new VBox(10);
            ID = UUID.randomUUID().toString();
            for(Attribute a : allAttributes)
                {
                    if(a.getParent() != null && a.getParent().equals(URI))
                    {
                        FormControl form = createNestedForm(a,ID);
                        nestedForms.add(form);
                        newVBox.getChildren().add(form.getVBox());
                    }
                }
            System.out.println(padding);
            newVBox.setPadding(new Insets(0,0,0,padding));
            nameLabel.setPadding(new Insets(0,0,0,padding));
            myVBox.getChildren().addAll(nameLabel,newVBox);
            return;
        }

        myHBox.getChildren().addAll(textField);

        myVBox.getChildren().addAll(nameLabel,myHBox);
        myVBox.setPadding(new Insets(0,0,0,padding));

    }

    class MyConverter extends StringConverter<Integer> {

        @Override
        public String toString(Integer object) {
            return object + "";
        }

        @Override
        public Integer fromString(String string) {
            return Integer.parseInt(string);
        }

    }

    @Override
    public VBox getVBox() {
        return myVBox;
    }

    //recursive function that gets all the results from the nested forms
    @Override
    public void putResults(FormResults formResults) {
        if(hasNested)
        {
            for(FormControl f: nestedForms)
            {
                List<String> values = new ArrayList<>();
                values.add(ID);
                formResults.addResult(URI,values,parent);
                f.putResults(formResults);
            }
        }
        else {
            List<String> values = new ArrayList<>();
            values.addAll(getText(textField));
            if(values.size() > 0 && !values.get(0).equals("")) {
                if(parent != null)
                    formResults.addResult(URI, values,parent);
                else
                    formResults.addResult(URI,values,null);
            }
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
        if(getText(textField) == null || getText(textField).size() == 0 || getText(textField).get(0).trim().isEmpty())
        {
            return false;
        }
        else return true;
    }

    public void Alert()
    {
        textField.getStyleClass().add("error");
    }

    public void removeAlert()
    {
        textField.getStyleClass().remove("error");
    }

    private List<String> getText(Node node)
    {
        List<String> values = new ArrayList<>();

        if(type.equals(ConstValues.schemaURI + "Text"))
        {
            values.add(((TextField)node).getText());
        }
        else if(type.equals(ConstValues.passwordRange))
        {
            values.add(((PasswordField)node).getText());
        }
        else if(type.equals( ConstValues.nonNegativeInteger))
        {
            values.add(((TextField)textField).getText());
        }
        else if(type.equals(ConstValues.date))
        {
            values.add(((DatePicker)textField).getValue().toString());
        }
        else if(type.equals(ConstValues.policy))
        {
            for(int i = 0; i < allCheckboxes.size(); i++)
            {
                if(allCheckboxes.get(i).isSelected())
                    values.add(policies.get(i).getURI());
            }
        }

        return values;
    }

    private TextField createSpinner()
    {
        DecimalFormat format = new DecimalFormat( "#" );

        TextField field = new TextField();
        field.setTextFormatter( new TextFormatter<>(c ->
        {
            if ( c.getControlNewText().isEmpty() )
            {
                return c;
            }

            ParsePosition parsePosition = new ParsePosition( 0 );
            Object object = format.parse( c.getControlNewText(), parsePosition );

            if ( object == null || parsePosition.getIndex() < c.getControlNewText().length() )
            {
                return null;
            }
            else
            {
                return c;
            }
        }));



        return field;
    }

    private FormControl createNestedForm(Attribute attribute, String ID)
    {
        FormControl newForm;


        if(attribute.getMandatory() && !attribute.getIsMultiple())
        {
            newForm = new SingleForm(attribute.getName(), attribute.getURI(), true, attribute.getType(),attribute.HasNesting(),ID,attributes,mandatoryFields, padding + 30, null);
            if(!attribute.HasNesting())
                mandatoryFields.add(newForm);
        }
        else if(attribute.getMandatory() && attribute.getIsMultiple())
        {
            newForm = new MultipleForm(attribute.getName(), attribute.getURI(), true, padding + 30,ID);
            if(!attribute.HasNesting())
                mandatoryFields.add(newForm);
        }

        else if(!attribute.getMandatory() && !attribute.getIsMultiple())
        {
            newForm = new SingleForm(attribute.getName(), attribute.getURI(), false,attribute.getType(),attribute.HasNesting(),ID,attributes,mandatoryFields, padding + 30, null);
        }
        else
        {
            newForm = new MultipleForm(attribute.getName(), attribute.getURI(), false, padding + 30, ID);

        }

        return newForm;
    }
}
