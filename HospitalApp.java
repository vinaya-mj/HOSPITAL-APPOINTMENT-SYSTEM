import java.awt.*;
import java.io.File;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class HospitalApp extends JFrame {
    private static final String URL = "jdbc:sqlite:hospital.db";

    public HospitalApp() {
        setTitle("🏥 Hospital Appointment System");
        setSize(520, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));

        JButton addPatientBtn = new JButton("➕ Add Patient");
        JButton viewPatientBtn = new JButton("🔍 View Patient by ID");
        JButton viewAllPatientsBtn = new JButton("📋 View All Patients");
        JButton bookAppointmentBtn = new JButton("📅 Book Appointment");

        add(addPatientBtn);
        add(viewPatientBtn);
        add(viewAllPatientsBtn);
        add(bookAppointmentBtn);

        addPatientBtn.addActionListener(e -> openAddPatientPage());
        viewPatientBtn.addActionListener(e -> openViewPatientPage());
        viewAllPatientsBtn.addActionListener(e -> openViewAllPatientsPage());
        bookAppointmentBtn.addActionListener(e -> openAppointmentPage());

        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Initialize DB tables (create if not exists)
    private static void initDatabase() {
        try (Connection c = DriverManager.getConnection(URL);
             Statement s = c.createStatement()) {

            s.executeUpdate("CREATE TABLE IF NOT EXISTS patients (" +
                    "id INTEGER PRIMARY KEY," +          // primary key (manual or suggested)
                    "name TEXT NOT NULL," +
                    "age INTEGER," +
                    "disease TEXT," +
                    "medicine TEXT," +
                    "curing_time TEXT," +
                    "doctor TEXT," +
                    "photo TEXT)");

            s.executeUpdate("CREATE TABLE IF NOT EXISTS appointments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "patient_id INTEGER," +
                    "appointment_date TEXT," +
                    "appointment_time TEXT," +
                    "FOREIGN KEY(patient_id) REFERENCES patients(id))");

            System.out.println("✅ Database ready.");
        } catch (Exception e) {
            System.out.println("❌ DB init error: " + e.getMessage());
        }
    }

    // Utility: get next available ID (max(id)+1) or 1 if none
    private int getNextPatientId() {
        try (Connection c = DriverManager.getConnection(URL);
             PreparedStatement ps = c.prepareStatement("SELECT MAX(id) FROM patients");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int max = rs.getInt(1);
                if (rs.wasNull()) return 1;
                return max + 1;
            }
        } catch (Exception e) {
            // ignore and fallback
        }
        return 1;
    }

    // --- Add Patient Page (with visible ID field pre-filled) ---
    private void openAddPatientPage() {
        JFrame f = new JFrame("➕ Add New Patient");
        f.setSize(520, 520);
        f.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = new JTextField(10);
        idField.setText(String.valueOf(getNextPatientId())); // prefill next id
        JTextField nameField = new JTextField(20);
        JTextField ageField = new JTextField(6);

        String[] diseases = {"Fever", "Cold", "Headache", "Cough", "Diabetes", "Other"};
        JComboBox<String> diseaseBox = new JComboBox<>(diseases);

        JTextField medicineField = new JTextField(20);

        String[] curingTimes = {"2 days", "3 days", "5 days", "1 week", "2 weeks"};
        JComboBox<String> curingBox = new JComboBox<>(curingTimes);

        String[] doctors = {"Dr. Kumar", "Dr. Mehta", "Dr. Sharma", "Dr. Ramesh"};
        JComboBox<String> doctorBox = new JComboBox<>(doctors);

        JTextField photoField = new JTextField(20);
        JButton browseBtn = new JButton("Browse...");
        JButton saveBtn = new JButton("Save Patient");

        int y = 0;

        gbc.gridx = 0; gbc.gridy = y; f.add(new JLabel("Patient ID:"), gbc);
        gbc.gridx = 1; f.add(idField, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; f.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; f.add(nameField, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; f.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1; f.add(ageField, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; f.add(new JLabel("Disease:"), gbc);
        gbc.gridx = 1; f.add(diseaseBox, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; f.add(new JLabel("Medicine:"), gbc);
        gbc.gridx = 1; f.add(medicineField, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; f.add(new JLabel("Curing Time:"), gbc);
        gbc.gridx = 1; f.add(curingBox, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; f.add(new JLabel("Doctor:"), gbc);
        gbc.gridx = 1; f.add(doctorBox, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; f.add(new JLabel("Photo:"), gbc);
        gbc.gridx = 1; f.add(photoField, gbc);
        y++;

        gbc.gridx = 1; gbc.gridy = y; f.add(browseBtn, gbc);
        y++;

        gbc.gridx = 1; gbc.gridy = y; f.add(saveBtn, gbc);

        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(f) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                photoField.setText(file.getAbsolutePath());
            }
        });

        saveBtn.addActionListener(e -> {
            String idText = idField.getText().trim();
            String name = nameField.getText().trim();
            String ageText = ageField.getText().trim();
            String medicine = medicineField.getText().trim();
            String photo = photoField.getText().trim();

            // Validate
            if (idText.isEmpty()) { JOptionPane.showMessageDialog(f, "❌ ID cannot be empty."); return; }
            int id;
            try { id = Integer.parseInt(idText); if (id <= 0) throw new NumberFormatException(); }
            catch (NumberFormatException ex) { JOptionPane.showMessageDialog(f, "❌ Invalid ID. Enter a positive number."); return; }

            if (name.isEmpty()) { JOptionPane.showMessageDialog(f, "❌ Name cannot be empty."); return; }

            int age = 0;
            try {
                if (!ageText.isEmpty()) {
                    age = Integer.parseInt(ageText);
                    if (age <= 0) { JOptionPane.showMessageDialog(f, "❌ Age must be positive."); return; }
                }
            } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(f, "❌ Invalid age."); return; }

            // Insert (check duplicate id)
            try (Connection c = DriverManager.getConnection(URL)) {
                PreparedStatement check = c.prepareStatement("SELECT COUNT(*) FROM patients WHERE id = ?");
                check.setInt(1, id);
                ResultSet rs = check.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(f, "❌ ID already exists. Choose a different ID.");
                    return;
                }

                String sql = "INSERT INTO patients (id, name, age, disease, medicine, curing_time, doctor, photo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = c.prepareStatement(sql);
                ps.setInt(1, id);
                ps.setString(2, name);
                if (age == 0) ps.setNull(3, Types.INTEGER); else ps.setInt(3, age);
                ps.setString(4, (String) diseaseBox.getSelectedItem());
                ps.setString(5, medicine.isEmpty() ? null : medicine);
                ps.setString(6, (String) curingBox.getSelectedItem());
                ps.setString(7, (String) doctorBox.getSelectedItem());
                ps.setString(8, photo.isEmpty() ? null : photo);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(f, "✅ Patient added successfully! ID: " + id);

                // clear + set next id
                nameField.setText("");
                ageField.setText("");
                medicineField.setText("");
                photoField.setText("");
                idField.setText(String.valueOf(getNextPatientId()));

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(f, "❌ Error: " + ex.getMessage());
            }
        });

        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    // --- View One Patient by ID (shows photo if path valid) ---
    private void openViewPatientPage() {
        JFrame f = new JFrame("🔍 View Patient by ID");
        f.setSize(540, 420);
        f.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = new JTextField(6);
        JButton searchBtn = new JButton("Search");
        JTextArea result = new JTextArea(12, 30);
        result.setEditable(false);
        JLabel photoLabel = new JLabel();

        gbc.gridx = 0; gbc.gridy = 0; f.add(new JLabel("Enter Patient ID:"), gbc);
        gbc.gridx = 1; f.add(idField, gbc);
        gbc.gridx = 2; f.add(searchBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        f.add(new JScrollPane(result), gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        f.add(photoLabel, gbc);

        searchBtn.addActionListener(e -> {
            String idText = idField.getText().trim();
            if (idText.isEmpty()) { JOptionPane.showMessageDialog(f, "❌ Enter ID"); return; }
            int id;
            try { id = Integer.parseInt(idText); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(f, "❌ Invalid ID"); return; }

            try (Connection c = DriverManager.getConnection(URL)) {
                PreparedStatement ps = c.prepareStatement("SELECT * FROM patients WHERE id = ?");
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("ID: ").append(rs.getInt("id")).append("\n");
                    sb.append("Name: ").append(rs.getString("name")).append("\n");
                    sb.append("Age: ").append(rs.getObject("age")).append("\n");
                    sb.append("Disease: ").append(rs.getString("disease")).append("\n");
                    sb.append("Medicine: ").append(rs.getString("medicine")).append("\n");
                    sb.append("Curing Time: ").append(rs.getString("curing_time")).append("\n");
                    sb.append("Doctor: ").append(rs.getString("doctor")).append("\n");
                    sb.append("Photo Path: ").append(rs.getString("photo")).append("\n");
                    result.setText(sb.toString());

                    String path = rs.getString("photo");
                    if (path != null && !path.isEmpty()) {
                        File imgFile = new File(path);
                        if (imgFile.exists()) {
                            ImageIcon icon = new ImageIcon(new ImageIcon(path).getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH));
                            photoLabel.setIcon(icon);
                            photoLabel.setText("");
                        } else {
                            photoLabel.setIcon(null);
                            photoLabel.setText("Photo file not found");
                        }
                    } else {
                        photoLabel.setIcon(null);
                        photoLabel.setText("No photo");
                    }
                } else {
                    JOptionPane.showMessageDialog(f, "❌ Patient not found!");
                    result.setText("");
                    photoLabel.setIcon(null);
                    photoLabel.setText("");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(f, "❌ Error: " + ex.getMessage());
            }
        });

        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    // --- View All Patients (table) ---
    private void openViewAllPatientsPage() {
        JFrame f = new JFrame("📋 All Patients");
        f.setSize(820, 420);

        String[] cols = {"ID", "Name", "Age", "Disease", "Medicine", "Curing Time", "Doctor", "Photo"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);

        try (Connection c = DriverManager.getConnection(URL);
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM patients ORDER BY id")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getObject("age"),
                        rs.getString("disease"),
                        rs.getString("medicine"),
                        rs.getString("curing_time"),
                        rs.getString("doctor"),
                        rs.getString("photo")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Error loading patients: " + ex.getMessage());
        }

        f.add(new JScrollPane(table));
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    // --- Book Appointment Page ---
    private void openAppointmentPage() {
        JFrame f = new JFrame("📅 Book Appointment");
        f.setSize(480, 320);
        f.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = new JTextField(8);
        JTextField dateField = new JTextField(10); // YYYY-MM-DD
        JTextField timeField = new JTextField(6);  // HH:MM
        JButton bookBtn = new JButton("Book");
        JTextArea result = new JTextArea(8, 30);
        result.setEditable(false);

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; f.add(new JLabel("Patient ID:"), gbc);
        gbc.gridx = 1; f.add(idField, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; f.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; f.add(dateField, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; f.add(new JLabel("Time (HH:MM):"), gbc);
        gbc.gridx = 1; f.add(timeField, gbc);
        y++;

        gbc.gridx = 1; gbc.gridy = y; f.add(bookBtn, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; f.add(new JScrollPane(result), gbc);

        bookBtn.addActionListener(e -> {
            String idText = idField.getText().trim();
            String date = dateField.getText().trim();
            String time = timeField.getText().trim();

            if (idText.isEmpty()) { JOptionPane.showMessageDialog(f, "❌ Enter Patient ID."); return; }
            int patientId;
            try { patientId = Integer.parseInt(idText); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(f, "❌ Invalid ID."); return; }

            if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) { JOptionPane.showMessageDialog(f, "❌ Date must be YYYY-MM-DD"); return; }
            if (!time.matches("\\d{2}:\\d{2}")) { JOptionPane.showMessageDialog(f, "❌ Time must be HH:MM"); return; }

            try (Connection c = DriverManager.getConnection(URL)) {
                PreparedStatement check = c.prepareStatement("SELECT COUNT(*) FROM patients WHERE id = ?");
                check.setInt(1, patientId);
                ResultSet rsCheck = check.executeQuery();
                if (rsCheck.next() && rsCheck.getInt(1) == 0) {
                    JOptionPane.showMessageDialog(f, "❌ Patient ID does not exist.");
                    return;
                }

                PreparedStatement ps = c.prepareStatement("INSERT INTO appointments (patient_id, appointment_date, appointment_time) VALUES (?, ?, ?)");
                ps.setInt(1, patientId);
                ps.setString(2, date);
                ps.setString(3, time);
                ps.executeUpdate();

                result.setText("✅ Appointment booked for patient ID: " + patientId + "\n");

                // list appointments on that date
                PreparedStatement list = c.prepareStatement(
                        "SELECT patients.id, patients.name, appointment_time FROM appointments " +
                                "JOIN patients ON appointments.patient_id = patients.id " +
                                "WHERE appointment_date = ? ORDER BY appointment_time");
                list.setString(1, date);
                ResultSet rs = list.executeQuery();
                result.append("\nAppointments on " + date + ":\n");
                while (rs.next()) {
                    result.append("ID: " + rs.getInt("id") + " - " + rs.getString("name") + " at " + rs.getString("appointment_time") + "\n");
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(f, "❌ Error: " + ex.getMessage());
            }
        });

        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            // optional but safe: load driver if available
            Class.forName("org.sqlite.JDBC");
        } catch (Exception ignored) {}

        initDatabase();
        SwingUtilities.invokeLater(HospitalApp::new);
    }
}
