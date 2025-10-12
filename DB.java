// DB.java
import java.sql.*;

@SuppressWarnings("CallToPrintStackTrace")
public class DB {
    private static final String URL = "jdbc:sqlite:hospital.db";

    static {
        try (Connection c = DriverManager.getConnection(URL);
             Statement s = c.createStatement()) {

            // Patients table
            s.executeUpdate("CREATE TABLE IF NOT EXISTS patients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "age INTEGER," +
                    "disease TEXT," +
                    "medicine TEXT," +
                    "curing_time TEXT," +
                    "doctor TEXT)");

            // Appointments table
            s.executeUpdate("CREATE TABLE IF NOT EXISTS appointments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "patient_id INTEGER NOT NULL," +
                    "appointment_date TEXT," +   // YYYY-MM-DD
                    "appointment_time TEXT," +   // HH:mm
                    "FOREIGN KEY(patient_id) REFERENCES patients(id))");

            // Insert 20 sample patients if table is empty
            ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM patients");
            if (rs.next() && rs.getInt(1) == 0) {
                s.executeUpdate(
                    "INSERT INTO patients (name, age, disease, medicine, curing_time, doctor) VALUES " +
                    "('Ravi Kumar', 34, 'Fever', 'Paracetamol', '5 days', 'Dr. Sharma')," +
                    "('Anita Singh', 28, 'Cold', 'Cetirizine', '3 days', 'Dr. Mehta')," +
                    "('Manoj Yadav', 45, 'Diabetes', 'Metformin', 'Ongoing', 'Dr. Verma')," +
                    "('Pooja Rani', 19, 'Typhoid', 'Ciprofloxacin', '10 days', 'Dr. Gupta')," +
                    "('Amit Sharma', 52, 'Hypertension', 'Amlodipine', 'Ongoing', 'Dr. Iyer')," +
                    "('Neha Patel', 30, 'Asthma', 'Inhaler', 'Ongoing', 'Dr. Banerjee')," +
                    "('Suresh Das', 40, 'Back Pain', 'Ibuprofen', '7 days', 'Dr. Sharma')," +
                    "('Sunita Jain', 37, 'Anemia', 'Iron Supplements', '30 days', 'Dr. Mehta')," +
                    "('Rajesh Kumar', 50, 'Arthritis', 'Painkillers', 'Ongoing', 'Dr. Verma')," +
                    "('Deepa Nair', 25, 'Allergy', 'Antihistamine', '5 days', 'Dr. Gupta')," +
                    "('Karan Singh', 42, 'Migraine', 'Sumatriptan', '10 days', 'Dr. Iyer')," +
                    "('Meena Joshi', 33, 'Skin Rash', 'Ointment', '7 days', 'Dr. Banerjee')," +
                    "('Vikram Rao', 29, 'Covid-19', 'Remdesivir', '14 days', 'Dr. Sharma')," +
                    "('Priya Mishra', 22, 'Food Poisoning', 'ORS & Antibiotic', '3 days', 'Dr. Mehta')," +
                    "('Arun Kumar', 48, 'Liver Disease', 'Lactulose', 'Ongoing', 'Dr. Verma')," +
                    "('Shalini Gupta', 36, 'Thyroid', 'Levothyroxine', 'Ongoing', 'Dr. Gupta')," +
                    "('Rohit Singh', 27, 'Dengue', 'Paracetamol & Fluids', '7 days', 'Dr. Iyer')," +
                    "('Savita Devi', 55, 'Heart Disease', 'Aspirin', 'Ongoing', 'Dr. Banerjee')," +
                    "('Nikhil Jain', 31, 'Kidney Stones', 'Painkillers', '5 days', 'Dr. Sharma')," +
                    "('Anjali Rao', 44, 'Ulcer', 'Omeprazole', '14 days', 'Dr. Mehta')"
                );
                System.out.println("✅ Sample patients inserted into database.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // helper to get connection
    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
