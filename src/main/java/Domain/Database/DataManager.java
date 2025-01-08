package Domain.Database;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.jooq.generated.Tables.*;

public class DataManager {
    public static void main(String[] args) {
        String path = "jdbc:sqlite:src/main/resources/javenderDataBase.db";

        try (Connection conn = DriverManager.getConnection(path)) {
            DSLContext create = DSL.using(conn, SQLDialect.SQLITE);

            Result<?> result = create.select()
                    .from(APPOINTMENT)
                    .where(APPOINTMENT.TITLE.like("%Meeting%"))
                    .fetch();

            result.forEach(record -> {
                Integer id = record.getValue(APPOINTMENT.APPOINTMENTID);
                String title = record.getValue(APPOINTMENT.TITLE);

                System.out.println("ID: " + id + " Title: " + title);
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
