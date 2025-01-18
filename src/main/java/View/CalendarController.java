package View;

import Model.Database.DataManager;
import Model.Database.DataManagerException;
import Model.Database.JooqDataManager;
import Model.Entities.Appointment;
import Model.Entities.Tag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class CalendarController {

    private final String PATH_TO_DATABASE = "jdbc:sqlite:src/test/resources/javenderDatabase.db";
    UserInterface uI = new CalendarInterface();
    DataManager dM;

    {
        try {
            dM = new JooqDataManager(PATH_TO_DATABASE);
        } catch (DataManagerException e) {
            throw new RuntimeException("Couldn't connect to database");
        }
    }

    /**
     * gets the data of the appointment that the user wants to add
     * through the user interface and adds it to the database.
     * **/
    public void addAppointment(){
        Appointment appointment;

        uI.startAppointmentCreation();

        String title = uI.getTitle();

        String startDate = uI.getStartDate();
        while(validateDate(startDate)){
            startDate = uI.getStartDate();
        }

        String startTime = uI.getStartTime();
        while(validateTime(startTime)){
            startTime = uI.getStartTime();
        }

        LocalDateTime startDateTime = LocalDateTime.of(LocalDate.parse(startDate), LocalTime.parse(startTime));


        String endDate = uI.getEndDate();
        while(validateDate(endDate)){
            endDate = uI.getEndDate();
        }
        String endTime = uI.getEndTime();
        while(validateTime(endTime)){ //TODO manchmal wird hier trotz richtiger Eingabe die Zeit nicht akzeptiert
            endTime = uI.getEndTime();
        }

        LocalDateTime endDateTime = LocalDateTime.of(LocalDate.parse(endDate), LocalTime.parse(endTime));

        if(validateDateTimeOrder(startDateTime, endDateTime)){
            addAppointment();
            return;
        }

        String description = uI.getDescription();

        List<Tag> tags = uI.getTags();

        appointment = new Appointment(startDateTime, endDateTime, title, description, tags);

        try {
            dM.addAppointment(appointment);
        }catch(DataManagerException e){
            uI.printError("There was a problem with adding the created appointment to the database in addAppointment.");
            e.printStackTrace();
        }

    }
    public boolean validateDate(String dateString){
        try {
            LocalDate.parse(dateString);
            return false;
        }catch (DateTimeParseException e){
            uI.printError("Your input is not formatted correctly.");
            return true;
        }
    }

    public boolean validateTime(String timeString){
        try {
            LocalTime.parse(timeString);
            return false;
        }catch (DateTimeParseException e){
            uI.printError("Your input is not formatted correctly.");
            return true;
        }
    }

    public boolean validateDateTimeOrder(LocalDateTime start, LocalDateTime end){
        if(start.isAfter(end)){
            uI.printError("Your appointment can not end before it starts.");
        }
        return start.isAfter(end);
    }
}
