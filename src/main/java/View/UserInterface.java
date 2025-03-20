package View;

import Model.Entities.Appointment;
import Model.Entities.Tag;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserInterface {
    String getCalendar(LocalDate date, List<Appointment> appointmentList, int monthAmount);

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

    String startEditingAppointment();

    int chooseAppointment(List<Appointment> appointments);

    int appointmentEditMenu();

    String startEditingTag();

    void tagEditMenu();

    void displayError(String prompt);

    int tagAlreadyExists(Tag existingTag);

    void successfullyOverwriteTag(Tag newTag);

    void cancelOverwriteTag();

    String startDeletingAppointment();

    String startDeletingTag();

    int getIntegerInput();

    void displayMessage(String s);

    void displayCommandList();

    String getUserCommand();

    void displayAppointments(List<Appointment> appointments);

    boolean confirmAction(String s);
}
