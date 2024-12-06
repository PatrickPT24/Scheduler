package main;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.table.*;
import java.sql.*;

public class Dashboard extends JFrame {
    private String userEmail;
    private JTable taskTable;
    private JLabel statusBar;

    public Dashboard(String email) {
        this.userEmail = email;
        setTitle("Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Welcome Label
        JLabel welcomeLabel = new JLabel("Welcome, " + email);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(welcomeLabel, BorderLayout.NORTH);

        // Task Table
        taskTable = new JTable();
        add(new JScrollPane(taskTable), BorderLayout.CENTER);

        // Filter Dropdown
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel filterLabel = new JLabel("Filter by Status:");
        JComboBox<String> filterDropdown = new JComboBox<>(new String[]{"All", "Pending", "Completed"});
        filterDropdown.addActionListener(e -> filterTasks(filterDropdown.getSelectedItem().toString()));
        filterPanel.add(filterLabel);
        filterPanel.add(filterDropdown);
        add(filterPanel, BorderLayout.WEST);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton markCompleteButton = new JButton("Mark as Complete");
        markCompleteButton.addActionListener(this::markTaskComplete);
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        buttonPanel.add(markCompleteButton);
        buttonPanel.add(logoutButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add Button to Launch AddTaskWindow
        JButton addTaskButton = new JButton("Add/Assign Task");
        addTaskButton.addActionListener(e -> new AddTaskWindow(this).setVisible(true));
        buttonPanel.add(addTaskButton);
        
        // Status Bar
        statusBar = new JLabel("Total Tasks: 0");
        statusBar.setHorizontalAlignment(SwingConstants.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        // Load Tasks
        loadTasks();
    }

    void loadTasks() {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM tasks WHERE assigned_to = ?")) {
            stmt.setString(1, userEmail);

            ResultSet rs = stmt.executeQuery();
            DefaultTableModel model = new DefaultTableModel(
                new String[]{"Task Name", "Assigned Date", "Due Date", "Status", "Assigned To"}, 0);

            int taskCount = 0;
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("task_name"),
                    rs.getDate("assigned_date"),
                    rs.getDate("due_date"),
                    rs.getString("status"),
                    rs.getString("assigned_to")
                });
                taskCount++;
            }

            taskTable.setModel(model);
            statusBar.setText("Total Tasks: " + taskCount);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void filterTasks(String status) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM tasks WHERE assigned_to = ? AND (status = ? OR ? = 'All')")) {
            stmt.setString(1, userEmail);
            stmt.setString(2, status);
            stmt.setString(3, status);

            ResultSet rs = stmt.executeQuery();
            DefaultTableModel model = new DefaultTableModel(
                new String[]{"Task Name", "Assigned Date", "Due Date", "Status", "Assigned To"}, 0);

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("task_name"),
                    rs.getDate("assigned_date"),
                    rs.getDate("due_date"),
                    rs.getString("status"),
                    rs.getString("assigned_to")
                });
            }

            taskTable.setModel(model);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error filtering tasks: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void markTaskComplete(ActionEvent e) {
        int selectedRow = taskTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a task to mark as complete!");
            return;
        }

        String taskName = taskTable.getValueAt(selectedRow, 0).toString();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE tasks SET status = 'Completed' WHERE task_name = ? AND assigned_to = ?")) {
            stmt.setString(1, taskName);
            stmt.setString(2, userEmail);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Task marked as complete!");
                loadTasks();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error marking task complete: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            new Login().setVisible(true);
            this.dispose();
        }
    }
}
