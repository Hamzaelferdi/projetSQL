package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Workshop;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.WorkshopService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controleur de l'onglet "Workshops" : lecture + CRUD persistante.
 */
public class WorkshopController {
    @FXML private TableView<Workshop> workshopTable;
    @FXML private TableColumn<Workshop, String> titleColumn;
    @FXML private TableColumn<Workshop, LocalDateTime> dateColumn;
    @FXML private TableColumn<Workshop, String> instructorColumn;
    @FXML private TableColumn<Workshop, Double> priceColumn;
    @FXML private TableColumn<Workshop, String> levelColumn;

    private final WorkshopService workshopService = ServiceProvider.getWorkshopService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        instructorColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getInstructor() != null ? cd.getValue().getInstructor().getName() : "Unknown"));
        refreshTable();
    }

    private void refreshTable() {
        workshopTable.setItems(FXCollections.observableArrayList(workshopService.getAllWorkshops()));
    }

    @FXML
    private void handleAdd() {
        showDialog(null).ifPresent(w -> {
            try {
                workshopService.createWorkshop(w);
                refreshTable();
                DialogUtil.showInfo("Workshop cree",
                        "Le workshop \"" + w.getTitle() + "\" a ete ajoute.");
            } catch (RuntimeException ex) {
                DialogUtil.showError("Echec de la creation", ex);
            }
        });
    }

    @FXML
    private void handleEdit() {
        Workshop selected = workshopTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Aucun workshop selectionne",
                    "Selectionne d'abord un workshop.");
            return;
        }
        showDialog(selected).ifPresent(w -> {
            try {
                workshopService.updateWorkshop(w);
                refreshTable();
                DialogUtil.showInfo("Workshop mis a jour",
                        "Les modifications ont ete enregistrees.");
            } catch (RuntimeException ex) {
                DialogUtil.showError("Echec de la mise a jour", ex);
            }
        });
    }

    @FXML
    private void handleDelete() {
        Workshop selected = workshopTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Aucun workshop selectionne",
                    "Selectionne d'abord un workshop.");
            return;
        }
        if (DialogUtil.confirm("Confirmation de suppression",
                "Supprimer definitivement \"" + selected.getTitle() + "\" ?")) {
            try {
                workshopService.deleteWorkshop(selected.getTitle());
                refreshTable();
                DialogUtil.showInfo("Workshop supprime", "Suppression effectuee.");
            } catch (RuntimeException ex) {
                DialogUtil.showError("Echec de la suppression", ex);
            }
        }
    }

    private Optional<Workshop> showDialog(Workshop existing) {
        Dialog<Workshop> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouveau workshop" : "Modifier le workshop");
        dialog.setHeaderText(existing == null
                ? "Saisir les informations du nouveau workshop"
                : "Modifier \"" + existing.getTitle() + "\"");

        ButtonType okType = new ButtonType(existing == null ? "Creer" : "Enregistrer",
                ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField titleField = new TextField();
        titleField.setPromptText("Titre (identifiant)");
        DatePicker dateField = new DatePicker();
        TextField timeField = new TextField();
        timeField.setPromptText("Heure (HH:mm)");
        TextField durationField = new TextField();
        durationField.setPromptText("Duree (minutes)");
        TextField maxField = new TextField();
        maxField.setPromptText("Nb participants max");
        TextField priceField = new TextField();
        priceField.setPromptText("Prix");
        TextField locationField = new TextField();
        locationField.setPromptText("Lieu");
        ComboBox<String> levelBox = new ComboBox<>(FXCollections.observableArrayList(
                "beginner", "intermediate", "advanced"));
        TextArea descField = new TextArea();
        descField.setPromptText("Description");
        descField.setPrefRowCount(3);

        ComboBox<Artist> instructorBox = new ComboBox<>(FXCollections.observableArrayList(
                artistService.getAllArtists()));
        instructorBox.setConverter(new StringConverter<>() {
            @Override public String toString(Artist a) { return a == null ? "" : a.getName(); }
            @Override public Artist fromString(String s) { Artist a = new Artist(); a.setName(s); return a; }
        });
        instructorBox.setPromptText("Instructeur");

        if (existing != null) {
            titleField.setText(DialogUtil.safe(existing.getTitle()));
            titleField.setEditable(false);
            if (existing.getDate() != null) {
                dateField.setValue(existing.getDate().toLocalDate());
                timeField.setText(existing.getDate().toLocalTime().format(TIME_FMT));
            }
            durationField.setText(String.valueOf(existing.getDurationMinutes()));
            maxField.setText(String.valueOf(existing.getMaxParticipants()));
            priceField.setText(String.valueOf(existing.getPrice()));
            locationField.setText(DialogUtil.safe(existing.getLocation()));
            levelBox.setValue(existing.getLevel());
            descField.setText(DialogUtil.safe(existing.getDescription()));
            if (existing.getInstructor() != null) {
                for (Artist a : instructorBox.getItems()) {
                    if (a.getName().equals(existing.getInstructor().getName())) {
                        instructorBox.setValue(a);
                        break;
                    }
                }
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.setPadding(new Insets(20, 10, 10, 10));
        int r = 0;
        grid.add(new Label("Titre *"), 0, r);     grid.add(titleField, 1, r++);
        grid.add(new Label("Instructeur"), 0, r); grid.add(instructorBox, 1, r++);
        grid.add(new Label("Date"), 0, r);        grid.add(dateField, 1, r++);
        grid.add(new Label("Heure"), 0, r);       grid.add(timeField, 1, r++);
        grid.add(new Label("Duree (min)"), 0, r); grid.add(durationField, 1, r++);
        grid.add(new Label("Participants max"), 0, r); grid.add(maxField, 1, r++);
        grid.add(new Label("Prix"), 0, r);        grid.add(priceField, 1, r++);
        grid.add(new Label("Lieu"), 0, r);        grid.add(locationField, 1, r++);
        grid.add(new Label("Niveau"), 0, r);      grid.add(levelBox, 1, r++);
        grid.add(new Label("Description"), 0, r); grid.add(descField, 1, r++);
        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node okBtn = dialog.getDialogPane().lookupButton(okType);
        okBtn.setDisable(titleField.getText().trim().isEmpty());
        titleField.textProperty().addListener((o, ov, nv) -> okBtn.setDisable(nv.trim().isEmpty()));

        dialog.setResultConverter(button -> {
            if (button != okType) return null;
            Workshop w = existing == null ? new Workshop() : existing;
            w.setTitle(titleField.getText().trim());
            // Construction du LocalDateTime a partir de la date + heure
            LocalDate ld = dateField.getValue();
            LocalTime lt = LocalTime.MIDNIGHT;
            try {
                if (!timeField.getText().isBlank()) lt = LocalTime.parse(timeField.getText().trim(), TIME_FMT);
            } catch (Exception ignored) { /* heure invalide -> minuit */ }
            if (ld != null) w.setDate(LocalDateTime.of(ld, lt));
            try { w.setDurationMinutes(durationField.getText().isBlank() ? 0 : Integer.parseInt(durationField.getText().trim())); }
            catch (NumberFormatException nfe) { w.setDurationMinutes(0); }
            try { w.setMaxParticipants(maxField.getText().isBlank() ? 0 : Integer.parseInt(maxField.getText().trim())); }
            catch (NumberFormatException nfe) { w.setMaxParticipants(0); }
            try { w.setPrice(priceField.getText().isBlank() ? 0.0 : Double.parseDouble(priceField.getText().trim())); }
            catch (NumberFormatException nfe) { w.setPrice(0.0); }
            w.setLocation(DialogUtil.emptyToNull(locationField.getText()));
            w.setLevel(levelBox.getValue());
            w.setDescription(DialogUtil.emptyToNull(descField.getText()));
            w.setInstructor(instructorBox.getValue());
            return w;
        });

        return dialog.showAndWait();
    }
}
