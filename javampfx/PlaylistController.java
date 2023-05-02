/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javampfx;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.media.Media;

/**
 * FXML Controller class
 *
 * @author Kuba
 */
public class PlaylistController implements Initializable {

    private Model model;
    private Media mediaFile;

    @FXML
    private ListView playlistView;
    @FXML
    private Label noOfItemsLabel;
    @FXML
    Button dragButton;

    private JavAmpFX main;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        playlistView.setPlaceholder(new Label("Nuffin' here!"));

    }

    public void initModel(Model model) {
        // ensure model is only set once:
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        this.model = model;
        playlistView.setItems(model.getPlaylistItems());
        playlistView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        model.setPlaylistView(playlistView);
        removeFromPlaylist();
        model.getplaylistItemsProperty().bind(playlistView.itemsProperty());
    }

    @FXML
    private void listClick() {
        playlistView.getSelectionModel().select(model.getCurrentItemIndex());
        playlistView.setOnMouseClicked((event) -> {
            if (playlistView.getSelectionModel().isEmpty() && event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY) {
                model.open();
            }
            if (!playlistView.getSelectionModel().isEmpty()) {
                if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
                    Object currentItemSelected = playlistView.getSelectionModel().getSelectedItem();
                    model.setCurrentItemIndex(playlistView.getSelectionModel().getSelectedIndex());
                    mediaFile = Utils.convertObjectToMedia(currentItemSelected);
                    if (model.getIsPlaying()) {
                        model.stop();
                    }
                    model.play(mediaFile);
                    model.playbackTime();
                }
                if (event.getClickCount() == 1 && event.getButton() == MouseButton.PRIMARY) {
                    model.setCurrentItemIndex(playlistView.getSelectionModel().getSelectedIndex());
                    Object currentItemSelected = playlistView.getSelectionModel().getSelectedItem();
                    model.setCurrentItem(currentItemSelected);
                }
                if (event.getClickCount() == 1 && event.getButton() == MouseButton.SECONDARY) {
                    model.setCurrentItemIndex(playlistView.getSelectionModel().getSelectedIndex());
                    Object currentItemSelected = playlistView.getSelectionModel().getSelectedItem();
                    model.setCurrentItem(currentItemSelected);
                }
            }
        });
    }

    private void removeFromPlaylist() {
        List<Object> toRemove = playlistView.getSelectionModel().getSelectedItems();
        playlistView.setOnKeyPressed((KeyEvent event) -> {
            KeyCode key = event.getCode();
            switch (key) {
                case DELETE:
                    model.getPlaylistItems().removeAll(toRemove);
                    break;
            }
        });

    }

    double xOffset;
    double yOffset;

    @FXML
    public void dragMe() {
        main = new JavAmpFX();
        main.setIsAttached(false);
        dragButton.setOnMousePressed((event) -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        dragButton.setOnMouseDragged((event) -> {
            main.getPlaylistStage().setX(event.getScreenX() - xOffset);
            main.getPlaylistStage().setY(event.getScreenY() - yOffset);
        });
    }

    public Label getNoOfItemsLabel() {
        return noOfItemsLabel;
    }

    public void setNoOfItemsLabel(Label noOfItemsLabel) {
        this.noOfItemsLabel = noOfItemsLabel;
    }

    public ListView getPlaylistView() {
        return playlistView;
    }

    public void setPlaylistView(ListView playlistView) {
        this.playlistView = playlistView;
    }

}
