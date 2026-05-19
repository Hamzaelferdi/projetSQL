package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Artwork;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.service.ArtworkService;
import com.project.artconnect.util.ServiceProvider;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.Optional;

/**
 * Controleur de l'onglet "Artworks" : lecture + CRUD persistante.
 */
public class ArtworkController {
    @FXML private TableView<Artwork> artworkTable;
    @FXML private TableColumn<Artwork, String> titleColumn;
    @FXML private TableColumn<Artwork, String> typeColumn;
    @FXML private TableColumn<Artwork, Double> priceColumn;
    @FXML private TableColumn<Artwork, String> statusColumn;
    @FXML private TableColumn<Artwork, String> artistColumn;

    private final ArtworkService artworkService = ServiceProvider.getArtworkService();
    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        artistColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getArtist() != null ? cd.getValue().getArtist().getName() : "Unknown"));
        refreshTable();
    }

    private void refreshTable() {
        artworkTable.setItems(FXCollections.observableArrayList(artworkService.getAllArtworks()));
    }

    @FXML
    private void handleAdd() {
        showDialog(null).ifPresent(a -> {
            try {
                artworkService.createArtwork(a);
                refreshTable();
                DialogUtil.showInfo("Oeuvre creee", "L'oeuvre \"" + a.getTitle() + "\" a ete ajoutee.");
            } catch (RuntimeException ex) {
                DialogUtil.showError("Echec de la creation", ex);
            }
        });
    }

    @FXML
    private void handleEdit() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Aucune oeuvre selectionnee",
                    "Selectionne d'abord une oeuvre dans la table.");
            return;
        }
        showDialog(selected).ifPresent(a -> {
            try {
                artworkService.updateArtwork(a);
                refreshTable();
                DialogUtil.showInfo("Oeuvre mise a jour",
                        "Les modifications ont ete enregistrees.");
            } catch (RuntimeException ex) {
                DialogUtil.showError("Echec de la mise a jour", ex);
            }
        });
    }

    @FXML
    private void handleDelete() {
        Artwork selected = artworkTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Aucune oeuvre selectionnee",
                    "Selectionne d'abord une oeuvre dans la table.");
            return;
        }
        if (DialogUtil.confirm("Confirmation de suppression",
                "Supprimer definitivement l'oeuvre \"" + selected.getTitle() + "\" ?")) {
            try {
                artworkService.deleteArtwork(selected.getTitle());
                refreshTable();
                DialogUtil.showInfo("Oeuvre supprimee", "Suppression effectuee.");
            } catch (RuntimeException ex) {
                DialogUtil.showError("Echec de la suppression", ex);
            }
        }
    }

    /** Dialog de saisie/edition d'une Artwork. */
    private Optional<Artwork> showDialog(Artwork existing) {
        Dialog<Artwork> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouvelle oeuvre" : "Modifier l'oeuvre");
        dialog.setHeaderText(existing == null
                ? "Saisir les informations de la nouvelle oeuvre"
                : "Modifier les informations de \"" + existing.getTitle() + "\"");

        ButtonType okType = new ButtonType(existing == null ? "Creer" : "Enregistrer",
                ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField titleField = new TextField();
        titleField.setPromptText("Titre (identifiant)");
        TextField yearField = new TextField();
        yearField.setPromptText("Annee de creation");
        TextField typeField = new TextField();
        typeField.setPromptText("Type (peinture, sculpture...)");
        TextField mediumField = new TextField();
        mediumField.setPromptText("Medium (huile, aquarelle...)");
        TextField dimField = new TextField();
        dimField.setPromptText("Dimensions");
        TextField priceField = new TextField();
        priceField.setPromptText("Prix");
        ComboBox<String> statusBox = new ComboBox<>(FXCollections.observableArrayList(
                "FOR_SALE", "SOLD", "EXHIBITED"));
        statusBox.setValue("FOR_SALE");
        TextArea descField = new TextArea();
        descField.setPromptText("Description");
        descField.setPrefRowCount(3);

        // ComboBox des artistes existants
        ComboBox<Artist> artistBox = new ComboBox<>(FXCollections.observableArrayList(
                artistService.getAllArtists()));
        artistBox.setConverter(new StringConverter<>() {
            @Override public String toString(Artist a) { return a == null ? "" : a.getName(); }
            @Override public Artist fromString(String s) { Artist a = new Artist(); a.setName(s); return a; }
        });
        artistBox.setPromptText("Artiste");

        if (existing != null) {
            titleField.setText(DialogUtil.safe(existing.getTitle()));
            titleField.setEditable(false);
            yearField.setText(existing.getCreationYear() == null ? "" : existing.getCreationYear().toString());
            typeField.setText(DialogUtil.safe(existing.getType()));
            mediumField.setText(DialogUtil.safe(existing.getMedium()));
            dimField.setText(DialogUtil.safe(existing.getDimensions()));
            priceField.setText(String.valueOf(existing.getPrice()));
            statusBox.setValue(existing.getStatus() != null ? existing.getStatus().name() : "FOR_SALE");
            descField.setText(DialogUtil.safe(existing.getDescription()));
            if (existing.getArtist() != null) {
                // selectionne par nom
                for (Artist a : artistBox.getItems()) {
                    if (a.getName().equals(existing.getArtist().getName())) {
                        artistBox.setValue(a);
                        break;
                    }
                }
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.setPadding(new Insets(20, 10, 10, 10));
        int r = 0;
        grid.add(new Label("Titre *"), 0, r);   grid.add(titleField, 1, r++);
        grid.add(new Label("Artiste"), 0, r);   grid.add(artistBox, 1, r++);
        grid.add(new Label("Annee"), 0, r);     grid.add(yearField, 1, r++);
        grid.add(new Label("Type"), 0, r);      grid.add(typeField, 1, r++);
        grid.add(new Label("Medium"), 0, r);    grid.add(mediumField, 1, r++);
        grid.add(new Label("Dimensions"), 0, r);grid.add(dimField, 1, r++);
        grid.add(new Label("Prix"), 0, r);      grid.add(priceField, 1, r++);
        grid.add(new Label("Statut"), 0, r);    grid.add(statusBox, 1, r++);
        grid.add(new Label("Description"), 0, r);grid.add(descField, 1, r++);
        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node okBtn = dialog.getDialogPane().lookupButton(okType);
        okBtn.setDisable(titleField.getText().trim().isEmpty());
        titleField.textProperty().addListener((o, ov, nv) -> okBtn.setDisable(nv.trim().isEmpty()));

        dialog.setResultConverter(button -> {
            if (button != okType) return null;
            Artwork a = existing == null ? new Artwork() : existing;
            a.setTitle(titleField.getText().trim());
            try { a.setCreationYear(yearField.getText().isBlank() ? null : Integer.parseInt(yearField.getText().trim())); }
            catch (NumberFormatException nfe) { a.setCreationYear(null); }
            a.setType(DialogUtil.emptyToNull(typeField.getText()));
            a.setMedium(DialogUtil.emptyToNull(mediumField.getText()));
            a.setDimensions(DialogUtil.emptyToNull(dimField.getText()));
            try { a.setPrice(priceField.getText().isBlank() ? 0.0 : Double.parseDouble(priceField.getText().trim())); }
            catch (NumberFormatException nfe) { a.setPrice(0.0); }
            try {
                a.setStatus(statusBox.getValue() != null
                        ? Artwork.Status.valueOf(statusBox.getValue())
                        : Artwork.Status.FOR_SALE);
            } catch (Exception ignored) { /* enum manquant ? */ }
            a.setDescription(DialogUtil.emptyToNull(descField.getText()));
            a.setArtist(artistBox.getValue());
            return a;
        });

        return dialog.showAndWait();
    }
}
