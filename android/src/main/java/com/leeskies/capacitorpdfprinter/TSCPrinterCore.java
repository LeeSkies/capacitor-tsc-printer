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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class TSCPrinterCore {
  private static final String TAG = "Printerנו";
  
  private static final boolean D = true;
  
  private Context context;
  
  private InputStream InStream = null;
  
  private OutputStream OutStream = null;
  
  private Socket socket = null;
  
  private String printerstatus = "";
  
  private int last_bytes = 0;
  
  private byte[] buffer = new byte[1024];
  
  private byte[] readBuf = new byte[1024];
  
  private static String receive_data = "";
  
  private Button connect = null;
  
  private Button closeport = null;
  
  private Button sendfile = null;
  
  private Button status = null;
  
  private TextView tv1 = null;
  
  private TextView tv2 = null;
  
  private static int counter = 0;
  
  private byte[] udp_data = new byte[512];
  
  private static byte[] udpbyte = new byte[1024];
  
  private int[] decipaddress = new int[200];
  
  private static String[] get_NAME = new String[100];
  
  private static String[] get_IP = new String[100];
  
  private static String[] get_MAC = new String[100];
  
  private static String[] get_status = new String[100];
  
  public String[] UDP_NAME = new String[100];
  
  public String[] UDP_IP = new String[100];
  
  public String[] UDP_MAC = new String[100];
  
  public String[] UDP_status = new String[100];
  
  private int port_connected = 0;

  public TSCPrinterCore(Context context) {
    this.context = context;
  }

  public String openport(String ipaddress, int portnumber) {
    Log.e("openport", Thread.currentThread().toString());
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
      this.socket.connect(new InetSocketAddress(ipaddress, portnumber), 2000);
      this.InStream = this.socket.getInputStream();
      this.OutStream = this.socket.getOutputStream();
      this.port_connected = 1;
    } catch (Exception ex) {
      try {
        this.socket.close();
      } catch (IOException e) {
        this.port_connected = 0;
        return "-2";
      } 
      this.port_connected = 0;
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    } 
    return "1";
  }
  
  public String openport(String ipaddress, int portnumber, int delay) {
    Log.e("openport", Thread.currentThread().toString());
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
      this.socket.connect(new InetSocketAddress(ipaddress, portnumber), 2000);
      this.InStream = this.socket.getInputStream();
      this.OutStream = this.socket.getOutputStream();
      this.port_connected = 1;
    } catch (Exception ex) {
      try {
        this.socket.close();
      } catch (IOException e) {
        this.port_connected = 0;
        return "-2";
      } 
      this.port_connected = 0;
      return "-1";
    } 
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    } 
    return "1";
  }
  
  public String openport_time(String ipaddress, int portnumber, int timeout) {
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
      this.socket.connect(new InetSocketAddress(ipaddress, portnumber), timeout);
      this.InStream = this.socket.getInputStream();
      this.OutStream = this.socket.getOutputStream();
      this.port_connected = 1;
    } catch (Exception ex) {
      this.port_connected = 0;
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    } 
    return "1";
  }
  
  public String openport_thread(final String ipaddress, final int portnumber) {
    Log.e("openport_thread", Thread.currentThread().toString());
    Thread thread = new Thread(new Runnable() {
          public void run() {
            Log.e("openport_thread2", Thread.currentThread().toString());
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
              TSCPrinterCore.this.socket = new Socket();
              TSCPrinterCore.this.socket.connect(new InetSocketAddress(ipaddress, portnumber), 2000);
              TSCPrinterCore.this.InStream = TSCPrinterCore.this.socket.getInputStream();
              TSCPrinterCore.this.OutStream = TSCPrinterCore.this.socket.getOutputStream();
              TSCPrinterCore.this.port_connected = 1;
            } catch (Exception ex) {
              TSCPrinterCore.this.port_connected = 0;
            } 
          }
        });
    thread.start();
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    } 
    return "1";
  }
  
  public String openport_thread(final String ipaddress, final int portnumber, int delay) {
    Log.e("openport_thread", Thread.currentThread().toString());
    Thread thread = new Thread(new Runnable() {
          public void run() {
            Log.e("openport_thread2", Thread.currentThread().toString());
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
              TSCPrinterCore.this.socket = new Socket();
              TSCPrinterCore.this.socket.connect(new InetSocketAddress(ipaddress, portnumber), 2000);
              TSCPrinterCore.this.InStream = TSCPrinterCore.this.socket.getInputStream();
              TSCPrinterCore.this.OutStream = TSCPrinterCore.this.socket.getOutputStream();
              TSCPrinterCore.this.port_connected = 1;
            } catch (Exception ex) {
              TSCPrinterCore.this.port_connected = 0;
            } 
          }
        });
    thread.start();
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    } 
    return "1";
  }
  
  public String sendcommand_thread(final String message) {
    if (this.port_connected == 0)
      return "-1"; 
    Log.e("sendcommand_thread", Thread.currentThread().toString());
    Thread thread = new Thread(new Runnable() {
          public void run() {
            Log.e("sendcommand_thread2", Thread.currentThread().toString());
            byte[] msgBuffer = message.getBytes();
            try {
              TSCPrinterCore.this.OutStream.write(msgBuffer);
            } catch (IOException e) {
              e.printStackTrace();
            } 
          }
        });
    thread.start();
    return "1";
  }
  
  public synchronized String sendcommand(String message) {
    if (this.port_connected == 0)
      return "-1"; 
    if (this.OutStream == null)
      return "-1";
    byte[] msgBuffer = message.getBytes();
    try {
      this.OutStream.write(msgBuffer);
      this.OutStream.flush();
    } catch (IOException e) {
      Log.e(TAG, "IOException in sendcommand: " + e.getMessage());
      e.printStackTrace();
      return "-1";
    } 
    return "1";
  }
  
  public String sendcommandUTF8(String message) {
    if (this.port_connected == 0)
      return "-1"; 
    byte[] msgBuffer = null;
    try {
      msgBuffer = message.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
    } 
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      return "-1";
    } 
    return "1";
  }
  
  public String sendcommandBig5(String message) {
    if (this.port_connected == 0)
      return "-1"; 
    byte[] msgBuffer = null;
    try {
      msgBuffer = message.getBytes("big5");
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
    } 
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      return "-1";
    } 
    return "1";
  }
  
  public String sendcommandGB2312(String message) {
    if (this.port_connected == 0)
      return "-1"; 
    byte[] msgBuffer = null;
    try {
      msgBuffer = message.getBytes("GB2312");
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
    } 
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      return "-1";
    } 
    return "1";
  }
  
  public String sendcommand(byte[] message) {
    if (this.port_connected == 0)
      return "-1"; 
    try {
      this.OutStream.write(message);
    } catch (IOException e) {
      return "-1";
    } 
    return "1";
  }
  
  public String status() {
    if (this.port_connected == 0)
      return "-1"; 
    byte[] message = { 27, 33, 83 };
    this.readBuf = new byte[1024];
    try {
      this.OutStream.write(message);
    } catch (IOException e) {
      return "-1";
    } 
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 0) {
          this.readBuf = new byte[1024];
          int i = this.InStream.read(this.readBuf);
        } 
      } catch (IOException iOException) {
        return "-1";
      } 
    }
    try {
      while (this.InStream.available() <= 0) {
        this.readBuf = new byte[1024];
        int i = this.InStream.read(this.readBuf);
      }
    } catch (IOException e) {
      return "-1";
    }
    return "1";
  }
  
  public String status(int delay) {
    if (this.port_connected == 0)
      return "-1"; 
    byte[] message = { 27, 33, 83 };
    this.readBuf = new byte[1024];
    try {
      this.OutStream.write(message);
    } catch (IOException e) {
      return "-1";
    } 
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 0) {
          this.readBuf = new byte[1024];
          int i = this.InStream.read(this.readBuf);
        } 
      } catch (IOException iOException) {
        return "-1";
      } 
    }
    try {
      while (this.InStream.available() <= 0) {
        this.readBuf = new byte[1024];
        int i = this.InStream.read(this.readBuf);
      }
    } catch (IOException e) {
      return "-1";
    }
    return "1";
  }
  
  public String status(int delay1, int delay2) {
    if (this.port_connected == 0)
      return "-1"; 
    byte[] message = { 27, 33, 83 };
    this.readBuf = new byte[1024];
    String printername = "";
    printername = printername(delay1);
    int name_length = printername.toString().trim().length();
    if (name_length < 3)
      return "-1"; 
    try {
      this.OutStream.write(message);
    } catch (IOException e) {
      return "-1";
    } 
    try {
      Thread.sleep(delay2);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 0) {
          this.readBuf = new byte[1024];
          int i = this.InStream.read(this.readBuf);
        } 
      } catch (IOException iOException) {
        return "-1";
      } 
    }
    try {
      while (this.InStream.available() <= 0) {
        this.readBuf = new byte[1024];
        int i = this.InStream.read(this.readBuf);
      }
    } catch (IOException e) {
      return "-1";
    }
     return "1";
  }
  
  public String printer_completestatus() {
    if (this.port_connected == 0)
      return "-1"; 
    byte[] message = { 27, 33, 83 };
    try {
      this.OutStream.write(message);
    } catch (IOException e) {
      return "-1";
    } 
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    try {
      do {
      
      } while (this.InStream.available() <= 0);
      this.readBuf = new byte[1024];
      int length = this.InStream.read(this.readBuf);
      if (length > 0) {
        String completestatus = new String(this.readBuf, 1, 4);
        return completestatus;
      } 
      return "-1";
    } catch (IOException e) {
      return "-1";
    } 
  }
  
  public String printer_completestatus(int delaytime) {
    if (this.port_connected == 0)
      return "-1"; 
    byte[] message = { 27, 33, 83 };
    try {
      this.OutStream.write(message);
    } catch (IOException e) {
      return "-1";
    } 
    try {
      Thread.sleep(delaytime);
    } catch (InterruptedException interruptedException) {}
    try {
      do {
      
      } while (this.InStream.available() <= 0);
      this.readBuf = new byte[1024];
      int length = this.InStream.read(this.readBuf);
      if (length > 0) {
        String completestatus = new String(this.readBuf, 1, 4);
        return completestatus;
      } 
      return "-1";
    } catch (IOException e) {
      return "-1";
    } 
  }
  
  private String batch() {
    if (this.port_connected == 0)
      return "Connected Error"; 
    int printvalue = 0;
    String printbatch = "";
    String stringbatch = "0";
    String message = "~HS";
    this.readBuf = new byte[1024];
    byte[] batcharray = { 48, 48, 48, 48, 48, 48, 48, 48 };
    byte[] msgBuffer = message.getBytes();
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      Log.e("THINBTCLIENT", "ON RESUME: Exception during write.", e);
    } 
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 50) {
          this.readBuf = new byte[1024];
          int i = this.InStream.read(this.readBuf);
        } 
      } catch (IOException iOException) {
        iOException.printStackTrace();
      } 
    }
    try {
      while (this.InStream.available() <= 50) {
        this.readBuf = new byte[1024];
        int i = this.InStream.read(this.readBuf);
      }
    } catch (IOException e) {
      return "-1";
    }
     return "1";
  }
  
  public String printername() {
    if (this.port_connected == 0)
      return "-1"; 
    String message = "~!T";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      return "-1";
    } 
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 5) {
          this.readBuf = new byte[1024];
          int i = this.InStream.read(this.readBuf);
        } 
      } catch (IOException iOException) {
        return "-1";
      } 
    } 
    try {
      while (this.InStream.available() <= 5) {
        this.readBuf = new byte[1024];
        int i = this.InStream.read(this.readBuf);
      }
    } catch (IOException IOException) {
      return "-1";
    }
    return "1";
  }
  
  public String printername(int delay) {
    if (this.port_connected == 0)
      return "-1"; 
    String message = "~!T";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      return "-1";
    } 
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 5) {
          this.readBuf = new byte[1024];
          int i = this.InStream.read(this.readBuf);
        } 
      } catch (IOException iOException) {
        return "-1";
      } 
    } 
    try {
      while (this.InStream.available() <= 5) {
        this.readBuf = new byte[1024];
        int i = this.InStream.read(this.readBuf);
      }
    } catch (IOException IOException) {
      return "-1";
    }
    return "1";
  }
  
  public String printermemory() {
    if (this.port_connected == 0)
      return "-1"; 
    String message = "~!A";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      Log.e("THINBTCLIENT", "ON RESUME: Exception during write.", e);
    } 
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 10) {
          this.readBuf = new byte[1024];
          int i = this.InStream.read(this.readBuf);
        } 
      } catch (IOException iOException) {
        return "-1";
      } 
    }
    try {
      while (this.InStream.available() <= 10) {
        this.readBuf = new byte[1024];
        int i = this.InStream.read(this.readBuf);
      }
    } catch (IOException e) {
      return "-1";
    }
     return "1";
  }
  
  public String printermemory(int delay) {
    if (this.port_connected == 0)
      return "-1"; 
    String message = "~!A";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      Log.e("THINBTCLIENT", "ON RESUME: Exception during write.", e);
    } 
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 10) {
          this.readBuf = new byte[1024];
          int i = this.InStream.read(this.readBuf);
        } 
      } catch (IOException iOException) {
        return "-1";
      } 
    }
    try {
      while (this.InStream.available() <= 10) {
        this.readBuf = new byte[1024];
        int i = this.InStream.read(this.readBuf);
      }
    } catch (IOException e) {
      return "-1";
    }
     return "1";
  }
  
  public String printermileage() {
    if (this.port_connected == 0)
      return "-1"; 
    String message = "~!@";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      Log.e("THINBTCLIENT", "ON RESUME: Exception during write.", e);
    } 
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 1) {
          this.readBuf = new byte[1024];
          int i = this.InStream.read(this.readBuf);
        } 
      } catch (IOException iOException) {
        return "-1";
      } 
    } 
    try {
      while (this.InStream.available() <= 1) {
        this.readBuf = new byte[1024];
        int i = this.InStream.read(this.readBuf);
      }
    } catch (IOException IOException) {
      return "-1";
    }
    return "1";
  }
  
  public String printermileage(int delay) {
    if (this.port_connected == 0)
      return "-1"; 
    String message = "~!@";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      Log.e("THINBTCLIENT", "ON RESUME: Exception during write.", e);
    } 
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 1) {
          this.readBuf = new byte[1024];
          int i = this.InStream.read(this.readBuf);
        } 
      } catch (IOException iOException) {
        return "-1";
      } 
    } 
    try {
      while (this.InStream.available() <= 1) {
        this.readBuf = new byte[1024];
        int i = this.InStream.read(this.readBuf);
      }
    } catch (IOException IOException) {
      return "-1";
    }
    return "1";
  }
  
  public String sendcommand_getstring(String message) {
    if (this.port_connected == 0)
      return "-1"; 
    String end_judge = "OUT \"ENDLINE\"\r\n";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    byte[] msgBuffer1 = "\r\n".getBytes();
    byte[] msgBuffer2 = end_judge.getBytes();
    try {
      this.OutStream.write(msgBuffer);
      this.OutStream.write(msgBuffer1);
      this.OutStream.write(msgBuffer2);
    } catch (IOException e) {
      Log.e("THINBTCLIENT", "Exception during write\r\n");
      e.printStackTrace();
      return "-1";
    } 
    try {
      do {
      
      } while (!ReadStream_judge());
    } catch (Exception e) {
      return "-1";
    } 
    return receive_data;
  }
  
  public String sendcommand_getstring(String message, int delay) {
    if (this.port_connected == 0)
      return "-1"; 
    String end_judge = "OUT \"ENDLINE\"\r\n";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    byte[] msgBuffer1 = "\r\n".getBytes();
    byte[] msgBuffer2 = end_judge.getBytes();
    try {
      this.OutStream.write(msgBuffer);
      this.OutStream.write(msgBuffer1);
      this.OutStream.write(msgBuffer2);
    } catch (IOException e) {
      Log.e("THINBTCLIENT", "Exception during write\r\n");
      e.printStackTrace();
      return "-1";
    } 
    try {
      do {
      
      } while (!ReadStream_judge(delay));
    } catch (Exception e) {
      return "-1";
    } 
    return receive_data;
  }
  
  private boolean ReadStream_judge() {
    // Byte code:
    //   0: ldc ''
    //   2: putstatic com/example/tscdll/TSCPrinterCore.receive_data : Ljava/lang/String;
    //   5: ldc2_w 1000
    //   8: invokestatic sleep : (J)V
    //   11: goto -> 82
    //   14: astore_1
    //   15: aload_1
    //   16: invokevirtual printStackTrace : ()V
    //   19: goto -> 82
    //   22: aload_0
    //   23: getfield InStream : Ljava/io/InputStream;
    //   26: aload_0
    //   27: getfield readBuf : [B
    //   30: invokevirtual read : ([B)I
    //   33: istore_1
    //   34: getstatic com/example/tscdll/TSCPrinterCore.receive_data : Ljava/lang/String;
    //   37: new java/lang/StringBuilder
    //   40: dup_x1
    //   41: swap
    //   42: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   45: invokespecial <init> : (Ljava/lang/String;)V
    //   48: new java/lang/String
    //   51: dup
    //   52: aload_0
    //   53: getfield readBuf : [B
    //   56: invokespecial <init> : ([B)V
    //   59: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   62: invokevirtual toString : ()Ljava/lang/String;
    //   65: putstatic com/example/tscdll/TSCPrinterCore.receive_data : Ljava/lang/String;
    //   68: ldc2_w 100
    //   71: invokestatic sleep : (J)V
    //   74: goto -> 82
    //   77: astore_2
    //   78: aload_2
    //   79: invokevirtual printStackTrace : ()V
    //   82: aload_0
    //   83: getfield InStream : Ljava/io/InputStream;
    //   86: invokevirtual available : ()I
    //   89: iconst_1
    //   90: if_icmpgt -> 22
    //   93: goto -> 99
    //   96: astore_1
    //   97: iconst_0
    //   98: ireturn
    //   99: getstatic com/example/tscdll/TSCPrinterCore.receive_data : Ljava/lang/String;
    //   102: ldc_w 'ENDLINE\\r\\n'
    //   105: invokevirtual contains : (Ljava/lang/CharSequence;)Z
    //   108: ifeq -> 5
    //   111: getstatic com/example/tscdll/TSCPrinterCore.receive_data : Ljava/lang/String;
    //   114: ldc_w 'ENDLINE\\r\\n'
    //   117: ldc ''
    //   119: invokevirtual replace : (Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   122: putstatic com/example/tscdll/TSCPrinterCore.receive_data : Ljava/lang/String;
    //   125: iconst_1
    //   126: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #1928	-> 0
    //   #1933	-> 5
    //   #1934	-> 11
    //   #1936	-> 15
    //   #1941	-> 19
    //   #1944	-> 22
    //   #1945	-> 34
    //   #1948	-> 68
    //   #1949	-> 74
    //   #1951	-> 78
    //   #1941	-> 82
    //   #1956	-> 93
    //   #1957	-> 96
    //   #1959	-> 97
    //   #1962	-> 99
    //   #1964	-> 111
    //   #1965	-> 125
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   0	127	0	this	Lcom/example/tscdll/TSCPrinterCore;
    //   15	4	1	e1	Ljava/lang/InterruptedException;
    //   34	48	1	length	I
    //   78	4	2	e1	Ljava/lang/InterruptedException;
    //   97	2	1	e	Ljava/io/IOException;
    // Exception table:
    //   from	to	target	type
    //   5	11	14	java/lang/InterruptedException
    //   19	93	96	java/io/IOException
    //   68	74	77	java/lang/InterruptedException
    // PLACEHOLDER RETURN VALUE
    return true;
  }
  
  private boolean ReadStream_judge(int delay) {
    // Byte code:
    //   0: ldc ''
    //   2: putstatic com/example/tscdll/TSCPrinterCore.receive_data : Ljava/lang/String;
    //   5: iload_1
    //   6: i2l
    //   7: invokestatic sleep : (J)V
    //   10: goto -> 81
    //   13: astore_2
    //   14: aload_2
    //   15: invokevirtual printStackTrace : ()V
    //   18: goto -> 81
    //   21: aload_0
    //   22: getfield InStream : Ljava/io/InputStream;
    //   25: aload_0
    //   26: getfield readBuf : [B
    //   29: invokevirtual read : ([B)I
    //   32: istore_2
    //   33: getstatic com/example/tscdll/TSCPrinterCore.receive_data : Ljava/lang/String;
    //   36: new java/lang/StringBuilder
    //   39: dup_x1
    //   40: swap
    //   41: invokestatic valueOf : (Ljava/lang/Object;)Ljava/lang/String;
    //   44: invokespecial <init> : (Ljava/lang/String;)V
    //   47: new java/lang/String
    //   50: dup
    //   51: aload_0
    //   52: getfield readBuf : [B
    //   55: invokespecial <init> : ([B)V
    //   58: invokevirtual append : (Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   61: invokevirtual toString : ()Ljava/lang/String;
    //   64: putstatic com/example/tscdll/TSCPrinterCore.receive_data : Ljava/lang/String;
    //   67: ldc2_w 100
    //   70: invokestatic sleep : (J)V
    //   73: goto -> 81
    //   76: astore_3
    //   77: aload_3
    //   78: invokevirtual printStackTrace : ()V
    //   81: aload_0
    //   82: getfield InStream : Ljava/io/InputStream;
    //   85: invokevirtual available : ()I
    //   88: iconst_1
    //   89: if_icmpgt -> 21
    //   92: goto -> 98
    //   95: astore_2
    //   96: iconst_0
    //   97: ireturn
    //   98: getstatic com/example/tscdll/TSCPrinterCore.receive_data : Ljava/lang/String;
    //   101: ldc_w 'ENDLINE\\r\\n'
    //   104: invokevirtual contains : (Ljava/lang/CharSequence;)Z
    //   107: ifeq -> 5
    //   110: getstatic com/example/tscdll/TSCPrinterCore.receive_data : Ljava/lang/String;
    //   113: ldc_w 'ENDLINE\\r\\n'
    //   116: ldc ''
    //   118: invokevirtual replace : (Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;
    //   121: putstatic com/example/tscdll/TSCPrinterCore.receive_data : Ljava/lang/String;
    //   124: iconst_1
    //   125: ireturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #1976	-> 0
    //   #1981	-> 5
    //   #1982	-> 10
    //   #1984	-> 14
    //   #1989	-> 18
    //   #1992	-> 21
    //   #1993	-> 33
    //   #1996	-> 67
    //   #1997	-> 73
    //   #1999	-> 77
    //   #1989	-> 81
    //   #2004	-> 92
    //   #2005	-> 95
    //   #2007	-> 96
    //   #2010	-> 98
    //   #2012	-> 110
    //   #2013	-> 124
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   0	126	0	this	Lcom/example/tscdll/TSCPrinterCore;
    //   0	126	1	delay	I
    //   14	4	2	e1	Ljava/lang/InterruptedException;
    //   33	48	2	length	I
    //   77	4	3	e1	Ljava/lang/InterruptedException;
    //   96	2	2	e	Ljava/io/IOException;
    // Exception table:
    //   from	to	target	type
    //   5	10	13	java/lang/InterruptedException
    //   18	92	95	java/io/IOException
    //   67	73	76	java/lang/InterruptedException
    // PLACEHOLDER RETURN VALUE
    return true;
  }
  
  public String printercodepage() {
    if (this.port_connected == 0)
      return "-1"; 
    String message = "~!I";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      Log.e("THINBTCLIENT", "ON RESUME: Exception during write.", e);
    } 
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 5) {
          this.readBuf = new byte[1024];
          int i = this.InStream.read(this.readBuf);
        } 
      } catch (IOException iOException) {
        return "-1";
      } 
    } 
    try {
      while (this.InStream.available() <= 5) {
        this.readBuf = new byte[1024];
        int i = this.InStream.read(this.readBuf);
      }
    } catch (IOException IOException) {
      return "-1";
    }
    return "1";
  }
  
  public String printercodepage(int delay) {
    if (this.port_connected == 0)
      return "-1"; 
    String message = "~!I";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      Log.e("THINBTCLIENT", "ON RESUME: Exception during write.", e);
    } 
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 5) {
          this.readBuf = new byte[1024];
          int i = this.InStream.read(this.readBuf);
        } 
      } catch (IOException iOException) {
        return "-1";
      } 
    } 
    try {
      while (this.InStream.available() <= 5) {
        this.readBuf = new byte[1024];
        int i = this.InStream.read(this.readBuf);
      }
    } catch (IOException IOException) {
      return "-1";
    }
    return "1";
  }
  
  public String printerfile() {
    // Byte code:
    //   0: aload_0
    //   1: getfield port_connected : I
    //   4: ifne -> 11
    //   7: ldc_w '-1'
    //   10: areturn
    //   11: ldc_w '~!F'
    //   14: astore_1
    //   15: aload_0
    //   16: sipush #1024
    //   19: newarray byte
    //   21: putfield readBuf : [B
    //   24: aload_0
    //   25: sipush #1024
    //   28: newarray byte
    //   30: putfield buffer : [B
    //   33: sipush #1024
    //   36: newarray byte
    //   38: astore_2
    //   39: iconst_0
    //   40: istore_3
    //   41: iconst_0
    //   42: istore #4
    //   44: aload_1
    //   45: invokevirtual getBytes : ()[B
    //   48: astore #5
    //   50: aload_0
    //   51: getfield OutStream : Ljava/io/OutputStream;
    //   54: aload #5
    //   56: invokevirtual write : ([B)V
    //   59: goto -> 75
    //   62: astore #6
    //   64: ldc 'THINBTCLIENT'
    //   66: ldc_w 'ON RESUME: Exception during write.'
    //   69: aload #6
    //   71: invokestatic e : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    //   74: pop
    //   75: ldc2_w 2000
    //   78: invokestatic sleep : (J)V
    //   81: goto -> 149
    //   84: astore #6
    //   86: aload #6
    //   88: invokevirtual printStackTrace : ()V
    //   91: goto -> 149
    //   94: aload_0
    //   95: getfield InStream : Ljava/io/InputStream;
    //   98: aload_0
    //   99: getfield readBuf : [B
    //   102: invokevirtual read : ([B)I
    //   105: istore #4
    //   107: iload #4
    //   109: iflt -> 132
    //   112: aload_0
    //   113: aload_0
    //   114: getfield readBuf : [B
    //   117: putfield buffer : [B
    //   120: aload_0
    //   121: getfield buffer : [B
    //   124: iconst_0
    //   125: aload_2
    //   126: iload_3
    //   127: iload #4
    //   129: invokestatic arraycopy : (Ljava/lang/Object;ILjava/lang/Object;II)V
    //   132: aload_0
    //   133: sipush #1024
    //   136: newarray byte
    //   138: putfield buffer : [B
    //   141: iload #4
    //   143: istore_3
    //   144: aload_0
    //   145: aload_2
    //   146: putfield readBuf : [B
    //   149: aload_0
    //   150: getfield InStream : Ljava/io/InputStream;
    //   153: invokevirtual available : ()I
    //   156: ifgt -> 94
    //   159: goto -> 168
    //   162: astore #6
    //   164: ldc_w '-1'
    //   167: areturn
    //   168: new java/lang/String
    //   171: dup
    //   172: aload_0
    //   173: getfield readBuf : [B
    //   176: invokespecial <init> : ([B)V
    //   179: astore #6
    //   181: aload #6
    //   183: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #2137	-> 0
    //   #2139	-> 7
    //   #2142	-> 11
    //   #2143	-> 15
    //   #2144	-> 24
    //   #2145	-> 33
    //   #2146	-> 39
    //   #2147	-> 41
    //   #2148	-> 44
    //   #2150	-> 50
    //   #2151	-> 59
    //   #2152	-> 64
    //   #2157	-> 75
    //   #2158	-> 81
    //   #2160	-> 86
    //   #2166	-> 91
    //   #2171	-> 94
    //   #2173	-> 107
    //   #2175	-> 112
    //   #2176	-> 120
    //   #2179	-> 132
    //   #2180	-> 141
    //   #2181	-> 144
    //   #2166	-> 149
    //   #2188	-> 159
    //   #2189	-> 162
    //   #2191	-> 164
    //   #2198	-> 168
    //   #2199	-> 181
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   0	184	0	this	Lcom/example/tscdll/TSCPrinterCore;
    //   15	169	1	message	Ljava/lang/String;
    //   39	145	2	buffer2	[B
    //   41	143	3	last_bytes	I
    //   44	140	4	bytes	I
    //   50	134	5	msgBuffer	[B
    //   64	11	6	e	Ljava/io/IOException;
    //   86	5	6	e	Ljava/lang/InterruptedException;
    //   164	4	6	e	Ljava/io/IOException;
    //   181	3	6	files	Ljava/lang/String;
    // Exception table:
    //   from	to	target	type
    //   50	59	62	java/io/IOException
    //   75	81	84	java/lang/InterruptedException
    //   91	159	162	java/io/IOException
    // PLACEHOLDER RETURN VALUE
    return "";
  }
  
  public String printerfile(int delay) {
    // Byte code:
    //   0: aload_0
    //   1: getfield port_connected : I
    //   4: ifne -> 11
    //   7: ldc_w '-1'
    //   10: areturn
    //   11: ldc_w '~!F'
    //   14: astore_2
    //   15: aload_0
    //   16: sipush #1024
    //   19: newarray byte
    //   21: putfield readBuf : [B
    //   24: aload_0
    //   25: sipush #1024
    //   28: newarray byte
    //   30: putfield buffer : [B
    //   33: sipush #1024
    //   36: newarray byte
    //   38: astore_3
    //   39: iconst_0
    //   40: istore #4
    //   42: iconst_0
    //   43: istore #5
    //   45: aload_2
    //   46: invokevirtual getBytes : ()[B
    //   49: astore #6
    //   51: aload_0
    //   52: getfield OutStream : Ljava/io/OutputStream;
    //   55: aload #6
    //   57: invokevirtual write : ([B)V
    //   60: goto -> 76
    //   63: astore #7
    //   65: ldc 'THINBTCLIENT'
    //   67: ldc_w 'ON RESUME: Exception during write.'
    //   70: aload #7
    //   72: invokestatic e : (Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    //   75: pop
    //   76: iload_1
    //   77: i2l
    //   78: invokestatic sleep : (J)V
    //   81: goto -> 151
    //   84: astore #7
    //   86: aload #7
    //   88: invokevirtual printStackTrace : ()V
    //   91: goto -> 151
    //   94: aload_0
    //   95: getfield InStream : Ljava/io/InputStream;
    //   98: aload_0
    //   99: getfield readBuf : [B
    //   102: invokevirtual read : ([B)I
    //   105: istore #5
    //   107: iload #5
    //   109: iflt -> 133
    //   112: aload_0
    //   113: aload_0
    //   114: getfield readBuf : [B
    //   117: putfield buffer : [B
    //   120: aload_0
    //   121: getfield buffer : [B
    //   124: iconst_0
    //   125: aload_3
    //   126: iload #4
    //   128: iload #5
    //   130: invokestatic arraycopy : (Ljava/lang/Object;ILjava/lang/Object;II)V
    //   133: aload_0
    //   134: sipush #1024
    //   137: newarray byte
    //   139: putfield buffer : [B
    //   142: iload #5
    //   144: istore #4
    //   146: aload_0
    //   147: aload_3
    //   148: putfield readBuf : [B
    //   151: aload_0
    //   152: getfield InStream : Ljava/io/InputStream;
    //   155: invokevirtual available : ()I
    //   158: ifgt -> 94
    //   161: goto -> 170
    //   164: astore #7
    //   166: ldc_w '-1'
    //   169: areturn
    //   170: new java/lang/String
    //   173: dup
    //   174: aload_0
    //   175: getfield readBuf : [B
    //   178: invokespecial <init> : ([B)V
    //   181: astore #7
    //   183: aload #7
    //   185: areturn
    // Line number table:
    //   Java source line number -> byte code offset
    //   #2213	-> 0
    //   #2215	-> 7
    //   #2218	-> 11
    //   #2219	-> 15
    //   #2220	-> 24
    //   #2221	-> 33
    //   #2222	-> 39
    //   #2223	-> 42
    //   #2224	-> 45
    //   #2226	-> 51
    //   #2227	-> 60
    //   #2228	-> 65
    //   #2233	-> 76
    //   #2234	-> 81
    //   #2236	-> 86
    //   #2242	-> 91
    //   #2247	-> 94
    //   #2249	-> 107
    //   #2251	-> 112
    //   #2252	-> 120
    //   #2255	-> 133
    //   #2256	-> 142
    //   #2257	-> 146
    //   #2242	-> 151
    //   #2264	-> 161
    //   #2265	-> 164
    //   #2267	-> 166
    //   #2274	-> 170
    //   #2275	-> 183
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   0	186	0	this	Lcom/example/tscdll/TSCPrinterCore;
    //   0	186	1	delay	I
    //   15	171	2	message	Ljava/lang/String;
    //   39	147	3	buffer2	[B
    //   42	144	4	last_bytes	I
    //   45	141	5	bytes	I
    //   51	135	6	msgBuffer	[B
    //   65	11	7	e	Ljava/io/IOException;
    //   86	5	7	e	Ljava/lang/InterruptedException;
    //   166	4	7	e	Ljava/io/IOException;
    //   183	3	7	files	Ljava/lang/String;
    // Exception table:
    //   from	to	target	type
    //   51	60	63	java/io/IOException
    //   76	81	84	java/lang/InterruptedException
    //   91	161	164	java/io/IOException
    // PLACEHOLDER RETURN VALUE
    return "";
  }
  
  public void restart() {
    if (this.port_connected == 0)
      return; 
    byte[] message = { 27, 33, 82 };
    try {
      this.OutStream.write(message);
    } catch (IOException e) {
      Log.e("THINBTCLIENT", "ON RESUME: Exception during write.", e);
    } 
  }
  
  public byte printerstatus_byte() {
    if (this.port_connected == 0)
      return -1; 
    byte[] message = { 27, 33, 63 };
    int length = 0;
    String query = "";
    try {
      this.OutStream.write(message);
    } catch (IOException e) {
      Log.e("THINBTCLIENT", "ON RESUME: Exception during write.", e);
    } 
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 0)
          length = this.InStream.read(this.readBuf); 
      } catch (IOException iOException) {
        this.readBuf[0] = -1;
      } 
    }
    try {
      while (this.InStream.available() > 0)
        length = this.InStream.read(this.readBuf);
    } catch (IOException iOException) {
      this.readBuf[0] = -1;
    }
    return this.readBuf[0];
  }
  
  public byte printerstatus_byte(int delay) {
    if (this.port_connected == 0)
      return -1; 
    byte[] message = { 27, 33, 63 };
    int length = 0;
    String query = "";
    try {
      this.OutStream.write(message);
    } catch (IOException e) {
      Log.e("THINBTCLIENT", "ON RESUME: Exception during write.", e);
    } 
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 0)
          length = this.InStream.read(this.readBuf);
      } catch (IOException iOException) {
        this.readBuf[0] = -1;
      } 
    }
    try {
      while (this.InStream.available() > 0)
        length = this.InStream.read(this.readBuf);
    } catch (IOException iOException) {
      this.readBuf[0] = -1;
    }
    return this.readBuf[0];
  }
  
  public String printerstatus() {
    if (this.port_connected == 0)
      return "-1"; 
    byte[] message = { 27, 33, 63 };
    int length = 0;
    String query = "";
    try {
      this.OutStream.write(message);
    } catch (IOException e) {
      Log.e("THINBTCLIENT", "ON RESUME: Exception during write.", e);
    } 
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 0)
          length = this.InStream.read(this.readBuf); 
      } catch (IOException iOException) {
        return "-1";
      } 
    }
    try {
      while (this.InStream.available() > 0)
        length = this.InStream.read(this.readBuf);
    } catch (IOException iOException) {
      this.readBuf[0] = -1;
    }
    return "1";
  }
  
  public String printerstatus(int delay) {
    if (this.port_connected == 0)
      return "-1"; 
    byte[] message = { 27, 33, 63 };
    int length = 0;
    String query = "";
    try {
      this.OutStream.write(message);
    } catch (IOException e) {
      Log.e("THINBTCLIENT", "ON RESUME: Exception during write.", e);
    } 
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 0)
          length = this.InStream.read(this.readBuf); 
      } catch (IOException iOException) {
        return "-1";
      } 
    }
    try {
      while (this.InStream.available() > 0)
        length = this.InStream.read(this.readBuf);
    } catch (IOException iOException) {
      this.readBuf[0] = -1;
    }
    return "1";
  }
  
  public String queryprinter() {
    if (this.port_connected == 0)
      return "-1"; 
    byte[] message = { 27, 33, 63 };
    int length = 0;
    String query = "";
    try {
      this.OutStream.write(message);
    } catch (IOException e) {
      Log.e("THINBTCLIENT", "ON RESUME: Exception during write.", e);
    } 
    try {
      Thread.sleep(1000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 0) {
          this.readBuf = new byte[1024];
          length = this.InStream.read(this.readBuf);
        } 
      } catch (IOException iOException) {
        return "-1";
      } 
    }
    try {
      while (this.InStream.available() > 0)
        length = this.InStream.read(this.readBuf);
    } catch (IOException iOException) {
      this.readBuf[0] = -1;
    }
    return "1";
  }
  
  public String queryprinter(int delay) {
    if (this.port_connected == 0)
      return "-1"; 
    byte[] message = { 27, 33, 63 };
    int length = 0;
    String query = "";
    try {
      this.OutStream.write(message);
    } catch (IOException e) {
      Log.e("THINBTCLIENT", "ON RESUME: Exception during write.", e);
    } 
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
      try {
        while (this.InStream.available() > 0) {
          this.readBuf = new byte[1024];
          length = this.InStream.read(this.readBuf);
        } 
      } catch (IOException iOException) {
        return "-1";
      } 
    }
    try {
      while (this.InStream.available() > 0)
        length = this.InStream.read(this.readBuf);
    } catch (IOException iOException) {
      this.readBuf[0] = -1;
    }
    return "1";
  }
  
  public String closeport() {
    try {
      Thread.sleep(1500L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    if (this.port_connected == 0)
      return "-1"; 
    try {
      this.socket.close();
    } catch (IOException e) {
      return "-1";
    } 
    return "1";
  }
  
  public String closeport(int timeout) {
    try {
      Thread.sleep(timeout);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    if (this.port_connected == 0)
      return "-1"; 
    try {
      this.socket.close();
    } catch (IOException e) {
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
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
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      return "-1";
    } 
    return "1";
  }
  
  public String clearbuffer() {
    if (this.port_connected == 0)
      return "-1"; 
    String message = "CLS\r\n";
    byte[] msgBuffer = message.getBytes();
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      return "-1";
    } 
    return "1";
  }
  
  public String barcode(int x, int y, String type, int height, int human_readable, int rotation, int narrow, int wide, String string) {
    if (this.port_connected == 0)
      return "-1"; 
    String message = "";
    String barcode = "BARCODE ";
    String position = String.valueOf(x) + "," + y;
    String mode = "\"" + type + "\"";
    int i = height;
    int j = human_readable;
    int k = rotation;
    int m = narrow;
    int n = wide;
    String string_value = "\"" + string + "\"";
    message = String.valueOf(barcode) + position + " ," + mode + " ," + i + " ," + j + " ," + k + " ," + m + " ," + n + " ," + string_value + "\r\n";
    byte[] msgBuffer = message.getBytes();
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      return "-1";
    } 
    return "1";
  }
  
  public String qrcode(int x, int y, String ecc, String cell, String mode, String rotation, String model, String mask, String content) {
    String message = "QRCODE " + x + "," + y + "," + ecc + "," + cell + "," + mode + "," + rotation + "," + model + "," + mask + "," + "\"" + content + "\"" + "\r\n";
    byte[] msgBuffer = message.getBytes();
    if (this.port_connected == 0)
      return "-1"; 
    if (this.OutStream == null || this.InStream == null)
      return "-1"; 
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      return "-1";
    } 
    return "1";
  }
  
  public String bar(String x, String y, String width, String height) {
    String message = "BAR " + x + "," + y + "," + width + "," + height + "\r\n";
    byte[] msgBuffer = message.getBytes();
    if (this.port_connected == 0)
      return "-1"; 
    if (this.OutStream == null || this.InStream == null)
      return "-1"; 
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      return "-1";
    } 
    return "1";
  }
  
  public String printerfont(int x, int y, String size, int rotation, int x_multiplication, int y_multiplication, String string) {
    if (this.port_connected == 0)
      return "-1"; 
    String message = "";
    String text = "TEXT ";
    String position = String.valueOf(x) + "," + y;
    String size_value = "\"" + size + "\"";
    int i = rotation;
    int j = x_multiplication;
    int k = y_multiplication;
    String string_value = "\"" + string + "\"";
    message = String.valueOf(text) + position + " ," + size_value + " ," + i + " ," + j + " ," + k + " ," + string_value + "\r\n";
    byte[] msgBuffer = message.getBytes();
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      return "-1";
    } 
    return "1";
  }
  
  public String printlabel(int quantity, int copy) {
    if (this.port_connected == 0)
      return "-1"; 
    String message = "";
    message = "PRINT " + quantity + ", " + copy + "\r\n";
    byte[] msgBuffer = message.getBytes();
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      return "-1";
    } 
    return "1";
  }
  
  public String formfeed() {
    if (this.port_connected == 0)
      return "-1"; 
    String message = "";
    message = "FORMFEED\r\n";
    byte[] msgBuffer = message.getBytes();
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      return "-1";
    } 
    return "1";
  }
  
  public String nobackfeed() {
    if (this.port_connected == 0)
      return "-1"; 
    String message = "";
    message = "SET TEAR OFF\r\n";
    byte[] msgBuffer = message.getBytes();
    try {
      this.OutStream.write(msgBuffer);
    } catch (IOException e) {
      return "-1";
    } 
    return "1";
  }
  
  public String sendfile(String filename) {
    if (this.port_connected == 0)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(String.valueOf(Environment.getExternalStorageDirectory().getPath()) + "/Download/" + filename);
      byte[] data = new byte[fis.available()];
      do {
      
      } while (fis.read(data) != -1);
      this.OutStream.write(data);
      fis.close();
    } catch (Exception e) {
      return "-1";
    } 
    return "1";
  }
  
  public String sendfile(String path, String filename) {
    if (this.port_connected == 0)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(String.valueOf(Environment.getExternalStorageDirectory().getPath()) + "/" + path + "/" + filename);
      byte[] data = new byte[fis.available()];
      do {
      
      } while (fis.read(data) != -1);
      this.OutStream.write(data);
      fis.close();
    } catch (Exception e) {
      return "-1";
    } 
    return "1";
  }
  
  public String sendfile(Context ctx, Uri uri) {
    if (this.port_connected == 0)
      return "-1"; 
    try {
      InputStream fis = ctx.getContentResolver().openInputStream(uri);
      byte[] data = new byte[fis.available()];
      do {
      
      } while (fis.read(data) != -1);
      this.OutStream.write(data);
      fis.close();
    } catch (Exception e) {
      return "-1";
    } 
    return "1";
  }
  
  public byte[] return_file_byte(String path, String filename) {
    byte[] error_status = "ERROR".getBytes();
    byte[] data = new byte[1024];
    try {
      FileInputStream fis = new FileInputStream(String.valueOf(Environment.getExternalStorageDirectory().getPath()) + "/" + path + filename);
      data = new byte[fis.available()];
      fis.close();
    } catch (Exception e) {
      return error_status;
    } 
    return data;
  }
  
  public String send_file_data(String download_name, byte[] data) {
    try {
      String download = "DOWNLOAD F,\"" + download_name + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      this.OutStream.write(download_head);
      this.OutStream.write(data);
      this.OutStream.flush();
    } catch (Exception e) {
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadfile_absolutePath(String path, String Store_name) {
    if (this.OutStream == null || this.InStream == null)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(path);
      byte[] data = new byte[fis.available()];
      int[] FF = new int[data.length];
      String download = "DOWNLOAD F,\"" + Store_name + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      this.OutStream.write(download_head);
      this.OutStream.write(data);
      this.OutStream.flush();
      fis.close();
    } catch (Exception e) {
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadfile_absolutePath(File file, String Store_name) {
    if (this.OutStream == null || this.InStream == null)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(file);
      byte[] data = new byte[fis.available()];
      int[] FF = new int[data.length];
      String download = "DOWNLOAD F,\"" + Store_name + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      this.OutStream.write(download_head);
      this.OutStream.write(data);
      this.OutStream.flush();
      fis.close();
    } catch (Exception e) {
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadfile(String path, String filename) {
    if (this.port_connected == 0)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(String.valueOf(path) + filename);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + filename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      this.OutStream.write(download_head);
      this.OutStream.write(data);
      this.OutStream.flush();
      fis.close();
    } catch (Exception e) {
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadfile(File file, String filename) {
    if (this.port_connected == 0)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(file);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + filename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      this.OutStream.write(download_head);
      this.OutStream.write(data);
      this.OutStream.flush();
      fis.close();
    } catch (Exception e) {
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadfile(String path, String filename, String savename) {
    if (this.port_connected == 0)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(String.valueOf(path) + filename);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + savename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      this.OutStream.write(download_head);
      this.OutStream.write(data);
      this.OutStream.flush();
      fis.close();
    } catch (Exception e) {
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadpcx(String filename) {
    if (this.port_connected == 0)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(String.valueOf(Environment.getExternalStorageDirectory().getPath()) + "/Download/" + filename);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + filename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      this.OutStream.write(download_head);
      this.OutStream.write(data);
      this.OutStream.flush();
      fis.close();
    } catch (Exception e) {
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadbmp(String filename) {
    if (this.port_connected == 0)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(String.valueOf(Environment.getExternalStorageDirectory().getPath()) + "/Download/" + filename);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + filename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      this.OutStream.write(download_head);
      this.OutStream.write(data);
      this.OutStream.flush();
      fis.close();
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadttf(String filename) {
    if (this.port_connected == 0)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(String.valueOf(Environment.getExternalStorageDirectory().getPath()) + "/Download/" + filename);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + filename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      this.OutStream.write(download_head);
      this.OutStream.write(data);
      this.OutStream.flush();
      fis.close();
    } catch (Exception e) {
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadpcx(Context ctx, Uri uri, String filename) {
    if (this.port_connected == 0)
      return "-1"; 
    try {
      InputStream fis = ctx.getContentResolver().openInputStream(uri);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + filename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      this.OutStream.write(download_head);
      this.OutStream.write(data);
      this.OutStream.flush();
      fis.close();
    } catch (Exception e) {
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadbmp(Context ctx, Uri uri, String filename) {
    if (this.port_connected == 0)
      return "-1"; 
    try {
      InputStream fis = ctx.getContentResolver().openInputStream(uri);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + filename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      this.OutStream.write(download_head);
      this.OutStream.write(data);
      this.OutStream.flush();
      fis.close();
    } catch (Exception e) {
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadttf(Context ctx, Uri uri, String filename) {
    if (this.port_connected == 0)
      return "-1"; 
    try {
      InputStream fis = ctx.getContentResolver().openInputStream(uri);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + filename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      this.OutStream.write(download_head);
      this.OutStream.write(data);
      this.OutStream.flush();
      fis.close();
    } catch (Exception e) {
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public void Discovery_UDP(int delay) {
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
      SendUDP();
      Thread.sleep(delay);
      DatagramPacket packet = new DatagramPacket(this.udp_data, this.udp_data.length);
      packet.setAddress(InetAddress.getByName("255.255.255.255"));
      packet.setPort(22368);
      DatagramSocket ds = new DatagramSocket(22368, InetAddress.getByName("0.0.0.0"));
      ds.setBroadcast(true);
      ds.send(packet);
      ds.setSoTimeout(2000);
      while (true) {
        this.decipaddress = new int[200];
        ds.receive(packet);
        udpbyte = packet.getData();
        ds.close();
        String test = new String(udpbyte);
        String ip = printer_ipaddress();
        String name = printer_name();
        String mac = printer_macaddress();
        String status = printer_status();
        if (!ip.equals("0.0.0.0")) {
          for (int i = 0; i <= 150; i++) {
            int dec = udpbyte[i];
            if (dec < 0)
              this.decipaddress[i] = 256 + dec; 
          } 
          String udpend = "END";
          get_NAME[counter] = name;
          get_IP[counter] = ip;
          get_MAC[counter] = mac;
          get_status[counter] = status;
          counter++;
        } 
      } 
    } catch (Exception e) {
      if (counter <= 0)
        return; 
      for (int i = 0; i <= 100; i++) {
        if (get_NAME[i] == null && get_IP[i] == null && get_MAC[i] == null && get_status[i] == null) {
          this.UDP_NAME = new String[i];
          this.UDP_IP = new String[i];
          this.UDP_MAC = new String[i];
          this.UDP_status = new String[i];
          for (int j = 0; j <= i - 1; j++) {
            this.UDP_NAME[j] = get_NAME[j];
            this.UDP_IP[j] = get_IP[j];
            this.UDP_MAC[j] = get_MAC[j];
            this.UDP_status[j] = get_status[j];
          } 
          return;
        } 
      } 
      return;
    } 
  }
  
  public void Discovery_UDP_Thread() {
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
    (new Thread() {
        public void run() {
          try {
            TSCPrinterCore.this.SendUDP();
            DatagramPacket packet = new DatagramPacket(TSCPrinterCore.this.udp_data, TSCPrinterCore.this.udp_data.length);
            packet.setAddress(InetAddress.getByName("255.255.255.255"));
            packet.setPort(22368);
            DatagramSocket socket = new DatagramSocket(22368, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);
            socket.send(packet);
            socket.setSoTimeout(1000);
            while (true) {
              TSCPrinterCore.this.decipaddress = new int[200];
              socket.receive(packet);
              TSCPrinterCore.udpbyte = packet.getData();
              socket.close();
              String test = new String(TSCPrinterCore.udpbyte);
              String ip = TSCPrinterCore.this.printer_ipaddress();
              String name = TSCPrinterCore.this.printer_name();
              String mac = TSCPrinterCore.this.printer_macaddress();
              String status = TSCPrinterCore.this.printer_status();
              if (!ip.equals("0.0.0.0")) {
                for (int i = 0; i <= 150; i++) {
                  int dec = TSCPrinterCore.udpbyte[i];
                  if (dec < 0)
                    TSCPrinterCore.this.decipaddress[i] = 256 + dec;
                } 
                String udpend = "END";
                TSCPrinterCore.get_NAME[TSCPrinterCore.counter] = name;
                TSCPrinterCore.get_IP[TSCPrinterCore.counter] = ip;
                TSCPrinterCore.get_MAC[TSCPrinterCore.counter] = mac;
                TSCPrinterCore.get_status[TSCPrinterCore.counter] = status;
                TSCPrinterCore.counter = TSCPrinterCore.counter + 1;
              } 
            } 
          } catch (Exception e) {
            if (TSCPrinterCore.counter <= 0)
              return; 
            for (int i = 0; i <= 100; i++) {
              if (TSCPrinterCore.get_NAME[i] == null && TSCPrinterCore.get_IP[i] == null && TSCPrinterCore.get_MAC[i] == null && TSCPrinterCore.get_status[i] == null) {
                TSCPrinterCore.this.UDP_NAME = new String[i];
                TSCPrinterCore.this.UDP_IP = new String[i];
                TSCPrinterCore.this.UDP_MAC = new String[i];
                TSCPrinterCore.this.UDP_status = new String[i];
                for (int j = 0; j <= i - 1; j++) {
                  TSCPrinterCore.this.UDP_NAME[j] = TSCPrinterCore.get_NAME[j];
                  TSCPrinterCore.this.UDP_IP[j] = TSCPrinterCore.get_IP[j];
                  TSCPrinterCore.this.UDP_MAC[j] = TSCPrinterCore.get_MAC[j];
                  TSCPrinterCore.this.UDP_status[j] = TSCPrinterCore.get_status[j];
                } 
                return;
              } 
            } 
            return;
          } 
        }
      }).start();
  }
  
  private String printer_ipaddress() {
    String ip = "";
    String singlebyte = "";
    for (int i = 0; i <= 3; i++) {
      if (i == 1 || i == 2 || i == 3)
        ip = String.valueOf(ip) + "."; 
      if (udpbyte[44 + i] < 0) {
        int integer = 256 + udpbyte[44 + i];
        singlebyte = String.valueOf(integer);
      } else {
        int integer = udpbyte[44 + i];
        singlebyte = String.valueOf(integer);
      } 
      ip = String.valueOf(ip) + singlebyte;
      if (ip == "0.0.0.0")
        return ip; 
    } 
    return ip;
  }
  
  private String printer_name() {
    String name = "";
    String singlebyte = "";
    byte[] namebyte = new byte[16];
    for (int i = 0; i <= 15; i++) {
      if (udpbyte[52 + i] < 0) {
        namebyte[i] = udpbyte[52 + i];
      } else {
        namebyte[i] = udpbyte[52 + i];
      } 
      name = new String(namebyte);
    } 
    return name;
  }
  
  private String printer_macaddress() {
    String mac = "";
    String singlebyte = "";
    for (int i = 0; i <= 5; i++) {
      if (i == 1 || i == 2 || i == 3 || i == 4 || i == 5)
        mac = String.valueOf(mac) + ":"; 
      if (udpbyte[22 + i] < 0) {
        int integer = 256 + udpbyte[22 + i];
        singlebyte = Integer.toHexString(integer).toString().toUpperCase();
        if (singlebyte.length() < 2)
          singlebyte = "0" + singlebyte; 
      } else {
        int integer = udpbyte[22 + i];
        singlebyte = Integer.toHexString(integer).toString().toUpperCase();
        if (singlebyte.length() < 2)
          singlebyte = "0" + singlebyte; 
      } 
      mac = String.valueOf(mac) + singlebyte;
    } 
    return mac;
  }
  
  private String printer_status() {
    byte status = udpbyte[40];
    String printerstatus = String.valueOf(status);
    return printerstatus;
  }
  
  private String[] UDP_Name() {
    return this.UDP_NAME;
  }
  
  private String[] UDP_IP() {
    return this.UDP_IP;
  }
  
  private String[] UDP_Mac() {
    return this.UDP_MAC;
  }
  
  private String[] UDP_status() {
    return this.UDP_status;
  }
  
  private void SendUDP() {
    this.udp_data[0] = 0;
    this.udp_data[1] = 32;
    this.udp_data[2] = 0;
    this.udp_data[3] = 1;
    this.udp_data[4] = 0;
    this.udp_data[5] = 1;
    this.udp_data[6] = 8;
    this.udp_data[7] = 0;
    this.udp_data[8] = 0;
    this.udp_data[9] = 2;
    this.udp_data[10] = 0;
    this.udp_data[11] = 0;
    this.udp_data[12] = 0;
    this.udp_data[13] = 1;
    this.udp_data[14] = 0;
    this.udp_data[15] = 0;
    this.udp_data[16] = 1;
    this.udp_data[17] = 0;
    this.udp_data[18] = 0;
    this.udp_data[19] = 0;
    this.udp_data[20] = 0;
    this.udp_data[21] = 0;
    this.udp_data[22] = -1;
    this.udp_data[23] = -1;
    this.udp_data[24] = -1;
    this.udp_data[25] = -1;
    this.udp_data[26] = -1;
    this.udp_data[27] = -1;
    this.udp_data[28] = 0;
    this.udp_data[29] = 0;
    this.udp_data[30] = 0;
    this.udp_data[31] = 0;
  }
  
  private static void broadcast(int delay) {
    byte[] buf = new byte[256];
    byte[] TSCUDP = new byte[32];
    get_NAME = new String[100];
    get_IP = new String[100];
    get_MAC = new String[100];
    get_status = new String[100];
    TSCUDP[0] = 0;
    TSCUDP[1] = 32;
    TSCUDP[2] = 0;
    TSCUDP[3] = 1;
    TSCUDP[4] = 0;
    TSCUDP[5] = 1;
    TSCUDP[6] = 8;
    TSCUDP[7] = 0;
    TSCUDP[8] = 0;
    TSCUDP[9] = 2;
    TSCUDP[10] = 0;
    TSCUDP[11] = 0;
    TSCUDP[12] = 0;
    TSCUDP[13] = 2;
    TSCUDP[14] = 0;
    TSCUDP[15] = 0;
    TSCUDP[16] = 1;
    TSCUDP[17] = 0;
    TSCUDP[18] = 0;
    TSCUDP[19] = 0;
    TSCUDP[20] = 0;
    TSCUDP[21] = 0;
    TSCUDP[22] = -1;
    TSCUDP[23] = -1;
    TSCUDP[24] = -1;
    TSCUDP[25] = -1;
    TSCUDP[26] = -1;
    TSCUDP[27] = -1;
    TSCUDP[28] = 0;
    TSCUDP[29] = 0;
    TSCUDP[30] = 0;
    TSCUDP[31] = 0;
    DatagramSocket socket = null;
    InetAddress IPAddress = null;
    try {
      IPAddress = InetAddress.getByName("255.255.255.255");
    } catch (UnknownHostException e4) {
      e4.printStackTrace();
    } 
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    try {
      socket = new DatagramSocket(22368);
    } catch (SocketException e3) {
      e3.printStackTrace();
    } 
    try {
      socket.setBroadcast(true);
    } catch (SocketException e3) {
      e3.printStackTrace();
    } 
    DatagramPacket packet = new DatagramPacket(TSCUDP, TSCUDP.length, IPAddress, 22368);
    try {
      socket.send(packet);
    } catch (IOException iOException) {}
    while (true) {
      DatagramPacket receive_packet = new DatagramPacket(udpbyte, udpbyte.length);
      try {
        socket.setSoTimeout(1000);
      } catch (SocketException e) {
        e.printStackTrace();
      } 
      try {
        socket.receive(receive_packet);
        int broadcast_length = receive_packet.getLength();
        if (broadcast_length > 32) {
          String ip = broadcast_ipaddress();
          get_IP[counter] = ip;
          String recvString = new String(receive_packet.getData(), 0, receive_packet.getLength());
          counter++;
        } 
      } catch (IOException iOException) {
        break;
      } 
    } 
  }
  
  private static String broadcast_ipaddress() {
    String ip = "";
    String singlebyte = "";
    for (int i = 0; i <= 3; i++) {
      if (i == 1 || i == 2 || i == 3)
        ip = String.valueOf(ip) + "."; 
      if (udpbyte[44 + i] < 0) {
        int integer = 256 + udpbyte[44 + i];
        singlebyte = String.valueOf(integer);
      } else {
        int integer = udpbyte[44 + i];
        singlebyte = String.valueOf(integer);
      } 
      ip = String.valueOf(ip) + singlebyte;
      if (ip == "0.0.0.0")
        return ip; 
    } 
    return ip;
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
    sendcommand(command);
    sendcommand(stream);
    sendcommand("\r\n");
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
    sendcommand(command);
    sendcommand(stream);
    sendcommand("\r\n");
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
    sendcommand(out.toByteArray());
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
    sendcommand(command);
    sendcommand(stream);
    sendcommand("\r\n");
  }
  
  public void sendbitmap(int x_coordinates, int y_coordinates, Bitmap original_bitmap) {
    sendbitmap(x_coordinates, y_coordinates, original_bitmap, 128);
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
      try {
        File downloadsDir = new File("/storage/emulated/0/Download");
        File binaryFile = new File(downloadsDir, "debug_binary_" + System.currentTimeMillis() + ".png");
        FileOutputStream fos = new FileOutputStream(binaryFile);
        binary_bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        fos.close();
        Log.d(TAG, "sendbitmap: Saved binary bitmap to " + binaryFile.getAbsolutePath());
      } catch (Exception e) {
        Log.e(TAG, "sendbitmap: Failed to save binary bitmap", e);
      }
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
    Log.d(TAG, "sendbitmap: Sending BITMAP command: " + command);
    sendcommand(command);
    Log.d(TAG, "sendbitmap: Sending raw bitmap data (" + stream.length + " bytes)");
    sendcommand(stream);
    sendcommand("\r\n");
    Log.d(TAG, "sendbitmap: Bitmap sent using simple method");
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
    sendcommand(out.toByteArray());
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
  
  public void DownloadPNG(Bitmap original_bitmap, int resize_width, int resize_height, String PNGName, boolean ToDRAM) {
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
    byte[] buf = new byte[0];
    try {
      int threshold = 128;
      int width = resizedBitmap.getWidth();
      int height = resizedBitmap.getHeight();
      int error = 0;
      int[] errors_1d = new int[width * height];
      int[] pixels_orig = new int[width * height];
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      resizedBitmap.getPixels(pixels_orig, 0, width, 0, 0, width, height);
      PngWriter pngPngWriter = new PngWriter(baos, new ImageInfo(resize_width, resize_height, 1, false, true, false));
      ImageLineInt iline1 = new ImageLineInt(pngPngWriter.imgInfo);
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          int pixel = pixels_orig[y * width + x];
          int gray = (21 * Color.red(pixel) + 72 * Color.green(pixel) + 7 * Color.blue(pixel)) / 100;
          if (gray + errors_1d[x + y * width] < threshold) {
            error = gray + errors_1d[x + y * width];
            iline1.getScanline()[x] = 0;
          } else {
            error = gray + errors_1d[x + y * width] - 255;
            iline1.getScanline()[x] = 1;
          } 
          if (x < width - 1)
            errors_1d[x + 1 + y * width] = errors_1d[x + 1 + y * width] + 7 * error / 16; 
          if (x > 0 && y < height - 1)
            errors_1d[x - 1 + (y + 1) * width] = errors_1d[x - 1 + (y + 1) * width] + 3 * error / 16; 
          if (y < height - 1)
            errors_1d[x + (y + 1) * width] = errors_1d[x + (y + 1) * width] + 5 * error / 16; 
          if (x < width - 1 && y < height - 1)
            errors_1d[x + 1 + (y + 1) * width] = errors_1d[x + 1 + (y + 1) * width] + 1 * error / 16; 
        } 
        pngPngWriter.writeRow((IImageLine)iline1, y);
      } 
      errors_1d = null;
      pixels_orig = null;
      pngPngWriter.end();
      baos.flush();
      baos.close();
      buf = baos.toByteArray();
      String command = "DOWNLOAD " + (ToDRAM ? "" : "F,") + "\"" + PNGName + "\"," + buf.length + ",";
      sendcommand(command);
      sendcommand(buf);
      resizedBitmap.recycle();
    } catch (Exception e) {
      e.printStackTrace();
    } 
    buf = null;
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
    sendcommand(command);
    sendcommand(stream);
    sendcommand("\r\n");
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
        sendcommand(command);
        sendcommand(stream);
        sendcommand("\r\n");
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
        sendcommand(command);
        sendcommand(stream);
        sendcommand("\r\n");
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
        sendcommand(command);
        sendcommand(stream);
        sendcommand("\r\n");
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
        sendcommand(command);
        sendcommand(stream);
        sendcommand("\r\n");
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
        sendcommand("SIZE " + resize_width + " dot, " + resize_height + " dot\r\nCLS\r\n");
        sendcommand(command);
        sendcommand(stream);
        sendcommand("\r\nPRINT 1\r\n");
      } 
      renderer.close();
      return bitmaps;
    } catch (Exception ex) {
      ex.printStackTrace();
      return bitmaps;
    } 
  }
  
  public void sendpicture_halftone(int x_coordinates, int y_coordinates, String path) {
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
    binary_bitmap = gray2halftone(gray_bitmap);
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
    sendcommand(command);
    sendcommand(stream);
    sendcommand("\r\n");
  }
  
  public void sendpicture_CPCL(int x_coordinates, int y_coordinates, String path) {
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
    String command = "EG " + picture_wdith + " " + picture_height + " " + x_axis + " " + y_axis + " ";
    byte[] stream = new byte[(binary_bitmap.getWidth() + 7) / 8 * binary_bitmap.getHeight()];
    int Width_bytes = (binary_bitmap.getWidth() + 7) / 8;
    int Width = binary_bitmap.getWidth();
    int Height = binary_bitmap.getHeight();
    for (int i = 0; i < Height * Width_bytes; i++)
      stream[i] = 0; 
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
    String hex_to_string = byteArrayToHex(stream);
    sendcommand(command);
    sendcommand(hex_to_string.toUpperCase());
    sendcommand("\r\n");
  }
  
  private void sendpicture_resize(int x_coordinates, int y_coordinates, String path, int resize_width, int resize_height) {
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
    Bitmap resizedBitmap = Bitmap.createScaledBitmap(original_bitmap, resize_width, resize_height, false);
    gray_bitmap = bitmap2Gray(resizedBitmap);
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
    sendcommand(command);
    sendcommand(stream);
    sendcommand("\r\n");
  }
  
  private void sendpicture_resize_halftone(int x_coordinates, int y_coordinates, String path, int resize_width, int resize_height) {
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
    Bitmap resizedBitmap = Bitmap.createScaledBitmap(original_bitmap, resize_width, resize_height, false);
    gray_bitmap = bitmap2Gray(resizedBitmap);
    binary_bitmap = gray2halftone(gray_bitmap);
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
    sendcommand(command);
    sendcommand(stream);
    sendcommand("\r\n");
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
  
  public String WiFi_Default() {
    if (this.OutStream == null || this.InStream == null)
      return "-1"; 
    byte[] message = { 27, 33, 82 };
    String default_command = "WLAN DEFAULT\r\n";
    sendcommand(default_command);
    sendcommand(message);
    return "1";
  }
  
  public String WiFi_SSID(String SSID) {
    if (this.OutStream == null || this.InStream == null)
      return "-1"; 
    String command = "WLAN SSID \"" + SSID + "\"\r\n";
    sendcommand(command);
    return "1";
  }
  
  public String WiFi_WPA(String WPA) {
    if (this.OutStream == null || this.InStream == null)
      return "-1"; 
    String command = "WLAN WPA \"" + WPA + "\"\r\n";
    sendcommand(command);
    return "1";
  }
  
  public String WiFi_WEP(int number, String WEP) {
    if (this.OutStream == null || this.InStream == null)
      return "-1"; 
    String command = "WLAN WEP " + Integer.toString(number) + "," + "\"" + WEP + "\"\r\n";
    sendcommand(command);
    return "1";
  }
  
  public String WiFi_DHCP() {
    if (this.OutStream == null || this.InStream == null)
      return "-1"; 
    String command = "WLAN DHCP\r\n";
    sendcommand(command);
    return "1";
  }
  
  public String WiFi_Port(int port) {
    if (this.OutStream == null || this.InStream == null)
      return "-1"; 
    String command = "WLAN PORT " + Integer.toString(port) + "\r\n";
    sendcommand(command);
    return "1";
  }
  
  public String WiFi_StaticIP(String ip, String mask, String gateway) {
    if (this.OutStream == null || this.InStream == null)
      return "-1"; 
    String command = "WLAN IP \"" + ip + "\"" + "," + "\"" + mask + "\"" + "," + "\"" + gateway + "\"\r\n";
    sendcommand(command);
    return "1";
  }

  public String NFC_Read_data(int delay) {
    this.readBuf = new byte[1024];
    sendcommand("NFC MODE OFF\r\n");
    sendcommand("NFC READ\r\n");
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    try {
      if (this.InStream.available() > 0) {
        int i = this.InStream.read(this.readBuf);
      }
    } catch (IOException e) {
      return "-1";
    }
    String nfcdata = new String(this.readBuf);
    return nfcdata;
  }
  
  public int NFC_Write_data(String data) {
    sendcommand("NFC MODE OFF\r\n");
    sendcommand("NFC WRITE \"" + data + "\"" + "\r\n");
    return 1;
  }
  
  public int NFC_Timeout(int delay) {
    sendcommand("NFC TIMEOUT " + delay + "\r\n");
    return 1;
  }
  
  public String smartbattery_status(int index) {
    sendcommand_getstring("DIAGNOSTIC INTERFACE NET\r\n");
    String smb_info = "-1";
    switch (index) {
      case 0:
        smb_info = sendcommand_getstring("DIAGNOSTIC REPORT SMBSERIAL\r\n");
        break;
      case 1:
        smb_info = sendcommand_getstring("DIAGNOSTIC REPORT SMBVOLTAGE\r\n");
        break;
      case 2:
        smb_info = sendcommand_getstring("DIAGNOSTIC REPORT SMBREMCAPCITY\r\n");
        break;
      case 3:
        smb_info = sendcommand_getstring("DIAGNOSTIC REPORT SMBTEMPERATURE\r\n");
        break;
      case 4:
        smb_info = sendcommand_getstring("DIAGNOSTIC REPORT SMBDISCYCLE\r\n");
        break;
      case 5:
        smb_info = sendcommand_getstring("DIAGNOSTIC REPORT SMBMANUDATE\r\n");
        break;
      case 6:
        smb_info = sendcommand_getstring("DIAGNOSTIC REPORT SMBREPLACECOUNT\r\n");
        break;
      case 7:
        smb_info = sendcommand_getstring("DIAGNOSTIC REPORT SMBLIFE\r\n");
        break;
      case 8:
        smb_info = sendcommand_getstring("DIAGNOSTIC REPORT SMBSOH\r\n");
        break;
    } 
    if (smb_info.equals("") || !smb_info.contains("{"))
      return "-1"; 
    return smb_info.substring(smb_info.indexOf('|') + 1, smb_info.lastIndexOf('}'));
  }
  
  public String printPDFbyPath(String filename, int x_coordinates, int y_coordinates, int printer_dpi) {
    try {
      File file = new File(String.valueOf(Environment.getExternalStorageDirectory().getPath()) + "/Download/" + filename);
      if (filename.toLowerCase().endsWith(".pdf")) {
        PdfRenderer mPdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(file, 268435456));
        int PageCount = mPdfRenderer.getPageCount();
        for (int idx = 0; idx < PageCount; idx++) {
          PdfRenderer.Page page = mPdfRenderer.openPage(idx);
          int width = page.getWidth() * printer_dpi / 72;
          int height = page.getHeight() * printer_dpi / 72;
          Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
          Canvas canvas = new Canvas(bitmap);
          canvas.drawColor(-1);
          canvas.drawBitmap(bitmap, 0.0F, 0.0F, null);
          page.render(bitmap, new Rect(0, 0, width, height), null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT); // WAS DISPLAY
          page.close();
          sendcommand("CLS\r\n");
          sendbitmap(x_coordinates, y_coordinates, bitmap);
          sendcommand("PRINT 1\r\n");
          bitmap.recycle();
        } 
        mPdfRenderer.close();
      } else {
        return "-1";
      } 
    } catch (Exception ex) {
      ex.printStackTrace();
      return "-1";
    } 
    return "1";
  }
  
  public String printPDFbyPath(String filename, int x_coordinates, int y_coordinates, int printer_dpi, int page_index) {
    try {
      File file = new File(String.valueOf(Environment.getExternalStorageDirectory().getPath()) + "/Download/" + filename);
      if (filename.toLowerCase().endsWith(".pdf")) {
        PdfRenderer mPdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(file, 268435456));
        int PageCount = mPdfRenderer.getPageCount();
        int idx = page_index - 1;
        if (idx >= 0 && idx < PageCount) {
          PdfRenderer.Page page = mPdfRenderer.openPage(idx);
          int width = page.getWidth() * printer_dpi / 72;
          int height = page.getHeight() * printer_dpi / 72;
          Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
          Canvas canvas = new Canvas(bitmap);
          canvas.drawColor(-1);
          canvas.drawBitmap(bitmap, 0.0F, 0.0F, null);
          page.render(bitmap, new Rect(0, 0, width, height), null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
          page.close();
          sendcommand("CLS\r\n");
          sendbitmap(x_coordinates, y_coordinates, bitmap);
          sendcommand("PRINT 1\r\n");
          bitmap.recycle();
        } 
        mPdfRenderer.close();
      } else {
        return "-1";
      } 
    } catch (Exception ex) {
      ex.printStackTrace();
      return "-1";
    } 
    return "1";
  }
  
  public int getPDFPageCountbyPath(String filename) {
    try {
      File file = new File(String.valueOf(Environment.getExternalStorageDirectory().getPath()) + "/Download/" + filename);
      if (filename.toLowerCase().endsWith(".pdf")) {
        PdfRenderer mPdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(file, 268435456));
        int PageCount = mPdfRenderer.getPageCount();
        mPdfRenderer.close();
        return PageCount;
      } 
      return -1;
    } catch (Exception ex) {
      ex.printStackTrace();
      return -1;
    } 
  }
  
  public String printPDFbyPath(Context ctx, Uri uri, int x_coordinates, int y_coordinates, int printer_dpi) {
    try {
      PdfRenderer mPdfRenderer = new PdfRenderer(ctx.getContentResolver().openFileDescriptor(uri, "r"));
      int PageCount = mPdfRenderer.getPageCount();
      for (int idx = 0; idx < PageCount; idx++) {
        PdfRenderer.Page page = mPdfRenderer.openPage(idx);
        int width = page.getWidth() * printer_dpi / 72;
        int height = page.getHeight() * printer_dpi / 72;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(-1);
        canvas.drawBitmap(bitmap, 0.0F, 0.0F, null);
        page.render(bitmap, new Rect(0, 0, width, height), null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        page.close();
        sendcommand("CLS\r\n");
        sendbitmap(x_coordinates, y_coordinates, bitmap);
        sendcommand("PRINT 1\r\n");
        bitmap.recycle();
      } 
      mPdfRenderer.close();
    } catch (Exception ex) {
      ex.printStackTrace();
      return "-1";
    } 
    return "1";
  }
  
  public String printPDFbyPath(Context ctx, Uri uri, int x_coordinates, int y_coordinates, int printer_dpi, int page_index) {
    try {
      PdfRenderer mPdfRenderer = new PdfRenderer(ctx.getContentResolver().openFileDescriptor(uri, "r"));
      int PageCount = mPdfRenderer.getPageCount();
      int idx = page_index - 1;
      if (idx >= 0 && idx < PageCount) {
        PdfRenderer.Page page = mPdfRenderer.openPage(idx);
        int width = page.getWidth() * printer_dpi / 72;
        int height = page.getHeight() * printer_dpi / 72;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(-1);
        canvas.drawBitmap(bitmap, 0.0F, 0.0F, null);
        page.render(bitmap, new Rect(0, 0, width, height), null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        page.close();
        sendcommand("CLS\r\n");
        sendbitmap(x_coordinates, y_coordinates, bitmap);
        sendcommand("PRINT 1\r\n");
        bitmap.recycle();
      } 
      mPdfRenderer.close();
    } catch (Exception ex) {
      ex.printStackTrace();
      return "-1";
    } 
    return "1";
  }
  
  public int getPDFPageCountbyPath(Context ctx, Uri uri) {
    try {
      PdfRenderer mPdfRenderer = new PdfRenderer(ctx.getContentResolver().openFileDescriptor(uri, "r"));
      int PageCount = mPdfRenderer.getPageCount();
      mPdfRenderer.close();
      return PageCount;
    } catch (Exception ex) {
      ex.printStackTrace();
      return -1;
    } 
  }
  
  // public String printPDFbyFile(File file, int x_coordinates, int y_coordinates, int printer_dpi) {
  //   return printPDFbyFile(file, x_coordinates, y_coordinates, printer_dpi, null);
  // }
  
  public String printPDFbyFileSync(File file, int x_coordinates, int y_coordinates, int printer_dpi) {
    Log.d(TAG, "=== SYNC VERSION CALLED ===");
    Log.d(TAG, "printPDFbyFile: Starting PDF print process");
    Log.d(TAG, "printPDFbyFile: File=" + file.getAbsolutePath() + ", exists=" + file.exists() + ", size=" + file.length());
    Log.d(TAG, "printPDFbyFile: Coordinates=(" + x_coordinates + "," + y_coordinates + "), DPI=" + printer_dpi);
    Log.d(TAG, "printPDFbyFile: Context=" + (this.context != null ? this.context.getClass().getSimpleName() : "null"));
    Log.d(TAG, "printPDFbyFile: Current thread=" + Thread.currentThread().getName());
    
    try {
      Log.d(TAG, "printPDFbyFile: Opening PdfRenderer");
      
      PdfRenderer mPdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(file, 268435456));
      int PageCount = mPdfRenderer.getPageCount();
      Log.d(TAG, "printPDFbyFile: PDF opened successfully, pageCount=" + PageCount);
      
      for (int idx = 0; idx < PageCount; idx++) {
        Log.d(TAG, "printPDFbyFile: Processing page " + (idx + 1) + "/" + PageCount);
        
        PdfRenderer.Page page = mPdfRenderer.openPage(idx);
        int width = page.getWidth() * printer_dpi / 72;
        int height = page.getHeight() * printer_dpi / 72;
        Log.d(TAG, "printPDFbyFile: Page " + idx + " original size=" + page.getWidth() + "x" + page.getHeight());
        Log.d(TAG, "printPDFbyFile: Page " + idx + " scaled size=" + width + "x" + height);
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Log.d(TAG, "printPDFbyFile: Created bitmap " + bitmap.getWidth() + "x" + bitmap.getHeight() + ", config=" + bitmap.getConfig());
        
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(-1);
        Log.d(TAG, "printPDFbyFile: Canvas created and filled with white");
        
        Log.d(TAG, "printPDFbyFile: Starting PDF page render");
        page.render(bitmap, new Rect(0, 0, width, height), null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT );
        Log.d(TAG, "printPDFbyFile: PDF page rendered to bitmap");
        
        page.close();
        
        Log.d(TAG, "printPDFbyFile: Sending to printer - CLS command");
        sendcommand("CLS\r\n");
        
        Log.d(TAG, "printPDFbyFile: Sending bitmap to printer");
        Log.d(TAG, "printPDFbyFile: Bitmap details - width=" + bitmap.getWidth() + ", height=" + bitmap.getHeight() + ", coords=(" + x_coordinates + "," + y_coordinates + ")");
        Log.d(TAG, "printPDFbyFile: Bitmap hasAlpha=" + bitmap.hasAlpha() + ", isRecycled=" + bitmap.isRecycled());
        
        // Use the actual PDF-rendered bitmap
        try {
          // Save actual PDF bitmap to Downloads for debugging
          try {
            File downloadsDir = new File("/storage/emulated/0/Download");
            File debugFile = new File(downloadsDir, "debug_pdf_bitmap_" + System.currentTimeMillis() + ".png");
            FileOutputStream fos = new FileOutputStream(debugFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Log.d(TAG, "printPDFbyFile: Saved actual PDF bitmap to " + debugFile.getAbsolutePath());
          } catch (Exception e) {
            Log.e(TAG, "printPDFbyFile: Failed to save PDF bitmap", e);
          }
          
          // Now actually send the good bitmap to printer
          Log.d(TAG, "printPDFbyFile: Sending actual PDF bitmap to printer");
          sendbitmap(x_coordinates, y_coordinates, bitmap);
          Log.d(TAG, "printPDFbyFile: PDF bitmap sent to printer");
        } catch (Exception e) {
          Log.e(TAG, "printPDFbyFile: Exception processing PDF bitmap", e);
        }
        
        Log.d(TAG, "printPDFbyFile: Sending PRINT command");
        sendcommand("PRINT 1\r\n");

        bitmap.recycle();
        Log.d(TAG, "printPDFbyFile: Bitmap recycled, page " + idx + " complete");
      } 
      mPdfRenderer.close();
      Log.d(TAG, "printPDFbyFile: PDF renderer closed, all pages processed successfully");
      return "1";
    } catch (Exception ex) {
      Log.e(TAG, "printPDFbyFile: Exception during PDF processing", ex);
      ex.printStackTrace();
      return "-1";
    }
  }
  
  public String printPDFbyFileWithContext(File file, int x_coordinates, int y_coordinates, int printer_dpi) {
    Log.d(TAG, "=== CONTEXT VERSION CALLED ===");
    Log.d(TAG, "printPDFbyFile: Starting PDF print process");
    Log.d(TAG, "printPDFbyFile: File=" + file.getAbsolutePath() + ", exists=" + file.exists() + ", size=" + file.length());
    Log.d(TAG, "printPDFbyFile: Coordinates=(" + x_coordinates + "," + y_coordinates + "), DPI=" + printer_dpi);
    Log.d(TAG, "printPDFbyFile: Context=" + (this.context != null ? this.context.getClass().getSimpleName() : "null"));
    Log.d(TAG, "printPDFbyFile: Current thread=" + Thread.currentThread().getName());
    
    AtomicReference<String> result = new AtomicReference<>("-1");
    CountDownLatch latch = new CountDownLatch(1);
    
    Handler mainHandler = new Handler(Looper.getMainLooper());
    Log.d(TAG, "printPDFbyFile: Posting to main thread");
    
    mainHandler.post(() -> {
      try {
        Log.d(TAG, "printPDFbyFile: Now running on thread=" + Thread.currentThread().getName());
        Log.d(TAG, "printPDFbyFile: Opening PdfRenderer");
        
        PdfRenderer mPdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(file, 268435456));
        int PageCount = mPdfRenderer.getPageCount();
        Log.d(TAG, "printPDFbyFile: PDF opened successfully, pageCount=" + PageCount);
        
        for (int idx = 0; idx < PageCount; idx++) {
          Log.d(TAG, "printPDFbyFile: Processing page " + (idx + 1) + "/" + PageCount);
          
          PdfRenderer.Page page = mPdfRenderer.openPage(idx);
          int width = page.getWidth() * printer_dpi / 72;
          int height = page.getHeight() * printer_dpi / 72;
          Log.d(TAG, "printPDFbyFile: Page " + idx + " original size=" + page.getWidth() + "x" + page.getHeight());
          Log.d(TAG, "printPDFbyFile: Page " + idx + " scaled size=" + width + "x" + height);
          
          Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
          Log.d(TAG, "printPDFbyFile: Created bitmap " + bitmap.getWidth() + "x" + bitmap.getHeight() + ", config=" + bitmap.getConfig());
          
          Canvas canvas = new Canvas(bitmap);
          canvas.drawColor(-1);
          Log.d(TAG, "printPDFbyFile: Canvas created and filled with white");
          
          Log.d(TAG, "printPDFbyFile: Starting PDF page render");
          page.render(bitmap, new Rect(0, 0, width, height), null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
          Log.d(TAG, "printPDFbyFile: PDF page rendered to bitmap");
          
          page.close();
          
          Log.d(TAG, "printPDFbyFile: Sending to printer - CLS command");
          sendcommand("CLS\r\n");
          
          Log.d(TAG, "printPDFbyFile: Sending bitmap to printer");
          sendbitmap(x_coordinates, y_coordinates, bitmap);
          
          Log.d(TAG, "printPDFbyFile: Sending PRINT command");
          sendcommand("PRINT 1\r\n");
          
          bitmap.recycle();
          Log.d(TAG, "printPDFbyFile: Bitmap recycled, page " + idx + " complete");
        } 
        mPdfRenderer.close();
        Log.d(TAG, "printPDFbyFile: PDF renderer closed, all pages processed successfully");
        result.set("1");
      } catch (Exception ex) {
        Log.e(TAG, "printPDFbyFile: Exception during PDF processing", ex);
        ex.printStackTrace();
        result.set("-1");
      } finally {
        latch.countDown();
        Log.d(TAG, "printPDFbyFile: Main thread processing complete, releasing latch");
      }
    });
    
    try {
      Log.d(TAG, "printPDFbyFile: Waiting for main thread completion");
      latch.await(); // Wait for main thread execution to complete
      Log.d(TAG, "printPDFbyFile: Main thread completed, result=" + result.get());
    } catch (InterruptedException e) {
      Log.e(TAG, "printPDFbyFile: Thread interrupted while waiting", e);
      Thread.currentThread().interrupt();
      return "-1";
    }
    
    return result.get();
  }
  
  public String printPDFbyFile(File file, int x_coordinates, int y_coordinates, int printer_dpi, int page_index) {
    try {
      PdfRenderer mPdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(file, 268435456));
      int PageCount = mPdfRenderer.getPageCount();
      int idx = page_index - 1;
      if (idx >= 0 && idx < PageCount) {
        PdfRenderer.Page page = mPdfRenderer.openPage(idx);
        int width = page.getWidth() * printer_dpi / 72;
        int height = page.getHeight() * printer_dpi / 72;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(-1);
        canvas.drawBitmap(bitmap, 0.0F, 0.0F, null);
        page.render(bitmap, new Rect(0, 0, width, height), null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        page.close();
        sendcommand("CLS\r\n");
        sendbitmap(x_coordinates, y_coordinates, bitmap);
        sendcommand("PRINT 1\r\n");
        bitmap.recycle();
      } 
      mPdfRenderer.close();
    } catch (Exception ex) {
      ex.printStackTrace();
      return "-1";
    } 
    return "1";
  }
  
  public int getPDFPageCountbyFile(File file) {
    try {
      PdfRenderer mPdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(file, 268435456));
      int PageCount = mPdfRenderer.getPageCount();
      mPdfRenderer.close();
      return PageCount;
    } catch (Exception ex) {
      ex.printStackTrace();
      return -1;
    } 
  }
  
  public boolean CheckIsOnline() {
    try {
      return this.socket.getInetAddress().isReachable(500);
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    } 
  }
}
