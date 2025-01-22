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

        LocalDateTime startDateTime = getStartDateTime();

        LocalDateTime endDateTime = getEndDateTime();

        if(validateDateTimeOrder(startDateTime, endDateTime)){
            addAppointment();
            return;
        }

        String description = uI.getDescription();
        try {
            List<Tag> tags = getAddedTagsList(dM.getAllTags());
            appointment = new Appointment(startDateTime, endDateTime, title, description, tags);
            dM.addAppointment(appointment);

        }catch(DataManagerException e){
            uI.displayError("There was a problem with adding the created appointment to the database in addAppointment.");
            e.printStackTrace();
        }

    }

    private LocalDateTime getStartDateTime(){
        String startDate = uI.getStartDate();
        while(validateDate(startDate)){
            startDate = uI.getStartDate();
        }

        String startTime = uI.getStartTime();
        while(validateTime(startTime)){
            startTime = uI.getStartTime();
        }

        return LocalDateTime.of(LocalDate.parse(startDate), LocalTime.parse(startTime));
    }

    private LocalDateTime getEndDateTime(){
        String endDate = uI.getEndDate();
        while(validateDate(endDate)){
            endDate = uI.getEndDate();
        }
        String endTime = uI.getEndTime();
        while(validateTime(endTime)){ //TODO manchmal wird hier trotz richtiger Eingabe die Zeit nicht akzeptiert
            endTime = uI.getEndTime();
        }

        return LocalDateTime.of(LocalDate.parse(endDate), LocalTime.parse(endTime));
    }

    public boolean validateDate(String dateString){
        try {
            LocalDate.parse(dateString);
            return false;
        }catch (DateTimeParseException e){
            uI.displayError("Your input is not formatted correctly.");
            return true;
        }
    }

    public boolean validateTime(String timeString){
        try {
            LocalTime.parse(timeString);
            return false;
        }catch (DateTimeParseException e){
            uI.displayError("Your input is not formatted correctly.");
            return true;
        }
    }

    public boolean validateDateTimeOrder(LocalDateTime start, LocalDateTime end){
        if(start.isAfter(end)){
            uI.displayError("Your appointment can not end before it starts.");
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
        try{
            Optional<Tag> optionalTag = dM.getTagByTitle(title);
            if(optionalTag.isEmpty()){
                String color = uI.getTagColor();
                Tag newTag = new Tag(title,color);
                dM.addTag(newTag);
                return;
            }
            Tag tag = optionalTag.get();
            int choice = uI.tagAlreadyExists(optionalTag.get());
            if(choice == 1){
                String color = uI.getTagColor();
                Tag overwritingTag = new Tag(tag.getTagId(), title, color);
                dM.updateTag(overwritingTag);
                uI.successfullyOverwriteTag(overwritingTag);
                return;
            }
            uI.cancleOverwriteTag();
        }catch (DataManagerException e){
            uI.displayError(e.getMessage());
        }
    }

    public void editTag(){
        String title = uI.startTagEditing();
        try {
            Optional<Tag> optionalTag = dM.getTagByTitle(title);
            if(optionalTag.isEmpty()){
                uI.displayError("There was no tag with the title \"" + title + "\".");
                return;
            }
            Tag tag = optionalTag.get();
            uI.tagEditingMenu();
            tag.setName(uI.getTagTitle());
            tag.setColor(uI.getTagColor());
            //dM.saveTag(tag);
        }catch (DataManagerException e){
            e.printStackTrace();
        }

    }

    public void editAppointment() {

        String appointmentTitle = uI.startEditingAppointment();
        try {
            List<Appointment> appointments = dM.getAppointmentsByTitle(appointmentTitle);

            int appointmentIndex = 0;

            if (appointments.isEmpty()) {
                uI.displayError("There are no appointments with the name \"" + appointmentTitle + "\"");
                return;
            }

            if (appointments.size() > 1) {
                appointmentIndex = uI.chooseAppointment(appointments);
                while (appointmentIndex >= appointments.size()) {
                    uI.displayError("Invalid input.");
                    appointmentIndex = uI.chooseAppointment(appointments);
                }
            }
            Appointment newAppointment = createNewAppointment(appointments.get(appointmentIndex));
            dM.updateAppointment(newAppointment);
        }catch (DataManagerException e){
            uI.displayError(e.getMessage());
        }
    }

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
                    }catch(DataManagerException e){
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
}
