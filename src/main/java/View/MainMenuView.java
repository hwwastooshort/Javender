package View;

import Controller.CalendarController;

import java.time.LocalDate;

public class MainMenuView {
    private final UserInterface ui;

    public MainMenuView(UserInterface ui) {
        this.ui = ui;
    }

    public void displayMainMenu() {
        ui.displayMessage("\n=== Main Menu ===");
        ui.displayMessage("1. Add appointment");
        ui.displayMessage("2. Edit appointment");
        ui.displayMessage("3. Remove appointment");
        ui.displayMessage("4. Add tag");
        ui.displayMessage("5. Edit tag");
        ui.displayMessage("6. Exit");
    }

    public int getUserChoice() {
        ui.displayMessage("Enter your choice: ");
        return ui.getIntegerInput();
    }

    public void displayExitMessage() {
        ui.displayMessage("Closing the application.");
    }

    public void displayInvalidChoiceMessage() {
        ui.displayMessage("Invalid choice. Try again.");
    }
}
