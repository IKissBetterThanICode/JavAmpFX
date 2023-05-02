/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javampfx;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author Kuba
 */
public class JavAmpFX extends Application {

    private Model model = new Model();
    private double xOffset, yOffset, newX, newY;
    private static Scene playlistScene;
    private static Stage playlistStage;
    private static DoubleProperty playlistSnapXProperty = new SimpleDoubleProperty();
    private static DoubleProperty playlistSnapYProperty = new SimpleDoubleProperty();
    private static boolean isAttached;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader guiLoader = new FXMLLoader(getClass().getResource("GUI.fxml"));
        Scene scene = new Scene(guiLoader.load());
        stage.initStyle(StageStyle.UNDECORATED);
        GUIController guiController = guiLoader.getController();
        stage.setScene(scene);
        stage.setTitle("JavAmpFX");
        stage.setResizable(false);
        stage.show();

        FXMLLoader playlistLoader = new FXMLLoader(getClass().getResource("Playlist.fxml"));
        playlistScene = new Scene(playlistLoader.load());
        playlistStage = new Stage();
        PlaylistController playlistController = playlistLoader.getController();
        playlistStage.initStyle(StageStyle.UNDECORATED);
        playlistStage.setScene(playlistScene);
        playlistStage.setX(stage.getX());
        playlistStage.setY(stage.getY() + stage.getHeight());
        playlistStage.requestFocus();
        playlistStage.setTitle("Playlist");
        playlistStage.show();

        setIsAttached(true);
        playlistSnapXProperty.bind(stage.xProperty());
        playlistSnapYProperty.bind(stage.yProperty().add(stage.getHeight()));

        scene.setOnMousePressed((event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        scene.setOnMouseDragged((event) -> {
            //new x y coords of the primary stage
            newX = (event.getScreenX() - xOffset);
            newY = (event.getScreenY() - yOffset);
            stage.setX(newX);
            stage.setY(newY);

            //moves the secondary scene with the primary one
            if (getIsAttached()) {
                playlistStage.setX(getPlaylistSnapX());
                playlistStage.setY(getPlaylistSnapY());
            } 
        });

        playlistController.initModel(model);
        guiController.initModel(model);
        model.initializeVideoScene();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    public Scene getPlaylistScene() {
        return playlistScene;
    }

    public Stage getPlaylistStage() {
        return playlistStage;
    }

    public double getPlaylistSnapX() {
        return playlistSnapXProperty.get();
    }

    public void setPlaylistSnapX(double d) {
        this.playlistSnapXProperty.set(d);
    }

    public DoubleProperty getPlaylistSnapXProperty() {
        return playlistSnapXProperty;
    }

    public double getPlaylistSnapY() {
        return playlistSnapYProperty.get();
    }

    public void setPlaylistSnapY(double d) {
        this.playlistSnapYProperty.set(d);
    }

    public DoubleProperty getPlaylistSnapYProperty() {
        return playlistSnapYProperty;
    }
    
    public boolean getIsAttached() {
        return isAttached;
    }

    public void setIsAttached(boolean isAttached) {
        this.isAttached = isAttached;
    }    

}
