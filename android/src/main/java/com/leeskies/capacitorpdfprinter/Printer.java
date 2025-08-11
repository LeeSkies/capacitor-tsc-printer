package com.leeskies.capacitorpdfprinter;

import android.content.Context;
import android.util.Log;

import com.leeskies.capacitorpdfprinter.TSCPrinterCore;
import com.leeskies.capacitorpdfprinter.TSCNetworkCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.util.Base64;
import com.getcapacitor.PluginCall;

import java.util.concurrent.atomic.AtomicBoolean;

public class Printer {
        
    public void printPDFByNetwork(int port, String IPAddress, String base64String, int x, int y, int dpi, PluginCall call, Context context) {
        Log.d("Printer", "\n\n\n\n === ENTRY POINT: printPdf called with context === \n\n\n\n");
        
        TSCPrinterCore tsc = new TSCPrinterCore(context);
        TSCNetworkCore network = new TSCNetworkCore(context);
        boolean connectionOpened = false;
        
        try {
            Log.d("Printer", "Attempting to connect to printer at " + IPAddress + ":" + port);
            String openResult = network.openport(IPAddress, port);
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
            
            // Process PDF and get bitmap data to send
            Log.d("Printer", "Processing PDF to extract bitmap data");
            
            try {
                // We need to manually process the PDF and send the bitmap data
                // since printPDFbyFileSync no longer sends data automatically
                android.graphics.pdf.PdfRenderer mPdfRenderer = new android.graphics.pdf.PdfRenderer(android.os.ParcelFileDescriptor.open(tempFile, android.os.ParcelFileDescriptor.MODE_READ_ONLY));
                int PageCount = mPdfRenderer.getPageCount();
                Log.d("Printer", "PDF has " + PageCount + " pages");
                
                for (int idx = 0; idx < PageCount; idx++) {
                    Log.d("Printer", "Processing page " + (idx + 1) + "/" + PageCount);
                    
                    android.graphics.pdf.PdfRenderer.Page page = mPdfRenderer.openPage(idx);
                    int width = page.getWidth() * dpi / 72;
                    int height = page.getHeight() * dpi / 72;
                    
                    android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
                    android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                    canvas.drawColor(android.graphics.Color.WHITE);
                    
                    page.render(bitmap, new android.graphics.Rect(0, 0, width, height), null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                    page.close();
                    
                    // Process bitmap and get data to send
                    TSCPrinterCore.BitmapData bitmapData = tsc.processBitmapForPrinting(printX, printY, bitmap);
                    Log.d("Printer", "Bitmap processed, sending to printer: " + bitmapData.command);
                    
                    // Send the bitmap data to the printer
                    String sendResult = network.sendBitmapData(bitmapData.command, bitmapData.stream);
                    Log.d("Printer", "Bitmap data send result: " + sendResult);
                    
                    if (sendResult == null || sendResult.equals("-1")) {
                        call.reject("Print failed: Could not send bitmap data to printer");
                        return;
                    }
                    
                    bitmap.recycle();
                }
                mPdfRenderer.close();
                
                // Send print command to actually print
                Log.d("Printer", "Sending print command");
                String printResult = network.sendPrintCommand();
                Log.d("Printer", "Print command result: " + printResult);
                
                if (printResult == null || printResult.equals("-1")) {
                    call.reject("Print failed: Could not send print command to printer");
                    return;
                }
                
            } catch (Exception e) {
                Log.e("Printer", "Exception during PDF processing and printing", e);
                call.reject("Print failed: " + e.getMessage());
                return;
            }
            
            Log.d("Printer", "Print completed successfully");
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
                network.clearbuffer();
                network.closeport(1000);
            } catch (Exception e) {
                Log.d("Printer", "Error during cleanup: " + e.getMessage());
            }
        }
    }
    
}