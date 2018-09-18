package UserClient;

import GlobalClasses.Attribute;
import GlobalClasses.FormControl.FormControl;
import GlobalClasses.FormControl.MultipleForm;
import GlobalClasses.FormControl.SingleForm;
import GlobalClasses.FormResults;
import GlobalClasses.Subclass;
import Utilities.ConstValues;
import Utilities.FormCreator;
import javafx.application.Application;
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

import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UserClient extends Application {

    Stage window;
    Scene initScene;
    private FormCreator formCreator;



    private ObjectInputStream in = null;
    private ObjectOutputStream out;
    String hostName = "127.0.0.1";


    public static void main(String[] args) {
        launch(args);
    }



    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        primaryStage.setTitle("Welcome User!");
        createInitLayout();


        Socket socket = null;
        try {
            socket = new Socket(hostName, 9899);
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

        formCreator = new FormCreator(out,in,window,initScene,ConstValues.vCardName);
        primaryStage.setScene(initScene);
        primaryStage.show();
    }


    //this method creates the initial layout, the one containing the main menu
    private void createInitLayout()
    {
        VBox layout1 = new VBox(20);
        Button createNewUserButton = new Button();
        Button logInButton = new Button();
        createNewUserButton.setMaxWidth(200);
        createNewUserButton.setPrefHeight(70);
        logInButton.setMaxWidth(200);
        logInButton.setPrefHeight(70);
        logInButton.setText("Login");
        logInButton.setOnAction(event -> {
                    createLoginScreen();
                }
        );
        createNewUserButton.setText("Create new User");
        createNewUserButton.setOnAction(event -> {
                    try {
                        out.writeObject("InterfaceCreation");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    formCreator.askServerForNewInterface(ConstValues.vCardName);
        }
        );


        layout1.getChildren().addAll(createNewUserButton,logInButton);
        layout1.setAlignment(Pos.CENTER);
        initScene = new Scene(layout1,300,300);
        window.centerOnScreen();
    }



    private void createLoginScreen()
    {
        LoginScreen loginScreen = new LoginScreen(window,initScene,out,in);
        Scene newScene = new Scene(loginScreen.getLayout(),400,600);
        window.setScene(newScene);
        window.centerOnScreen();
    }





    public String fileToStylesheetString ( File stylesheetFile ) {
        try {
            return stylesheetFile.toURI().toURL().toString();
        } catch ( MalformedURLException e ) {
            return null;
        }
    }
}