package Utilities;

import GlobalClasses.Attribute;
import GlobalClasses.FormControl.AlreadyFilledForm;
import GlobalClasses.FormControl.FormControl;
import GlobalClasses.FormControl.MultipleForm;
import GlobalClasses.FormControl.SingleForm;
import GlobalClasses.FormResults;
import GlobalClasses.Policy;
import GlobalClasses.Subclass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class FormCreator {

    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Stage window;
    List<FormControl> allForms;
    List<FormResults> finalAsset;
    List<String> allURI;
    String lastURI;
    private Scene initScene;
    private String initURI;
    private List<Policy> optionalPolicies;



    public FormCreator(ObjectOutputStream out, ObjectInputStream in, Stage window, Scene initScene, String initURI, List<Policy> optionalPolicies)
    {
        this.in = in;
        this.out = out;
        this.window = window;
        finalAsset = new ArrayList<>();
        this.initScene = initScene;
        allURI = new ArrayList<>();
        this.initURI = initURI;
        allURI.add(initURI);
        this.optionalPolicies = optionalPolicies;

    }

    public FormCreator(ObjectOutputStream out, ObjectInputStream in, Stage window, Scene initScene, String initURI)
    {
        this.in = in;
        this.out = out;
        this.window = window;
        finalAsset = new ArrayList<>();
        this.initScene = initScene;
        allURI = new ArrayList<>();
        this.initURI = initURI;
        allURI.add(initURI);
    }
    //this method contacts the server and asks for the subclasses and the attributes of the chosen class
    public void askServerForNewInterface(String subclassName)
    {
        List<Attribute> attributes = new ArrayList<>();
        List<Subclass> subclasses = new ArrayList<>();
        System.out.println(subclassName);
        try {
            out.writeObject(subclassName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //first read attributes
        while (true) {
            Attribute attr = null;
            try {
                attr = (Attribute)in.readObject();
                if(attr != null) {
                    attributes.add(attr);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            if (attr == null) {
                break;
            }
        }

        //then read subclasses
        while (true) {
            Subclass subcl = null;
            try {
                subcl = (Subclass) in.readObject();
                if(subcl != null)
                    subclasses.add(subcl);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            if (subcl == null) {
                break;
            }
        }

        //finally create new scene
        Scene addAssetScene = new Scene(createNewInterface(subclasses,attributes),450,700);
        String fontSheet = fileToStylesheetString( new File("C:\\Users\\simone\\IdeaProjects\\Tesi2\\src\\main\\java\\GlobalClasses\\error.css") );
        addAssetScene.getStylesheets().add( fontSheet );
        window.setScene(addAssetScene);
        window.centerOnScreen();
    }

    //This method uses the attributes and the subclasses to create the interface with the client
    private ScrollPane createNewInterface(List<Subclass> subclasses, List<Attribute> attributes)
    {
        Text title = new Text("Creation");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 20));
        ArrayList<FormControl> mandatoryFields = new ArrayList<>();
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().add(title);
        allForms = new ArrayList<>();
        final ToggleGroup group = new ToggleGroup();
        Control[] controls = new Control[attributes.size()];
        //First create the attributes controls
        for(int i = 0 ; i < attributes.size(); i++)
        {
            if(attributes.get(i).getParent() == null) {
                allForms.add(createControl(attributes.get(i), mandatoryFields, attributes));
            }
        }

        for(FormControl f : allForms)
        {
            if(f.getVBox() != null) {
                layout.getChildren().add(f.getVBox());
                f.getVBox().setMaxWidth(300);
            }

        }

        //Then create the subclasses radio buttons
        if(subclasses != null && subclasses.size() > 0) {
            Text subclassSelection = new Text("Select type:");
            subclassSelection.setWrappingWidth(300);
            subclassSelection.setFont(Font.font("Verdana",FontWeight.BOLD,14));
            layout.getChildren().add(subclassSelection);
            RadioButton[] radioButtons = new RadioButton[subclasses.size()];
            for (int i = 0; i < subclasses.size(); i++) {
                radioButtons[i] = new RadioButton(subclasses.get(i).getName().substring(1,subclasses.get(i).getName().length() -1));
                radioButtons[i].setToggleGroup(group);
                radioButtons[i].setUserData(subclasses.get(i).getURI());
                radioButtons[i].setMaxWidth(300);
            }
            radioButtons[0].setSelected(true);
            layout.getChildren().addAll(radioButtons);
        }

        Button submitButton = new Button();
        submitButton.setText("Submit");
        submitButton.setMaxWidth(300);
        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                for (int i = 0; i < mandatoryFields.size(); i++) {
                    mandatoryFields.get(i).removeAlert();
                }
                boolean validForm = true;
                for (int i = 0; i < mandatoryFields.size(); i++) {
                    if (!mandatoryFields.get(i).filledIn()) {
                        validForm = false;
                        mandatoryFields.get(i).Alert();
                    }
                    else
                        mandatoryFields.get(i).removeAlert();
                }
                if (validForm) {
                    FormResults formResults = new FormResults();

                    for (FormControl f : allForms) {
                        String URI = f.getURI();
                        f.putResults(formResults);

                    }
                    finalAsset.add(formResults);
                    formResults.printResult();

                    if(subclasses.size() != 0) {
                        String selectedToggle;
                        selectedToggle = ((RadioButton) group.getSelectedToggle()).getUserData().toString();
                        allURI.add(selectedToggle);
                        lastURI = ((RadioButton) group.getSelectedToggle()).getText();
                        askServerForNewInterface(selectedToggle);
                    }
                    else
                    {
                        //Send results to TDB and creation window
                        String end = ".";
                        try {
                            out.writeObject(end);
                            out.reset();
                            out.writeObject("WriteResults");
                            out.writeObject(lastURI);
                            out.writeObject(allURI.get(allURI.size()-1));
                            out.writeObject(finalAsset);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        createConfirmationWindow();
                    }

                }
                else
                {

                }
            }


        });

        Button backButton = new Button();
        backButton.setText("Back");
        backButton.setMaxWidth(300);
        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(finalAsset.size() <= 0)
                {
                    String end = ".";
                    try {
                        out.writeObject(end);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    window.setScene(initScene);
                    window.centerOnScreen();
                }
                else
                {
                    finalAsset.remove(finalAsset.size() - 1);
                    allURI.remove(allURI.size() - 1);
                    askServerForNewInterface(allURI.get(allURI.size() - 1));
                }

            }
        });
        layout.getChildren().addAll(submitButton,backButton);
        ScrollPane sideBarScroller = new ScrollPane(layout);
        sideBarScroller.setFitToWidth(true);
        sideBarScroller.setPadding(new Insets(20,20,20,20));
        return sideBarScroller;
    }

    //this method receives an attribute and creates the appropriate UI control
    private FormControl createControl(Attribute attribute, List<FormControl> mandatoryFields, List<Attribute> attributes)
    {
        FormControl newForm;

        if(attribute.getParent() == null) {

            if (attribute.isAlreadyDefined()) {
                    newForm = new AlreadyFilledForm(attribute.getURI(),attribute.getType());
            } else {
                List<Policy> additionalParameters = null;
                if (optionalPolicies != null && attribute.getURI().equals(ConstValues.hasPolicy)) {
                    additionalParameters = optionalPolicies;
                }

                if (attribute.getMandatory() && !attribute.getIsMultiple()) {
                    newForm = new SingleForm(attribute.getName(), attribute.getURI(), true, attribute.getType(), attribute.HasNesting(), null, attributes, mandatoryFields, 0, additionalParameters);
                    if (!attribute.HasNesting())
                        mandatoryFields.add(newForm);
                } else if (attribute.getMandatory() && attribute.getIsMultiple()) {
                    newForm = new MultipleForm(attribute.getName(), attribute.getURI(), true, 0, null);
                    if (!attribute.HasNesting())
                        mandatoryFields.add(newForm);
                } else if (!attribute.getMandatory() && !attribute.getIsMultiple()) {
                    newForm = new SingleForm(attribute.getName(), attribute.getURI(), false, attribute.getType(), attribute.HasNesting(), null, attributes, mandatoryFields, 0, additionalParameters);
                } else {
                    newForm = new MultipleForm(attribute.getName(), attribute.getURI(), false, 0, null);

                }

            }
            return newForm;
        }
        else return null;

    }



    public String fileToStylesheetString ( File stylesheetFile ) {
        try {
            return stylesheetFile.toURI().toURL().toString();
        } catch ( MalformedURLException e ) {
            return null;
        }
    }

    //this method creates the initial layout, the one containing the main menu
    private void createConfirmationWindow()
    {
        VBox layout1 = new VBox(20);
        Button backToMenuButton = new Button();
        Text confirmationLabel = new Text();
        confirmationLabel.setText("Creation Completed!");
        confirmationLabel.setFont(Font.font("Verdana",FontWeight.BOLD, 20));
        backToMenuButton.setMaxWidth(200);
        backToMenuButton.setPrefHeight(70);
        backToMenuButton.setText("Back to main menu");
        backToMenuButton.setOnAction(event -> window.setScene(initScene));
        allURI.clear();
        allURI.add(initURI);
        finalAsset.clear();
        layout1.getChildren().addAll(confirmationLabel,backToMenuButton);
        layout1.setAlignment(Pos.CENTER);
        Scene currentScene = new Scene(layout1,300,300);

        window.setScene(currentScene);
        window.centerOnScreen();
    }
}
