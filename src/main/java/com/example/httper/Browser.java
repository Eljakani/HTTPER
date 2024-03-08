package com.example.httper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import okhttp3.*;

class ServerThread implements Runnable {
    private int port = 8080;
    private boolean running = false;

    public ServerThread(int port) {
        this.port = port;
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server listening on port " + port);
            running = true;
            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                    if (line.isEmpty()) {
                        break;
                    }
                }
                String response = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/html\r\n"
                        + "\r\n"
                        + "Hello From HTTPer!\r\n"
                        + "@Eljakani";
                OutputStream out = socket.getOutputStream();
                out.write(response.getBytes());
                out.flush();
                socket.close();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
    }
}

public class Browser extends Application {

    @FXML
    private TextField urlTextField;
    @FXML
    private TextArea ResponseOverview;
    @FXML
    private TextArea responseHeaders;
    @FXML
    private TextArea responseBody;
    @FXML
    private Circle serverStatus;
    @FXML
    private Button serverToggle;

    private ServerThread serverThread;



    public void ServerStatusToggle() {
        if (serverStatus.getFill().toString().equals(javafx.scene.paint.Color.GREEN.toString())) {
            serverThread.stop();
            serverThread = null;
            serverToggle.setText("Start Server");
            serverStatus.setFill(Color.RED);
        } else {
            serverThread = new ServerThread(8080);
            new Thread(serverThread).start();
            serverStatus.setFill(Color.GREEN);
            serverToggle.setText("Stop Server");
        }


    }
    public void LoadFromUrl (){
        System.out.println("Sending GET request to " + urlTextField.getText());
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(urlTextField.getText())
                .method("GET", null)
                .build();
        try {

            Response response = client.newCall(request).execute();
            ResponseOverview.setText(response.toString());
            responseHeaders.setText(response.headers().toString());
            responseBody.setText(response.body().string());

        } catch (Exception e) {
            if (e.getMessage().contains("Failed to connect to")) {
                ResponseOverview.setText("Failed to connect to " + urlTextField.getText());
            } else {
                ResponseOverview.setText(e.getMessage());
            }
            responseHeaders.setText("");
            responseBody.setText("");
        }


     }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("browser.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("HTTPer");
        stage.setScene(scene);

        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }

}
