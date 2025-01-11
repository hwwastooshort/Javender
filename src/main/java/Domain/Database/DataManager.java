package Domain.Database;

import Domain.Entities.Appointment;
import Domain.Entities.Tag;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.jooq.generated.Tables.*;

public class DataManager {
    private static final String PATH_TO_DATABASE = "jdbc:sqlite:src/main/resources/javenderDatabase.db";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(PATH_TO_DATABASE);
    }

    /**
     * Method for fetching data of an Appointment based on the appointmentId
     * @param appointmentId the Appointment we want to look up
     * @return Optional of Appointment, returns empty Optional if the appointmentId doesn't exist
     */
    public Optional<Appointment> getAppointmentById(int appointmentId) {
        try (Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.SQLITE);

            Record record = create.select()
                    .from(APPOINTMENT)
                    .where(APPOINTMENT.APPOINTMENTID.eq(appointmentId))
                    .fetchOne();

            if (record == null) {
                return Optional.empty();
            }

            String startDateString = record.getValue(APPOINTMENT.STARTDATE);
            String endDateString = record.getValue(APPOINTMENT.ENDDATE);

            LocalDateTime startDate = LocalDateTime.parse(startDateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime endDate = LocalDateTime.parse(endDateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            Appointment appointment = new Appointment(
                    record.getValue(APPOINTMENT.APPOINTMENTID),
                    startDate,
                    endDate,
                    record.getValue(APPOINTMENT.TITLE),
                    record.getValue(APPOINTMENT.DESCRIPTION),
                    getTagByAppointmentId(record.getValue(APPOINTMENT.APPOINTMENTID)).orElseGet(ArrayList::new)
            );

            return Optional.of(appointment);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Method for fetching Data of Tags, which belong to an unique appointmentId
     * @param appointmentId the appointmentId of the Appointment which is used for Joining
     * @return the list of tags matching the appointmentId as an Optional, returns empty Optional if there are none
     */

    public Optional<List<Tag>> getTagByAppointmentId(int appointmentId) {
        try (Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.SQLITE);

            Result<?> result = create.select()
                    .from(APPOINTMENTTAG)
                    .join(TAG).on(APPOINTMENTTAG.TAGID.eq(TAG.TAGID))
                    .where(APPOINTMENTTAG.APPOINTMENTID.eq(appointmentId))
                    .fetch();

            if (result.isEmpty()) {
                return Optional.empty();
            }

            List<Tag> tagList = new ArrayList<>();
            result.forEach(record -> {
                int tagId = record.getValue(TAG.TAGID);
                String color = record.getValue(TAG.COLOR);
                String name = record.getValue(TAG.NAME);
                tagList.add(new Tag(tagId, name, color));
            });

            return Optional.of(tagList);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}