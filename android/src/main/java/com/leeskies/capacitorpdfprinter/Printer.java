package com.leeskies.capacitorpdfprinter;

import android.content.Context;
import android.util.Log;

import com.leeskies.capacitorpdfprinter.TSCPrinterCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.util.Base64;
import com.getcapacitor.PluginCall;

import java.util.concurrent.atomic.AtomicBoolean;

public class Printer {
        
    public void printPdf(int port, String IPAddress, String base64String, int x, int y, int dpi, PluginCall call, Context context) {
        Log.d("Printer", "\n\n\n\n === ENTRY POINT: printPdf called with context === \n\n\n\n");
        
        TSCPrinterCore tsc = new TSCPrinterCore(context);
        boolean connectionOpened = false;
        
        try {
            Log.d("Printer", "Attempting to connect to printer at " + IPAddress + ":" + port);
            String openResult = tsc.openport(IPAddress, port);
            Log.d("Printer", "Connection result: " + openResult);
            
            if (openResult == null) {
                call.reject("Connection failed: No response from printer at " + IPAddress + ":" + port);
                return;
            }
            if (openResult.equals("-1")) {
                call.reject("Connection failed: Cannot reach printer at " + IPAddress + ":" + port + " (timeout or network error)");
                return;
            }
            if (openResult.equals("-2")) {
                call.reject("Connection failed: Socket error when connecting to " + IPAddress + ":" + port);
                return;
            }
            if (openResult.toLowerCase().contains("error") || openResult.toLowerCase().contains("failed")) {
                call.reject("Connection failed: " + openResult + " at " + IPAddress + ":" + port);
                return;
            }
            
            connectionOpened = true;
            Log.d("Printer", "Successfully connected to printer");
            
        } catch (Exception e) {
            call.reject("Printer connection error: " + e.getMessage());
            return;
        }

        if (base64String == null || base64String.isEmpty()) {
            call.reject("Print failed: No PDF data provided (base64String is empty)");
            return;
        }
        
        Log.d("Printer", "Decoding PDF data (" + base64String.length() + " characters)");
        byte[] pdfBytes;
        try {
            pdfBytes = Base64.decode(base64String, Base64.DEFAULT);
            Log.d("Printer", "Decoded PDF: " + pdfBytes.length + " bytes");
        } catch (IllegalArgumentException e) {
            call.reject("Print failed: Invalid base64 PDF data - " + e.getMessage());
            return;
        }
        
        File tempFile = null;
        try {
            tempFile = File.createTempFile("tempPDF", ".pdf");
            Log.d("Printer", "Created temp file: " + tempFile.getAbsolutePath());
            
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(pdfBytes);
                fos.flush();
            }
            Log.d("Printer", "Written PDF data to temp file (" + tempFile.length() + " bytes)");
            Log.d("Printer", "Attempting to start print operation");
            Log.d("Printer", "Starting print operation with params: x=" + x + ", y=" + y + ", dpi=" + dpi);
            
            // Force coordinates to ensure bitmap is within label bounds
            int printX = Math.max(0, Math.min(x, 832 - 100)); // Keep some margin
            int printY = Math.max(0, Math.min(y, 1216 - 100));
            Log.d("Printer", "Adjusted print coordinates: x=" + printX + ", y=" + printY);
            
            String result = tsc.printPDFbyFileSync(tempFile, printX, printY, dpi);
            Log.d("Printer", "Print operation result: " + result);

            if (result == null) {
                call.reject("Print failed: No response from print operation");
                return;
            }
            if (result.equals("-1")) {
                call.reject("Print failed: Print operation returned error (-1)");
                return;
            }
            
            Log.d("Printer", "Print completed successfully: " + result);
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
        
        // Cleanup printer connection if it was opened
        if (connectionOpened) {
            try {
                tsc.clearbuffer();
                tsc.closeport(1000);
            } catch (Exception e) {
                Log.d("Printer", "Error during cleanup: " + e.getMessage());
            }
        }
    }

    // public void printPdf(int port, String IPAddress, String base64String, int x, int y, int width, int height, int dpi, PluginCall call, Context context) {
    //     Log.d("Printer", "printPdfWithSetup called with port=" + port + ", IP=" + IPAddress + ", width=" + width + ", height=" + height);
    //     TSCPrinterCore tsc = new TSCPrinterCore();
    //     boolean connectionOpened = false;

    //     try {
    //         String openResult = tsc.openport(IPAddress, port);
    //         if (openResult == null || openResult.equals("-1") || openResult.toLowerCase().contains("error") || openResult.toLowerCase().contains("failed")) {
    //             call.reject("Failed to connect to printer at " + IPAddress + ":" + port + " - " + openResult);
    //             return;
    //         }
    //         connectionOpened = true;

    //         int speed = 6;
    //         int density = 8;
    //         int sensor = 0;
    //         int gap = 30;
    //         int shift = 0;
    //         tsc.setup(width, height, speed, density, sensor, gap, shift);

    //         if (base64String == null || base64String.isEmpty()) {
    //             call.reject("Print failed: No PDF data provided (base64String is empty)");
    //             return;
    //         }
            
    //         Log.d("Printer", "Decoding PDF data (" + base64String.length() + " characters)");
    //         byte[] pdfBytes;
    //         try {
    //             pdfBytes = Base64.decode(base64String, Base64.DEFAULT);
    //             Log.d("Printer", "Decoded PDF: " + pdfBytes.length + " bytes");
    //         } catch (IllegalArgumentException e) {
    //             call.reject("Print failed: Invalid base64 PDF data - " + e.getMessage());
    //             return;
    //         }
    //         File tempFile = null;
    //         try {
    //             tempFile = File.createTempFile("tempPDF", ".pdf");
    //             try (FileOutputStream fos = new FileOutputStream(tempFile)) {
    //                 fos.write(pdfBytes);
    //             }
    //             Log.d("Printer", "Created temp PDF file: " + tempFile.getAbsolutePath() + " (" + tempFile.length() + " bytes)");
                
    //             Log.d("Printer", "Starting print operation with params: x=" + x + ", y=" + y + ", dpi=" + dpi);
    //             String result = tsc.printPDFbyFileWithContext(tempFile, x, y, dpi, context);
    //             Log.d("Printer", "Print operation result: " + result);

    //             if (result == null) {
    //                 call.reject("Print failed: No response from print operation");
    //                 return;
    //             }
    //             if (result.equals("-1")) {
    //                 call.reject("Print failed: Print operation returned error (-1)");
    //                 return;
    //             }
                
    //             Log.d("Printer", "Print completed successfully: " + result);
    //             call.resolve();
    //         } catch (Exception e) {
    //             e.printStackTrace();
    //             call.reject("Error creating temporary file: " + e.getMessage());
    //             return;
    //         } finally {
    //             if (tempFile != null && tempFile.exists()) {
    //                 tempFile.delete();
    //             }
    //         }
    //     } catch (Exception e) {
    //         call.reject("Printer connection error: " + e.getMessage());
    //         return;
    //     } finally {
    //         if (connectionOpened) {
    //             try {
    //                 tsc.clearbuffer();
    //                 tsc.closeport(1000);
    //             } catch (Exception e) {
    //                 Log.d("Printer", "Error during cleanup: " + e.getMessage());
    //             }
    //         }
    //     }
    // }
}