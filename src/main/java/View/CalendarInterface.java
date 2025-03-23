package View;

import Model.Entities.Appointment;
import Model.Entities.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class CalendarInterface implements UserInterface {

    private static final Logger logger = LoggerFactory.getLogger(CalendarInterface.class);
    private final Scanner scanner = new Scanner(System.in);
    private final int COMMENT_LINE_LENGTH = 30;
    @SuppressWarnings("FieldCanBeLocal")
    private final int MAX_COMMENT_LENGTH = COMMENT_LINE_LENGTH * 2 - 3; // intentionally declared as a constant to make adding further features easier
    @SuppressWarnings("FieldCanBeLocal")
    private final int SPACING = 10;   // Space between the calendar & upcoming appointments & intentionally declared as a constant to make adding further features easier

    private String getMonthDaysFormatted(LocalDate date) {
        StringBuilder monthString = new StringBuilder();

        int offset = getDayOffset(date);
        monthString.append("   ".repeat(Math.max(0, offset)));

        int dayPosition = offset;
        for (int day = 1; day <= date.lengthOfMonth(); day++) {
            monthString.append(String.format("%2d ", day));
            dayPosition = (dayPosition + 1) % 7;
            if (dayPosition == 0) {
                monthString.append("\n");
            }
        }
        monthString.append("   ".repeat(7 - dayPosition));

        return monthString.toString();
    }

    private int getDayOffset(LocalDate date) {
        int offset;
        DayOfWeek firstDayOfMonth = date.minusDays(date.getDayOfMonth() - 1).getDayOfWeek();
        offset = firstDayOfMonth.getValue() - 1;
        return offset;
    }

    public String getCalendar(LocalDate date, List<Appointment> appointmentList, int monthAmount) {
        StringBuilder calendarView = new StringBuilder();
        String days = monthAmount == 1 ? "MO TU WE TH FR SA SU " : "    MO TU WE TH FR SA SU ";
        int maxLineLength = days.length();

        if (monthAmount == 1) {
            return getSingleMonthCalendar(date, appointmentList, days, maxLineLength);
        }

        String currentDay = formatCurrentDayHeader();
        int repeatCountDate = Math.max(0, maxLineLength - currentDay.length());
        int repeatCountCalender = Math.max(0, currentDay.length() - days.length());

        if (!isLocalDateMonthEqual(date, LocalDate.now())) {
            calendarView.append(ColorManager.getColoredText("yellow", getWarningString(date)));
        }

        calendarView.append(ColorManager.getColoredText("bold", ColorManager.getColoredText("underline", currentDay)))
                .append(" ".repeat(repeatCountDate)).append("\n")
                .append(days).append(" ".repeat(repeatCountCalender));

        for (int i = 0; i < monthAmount; i++) {
            calendarView.append(formatCalendarMonthWithMargin(date.plusMonths(i), appointmentList, repeatCountCalender));
        }
        return calendarView.toString();
    }

    private String getSingleMonthCalendar(LocalDate date, List<Appointment> appointmentList, String days, int maxLineLength) {
        StringBuilder calendarView = new StringBuilder();
        String dateHeader = date.getMonth() + " " + date.getYear();
        int margin = (days.length() - dateHeader.length()) / 2;

        String dateHeaderCentered = " ".repeat(margin) + dateHeader + " "
                .repeat(maxLineLength - margin - dateHeader.length());

        if (!isLocalDateMonthEqual(date, LocalDate.now())) {
            calendarView.append(ColorManager.getColoredText("yellow", getWarningString(date)));
        }

        return calendarView.append(ColorManager.getColoredText("bold", dateHeaderCentered))
                .append("\n").append(days).append("\n")
                .append(getMonthWithAppointments(date, appointmentList))
                .toString();
    }

    private String formatCurrentDayHeader() {
        LocalDateTime now = LocalDateTime.now();
        String numberSuffix = getNumberSuffix(now.getDayOfMonth());

        return now.getDayOfWeek().toString().charAt(0)
                + now.getDayOfWeek().toString().substring(1).toLowerCase() + ", "
                + now.getMonth().toString().charAt(0)
                + now.getMonth().toString().substring(1).toLowerCase() + " "
                + now.getDayOfMonth() + numberSuffix + ", "
                + now.getYear();
    }

    private String getWarningString(LocalDate date) {
        return "(You are currently viewing " + date.getMonth().toString().charAt(0)
                + date.getMonth().toString().substring(1).toLowerCase()
                + " " + date.getYear() + ")\n\n";
    }

    private String formatCalendarMonthWithMargin(LocalDate date, List<Appointment> appointmentList, int repeatCountCalender) {
        String[] month = getMonthWithAppointments(date, appointmentList).split("\n");
        StringBuilder formattedMonth = new StringBuilder();

        for (int j = 0; j < month.length; j++) {
            String preString = (j == 0) ? date.getMonth().toString().substring(0, 3) + " " : "    ";
            formattedMonth.append("\n").append(preString).append(month[j]).append(" ".repeat(repeatCountCalender));
        }

        return formattedMonth.toString();
    }

    private String getNumberSuffix(int number) {
        if (number >= 11 && number <= 13) {
            return "th";
        }
        return switch (number % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }

    private String getMonthWithAppointments(LocalDate date, List<Appointment> appointmentList) {
        String monthString = getMonthDaysFormatted(date);
        LocalDate today = LocalDate.now();

        for (int day = 1; day <= date.lengthOfMonth(); day++) {
            LocalDate currentDay = date.withDayOfMonth(day);

            // Check whether the day is today or has appointments
            boolean isToday = currentDay.isEqual(today);
            List<Appointment> dayAppointments = appointmentList.stream()
                    .filter(appointment ->
                            (currentDay.isEqual(appointment.getStartDate().toLocalDate()) || currentDay.isAfter(appointment.getStartDate()
                                    .toLocalDate())) &&
                                    (currentDay.isEqual(appointment.getEndDate().toLocalDate())
                                            || currentDay.isBefore(appointment.getEndDate().toLocalDate())))
                    .toList();

            // Only replace if the day is today or appointments exist
            if (!dayAppointments.isEmpty() || isToday) {
                String color = "white";
                if (!dayAppointments.isEmpty()) {
                    color = dayAppointments.getFirst().getTags().isEmpty() ? "white" :
                            dayAppointments.getFirst().getTags().getFirst().getColor();
                }

                String formattedDay = ColorManager.getColoredText(isToday ? "bg_" + color : color, Integer.toString(day));


                // Set the formatting of the numbers correctly
                String dayRegex = "\\b" + String.format(day < 10 ? "%d" : "%2d", day) + "\\b";
                String formattedReplacement = String.format(day < 10 ? "%10s" : "%11s", formattedDay);

                // Replace the tag in the string
                monthString = monthString.replaceFirst(dayRegex, formattedReplacement);
            }
        }

        return monthString;
    }

    public String getCalendarWithUpcomingAppointments(LocalDate date, List<Appointment> appointmentList, int monthAmount) {
        String calendarString = getCalendar(date, appointmentList, monthAmount);

        List<Appointment> upcomingAppointments = appointmentList.stream()
                .filter(appointment -> appointment.getStartDate().isAfter(LocalDateTime.now()) // All appointments in the future
                        || (appointment.getStartDate().isBefore(LocalDateTime.now()) // All appointments which are currently running
                        && appointment.getEndDate().isAfter(LocalDateTime.now())))
                .sorted(Comparator.comparing(Appointment::getStartDate))
                .toList();

        if (upcomingAppointments.isEmpty()) {
            return calendarString;
        }

        StringBuilder appointmentString = new StringBuilder();
        appointmentString.append(isLocalDateMonthEqual(date, LocalDate.now()) ? "" : "\n\n")
                .append(ColorManager.getColoredText("bold", "Upcoming Appointment" +
                        (upcomingAppointments.size() > 1 ? "s:" : ":"))).append("\n\n");

        appointmentString.append(formatAppointment(upcomingAppointments.getFirst()));
        if (monthAmount > 1) {
            appointmentString.append(upcomingAppointments.size() > 1
                    ? "\n\n" + formatAppointment(upcomingAppointments.get(1))
                    : "");
        }

        return mergeCalendarWithAppointments(calendarString, appointmentString.toString());
    }

    private String mergeCalendarWithAppointments(String calendar, String appointments) {
        List<String> calendarLines = new ArrayList<>(Arrays.stream(calendar.split("\n")).toList());
        String[] appointmentLines = appointments.split("\n");

        StringBuilder resultString = new StringBuilder();

        int maxLineLength = calendarLines.getFirst().length();
        int maxLineAmount = Math.max(calendarLines.size(), appointmentLines.length);

        for (int i = 0; i < maxLineAmount; i++) {
            String monthPart = i < calendarLines.size() ? calendarLines.get(i) : " ".repeat(maxLineLength);
            String appointmentPart = i < appointmentLines.length ? appointmentLines[i] : "";

            resultString.append(monthPart).repeat(" ", SPACING).append(appointmentPart).append("\n");
        }
        return resultString.toString();
    }

    private boolean isLocalDateMonthEqual(LocalDate date1, LocalDate date2) {
        return date1.getMonth() == date2.getMonth() && date1.getYear() == date2.getYear();
    }

    public String formatAppointment(Appointment appointment) {
        StringBuilder formattedAppointment = new StringBuilder();

        String appointmentDate = formatAppointmentDate(appointment);
        formattedAppointment.append(appointmentDate).append("\n");

        String color = appointment.getTags().isEmpty()
                ? "white"
                : appointment.getTags().getFirst().getColor();

        formattedAppointment.append("-> ")
                .append(ColorManager.getColoredText(color, appointment.getTitle()))
                .append("\n");

        formattedAppointment.append(formatAppointmentDescription(appointment)).append("\n");
        formattedAppointment.append(formatAppointmentTags(appointment));

        return formattedAppointment.toString();
    }

    private String formatAppointmentDate(Appointment appointment) {
        String singleDayAppointment = appointment.getStartDate().format(DateTimeFormatter.ofPattern("(yyyy/MM/dd | HH:mm "))
                + appointment.getEndDate().format(DateTimeFormatter.ofPattern("- HH:mm)"));

        String multipleDayAppointment = appointment.getStartDate().format(DateTimeFormatter.ofPattern("(yyyy/MM/dd, HH:mm "))
                + appointment.getEndDate().format(DateTimeFormatter.ofPattern("- yyyy/MM/dd, HH:mm)"));

        if (appointment.getStartDate().isBefore(LocalDateTime.now())
                && appointment.getEndDate().isAfter(LocalDateTime.now())) {
            singleDayAppointment += ColorManager.getColoredText("yellow", " (Running)");
            multipleDayAppointment += ColorManager.getColoredText("yellow", " (Running)");
        }

        return appointment.getStartDate().toLocalDate().isEqual(appointment.getEndDate().toLocalDate())
                ? singleDayAppointment
                : multipleDayAppointment;
    }

    private String formatAppointmentTags(Appointment appointment) {
        StringBuilder tags = new StringBuilder();

        if (appointment.getTags().isEmpty()) {
            return "";
        }

        tags.append("Tags: ");
        for (int i = 0; i < appointment.getTags().size(); i++) {
            String appointmentColor = appointment.getTags().get(i).getColor();
            String appointmentName = appointment.getTags().get(i).getName();
            if (i == 0) {
                tags.append(ColorManager.getColoredText(appointmentColor, appointmentName));
            } else {
                tags.append(", ").append(ColorManager.getColoredText(appointmentColor, appointmentName));
            }
        }
        tags.append("\n");
        return tags.toString();
    }

    private String formatAppointmentDescription(Appointment appointment) {
        StringBuilder formattedDescription = new StringBuilder();

        String[] appointmentSplit = appointment.getDescription().split(" ");
        formattedDescription.append("   ");

        int currLineLength = 0;
        int totalCommentLength = 0;

        for (String string : appointmentSplit) {
            if (totalCommentLength + string.length() > MAX_COMMENT_LENGTH) {
                formattedDescription.append("...");
                break;
            }
            if (currLineLength + string.length() <= COMMENT_LINE_LENGTH) {
                currLineLength += string.length();
                totalCommentLength += string.length();
                formattedDescription.append(string).append(" ");
            } else {
                currLineLength = string.length();
                totalCommentLength += string.length();
                formattedDescription.append("\n").append("   ").append(string).append(" ");
            }
        }
        return formattedDescription.toString();
    }

    public void startAppointmentCreation() {
        System.out.println("Enter the details of your appointment:\n");
    }

    public String getTitle() {
        System.out.print("Title: ");
        return scanner.nextLine();
    }

    public String getStartDate() {
        System.out.print("Start Date (e.g: YYYY-MM-DD): ");
        String startDate = scanner.next();
        scanner.nextLine(); //consumes \n from the input above, since scanner.next doesn't read \n characters
        return startDate;
    }

    public String getEndDate() {
        System.out.print("End Date (e.g: YYYY-MM-DD): ");
        String endDate = scanner.next();
        scanner.nextLine(); //consumes \n from the input above, since scanner.next doesn't read \n characters
        return endDate;
    }

    public String getStartTime() {
        System.out.print("Start Time (e.g hh:mm): ");
        String startTime = scanner.next();
        scanner.nextLine(); //consumes \n from the input above, since scanner.next doesn't read \n characters
        return startTime;
    }

    public String getEndTime() {
        System.out.print("End Time (e.g hh:mm): ");
        String endTime = scanner.next();
        scanner.nextLine(); //consumes \n from the input above, since scanner.next doesn't read \n characters
        return endTime;
    }

    public String getDescription() {
        System.out.println("Description: ");
        return scanner.nextLine();
    }

    public Optional<Tag> getTag(List<Tag> allTags, List<Tag> appliedTags) {
        System.out.println("Select the tags you want to add to your appointment:");
        for (int i = 0; i < allTags.size(); i++) {
            System.out.println(i + 1 + ". [" + (appliedTags.contains(allTags.get(i)) ? "X" : " ") + "] "
                    + ColorManager.getColoredText(allTags.get(i).getColor(), allTags.get(i).getName()));
        }
        System.out.println(allTags.size() + 1 + ". Exit\n");

        int input = scanner.nextInt();
        scanner.nextLine();
        if (input >= allTags.size() + 1) {
            return Optional.empty();
        }
        return Optional.of(allTags.get(input - 1));
    }

    public void startTagCreation() {
        System.out.println("Enter the details of the new Tag");
    }

    public String getTagTitle() {
        System.out.print("Tag title: ");
        return scanner.nextLine();
    }

    public int getTagColorIndex() {
        System.out.print("Choose one of the following colors for your tag: ");
        System.out.println(
                ColorManager.getColoredText("red", "\n1.RED") +
                        ColorManager.getColoredText("green", "\n2.GREEN") +
                        ColorManager.getColoredText("yellow", "\n3.YELLOW") +
                        ColorManager.getColoredText("blue", "\n4.BLUE") +
                        ColorManager.getColoredText("purple", "\n5.PURPLE") +
                        ColorManager.getColoredText("cyan", "\n6.CYAN") +
                        ColorManager.getColoredText("white", "\n7.WHITE"));
        return getIntegerInput();
    }

    public int tagAlreadyExists(Tag existingTag) {
        System.out.println("The tag with the name \"" + existingTag.getName() + "\" already exists.\n"
                + "Would you like to overwrite it?\n"
                + "1. Yes\n"
                + "2. No");
        int userInput = 0;
        boolean loop = true;
        while (loop) {
            userInput = scanner.nextInt();
            scanner.nextLine();
            switch (userInput) {
                case 1, 2:
                    loop = false;
                    break;
                default:
                    System.out.println("Invalid input! Please choose from the selection above!");
                    break;
            }
        }
        return userInput;
    }

    public void successfullyOverwriteTag(Tag newTag) {
        System.out.println("You have successfully overwritten the tag. Updated tag: \"" + newTag.getName() + "\"");
    }

    public void cancelOverwriteTag() {
        System.out.println("Canceled! You have not overwritten the tag.");
    }

    public String startEditingAppointment() {
        System.out.println("Enter the name of the appointment you want to edit:");
        return scanner.nextLine();
    }

    public int chooseAppointment(List<Appointment> appointments) {
        System.out.println("Choose one of the following appointments: ");
        for (int i = 0; i < appointments.size(); i++) {
            System.out.println((i + 1) + ": " + appointments.get(i).getTitle()
                    + " Start Date: " + appointments.get(i).getStartDate()
                    + " End Date: " + appointments.get(i).getEndDate()
                    + " Description: " + appointments.get(i).getDescription());
        }
        int appointmentIndex = scanner.nextInt() - 1;
        scanner.nextLine();
        return appointmentIndex;
    }

    public int appointmentEditMenu() {
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

    public String startEditingTag() {
        System.out.println("Enter the title of the tag that you want to edit.");
        return scanner.nextLine();
    }

    public void tagEditMenu() {
        System.out.println("Enter the new details of the tag.");
    }


    public String startDeletingAppointment() {
        System.out.println("Enter the title of the appointment you want to delete.");
        return scanner.nextLine();
    }

    public String startDeletingTag() {
        System.out.println("Enter the name of the tag you want to delete:");
        return scanner.nextLine();
    }

    public void displayError(String prompt) {
        System.out.println(prompt);
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }

    public void displayCommandList() {
        clearScreen();
        System.out.println("""
                Name & description of all available commands:
                -"manage": opens the menu for managing appointments and tags
                -<name of a month> (+ <year>): display the respective month (of the corresponding year)
                -"now": display the current month
                -"upcoming" (+ amount (+tag name)): display the upcoming appointments according to the currently displayed month.
                -"exit": closes the program
                Enter anything to return to the calendar.""");
        scanner.nextLine();
    }

    public int getIntegerInput() {
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.next();
        }
        int input = scanner.nextInt();
        scanner.nextLine();
        return input;
    }

    public void displayAppointments(List<Appointment> appointments) {
        clearScreen();
        appointments.forEach(
                appointment -> {
                    System.out.println();
                    System.out.print(ColorManager.UNDERLINE + appointment.getTitle() + ColorManager.RESET
                            + " " + formatAppointmentDate(appointment) + "\n"
                            + "\"" + appointment.getDescription() + "\"\nTags: ");
                    appointment.getTags().forEach(
                            tag -> System.out.print(
                                    ColorManager.getColoredText(tag.getColor(), tag.getName()) + " "
                            )
                    );
                    System.out.println();
                }
        );
        System.out.println("\nEnter anything to return to the calendar.");
        scanner.nextLine();
    }

    public String getUserCommand() {
        return scanner.nextLine();
    }

    public boolean confirmAction(String message) {
        displayMessage(message + " (yes/no)");
        String response = getUserCommand().trim().toLowerCase();
        return response.equals("yes");
    }

    public static void clearScreen() {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            try {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } catch (IOException | InterruptedException e) {
                logger.error("Error while clearing the screen: ", e);
            }
        } else {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
        System.out.println();
    }
}