package org.vesalainen.nmea.viewer;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

public class MainApp extends Application
{
    private DoubleProperty fontSize = new SimpleDoubleProperty(10);
    private ViewerPreferences preferences;
    private ViewerController controller;
    private ViewerService service;
    private ResourceBundle bundle;
    private CachedScheduledThreadPool executor;

    @Override
    public void init() throws Exception
    {
        executor = new CachedScheduledThreadPool();
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        Locale locale = Locale.getDefault();
        bundle = ResourceBundle.getBundle(I18n.class.getName(), locale);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/viewer.fxml"), bundle);
        Parent root = loader.load();
        controller = loader.getController();
        preferences = new ViewerPreferences();
        controller.bindPreferences(preferences);
        service = new ViewerService(executor, preferences, locale);
        service.register(root.lookupAll("*"));
        service.start();
        
        StringBinding colorBinding = service.bindBackgroundColors();
        root.styleProperty().bind(Bindings.concat(
                "-fx-base: ", colorBinding, ";",
                "-fx-font-family: ", preferences.getBinding("fontFamily"), ";")
        );

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        stage.setTitle("NMEA Viewer");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception
    {
        service.stop();
        executor.shutdownNow();
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
