package com.project.artconnect.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Petites helpers pour afficher des alertes JavaFX de maniere uniforme.
 */
final class DialogUtil {

    private DialogUtil() {}

    static void showInfo(String header, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        a.setHeaderText(header);
        a.showAndWait();
    }

    static void showWarning(String header, String message) {
        Alert a = new Alert(Alert.AlertType.WARNING, message, ButtonType.OK);
        a.setHeaderText(header);
        a.showAndWait();
    }

    static void showError(String header, Throwable t) {
        Throwable cause = t.getCause() == null ? t : t.getCause();
        Alert a = new Alert(Alert.AlertType.ERROR,
                cause.getMessage() == null ? t.toString() : cause.getMessage(),
                ButtonType.OK);
        a.setHeaderText(header);
        a.showAndWait();
    }

    static boolean confirm(String header, String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, message,
                ButtonType.YES, ButtonType.NO);
        a.setHeaderText(header);
        return a.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    static String safe(String s) { return s == null ? "" : s; }
}
