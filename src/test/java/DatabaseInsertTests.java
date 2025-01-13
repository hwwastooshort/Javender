import static org.junit.jupiter.api.Assertions.*;

import Domain.Database.DataManager;
import Domain.Entities.Appointment;
import Domain.Entities.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class DatabaseInsertTests {

    private final DataManager dm = new DataManager();

    @AfterEach
    void cleanUp() {
        // Entfernt den Testdatensatz nach jedem Test
        dm.removeAppointmentById(100);
    }

    @Test
    void testSuccessfulAppointmentInsertion() {
        // Vorbereitung der Testdaten
        Appointment appointment = new Appointment(
                100,
                LocalDateTime.parse("2026-01-01T10:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                LocalDateTime.parse("2027-01-01T10:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "Handy spielen!",
                "Brawl Stars mit Simon",
                new ArrayList<>(Arrays.asList(new Tag(1, "testingTag", "yellow")))
        );

        // Ausführung der Methode
        boolean result = dm.addAppointment(appointment);

        // Überprüfung, ob die Methode erfolgreich war
        assertTrue(result, "Insertion of this Appointment should be possible");

        // Überprüfung, ob der Termin in der Datenbank gespeichert wurde
        Optional<Appointment> checkAppointment = dm.getAppointmentById(100);

        assertTrue(checkAppointment.isPresent(), "Appointment should now be in the database");

        // Vergleich der gespeicherten Daten mit den erwarteten Daten
        Appointment appointmentFromDatabase = checkAppointment.get();
        assertEquals(appointment.getAppointmentId(), appointmentFromDatabase.getAppointmentId());
        assertEquals(appointment.getStartDate(), appointmentFromDatabase.getStartDate());
        assertEquals(appointment.getEndDate(), appointmentFromDatabase.getEndDate());
        assertEquals(appointment.getTitle(), appointmentFromDatabase.getTitle());
        assertEquals(appointment.getDescription(), appointmentFromDatabase.getDescription());
        assertEquals(appointment.getTags(), appointmentFromDatabase.getTags());
    }
}