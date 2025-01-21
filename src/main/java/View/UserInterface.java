package View;

import Model.Entities.Appointment;
import Model.Entities.Tag;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserInterface {

    /**
     * Method to display the days of a month
     *
     * @param date any day in the month that is supposed to be displayed
     * @return a string of the days in a month divided by weeks
     */
    String getMonth(LocalDate date);

    void startAppointmentCreation();

    String getTitle();

    String getStartDate();

    String getEndDate();

    String getStartTime();

    String getEndTime();

    String getDescription();

    Optional<Tag> getTag(List<Tag> tags);

    void startTagCreation();

    String getTagTitle();

    String getTagColor();

    String startEditingAppointment();

    int chooseAppointment(List<Appointment> appointments);

    int appointmentEditMenu();


    void displayError(String prompt);
}
