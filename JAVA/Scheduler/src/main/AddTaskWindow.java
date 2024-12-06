package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class AddTaskWindow extends JFrame {
    private JTextField taskNameField, assignedToField, dueDateField;

    public AddTaskWindow(Dashboard dashboard) {
        setTitle("Add/Assign Task");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(5, 2, 10, 10));
        getContentPane().setBackground(new Color(230, 240, 255));

        // Task Name
        JLabel taskNameLabel = new JLabel("Task Name:");
        taskNameField = new JTextField();
        add(taskNameLabel);
        add(taskNameField);

        // Assigned To
        JLabel assignedToLabel = new JLabel("Assign To (Email):");
        assignedToField = new JTextField();
        add(assignedToLabel);
        add(assignedToField);

        // Due Date
        JLabel dueDateLabel = new JLabel("Due Date (YYYY-MM-DD):");
        dueDateField = new JTextField();
        add(dueDateLabel);
        add(dueDateField);

        // Buttons
        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(e -> addTask(dashboard));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        add(addButton);
        add(cancelButton);
    }

    private void addTask(Dashboard dashboard) {
        String taskName = taskNameField.getText().trim();
        String assignedTo = assignedToField.getText().trim();
        String dueDate = dueDateField.getText().trim();

        // Validations
        if (taskName.isEmpty() || assignedTo.isEmpty() || dueDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO tasks (task_name, assigned_to, due_date, status) VALUES (?, ?, ?, 'Pending')")) {
            stmt.setString(1, taskName);
            stmt.setString(2, assignedTo);
            stmt.setDate(3, Date.valueOf(dueDate));
            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                sendEmailNotification(assignedTo, taskName, dueDate);
                JOptionPane.showMessageDialog(this, "Task assigned successfully!");
                dashboard.loadTasks(); // Refresh Dashboard
                dispose(); // Close the Add Task Window
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendEmailNotification(String toEmail, String taskName, String dueDate) {
        String fromEmail = "your_email@gmail.com";
        String password = "your_email_password";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("New Task Assigned: " + taskName);
            message.setText("You have been assigned a new task:\n\nTask: " + taskName + "\nDue Date: " + dueDate);

            Transport.send(message);
        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Failed to send email notification: " + e.getMessage());
        }
    }
}
