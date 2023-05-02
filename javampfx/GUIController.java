/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javampfx;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.util.Duration;

/**
 *
 * @author Kuba
 */
public class GUIController implements Initializable {

    @FXML
    private AnchorPane bgPane, bottomPane;
    @FXML
    private Pane visualiserPane;
    @FXML
    private HBox leftHBox, rightHBox;
    @FXML
    private Label noOfItemsLabel, songLabel, timeLabel;
    @FXML
    private Button playButton, pauseButton, stopButton, clearListButton, previousButton,
            nextButton, openButton, replayButton, exitButton, menuButton, attachPlaylistButton;
    @FXML
    private ToggleButton shuffleToggleButton;
    @FXML
    private ComboBox<String> speedBox;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ProgressBar songProgressBar;

    private FileChooser fileChooser;
    private Model model;
    private MediaPlayer mediaPlayer;
    private Media mediaFile;
    private Timer timer;
    private TimerTask timerTask;
    private boolean shuffle;
    private double[] speeds = {0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 1.75, 2.0};
    private double playbackSpeed;
    private DoubleProperty speedProperty = new SimpleDoubleProperty(1.0);
    private JavAmpFX main;

    public void initModel(Model model) {
        if (this.model != null) {
            throw new IllegalStateException("Model can only be initialized once");
        }
        this.model = model;
        playButton.disableProperty().bind(model.getCurrentItemProperty().isNull());
        model.getVolumeProperty().bind(volumeSlider.valueProperty().multiply(.01));
        model.getSpeedProperty().bind(speedProperty);
        model.setTimeLabel(timeLabel);
        model.setVisualiserPane(visualiserPane);
        model.setSongLabel(songLabel);
        model.setShuffle(shuffle);
        model.setSongProgressBar(songProgressBar);
        playButton.disableProperty().bind(model.getplaylistItemsProperty().emptyProperty());   
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        for (int i = 0; i < speeds.length; i++) {
            speedBox.getItems().add(Double.toString(speeds[i]));
        }
        speedBox.setOnAction((event) -> {
            playbackSpeed = Double.parseDouble(speedBox.getSelectionModel().getSelectedItem());
            speedProperty.set(playbackSpeed);
        });
        main = new JavAmpFX();
    }

    @FXML
    private void openMedia() {
        model.open();
    }
    
    @FXML
    private void clearList() {
        model.clear();
    }

    @FXML
    public void playMedia() {
        if (!model.getIsPaused()) {
            mediaFile = Utils.convertObjectToMedia(model.getPlaylistView().getSelectionModel().getSelectedItem());
            if (model.getIsPlaying()) {
                model.stop();
            }
            model.play(mediaFile);
            model.playbackTime();
        } else {
            model.resume();
        }
    }

    @FXML
    public void pauseMedia() {
        model.pause();
    }

    @FXML
    public void replayMedia() {
        songProgressBar.setProgress(0);
        model.replay();
    }

    @FXML
    public void stopMedia() {
        model.stop();
        songProgressBar.setProgress(0);
    }

    @FXML
    public void seekMedia() {
        System.out.println(model.getMediaFile());
        if (model.getMediaFile() != null) {
            songProgressBar.setOnMousePressed((event) -> {
                double seekPos = (event.getX() / songProgressBar.getWidth());
                double seekTo = model.getMediaFile().getDuration().toSeconds() * seekPos;
                songProgressBar.setProgress(seekPos);
                if (seekPos == 1) {
//                    model.cancelTimer();
                }
                model.getMediaPlayer().seek(Duration.seconds(seekTo));
            });
        }
    }

    @FXML
    public void shuffleMedia() {
        model.setShuffle(shuffleToggleButton.isSelected());
    }

    @FXML
    public void previousMedia() {
        model.previous();
    }

    @FXML
    public void nextMedia() {
        model.next();
    }

    @FXML
    public void exitApp() {
        System.exit(0);
    }

    @FXML
    public void openMenu() {

    }

    @FXML
    public void attachPlaylist() {
        if (!main.getIsAttached()) {
            main.getPlaylistStage().setX(main.getPlaylistSnapX());
            main.getPlaylistStage().setY(main.getPlaylistSnapY());
            main.setIsAttached(true);
        }
    }

    //GETTERS AND SETTERS 
    public Slider getVolumeSlider() {
        return volumeSlider;
    }
}
