package View;

import Model.Entities.Appointment;
import Model.Entities.Tag;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserInterface {

    /**
     * Method to display the days of a month
     *
     * @param date any day in the month that is supposed to be displayed
     * @return a string of the days in a month divided by weeks
     */
    public String getCalendar(LocalDate date, List<Appointment> appointmentList, int monthAmount);
    String getCalendarWithUpcomingAppointments(LocalDate date, List<Appointment> appointmentList, int monthAmount);

    void startAppointmentCreation();

    String getTitle();

    String getStartDate();

    String getEndDate();

    String getStartTime();

    String getEndTime();

    String getDescription();

    Optional<Tag> getTag(List<Tag> allTags, List<Tag> appliedTags);

    void startTagCreation();

    String getTagTitle();

    int getTagColorIndex();

    /**
     * asks the user to enter the title of the appointment they want to edit
     * @return name of the appointment
     * **/
    String startEditingAppointment();

    int chooseAppointment(List<Appointment> appointments);

    int appointmentEditMenu();

    /**
     * asks the user to enter the title of the tag they want to edit
     * @return name of the tag
     * **/
    String startEditingTag();

    void tagEditMenu();

    void displayError(String prompt);
    String formatPrompt(String prompt);
    int tagAlreadyExists(Tag existingTag);
    void successfullyOverwriteTag(Tag newTag);
    void cancelOverwriteTag();
    /**
     * asks the user to enter the title of the appointment they want to delete
     * @return title of the appointment
     * **/
    String startDeletingAppointment();

    /**
     * asks the user to enter the name of the tag they want to delete
     * @return name of the tag
     * **/
    String startDeletingTag();

    int getIntegerInput();

    void displayMessage(String s);

    void displayCommandList();

    String getUserCommand();

    void displayAppointments(List<Appointment> appointments);

    boolean confirmAction(String s);
}
