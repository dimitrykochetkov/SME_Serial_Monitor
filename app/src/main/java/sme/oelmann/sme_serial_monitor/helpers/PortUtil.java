package sme.oelmann.sme_serial_monitor.helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;
import sme.oelmann.sme_serial_monitor.R;

public class PortUtil {
    private Context context;

    private boolean rwAllowed = false;
    private String openedPort = "", defaultPort = "";

    private InputStream inputStream;
    private OutputStream outputStream;
    private ReadThread readThread;
    private SerialPort serialPort;

    public PortUtil(Context context){
        this.context = context;
        checkDefaultPort(context.getString(R.string.default_port));
    }

    public String[] getPorts() {
        return countPorts();
    }

    public String getOpenedPort() {
        return openedPort;
    }

    public String getDefaultPort() {
        return defaultPort;
    }

    private void setDefaultPort(String defaultPort) { this.defaultPort = defaultPort; }

    private String[] countPorts(){
        String[] ports;
        try {
            SerialPortFinder spf = new SerialPortFinder();
            ports = spf.getAllDevicesPath();
        } catch (Exception e) {
            e.getMessage();
            ports = new String[1];
            ports[0] = "";
        }
        return ports;
    }

    private void checkDefaultPort(String defaultPort){
        String[] ports = countPorts();
        if (!ports[0].equals("")){
            boolean mt1exists = false;
            for (String port : ports){
                if (port.equals(defaultPort)) mt1exists = true;
            }
            if (!mt1exists) setDefaultPort(ports[0]);
            else setDefaultPort(defaultPort);
        } else setDefaultPort(context.getString(R.string.no_ports));
    }

    public boolean openPort(String portPath, int baudrate){
        File f = new File(portPath);
        try {
            serialPort = new SerialPort(f, baudrate, 0);
        } catch (IOException e) { e.printStackTrace(); }

        try {
            rwAllowed = serialPort.getFd() != null;
        } catch (Exception e) { e.printStackTrace(); }

        if (rwAllowed) {
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            if ((inputStream != null) && (outputStream != null)) {
                openedPort =  portPath.substring(portPath.lastIndexOf("/") + 1);
                startReadThread();
                return true;
            }
        } else {
            openedPort = "";
            return false;
        }
        openedPort = "";
        return false;
    }

    public void closePort(){
        rwAllowed = false;
        if (serialPort != null){
            try {
                if (readThread != null) readThread.interrupt();
                inputStream.close();
                outputStream.close();
                serialPort.close();
                openedPort = "";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startReadThread(){
        readThread = new ReadThread();
        readThread.start();
    }

    public void sendBytes(byte[] data){
        if (rwAllowed && (outputStream != null)){
            try {
                outputStream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                int size;
                try {
                    if (inputStream == null) return;
                    byte[] buffer = new byte[256];
                    size = inputStream.read(buffer);
                    if (size > 0) {
                        String bufferedString = convert(buffer);
                        sendIntent(bufferedString, context.getString(R.string.tag), context);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        private String convert(byte[] array) {
            StringBuilder sb = new StringBuilder(array.length*2);
            for(byte b : array) sb.append(String.format(" %02x", b & 0xff));
            return sb.toString();
        }

        private void sendIntent(String str, String broadcastTag, Context context){
            Intent intent = new Intent(broadcastTag);
            intent.putExtra(broadcastTag, str);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }
}
