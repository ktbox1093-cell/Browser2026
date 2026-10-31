package com.nokia.browser;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import java.io.*;

public class BrowserMIDlet extends MIDlet implements CommandListener {
    private Display display;
    private Form form;
    private TextField urlField;
    private StringItem resultItem;
    private Command exitCommand, goCommand;

    // Recuerda cambiar esto más adelante por tu URL real de Render
    private String proxyBaseUrl = "https://tu-backend.onrender.com/browse?url=";

    public BrowserMIDlet() {
        display = Display.getDisplay(this);
        
        exitCommand = new Command("Salir", Command.EXIT, 0);
        goCommand = new Command("Cargar", Command.SCREEN, 1);

        urlField = new TextField("URL Web:", "https://", 150, TextField.URL);
        resultItem = new StringItem("Contenido:", "Introduce una URL y presiona Cargar.");

        form = new Form("E72 Browser");
        form.append(urlField);
        form.append(resultItem);
        
        form.addCommand(exitCommand);
        form.addCommand(goCommand);
        form.setCommandListener(this);
    }

    public void startApp() {
        display.setCurrent(form);
    }

    public void pauseApp() {}

    public void destroyApp(boolean unconditional) {}

    public void commandAction(Command c, Displayable d) {
        if (c == exitCommand) {
            destroyApp(true);
            notifyDestroyed();
        } else if (c == goCommand) {
            String targetUrl = urlField.getString();
            resultItem.setText("Cargando...");
            new Thread(new FetchTask(targetUrl)).start();
        }
    }

    private class FetchTask implements Runnable {
        private String targetUrl;

        public FetchTask(String url) {
            this.targetUrl = url;
        }

        public void run() {
            HttpConnection conn = null;
            InputStream is = null;
            try {
                String fullUrl = proxyBaseUrl + targetUrl;
                conn = (HttpConnection) Connector.open(fullUrl);
                conn.setRequestMethod(HttpConnection.GET);

                int status = conn.getResponseCode();
                if (status == HttpConnection.HTTP_OK) {
                    is = conn.openInputStream();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    int ch;
                    while ((ch = is.read()) != -1) {
                        bos.write(ch);
                    }
                    String responseString = new String(bos.toByteArray(), "UTF-8");
                    resultItem.setText(responseString);
                } else {
                    resultItem.setText("Error HTTP: " + status);
                }
            } catch (Exception e) {
                resultItem.setText("Error de red: " + e.getMessage());
            } finally {
                try {
                    if (is != null) is.close();
                    if (conn != null) conn.close();
                } catch (IOException e) {}
            }
        }
    }
}

