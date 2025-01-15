package View;

import Model.Entities.Tag;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

    LocalDate getStartDate();

    LocalDate getEndDate();

    LocalTime getStartTime();

    LocalTime getEndTime();

    String getDescription();

    List<Tag> getTags();

    void printError(String prompt);
}
