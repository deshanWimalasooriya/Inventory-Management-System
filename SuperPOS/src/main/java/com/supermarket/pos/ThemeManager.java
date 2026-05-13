package com.supermarket.pos;

import javafx.scene.Scene;
import java.util.Objects;

public class ThemeManager {
    public static final String DARK_MODE = "Dark Mode";
    public static final String LIGHT_MODE = "Light Mode";

    // Default to Dark Mode
    public static String currentTheme = DARK_MODE;

    // This method is called to instantly flip the scene's colors
    public static void applyTheme(Scene scene) {
        if (scene == null) return;

        try {
            String lightThemePath = Objects.requireNonNull(ThemeManager.class.getResource("/css/light-theme.css")).toExternalForm();

            if (currentTheme.equals(LIGHT_MODE)) {
                // If light mode is selected, add the light-theme CSS to override the global CSS
                if (!scene.getStylesheets().contains(lightThemePath)) {
                    scene.getStylesheets().add(lightThemePath);
                }
            } else {
                // If dark mode is selected, remove the light-theme CSS entirely
                scene.getStylesheets().remove(lightThemePath);
            }
        } catch (NullPointerException e) {
            System.err.println("Could not find light-theme.css! Ensure it is in the resources/css folder.");
        }
    }
}