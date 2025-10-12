import java.sql.*;

public class HospitalDBSetup {
    private static final String URL = "jdbc:sqlite:hospital.db";

    @SuppressWarnings("CallToPrintStackTrace")
    public static void main(String[] args) {
        try (Connection c = DriverManager.getConnection(URL);
             Statement s = c.createStatement()) {

            // Drop old tables if they exist
            s.executeUpdate("DROP TABLE IF EXISTS appointments");
            s.executeUpdate("DROP TABLE IF EXISTS patients");

            // Create patients table with AUTOINCREMENT id
            s.executeUpdate("CREATE TABLE patients (" +
                    "id INTEGER, " +
                    "name TEXT NOT NULL, " +
                    "age INTEGER, " +
                    "disease TEXT, " +
                    "medicine TEXT, " +
                    "curing_time TEXT, " +
                    "doctor TEXT, " +
                    "photo TEXT)");

            // Create appointments table
            s.executeUpdate("CREATE TABLE appointments (" +
                    "id INTEGER , " +
                    "patient_id INTEGER, " +
                    "appointment_date TEXT, " +
                    "appointment_time TEXT, " +
                    "FOREIGN KEY(patient_id) REFERENCES patients(id))");

            // Insert sample patients (without specifying id)
            s.executeUpdate("INSERT INTO patients (id,name, age, disease, medicine, curing_time, doctor, photo) VALUES " +
                    "(1 'Vinay', 23, 'Fever', 'Dolo 650', '5 days', 'Dr. Kumar', 'vinay.jpg')");
            s.executeUpdate("INSERT INTO patients (id,name, age, disease, medicine, curing_time, doctor, photo) VALUES " +
                    "(2 'Arun', 30, 'Cold', 'Cetzine', '3 days', 'Dr. Mehta', 'arun.jpg')");
            s.executeUpdate("INSERT INTO patients (id,name, age, disease, medicine, curing_time, doctor, photo) VALUES " +
                    "(3 'Priya', 28, 'Headache', 'Paracetamol', '2 days', 'Dr. Sharma', 'priya.jpg')");

            // Display all patients
            ResultSet rs = s.executeQuery("SELECT * FROM patients");
            System.out.println("=== Patients in hospital.db ===");
            while (rs.next()) {
                System.out.println(
                        "ID: " + rs.getInt("id") +
                        ", Name: " + rs.getString("name") +
                        ", Age: " + rs.getInt("age") +
                        ", Disease: " + rs.getString("disease") +
                        ", Medicine: " + rs.getString("medicine") +
                        ", Curing Time: " + rs.getString("curing_time") +
                        ", Doctor: " + rs.getString("doctor") +
                        ", Photo: " + rs.getString("photo")
                );
            }
            System.out.println("================================");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}