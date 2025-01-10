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

import static org.jooq.generated.Tables.*;

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
                    getTagByAppointmentId(record.getValue(APPOINTMENT.APPOINTMENTID))
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Tag> getTagByAppointmentId(int appointmentId) {
        List<Tag> tagList = new ArrayList<>();
        try(Connection conn = getConnection()){
            DSLContext create = DSL.using(conn, SQLDialect.SQLITE);
            Result<?> result = create.select()
                                    .from(APPOINTMENTTAG)
                                    .join(TAG).on(APPOINTMENTTAG.TAGID.eq(TAG.TAGID))
                                    .where(APPOINTMENTTAG.APPOINTMENTID.eq(appointmentId))
                                    .fetch();
            result.forEach(record -> {
                   int tagId = record.getValue(TAG.TAGID);
                   String color = record.getValue(TAG.COLOR);
                   String name = record.getValue(TAG.NAME);
                   Tag tag = new Tag(tagId, name, color);
                   tagList.add(tag);
            });
            return tagList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}