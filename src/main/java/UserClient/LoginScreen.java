package UserClient;

import GlobalClasses.SearchableList;
import GlobalClasses.Subclass;
import Utilities.ConstValues;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static Utilities.ConstValues.CreativeWork;
import static Utilities.ConstValues.schemaURI;

public class LoginScreen {

    private VBox layout;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Stage window;
    private String email;
    private Scene lastScene;
    private List<Scene> historyScene;
    private Scene initScene;

    public LoginScreen(Stage window, Scene initScene, ObjectOutputStream out, ObjectInputStream in)
    {
        this.in = in;
        this.out = out;
        this.initScene = initScene;
        this.window = window;
        historyScene = new ArrayList<>();
        layout = new VBox(20);
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setPadding(new Insets(50,0,0,0));
        Label title = new Label("Login");
        title.setFont(Font.font("Verdana", FontWeight.BOLD,20));
        Button loginButton = new Button("Login");
        Button backButton = new Button("Back");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Attention!");
        alert.setHeaderText(null);
        alert.setContentText("Wrong username or password!");

        backButton.setMaxWidth(300);
        loginButton.setMaxWidth(300);


        Label emailLabel = new Label("Email");
        emailLabel.setMaxWidth(300);
        TextField emailTextField = new TextField();
        emailTextField.setMaxWidth(300);

        Label passwordLabel = new Label("Password");
        passwordLabel.setMaxWidth(300);
        PasswordField passwordTextField = new PasswordField();
        passwordTextField.setMaxWidth(300);


        loginButton.setOnAction(event -> {
            try {
                out.writeObject("Login");
                email = emailTextField.getText();
                out.writeObject(emailTextField.getText());
                out.writeObject(passwordTextField.getText());
                String code = (String)in.readObject();

                if(code.equals("OK"))
                {
                    createSubclassVBo(schemaURI + "CreativeWork");
                }
                else
                {
                    //Failed Login

                    alert.showAndWait();

                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        });

        backButton.setOnAction(event -> {
            window.setScene(initScene);
            window.centerOnScreen();
        });

        layout.getChildren().addAll(title, emailLabel,emailTextField,passwordLabel,passwordTextField,loginButton,backButton);

    }



    public VBox getLayout() {
        return layout;
    }

    private void createSubclassVBo(String nextClass)
    {
        try {
            out.writeObject("GetSubclasses");
            out.writeObject( nextClass);
            List<Subclass> subclasses = (List<Subclass>) in.readObject();
            if(subclasses.size() > 0)
            {
                VBox vbox = new VBox(10);
                Label title = new Label("Welcome!");
                title.setFont(Font.font("Verdana", FontWeight.BOLD,20));
                vbox.getChildren().add(title);
                final Separator separator = new Separator();
                Button backButton = new Button("Back");
                backButton.setPrefSize(200,50);
                backButton.setOnAction(event -> {
                    if(nextClass.equals(CreativeWork))
                    {
                        window.setScene(initScene);
                    }

                    else
                    {
                        historyScene.remove(historyScene.size() -1);
                        window.setScene(historyScene.get(historyScene.size()));
                    }
                });

                Button vCardButton = new Button("My Vcard");
                vCardButton.setOnAction(event -> {
                    createVCardScreen(historyScene.get(historyScene.size()-1));
                });
                vCardButton.setPrefSize(200,50);
                for(Subclass s : subclasses) {
                    Button b = new Button(s.getName().substring(1, s.getName().length()-1));
                    b.setPrefSize(200,50);
                    b.setUserData(s.getURI());
                    b.setOnAction(event -> {
                        createSubclassVBo(b.getUserData().toString());
                    });
                    vbox.getChildren().add(b);
                }

                if(nextClass.equals(CreativeWork))
                {
                    vbox.getChildren().add(separator);
                    vbox.getChildren().add(vCardButton);
                }
                vbox.getChildren().add(backButton);

                vbox.setAlignment(Pos.CENTER);
                lastScene = new Scene(vbox, 400 ,400);
                historyScene.add(lastScene);
                window.setScene(lastScene);
                window.centerOnScreen();
            }
            else{
                //LOGIN
                out.writeObject("RebuildAsset");
                out.writeObject(nextClass);
                SearchableList sL = (SearchableList)in.readObject();
                AssetListScreen assetScreen = new AssetListScreen(sL,window,lastScene,email,out,in);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createVCardScreen(Scene myScene)
    {
        try {
            out.writeObject("MyVcard");
            out.writeObject(email);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SearchableList sL = null;

        try {
            sL = (SearchableList)in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        VcardScreen vcardScreen = new VcardScreen(sL.getAsset(0).getProperties(),window,myScene);
        Scene assetScene = new Scene(vcardScreen.getRootLayout(),500,600);
        window.setScene(assetScene);
        window.centerOnScreen();
    }
}
