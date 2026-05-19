package com.project.artconnect.ui;

import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Gallery;
import com.project.artconnect.service.GalleryService;
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
import java.util.Optional;

/**
 * Controleur de l'onglet "Exhibitions" : lecture + CRUD persistante.
 */
public class ExhibitionController {
    @FXML private TableView<Exhibition> exhibitionTable;
    @FXML private TableColumn<Exhibition, String> titleColumn;
    @FXML private TableColumn<Exhibition, LocalDate> dateColumn;
    @FXML private TableColumn<Exhibition, String> themeColumn;
    @FXML private TableColumn<Exhibition, String> galleryColumn;

    private final GalleryService galleryService = ServiceProvider.getGalleryService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        themeColumn.setCellValueFactory(new PropertyValueFactory<>("theme"));
        galleryColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getGallery() != null ? cd.getValue().getGallery().getName() : "Unknown"));
        refreshTable();
    }

    private void refreshTable() {
        exhibitionTable.setItems(FXCollections.observableArrayList(galleryService.getAllExhibitions()));
    }

    @FXML
    private void handleAdd() {
        showDialog(null).ifPresent(e -> {
            try {
                galleryService.createExhibition(e);
                refreshTable();
                DialogUtil.showInfo("Exposition creee",
                        "L'exposition \"" + e.getTitle() + "\" a ete ajoutee.");
            } catch (RuntimeException ex) {
                DialogUtil.showError("Echec de la creation", ex);
            }
        });
    }

    @FXML
    private void handleEdit() {
        Exhibition selected = exhibitionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Aucune exposition selectionnee",
                    "Selectionne d'abord une exposition.");
            return;
        }
        showDialog(selected).ifPresent(e -> {
            try {
                galleryService.updateExhibition(e);
                refreshTable();
                DialogUtil.showInfo("Exposition mise a jour",
                        "Les modifications ont ete enregistrees.");
            } catch (RuntimeException ex) {
                DialogUtil.showError("Echec de la mise a jour", ex);
            }
        });
    }

    @FXML
    private void handleDelete() {
        Exhibition selected = exhibitionTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Aucune exposition selectionnee",
                    "Selectionne d'abord une exposition.");
            return;
        }
        if (DialogUtil.confirm("Confirmation de suppression",
                "Supprimer definitivement \"" + selected.getTitle() + "\" ?")) {
            try {
                galleryService.deleteExhibition(selected.getTitle());
                refreshTable();
                DialogUtil.showInfo("Exposition supprimee", "Suppression effectuee.");
            } catch (RuntimeException ex) {
                DialogUtil.showError("Echec de la suppression", ex);
            }
        }
    }

    /** Dialog de saisie/edition d'une Exhibition. */
    private Optional<Exhibition> showDialog(Exhibition existing) {
        Dialog<Exhibition> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouvelle exposition" : "Modifier l'exposition");
        dialog.setHeaderText(existing == null
                ? "Saisir les informations de la nouvelle exposition"
                : "Modifier \"" + existing.getTitle() + "\"");

        ButtonType okType = new ButtonType(existing == null ? "Creer" : "Enregistrer",
                ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField titleField = new TextField();
        titleField.setPromptText("Titre (identifiant)");
        DatePicker startField = new DatePicker();
        DatePicker endField = new DatePicker();
        TextField curatorField = new TextField();
        curatorField.setPromptText("Curateur");
        TextField themeField = new TextField();
        themeField.setPromptText("Theme");
        TextArea descField = new TextArea();
        descField.setPromptText("Description");
        descField.setPrefRowCount(3);

        ComboBox<Gallery> galleryBox = new ComboBox<>(FXCollections.observableArrayList(
                galleryService.getAllGalleries()));
        galleryBox.setConverter(new StringConverter<>() {
            @Override public String toString(Gallery g) { return g == null ? "" : g.getName(); }
            @Override public Gallery fromString(String s) { Gallery g = new Gallery(); g.setName(s); return g; }
        });
        galleryBox.setPromptText("Galerie");

        if (existing != null) {
            titleField.setText(DialogUtil.safe(existing.getTitle()));
            titleField.setEditable(false);
            startField.setValue(existing.getStartDate());
            endField.setValue(existing.getEndDate());
            curatorField.setText(DialogUtil.safe(existing.getCuratorName()));
            themeField.setText(DialogUtil.safe(existing.getTheme()));
            descField.setText(DialogUtil.safe(existing.getDescription()));
            if (existing.getGallery() != null) {
                for (Gallery g : galleryBox.getItems()) {
                    if (g.getName().equals(existing.getGallery().getName())) {
                        galleryBox.setValue(g);
                        break;
                    }
                }
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.setPadding(new Insets(20, 10, 10, 10));
        int r = 0;
        grid.add(new Label("Titre *"), 0, r);    grid.add(titleField, 1, r++);
        grid.add(new Label("Galerie"), 0, r);    grid.add(galleryBox, 1, r++);
        grid.add(new Label("Debut"), 0, r);      grid.add(startField, 1, r++);
        grid.add(new Label("Fin"), 0, r);        grid.add(endField, 1, r++);
        grid.add(new Label("Curateur"), 0, r);   grid.add(curatorField, 1, r++);
        grid.add(new Label("Theme"), 0, r);      grid.add(themeField, 1, r++);
        grid.add(new Label("Description"), 0, r);grid.add(descField, 1, r++);
        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node okBtn = dialog.getDialogPane().lookupButton(okType);
        okBtn.setDisable(titleField.getText().trim().isEmpty());
        titleField.textProperty().addListener((o, ov, nv) -> okBtn.setDisable(nv.trim().isEmpty()));

        dialog.setResultConverter(button -> {
            if (button != okType) return null;
            Exhibition e = existing == null ? new Exhibition() : existing;
            e.setTitle(titleField.getText().trim());
            e.setStartDate(startField.getValue());
            e.setEndDate(endField.getValue());
            e.setCuratorName(DialogUtil.emptyToNull(curatorField.getText()));
            e.setTheme(DialogUtil.emptyToNull(themeField.getText()));
            e.setDescription(DialogUtil.emptyToNull(descField.getText()));
            e.setGallery(galleryBox.getValue());
            return e;
        });

        return dialog.showAndWait();
    }
}
