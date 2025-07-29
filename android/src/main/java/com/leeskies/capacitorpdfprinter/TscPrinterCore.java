package com.leeskies.capacitorpdfprinter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import compression.lzss;

public class TscPrinterCore {
    private Socket socket = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private boolean isConnected = false;

    public String openport(String ipAddress, int port) {
        StrictMode.setThreadPolicy((new StrictMode.ThreadPolicy.Builder())
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy((new StrictMode.VmPolicy.Builder())
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build());

        try {
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(ipAddress, port), 2000);
            this.inputStream = this.socket.getInputStream();
            this.outputStream = this.socket.getOutputStream();
            this.isConnected = true;
        } catch (Exception ex) {
            this.isConnected = false;
            try {
                if (this.inputStream != null) {
                    this.inputStream.close();
                }
                if (this.outputStream != null) {
                    this.outputStream.close();
                }
                if (this.socket != null) {
                    this.socket.close();
                }
            } catch (IOException e) {
                return "-2";
            } finally {
                this.inputStream = null;
                this.outputStream = null;
                this.socket = null;
            }
            return "-1";
        }

        try {
            Thread.sleep(100L);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        return "1";
    }

    public String closeport(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!this.isConnected)
            return "-1";
        try {
            if (this.inputStream != null) {
                this.inputStream.close();
            }
            if (this.outputStream != null) {
                this.outputStream.close();
            }
            if (this.socket != null) {
                this.socket.close();
            }
            this.isConnected = false;
        } catch (IOException e) {
            this.isConnected = false;
            return "-1";
        } finally {
            this.inputStream = null;
            this.outputStream = null;
            this.socket = null;
        }
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "1";
    }

    public String clearbuffer() {
        if (!this.isConnected)
            return "-1";
        String message = "CLS\r\n";
        byte[] msgBuffer = message.getBytes();
        try {
            this.outputStream.write(msgBuffer);
        } catch (IOException e) {
            return "-1";
        }
        return "1";
    }

    public String setup(int width, int height, int speed, int density, int sensor, int sensor_distance, int sensor_offset) {
        String message = "";
        String size = "SIZE " + width + " mm" + ", " + height + " mm";
        String speed_value = "SPEED " + speed;
        String density_value = "DENSITY " + density;
        String sensor_value = "";
        if (sensor == 0) {
            sensor_value = "GAP " + sensor_distance + " mm" + ", " + sensor_offset + " mm";
        } else if (sensor == 1) {
            sensor_value = "BLINE " + sensor_distance + " mm" + ", " + sensor_offset + " mm";
        }
        message = String.valueOf(size) + "\r\n" + speed_value + "\r\n" + density_value + "\r\n" + sensor_value + "\r\n";
        byte[] msgBuffer = message.getBytes();
        try {
            this.outputStream.write(msgBuffer);
        } catch (IOException e) {
            return "-1";
        }
        return "1";
    }

    public String sendcommand(String message) {
        if (!this.isConnected)
            return "-1";
        byte[] msgBuffer = message.getBytes();
        try {
            this.outputStream.write(msgBuffer);
        } catch (IOException e) {
            return "-1";
        }
        return "1";
    }

    public String sendcommand(byte[] message) {
        if (!this.isConnected)
            return "-1";
        try {
            this.outputStream.write(message);
        } catch (IOException e) {
            return "-1";
        }
        return "1";
    }

    // Original method for backward compatibility
    public String printPDFbyFile(File file, int x_coordinates, int y_coordinates, int printer_dpi) {
        return printPDFbyFile(file, x_coordinates, y_coordinates, printer_dpi, false);
    }

    // Modified method with debug parameter
    public String printPDFbyFile(File file, int x_coordinates, int y_coordinates, int printer_dpi, boolean debugMode) {
        try {
            PdfRenderer mPdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(file, 268435456));
            int PageCount = mPdfRenderer.getPageCount();
            for (int idx = 0; idx < PageCount; idx++) {
                PdfRenderer.Page page = mPdfRenderer.openPage(idx);
                
                // FIXED: Use working code's DPI calculation
                int width = (int)((page.getWidth() * 72) / 25.4D);
                int height = (int)((page.getHeight() * 72) / 25.4D);
                
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                
                // FIXED: Use working code's direct render (no Canvas setup)
                page.render(bitmap, null, null, 1);
                
                // Save bitmap as PNG for debugging
                saveBitmapAsPNG(bitmap, "debug_page_" + idx + ".png");
                
                page.close();
                
                if (!debugMode) {
                    // Normal mode - send commands to printer
                    sendcommand("CLS\r\n");
                    sendbitmap(x_coordinates, y_coordinates, bitmap);
                    sendcommand("PRINT 1\r\n");
                } else {
                    // Debug mode - just process bitmap for debug images
                    Log.d("DEBUG", "Debug mode: Processing bitmap without sending to printer");
                    sendbitmap(x_coordinates, y_coordinates, bitmap, 128, true); // Pass debug flag
                }
                
                bitmap.recycle();
            }
            mPdfRenderer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "-1";
        }
        return "1";
    }

    private void saveBitmapAsPNG(Bitmap bitmap, String filename) {
        try {
            // Save to Downloads folder
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, filename);
            
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            
            Log.d("DEBUG", "Bitmap saved to Downloads: " + file.getAbsolutePath());
        } catch (Exception e) {
            Log.e("DEBUG", "Error saving bitmap to Downloads: " + e.getMessage());
        }
    }

    // Original overloads for backward compatibility
    private void sendbitmap(int x_coordinates, int y_coordinates, Bitmap original_bitmap) {
        sendbitmap(x_coordinates, y_coordinates, original_bitmap, 128, false);
    }

    private void sendbitmap(int x_coordinates, int y_coordinates, Bitmap original_bitmap, int threshold) {
        sendbitmap(x_coordinates, y_coordinates, original_bitmap, threshold, false);
    }

    // FIXED: Modified method using uncompressed BITMAP command like working code
    private void sendbitmap(int x_coordinates, int y_coordinates, Bitmap original_bitmap, int threshold, boolean debugMode) {
        Bitmap gray_bitmap = null;
        Bitmap binary_bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            BitmapFactory.Options.class.getField("inNativeAlloc").setBoolean(options, true);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            gray_bitmap = bitmap2Gray(original_bitmap);
            binary_bitmap = gray2Binary(gray_bitmap, threshold);
            
            // Save final binary bitmap before printing for debugging
            saveBitmapAsPNG(binary_bitmap, "final_binary_" + System.currentTimeMillis() + ".png");
            
            if (!debugMode) {
                // FIXED: Use uncompressed BITMAP command format like working code
                String x_axis = Integer.toString(x_coordinates);
                String y_axis = Integer.toString(y_coordinates);
                String picture_width = Integer.toString((binary_bitmap.getWidth() + 7) / 8);
                String picture_height = Integer.toString(binary_bitmap.getHeight());
                String mode = Integer.toString(0);
                
                // Create BITMAP command string like working code
                String command = "BITMAP " + x_axis + "," + y_axis + "," + picture_width + "," + picture_height + "," + mode + ",";
                
                byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
                int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
                int Width = binary_bitmap.getWidth();
                int Height = binary_bitmap.getHeight();
                
                // Initialize stream with all 1s (white)
                for (int i = 0; i < Height * Width_bytes; i++)
                    stream[i] = -1;
                
                // Process pixels - same logic as working code
                for (int y = 0; y < Height; y++) {
                    for (int x = 0; x < Width; x++) {
                        int pixelColor = binary_bitmap.getPixel(x, y);
                        int colorR = Color.red(pixelColor);
                        int colorG = Color.green(pixelColor);
                        int colorB = Color.blue(pixelColor);
                        int total = (colorR + colorG + colorB) / 3;
                        if (total == 0)
                            stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8));
                    }
                }
                
                // FIXED: Send uncompressed data like working code
                sendcommand(command);        // Send BITMAP command
                sendcommand(stream);         // Send raw bitmap data
                sendcommand("\r\n");         // Send terminator
                
            } else {
                // Debug mode - just log that we're skipping printer communication
                Log.d("DEBUG", "Debug mode: Skipping printer data transmission. Binary bitmap saved for inspection.");
            }
            
        } finally {
            if (gray_bitmap != null && !gray_bitmap.isRecycled()) {
                gray_bitmap.recycle();
            }
            if (binary_bitmap != null && !binary_bitmap.isRecycled()) {
                binary_bitmap.recycle();
            }
        }
    }

    private Bitmap bitmap2Gray(Bitmap bmSrc) {
        int width = bmSrc.getWidth();
        int height = bmSrc.getHeight();
        Bitmap bmpGray = null;
        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.0F);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter((ColorFilter)f);
        c.drawBitmap(bmSrc, 0.0F, 0.0F, paint);
        return bmpGray;
    }

    private Bitmap gray2Binary(Bitmap graymap, int threshold) {
        int width = graymap.getWidth();
        int height = graymap.getHeight();
        Bitmap binarymap = null;
        binarymap = graymap.copy(Bitmap.Config.ARGB_8888, true);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int col = binarymap.getPixel(i, j);
                int alpha = col & 0xFF000000;
                int red = (col & 0xFF0000) >> 16;
                int green = (col & 0xFF00) >> 8;
                int blue = col & 0xFF;
                int gray = (int)(red * 0.3D + green * 0.59D + blue * 0.11D);
                if (gray <= threshold) {
                    gray = 0;
                } else {
                    gray = 255;
                }
                int newColor = alpha | gray << 16 | gray << 8 | gray;
                binarymap.setPixel(i, j, newColor);
            }
        }
        return binarymap;
    }

    public boolean isConnected() {
        return this.isConnected;
    }
}