package Domain.Database;

import Domain.Entities.Appointment;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.jooq.generated.Tables.APPOINTMENT;

public class DataManager {
    private static final String PATH_TO_DATABASE = "jdbc:sqlite:src/main/resources/javenderDatabase.db";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(PATH_TO_DATABASE);
    }

    public Appointment getAppointmentById(int appointmentId) {
        try (Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.SQLITE);

            Record record = create.select()
                    .from(APPOINTMENT)
                    .where(APPOINTMENT.APPOINTMENTID.eq(appointmentId))
                    .fetchOne();

            if (record == null) {
                return null;
            }

            String startDateString = record.getValue(APPOINTMENT.STARTDATE);
            String endDateString = record.getValue(APPOINTMENT.ENDDATE);

            LocalDateTime startDate = LocalDateTime.parse(startDateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime endDate = LocalDateTime.parse(endDateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            return new Appointment(
                    record.getValue(APPOINTMENT.APPOINTMENTID),
                    startDate,
                    endDate,
                    record.getValue(APPOINTMENT.TITLE),
                    record.getValue(APPOINTMENT.DESCRIPTION),
                    null //TODO: implement Method for getting the Tags associated with the Appointments
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}