package org.vesalainen.nmea.viewer;

import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

public class MainApp extends Application
{
    private DoubleProperty fontSize = new SimpleDoubleProperty(10);
    private ViewerPreferences preferences;
    private PreferenceController preferencesController;
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
        bundle = I18n.get(locale);
        // preferences
        FXMLLoader preferencesLoader = new FXMLLoader(getClass().getResource("/fxml/preferences.fxml"), bundle);
        Parent preferencesPage = preferencesLoader.load();
        preferencesController = preferencesLoader.getController();
        preferences = new ViewerPreferences();
        preferencesController.bindPreferences(preferences);
        // pages
        FXMLLoader sailPage1Loader = new FXMLLoader(getClass().getResource("/fxml/sailPage1.fxml"), bundle);
        Parent sailPage1 = sailPage1Loader.load();
        
        service = new ViewerService(executor, preferences, locale);
        service.register(sailPage1.lookupAll("*"));
        service.start();
        
        StringBinding colorBinding = service.bindBackgroundColors();
        preferencesPage.styleProperty().bind(Bindings.concat(
                "-fx-base: ", colorBinding, ";",
                "-fx-font-family: ", preferences.getBinding("fontFamily"), ";")
        );

        Scene scene = new ViewerScene(preferencesPage, sailPage1);
        scene.getStylesheets().add("/styles/Styles.css");
        stage.setTitle("NMEA Viewer");
        stage.setScene(scene);
        stage.setFullScreen(true);
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
