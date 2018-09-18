package UserClient;

import GlobalClasses.ListProperty;
import GlobalClasses.ListElement;
import GlobalClasses.SearchableList;
import GlobalClasses.Subclass;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class AssetListScreen {

    private VBox rootLayout;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String email;
    private Stage window;
    private Scene myScene;
    private List<ToggleGroup> allGroups;
    private List<VBox> allToggles;
    private String currentID;


    public AssetListScreen(SearchableList sL, Stage window, Scene initScene, String email, ObjectOutputStream out, ObjectInputStream in)
    {
        rootLayout = new VBox(10);
        this.out = out;
        this.in = in;
        this.email = email;
        this.window = window;
        Button button = new Button("Back");
        button.setOnAction(event -> {
            window.setScene(initScene);
            window.centerOnScreen();
        }
        );


        HBox buttonHBox = new HBox(10);



        buttonHBox.getChildren().add(button);

        HBox infoHBox = new HBox(10);
        infoHBox = buildRootLayout(sL);

        rootLayout.getChildren().addAll(buttonHBox,infoHBox);

        myScene = new Scene(rootLayout,800,500);
        window.setScene(myScene);
        window.centerOnScreen();

    }

    private VBox createAssetVBox(List<ListProperty> properties)
    {
        List<Node> allTexts = new ArrayList<>();
        VBox rightVBox = new VBox(20);
        allGroups = new ArrayList<>();
        allToggles = new ArrayList<>();
        for(ListProperty p : properties)
        {

            String values = "";
            if(p.isChoice())
            {
                RadioButton[] radioButtons = new RadioButton[p.getValues().size()];
                ToggleGroup group = new ToggleGroup();
                HBox hbox = new HBox(5);
                for (int i = 0; i < p.getValues().size(); i++) {
                    radioButtons[i] = new RadioButton(p.getValues().get(i));
                    radioButtons[i].setToggleGroup(group);
                    radioButtons[i].setUserData(p.getOptionalURIs().get(i));
                    Tooltip tooltip = new Tooltip(p.getOptionalTooltips().get(i));
                    radioButtons[i].setTooltip(tooltip);
                }
                radioButtons[0].setSelected(true);
                Text newText = new Text(p.getPropertyName()+ " :" );
                hbox.getChildren().addAll(radioButtons);
                allGroups.add(group);
                VBox vbox = new VBox(3);
                vbox.getChildren().addAll(newText,hbox);
                allToggles.add(vbox);
            }
            else if(p != null && p.getValues() != null ) {
                for (int i = 0; i < p.getValues().size(); i++) {
                    if (i < p.getValues().size() - 1)
                        values += p.getValues().get(i) + ", ";
                    else
                        values += p.getValues().get(i);
                }
                Text newText = new Text(p.getPropertyName()+ ": "+ values);
                allTexts.add(newText);
            }
            else if(p.getValues() == null)
            {
                HBox newHBox = new HBox(5);
                newHBox.getChildren().add(new Text(p.getPropertyName()+ ": "));
                VBox finalVBox = buildNestedText(p);
                newHBox.getChildren().add(finalVBox);
                allTexts.add(newHBox);

            }
        }
        rightVBox.getChildren().addAll(allTexts);

        Button buyButton = new Button("Buy");
        buyButton.setOnAction(event -> {
            for(ToggleGroup t :allGroups)
            {
                System.out.println(t.getSelectedToggle().getUserData().toString() + "   " + currentID);
            }
        });

        for(VBox vbox: allToggles)
        {
            rightVBox.getChildren().add(vbox);
        }

        rightVBox.getChildren().add(buyButton);
        return rightVBox;
    }

    private VBox buildNestedText(ListProperty property)
    {
        VBox newVBox = new VBox(5);
        for(ListProperty p : property.getNestedProperty())
        {
            if(p.getValues() == null)
            {
                HBox newHBox = new HBox(5);
                newHBox.getChildren().add(new Text("• " + p.getPropertyName()+ ": "));
                newHBox.getChildren().add(buildNestedText(p));
                newVBox.getChildren().add(newHBox);
            }
            else
            {
                VBox finalVBox = new VBox(5);
                finalVBox.getChildren().add(new Text("• " + p.getPropertyName()+ ": " +p.getValuesString()));
                System.out.println("• " + p.getPropertyName()+ ": " +p.getValues().toString());
                newVBox.getChildren().add(finalVBox);
            }

        }
        return newVBox;
    }



    private HBox buildRootLayout(SearchableList sL)
    {
        HBox rootLayout1 = new HBox(10);
        ListView<String> assetList = new ListView<String>();

        System.out.println(sL.getAllNames());

        ObservableList<String> items = FXCollections.observableArrayList (
                sL.getAllNames());

        assetList.setItems(items);
        assetList.getSelectionModel().select(0);
        assetList.getFocusModel().focus(0);

        int selectedItem = assetList.getSelectionModel().getSelectedIndex();
        ListElement a = sL.getAsset(selectedItem);
        currentID = a.getId();
        VBox vBox = new VBox(10);
        if(a != null)
            vBox = createAssetVBox(a.getProperties());

        assetList.setOnMouseClicked(event -> {
            int newSelectedItem = assetList.getSelectionModel().getSelectedIndex();
            ListElement newA = sL.getAsset(newSelectedItem);
            currentID = newA.getId();
            VBox newVBox = createAssetVBox(newA.getProperties());
            rootLayout1.getChildren().remove(1);
            rootLayout1.getChildren().add(newVBox);
        });
        rootLayout1.getChildren().addAll(assetList,vBox);

        return rootLayout1;
    }


    public VBox getRootLayout()
    {
        return rootLayout;
    }
}
