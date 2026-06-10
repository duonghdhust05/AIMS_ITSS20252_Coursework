package com.aimsfx.controller.ProductManagerController;

import com.aimsfx.model.Track;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class CDTrackItemController {

    @FXML
    private Label numberLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private Label durationLabel;

    @FXML
    private Button removeBtn;

    public void setTrackData(Track track, int index, java.util.function.Consumer<Integer> onRemove) {
        numberLabel.setText((index + 1) + ".");
        titleLabel.setText(track.getTitle());
        durationLabel.setText(formatDuration(track.getDuration()));
        removeBtn.setOnAction(e -> onRemove.accept(index));
    }

    private String formatDuration(Integer seconds) {
        if (seconds == null)
            return "00:00";
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}
