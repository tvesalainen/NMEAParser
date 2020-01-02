package org.vesalainen.nmea.viewer;

import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application
{
    private DoubleProperty fontSize = new SimpleDoubleProperty(10);
    private ViewerPreferences preferences;
    private ViewerController controller;
    private ViewerService service;
    private ResourceBundle bundle;
    
    @Override
    public void start(Stage stage) throws Exception
    {
        bundle = ResourceBundle.getBundle(I18n.class.getName(), Locale.getDefault());
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/viewer.fxml"), bundle);
        Parent root = loader.load();
        controller = loader.getController();
        preferences = new ViewerPreferences();
        controller.bindPreferences(preferences);
        service = new ViewerService(root.lookupAll(".gauge"));
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        fontSize.bind(scene.widthProperty().add(scene.heightProperty()).divide(80));
        root.styleProperty().bind(Bindings.concat("-fx-font-size: ", fontSize.asString(), ";"));
        stage.setTitle("NMEA Viewer");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        launch(args);
    }

}
