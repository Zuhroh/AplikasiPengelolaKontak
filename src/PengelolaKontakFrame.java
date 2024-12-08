
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class PengelolaKontakFrame extends javax.swing.JFrame {
    public PengelolaKontakFrame() {
        initComponents();
        setSize(500, 700);
        setLocationRelativeTo(null);
        
         // Initialize the table model with columns
        tbl = new DefaultTableModel(new String[]{"ID", "Nama", "Nomor Hp", "Category"}, 0);
        tableKontak.setModel(tbl); // Set the model to Table
        getTable(); 
    }
    
    private final DefaultTableModel tbl;
    // Koneksi ke database
    GetConnection getConnection = new GetConnection();
    Connection conn = getConnection.connect();

    public void add() {
        // Mengambil data dari input form
        String nama = nmKontak.getText();  // Mengambil teks dari JTextField nmKontak
        String nohp = noHp.getText();      // Mengambil teks dari JTextField noHp
        String box = (String) categoryBox.getSelectedItem();  // Mengambil item yang dipilih dari JComboBox

        // Memeriksa apakah nama, nomor telepon, atau kategori kosong
        if (nama.isEmpty() || nohp.isEmpty() || box == null || box.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, phone, and category cannot be empty.");
            return;  // Menghentikan eksekusi jika ada input yang kosong
        }

        // SQL untuk memasukkan data ke tabel db_contacts
        String sql = "INSERT INTO db_contacts(nama, noHp, category) VALUES(?, ?, ?)";  // Pastikan nama tabel sesuai

        // Menyisipkan data menggunakan PreparedStatement
        try {
            // Memeriksa apakah koneksi ke database valid
            if (conn == null || conn.isClosed()) {
                System.out.println("Error: Connection is not established.");
                JOptionPane.showMessageDialog(this, "Database connection error.");
                return;  // Menghentikan eksekusi jika koneksi tidak valid
            }

            // Menyisipkan data ke dalam database
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, nama);  // Set nama
                pstmt.setString(2, nohp);  // Set noHp
                pstmt.setString(3, box);   // Set category
                pstmt.executeUpdate();  // Menjalankan query insert

                // Memberikan feedback sukses
                JOptionPane.showMessageDialog(this, "Data berhasil ditambahkan.");
                nmKontak.requestFocus();
                getTable();  // Memperbarui tampilan tabel setelah data ditambahkan
            }
        } catch (SQLException e) {
            // Menangani kesalahan SQL dan mencetak pesan kesalahan
            System.out.println("Error: " + e.getMessage());
            // Menampilkan stack trace untuk debugging
            JOptionPane.showMessageDialog(this, "Error adding data to the database.");
        }
    }

    
    // Method untuk mengedit PengelolaKontakFrame
    private void edit() {
        int row = tableKontak.getSelectedRow();  // Pastikan ini sesuai dengan nama JTable yang kamu gunakan
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a contact to edit.");
            return;
        }

        // Mendapatkan ID dari PengelolaKontakFrame yang dipilih di tabel
        String id = tableKontak.getValueAt(row, 0).toString(); // Ganti 'tbl' menjadi 'tableKontak'
        String name = nmKontak.getText();
        String phone = noHp.getText();
        String category = (String) categoryBox.getSelectedItem(); // Menggunakan getSelectedItem() untuk JComboBox

        // Validasi input
        if (name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and phone cannot be empty.");
            return;
        } 

        // SQL untuk mengupdate data PengelolaKontakFrame
        String sql = "UPDATE db_contacts SET nama = ?, noHp = ?, category = ? WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, category);
            pstmt.setInt(4, Integer.parseInt(id));  // Menyisipkan ID PengelolaKontakFrame
            pstmt.executeUpdate(); // Menjalankan query update
            getTable(); // Memperbarui tampilan tabel

            // Mengosongkan field setelah update
            nmKontak.setText("");
            noHp.setText("");
            categoryBox.setSelectedIndex(0);  // Reset kategori jika menggunakan JComboBox
             JOptionPane.showMessageDialog(this, "Contact Edited successfully.");
            btnEdit.setEnabled(false);
            btnHapus.setEnabled(false);
            btnAdd.setEnabled(true);
            nmKontak.requestFocus();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage()); // Menangani kesalahan SQL
        }
    }

    // Method untuk menghapus PengelolaKontakFrame
    private void delete() {
        // Memastikan ada baris yang dipilih untuk dihapus
        int row = tableKontak.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a contact to delete.");
            return;  // Menghentikan eksekusi jika tidak ada yang dipilih
        }

        // Mendapatkan ID dari baris yang dipilih di tabel
        String id = tableKontak.getValueAt(row, 0).toString();  // Ganti 'tbl' menjadi 'tableKontak'

        // SQL query untuk menghapus data berdasarkan ID
        String sql = "DELETE FROM db_contacts WHERE id = ?";

        // Menghapus data dari database
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set parameter ID yang akan dihapus
            pstmt.setInt(1, Integer.parseInt(id));

            // Eksekusi query untuk menghapus data
            int affectedRows = pstmt.executeUpdate();

            // Jika ada baris yang terpengaruh (data dihapus)
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Contact deleted successfully.");
                btnEdit.setEnabled(false);
                btnHapus.setEnabled(false);
                btnAdd.setEnabled(true);
                getTable();  // Memperbarui tampilan tabel setelah penghapusan
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete contact.");
            }

            // Membersihkan form input setelah penghapusan
            nmKontak.setText("");
            noHp.setText("");
            categoryBox.setSelectedIndex(0);
            nmKontak.requestFocus();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());  // Menangani error jika terjadi masalah dengan SQL
        }
    }

    private void getTable() {
        // Pastikan tableModel sudah diinisialisasi
        if (tbl == null) {
            System.err.println("Error: tableModel is still not initialized in getTable.");
            return;
        }

        String sql = "SELECT * FROM db_contacts";
        try (Statement stmt = conn.createStatement(); // Menggunakan koneksi yang sudah ada
             ResultSet rs = stmt.executeQuery(sql)) {

            tbl.setRowCount(0); // Clear existing data

            // Menambahkan baris ke dalam tableModel
            while (rs.next()) {
                tbl.addRow(new Object[]{
                    rs.getInt("id"), 
                    rs.getString("nama"), 
                    rs.getString("noHp"), 
                    rs.getString("category")
                });
            }

        } catch (SQLException e) {
            System.err.println("Error while fetching data: " + e.getMessage()); // Pesan error lebih jelas
        }
    }
    
    private void searchDb() {
        String searchQuery = cariData.getText();  // Mendapatkan input dari JTextField
        if (searchQuery.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Search data cannot be empty.");
            return;
        }

    // Membuat query SQL yang mencakup pencarian berdasarkan Nama dan Telepon
        String sql = "SELECT * FROM db_contacts WHERE nama LIKE ? OR noHp LIKE ?";

        // Pastikan koneksi database conn tidak null
        if (conn == null) {
            JOptionPane.showMessageDialog(null, "Database connection is not established.");
            return;
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        // Mengatur parameter pencarian untuk Nama dan Telepon
        pstmt.setString(1, "%" + searchQuery + "%");
        pstmt.setString(2, "%" + searchQuery + "%");

        try (ResultSet rs = pstmt.executeQuery()) {
            tbl.setRowCount(0); // Clear existing data

            while (rs.next()) {
                tbl.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama"),
                    rs.getString("noHp"),
                    rs.getString("category")
                });
            }
        }
        } catch (SQLException e) {
            // Tangani SQLException dengan benar
            System.out.println("Error while searching contacts: " + e.getMessage());
        }
}

    private void exportToCSV() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan File CSV");
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                // Tambahkan ekstensi .csv jika pengguna tidak menambahkannya
            if (!fileToSave.getAbsolutePath().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }
            try (BufferedWriter writer = new BufferedWriter(new
                FileWriter(fileToSave))) {
                    writer.write("ID,Nama,Nomor Telepon,Kategori\n"); // Header CSV
                    for (int i = 0; i < tbl.getRowCount(); i++) {
                    writer.write(
                        tbl.getValueAt(i, 0) + "," +
                        tbl.getValueAt(i, 1) + "," +
                        tbl.getValueAt(i, 2) + "," +
                        tbl.getValueAt(i, 3) + "\n"
                    );
                }
                JOptionPane.showMessageDialog(this, "Data berhasil diekspor ke " + fileToSave.getAbsolutePath());
            } catch (IOException ex) {
                showError("Gagal menulis file: " + ex.getMessage());
            }
        }
    }
    private void showCSVGuide() {
        String guideMessage = """
                          Format CSV untuk impor data:
                          - Header wajib: ID, Nama, Nomor Telepon, Kategori
                          - ID dapat kosong (akan diisi otomatis)
                          - Nama dan Nomor Telepon wajib diisi
                          - Contoh isi file CSV:
                           1, Andi, 08123456789, Teman
                           2, Budi Doremi, 08567890123, Keluarga

                          Pastikan file CSV sesuai format sebelum melakukan impor.""";
        JOptionPane.showMessageDialog(this, guideMessage, "Panduan Format CSV", JOptionPane.INFORMATION_MESSAGE);
        }
        private boolean validateCSVHeader(String header) {
        return header != null && header.trim().equalsIgnoreCase("ID,Nama,Nomor Telepon,Kategori");
    }

    private void importFromCSV() throws FileNotFoundException, IOException {
        showCSVGuide();
        int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin file CSV yang dipilih sudah sesuai dengan format?", "Konfirmasi Impor CSV", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Pilih File CSV");
            int userSelection = fileChooser.showOpenDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToOpen = fileChooser.getSelectedFile();
                try (BufferedReader reader = new BufferedReader(new FileReader(fileToOpen))) {
                    String line = reader.readLine(); // Baca header
                    if (!validateCSVHeader(line)) {
                        JOptionPane.showMessageDialog(this, "Format header CSV tidak valid. Pastikan header adalah: ID,Nama,Nomor Telepon,Kategori", "Kesalahan CSV", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int rowCount = 0;
                    int errorCount = 0;
                    int duplicateCount = 0;
                    StringBuilder errorLog = new StringBuilder("Baris dengan kesalahan:\n");

                    while ((line = reader.readLine()) != null) {
                        rowCount++;
                        String[] data = line.split(",");

                        // Periksa apakah format kolom sesuai
                        if (data.length != 4) {
                            errorCount++;
                            errorLog.append("Baris ").append(rowCount).append(": Format kolom tidak sesuai.\n");
                            continue;
                        }

                        String nama = data[1].trim();
                        String nomorTelepon = data[2].trim();
                        String kategori = data[3].trim();

                        // Periksa jika nama atau nomor telepon kosong
                        if (nama.isEmpty() || nomorTelepon.isEmpty()) {
                            errorCount++;
                            errorLog.append("Baris ").append(rowCount).append(": Nama atau Nomor Telepon kosong.\n");
                            continue;
                        }

                        // Validasi nomor telepon
                        if (!validatePhoneNumber(nomorTelepon)) {
                            errorCount++;
                            errorLog.append("Baris ").append(rowCount).append(": Nomor Telepon tidak valid.\n");
                            continue;
                        }

                        // Periksa apakah PengelolaKontakFrame sudah ada di database
                        boolean isDuplicate = false;
                        String checkDuplicateSQL = "SELECT COUNT(*) FROM db_contacts WHERE nama = ? AND noHp = ?";
                        try (PreparedStatement pstmt = conn.prepareStatement(checkDuplicateSQL)) {
                            pstmt.setString(1, nama);
                            pstmt.setString(2, nomorTelepon);
                            ResultSet rs = pstmt.executeQuery();
                            if (rs.next() && rs.getInt(1) > 0) {
                                isDuplicate = true;
                                duplicateCount++;
                                errorLog.append("Baris ").append(rowCount).append(": Kontak sudah ada.\n");
                            }
                        } catch (SQLException ex) {
                            errorCount++;
                            errorLog.append("Baris ").append(rowCount).append(": Error pengecekan duplikat - ").append(ex.getMessage()).append("\n");
                        }

                        // Jika tidak duplikat, tambahkan PengelolaKontakFrame ke database
                        if (!isDuplicate) {
                            String insertSQL = "INSERT INTO db_contacts(nama, noHp, category) VALUES(?, ?, ?)";
                            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                                pstmt.setString(1, nama);
                                pstmt.setString(2, nomorTelepon);
                                pstmt.setString(3, kategori);
                                pstmt.executeUpdate();
                            } catch (SQLException ex) {
                                errorCount++;
                                errorLog.append("Baris ").append(rowCount).append(": Gagal menyimpan ke database - ").append(ex.getMessage()).append("\n");
                            }
                        }
                    }

                    // Memuat ulang PengelolaKontakFrame setelah impor
                    getTable();

                    // Tampilkan pesan kesalahan atau sukses
                    if (errorCount > 0 || duplicateCount > 0) {
                        errorLog.append("\nTotal baris dengan kesalahan: ").append(errorCount).append("\n");
                        errorLog.append("Total baris duplikat: ").append(duplicateCount).append("\n");
                        JOptionPane.showMessageDialog(this, errorLog.toString(), "Kesalahan Impor", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Semua data berhasil diimpor.");
                    }

                } catch (IOException ex) {
                    showError("Gagal membaca file: " + ex.getMessage());
                }
            }
        }
    }

    private boolean validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nomor telepon tidak boleh kosong.");
        return false;
        }
        if (!phoneNumber.matches("\\d+")) { // Hanya angka
            JOptionPane.showMessageDialog(this, "Nomor telepon hanya boleh berisi angka.");
        return false;
        }
        if (phoneNumber.length() < 8 || phoneNumber.length() > 15) { //Panjang 8-15
            JOptionPane.showMessageDialog(this, "Nomor telepon harus memiliki panjang antara 8 hingga 15 karakter.");
        return false;
        }
        return true;
    }
        private void filter(java.awt.event.KeyEvent evt) {
        char c = evt.getKeyChar();  // Get the character from the event

        // Check if the character is a digit or special keys like backspace or delete
        if (!(Character.isDigit(c) || c == KeyEvent.VK_BACK_SPACE || c == KeyEvent.VK_DELETE)) {
            Toolkit.getDefaultToolkit().beep();  // Play beep sound
            JOptionPane.showMessageDialog(null, "Masukkan hanya angka!"); // Inform user only numbers are allowed
            evt.consume();  // Consume the event to prevent further processing
        }

        // Check the length of the text in the phone field
        JTextField source = (JTextField) evt.getSource();
        if (source.getText().length() >= 12) {
            Toolkit.getDefaultToolkit().beep();  // Play beep sound
            JOptionPane.showMessageDialog(null, "Masukkan maksimal 12 angka saja!");
            source.setText("");
            evt.consume();  // Consume the event to prevent additional characters
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnAdd = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        categoryBox = new javax.swing.JComboBox<>();
        noHp = new javax.swing.JTextField();
        cariData = new javax.swing.JTextField();
        nmKontak = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableKontak = new javax.swing.JTable();
        export = new javax.swing.JButton();
        imports = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Aplikasi Pengelola Kontak");

        jPanel2.setBackground(new java.awt.Color(0, 102, 102));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "APLIKASI PENGELOLAAN KONTAK", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Century Gothic", 1, 18), new java.awt.Color(255, 255, 255))); // NOI18N
        jPanel2.setLayout(new java.awt.GridBagLayout());

        jLabel1.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        jLabel1.setText("Nama Kontak");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 92);
        jPanel2.add(jLabel1, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        jLabel2.setText("Nomor Telepon");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 92);
        jPanel2.add(jLabel2, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        jLabel3.setText("Pencarian");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 92);
        jPanel2.add(jLabel3, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        jLabel4.setText("Kategori");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 92);
        jPanel2.add(jLabel4, gridBagConstraints);

        btnAdd.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        btnAdd.setText("Tambah");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 11);
        jPanel2.add(btnAdd, gridBagConstraints);

        btnEdit.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        btnEdit.setText("Edit");
        btnEdit.setEnabled(false);
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 11);
        jPanel2.add(btnEdit, gridBagConstraints);

        btnHapus.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        btnHapus.setText("Hapus");
        btnHapus.setEnabled(false);
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 11);
        jPanel2.add(btnHapus, gridBagConstraints);

        categoryBox.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        categoryBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Keluarga", "Teman", "Kantor" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 9;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 11);
        jPanel2.add(categoryBox, gridBagConstraints);

        noHp.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        noHp.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                noHpKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 9;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 11);
        jPanel2.add(noHp, gridBagConstraints);

        cariData.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        cariData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cariDataActionPerformed(evt);
            }
        });
        cariData.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                cariDataKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 10;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 11);
        jPanel2.add(cariData, gridBagConstraints);

        nmKontak.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        nmKontak.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nmKontakKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipady = 9;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 11, 11);
        jPanel2.add(nmKontak, gridBagConstraints);

        tableKontak.setFont(new java.awt.Font("Century Gothic", 0, 12)); // NOI18N
        tableKontak.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tableKontak.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableKontakMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tableKontak);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 482, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 226, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipady = 50;
        jPanel2.add(jPanel3, gridBagConstraints);

        export.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        export.setText("Export");
        export.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(export, gridBagConstraints);

        imports.setFont(new java.awt.Font("Century Gothic", 1, 12)); // NOI18N
        imports.setText("Import");
        imports.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel2.add(imports, gridBagConstraints);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportActionPerformed
        // TODO add your handling code here:
        exportToCSV();
    }//GEN-LAST:event_exportActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // Memanggil metode add dengan parameter nama, nohp, dan box
        add();
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        edit();
    }//GEN-LAST:event_btnEditActionPerformed

    private void tableKontakMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableKontakMouseClicked
        // TODO add your handling code here:
         int row = tableKontak.getSelectedRow();
        nmKontak.setText(tableKontak.getValueAt(row,1).toString());
        noHp.setText(tableKontak.getValueAt(row,2).toString());
        categoryBox.setSelectedItem(tableKontak.getValueAt(row,3).toString());
        btnEdit.setEnabled(true);
        btnHapus.setEnabled(true);
        btnAdd.setEnabled(false);
    }//GEN-LAST:event_tableKontakMouseClicked

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        // TODO add your handling code here:
        delete();
    }//GEN-LAST:event_btnHapusActionPerformed

    private void nmKontakKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nmKontakKeyReleased
        // TODO add your handling code here:
        
    }//GEN-LAST:event_nmKontakKeyReleased

    private void noHpKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_noHpKeyReleased
        // TODO add your handling code here:
        filter(evt);
    }//GEN-LAST:event_noHpKeyReleased

    private void cariDataKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cariDataKeyReleased
        // TODO add your handling code here:
        if (!cariData.getText().isEmpty()){
        searchDb();
        } else {
        getTable();
        }
    }//GEN-LAST:event_cariDataKeyReleased

    private void cariDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cariDataActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cariDataActionPerformed

    private void importsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importsActionPerformed
        try {
            // TODO add your handling code here:
            importFromCSV();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(PengelolaKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_importsActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PengelolaKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new PengelolaKontakFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnHapus;
    private javax.swing.JTextField cariData;
    private javax.swing.JComboBox<String> categoryBox;
    private javax.swing.JButton export;
    private javax.swing.JButton imports;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField nmKontak;
    private javax.swing.JTextField noHp;
    private javax.swing.JTable tableKontak;
    // End of variables declaration//GEN-END:variables

    private void showError(String string) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
