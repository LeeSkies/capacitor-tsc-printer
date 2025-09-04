package com.leeskies.capacitorpdfprinter;

import android.content.Context;
import android.util.Log;

import com.leeskies.capacitorpdfprinter.TSCPrinterCore;
import com.leeskies.capacitorpdfprinter.TSCNetworkCore;
import com.leeskies.capacitorpdfprinter.TSCUSBCore;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

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
                    
                    // Clear buffer before each page (like original working version)
                    Log.d("Printer", "Clearing buffer before page " + (idx + 1));
                    String clearResult = network.clearbuffer();
                    if (clearResult == null || clearResult.equals("-1")) {
                        Log.w("Printer", "Warning: Could not clear buffer for page " + (idx + 1));
                    }
                    
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
                        call.reject("Print failed: Could not send bitmap data to printer for page " + (idx + 1));
                        return;
                    }
                    
                    // Print THIS page immediately (like the working version)
                    Log.d("Printer", "Printing page " + (idx + 1) + " immediately");
                    String printResult = network.sendPrintCommand();
                    Log.d("Printer", "Page " + (idx + 1) + " print result: " + printResult);
                    
                    if (printResult == null || printResult.equals("-1")) {
                        call.reject("Print failed: Could not print page " + (idx + 1));
                        return;
                    }
                    
                    bitmap.recycle();
                    Log.d("Printer", "Page " + (idx + 1) + "/" + PageCount + " completed successfully");
                }
                mPdfRenderer.close();
                
                Log.d("Printer", "All " + PageCount + " pages processed and printed successfully");
                
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

    public void printPDFByUSB(String base64String, int x, int y, int dpi, PluginCall call, Context context) {
        Log.d("Printer", "\n\n\n\n === ENTRY POINT: printPDFByUSB called with context === \n\n\n\n");
        
        TSCPrinterCore tsc = new TSCPrinterCore(context);
        TSCUSBCore usb = new TSCUSBCore(context);
        boolean connectionOpened = false;
        
        try {
            // Find USB printer device
            Log.d("Printer", "Searching for USB printer device");
            UsbDevice printer = usb.findPrinterDevice();
            if (printer == null) {
                call.reject("USB printer not found - please connect printer and try again");
                return;
            }
            
            Log.d("Printer", "Found USB printer: " + printer.getDeviceName() + " (VID: " + printer.getVendorId() + ", PID: " + printer.getProductId() + ")");
            
            // Request permission if needed
            if (!usb.hasPermission()) {
                Log.d("Printer", "Requesting USB permission");
                usb.requestPermission(printer);
                
                // Wait for permission (simplified - in production you'd use proper async handling)
                int attempts = 0;
                while (!usb.hasPermission() && attempts < 50) {
                    Thread.sleep(100);
                    attempts++;
                }
                
                if (!usb.hasPermission()) {
                    call.reject("USB permission denied - please grant permission to access printer");
                    return;
                }
            }
            
            // Open USB connection
            Log.d("Printer", "Attempting to open USB connection");
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            String openResult = usb.openport(usbManager, printer);
            Log.d("Printer", "USB connection result: " + openResult);
            
            if (openResult == null || openResult.equals("-1")) {
                call.reject("USB connection failed: Cannot establish connection to printer");
                return;
            }
            
            connectionOpened = true;
            Log.d("Printer", "Successfully connected to USB printer");
            
        } catch (Exception e) {
            call.reject("USB printer connection error: " + e.getMessage());
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
            Log.d("Printer", "Attempting to start USB print operation");
            Log.d("Printer", "Starting print operation with params: x=" + x + ", y=" + y + ", dpi=" + dpi);
            
            // Force coordinates to ensure bitmap is within label bounds
            int printX = Math.max(0, Math.min(x, 832 - 100)); // Keep some margin
            int printY = Math.max(0, Math.min(y, 1216 - 100));
            Log.d("Printer", "Adjusted print coordinates: x=" + printX + ", y=" + printY);
            
            // Process PDF and get bitmap data to send
            Log.d("Printer", "Processing PDF to extract bitmap data for USB");
            
            try {
                
                // Process PDF pages manually for USB printing
                android.graphics.pdf.PdfRenderer mPdfRenderer = new android.graphics.pdf.PdfRenderer(android.os.ParcelFileDescriptor.open(tempFile, android.os.ParcelFileDescriptor.MODE_READ_ONLY));
                int PageCount = mPdfRenderer.getPageCount();
                Log.d("Printer", "PDF has " + PageCount + " pages for USB printing");
                
                for (int idx = 0; idx < PageCount; idx++) {
                    Log.d("Printer", "Processing USB page " + (idx + 1) + "/" + PageCount);
                    
                    // Clear buffer before each page (like original working version)
                    Log.d("Printer", "Clearing USB buffer before page " + (idx + 1));
                    String clearResult = usb.clearbuffer();
                    if (clearResult == null || clearResult.equals("-1")) {
                        Log.w("Printer", "Warning: Could not clear USB buffer for page " + (idx + 1));
                    }
                    
                    android.graphics.pdf.PdfRenderer.Page page = mPdfRenderer.openPage(idx);
                    int width = page.getWidth() * dpi / 72;
                    int height = page.getHeight() * dpi / 72;
                    
                    android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888);
                    android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                    canvas.drawColor(android.graphics.Color.WHITE);
                    
                    page.render(bitmap, new android.graphics.Rect(0, 0, width, height), null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
                    page.close();
                    
                    // Process bitmap and get data to send via USB
                    TSCPrinterCore.BitmapData bitmapData = tsc.processBitmapForPrinting(printX, printY, bitmap);
                    Log.d("Printer", "USB Bitmap processed, sending to printer: " + bitmapData.command);
                    
                    // Send the bitmap data to the USB printer
                    String sendResult = usb.sendBitmapData(bitmapData.command, bitmapData.stream);
                    Log.d("Printer", "USB Bitmap data send result: " + sendResult);
                    
                    if (sendResult == null || sendResult.equals("-1")) {
                        call.reject("USB print failed: Could not send bitmap data to printer for page " + (idx + 1));
                        return;
                    }
                    
                    // Print THIS page immediately via USB (like the working version)
                    Log.d("Printer", "Printing USB page " + (idx + 1) + " immediately");
                    String printResult = usb.sendPrintCommand();
                    Log.d("Printer", "USB Page " + (idx + 1) + " print result: " + printResult);
                    
                    if (printResult == null || printResult.equals("-1")) {
                        call.reject("USB print failed: Could not print page " + (idx + 1));
                        return;
                    }
                    
                    bitmap.recycle();
                    Log.d("Printer", "USB Page " + (idx + 1) + "/" + PageCount + " completed successfully");
                }
                mPdfRenderer.close();
                
                Log.d("Printer", "All " + PageCount + " USB pages processed and printed successfully");
                
            } catch (Exception e) {
                Log.e("Printer", "Exception during USB PDF processing and printing", e);
                call.reject("USB print failed: " + e.getMessage());
                return;
            }
            
            Log.d("Printer", "USB Print completed successfully");
            call.resolve();
            
        } catch (Exception e) {
            e.printStackTrace();
            call.reject("Error creating temporary file for USB printing: " + e.getMessage());
            return;
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
        
        // Cleanup USB connection if it was opened
        if (connectionOpened) {
            try {
                usb.clearbuffer();
                usb.closeport(1000);
            } catch (Exception e) {
                Log.d("Printer", "Error during USB cleanup: " + e.getMessage());
            }
        }
    }
    
}