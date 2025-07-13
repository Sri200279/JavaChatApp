import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ChatClient {
    private JFrame frame;

    private JTextField textField;
    private JTextArea textArea;
    private PrintWriter out;

    public ChatClient(String serverAddress) {
        frame = new JFrame("Private Chat Client");
        textField = new JTextField(40);
        textArea = new JTextArea(20, 50);
        textArea.setEditable(false);

        frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.pack();

        textField.addActionListener(e -> {
            out.println(textField.getText());
            textField.setText("");
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try {
            Socket socket = new Socket(serverAddress, 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Handle server protocol
            while (true) {
                String line = in.readLine();
                if (line == null) break;

                if (line.startsWith("SUBMITNAME")) {
                    String username = JOptionPane.showInputDialog(frame, "Choose a username:", "Login", JOptionPane.PLAIN_MESSAGE);
                    out.println(username);
                } else if (line.startsWith("NAMEEXISTS")) {
                    JOptionPane.showMessageDialog(frame, "Username already exists. Try another.");
                    frame.dispose();
                    return;
                } else if (line.startsWith("NAMEACCEPTED")) {
                    textField.setEditable(true);
                    break;
                }
            }

            // Receive messages
            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        textArea.append(line + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Unable to connect to server.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

  
    public static void main(String[] args) {
        new ChatClient("0.0.0.0");
    }
}
