package com.project.artconnect.ui;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.service.CommunityService;
import com.project.artconnect.util.ServiceProvider;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.util.Optional;

/**
 * Controleur de l'onglet "Community" : lecture + CRUD persistante des membres.
 */
public class CommunityController {
    @FXML private TableView<CommunityMember> memberTable;
    @FXML private TableColumn<CommunityMember, String> nameColumn;
    @FXML private TableColumn<CommunityMember, String> emailColumn;
    @FXML private TableColumn<CommunityMember, String> cityColumn;

    private final CommunityService communityService = ServiceProvider.getCommunityService();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        cityColumn.setCellValueFactory(new PropertyValueFactory<>("city"));
        refreshTable();
    }

    private void refreshTable() {
        memberTable.setItems(FXCollections.observableArrayList(communityService.getAllMembers()));
    }

    @FXML
    private void handleAdd() {
        showDialog(null).ifPresent(m -> {
            try {
                communityService.createMember(m);
                refreshTable();
                DialogUtil.showInfo("Membre cree",
                        "Le membre \"" + m.getName() + "\" a ete ajoute.");
            } catch (RuntimeException ex) {
                DialogUtil.showError("Echec de la creation", ex);
            }
        });
    }

    @FXML
    private void handleEdit() {
        CommunityMember selected = memberTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Aucun membre selectionne",
                    "Selectionne d'abord un membre.");
            return;
        }
        showDialog(selected).ifPresent(m -> {
            try {
                communityService.updateMember(m);
                refreshTable();
                DialogUtil.showInfo("Membre mis a jour",
                        "Les modifications ont ete enregistrees.");
            } catch (RuntimeException ex) {
                DialogUtil.showError("Echec de la mise a jour", ex);
            }
        });
    }

    @FXML
    private void handleDelete() {
        CommunityMember selected = memberTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            DialogUtil.showWarning("Aucun membre selectionne",
                    "Selectionne d'abord un membre.");
            return;
        }
        if (DialogUtil.confirm("Confirmation de suppression",
                "Supprimer definitivement \"" + selected.getName() + "\" ?")) {
            try {
                communityService.deleteMember(selected.getName());
                refreshTable();
                DialogUtil.showInfo("Membre supprime", "Suppression effectuee.");
            } catch (RuntimeException ex) {
                DialogUtil.showError("Echec de la suppression", ex);
            }
        }
    }

    private Optional<CommunityMember> showDialog(CommunityMember existing) {
        Dialog<CommunityMember> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nouveau membre" : "Modifier le membre");
        dialog.setHeaderText(existing == null
                ? "Saisir les informations du nouveau membre"
                : "Modifier \"" + existing.getName() + "\"");

        ButtonType okType = new ButtonType(existing == null ? "Creer" : "Enregistrer",
                ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Nom (identifiant)");
        TextField emailField = new TextField();
        emailField.setPromptText("Email (unique)");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Telephone");
        TextField cityField = new TextField();
        cityField.setPromptText("Ville");
        TextField yearField = new TextField();
        yearField.setPromptText("Annee de naissance");
        ComboBox<String> memberTypeBox = new ComboBox<>(FXCollections.observableArrayList(
                "free", "premium"));
        memberTypeBox.setValue("free");

        if (existing != null) {
            nameField.setText(DialogUtil.safe(existing.getName()));
            nameField.setEditable(false);
            emailField.setText(DialogUtil.safe(existing.getEmail()));
            phoneField.setText(DialogUtil.safe(existing.getPhone()));
            cityField.setText(DialogUtil.safe(existing.getCity()));
            yearField.setText(existing.getBirthYear() == null ? "" : existing.getBirthYear().toString());
            memberTypeBox.setValue(existing.getMembershipType() != null
                    ? existing.getMembershipType() : "free");
        }

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        grid.setPadding(new Insets(20, 10, 10, 10));
        int r = 0;
        grid.add(new Label("Nom *"), 0, r);     grid.add(nameField, 1, r++);
        grid.add(new Label("Email"), 0, r);     grid.add(emailField, 1, r++);
        grid.add(new Label("Telephone"), 0, r); grid.add(phoneField, 1, r++);
        grid.add(new Label("Ville"), 0, r);     grid.add(cityField, 1, r++);
        grid.add(new Label("Annee naissance"), 0, r); grid.add(yearField, 1, r++);
        grid.add(new Label("Adhesion"), 0, r);  grid.add(memberTypeBox, 1, r++);
        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node okBtn = dialog.getDialogPane().lookupButton(okType);
        okBtn.setDisable(nameField.getText().trim().isEmpty());
        nameField.textProperty().addListener((o, ov, nv) -> okBtn.setDisable(nv.trim().isEmpty()));

        dialog.setResultConverter(button -> {
            if (button != okType) return null;
            CommunityMember m = existing == null ? new CommunityMember() : existing;
            m.setName(nameField.getText().trim());
            m.setEmail(DialogUtil.emptyToNull(emailField.getText()));
            m.setPhone(DialogUtil.emptyToNull(phoneField.getText()));
            m.setCity(DialogUtil.emptyToNull(cityField.getText()));
            try {
                String y = yearField.getText().trim();
                m.setBirthYear(y.isEmpty() ? null : Integer.parseInt(y));
            } catch (NumberFormatException nfe) {
                m.setBirthYear(null);
            }
            m.setMembershipType(memberTypeBox.getValue());
            return m;
        });

        return dialog.showAndWait();
    }
}
