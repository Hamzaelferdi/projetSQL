package com.project.artconnect.ui;

import com.project.artconnect.model.Artist;
import com.project.artconnect.model.Discipline;
import com.project.artconnect.service.ArtistService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controleur de l'onglet "Artists".
 *
 * <p>Gere :</p>
 * <ul>
 *   <li>l'affichage de la liste des artistes (lecture depuis la BD via le service),</li>
 *   <li>la recherche / filtre par discipline,</li>
 *   <li>les operations CRUD (ajout, modification, suppression) persistantes.</li>
 * </ul>
 */
public class ArtistController {
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<Discipline> disciplineFilter;
    @FXML
    private TableView<Artist> artistTable;
    @FXML
    private TableColumn<Artist, String> nameColumn;
    @FXML
    private TableColumn<Artist, String> cityColumn;
    @FXML
    private TableColumn<Artist, String> emailColumn;
    @FXML
    private TableColumn<Artist, Integer> yearColumn;

    private final ArtistService artistService = ServiceProvider.getArtistService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("birthYear"));

        // Affichage propre des Discipline dans le ComboBox
        disciplineFilter.setConverter(new StringConverter<>() {
            @Override public String toString(Discipline d) { return d == null ? "" : d.getName(); }
            @Override public Discipline fromString(String s) { return s == null ? null : new Discipline(s); }
        });
        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
        refreshTable();
    }

    // ---------------------------------------------------------------------
    // Recherche / filtre
    // ---------------------------------------------------------------------

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        Discipline d = disciplineFilter.getValue();
        String dName = (d != null) ? d.getName() : null;
        artistTable.setItems(FXCollections.observableArrayList(
                artistService.searchArtists(query, dName, null)));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        disciplineFilter.setValue(null);
        refreshTable();
    }

    private void refreshTable() {
        artistTable.setItems(FXCollections.observableArrayList(artistService.getAllArtists()));
    }

    // ---------------------------------------------------------------------
    // CRUD : Ajout / Modification / Suppression (persistants en base)
    // ---------------------------------------------------------------------

    @FXML
    private void handleAdd() {
        Optional<Artist> result = showArtistDialog(null);
        result.ifPresent(artist -> {
            try {
                artistService.createArtist(artist);
                refreshTable();
                refreshDisciplineFilter();
                showInfo("Artiste cree", "L'artiste \"" + artist.getName() + "\" a ete ajoute.");
            } catch (RuntimeException ex) {
                showError("Echec de la creation", ex);
            }
        });
    }

    @FXML
    private void handleEdit() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucun artiste selectionne",
                    "Selectionne d'abord un artiste dans la table.");
            return;
        }
        Optional<Artist> result = showArtistDialog(selected);
        result.ifPresent(artist -> {
            try {
                artistService.updateArtist(artist);
                refreshTable();
                refreshDisciplineFilter();
                showInfo("Artiste mis a jour", "Les modifications ont ete enregistrees.");
            } catch (RuntimeException ex) {
                showError("Echec de la mise a jour", ex);
            }
        });
    }

    @FXML
    private void handleDelete() {
        Artist selected = artistTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucun artiste selectionne",
                    "Selectionne d'abord un artiste dans la table.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer definitivement l'artiste \"" + selected.getName() + "\" ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmation de suppression");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    artistService.deleteArtist(selected.getName());
                    refreshTable();
                    showInfo("Artiste supprime",
                            "L'artiste \"" + selected.getName() + "\" a ete supprime.");
                } catch (RuntimeException ex) {
                    showError("Echec de la suppression", ex);
                }
            }
        });
    }

    private void refreshDisciplineFilter() {
        Discipline current = disciplineFilter.getValue();
        disciplineFilter.setItems(FXCollections.observableArrayList(artistService.getAllDisciplines()));
        disciplineFilter.setValue(current);
    }

    // ---------------------------------------------------------------------
    // Dialog de saisie / edition d'un Artist
    // ---------------------------------------------------------------------

    /**
     * Ouvre un Dialog modal pour saisir les champs d'un artiste.
     *
     * @param existing si non null, le dialog est pre-rempli pour modification
     *                 (le nom devient en lecture seule car il sert d'identifiant).
     * @return Optional contenant l'artiste construit, vide si annule.
     */
    private Optional<Artist> showArtistDialog(Artist existing) {
        Dialog<Artist> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouvel artiste" : "Modifier l'artiste");
        dialog.setHeaderText(existing == null
                ? "Saisir les informations du nouvel artiste"
                : "Modifier les informations de \"" + existing.getName() + "\"");

        ButtonType okType = new ButtonType(existing == null ? "Creer" : "Enregistrer",
                ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Nom (identifiant)");
        TextField cityField = new TextField();
        cityField.setPromptText("Ville");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Telephone");
        TextField yearField = new TextField();
        yearField.setPromptText("Annee de naissance");
        TextField websiteField = new TextField();
        websiteField.setPromptText("Site web");
        TextField socialField = new TextField();
        socialField.setPromptText("Reseau social");
        TextArea bioField = new TextArea();
        bioField.setPromptText("Biographie");
        bioField.setPrefRowCount(3);
        TextField disciplinesField = new TextField();
        disciplinesField.setPromptText("Disciplines (separees par des virgules)");
        CheckBox activeBox = new CheckBox("Actif");
        activeBox.setSelected(true);

        if (existing != null) {
            nameField.setText(safe(existing.getName()));
            nameField.setEditable(false); // le nom est l'identifiant
            cityField.setText(safe(existing.getCity()));
            emailField.setText(safe(existing.getContactEmail()));
            phoneField.setText(safe(existing.getPhone()));
            yearField.setText(existing.getBirthYear() == null ? "" : existing.getBirthYear().toString());
            websiteField.setText(safe(existing.getWebsite()));
            socialField.setText(safe(existing.getSocialMedia()));
            bioField.setText(safe(existing.getBio()));
            activeBox.setSelected(existing.isActive());
            if (existing.getDisciplines() != null) {
                disciplinesField.setText(existing.getDisciplines().stream()
                        .map(Discipline::getName)
                        .collect(Collectors.joining(", ")));
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(20, 10, 10, 10));
        int row = 0;
        grid.add(new Label("Nom *"), 0, row);          grid.add(nameField, 1, row++);
        grid.add(new Label("Ville"), 0, row);          grid.add(cityField, 1, row++);
        grid.add(new Label("Email"), 0, row);          grid.add(emailField, 1, row++);
        grid.add(new Label("Telephone"), 0, row);      grid.add(phoneField, 1, row++);
        grid.add(new Label("Annee naissance"), 0, row);grid.add(yearField, 1, row++);
        grid.add(new Label("Site web"), 0, row);       grid.add(websiteField, 1, row++);
        grid.add(new Label("Reseau social"), 0, row);  grid.add(socialField, 1, row++);
        grid.add(new Label("Disciplines"), 0, row);    grid.add(disciplinesField, 1, row++);
        grid.add(new Label("Bio"), 0, row);            grid.add(bioField, 1, row++);
        grid.add(activeBox, 1, row);

        dialog.getDialogPane().setContent(grid);

        // Desactive OK tant que le nom est vide
        javafx.scene.Node okButton = dialog.getDialogPane().lookupButton(okType);
        okButton.setDisable(nameField.getText().trim().isEmpty());
        nameField.textProperty().addListener((obs, oldV, newV) ->
                okButton.setDisable(newV.trim().isEmpty()));

        dialog.setResultConverter(button -> {
            if (button != okType) return null;
            Artist a = existing == null ? new Artist() : existing;
            a.setName(nameField.getText().trim());
            a.setCity(emptyToNull(cityField.getText()));
            a.setContactEmail(emptyToNull(emailField.getText()));
            a.setPhone(emptyToNull(phoneField.getText()));
            a.setWebsite(emptyToNull(websiteField.getText()));
            a.setSocialMedia(emptyToNull(socialField.getText()));
            a.setBio(emptyToNull(bioField.getText()));
            a.setActive(activeBox.isSelected());
            try {
                String y = yearField.getText().trim();
                a.setBirthYear(y.isEmpty() ? null : Integer.parseInt(y));
            } catch (NumberFormatException nfe) {
                a.setBirthYear(null);
            }
            String raw = disciplinesField.getText();
            if (raw != null && !raw.isBlank()) {
                List<Discipline> ds = Arrays.stream(raw.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Discipline::new)
                        .collect(Collectors.toList());
                a.setDisciplines(ds);
            } else {
                a.setDisciplines(new java.util.ArrayList<>());
            }
            return a;
        });

        return dialog.showAndWait();
    }

    // ---------------------------------------------------------------------
    // Utilitaires
    // ---------------------------------------------------------------------

    private static String safe(String s) { return s == null ? "" : s; }
    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void showInfo(String header, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        a.setHeaderText(header);
        a.showAndWait();
    }

    private void showWarning(String header, String message) {
        Alert a = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        a.setHeaderText(header);
        a.showAndWait();
    }

    private void showError(String header, Throwable t) {
        Throwable cause = t.getCause() == null ? t : t.getCause();
        Alert a = new Alert(Alert.AlertType.ERROR,
                cause.getMessage() == null ? t.toString() : cause.getMessage(),
                ButtonType.OK);
        a.setHeaderText(header);
        a.showAndWait();
    }
}
