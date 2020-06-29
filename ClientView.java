package uk.chat.client;

import uk.chat.network.TCPConnection;
import uk.chat.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientView extends JFrame implements ActionListener, TCPConnectionListener {
    private static final String IP_ADDRESS = "fe80::6c97:f018:d543:ec52%15";
    private static final int PORT = 22;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private final JTextArea log = new JTextArea();
    private final JTextField nickName = new JTextField("Alan");
    private final JTextField input = new JTextField();
    private TCPConnection connection;


    private ClientView(){
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      setSize(WIDTH, HEIGHT);
      setLocationRelativeTo(null);
      setAlwaysOnTop(true);
      setBackground(Color.BLACK);

      log.setEditable(false);
      log.setLineWrap(true);
      add(log, BorderLayout.CENTER);

      input.addActionListener(this);
      add(input, BorderLayout.SOUTH);
      add(nickName, BorderLayout.NORTH);

      setVisible(true);
        try {
            connection = new TCPConnection(this, IP_ADDRESS, PORT);
        } catch (IOException e) {
            printMessage("Connection exception: " + e);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientView::new);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String message = input.getText();
        if(message.equals("")) return;

        input.setText(null);
        connection.sendMessage(nickName.getText() + ": " + message);
    }


    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMessage("Connection is ready...");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
       printMessage(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
       printMessage("Connection is close");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
          printMessage("Connection exception: " + e);
    }

    private synchronized void printMessage(String message){
        SwingUtilities.invokeLater(() -> {
            log.append(message + "\n");
            log.setCaretPosition(log.getDocument().getLength());
        });
    }
}
