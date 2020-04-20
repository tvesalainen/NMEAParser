package org.vesalainen.nmea.viewer;

import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;

public class MainApp extends Application
{
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
        preferences = new ViewerPreferences();

        ViewerPage preferencesPage = ViewerPage.loadPreferencePage(preferences, "/fxml/preferences.fxml", bundle);

        service = new ViewerService(executor, preferences, locale);
        StringBinding colorBinding = service.bindBackgroundColors();
        StringExpression styleExpression = Bindings.concat(
                "-fx-base: ", colorBinding, ";",
                "-fx-font-family: ", preferences.getBinding("fontFamily"), ";");
        preferencesPage.getParent().styleProperty().bind(styleExpression);
        // pages
        ViewerPage sailPage1 = ViewerPage.loadPage(service, "/fxml/sailPage1.fxml", bundle, styleExpression);

        service.start();
        

        Property<Integer> currentPage = new SimpleObjectProperty<>(0);
        preferences.bindInteger("currentPage", 0, currentPage);
        Scene scene = new ViewerScene(stage, currentPage, preferencesPage, sailPage1);
        scene.getStylesheets().add("/styles/Styles.css");
        stage.setScene(scene);
        stage.setFullScreen(true);
        I18n.bind(stage.titleProperty(), "mainTitle");
        I18n.bind(stage.fullScreenExitHintProperty(), "fullScreenExitHint");
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
