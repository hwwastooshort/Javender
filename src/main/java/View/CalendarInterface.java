package View;

import Model.Entities.Appointment;
import Model.Entities.Tag;
import java.time.*;
import java.util.*;


public class CalendarInterface implements UserInterface{

    private Scanner scanner = new Scanner(System.in);
    private final int COMMENT_LENGTH = 20;
    private final int SPACING = 10;

    /**
     * Generates a formatted calendar for the specified month and year.
     *
     * @param date A LocalDate object representing the date, used to determine the month and year.
     * @return A formatted string that visually represents the calendar for the given month,
     *         including the month/year header, days of the week, and the days of the month.
     */
    public String getMonth(LocalDate date) {
        StringBuilder monthString = new StringBuilder();

        String days = "MO TU WE TH FR SA SU";

        String dateHeader = date.getMonth().toString() + "\t" + date.getYear();
        int dateHeaderMargin = dateHeader.length() + (days.length() - dateHeader.length()) / 2;

        String dateHeaderCentered = String.format("%" + dateHeaderMargin + "s\n", dateHeader);

        monthString.append(dateHeaderCentered).append(days).append("\n");
        int offset = getDayOffset(date);
        for (int i = 0; i < offset; i++) {
            monthString.append("   ");
        }

        int dayPosition = offset;
        for (int day = 1; day <= date.getMonth().length(date.isLeapYear()); day++) {
            monthString.append(String.format("%2d ", day));

            dayPosition = (dayPosition + 1) % 7;
            if (dayPosition == 0) {
                monthString.append("\n");
            }
        }
        return monthString.toString();
    }

    /**
     * Combines calendar and a text prompt into a single formatted string.
     *
     * @param date   The LocalDate object representing the date, used to generate the calendar.
     * @param prompt A string prompt that will be formatted into multiple lines and aligned with the month data.
     * @return A formatted string where each line contains a part of the month data (left-aligned)
     *         and a corresponding part of the prompt (right-aligned), with proper spacing.
     */
    public String getMonthWithText(LocalDate date, String prompt){
        String monthString = getMonth(date);
        String promptString = formatPrompt(prompt);

        List<String> monthLines = new ArrayList<>(Arrays.stream(monthString.split("\n")).toList());
        String[] promptLines = promptString.split("\n");

        StringBuilder resultString = new StringBuilder();

        int maxLineLength = getMaxLineLength(monthLines);
        int padding = maxLineLength + SPACING;

        resultString.append(monthLines.removeFirst()).append("\n");

        int maxLineAmount = Math.max(monthLines.size(), promptLines.length);

        for(int i = 0; i < maxLineAmount; i++){
            String monthPart = i < monthLines.size() ? monthLines.get(i): "";
            String promptPart = i < promptLines.length ? promptLines[i]: "";

            resultString.append(String.format("%-" + padding + "s %s", monthPart, promptPart)).append("\n");
        }

        return resultString.toString();
    }

    /**
     * Formats a given prompt string into multiple lines, ensuring that each line does not exceed a specified length.
     *
     * @param prompt The input string to be formatted.
     * @return A formatted string where words are split into lines such that the total length of each line
     *         (including spaces) does not exceed the specified `commentLength`.
     */
    public String formatPrompt(String prompt){
        String[] promptSplit = prompt.split(" ");
        StringBuilder formattedString = new StringBuilder();
        int currLineLength = 0;

        for (String string : promptSplit) {
            if (currLineLength + string.length() <= COMMENT_LENGTH) {
                currLineLength += string.length();
                formattedString.append(string).append(" ");
            } else {
                currLineLength = string.length();
                formattedString.append("\n").append(string).append(" ");
            }
        }
        return formattedString.toString();
    }

    private int getMaxLineLength(List<String> prompt){
        int maxLineLength = 0;
        for (String string : prompt) {
            if (maxLineLength < string.length()) {
                maxLineLength = string.length();
            }
        }
        return maxLineLength;
    }
    /**
     * @param date any day in the month that is supposed to be displayed
     * @return amount of days that need to be skipped in order for the first day
     * of the month to be aligned with the corresponding day of the week in the CLI
     * **/
    private int getDayOffset(LocalDate date) {
        int offset = 0;
        DayOfWeek firstDayOfMonth = date.minusDays(date.getDayOfMonth() - 1).getDayOfWeek();
        offset = firstDayOfMonth.getValue() - 1;
        return offset;
    }

    public void startAppointmentCreation(){
        System.out.println("Enter the details of your appointment:\n");
    }

    public String getTitle(){
        System.out.print("Title: ");
        return scanner.nextLine();
    }

    public String getStartDate(){
        System.out.print("Start Date (e.g: YYYY-MM-DD): ");
        String startDate = scanner.next();
        scanner.nextLine(); //consumes \n from the input above, since scanner.next doesn't read \n characters
        return startDate;
    }

    public String getEndDate(){
        System.out.print("End Date (e.g: YYYY-MM-DD): ");
        String endDate = scanner.next();
        scanner.nextLine(); //consumes \n from the input above, since scanner.next doesn't read \n characters
        return endDate;
    }

    public String getStartTime(){
        System.out.print("Start Time (e.g hh:mm): ");
        String startTime = scanner.next();
        scanner.nextLine(); //consumes \n from the input above, since scanner.next doesn't read \n characters
        return startTime;
    }

    public String getEndTime(){
        System.out.print("End Time (e.g hh:mm): ");
        String endTime = scanner.next();
        scanner.nextLine(); //consumes \n from the input above, since scanner.next doesn't read \n characters
        return endTime;
    }

    public String getDescription(){
        System.out.println("Description: ");
        return scanner.nextLine();
    }

    public Optional<Tag> getTag(List<Tag> tags){
        System.out.println("Select the tag you want to add to your appointment:");
        for (int i = 0; i < tags.size(); i++) {
            System.out.println(i + 1 + ". " + tags.get(i).getName());
        }
        System.out.println(tags.size() + 1 +". Exit\n");
        int input = scanner.nextInt();
        scanner.nextLine();
        if(input >= tags.size() +1){
            return Optional.empty();
        }
        return Optional.of(tags.get(input - 1));
    }

    public void startTagCreation(){
        System.out.println("Enter the details of the new Tag");
    }
    public String getTagTitle(){
        System.out.print("Tag title: ");
        return scanner.nextLine();
    }

    public String getTagColor(){
        System.out.print("Tag color: ");
        return scanner.nextLine();
    }

    public int tagAlreadyExists(Tag existingTag){
        System.out.println("The tag with the name \"" + existingTag.getName() + "\" already exists.\n"
                            + "Would you like to overwrite it?\n"
                            + "1. Yes\n"
                            + "2. No");
        int userInput = 0;
        while(userInput < 1 || userInput > 2){
            userInput = scanner.nextInt();
            scanner.nextLine();
            if(userInput < 1 || userInput > 2){
                System.out.println("Invalid input! Please choose from the selection above!\n");
            }
        }
        return userInput;
    }

    public void successfullyOverwriteTag(Tag newTag){
        System.out.println("You have successfully overwritten the tag. New tag: \"" + newTag.getName() + "\"");
    }

    public void cancleOverwriteTag(){
        System.out.println("Canceled! You have not overwritten the tag.");
    }

    public String startEditingAppointment(){
        System.out.println("Enter the name of the appointment you want to edit:");
        return scanner.nextLine();
    }

    public int chooseAppointment(List<Appointment> appointments){
        System.out.println("Choose one of the following appointments: ");
        for(int i = 0; i < appointments.size(); i++){
            System.out.println(i + ": " + appointments.get(i).getTitle()
                    + " Start Date: " + appointments.get(i).getStartDate()
                    + " End Date: " + appointments.get(i).getEndDate()
                    + " Description: " + appointments.get(i).getDescription());
        }
        int appointmentIndex = scanner.nextInt();
        scanner.nextLine();
        return appointmentIndex;
    }

    public int appointmentEditMenu(){
        System.out.println("What do you want to edit?");
        System.out.println("1.Title");
        System.out.println("2.Start and End Date");
        System.out.println("3.Description");
        System.out.println("4.Tags");
        System.out.println("5.Exit");
        int input = scanner.nextInt();
        scanner.nextLine();
        return input;
    }

    public String startTagEditing(){
        System.out.println("Enter the title of the tag that you want to edit.");
        return scanner.nextLine();
    }

    public void tagEditingMenu(){
        System.out.println("Enter the new details of the tag.");
    }

    public void displayError(String prompt){
        System.out.println(prompt);
    }

}