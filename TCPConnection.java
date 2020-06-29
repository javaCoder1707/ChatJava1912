package uk.chat.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class TCPConnection {
      private final Socket socket;
      private final Thread thread;
      private final TCPConnectionListener eventListener;
      private final BufferedReader reader;
      private final BufferedWriter writer;

      public TCPConnection(TCPConnectionListener eventListener, String ipAddress, int port) throws IOException{
          this(eventListener, new Socket(ipAddress,port));
      }

      public TCPConnection(TCPConnectionListener eventListener ,Socket socket) throws IOException {
          this.eventListener = eventListener;
          this.socket = socket;
          reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
          writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
          thread = new Thread(new Runnable() {
              @Override
              public void run() {
                  try {
                      eventListener.onConnectionReady(TCPConnection.this);
                      while (!thread.isInterrupted()){
                          eventListener.onReceiveString(TCPConnection.this, reader.readLine());
                      }

                  } catch (IOException e) {
                      eventListener.onException(TCPConnection.this, e);
                  }finally {
                      eventListener.onDisconnect(TCPConnection.this);
                  }
              }
          });
          thread.start();
      }

      public synchronized void sendMessage(String message){
          try {
              writer.write(message + "\r\n");
              writer.flush();
          } catch (IOException e) {
              eventListener.onException(TCPConnection.this, e);
              disconnect();
          }
      }

      public synchronized void disconnect(){
          thread.interrupt();
          try {
              socket.close();
          } catch (IOException e) {
              eventListener.onException(TCPConnection.this, e);
          }
      }

    @Override
    public String toString() {
        return "TCPConnection: " + socket.getInetAddress() + ": " + socket.getPort();
    }
}
