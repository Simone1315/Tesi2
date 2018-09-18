package AssetClient;

import GlobalClasses.*;
import GlobalClasses.FormControl.FormControl;
import GlobalClasses.FormControl.MultipleForm;
import GlobalClasses.FormControl.SingleForm;
import Utilities.ConstValues;
import Utilities.FormCreator;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.w3c.dom.Attr;

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AssetClient extends Application {

    Stage window;
    Scene initScene;
    private static final String schemaURI = "http://schema.org/";
    private static final String CreativeWork = schemaURI + "CreativeWork";

    private ObjectInputStream in = null;
    private ObjectOutputStream out;
    private List<Policy> policies;
    private FormCreator formCreator;
    String hostName = "127.0.0.1";


    public static void main(String[] args) {
        launch(args);
    }



    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        primaryStage.setTitle("Asset Manager window");
        primaryStage.setResizable(false);
        createInitLayout();

        Socket socket = null;

        try {
            socket = new Socket(hostName, 9898);
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Read the welcome message
        try {
            System.out.println((in.readObject()) + "\n");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            out.writeObject("GetPolicies");
            policies = (List<Policy>) in.readObject();
            System.out.println(policies);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        formCreator = new FormCreator(out,in,window,initScene,CreativeWork,policies);
        primaryStage.setScene(initScene);
        primaryStage.show();
    }


    //this method creates the initial layout, the one containing the main menu
    private void createInitLayout()
    {
        VBox layout1 = new VBox(20);
        Button addNewAssetButton = new Button();
        Button seeMyAssetsButton = new Button();
        addNewAssetButton.setText("Create new asset");
        addNewAssetButton.setOnAction(event ->
                {
                    try {
                        out.writeObject("InterfaceCreation");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    formCreator.askServerForNewInterface(CreativeWork);
                });
        addNewAssetButton.setMaxWidth(200);
        addNewAssetButton.setPrefHeight(70);
        seeMyAssetsButton.setText("All assets");
        seeMyAssetsButton.setMaxWidth(200);
        seeMyAssetsButton.setPrefHeight(70);
        seeMyAssetsButton.setOnAction(event -> {
            try {
                out.writeObject("GetAllAssets");
                SearchableList sL = (SearchableList)in.readObject();
                addListView(sL);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        layout1.getChildren().addAll(addNewAssetButton,seeMyAssetsButton);
        layout1.setAlignment(Pos.CENTER);
        initScene = new Scene(layout1,300,300);
        window.centerOnScreen();
    }





    private void addListView( SearchableList sL)
    {
        HBox rootLayout = new HBox(10);
        ListView<String> assetList = new ListView<String>();
        Button button = new Button("Back");
        button.setOnAction(event -> window.setScene(initScene)
        );
        System.out.println(sL.getAllNames());

        ObservableList<String> items = FXCollections.observableArrayList (
                sL.getAllNames());

        assetList.setItems(items);
        assetList.getSelectionModel().select(0);
        assetList.getFocusModel().focus(0);

        int selectedItem = assetList.getSelectionModel().getSelectedIndex();
        ListElement a = sL.getAsset(selectedItem);
        VBox vBox = createAssetVBox(a.getProperties());
        vBox.getChildren().add(button);

        assetList.setOnMouseClicked(event -> {
            int newSelectedItem = assetList.getSelectionModel().getSelectedIndex();
            ListElement newA = sL.getAsset(newSelectedItem);
            VBox newVBox = createAssetVBox(newA.getProperties());
            newVBox.getChildren().add(button);
            rootLayout.getChildren().remove(1);
            rootLayout.getChildren().add(newVBox);
        });
        rootLayout.getChildren().addAll(assetList,vBox);
        Scene assetScene = new Scene(rootLayout,800,500);
        window.setScene(assetScene);
    }

    private VBox createAssetVBox(List<ListProperty> properties)
    {
        List<Node> allTexts = new ArrayList<>();
        VBox rightVBox = new VBox(20);
        for(ListProperty p : properties)
        {

            String values = "";
            if(p != null && p.getValues() != null ) {
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

}
