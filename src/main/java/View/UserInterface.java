package View;

import Model.Entities.Tag;

import java.time.LocalDate;
import java.time.LocalTime;
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

    String formatPrompt(String prompt);
    String getMonthWithText(LocalDate date, String prompt);
    void printError(String prompt);
}
