package View;

public class ManageMenuView {
    private final UserInterface ui;

    public ManageMenuView(UserInterface ui) {
        this.ui = ui;
    }

    public void displayManageMenu() {
        ui.displayMessage("\n=== Manage Menu ===");
        ui.displayMessage("1. Add appointment");
        ui.displayMessage("2. Edit appointment");
        ui.displayMessage("3. Remove appointment");
        ui.displayMessage("4. Add tag");
        ui.displayMessage("5. Edit tag");
        ui.displayMessage("6. Remove Tag");
        ui.displayMessage("7. Exit");
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
