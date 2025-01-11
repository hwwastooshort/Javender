package Domain.Database;

import Domain.Entities.Appointment;
import Domain.Entities.Tag;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.TableField;
import org.jooq.impl.DSL;

import javax.swing.text.html.Option;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.jooq.generated.Tables.*;

public class DataManager {

    private static final String PATH_TO_DATABASE = "jdbc:sqlite:src/main/resources/javenderDatabase.db";

    public enum DateFilter{
        STARTDATE,
        ENDDATE
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(PATH_TO_DATABASE);
    }

    /**
     * Method for fetching data of an Appointment based on the appointmentId
     *
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
     *
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

            List<Tag> tagList = result.stream()
                .map(record -> new Tag(
                    record.getValue(TAG.TAGID),
                    record.getValue(TAG.NAME),
                    record.getValue(TAG.COLOR)
                ))
                .collect(Collectors.toList());

            return Optional.of(tagList);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public Optional<List<Appointment>> getAppointmentsByDate(LocalDate date, DateFilter filter) {
        try (Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.SQLITE);

            String datePrefix = date.toString() + "T"; // example: "2025-01-01T"

            Result<?> result;

            switch (filter){
                case STARTDATE:
                    result = create.select()
                        .from(APPOINTMENT)
                        .where(APPOINTMENT.STARTDATE.like(datePrefix + "%"))
                        .fetch();
                    break;
                case ENDDATE:
                    result = create.select()
                        .from(APPOINTMENT)
                        .where(APPOINTMENT.ENDDATE.like(datePrefix + "%"))
                        .fetch();
                    break;
                default:
                    result = null;
                    break;
            }

            if (result.isEmpty()) {
                return Optional.empty();
            }

            List<Appointment> appointmentList = result.stream()
                .map(record -> {
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
                        getTagByAppointmentId(record.getValue(APPOINTMENT.APPOINTMENTID)).orElseGet(ArrayList::new)
                    );
                })
                .collect(Collectors.toList());

            return Optional.of(appointmentList);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<List<Appointment>> getAppointmentsByRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        try (Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.SQLITE);

            Result<?> result = create.select()
                .from(APPOINTMENT)
                .where(APPOINTMENT.STARTDATE.between(
                    startDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    endDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .fetch();

            if (result.isEmpty()) {
                return Optional.empty();
            }

            List<Appointment> appointmentList = result.stream()
                .map(record -> {
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
                        getTagByAppointmentId(record.getValue(APPOINTMENT.APPOINTMENTID)).orElseGet(ArrayList::new)
                    );

                })
                .collect(Collectors.toList());

            return Optional.of(appointmentList);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}