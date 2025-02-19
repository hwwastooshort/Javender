package Controller;

import Model.Database.DataManager;
import Model.Database.DataManagerException;
import Model.Database.JooqDataManager;
import Model.Entities.Appointment;
import Model.Entities.Tag;
import View.CalendarInterface;
import View.ManageMenuView;
import View.UserInterface;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.TemporalAdjusters.firstDayOfYear;

public class CalendarController {

    UserInterface uI = new CalendarInterface();
    ManageMenuView manageMenuView = new ManageMenuView(uI);
    DataManager dM;

    public CalendarController(DataManager dataManager) {
        this.dM = dataManager;
    }

    public void shutdown() {
        if (dM instanceof JooqDataManager) {
            ((JooqDataManager) dM).close();
        }
    }

    public void mainMenu() {
        boolean running = true;
        LocalDate monthToShow = LocalDate.now();
        while (running) {
            CalendarInterface.clearScreen();
            showMonthsAccordingToDate(monthToShow);
            uI.displayMessage("Enter \"help\" to see all available commands.");
            String[] arguments = splitUserCommandIntoArgs(uI.getUserCommand().toLowerCase());

            switch (arguments[0]) {
                case "help":
                    uI.displayCommandList();
                    break;
                case "manage":
                    manageMenu();
                    break;
                case "january":
                case "february":
                case "march":
                case "april":
                case "may":
                case "june":
                case "july":
                case "august":
                case "september":
                case "october":
                case "november":
                case "december":
                    int year = Year.now().getValue();
                    if (arguments.length > 1) {
                        try {
                            year = Year.parse(arguments[1]).getValue();
                        } catch (DateTimeParseException e) {
                            uI.displayError("The year you entered was not formatted correctly.");
                        }
                    }
                    int month = Month.valueOf(arguments[0].toUpperCase()).getValue();
                    monthToShow = LocalDate.of(year, month, 1);
                    break;
                case "now":
                    monthToShow = LocalDate.now();
                    break;
                case "upcoming":
                    int appointmentAmount = 5;
                    if(arguments.length > 1){
                        try {
                            appointmentAmount = Integer.parseInt(arguments[1]);
                        }catch(NumberFormatException e){
                            uI.displayError("The second argument has to be a number.");
                        }
                    }
                    try {
                        List<Appointment> upcomingAppointments = dM.getUpcomingAppointments(monthToShow.atStartOfDay(), appointmentAmount);
                        uI.displayAppointments(upcomingAppointments);
                    } catch (DataManagerException e) {
                        uI.displayError("There was a problem fetching the upcoming appointments.");
                    }
                    break;
                case "exit":
                    manageMenuView.displayExitMessage();
                    shutdown();
                    running = false;
                    break;
                default:
                    manageMenuView.displayInvalidChoiceMessage();
            }
        }
    }

    /**
     * gets the data of the appointment that the user wants to add
     * through the user interface and adds it to the database.
     **/
    public void addAppointment() {
        Appointment appointment;

        uI.startAppointmentCreation();

        String title = uI.getTitle();

        LocalDateTime startDateTime = getStartDateTime();

        LocalDateTime endDateTime = getEndDateTime();

        if (validateDateTimeOrder(startDateTime, endDateTime)) {
            addAppointment();
            return;
        }

        String description = uI.getDescription();
        try {
            List<Tag> tags = getAddedTagsList(dM.getAllTags());
            appointment = new Appointment(startDateTime, endDateTime, title, description, tags);
            dM.addAppointment(appointment);

        } catch (DataManagerException e) {
            uI.displayError("There was a problem with adding the created appointment to the database in addAppointment.");
        }

    }

    /**
     * gets and validates a StartDateTime
     **/
    private LocalDateTime getStartDateTime() {
        String startDate = uI.getStartDate();
        while (validateDate(startDate)) {
            startDate = uI.getStartDate();
        }

        String startTime = uI.getStartTime();
        while (validateTime(startTime)) {
            startTime = uI.getStartTime();
        }

        return LocalDateTime.of(LocalDate.parse(startDate), LocalTime.parse(startTime));
    }

    /**
     * gets and validates EndDateTime
     **/
    private LocalDateTime getEndDateTime() {
        String endDate = uI.getEndDate();
        while (validateDate(endDate)) {
            endDate = uI.getEndDate();
        }
        String endTime = uI.getEndTime();
        while (validateTime(endTime)) {
            endTime = uI.getEndTime();
        }

        return LocalDateTime.of(LocalDate.parse(endDate), LocalTime.parse(endTime));
    }

    /**
     * checks if the entered String is formatted correctly to be parsed to
     * a LocalDate object
     **/
    public boolean validateDate(String dateString) {
        try {
            LocalDate.parse(dateString);
            return false;
        } catch (DateTimeParseException e) {
            uI.displayError("Your input is not formatted correctly.");
            return true;
        }
    }

    /**
     * checks of the entered String is formatted correctly to be parsed to
     * a LocalTime object
     **/
    public boolean validateTime(String timeString) {
        try {
            LocalTime.parse(timeString);
            return false;
        } catch (DateTimeParseException e) {
            uI.displayError("Your input is not formatted correctly.");
            return true;
        }
    }

    /**
     * checks if the startTime is chronologically after the endTime
     **/
    public boolean validateDateTimeOrder(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            uI.displayError("Your appointment can not end before it starts.");
        }
        return start.isAfter(end);
    }

    public List<Tag> getAddedTagsList(List<Tag> tags) {
        List<Tag> addedTags = new ArrayList<>();
        boolean exit = false;

        while (!exit) {
            Optional<Tag> tag = uI.getTag(tags);

            if (tag.isEmpty()) {
                exit = true;
            } else {
                addedTags.add(tag.get());
            }
        }
        return addedTags;
    }

    //TODO Ask to replace tag if already existing
    public void addTag() {
        uI.startTagCreation();
        String title = uI.getTagTitle();
        try {
            Optional<Tag> optionalTag = dM.getTagByName(title);
            if (optionalTag.isEmpty()) {
                int colorIndex = uI.getTagColorIndex();
                String color = intToColor(colorIndex);
                Tag newTag = new Tag(title, color);
                dM.addTag(newTag);
                return;
            }
            Tag tag = optionalTag.get();
            int choice = uI.tagAlreadyExists(optionalTag.get());
            if (choice == 1) {
                int colorIndex = uI.getTagColorIndex();
                String color = intToColor(colorIndex);
                Tag overwritingTag = new Tag(tag.getTagId(), title, color);
                dM.updateTag(overwritingTag);
                uI.successfullyOverwriteTag(overwritingTag);
                return;
            }
            uI.cancelOverwriteTag();
        } catch (DataManagerException e) {
            uI.displayError(e.getMessage());
        }
    }

    /**
     * @param colorIndex index of the color the user chose
     * @return Color corresponding to the colorIndex
     **/
    private String intToColor(int colorIndex) {
        String color;
        switch (colorIndex) {
            case 1:
                color = "red";
                break;
            case 2:
                color = "green";
                break;
            case 3:
                color = "yellow";
                break;
            case 4:
                color = "blue";
                break;
            case 5:
                color = "purple";
                break;
            case 6:
                color = "cyan";
                break;
            default:
                color = "white";
                break;
        }
        return color;
    }

    /**
     * logic to edit the tags assigned to an appointment the is chosen by the user
     **/
    public void editTag() {
        String title = uI.startEditingTag();
        try {
            Optional<Tag> optionalTag = dM.getTagByName(title);
            if (optionalTag.isEmpty()) {
                uI.displayError("There was no tag with the title \"" + title + "\".");
                return;
            }
            Tag tag = optionalTag.get();
            uI.tagEditMenu();
            tag.setName(uI.getTagTitle());
            tag.setColor(intToColor(uI.getTagColorIndex()));
            dM.updateTag(tag);
        } catch (DataManagerException e) {
            uI.displayError(e.getMessage());
        }

    }

    /**
     * edit an appointment that the user chooses via the ui
     **/
    public void editAppointment() {
        String appointmentTitle = uI.startEditingAppointment();
        try {
            List<Appointment> appointments = dM.getAppointmentsByTitle(appointmentTitle);

            int appointmentIndex = chooseAppointmentLogic(appointments);

            if (appointmentIndex >= 0) {
                Appointment updatedAppointment = createNewAppointment(appointments.get(appointmentIndex));

                dM.updateAppointment(updatedAppointment);
            }
        } catch (DataManagerException e) {
            uI.displayError(e.getMessage());
        }
    }

    /**
     * @param appointments All appointments the user gets to chose from
     * @return Index of the appointment chosen by the user,
     * returns -1 if appointments is empty
     **/
    private int chooseAppointmentLogic(List<Appointment> appointments) {
        int appointmentIndex = 0;

        if (appointments.isEmpty()) {
            uI.displayError("There are no appointments with the name you entered.");
            return -1;
        }

        if (appointments.size() > 1) {
            appointmentIndex = uI.chooseAppointment(appointments);
            while (appointmentIndex >= appointments.size() || appointmentIndex < 0) {
                uI.displayError("Invalid input.");
                appointmentIndex = uI.chooseAppointment(appointments);
            }
        }
        return appointmentIndex;
    }

    /**
     * create new appointment object based on user inputs
     *
     * @param appointment appointment that the user wants to edit
     * @return Appointment that contains the new information entered by the user
     * variables that the user does not update contain the data of the parameter
     **/
    private Appointment createNewAppointment(Appointment appointment) {

        int input = 0;

        while (input != 5) {
            input = uI.appointmentEditMenu();

            switch (input) {
                case 1:
                    appointment.setTitle(uI.getTitle());
                    break;
                case 2:
                    LocalDateTime newStartDateTime = getStartDateTime();
                    LocalDateTime newEndDateTime = getEndDateTime();
                    if (validateDateTimeOrder(newStartDateTime, newEndDateTime)) {
                        uI.displayError("Your appointment can not start before it ends.");
                        break;
                    }
                    appointment.setStartDate(newStartDateTime);
                    appointment.setEndDate(newEndDateTime);
                    break;

                case 3:
                    appointment.setDescription(uI.getDescription());
                    break;

                case 4:
                    List<Tag> newTags = new ArrayList<>();
                    try {
                        newTags = getAddedTagsList(dM.getAllTags());
                    } catch (DataManagerException e) {
                        uI.displayError(e.getMessage());
                    }
                    appointment.setTags(newTags);
                    break;
                case 5:
                    break;
                default:
                    uI.displayError("Invalid input");
                    break;
            }
        }
        return appointment;
    }

    public void deleteAppointment() {
        String title = uI.startDeletingAppointment();
        try {
            List<Appointment> appointments = dM.getAppointmentsByTitle(title);
            int appointmentIndex = chooseAppointmentLogic(appointments);
            if (appointmentIndex >= 0) {
                Appointment appointmentToBeRemoved = appointments.get(appointmentIndex);
                dM.removeAppointment(appointmentToBeRemoved);
            }
        } catch (DataManagerException e) {
            uI.displayError("There was a problem with removing the appointment.");
            uI.displayError(e.getMessage());
        }
    }

    public void deleteAllAppointments() {
        boolean confirm = uI.confirmAction("Are you sure you want to delete all appointments? This action cannot be undone.");

        if (!confirm) {
            uI.displayMessage("Task canceled. No appointments were deleted.");
            return;
        }

        try {
            dM.removeAllAppointments();
            uI.displayMessage("All appointments have been successfully deleted.");
        } catch (DataManagerException e) {
            uI.displayError("There was a problem deleting all appointments.");
            uI.displayError("Details: " + e.getMessage());
        }
    }

    public void deleteAllTags() {
        boolean confirm = uI.confirmAction("Are you sure you want to delete all tags? This action cannot be undone.");

        if (!confirm) {
            uI.displayMessage("Task canceled. No tags were deleted.");
            return;
        }

        try {
            dM.removeAllTags();
            uI.displayMessage("All tags have been successfully deleted.");
        } catch (DataManagerException e) {
            uI.displayError("There was a problem deleting all tags.");
            uI.displayError("Details: " + e.getMessage());
        }
    }

    public void deleteTag() {
        String name = uI.startDeletingTag();
        try {
            Optional<Tag> optionalTag = dM.getTagByName(name);
            if (optionalTag.isEmpty()) {
                uI.displayError("There was no tag with the name \"" + name + "\"");
                return;
            }
            Tag tag = optionalTag.get();
            dM.removeTag(tag);
        } catch (DataManagerException e) {
            uI.displayError(e.getMessage());
        }
    }

    private void manageMenu(){
        manageMenuView.displayManageMenu();
        int choice = manageMenuView.getUserChoice();
        switch (choice){
            case 1:
                addAppointment();
                break;
            case 2:
                editAppointment();
                break;
            case 3:
                deleteAppointment();
                break;
            case 4:
                deleteAllAppointments();
                break;
            case 5:
                addTag();
                break;
            case 6:
                editTag();
                break;
            case 7:
                deleteTag();
                break;
            case 8:
                deleteAllTags();
                break;
            case 9:
                uI.displayMessage("Leaving manage menu...");
                break;
            default:
                manageMenuView.displayInvalidChoiceMessage();
                manageMenu();
                break;
        }
    }

    /**
     * splits the arguments of the user command into separate Strings
     **/
    public String[] splitUserCommandIntoArgs(String userCommand) {
        return userCommand.split(" ");
    }

    private List<Appointment> getAppointmentsAccordingToMonth(LocalDate date) throws DataManagerException {
        return dM.getAppointmentsByRange(
                date.withDayOfMonth(1).atStartOfDay(),
                date.plusMonths(1).withDayOfMonth(date.plusMonths(1).lengthOfMonth()).atTime(23, 59, 59)
        );
    }

    private void showMonthsAccordingToDate(LocalDate date) {
        try {
            System.out.println(
                    uI.getCalendarWithUpcomingAppointments(
                            date,
                            getAppointmentsAccordingToMonth(date),
                            2
                    ));
        } catch (DataManagerException e) {
            throw new RuntimeException(e);
        }
    }
}
