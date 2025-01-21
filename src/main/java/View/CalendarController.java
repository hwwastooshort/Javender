package View;

import Model.Database.DataManager;
import Model.Database.DataManagerException;
import Model.Database.JooqDataManager;
import Model.Entities.Appointment;
import Model.Entities.Tag;

import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        List<Tag> tags = getAddedTagsList(new ArrayList<Tag>()); // TODO Hier muss noch eine Methode im DataManager eingefügt werden, um alle Tags zu bekommen

        appointment = new Appointment(startDateTime, endDateTime, title, description, tags);

        try {
            dM.addAppointment(appointment);
        }catch(DataManagerException e){
            Color c = new Color(15, 15, 15);

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

    public List<Tag> getAddedTagsList(List<Tag> tags) {
        List<Tag> addedTags = new ArrayList<Tag>();
        boolean exit = false;

        while(!exit) {
            Optional<Tag> tag = uI.getTag(tags);

            if(tag.isEmpty()) {
                exit = true;
            } else {
                addedTags.add(tag.get());
            }
        }
        return addedTags;
    }

    //TODO Ask to replace tag if already existing
    public void addTag(){
        uI.startTagCreation();
        String title = uI.getTagTitle();
        String color = uI.getTagColor();
        Tag newTag = new Tag(title,color);
        try{
            dM.addTag(newTag);
        }catch (DataManagerException e){
            uI.printError("There was a problem with adding the created tag to the database in addTag.");
        }

    }
}
