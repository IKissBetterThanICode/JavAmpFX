/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javampfx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 *
 * @author Kuba
 */
public class Model {

    //a list underlying the playlistView
    private static ObservableList<Object> playlistItems = FXCollections.observableArrayList();
    //a "temporary" list only for adding files via a fileChooser
    private static List<File> selectedFileList = new ArrayList();
    //a list of files selected from a fileChooser
    private static List<File> selectedFileListFromChooser;
    //a "general" list of paths of files to be converted to media
    private List<Path> selectedFilePathList;

    private ObjectProperty currentItemProperty = new SimpleObjectProperty();
    private ObjectProperty<Paint> paintProperty = new SimpleObjectProperty();
    private DoubleProperty volumeProperty = new SimpleDoubleProperty();
    private DoubleProperty speedProperty = new SimpleDoubleProperty();
    private DoubleProperty progressProperty = new SimpleDoubleProperty();
    private ListProperty playlistItemsProperty = new SimpleListProperty();
    private boolean isPlaying, isPaused, shuffle, isSelected;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private Stage videoStage;
    private Scene videoScene;
    private StackPane videoPane;
    private Media mediaFile;
    private Label timeLabel, songLabel;
    private ListView playlistView;
    private ComboBox<String> speedBox;
    private Pane visualiserPane;
    private int currentItemIndex, currentPlaybackSpeed;
    private ProgressBar songProgressBar;
    private Timeline visualiserTimeline;
    private FileChooser fileChooser;

    public void play(Media media) {
        mediaFile = media;
        mediaPlayer = new MediaPlayer(media);
        mediaView = new MediaView(mediaPlayer);
        videoPane.getChildren().add(mediaView);
        if (Utils.getMediaFileExtension(media).equals("mp4")
                || Utils.getMediaFileExtension(media).equals("flv")) {
            mediaView.fitWidthProperty().bind(videoPane.widthProperty());
            mediaView.fitHeightProperty().bind(videoPane.heightProperty());
            videoStage.show();
        }

        mediaPlayer.volumeProperty().bind(volumeProperty);
        mediaPlayer.rateProperty().bind(speedProperty);

        if (!getIsPlaying()) {
            mediaPlayer.play();
            setIsPlaying(true);
        } else {
            mediaPlayer.stop();
            mediaPlayer.play();
            setIsPlaying(true);
        }
        if (getIsPaused()) {
            resume();
        }

        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {
                Duration totalDuration = mediaFile.getDuration();
                updateProgressBar(totalDuration);
            }
        });
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                if (Utils.getMediaFileExtension(media).equals("mp4")
                        || Utils.getMediaFileExtension(media).equals("flv")) {
                    videoStage.hide();
                }
                mediaPlayer.stop();
                next();
            }
        });
        playbackTime();
        getPlaylistView().scrollTo(getCurrentItemIndex());
        Platform.runLater(() -> {
            audioVisualiser(getVisualiserPane());
            songLabel.setText(getPlaylistItems().get(getCurrentItemIndex()).toString());
        });
    }

    public void open() {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Select file(s)");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio/Video files",
                        "*.aif", "*.aiff", "*.wav", "*.mp3", "*.aac", "*.mp4", "*.flv"));
        selectedFilePathList = new ArrayList();
        selectedFileListFromChooser = fileChooser.showOpenMultipleDialog(null);
        if (selectedFileListFromChooser != null) {
            //adds to an <Object> list underlying the playlistView
            selectedFilePathList.stream().forEach(getPlaylistItems()::add);
            //converts files to paths 
            selectedFilePathList = Utils.convertFileToPathList(selectedFileListFromChooser);
            //adds to the list in model
            getSelectedFileList().addAll(selectedFileListFromChooser);
        }
        if (selectedFilePathList != null) {
            //populates the playlist
            selectedFilePathList.stream().forEach(getPlaylistItems()::add);
        }
        getPlaylistView().getSelectionModel().selectFirst();
    }

    public void clear() {
        if (!getPlaylistItems().isEmpty()) {
            getPlaylistItems().clear();
        }
    }

    public void resume() {
        if (getIsPlaying()) {
            setIsPaused(false);
            mediaPlayer.play();
        }
    }

    public void stop() {
        if (getIsPlaying()) {
            mediaPlayer.stop();
            setIsPlaying(false);
        }
        visualiserPane.getChildren().clear();
    }

    public void pause() {
        if (getIsPlaying()) {
            mediaPlayer.pause();
            setIsPaused(true);
        }
    }

    public void previous() {
        if (!getShuffle()) {
            if (getCurrentItemIndex() > 0) {
                setCurrentItemIndex(getCurrentItemIndex() - 1);
            } else {
                setCurrentItemIndex(getPlaylistItems().size() - 1);
            }
        } else {
            setCurrentItemIndex(shuffleSelector());
        }
        if (getIsPlaying()) {
            mediaPlayer.stop();
            setIsPlaying(false);
        }
        if (playlistItems.size() != 0) {
            playlistView.requestFocus();
            playlistView.getSelectionModel().clearAndSelect(getCurrentItemIndex());
            System.out.println("Index: " + getCurrentItemIndex());
            mediaFile = Utils.convertObjectToMedia(playlistItems.get(getCurrentItemIndex()));
            play(mediaFile);
        }
    }

    public void next() {
        if (!getShuffle()) {
            if (getCurrentItemIndex() < getPlaylistItems().size() - 1) {
                setCurrentItemIndex(getCurrentItemIndex() + 1);
            } else {
                setCurrentItemIndex(0);
            }
        } else {
            setCurrentItemIndex(shuffleSelector());
        }

        if (getIsPlaying()) {
            mediaPlayer.stop();
            setIsPlaying(false);
        }
        if (playlistItems.size() != 0) {
            playlistView.getSelectionModel().clearAndSelect(getCurrentItemIndex());
            System.out.println("Index: " + getCurrentItemIndex());
            mediaFile = Utils.convertObjectToMedia(playlistItems.get(getCurrentItemIndex()));
            getPlaylistView().getFocusModel().focus(currentItemIndex);
            play(mediaFile);
        }
    }

    public void replay() {
        mediaPlayer.seek(Duration.seconds(0));
    }

    public void updateProgressBar(Duration duration) {
        double total = duration.toSeconds();
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            songProgressBar.setProgress(newValue.toSeconds() / total);
        });
    }

    public void playbackTime() {
        Platform.runLater(() -> {
            getTimeLabel().textProperty().bind(Bindings.createStringBinding(() -> {
                Duration currentTime = getMediaPlayer().getCurrentTime();
                return playbackTimeFormatter(currentTime);
            }, getMediaPlayer().currentTimeProperty()));
        });

    }

    private String playbackTimeFormatter(Duration elapsed) {
        int totalTime = (int) Math.floor(elapsed.toSeconds());
        int hrs = (totalTime / 3600);
        int mins = (totalTime / 60);
        int secs = (totalTime - hrs * 3600 - mins * 60);
        return String.format("%d:%02d:%02d",
                hrs, mins, secs);
    }

    private void audioVisualiser(Pane pane) {
        final int BANDS = 32;
        mediaPlayer.setAudioSpectrumNumBands(BANDS);

        pane.getChildren().clear();
        Rectangle[] bars = new Rectangle[BANDS];
        for (int i = 0; i < BANDS; i++) {
            bars[i] = new Rectangle();
            bars[i].setWidth(pane.getWidth() / BANDS);
            Bindings.bindBidirectional(bars[i].fillProperty(), paintProperty);
            pane.getChildren().add(bars[i]);
        }
        float[] correctedMagnitude = new float[BANDS];
        mediaPlayer.setAudioSpectrumThreshold(-60);                             //negative values; -60 = no correction
        mediaPlayer.setAudioSpectrumListener(new AudioSpectrumListener() {
            @Override
            public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
                for (int i = 0; i < BANDS; i++) {
                    correctedMagnitude[i] = magnitudes[i] - mediaPlayer.getAudioSpectrumThreshold();
                    bars[i].setHeight(correctedMagnitude[i]);
                    bars[i].relocate((i * (pane.getWidth()) / BANDS),
                            pane.getHeight() - bars[i].getHeight());
                    bars[i].setFill(Color.MAGENTA);
                }
            }
        });

        KeyFrame kf1 = new KeyFrame(Duration.ZERO, new KeyValue(paintProperty, Color.MAGENTA));
        KeyFrame kf2 = new KeyFrame(Duration.seconds(10), new KeyValue(paintProperty, Color.AQUAMARINE));
        KeyFrame kf3 = new KeyFrame(Duration.seconds(20), new KeyValue(paintProperty, Color.MEDIUMPURPLE));
        KeyFrame kf4 = new KeyFrame(Duration.seconds(30), new KeyValue(paintProperty, Color.MAGENTA));
        visualiserTimeline = new Timeline(kf1, kf2, kf3, kf4);
        visualiserTimeline.setCycleCount(Timeline.INDEFINITE);
        visualiserTimeline.setAutoReverse(true);
        visualiserTimeline.play();
    }

    public int shuffleSelector() {
        int selector = 0;
        if (!getPlaylistItems().isEmpty()) {
            selector = ThreadLocalRandom.current().nextInt(0, getPlaylistItems().size());
            System.out.println("szufiel: " + selector);
        }
        return selector;
    }

    public void initializeVideoScene() {
        videoStage = new Stage();
        videoPane = new StackPane();
        videoScene = new Scene(videoPane, 400, 300);
        videoStage.setScene(videoScene);
    }

    ///HALF A TON OF GETTERS AND SETTERS BELOW
    public ObservableList<Object> getPlaylistItems() {
        return playlistItems;
    }

    public List<File> getSelectedFileList() {
        return selectedFileList;
    }

    public void setSelectedFileList(List<File> selectedFileList) {
        this.selectedFileList = selectedFileList;
    }

    public ListView getPlaylistView() {
        return playlistView;
    }

    public void setPlaylistView(ListView playlistView) {
        this.playlistView = playlistView;
    }

    public Object getCurrentItem() {
        return currentItemProperty.get();
    }

    public void setCurrentItem(Object o) {
        currentItemProperty.set(o);
    }

    public ObjectProperty<Object> getCurrentItemProperty() {
        return currentItemProperty;
    }

    public double getVolume() {
        return volumeProperty.get();
    }

    public void setVolume(double d) {
        volumeProperty.set(d);
    }

    public DoubleProperty getVolumeProperty() {
        return volumeProperty;
    }

    public double getSpeed() {
        return speedProperty.get();
    }

    public void setSpeed(double d) {
        speedProperty.set(d);
    }

    public DoubleProperty getSpeedProperty() {
        return speedProperty;
    }

    public double getProgress() {
        return progressProperty.get();
    }

    public void setProgress(double d) {
        progressProperty.set(d);
    }

    public DoubleProperty getProgressProperty() {
        return progressProperty;
    }

    public Media getMediaFile() {
        return mediaFile;
    }

    public void setMediaFile(Media mediaFile) {
        this.mediaFile = mediaFile;
    }

    public boolean getIsPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public int getCurrentItemIndex() {
        return currentItemIndex;
    }

    public void setCurrentItemIndex(int currentItemIndex) {
        this.currentItemIndex = currentItemIndex;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public Label getTimeLabel() {
        return timeLabel;
    }

    public void setTimeLabel(Label timeLabel) {
        this.timeLabel = timeLabel;
    }

    public Pane getVisualiserPane() {
        return visualiserPane;
    }

    public void setVisualiserPane(Pane visualiserPane) {
        this.visualiserPane = visualiserPane;
    }

    public Label getSongLabel() {
        return songLabel;
    }

    public void setSongLabel(Label songLabel) {
        this.songLabel = songLabel;
    }

    public ComboBox<String> getSpeedBox() {
        return speedBox;
    }

    public void setSpeedBox(ComboBox<String> speedBox) {
        this.speedBox = speedBox;
    }

    public boolean getIsPaused() {
        return isPaused;
    }

    public void setIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public boolean getShuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public ProgressBar getSongProgressBar() {
        return songProgressBar;
    }

    public void setSongProgressBar(ProgressBar songProgressBar) {
        this.songProgressBar = songProgressBar;
    }

    public Paint getPaint() {
        return paintProperty.get();
    }

    public void setPaint(Paint paint) {
        paintProperty.set(paint);
    }

    public ObjectProperty<Paint> getPaintProperty() {
        return paintProperty;
    }

    public int getCurrentPlaybackSpeed() {
        return currentPlaybackSpeed;
    }

    public void setCurrentPlaybackSpeed(int currentPlaybackSpeed) {
        this.currentPlaybackSpeed = currentPlaybackSpeed;
    }

    public ListProperty getplaylistItemsProperty() {
        return playlistItemsProperty;
    }

}
