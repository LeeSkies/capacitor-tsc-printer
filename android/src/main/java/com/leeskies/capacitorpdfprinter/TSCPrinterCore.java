package com.leeskies.capacitorpdfprinter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngWriter;
import compression.lzss;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TSCPrinterCore {
  private static final String TAG = "Printerנו";
  
  private static final boolean D = true;
  
  private Context context;

  public TSCPrinterCore(Context context) {
    this.context = context;
  }
  
  public String windowsfont(int x_coordinates, int y_coordinates, int fontsize, String path, String textToPrint) {
    File file = new File(path);
    if (!file.exists())
      return "-1"; 
    Bitmap original_bitmap = null;
    Bitmap gray_bitmap = null;
    Bitmap binary_bitmap = null;
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(-16777216);
    paint.setAntiAlias(true);
    Typeface typeface = Typeface.createFromFile(path);
    paint.setTypeface(typeface);
    paint.setTextSize(fontsize);
    TextPaint textpaint = new TextPaint(paint);
    StaticLayout staticLayout = new StaticLayout(
        textToPrint, textpaint, 832, 
        Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
    int height = staticLayout.getHeight();
    int width = (int)Layout.getDesiredWidth(textToPrint, textpaint);
    if (height > 2378)
      height = 2378; 
    try {
      original_bitmap = Bitmap.createBitmap(width + 8, height, Bitmap.Config.RGB_565);
      Canvas c = new Canvas(original_bitmap);
      c.drawColor(-1);
      c.translate(0.0F, 0.0F);
      staticLayout.draw(c);
    } catch (IllegalArgumentException illegalArgumentException) {
    
    } catch (OutOfMemoryError outOfMemoryError) {}
    gray_bitmap = bitmap2Gray(original_bitmap);
    binary_bitmap = gray2Binary(gray_bitmap);
    String x_axis = Integer.toString(x_coordinates);
    String y_axis = Integer.toString(y_coordinates);
    String picture_wdith = Integer.toString((binary_bitmap.getWidth() + 7) / 8);
    String picture_height = Integer.toString(binary_bitmap.getHeight());
    String mode = Integer.toString(0);
    String command = "BITMAP " + x_axis + "," + y_axis + "," + picture_wdith + "," + picture_height + "," + mode + ",";
    byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
    int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
    int Width = binary_bitmap.getWidth();
    int Height = binary_bitmap.getHeight();
    for (int i = 0; i < Height * Width_bytes; i++)
      stream[i] = -1; 
    for (int y = 0; y < Height; y++) {
      for (int x = 0; x < Width; x++) {
        int pixelColor = binary_bitmap.getPixel(x, y);
        int colorR = Color.red(pixelColor);
        int colorG = Color.green(pixelColor);
        int colorB = Color.blue(pixelColor);
        int total = (colorR + colorG + colorB) / 3;
        if (total < 128)  // Changed from == 0 to < 128 to detect dark pixels, not just pure black
          stream[y * Width_bytes + x / 8] = (byte)(stream[y * Width_bytes + x / 8] ^ (byte)(128 >> x % 8)); 
      } 
    } 
    // Note: Network sendcommand calls removed - this method now only processes bitmaps
    // The caller needs to handle sending the command and stream data to the printer
    return "1";
  }
  
  public String windowsfont(int x_coordinates, int y_coordinates, int fontsize, String path, String textToPrint, int direction, int alignment) {
    File file = new File(path);
    if (!file.exists())
      return "-1"; 
    Bitmap original_bitmap = null;
    Bitmap gray_bitmap = null;
    Bitmap binary_bitmap = null;
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(-16777216);
    paint.setAntiAlias(true);
    Typeface typeface = Typeface.createFromFile(path);
    paint.setTypeface(typeface);
    paint.setTextSize(fontsize);
    TextPaint textpaint = new TextPaint(paint);
    StaticLayout staticLayout = new StaticLayout(
        textToPrint, textpaint, 832, 
        Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, false);
    int height = staticLayout.getHeight();
    int width = (int)Layout.getDesiredWidth(textToPrint, textpaint);
    if (height > 2378)
      height = 2378; 
    try {
      original_bitmap = Bitmap.createBitmap(width + 8, height, Bitmap.Config.RGB_565);
      Canvas c = new Canvas(original_bitmap);
      c.drawColor(-1);
      c.translate(0.0F, 0.0F);
      staticLayout.draw(c);
    } catch (IllegalArgumentException illegalArgumentException) {
    
    } catch (OutOfMemoryError outOfMemoryError) {}
    Bitmap rotate_bmp = null;
    switch (direction) {
      case 0:
        rotate_bmp = rotateBitmap(original_bitmap, 0.0F);
        break;
      case 90:
        rotate_bmp = rotateBitmap(original_bitmap, 90.0F);
        break;
      case 180:
        rotate_bmp = rotateBitmap(original_bitmap, 180.0F);
        break;
      case 270:
        rotate_bmp = rotateBitmap(original_bitmap, 270.0F);
        break;
      default:
        rotate_bmp = rotateBitmap(original_bitmap, 0.0F);
        break;
    } 
    gray_bitmap = bitmap2Gray(rotate_bmp);
    binary_bitmap = gray2Binary(gray_bitmap);
    int x_coordinates_m = x_coordinates;
    int y_coordinates_m = y_coordinates;
    if (alignment == 2) {
      switch (direction) {
        case 0:
          x_coordinates_m -= rotate_bmp.getWidth() / 2;
          break;
        case 90:
          x_coordinates_m -= rotate_bmp.getWidth();
          y_coordinates_m -= rotate_bmp.getHeight() / 2;
          break;
        case 180:
          x_coordinates_m -= rotate_bmp.getWidth() / 2;
          y_coordinates_m -= rotate_bmp.getHeight();
          break;
        case 270:
          y_coordinates_m -= rotate_bmp.getHeight() / 2;
          break;
        default:
          x_coordinates_m -= rotate_bmp.getWidth() / 2;
          break;
      } 
    } else if (alignment == 3) {
      switch (direction) {
        case 0:
          x_coordinates_m -= rotate_bmp.getWidth();
          break;
        case 90:
          x_coordinates_m -= rotate_bmp.getWidth();
          y_coordinates_m -= rotate_bmp.getHeight();
          break;
        case 180:
          x_coordinates_m = x_coordinates_m;
          y_coordinates_m -= rotate_bmp.getHeight();
          break;
        case 270:
          break;
        default:
          x_coordinates_m -= rotate_bmp.getWidth();
          break;
      } 
    } else {
      switch (direction) {
        case 90:
          x_coordinates_m -= rotate_bmp.getWidth();
          break;
        case 180:
          x_coordinates_m -= rotate_bmp.getWidth();
          y_coordinates_m -= rotate_bmp.getHeight();
          break;
        case 270:
          y_coordinates_m -= rotate_bmp.getHeight();
          break;
      } 
    } 
    String x_axis = Integer.toString(x_coordinates_m);
    String y_axis = Integer.toString(y_coordinates_m);
    String picture_wdith = Integer.toString((binary_bitmap.getWidth() + 7) / 8);
    String picture_height = Integer.toString(binary_bitmap.getHeight());
    String mode = Integer.toString(0);
    String command = "BITMAP " + x_axis + "," + y_axis + "," + picture_wdith + "," + picture_height + "," + mode + ",";
    byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
    int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
    int Width = binary_bitmap.getWidth();
    int Height = binary_bitmap.getHeight();
    for (int i = 0; i < Height * Width_bytes; i++)
      stream[i] = -1; 
    for (int y = 0; y < Height; y++) {
      for (int x = 0; x < Width; x++) {
        int pixelColor = binary_bitmap.getPixel(x, y);
        int colorR = Color.red(pixelColor);
        int colorG = Color.green(pixelColor);
        int colorB = Color.blue(pixelColor);
        int total = (colorR + colorG + colorB) / 3;
        if (total < 128)  // Changed from == 0 to < 128 to detect dark pixels, not just pure black
          stream[y * Width_bytes + x / 8] = (byte)(stream[y * Width_bytes + x / 8] ^ (byte)(128 >> x % 8)); 
      } 
    } 
    // Note: Network sendcommand calls removed - this method now only processes bitmaps
    // The caller needs to handle sending the command and stream data to the printer
    return "1";
  }
  
  private Bitmap rotateBitmap(Bitmap original, float degrees) {
    int width = original.getWidth();
    int height = original.getHeight();
    Matrix matrix = new Matrix();
    matrix.preRotate(degrees);
    Bitmap rotatedBitmap = Bitmap.createBitmap(original, 0, 0, width, height, matrix, true);
    return rotatedBitmap;
  }
  
  public String windowsfont(int x_coordinates, int y_coordinates, Bitmap original_bitmap) {
    Bitmap gray_bitmap = null;
    Bitmap binary_bitmap = null;
    gray_bitmap = bitmap2Gray(original_bitmap);
    binary_bitmap = gray2Binary(gray_bitmap);
    String x_axis = Integer.toString(x_coordinates);
    String y_axis = Integer.toString(y_coordinates);
    String picture_wdith = Integer.toString((binary_bitmap.getWidth() + 7) / 8);
    String picture_height = Integer.toString(binary_bitmap.getHeight());
    String mode = Integer.toString(0);
    byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
    int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
    int Width = binary_bitmap.getWidth();
    int Height = binary_bitmap.getHeight();
    for (int i = 0; i < Height * Width_bytes; i++)
      stream[i] = -1; 
    for (int y = 0; y < Height; y++) {
      for (int x = 0; x < Width; x++) {
        int pixelColor = binary_bitmap.getPixel(x, y);
        int colorR = Color.red(pixelColor);
        int colorG = Color.green(pixelColor);
        int colorB = Color.blue(pixelColor);
        int total = (colorR + colorG + colorB) / 3;
        if (total < 128)  // Changed from == 0 to < 128 to detect dark pixels, not just pure black
          stream[y * Width_bytes + x / 8] = (byte)(stream[y * Width_bytes + x / 8] ^ (byte)(128 >> x % 8)); 
      } 
    } 
    byte[] header = { 33, 83, (byte)(x_coordinates % 256), (byte)(x_coordinates / 256), (byte)(y_coordinates % 256), (byte)(y_coordinates / 256), (byte)(Width_bytes % 256), (byte)(Width_bytes / 256), (byte)(Height % 256), (byte)(Height / 256) };
    lzss encode = new lzss();
    byte[] encoded_data = encode.LZSSEncoding(stream, stream.length);
    stream = null;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(header, 0, header.length);
    out.write(encoded_data, 0, encoded_data.length);
    out.write("\r\n".getBytes(), 0, 2);
    // Note: Network sendcommand calls removed - this method now only processes bitmaps
    // The caller needs to handle sending the compressed data to the printer
    return "1";
  }
  
  public void sendpicture(int x_coordinates, int y_coordinates, String path) {
    Bitmap original_bitmap = null;
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
    original_bitmap = BitmapFactory.decodeFile(path, options);
    gray_bitmap = bitmap2Gray(original_bitmap);
    binary_bitmap = gray2Binary(gray_bitmap);
    String x_axis = Integer.toString(x_coordinates);
    String y_axis = Integer.toString(y_coordinates);
    String picture_wdith = Integer.toString((binary_bitmap.getWidth() + 7) / 8);
    String picture_height = Integer.toString(binary_bitmap.getHeight());
    String mode = Integer.toString(0);
    String command = "BITMAP " + x_axis + "," + y_axis + "," + picture_wdith + "," + picture_height + "," + mode + ",";
    byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
    int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
    int Width = binary_bitmap.getWidth();
    int Height = binary_bitmap.getHeight();
    for (int i = 0; i < Height * Width_bytes; i++)
      stream[i] = -1; 
    for (int y = 0; y < Height; y++) {
      for (int x = 0; x < Width; x++) {
        int pixelColor = binary_bitmap.getPixel(x, y);
        int colorR = Color.red(pixelColor);
        int colorG = Color.green(pixelColor);
        int colorB = Color.blue(pixelColor);
        int total = (colorR + colorG + colorB) / 3;
        if (total < 128)  // Changed from == 0 to < 128 to detect dark pixels, not just pure black
          stream[y * Width_bytes + x / 8] = (byte)(stream[y * Width_bytes + x / 8] ^ (byte)(128 >> x % 8)); 
      } 
    } 
    // Note: Network sendcommand calls removed - this method now only processes bitmaps
    // The caller needs to handle sending the command and stream data to the printer
  }
  
  public void sendbitmap(int x_coordinates, int y_coordinates, Bitmap original_bitmap) {
    sendbitmap(x_coordinates, y_coordinates, original_bitmap, 128);
  }
  
  public static class BitmapData {
    public String command;
    public byte[] stream;
    
    public BitmapData(String command, byte[] stream) {
      this.command = command;
      this.stream = stream;
    }
  }
  
  public BitmapData processBitmapForPrinting(int x_coordinates, int y_coordinates, Bitmap original_bitmap) {
    return processBitmapForPrinting(x_coordinates, y_coordinates, original_bitmap, 128);
  }
  
  public BitmapData processBitmapForPrinting(int x_coordinates, int y_coordinates, Bitmap original_bitmap, int threshold) {
    Log.d(TAG, "processBitmapForPrinting: Starting bitmap processing");
    Bitmap gray_bitmap = null;
    Bitmap binary_bitmap = null;
    
    try {
      Log.d(TAG, "processBitmapForPrinting: Converting to grayscale");
      gray_bitmap = bitmap2Gray(original_bitmap);
      Log.d(TAG, "processBitmapForPrinting: Grayscale conversion complete");
      
      Log.d(TAG, "processBitmapForPrinting: Converting to binary with threshold=" + threshold);
      binary_bitmap = gray2Binary(gray_bitmap, threshold);
      Log.d(TAG, "processBitmapForPrinting: Binary conversion complete");
    } catch (Exception e) {
      Log.e(TAG, "processBitmapForPrinting: Exception during bitmap conversion", e);
      throw e;
    }
    
    String x_axis = Integer.toString(x_coordinates);
    String y_axis = Integer.toString(y_coordinates);
    String picture_wdith = Integer.toString((binary_bitmap.getWidth() + 7) / 8);
    String picture_height = Integer.toString(binary_bitmap.getHeight());
    String mode = Integer.toString(0);
    byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
    int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
    int Width = binary_bitmap.getWidth();
    int Height = binary_bitmap.getHeight();
    for (int i = 0; i < Height * Width_bytes; i++)
      stream[i] = -1; 
    for (int y = 0; y < Height; y++) {
      for (int x = 0; x < Width; x++) {
        int pixelColor = binary_bitmap.getPixel(x, y);
        int colorR = Color.red(pixelColor);
        int colorG = Color.green(pixelColor);
        int colorB = Color.blue(pixelColor);
        int total = (colorR + colorG + colorB) / 3;
        if (total < 128)
          stream[y * Width_bytes + x / 8] = (byte)(stream[y * Width_bytes + x / 8] ^ (byte)(128 >> x % 8)); 
      } 
    } 
    Log.d(TAG, "processBitmapForPrinting: Bitmap stream data created, length=" + stream.length);
    
    String command = "BITMAP " + x_axis + "," + y_axis + "," + picture_wdith + "," + picture_height + "," + mode + ",";
    Log.d(TAG, "processBitmapForPrinting: BITMAP command prepared: " + command);
    
    return new BitmapData(command, stream);
  }
  
  public void sendbitmap(int x_coordinates, int y_coordinates, Bitmap original_bitmap, int threshold) {
    Log.d(TAG, "sendbitmap: Starting bitmap processing");
    Bitmap gray_bitmap = null;
    Bitmap binary_bitmap = null;
    // Skip deprecated BitmapFactory.Options - not needed for bitmap processing 
    
    try {
      Log.d(TAG, "sendbitmap: Converting to grayscale");
      gray_bitmap = bitmap2Gray(original_bitmap);
      Log.d(TAG, "sendbitmap: Grayscale conversion complete");
      
      Log.d(TAG, "sendbitmap: Converting to binary with threshold=" + threshold);
      binary_bitmap = gray2Binary(gray_bitmap, threshold);
      Log.d(TAG, "sendbitmap: Binary conversion complete");
      
      // Save binary bitmap to see what it looks like after conversion
      // try {
      //   File downloadsDir = new File("/storage/emulated/0/Download");
      //   File binaryFile = new File(downloadsDir, "debug_binary_" + System.currentTimeMillis() + ".png");
      //   FileOutputStream fos = new FileOutputStream(binaryFile);
      //   binary_bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
      //   fos.close();
      //   Log.d(TAG, "sendbitmap: Saved binary bitmap to " + binaryFile.getAbsolutePath());
      // } catch (Exception e) {
      //   Log.e(TAG, "sendbitmap: Failed to save binary bitmap", e);
      // }
    } catch (Exception e) {
      Log.e(TAG, "sendbitmap: Exception during bitmap conversion", e);
      throw e;
    }
    String x_axis = Integer.toString(x_coordinates);
    String y_axis = Integer.toString(y_coordinates);
    String picture_wdith = Integer.toString((binary_bitmap.getWidth() + 7) / 8);
    String picture_height = Integer.toString(binary_bitmap.getHeight());
    String mode = Integer.toString(0);
    byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
    int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
    int Width = binary_bitmap.getWidth();
    int Height = binary_bitmap.getHeight();
    for (int i = 0; i < Height * Width_bytes; i++)
      stream[i] = -1; 
    for (int y = 0; y < Height; y++) {
      for (int x = 0; x < Width; x++) {
        int pixelColor = binary_bitmap.getPixel(x, y);
        int colorR = Color.red(pixelColor);
        int colorG = Color.green(pixelColor);
        int colorB = Color.blue(pixelColor);
        int total = (colorR + colorG + colorB) / 3;
        if (total < 128)  // Changed from == 0 to < 128 to detect dark pixels, not just pure black
          stream[y * Width_bytes + x / 8] = (byte)(stream[y * Width_bytes + x / 8] ^ (byte)(128 >> x % 8)); 
      } 
    } 
    Log.d(TAG, "sendbitmap: Bitmap stream data created, length=" + stream.length);
    // Use the simple approach like working methods - no compression, no headers
    String command = "BITMAP " + x_axis + "," + y_axis + "," + picture_wdith + "," + picture_height + "," + mode + ",";
    Log.d(TAG, "sendbitmap: BITMAP command prepared: " + command);
    // Note: Network sendcommand calls removed - this method now only processes bitmaps
    // The caller needs to handle sending the command and stream data to the printer
    Log.d(TAG, "sendbitmap: Raw bitmap data prepared (" + stream.length + " bytes)");
    Log.d(TAG, "sendbitmap: Bitmap processing complete");
  }
  
  public void sendbitmap_resize(int x_coordinates, int y_coordinates, Bitmap original_bitmap, int resize_width, int resize_height) {
    sendbitmap_resize(x_coordinates, y_coordinates, original_bitmap, resize_width, resize_height, 128);
  }
  
  private void sendbitmap_resize(int x_coordinates, int y_coordinates, Bitmap original_bitmap, int resize_width, int resize_height, int threshold) {
    Bitmap resizedBitmap = Bitmap.createScaledBitmap(original_bitmap, resize_width, resize_height, false);
    byte[] stream = new byte[(resizedBitmap.getWidth() + 7) / 8 * resizedBitmap.getHeight()];
    int Width_bytes = (resizedBitmap.getWidth() + 7) / 8;
    int Width = resizedBitmap.getWidth();
    int Height = resizedBitmap.getHeight();
    for (int i = 0; i < Height * Width_bytes; i++)
      stream[i] = -1; 
    for (int y = 0; y < Height; y++) {
      for (int x = 0; x < Width; x++) {
        int pixelColor = resizedBitmap.getPixel(x, y);
        int gray = (int)(Color.red(pixelColor) * 0.3D + Color.green(pixelColor) * 0.59D + Color.blue(pixelColor) * 0.11D);
        if (gray < threshold)
          stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8)); 
      } 
    } 
    byte[] header = { 33, 83, (byte)(x_coordinates % 256), (byte)(x_coordinates / 256), (byte)(y_coordinates % 256), (byte)(y_coordinates / 256), (byte)(Width_bytes % 256), (byte)(Width_bytes / 256), (byte)(Height % 256), (byte)(Height / 256) };
    lzss encode = new lzss();
    byte[] encoded_data = encode.LZSSEncoding(stream, stream.length);
    stream = null;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(header, 0, header.length);
    out.write(encoded_data, 0, encoded_data.length);
    out.write("\r\n".getBytes(), 0, 2);
    // Note: Network sendcommand calls removed - this method now only processes bitmaps
    // The caller needs to handle sending the compressed data to the printer
  }
  
  public static Bitmap Stucki(Bitmap src) {
    Bitmap out = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
    int threshold = 128;
    int width = src.getWidth();
    int height = src.getHeight();
    int error = 0;
    int[][] errors = new int[width][height];
    int[] pixels_orig = new int[width * height];
    int[] pixels_new = new int[width * height];
    src.getPixels(pixels_orig, 0, width, 0, 0, width, height);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int pixel = pixels_orig[y * width + x];
        int alpha = Color.alpha(pixel);
        int red = Color.red(pixel);
        int green = Color.green(pixel);
        int blue = Color.blue(pixel);
        int grayC = (int)(0.21D * red + 0.72D * green + 0.07D * blue);
        int gray = grayC;
        if (gray + errors[x][y] < threshold) {
          error = gray + errors[x][y];
          gray = 0;
        } else {
          error = gray + errors[x][y] - 255;
          gray = 255;
        } 
        if (x < width - 1)
          errors[x + 1][y] = errors[x + 1][y] + 7 * error / 42; 
        if (x < width - 2)
          errors[x + 2][y] = errors[x + 2][y] + 5 * error / 42; 
        if (x > 1 && y < height - 1)
          errors[x - 2][y + 1] = errors[x - 2][y + 1] + 2 * error / 42; 
        if (x > 0 && y < height - 1)
          errors[x - 1][y + 1] = errors[x - 1][y + 1] + 4 * error / 42; 
        if (y < height - 1)
          errors[x][y + 1] = errors[x][y + 1] + 8 * error / 42; 
        if (x < width - 1 && y < height - 1)
          errors[x + 1][y + 1] = errors[x + 1][y + 1] + 4 * error / 42; 
        if (x < width - 2 && y < height - 1)
          errors[x + 2][y + 1] = errors[x + 2][y + 1] + 2 * error / 42; 
        if (x > 1 && y < height - 2)
          errors[x - 2][y + 2] = errors[x - 2][y + 2] + 1 * error / 42; 
        if (x > 0 && y < height - 2)
          errors[x - 1][y + 2] = errors[x - 1][y + 2] + 2 * error / 42; 
        if (y < height - 2)
          errors[x][y + 2] = errors[x][y + 2] + 4 * error / 42; 
        if (x < width - 1 && y < height - 2)
          errors[x + 1][y + 2] = errors[x + 1][y + 2] + 2 * error / 42; 
        if (x < width - 2 && y < height - 2)
          errors[x + 2][y + 2] = errors[x + 2][y + 2] + 1 * error / 42; 
        out.setPixel(x, y, Color.argb(alpha, gray, gray, gray));
        pixels_new[y * width + x] = Color.argb(alpha, gray, gray, gray);
      } 
    } 
    out.setPixels(pixels_new, 0, width, 0, 0, width, height);
    errors = null;
    pixels_orig = null;
    pixels_new = null;
    return out;
  }
  
  public void sendbitmap_gray_resize(int x_coordinates, int y_coordinates, Bitmap original_bitmap, int resize_width, int resize_height) {
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
    Bitmap resizedBitmap = Bitmap.createScaledBitmap(original_bitmap, resize_width, resize_height, false);
    binary_bitmap = resizedBitmap;
    String x_axis = Integer.toString(x_coordinates);
    String y_axis = Integer.toString(y_coordinates);
    String picture_wdith = Integer.toString((binary_bitmap.getWidth() + 7) / 8);
    String picture_height = Integer.toString(binary_bitmap.getHeight());
    String mode = Integer.toString(0);
    String command = "BITMAP " + x_axis + "," + y_axis + "," + picture_wdith + "," + picture_height + "," + mode + ",";
    byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
    int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
    int Width = binary_bitmap.getWidth();
    int Height = binary_bitmap.getHeight();
    for (int i = 0; i < Height * Width_bytes; i++)
      stream[i] = -1; 
    for (int y = 0; y < Height; y++) {
      for (int x = 0; x < Width; x++) {
        int pixelColor = binary_bitmap.getPixel(x, y);
        int colorA = Color.alpha(pixelColor);
        int colorR = Color.red(pixelColor);
        int colorG = Color.green(pixelColor);
        int colorB = Color.blue(pixelColor);
        int total = (colorR + colorG + colorB) / 3;
        if (colorA != 0)
          stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8)); 
      } 
    } 
    // Note: Network sendcommand calls removed - this method now only processes bitmaps
    // The caller needs to handle sending the command and stream data to the printer
  }
  
  public ArrayList<Bitmap> pdf_color(int x_axis, int y_axis, File f1) throws FileNotFoundException {
    ArrayList<Bitmap> bitmaps = new ArrayList<>();
    try {
      PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(f1, 268435456));
      int pageCount = renderer.getPageCount();
      int t = 0;
      if (t < pageCount) {
        PdfRenderer.Page page = renderer.openPage(t);
        int width = (int)((page.getWidth() * 72) / 25.4D);
        int height = (int)((page.getHeight() * 72) / 25.4D);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        bitmaps.add(bitmap);
        page.close();
        renderer.close();
        Bitmap gray_bitmap = null;
        Bitmap binary_bitmap = null;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(-16777216);
        paint.setAntiAlias(true);
        gray_bitmap = bitmap2Gray(bitmap);
        binary_bitmap = gray2Binary(gray_bitmap);
        String picture_wdith = Integer.toString((binary_bitmap.getWidth() + 7) / 8);
        String picture_height = Integer.toString(binary_bitmap.getHeight());
        String mode = Integer.toString(0);
        String command = "BITMAP " + x_axis + "," + y_axis + "," + picture_wdith + "," + picture_height + "," + mode + ",";
        byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
        int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
        int Width = binary_bitmap.getWidth();
        int Height = binary_bitmap.getHeight();
        for (int i = 0; i < Height * Width_bytes; i++)
          stream[i] = -1; 
        for (int y = 0; y < Height; y++) {
          for (int x = 0; x < Width; x++) {
            int pixelColor = binary_bitmap.getPixel(x, y);
            int colorA = Color.alpha(pixelColor);
            int colorR = Color.red(pixelColor);
            int colorG = Color.green(pixelColor);
            int colorB = Color.blue(pixelColor);
            int total = (colorR + colorG + colorB) / 3;
            if (total == 0)
              stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8)); 
          } 
        } 
        // Note: Network sendcommand calls removed - this method now only processes bitmaps
        // The caller needs to handle sending the command and stream data to the printer
        return bitmaps;
      } 
      renderer.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return bitmaps;
  }
  
  public ArrayList<Bitmap> pdf_color_resize(int x_axis, int y_axis, File f1, int resize_width, int resize_height) throws FileNotFoundException {
    ArrayList<Bitmap> bitmaps = new ArrayList<>();
    try {
      PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(f1, 268435456));
      int pageCount = renderer.getPageCount();
      int t = 0;
      if (t < pageCount) {
        PdfRenderer.Page page = renderer.openPage(t);
        int width = (int)((page.getWidth() * 72) / 25.4D);
        int height = (int)((page.getHeight() * 72) / 25.4D);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap resize_bitmap = Bitmap.createScaledBitmap(bitmap, resize_width, resize_height, false);
        page.render(resize_bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        bitmaps.add(resize_bitmap);
        page.close();
        renderer.close();
        Bitmap gray_bitmap = null;
        Bitmap binary_bitmap = null;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(-16777216);
        paint.setAntiAlias(true);
        gray_bitmap = bitmap2Gray(resize_bitmap);
        binary_bitmap = gray2Binary(gray_bitmap);
        String picture_wdith = Integer.toString((binary_bitmap.getWidth() + 7) / 8);
        String picture_height = Integer.toString(binary_bitmap.getHeight());
        String mode = Integer.toString(0);
        String command = "BITMAP " + x_axis + "," + y_axis + "," + picture_wdith + "," + picture_height + "," + mode + ",";
        byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
        int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
        int Width = binary_bitmap.getWidth();
        int Height = binary_bitmap.getHeight();
        for (int i = 0; i < Height * Width_bytes; i++)
          stream[i] = -1; 
        for (int y = 0; y < Height; y++) {
          for (int x = 0; x < Width; x++) {
            int pixelColor = binary_bitmap.getPixel(x, y);
            int colorA = Color.alpha(pixelColor);
            int colorR = Color.red(pixelColor);
            int colorG = Color.green(pixelColor);
            int colorB = Color.blue(pixelColor);
            int total = (colorR + colorG + colorB) / 3;
            if (total == 0)
              stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8)); 
          } 
        } 
        // Note: Network sendcommand calls removed - this method now only processes bitmaps
        // The caller needs to handle sending the command and stream data to the printer
        return bitmaps;
      } 
      renderer.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return bitmaps;
  }
  
  public ArrayList<Bitmap> pdf_color_save(int x_axis, int y_axis, File f1) throws FileNotFoundException {
    ArrayList<Bitmap> bitmaps = new ArrayList<>();
    try {
      PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(f1, 268435456));
      int pageCount = renderer.getPageCount();
      int t = 0;
      if (t < pageCount) {
        PdfRenderer.Page page = renderer.openPage(t);
        int width = (int)((page.getWidth() * 72) / 25.4D);
        int height = (int)((page.getHeight() * 72) / 25.4D);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        bitmaps.add(bitmap);
        page.close();
        renderer.close();
        Bitmap gray_bitmap = null;
        Bitmap binary_bitmap = null;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(-16777216);
        paint.setAntiAlias(true);
        gray_bitmap = bitmap2Gray(bitmap);
        binary_bitmap = gray2Binary(gray_bitmap);
        String picture_wdith = Integer.toString((binary_bitmap.getWidth() + 7) / 8);
        String picture_height = Integer.toString(binary_bitmap.getHeight());
        String mode = Integer.toString(0);
        String command = "BITMAP " + x_axis + "," + y_axis + "," + picture_wdith + "," + picture_height + "," + mode + ",";
        byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
        int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
        int Width = binary_bitmap.getWidth();
        int Height = binary_bitmap.getHeight();
        for (int i = 0; i < Height * Width_bytes; i++)
          stream[i] = -1; 
        for (int y = 0; y < Height; y++) {
          for (int x = 0; x < Width; x++) {
            int pixelColor = binary_bitmap.getPixel(x, y);
            int colorA = Color.alpha(pixelColor);
            int colorR = Color.red(pixelColor);
            int colorG = Color.green(pixelColor);
            int colorB = Color.blue(pixelColor);
            int total = (colorR + colorG + colorB) / 3;
            if (total == 0)
              stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8)); 
          } 
        } 
        String filename = "document.txt";
        File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File outfile = new File(sdcard, filename);
        FileOutputStream outStream = new FileOutputStream(outfile);
        byte[] bytes1 = command.getBytes(StandardCharsets.US_ASCII);
        byte[] bytes2 = "\r\n".getBytes(StandardCharsets.US_ASCII);
        outStream.write(bytes1);
        outStream.write(stream);
        outStream.write(bytes2);
        outStream.close();
        return bitmaps;
      } 
      renderer.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return bitmaps;
  }
  
  public ArrayList<Bitmap> pdf_gray(int x_axis, int y_axis, File f1) throws FileNotFoundException {
    ArrayList<Bitmap> bitmaps = new ArrayList<>();
    try {
      PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(f1, 268435456));
      int pageCount = renderer.getPageCount();
      int t = 0;
      if (t < pageCount) {
        PdfRenderer.Page page = renderer.openPage(t);
        int width = (int)((page.getWidth() * 72) / 25.4D);
        int height = (int)((page.getHeight() * 72) / 25.4D);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT);
        bitmaps.add(bitmap);
        page.close();
        renderer.close();
        Bitmap gray_bitmap = null;
        Bitmap binary_bitmap = null;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(-16777216);
        paint.setAntiAlias(true);
        binary_bitmap = bitmap;
        String picture_wdith = Integer.toString((binary_bitmap.getWidth() + 7) / 8);
        String picture_height = Integer.toString(binary_bitmap.getHeight());
        String mode = Integer.toString(0);
        String command = "BITMAP " + x_axis + "," + y_axis + "," + picture_wdith + "," + picture_height + "," + mode + ",";
        byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
        int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
        int Width = binary_bitmap.getWidth();
        int Height = binary_bitmap.getHeight();
        for (int i = 0; i < Height * Width_bytes; i++)
          stream[i] = -1; 
        for (int y = 0; y < Height; y++) {
          for (int x = 0; x < Width; x++) {
            int pixelColor = binary_bitmap.getPixel(x, y);
            int colorA = Color.alpha(pixelColor);
            int colorR = Color.red(pixelColor);
            int colorG = Color.green(pixelColor);
            int colorB = Color.blue(pixelColor);
            int total = (colorR + colorG + colorB) / 3;
            if (colorA != 0)
              stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8)); 
          } 
        } 
        // Note: Network sendcommand calls removed - this method now only processes bitmaps
        // The caller needs to handle sending the command and stream data to the printer
        return bitmaps;
      } 
      renderer.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return bitmaps;
  }
  
  public ArrayList<Bitmap> pdf_gray_resize(int x_axis, int y_axis, File f1, int resize_width, int resize_height) throws FileNotFoundException {
    ArrayList<Bitmap> bitmaps = new ArrayList<>();
    try {
      PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(f1, 268435456));
      int pageCount = renderer.getPageCount();
      int t = 0;
      if (t < pageCount) {
        PdfRenderer.Page page = renderer.openPage(t);
        int width = (int)((page.getWidth() * 72) / 25.4D);
        int height = (int)((page.getHeight() * 72) / 25.4D);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap resize_bitmap = Bitmap.createScaledBitmap(bitmap, resize_width, resize_height, false);
        page.render(resize_bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        bitmaps.add(resize_bitmap);
        page.close();
        renderer.close();
        Bitmap gray_bitmap = null;
        Bitmap binary_bitmap = null;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(-16777216);
        paint.setAntiAlias(true);
        binary_bitmap = resize_bitmap;
        String picture_wdith = Integer.toString((binary_bitmap.getWidth() + 7) / 8);
        String picture_height = Integer.toString(binary_bitmap.getHeight());
        String mode = Integer.toString(0);
        String command = "BITMAP " + x_axis + "," + y_axis + "," + picture_wdith + "," + picture_height + "," + mode + ",";
        byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
        int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
        int Width = binary_bitmap.getWidth();
        int Height = binary_bitmap.getHeight();
        for (int i = 0; i < Height * Width_bytes; i++)
          stream[i] = -1; 
        for (int y = 0; y < Height; y++) {
          for (int x = 0; x < Width; x++) {
            int pixelColor = binary_bitmap.getPixel(x, y);
            int colorA = Color.alpha(pixelColor);
            int colorR = Color.red(pixelColor);
            int colorG = Color.green(pixelColor);
            int colorB = Color.blue(pixelColor);
            int total = (colorR + colorG + colorB) / 3;
            if (colorA != 0)
              stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8)); 
          } 
        } 
        // Note: Network sendcommand calls removed - this method now only processes bitmaps
        // The caller needs to handle sending the command and stream data to the printer
        return bitmaps;
      } 
      renderer.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return bitmaps;
  }
  
  public ArrayList<Bitmap> pdf_gray_save(int x_axis, int y_axis, File f1) throws FileNotFoundException {
    ArrayList<Bitmap> bitmaps = new ArrayList<>();
    try {
      PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(f1, 268435456));
      int pageCount = renderer.getPageCount();
      int t = 0;
      if (t < pageCount) {
        PdfRenderer.Page page = renderer.openPage(t);
        int width = (int)((page.getWidth() * 72) / 25.4D);
        int height = (int)((page.getHeight() * 72) / 25.4D);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        bitmaps.add(bitmap);
        page.close();
        renderer.close();
        Bitmap gray_bitmap = null;
        Bitmap binary_bitmap = null;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(-16777216);
        paint.setAntiAlias(true);
        binary_bitmap = bitmap;
        String picture_wdith = Integer.toString((binary_bitmap.getWidth() + 7) / 8);
        String picture_height = Integer.toString(binary_bitmap.getHeight());
        String mode = Integer.toString(0);
        String command = "BITMAP " + x_axis + "," + y_axis + "," + picture_wdith + "," + picture_height + "," + mode + ",";
        byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
        int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
        int Width = binary_bitmap.getWidth();
        int Height = binary_bitmap.getHeight();
        for (int i = 0; i < Height * Width_bytes; i++)
          stream[i] = -1; 
        for (int y = 0; y < Height; y++) {
          for (int x = 0; x < Width; x++) {
            int pixelColor = binary_bitmap.getPixel(x, y);
            int colorA = Color.alpha(pixelColor);
            int colorR = Color.red(pixelColor);
            int colorG = Color.green(pixelColor);
            int colorB = Color.blue(pixelColor);
            int total = (colorR + colorG + colorB) / 3;
            if (colorA != 0)
              stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8)); 
          } 
        } 
        String filename = "document.txt";
        File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File outfile = new File(sdcard, filename);
        FileOutputStream outStream = new FileOutputStream(outfile);
        byte[] bytes1 = command.getBytes(StandardCharsets.US_ASCII);
        byte[] bytes2 = "\r\n".getBytes(StandardCharsets.US_ASCII);
        outStream.write(bytes1);
        outStream.write(stream);
        outStream.write(bytes2);
        outStream.close();
        return bitmaps;
      } 
      renderer.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return bitmaps;
  }
  
  public ArrayList<Bitmap> pdf_gray_print_resize2fitWidth(int x_axis, int y_axis, File f1, int resize_width) throws FileNotFoundException {
    ArrayList<Bitmap> bitmaps = new ArrayList<>();
    try {
      PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(f1, 268435456));
      int pageCount = renderer.getPageCount();
      for (int t = 0; t < pageCount; t++) {
        PdfRenderer.Page page = renderer.openPage(t);
        int width = (int)((page.getWidth() * 72) / 25.4D);
        int height = (int)((page.getHeight() * 72) / 25.4D);
        int resize_height = resize_width * height / width;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap resize_bitmap = Bitmap.createScaledBitmap(bitmap, resize_width, resize_height, false);
        page.render(resize_bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        bitmaps.add(resize_bitmap);
        page.close();
        Bitmap gray_bitmap = null;
        Bitmap binary_bitmap = null;
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(-16777216);
        paint.setAntiAlias(true);
        binary_bitmap = resize_bitmap;
        String picture_wdith = Integer.toString((binary_bitmap.getWidth() + 7) / 8);
        String picture_height = Integer.toString(binary_bitmap.getHeight());
        String mode = Integer.toString(0);
        String command = "BITMAP " + x_axis + "," + y_axis + "," + picture_wdith + "," + picture_height + "," + mode + ",";
        byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
        int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
        int Width = binary_bitmap.getWidth();
        int Height = binary_bitmap.getHeight();
        for (int i = 0; i < Height * Width_bytes; i++)
          stream[i] = -1; 
        for (int y = 0; y < Height; y++) {
          for (int x = 0; x < Width; x++) {
            int pixelColor = binary_bitmap.getPixel(x, y);
            int colorA = Color.alpha(pixelColor);
            int colorR = Color.red(pixelColor);
            int colorG = Color.green(pixelColor);
            int colorB = Color.blue(pixelColor);
            int total = (colorR + colorG + colorB) / 3;
            if (colorA != 0)
              stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8)); 
          } 
        } 
        // Note: Network sendcommand calls removed - this method now only processes bitmaps
        // The caller needs to handle sending the size, command, stream data, and print commands to the printer
      } 
      renderer.close();
      return bitmaps;
    } catch (Exception ex) {
      ex.printStackTrace();
      return bitmaps;
    } 
  }
  
  public Bitmap bitmap2Gray(Bitmap bmSrc) {
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
  
  public Bitmap lineGrey(Bitmap image) {
    int width = image.getWidth();
    int height = image.getHeight();
    Bitmap linegray = null;
    linegray = image.copy(Bitmap.Config.ARGB_8888, true);
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        int col = image.getPixel(i, j);
        int alpha = col & 0xFF000000;
        int red = (col & 0xFF0000) >> 16;
        int green = (col & 0xFF00) >> 8;
        int blue = col & 0xFF;
        red = (int)(1.1D * red + 30.0D);
        green = (int)(1.1D * green + 30.0D);
        blue = (int)(1.1D * blue + 30.0D);
        if (red >= 255)
          red = 255; 
        if (green >= 255)
          green = 255; 
        if (blue >= 255)
          blue = 255; 
        int newColor = alpha | red << 16 | green << 8 | blue;
        linegray.setPixel(i, j, newColor);
      } 
    } 
    return linegray;
  }
  
  public Bitmap gray2Binary(Bitmap graymap) {
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
        if (gray <= 127) {
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
  
  public Bitmap gray2Binary(Bitmap graymap, int threshold) {
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
  
  public Bitmap gray2halftone(Bitmap graymap) {
    int width = graymap.getWidth();
    int height = graymap.getHeight();
    Bitmap binarymap = null;
    binarymap = graymap.copy(Bitmap.Config.ARGB_8888, true);
    for (int i = 0; i < width - 1; i += 2) {
      for (int j = 0; j < height - 1; j += 2) {
        int col = binarymap.getPixel(i, j);
        int alpha = col & 0xFF000000;
        int red = (col & 0xFF0000) >> 16;
        int green = (col & 0xFF00) >> 8;
        int blue = col & 0xFF;
        int gray1 = (int)(red * 0.3D + green * 0.59D + blue * 0.11D);
        col = binarymap.getPixel(i + 1, j);
        alpha = col & 0xFF000000;
        red = (col & 0xFF0000) >> 16;
        green = (col & 0xFF00) >> 8;
        blue = col & 0xFF;
        int gray2 = (int)(red * 0.3D + green * 0.59D + blue * 0.11D);
        col = binarymap.getPixel(i, j + 1);
        alpha = col & 0xFF000000;
        red = (col & 0xFF0000) >> 16;
        green = (col & 0xFF00) >> 8;
        blue = col & 0xFF;
        int gray3 = (int)(red * 0.3D + green * 0.59D + blue * 0.11D);
        col = binarymap.getPixel(i + 1, j + 1);
        alpha = col & 0xFF000000;
        red = (col & 0xFF0000) >> 16;
        green = (col & 0xFF00) >> 8;
        blue = col & 0xFF;
        int gray4 = (int)(red * 0.3D + green * 0.59D + blue * 0.11D);
        int helftone = 1020 - gray1 + gray2 + gray3 + gray4;
        int whiteColor = -1;
        int blackColor = 0;
        if (helftone <= 204) {
          binarymap.setPixel(i, j, whiteColor);
          binarymap.setPixel(i + 1, j, whiteColor);
          binarymap.setPixel(i, j + 1, whiteColor);
          binarymap.setPixel(i + 1, j + 1, whiteColor);
        } else if (helftone <= 408) {
          binarymap.setPixel(i, j, whiteColor);
          binarymap.setPixel(i + 1, j, whiteColor);
          binarymap.setPixel(i, j + 1, whiteColor);
          binarymap.setPixel(i + 1, j + 1, blackColor);
        } else if (helftone <= 612) {
          binarymap.setPixel(i, j, whiteColor);
          binarymap.setPixel(i + 1, j, blackColor);
          binarymap.setPixel(i, j + 1, blackColor);
          binarymap.setPixel(i + 1, j + 1, whiteColor);
        } else if (helftone <= 816) {
          binarymap.setPixel(i, j, whiteColor);
          binarymap.setPixel(i + 1, j, blackColor);
          binarymap.setPixel(i, j + 1, blackColor);
          binarymap.setPixel(i + 1, j + 1, blackColor);
        } else {
          binarymap.setPixel(i, j, blackColor);
          binarymap.setPixel(i + 1, j, blackColor);
          binarymap.setPixel(i, j + 1, blackColor);
          binarymap.setPixel(i + 1, j + 1, blackColor);
        } 
      } 
    } 
    return binarymap;
  }
  
  public static String byteArrayToHex(byte[] a) {
    StringBuilder sb = new StringBuilder(a.length * 2);
    byte b;
    int i;
    byte[] arrayOfByte;
    for (i = (arrayOfByte = a).length, b = 0; b < i; ) {
      byte b1 = arrayOfByte[b];
      sb.append(String.format("%02x", new Object[] { Integer.valueOf(b1 & 0xFF) }));
      b++;
    } 
    return sb.toString();
  }
  

  
  
}
