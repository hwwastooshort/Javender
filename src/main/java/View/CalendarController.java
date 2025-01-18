package View;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class CalendarController {

    UserInterface uI = new CalendarInterface();

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
