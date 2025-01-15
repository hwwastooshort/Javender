package View;

import java.time.LocalDate;
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

    public boolean validateDateOrder(LocalDate startDate, LocalDate endDate){
        if(startDate.isAfter(endDate)){
            uI.printError("The start date can't be after the end date.");
        }
        return startDate.isAfter(endDate);
    }

    public boolean validateTimeOrder(LocalTime startTime, LocalTime endTime){
        if(startTime.isAfter(endTime)){
            uI.printError("The start time can't be after the end time.");
        }
        return startTime.isAfter(endTime);
    }
}
