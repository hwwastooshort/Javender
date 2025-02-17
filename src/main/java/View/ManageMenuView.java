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
        ui.displayMessage("4. Remove all appointments");
        ui.displayMessage("5. Add tag");
        ui.displayMessage("6. Edit tag");
        ui.displayMessage("7. Remove Tag");
        ui.displayMessage("8. Remove all tags");
        ui.displayMessage("9. Exit");
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
