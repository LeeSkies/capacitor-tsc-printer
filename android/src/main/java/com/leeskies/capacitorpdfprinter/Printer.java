package com.leeskies.capacitorpdfprinter;

import android.content.Context;
import android.util.Log;

import com.example.tscdll.TscWifiActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.util.Base64;
import com.getcapacitor.PluginCall;

public class Printer {
    public void print(int port, String IPAddress, String uri, PluginCall call) {
        TscWifiActivity tsc = new TscWifiActivity();
        boolean connectionOpened = false;
        try {
            String result = tsc.openport(IPAddress, port);
            if (result == null || result.equals("-1") || result.toLowerCase().contains("error") || result.toLowerCase().contains("failed")) {
                call.reject("Failed to connect to printer at " + IPAddress + ":" + port + " - " + result);
                return;
            }
            connectionOpened = true;
            
            String flag = tsc.printPDFbyPath(uri, 0, 100, 300);
            if (flag.equals("-1")) {
                call.reject("Print operation failed");
                return;
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("Printer connection error: " + e.getMessage());
        } finally {
            if (connectionOpened) {
                try {
                    tsc.clearbuffer();
                    tsc.closeport(port);
                } catch (Exception e) {
                    Log.d("Printer", "Error during cleanup: " + e.getMessage());
                }
            }
        }
    }
    public void printPdf(int port, String IPAddress, String base64String, int x, int y, int dpi, PluginCall call) {
        TscWifiActivity tsc = new TscWifiActivity();
        boolean connectionOpened = false;
        try {
            String openResult = tsc.openport(IPAddress, port);
            if (openResult == null || openResult.equals("-1") || openResult.toLowerCase().contains("error") || openResult.toLowerCase().contains("failed")) {
                call.reject("Failed to connect to printer at " + IPAddress + ":" + port + " - " + openResult);
                return;
            }
            connectionOpened = true;
            
            byte[] pdfBytes = Base64.decode(base64String, Base64.DEFAULT);
            File tempFile = null;
            try {
                tempFile = File.createTempFile("tempPDF", ".pdf");
                try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                    fos.write(pdfBytes);
                }
                String result = tsc.printPDFbyFile(tempFile, x, y, dpi);

                if (result.equals("-1")) {
                    call.reject("Print operation failed");
                    return;
                }
                Log.d("Printer", "Print result: " + result);
                call.resolve();
            } catch (Exception e) {
                e.printStackTrace();
                call.reject("Error creating temporary file: " + e.getMessage());
                return;
            } finally {
                if (tempFile != null && tempFile.exists()) {
                    tempFile.delete();
                }
            }
        } catch (Exception e) {
            call.reject("Printer connection error: " + e.getMessage());
            return;
        } finally {
            if (connectionOpened) {
                try {
                    tsc.clearbuffer();
                    tsc.closeport(port);
                } catch (Exception e) {
                    Log.d("Printer", "Error during cleanup: " + e.getMessage());
                }
            }
        }
    }
    public void printPdf(int port, String IPAddress, String base64String, int x, int y, int width, int height, int dpi, PluginCall call) {
        TscWifiActivity tsc = new TscWifiActivity();
        boolean connectionOpened = false;

        // Create a new thread to handle network connection and printing logic
        new Thread(() -> {
            try {
                // Open the port in the background
                String openResult = tsc.openport(IPAddress, port);
                if (openResult == null || openResult.equals("-1") || openResult.toLowerCase().contains("error") || openResult.toLowerCase().contains("failed")) {
                    call.reject("Failed to connect to printer at " + IPAddress + ":" + port + " - " + openResult);
                    return;
                }
                connectionOpened = true;

                // Proceed with setup and printing on successful connection
                int speed = 6;
                int density = 8;
                int sensor = 0;
                int gap = 30;
                int shift = 0;
                tsc.setup(width, height, speed, density, sensor, gap, shift);

                // Decode base64 string to PDF bytes
                byte[] pdfBytes = Base64.decode(base64String, Base64.DEFAULT);
                File tempFile = null;
                try {
                    // Create a temporary file to save the PDF
                    tempFile = File.createTempFile("tempPDF", ".pdf");
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        fos.write(pdfBytes);
                    }
                    // Print the PDF file
                    String result = tsc.printPDFbyFile(tempFile, x, y, dpi);

                    if (result.equals("-1")) {
                        call.reject("Print operation failed");
                        return;
                    }
                    Log.d("Printer", "Print result: " + result);
                    call.resolve();
                } catch (Exception e) {
                    e.printStackTrace();
                    call.reject("Error creating temporary file: " + e.getMessage());
                    return;
                } finally {
                    if (tempFile != null && tempFile.exists()) {
                        tempFile.delete();
                    }
                }
            } catch (Exception e) {
                call.reject("Printer connection error: " + e.getMessage());
                return;
            } finally {
                if (connectionOpened) {
                    try {
                        tsc.clearbuffer();
                        tsc.closeport(port);
                    } catch (Exception e) {
                        Log.d("Printer", "Error during cleanup: " + e.getMessage());
                    }
                }
            }
        }).start(); // Start the background thread
    }


}
