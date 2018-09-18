package UserClient;

import GlobalClasses.ListProperty;
import GlobalClasses.SearchableList;
import GlobalClasses.Subclass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class VcardScreen {

    private VBox rootLayout;



    public VcardScreen(List<ListProperty> properties,  Stage window, Scene assetScene)
    {
        rootLayout = new VBox(30);

        Button button = new Button("Back");
        button.setMaxWidth(300);

        button.setOnAction(event -> {
                        window.setScene(assetScene);
                        window.centerOnScreen();
                }
        );
        Label title = new Label("My V-Card");
        title.setFont(Font.font("Verdana", FontWeight.BOLD,20));
        title.setTextAlignment(TextAlignment.CENTER);
        rootLayout.getChildren().add(title);
        rootLayout.getChildren().add(createVcardVBox(properties));
        rootLayout.getChildren().add(button);
        rootLayout.setAlignment(Pos.CENTER);

    }

    private VBox createVcardVBox(List<ListProperty> properties)
    {
        List<Node> allTexts = new ArrayList<>();
        VBox rightVBox = new VBox(20);
        rightVBox.setAlignment(Pos.CENTER);
        rightVBox.setPadding(new Insets(0,0,0,50));
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
                newText.setWrappingWidth(250);
                allTexts.add(newText);
            }
            else if(p.getValues() == null)
            {
                HBox newHBox = new HBox(5);
                newHBox.getChildren().add(new Text(p.getPropertyName()+ ": "));
                VBox finalVBox = buildNestedText(p);
                newHBox.getChildren().add(finalVBox);
                newHBox.setMaxWidth(250);
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

    public VBox getRootLayout()
    {
        return rootLayout;
    }
}
