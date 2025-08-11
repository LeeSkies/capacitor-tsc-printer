public package com.example.tscdll;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class TSCUSBActivity extends Activity implements Runnable {
  private int TIMEOUT = 1000;
  
  private TextView QQ;
  
  private static final String TAG = "DemoKit";
  
  public UsbManager mUsbManager;
  
  public UsbDevice mUsbDevice;
  
  private UsbInterface mUsbIntf;
  
  private static UsbDeviceConnection mUsbConnection;
  
  private static UsbEndpoint mUsbendpoint;
  
  private static UsbEndpoint usbEndpointIn;
  
  private static UsbEndpoint usbEndpointOut;
  
  private static PendingIntent mPermissionIntent;
  
  private boolean mPermissionRequestPending;
  
  private static UsbAccessory mAccessory;
  
  private static ParcelFileDescriptor mFileDescriptor;
  
  private static FileInputStream mInputStream;
  
  private static FileOutputStream mOutputStream;
  
  private static final int MESSAGE_SWITCH = 1;
  
  private static final int MESSAGE_TEMPERATURE = 2;
  
  private static final int MESSAGE_LIGHT = 3;
  
  private static final int MESSAGE_JOY = 4;
  
  public static final byte LED_SERVO_COMMAND = 2;
  
  public static final byte RELAY_COMMAND = 3;
  
  private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
  
  private TextView label1;
  
  private EditText editext1;
  
  private UsbConstants test;
  
  private Thread readThread;
  
  private StringBuilder strIN = new StringBuilder();
  
  private int receivelength = 0;
  
  private int response = 0;
  
  private static String printerstatus = "";
  
  private static String receive_data = "";
  
  private byte[] readBuf = new byte[1024];
  
  private int MAX_USBFS_BUFFER_SIZE = 10000;
  
  private static boolean hasPermissionToCommunicate = false;
  
  private UsbManager manager;
  
  private UsbDevice device;
  
  private UsbDevice device2;
  
  IntentFilter filterAttached_and_Detached = new IntentFilter("android.hardware.usb.action.USB_DEVICE_ATTACHED");
  
  private final BroadcastReceiver mUsbReceiver_main = new BroadcastReceiver() {
      public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("com.android.example.USB_PERMISSION".equals(action))
          synchronized (this) {
            UsbDevice device = (UsbDevice)intent.getParcelableExtra("device");
            if (intent.getBooleanExtra("permission", false) && 
              device != null)
              TSCUSBActivity.hasPermissionToCommunicate = true; 
          }  
      }
    };
  
  
  public void threadopen() {
    Thread thread = new Thread(new Runnable() {
          public void run() {
            TSCUSBActivity.this.mUsbManager = (UsbManager)TSCUSBActivity.this.getSystemService("usb");
            TSCUSBActivity.this.mUsbDevice = (UsbDevice)TSCUSBActivity.this.getIntent().getParcelableExtra("device");
            TSCUSBActivity.this.mUsbIntf = TSCUSBActivity.this.mUsbDevice.getInterface(0);
            TSCUSBActivity.mUsbendpoint = TSCUSBActivity.this.mUsbIntf.getEndpoint(0);
            TSCUSBActivity.mUsbConnection = TSCUSBActivity.this.mUsbManager.openDevice(TSCUSBActivity.this.mUsbDevice);
            TSCUSBActivity.mUsbConnection.claimInterface(TSCUSBActivity.this.mUsbIntf, true);
            for (int i = 0; i < TSCUSBActivity.this.mUsbIntf.getEndpointCount(); i++) {
              UsbEndpoint end = TSCUSBActivity.this.mUsbIntf.getEndpoint(i);
              if (end.getDirection() == 128) {
                TSCUSBActivity.usbEndpointIn = end;
              } else {
                TSCUSBActivity.usbEndpointOut = end;
              } 
            } 
            String printercommand = "FEED 100\n";
            byte[] command = printercommand.getBytes();
            TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.mUsbendpoint, command, command.length, TSCUSBActivity.this.TIMEOUT);
          }
        });
    thread.start();
  }
  
  public void open() {
    this.mUsbManager = (UsbManager)getSystemService("usb");
    this.mUsbDevice = (UsbDevice)getIntent().getParcelableExtra("device");
    this.mUsbIntf = this.mUsbDevice.getInterface(0);
    mUsbendpoint = this.mUsbIntf.getEndpoint(0);
    mUsbConnection = this.mUsbManager.openDevice(this.mUsbDevice);
    mUsbConnection.claimInterface(this.mUsbIntf, true);
    for (int i = 0; i < this.mUsbIntf.getEndpointCount(); i++) {
      UsbEndpoint end = this.mUsbIntf.getEndpoint(i);
      if (end.getDirection() == 128) {
        usbEndpointIn = end;
      } else {
        usbEndpointOut = end;
      } 
    } 
    this.label1.setText("Success");
    this.readThread = new Thread(this);
  }
  
  public void mopen() {
    this.mUsbManager = (UsbManager)getSystemService("usb");
    this.mUsbIntf = this.device.getInterface(0);
    mUsbendpoint = this.mUsbIntf.getEndpoint(0);
    mUsbConnection = this.mUsbManager.openDevice(this.device);
    mUsbConnection.claimInterface(this.mUsbIntf, true);
    for (int i = 0; i < this.mUsbIntf.getEndpointCount(); i++) {
      UsbEndpoint end = this.mUsbIntf.getEndpoint(i);
      if (end.getDirection() == 128) {
        usbEndpointIn = end;
      } else {
        usbEndpointOut = end;
      } 
    } 
    this.label1.setText("Success");
  }
  
  private void sendbythread() {
    Thread thread = new Thread(new Runnable() {
          public void run() {
            Log.d("DemoKit", "send finish. ");
            String printercommand = "FEED 100\n";
            String printercommand2 = "FEED 100\n";
            byte[] command = printercommand.getBytes();
            byte[] command2 = printercommand2.getBytes();
            int totalBytesRead = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.mUsbendpoint, command, command.length, TSCUSBActivity.this.TIMEOUT);
            int receivelength = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.mUsbendpoint, command2, command2.length, TSCUSBActivity.this.TIMEOUT);
          }
        });
    thread.start();
    USBReceive();
    try {
      Thread.sleep(100L);
    } catch (InterruptedException interruptedException) {}
    do {
      try {
        Thread.sleep(500L);
      } catch (InterruptedException interruptedException) {}
    } while (this.response != 1);
    this.label1.setText(this.strIN);
    this.editext1.setText(Integer.toString(this.receivelength));
  }
  
  public void msendbythread() {
    Thread thread = new Thread(new Runnable() {
          public void run() {
            Log.d("DemoKit", "send finish. ");
            String printercommand = "FEED 100\n";
            String printercommand2 = "~!F\n";
            byte[] command = printercommand.getBytes();
            byte[] command2 = printercommand2.getBytes();
            int totalBytesRead = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.mUsbendpoint, command, command.length, TSCUSBActivity.this.TIMEOUT);
            int receivelength = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.mUsbendpoint, command2, command2.length, TSCUSBActivity.this.TIMEOUT);
          }
        });
    thread.start();
    USBReceive();
    try {
      Thread.sleep(100L);
    } catch (InterruptedException interruptedException) {}
    do {
      try {
        Thread.sleep(500L);
      } catch (InterruptedException interruptedException) {}
    } while (this.response != 1);
    this.label1.setText(this.strIN);
    this.editext1.setText(Integer.toString(this.receivelength));
  }
  
  public void checkInfo() {
    this.manager = (UsbManager)getSystemService("usb");
    mPermissionIntent = PendingIntent.getBroadcast((Context)this, 0, new Intent("com.android.example.USB_PERMISSION"), 0);
    IntentFilter filter = new IntentFilter("com.android.example.USB_PERMISSION");
    registerReceiver(this.mUsbReceiver, filter);
    HashMap<String, UsbDevice> deviceList = this.manager.getDeviceList();
    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
    String i = "";
    if (deviceIterator.hasNext()) {
      this.device = deviceIterator.next();
      this.manager.requestPermission(this.device, mPermissionIntent);
      i = String.valueOf(i) + "\nDeviceID: " + this.device.getDeviceId() + "\n" + 
        "DeviceName: " + this.device.getDeviceName() + "\n" + 
        "DeviceClass: " + this.device.getDeviceClass() + " - " + 
        "DeviceSubClass: " + this.device.getDeviceSubclass() + "\n" + 
        "VendorID: " + this.device.getVendorId() + "\n" + 
        "ProductID: " + this.device.getProductId() + "\n";
      return;
    } 
  }
  
  private String USBReceive() {
    Thread ReceiveThread = new Thread(new Runnable() {
          public void run() {
            boolean mRunning = true;
            if (mRunning) {
              try {
                Thread.sleep(100L);
              } catch (InterruptedException interruptedException) {}
              byte[] dest = new byte[128];
              int timeoutMillis = 100;
              int readAmt = 512;
              int length = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.usbEndpointIn, dest, dest.length, timeoutMillis);
              if (length > 0) {
                TSCUSBActivity.this.response = 0;
                TSCUSBActivity.this.receivelength = length;
                for (int i = 0; i < TSCUSBActivity.this.receivelength - 1; i++)
                  TSCUSBActivity.this.strIN.append((char)dest[i]); 
              } else {
                TSCUSBActivity.this.response = 1;
              } 
            } 
          }
        });
    ReceiveThread.start();
    try {
      Thread.sleep(50L);
    } catch (InterruptedException interruptedException) {}
    if (this.response != 1)
      return ""; 
    return this.strIN.toString();
  }
  
  private boolean ReadStream_judge() {
    Thread ReceiveThread = new Thread(new Runnable() {
          public void run() {
            boolean mRunning = true;
            while (mRunning) {
              try {
                Thread.sleep(100L);
              } catch (InterruptedException interruptedException) {}
              byte[] dest = new byte[128];
              int timeoutMillis = 100;
              int readAmt = 512;
              int length = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.usbEndpointIn, dest, dest.length, timeoutMillis);
              if (length > 0) {
                TSCUSBActivity.this.response = 0;
                TSCUSBActivity.this.receivelength = length;
                for (int i = 0; i < TSCUSBActivity.this.receivelength - 1; i++)
                  TSCUSBActivity.this.strIN.append((char)dest[i]); 
                continue;
              } 
              TSCUSBActivity.this.response = 1;
              break;
            } 
          }
        });
    ReceiveThread.start();
    do {
      try {
        Thread.sleep(100L);
      } catch (InterruptedException interruptedException) {}
    } while (this.response != 1);
    receive_data = this.strIN.toString();
    if (receive_data.contains("ENDLINE")) {
      receive_data = receive_data.replace("ENDLINE", "");
      return true;
    } 
    return true;
  }
  
  private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
      public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("com.android.example.USB_PERMISSION".equals(action))
          synchronized (this) {
            TSCUSBActivity.this.mUsbDevice = (UsbDevice)intent.getParcelableExtra("device");
            if (intent.getBooleanExtra("permission", false)) {
              if (TSCUSBActivity.this.mUsbDevice != null)
                TSCUSBActivity.this.mopen(); 
            } else {
              Log.d("ERROR", "permission denied for device " + TSCUSBActivity.this.device);
            } 
          }  
      }
    };
  
  private void returndata_test() {
    Thread sendthread = new Thread(new Runnable() {
          public void run() {
            Log.d("DemoKit", "send finish. ");
            String printercommand = "~!F\n";
            byte[] command = printercommand.getBytes();
            int totalBytesRead = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.mUsbendpoint, command, command.length, TSCUSBActivity.this.TIMEOUT);
          }
        });
    sendthread.start();
    try {
      Thread.sleep(300L);
    } catch (InterruptedException interruptedException) {}
    Thread recvhread = new Thread(new Runnable() {
          public void run() {
            boolean mRunning = true;
            while (mRunning) {
              try {
                Thread.sleep(100L);
              } catch (InterruptedException interruptedException) {}
              byte[] dest = new byte[64];
              int timeoutMillis = 100;
              int readAmt = 512;
              int length = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.usbEndpointIn, dest, dest.length, timeoutMillis);
              if (length > 0) {
                TSCUSBActivity.this.response = 0;
                TSCUSBActivity.this.receivelength = length;
                for (int i = 0; i < TSCUSBActivity.this.receivelength - 1; i++)
                  TSCUSBActivity.this.strIN.append((char)dest[i]); 
                continue;
              } 
              TSCUSBActivity.this.response = 1;
              break;
            } 
          }
        });
    recvhread.start();
    do {
      try {
        Thread.sleep(100L);
      } catch (InterruptedException interruptedException) {}
    } while (this.response != 1);
    this.label1.setText(this.strIN);
    this.editext1.setText(Integer.toString(this.receivelength));
  }
  
  public String openport(UsbManager manager, UsbDevice device) {
    this.mUsbIntf = device.getInterface(0);
    mUsbendpoint = this.mUsbIntf.getEndpoint(0);
    mUsbConnection = manager.openDevice(device);
    boolean port_status = mUsbConnection.claimInterface(this.mUsbIntf, true);
    for (int i = 0; i < this.mUsbIntf.getEndpointCount(); i++) {
      UsbEndpoint end = this.mUsbIntf.getEndpoint(i);
      if (end.getDirection() == 128) {
        usbEndpointIn = end;
      } else {
        usbEndpointOut = end;
      } 
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    } 
    if (port_status)
      return "1"; 
    return "-1";
  }
  
  public String openport(UsbManager manager, UsbDevice device, int delay) {
    this.mUsbIntf = device.getInterface(0);
    mUsbendpoint = this.mUsbIntf.getEndpoint(0);
    mUsbConnection = manager.openDevice(device);
    boolean port_status = mUsbConnection.claimInterface(this.mUsbIntf, true);
    for (int i = 0; i < this.mUsbIntf.getEndpointCount(); i++) {
      UsbEndpoint end = this.mUsbIntf.getEndpoint(i);
      if (end.getDirection() == 128) {
        usbEndpointIn = end;
      } else {
        usbEndpointOut = end;
      } 
    } 
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    } 
    if (port_status)
      return "1"; 
    return "-1";
  }
  
  public String sendcommand(final String printercommand) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException interruptedException) {}
    Thread thread = new Thread(new Runnable() {
          public void run() {
            byte[] command = printercommand.getBytes();
            TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.mUsbendpoint, command, command.length, TSCUSBActivity.this.TIMEOUT);
          }
        });
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    try {
      Thread.sleep(50L);
    } catch (InterruptedException interruptedException) {}
    return "1";
  }
  
  public String sendcommandUTF8(final String message) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException interruptedException) {}
    Thread thread = new Thread(new Runnable() {
          public void run() {
            byte[] msgBuffer = null;
            try {
              msgBuffer = message.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e1) {
              e1.printStackTrace();
            } 
            TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.mUsbendpoint, msgBuffer, msgBuffer.length, TSCUSBActivity.this.TIMEOUT);
          }
        });
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    try {
      Thread.sleep(50L);
    } catch (InterruptedException interruptedException) {}
    return "1";
  }
  
  public String sendcommandBig5(final String message) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException interruptedException) {}
    Thread thread = new Thread(new Runnable() {
          public void run() {
            byte[] msgBuffer = null;
            try {
              msgBuffer = message.getBytes("big5");
            } catch (UnsupportedEncodingException e1) {
              e1.printStackTrace();
            } 
            TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.mUsbendpoint, msgBuffer, msgBuffer.length, TSCUSBActivity.this.TIMEOUT);
          }
        });
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    try {
      Thread.sleep(50L);
    } catch (InterruptedException interruptedException) {}
    return "1";
  }
  
  public String sendcommandGB2312(final String message) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException interruptedException) {}
    Thread thread = new Thread(new Runnable() {
          public void run() {
            byte[] msgBuffer = null;
            try {
              msgBuffer = message.getBytes("GB2312");
            } catch (UnsupportedEncodingException e1) {
              e1.printStackTrace();
            } 
            TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.mUsbendpoint, msgBuffer, msgBuffer.length, TSCUSBActivity.this.TIMEOUT);
          }
        });
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    try {
      Thread.sleep(50L);
    } catch (InterruptedException interruptedException) {}
    return "1";
  }
  
  public String sendcommand(final byte[] command) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException interruptedException) {}
    Thread thread = new Thread(new Runnable() {
          public void run() {
            int Offset = 0;
            int ReturnValue = 0;
            while (ReturnValue >= 0) {
              if (Offset >= command.length)
                break; 
              int Size = (command.length - Offset > 16384) ? 16384 : (command.length - Offset);
              ReturnValue = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.mUsbendpoint, command, Offset, Size, TSCUSBActivity.this.TIMEOUT);
              Offset += ReturnValue;
            } 
          }
        });
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    try {
      Thread.sleep(50L);
    } catch (InterruptedException interruptedException) {}
    return "1";
  }
  
  public String sendcommand_largebyte(final byte[] command) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException interruptedException) {}
    Thread thread = new Thread(new Runnable() {
          public void run() {
            int counter = 0;
            int total = 0;
            int length = command.length;
            int remain_data = 0;
            byte[] crlf_byte = "\r\n".getBytes();
            for (int i = 0; i < length; i += counter) {
              remain_data = length - total;
              if (remain_data >= TSCUSBActivity.this.MAX_USBFS_BUFFER_SIZE || i == 0) {
                counter = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.mUsbendpoint, command, i, TSCUSBActivity.this.MAX_USBFS_BUFFER_SIZE, TSCUSBActivity.this.TIMEOUT);
                total += counter;
              } else {
                if (remain_data == 0)
                  break; 
                TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.mUsbendpoint, command, i, remain_data, TSCUSBActivity.this.TIMEOUT);
                TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.mUsbendpoint, crlf_byte, 0, 2, TSCUSBActivity.this.TIMEOUT);
              } 
            } 
          }
        });
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    } 
    try {
      Thread.sleep(300L);
    } catch (InterruptedException interruptedException) {}
    return "1";
  }
  
  public String setup(int width, int height, int speed, int density, int sensor, int sensor_distance, int sensor_offset) {
    if (mUsbConnection == null)
      return "-1"; 
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
    sendcommand(msgBuffer);
    return "1";
  }
  
  public String clearbuffer() {
    if (mUsbConnection == null)
      return "-1"; 
    String message = "CLS\r\n";
    byte[] msgBuffer = message.getBytes();
    sendcommand(msgBuffer);
    return "1";
  }
  
  public String barcode(int x, int y, String type, int height, int human_readable, int rotation, int narrow, int wide, String string) {
    if (mUsbConnection == null)
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
    sendcommand(msgBuffer);
    return "1";
  }
  
  public String qrcode(int x, int y, String ecc, String cell, String mode, String rotation, String model, String mask, String content) {
    String message = "QRCODE " + x + "," + y + "," + ecc + "," + cell + "," + mode + "," + rotation + "," + model + "," + mask + "," + "\"" + content + "\"" + "\r\n";
    if (mUsbConnection == null)
      return "-1"; 
    byte[] msgBuffer = message.getBytes();
    sendcommand(msgBuffer);
    return "1";
  }
  
  public String bar(String x, String y, String width, String height) {
    String message = "BAR " + x + "," + y + "," + width + "," + height + "\r\n";
    if (mUsbConnection == null)
      return "-1"; 
    byte[] msgBuffer = message.getBytes();
    sendcommand(msgBuffer);
    return "1";
  }
  
  public String printerfont(int x, int y, String size, int rotation, int x_multiplication, int y_multiplication, String string) {
    if (mUsbConnection == null)
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
    sendcommand(msgBuffer);
    return "1";
  }
  
  public String printlabel(int quantity, int copy) {
    if (mUsbConnection == null)
      return "-1"; 
    String message = "";
    message = "PRINT " + quantity + ", " + copy + "\r\n";
    byte[] msgBuffer = message.getBytes();
    sendcommand(msgBuffer);
    return "1";
  }
  
  public String formfeed() {
    if (mUsbConnection == null)
      return "-1"; 
    String message = "";
    message = "FORMFEED\r\n";
    byte[] msgBuffer = message.getBytes();
    sendcommand(msgBuffer);
    return "1";
  }
  
  public String nobackfeed() {
    if (mUsbConnection == null)
      return "-1"; 
    String message = "";
    message = "SET TEAR OFF\r\n";
    byte[] msgBuffer = message.getBytes();
    sendcommand(msgBuffer);
    return "1";
  }
  
  public String sendfile(String filename) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(String.valueOf(Environment.getExternalStorageDirectory().getPath()) + "/Download/" + filename);
      byte[] data = new byte[fis.available()];
      do {
      
      } while (fis.read(data) != -1);
      sendcommand(data);
      fis.close();
    } catch (Exception exception) {}
    return "1";
  }
  
  public String sendfile(String path, String filename) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(String.valueOf(Environment.getExternalStorageDirectory().getPath()) + "/" + path + "/" + filename);
      byte[] data = new byte[fis.available()];
      do {
      
      } while (fis.read(data) != -1);
      sendcommand(data);
      fis.close();
    } catch (Exception exception) {}
    return "1";
  }
  
  public String sendfile(Context ctx, Uri uri) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      InputStream fis = ctx.getContentResolver().openInputStream(uri);
      byte[] data = new byte[fis.available()];
      do {
      
      } while (fis.read(data) != -1);
      sendcommand(data);
      fis.close();
    } catch (Exception exception) {}
    return "1";
  }
  
  public byte[] return_file_byte(String path, String filename) {
    byte[] error_status = "ERROR".getBytes();
    byte[] data = new byte[1024];
    try {
      FileInputStream fis = new FileInputStream(String.valueOf(Environment.getExternalStorageDirectory().getPath()) + "/" + path + "/" + filename);
      data = new byte[fis.available()];
      fis.close();
    } catch (Exception e) {
      return error_status;
    } 
    return data;
  }
  
  public String downloadfile(String path, String filename) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(String.valueOf(path) + filename);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + filename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      sendcommand(download_head);
      sendcommand(data);
      fis.close();
    } catch (Exception exception) {}
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadfile(File file, String filename) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(file);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + filename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      sendcommand(download_head);
      sendcommand(data);
      fis.close();
    } catch (Exception exception) {}
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadfile(String path, String filename, String savename) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(String.valueOf(path) + filename);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + savename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      sendcommand(download_head);
      sendcommand(data);
      fis.close();
    } catch (Exception exception) {}
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadpcx(String filename) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(String.valueOf(Environment.getExternalStorageDirectory().getPath()) + "/Download/" + filename);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + filename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      sendcommand(download_head);
      sendcommand(data);
      fis.close();
    } catch (Exception exception) {}
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadbmp(String filename) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(String.valueOf(Environment.getExternalStorageDirectory().getPath()) + "/Download/" + filename);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + filename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      sendcommand(download_head);
      sendcommand(data);
      fis.close();
    } catch (Exception exception) {}
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadttf(String filename) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(String.valueOf(Environment.getExternalStorageDirectory().getPath()) + "/Download/" + filename);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + filename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      sendcommand(download_head);
      fis.close();
    } catch (Exception exception) {}
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadpcx(Context ctx, Uri uri, String filename) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      InputStream fis = ctx.getContentResolver().openInputStream(uri);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + filename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      sendcommand(download_head);
      sendcommand(data);
      fis.close();
    } catch (Exception exception) {}
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadbmp(Context ctx, Uri uri, String filename) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      InputStream fis = ctx.getContentResolver().openInputStream(uri);
      byte[] data = new byte[fis.available()];
      String download = "DOWNLOAD F,\"" + filename + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      sendcommand(download_head);
      sendcommand(data);
      fis.close();
    } catch (Exception exception) {}
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadttf(Context ctx, Uri uri, String filename) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      InputStream fis = ctx.getContentResolver().openInputStream(uri);
      int length = fis.available();
      byte[] data = new byte[length];
      String download = "DOWNLOAD F,\"" + filename + "\"," + length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      sendcommand(download_head);
      sendcommand_largebyte(data);
      fis.close();
    } catch (Exception exception) {}
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String closeport() {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      Thread.sleep(1300L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    try {
      mUsbConnection.close();
      mUsbConnection.releaseInterface(this.mUsbIntf);
      mUsbConnection = null;
      mUsbendpoint = null;
      this.mUsbManager = null;
      this.mUsbDevice = null;
      this.mUsbIntf = null;
      Log.d("DemoKit", "Device closed. ");
    } catch (Exception e) {
      Log.e("DemoKit", "Exception: " + e.getMessage());
    } 
    return "1";
  }
  
  public String closeport(int timeout) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      Thread.sleep(timeout);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    try {
      mUsbConnection.close();
      mUsbConnection.releaseInterface(this.mUsbIntf);
      mUsbConnection = null;
      mUsbendpoint = null;
      this.mUsbManager = null;
      this.mUsbDevice = null;
      this.mUsbIntf = null;
      Log.d("DemoKit", "Device closed. ");
    } catch (Exception e) {
      Log.e("DemoKit", "Exception: " + e.getMessage());
    } 
    return "1";
  }
  
  public String printername() {
    if (mUsbConnection == null)
      return "-1"; 
    String message = "~!T";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    sendcommand(msgBuffer);
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    String name = USBReceive();
    return name;
  }
  
  public String printername(int delay) {
    if (mUsbConnection == null)
      return "-1"; 
    String message = "~!T";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    sendcommand(msgBuffer);
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    String name = USBReceive();
    return name;
  }
  
  public String printermemory() {
    if (mUsbConnection == null)
      return "-1"; 
    String message = "~!A";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    sendcommand(msgBuffer);
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    String memory = USBReceive();
    return memory;
  }
  
  public String printermemory(int dlelay) {
    if (mUsbConnection == null)
      return "-1"; 
    String message = "~!A";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    sendcommand(msgBuffer);
    try {
      Thread.sleep(dlelay);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    String memory = USBReceive();
    return memory;
  }
  
  public String printermileage() {
    if (mUsbConnection == null)
      return "-1"; 
    String message = "~!@";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    sendcommand(msgBuffer);
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    String mileage = USBReceive();
    return mileage;
  }
  
  public String printermileage(int delay) {
    if (mUsbConnection == null)
      return "-1"; 
    String message = "~!@";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    sendcommand(msgBuffer);
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    String mileage = USBReceive();
    return mileage;
  }
  
  public String sendcommand_getstring(String message) {
    this.strIN = new StringBuilder();
    if (mUsbConnection == null)
      return "-1"; 
    String end_judge = "OUT \"ENDLINE\"\r\n";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    byte[] msgBuffer1 = "\r\n".getBytes();
    byte[] msgBuffer2 = end_judge.getBytes();
    sendcommand(msgBuffer);
    sendcommand(msgBuffer1);
    sendcommand(msgBuffer2);
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      Log.e("DemoKit", "Exception during write\r\n");
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
  
  public String printercodepage() {
    if (mUsbConnection == null)
      return "-1"; 
    String message = "~!I";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    sendcommand(msgBuffer);
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    String codepage = USBReceive();
    return codepage;
  }
  
  public String printercodepage(int delay) {
    if (mUsbConnection == null)
      return "-1"; 
    String message = "~!I";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    sendcommand(msgBuffer);
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    String codepage = USBReceive();
    return codepage;
  }
  
  public String printerfile() {
    if (mUsbConnection == null)
      return "-1"; 
    String message = "~!F";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    sendcommand(msgBuffer);
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    String files = USBReceive();
    return files;
  }
  
  public String printerfile(int delay) {
    if (mUsbConnection == null)
      return "-1"; 
    String message = "~!F";
    this.readBuf = new byte[1024];
    byte[] msgBuffer = message.getBytes();
    sendcommand(msgBuffer);
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    String files = USBReceive();
    return files;
  }
  
  public String restart() {
    if (mUsbConnection == null)
      return "-1"; 
    byte[] message = { 27, 33, 82 };
    sendcommand(message);
    return "1";
  }
  
  public String send_file_data(String download_name, byte[] data) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      String download = "DOWNLOAD F,\"" + download_name + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      sendcommand(download_head);
      sendcommand_largebyte(data);
    } catch (Exception e) {
      return "-1";
    } 
    try {
      Thread.sleep(300L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String sendcommand_without_delay(final byte[] command) {
    if (mUsbConnection == null)
      return "-1"; 
    Thread thread = new Thread(new Runnable() {
          int counter = 0;
          
          public void run() {
            this.counter = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.mUsbendpoint, command, TSCUSBActivity.this.MAX_USBFS_BUFFER_SIZE, TSCUSBActivity.this.TIMEOUT);
          }
        });
    thread.start();
    try {
      Thread.sleep(1L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    try {
      thread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String send_file_data_without_delay(String download_name, byte[] data) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      String download = "DOWNLOAD F,\"" + download_name + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      sendcommand(download_head);
    } catch (Exception e) {
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    try {
      int i = 0;
      int total = 0;
      int length = data.length;
      byte[] thread_data = new byte[this.MAX_USBFS_BUFFER_SIZE];
      for (i = 0; i < length; i += this.MAX_USBFS_BUFFER_SIZE) {
        if (length - total >= this.MAX_USBFS_BUFFER_SIZE) {
          System.arraycopy(data, total, thread_data, 0, this.MAX_USBFS_BUFFER_SIZE);
          while (sendcommand_without_delay(thread_data) != "1") {
            try {
              Thread.sleep(50L);
            } catch (InterruptedException e) {
              e.printStackTrace();
            } 
          } 
          total += this.MAX_USBFS_BUFFER_SIZE;
        } else {
          System.arraycopy(data, total, thread_data, 0, length - total);
          sendcommand_without_delay(thread_data);
        } 
      } 
    } catch (Exception e) {
      return "-1";
    } 
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    try {
      byte[] crlf_byte = "\r\n".getBytes();
      sendcommand(crlf_byte);
    } catch (Exception e) {
      return "-1";
    } 
    try {
      Thread.sleep(300L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return "1";
  }
  
  public String downloadfile_absolutePath(String path, String Store_name) {
    if (mUsbConnection == null)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(path);
      byte[] data = new byte[fis.available()];
      int[] FF = new int[data.length];
      String download = "DOWNLOAD F,\"" + Store_name + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      sendcommand(download_head);
      sendcommand_largebyte(data);
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
    if (mUsbConnection == null)
      return "-1"; 
    try {
      FileInputStream fis = new FileInputStream(file);
      byte[] data = new byte[fis.available()];
      int[] FF = new int[data.length];
      String download = "DOWNLOAD F,\"" + Store_name + "\"," + data.length + ",";
      byte[] download_head = download.getBytes();
      do {
      
      } while (fis.read(data) != -1);
      sendcommand(download_head);
      sendcommand_largebyte(data);
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
  
  public byte printerstatus_byte() {
    if (mUsbConnection == null)
      return -1; 
    byte[] message = { 27, 33, 63 };
    int length = 0;
    String query = "";
    sendcommand(message);
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    Thread ReceiveThread = new Thread(new Runnable() {
          public void run() {
            boolean mRunning = true;
            while (mRunning) {
              try {
                Thread.sleep(100L);
              } catch (InterruptedException interruptedException) {}
              byte[] dest = new byte[64];
              int timeoutMillis = 100;
              int readAmt = 512;
              int length = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.usbEndpointIn, TSCUSBActivity.this.readBuf, TSCUSBActivity.this.readBuf.length, timeoutMillis);
              if (length > 0) {
                TSCUSBActivity.this.response = 0;
                TSCUSBActivity.this.receivelength = length;
                for (int i = 0; i < TSCUSBActivity.this.receivelength - 1; i++)
                  TSCUSBActivity.this.strIN.append((char)dest[i]); 
                continue;
              } 
              TSCUSBActivity.this.response = 1;
              TSCUSBActivity.this.readBuf[0] = -1;
              break;
            } 
          }
        });
    ReceiveThread.start();
    try {
      Thread.sleep(100L);
    } catch (InterruptedException interruptedException) {}
    return this.readBuf[0];
  }
  
  public byte printerstatus_byte(int delay) {
    if (mUsbConnection == null)
      return -1; 
    byte[] message = { 27, 33, 63 };
    int length = 0;
    String query = "";
    sendcommand(message);
    Thread ReceiveThread = new Thread(new Runnable() {
          public void run() {
            boolean mRunning = true;
            if (mRunning) {
              try {
                Thread.sleep(100L);
              } catch (InterruptedException interruptedException) {}
              byte[] dest = new byte[64];
              int timeoutMillis = 1000;
              int readAmt = 512;
              int length = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.usbEndpointIn, TSCUSBActivity.this.readBuf, TSCUSBActivity.this.readBuf.length, timeoutMillis);
              if (length > 0) {
                TSCUSBActivity.this.response = 0;
              } else {
                TSCUSBActivity.this.response = 1;
                TSCUSBActivity.this.readBuf[0] = -1;
              } 
            } 
          }
        });
    ReceiveThread.start();
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    return this.readBuf[0];
  }
  
  public String printerstatus() {
    if (mUsbConnection == null)
      return "-1"; 
    byte[] message = { 27, 33, 63 };
    int length = 0;
    String query = "";
    sendcommand(message);
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    Thread ReceiveThread = new Thread(new Runnable() {
          public void run() {
            boolean mRunning = true;
            if (mRunning) {
              try {
                Thread.sleep(100L);
              } catch (InterruptedException interruptedException) {}
              byte[] dest = new byte[64];
              int timeoutMillis = 100;
              int readAmt = 512;
              int length = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.usbEndpointIn, TSCUSBActivity.this.readBuf, TSCUSBActivity.this.readBuf.length, timeoutMillis);
              if (length > 0) {
                TSCUSBActivity.this.response = 0;
              } else {
                TSCUSBActivity.this.response = 1;
              } 
            } 
          }
        });
    ReceiveThread.start();
    try {
      Thread.sleep(100L);
    } catch (InterruptedException interruptedException) {}
    if (this.response == 0)
      return ""; 
    if (this.readBuf[0] == 0) {
      query = "00";
    } else if (this.readBuf[0] == 1) {
      query = "01";
    } else if (this.readBuf[0] == 2) {
      query = "02";
    } else if (this.readBuf[0] == 3) {
      query = "03";
    } else if (this.readBuf[0] == 4) {
      query = "04";
    } else if (this.readBuf[0] == 5) {
      query = "05";
    } else if (this.readBuf[0] == 8) {
      query = "08";
    } else if (this.readBuf[0] == 9) {
      query = "09";
    } else if (this.readBuf[0] == 10) {
      query = "0A";
    } else if (this.readBuf[0] == 11) {
      query = "0B";
    } else if (this.readBuf[0] == 12) {
      query = "0C";
    } else if (this.readBuf[0] == 13) {
      query = "0D";
    } else if (this.readBuf[0] == 16) {
      query = "10";
    } else if (this.readBuf[0] == 32) {
      query = "20";
    } else if (this.readBuf[0] == 128) {
      query = "80";
    } 
    return query;
  }
  
  public String printerstatus(int delay) {
    if (mUsbConnection == null)
      return "-1"; 
    byte[] message = { 27, 33, 63 };
    int length = 0;
    String query = "";
    sendcommand(message);
    Thread ReceiveThread = new Thread(new Runnable() {
          public void run() {
            boolean mRunning = true;
            if (mRunning) {
              try {
                Thread.sleep(100L);
              } catch (InterruptedException interruptedException) {}
              byte[] dest = new byte[64];
              int timeoutMillis = 300;
              int readAmt = 512;
              int length = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.usbEndpointIn, TSCUSBActivity.this.readBuf, TSCUSBActivity.this.readBuf.length, timeoutMillis);
              if (length > 0) {
                TSCUSBActivity.this.response = 0;
              } else {
                TSCUSBActivity.this.response = 1;
              } 
            } 
          }
        });
    ReceiveThread.start();
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    if (this.readBuf[0] == 0) {
      query = "00";
    } else if (this.readBuf[0] == 1) {
      query = "01";
    } else if (this.readBuf[0] == 2) {
      query = "02";
    } else if (this.readBuf[0] == 3) {
      query = "03";
    } else if (this.readBuf[0] == 4) {
      query = "04";
    } else if (this.readBuf[0] == 5) {
      query = "05";
    } else if (this.readBuf[0] == 8) {
      query = "08";
    } else if (this.readBuf[0] == 9) {
      query = "09";
    } else if (this.readBuf[0] == 10) {
      query = "0A";
    } else if (this.readBuf[0] == 11) {
      query = "0B";
    } else if (this.readBuf[0] == 12) {
      query = "0C";
    } else if (this.readBuf[0] == 13) {
      query = "0D";
    } else if (this.readBuf[0] == 16) {
      query = "10";
    } else if (this.readBuf[0] == 32) {
      query = "20";
    } else if (this.readBuf[0] == 128) {
      query = "80";
    } 
    return query;
  }
  
  public String queryprinter() {
    if (mUsbConnection == null)
      return "-1"; 
    byte[] message = { 27, 33, 63 };
    int length = 0;
    String query = "";
    sendcommand(message);
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    Thread ReceiveThread = new Thread(new Runnable() {
          public void run() {
            boolean mRunning = true;
            if (mRunning) {
              try {
                Thread.sleep(500L);
              } catch (InterruptedException interruptedException) {}
              byte[] dest = new byte[64];
              int timeoutMillis = 100;
              int readAmt = 512;
              int length = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.usbEndpointIn, TSCUSBActivity.this.readBuf, TSCUSBActivity.this.readBuf.length, timeoutMillis);
              if (length > 0) {
                TSCUSBActivity.this.response = 0;
              } else {
                TSCUSBActivity.this.response = 1;
              } 
            } 
          }
        });
    ReceiveThread.start();
    if (this.readBuf[0] == 0) {
      query = "0";
    } else if (this.readBuf[0] == 1) {
      query = "1";
    } else if (this.readBuf[0] == 2) {
      query = "2";
    } else if (this.readBuf[0] == 3) {
      query = "3";
    } else if (this.readBuf[0] == 4) {
      query = "4";
    } else if (this.readBuf[0] == 5) {
      query = "5";
    } else if (this.readBuf[0] == 8) {
      query = "8";
    } else if (this.readBuf[0] == 9) {
      query = "9";
    } else if (this.readBuf[0] == 10) {
      query = "A";
    } else if (this.readBuf[0] == 11) {
      query = "B";
    } else if (this.readBuf[0] == 12) {
      query = "C";
    } else if (this.readBuf[0] == 13) {
      query = "D";
    } else if (this.readBuf[0] == 16) {
      query = "10";
    } else if (this.readBuf[0] == 32) {
      query = "20";
    } else if (this.readBuf[0] == 128) {
      query = "80";
    } 
    return query;
  }
  
  public String queryprinter(int delay) {
    if (mUsbConnection == null)
      return "-1"; 
    byte[] message = { 27, 33, 63 };
    int length = 0;
    String query = "";
    sendcommand(message);
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    Thread ReceiveThread = new Thread(new Runnable() {
          public void run() {
            boolean mRunning = true;
            if (mRunning) {
              try {
                Thread.sleep(100L);
              } catch (InterruptedException interruptedException) {}
              byte[] dest = new byte[64];
              int timeoutMillis = 100;
              int readAmt = 512;
              int length = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.usbEndpointIn, TSCUSBActivity.this.readBuf, TSCUSBActivity.this.readBuf.length, timeoutMillis);
              if (length > 0) {
                TSCUSBActivity.this.response = 0;
              } else {
                TSCUSBActivity.this.response = 1;
              } 
            } 
          }
        });
    ReceiveThread.start();
    if (this.readBuf[0] == 0) {
      query = "0";
    } else if (this.readBuf[0] == 1) {
      query = "1";
    } else if (this.readBuf[0] == 2) {
      query = "2";
    } else if (this.readBuf[0] == 3) {
      query = "3";
    } else if (this.readBuf[0] == 4) {
      query = "4";
    } else if (this.readBuf[0] == 5) {
      query = "5";
    } else if (this.readBuf[0] == 8) {
      query = "8";
    } else if (this.readBuf[0] == 9) {
      query = "9";
    } else if (this.readBuf[0] == 10) {
      query = "A";
    } else if (this.readBuf[0] == 11) {
      query = "B";
    } else if (this.readBuf[0] == 12) {
      query = "C";
    } else if (this.readBuf[0] == 13) {
      query = "D";
    } else if (this.readBuf[0] == 16) {
      query = "10";
    } else if (this.readBuf[0] == 32) {
      query = "20";
    } else if (this.readBuf[0] == 128) {
      query = "80";
    } 
    return query;
  }
  
  public String status() {
    if (mUsbConnection == null)
      return "-1"; 
    int length = 0;
    String query = "";
    byte[] message = { 27, 33, 83 };
    this.readBuf = new byte[1024];
    this.strIN = new StringBuilder();
    sendcommand(message);
    try {
      Thread.sleep(100L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    Thread ReceiveThread = new Thread(new Runnable() {
          public void run() {
            boolean mRunning = true;
            if (mRunning) {
              try {
                Thread.sleep(100L);
              } catch (InterruptedException interruptedException) {}
              byte[] dest = new byte[64];
              int timeoutMillis = 100;
              int readAmt = 512;
              int length = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.usbEndpointIn, TSCUSBActivity.this.readBuf, TSCUSBActivity.this.readBuf.length, timeoutMillis);
              if (length > 0) {
                TSCUSBActivity.this.response = 0;
                TSCUSBActivity.this.receivelength = length;
                for (int i = 0; i < TSCUSBActivity.this.receivelength - 1; i++)
                  TSCUSBActivity.this.strIN.append((char)TSCUSBActivity.this.readBuf[i]); 
              } else {
                TSCUSBActivity.this.response = 1;
              } 
            } 
          }
        });
    ReceiveThread.start();
    try {
      Thread.sleep(300L);
    } catch (InterruptedException interruptedException) {}
    if (this.readBuf[0] == 2 && this.readBuf[5] == 3) {
      for (int tim = 0; tim <= 7; tim++) {
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 64 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Ready";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 96 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Head Open";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 64 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 96 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Head Open";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 72 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Ribbon Jam";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 68 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Ribbon Empty";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 65 && this.readBuf[tim + 5] == 3) {
          printerstatus = "No Paper";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 66 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Paper Jam";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 65 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Paper Empty";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 67 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Cutting";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 75 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Waiting to Press Print Key";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 76 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Waiting to Take Label";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 80 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Printing Batch";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 96 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Pause";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Pause";
          this.readBuf = new byte[1024];
          break;
        } 
      } 
      return printerstatus;
    } 
    return "";
  }
  
  public String status(int delay) {
    if (mUsbConnection == null)
      return "-1"; 
    int length = 0;
    String query = "";
    byte[] message = { 27, 33, 83 };
    this.readBuf = new byte[1024];
    this.strIN = new StringBuilder();
    sendcommand(message);
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    Thread ReceiveThread = new Thread(new Runnable() {
          public void run() {
            boolean mRunning = true;
            if (mRunning) {
              try {
                Thread.sleep(100L);
              } catch (InterruptedException interruptedException) {}
              byte[] dest = new byte[64];
              int timeoutMillis = 100;
              int readAmt = 512;
              int length = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.usbEndpointIn, TSCUSBActivity.this.readBuf, TSCUSBActivity.this.readBuf.length, timeoutMillis);
              if (length > 0) {
                TSCUSBActivity.this.response = 0;
                TSCUSBActivity.this.receivelength = length;
                for (int i = 0; i < TSCUSBActivity.this.receivelength - 1; i++)
                  TSCUSBActivity.this.strIN.append((char)TSCUSBActivity.this.readBuf[i]); 
              } else {
                TSCUSBActivity.this.response = 1;
              } 
            } 
          }
        });
    ReceiveThread.start();
    try {
      Thread.sleep(300L);
    } catch (InterruptedException interruptedException) {}
    if (this.readBuf[0] == 2 && this.readBuf[5] == 3) {
      for (int tim = 0; tim <= 7; tim++) {
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 64 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Ready";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 96 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Head Open";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 64 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 96 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Head Open";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 72 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Ribbon Jam";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 68 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Ribbon Empty";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 65 && this.readBuf[tim + 5] == 3) {
          printerstatus = "No Paper";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 66 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Paper Jam";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 65 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Paper Empty";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 67 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Cutting";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 75 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Waiting to Press Print Key";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 76 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Waiting to Take Label";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 80 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Printing Batch";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 96 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Pause";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Pause";
          this.readBuf = new byte[1024];
          break;
        } 
      } 
      return printerstatus;
    } 
    return "";
  }
  
  public String status(int delay1, int delay2) {
    if (mUsbConnection == null)
      return "-1"; 
    int length = 0;
    String query = "";
    byte[] message = { 27, 33, 83 };
    this.readBuf = new byte[1024];
    this.strIN = new StringBuilder();
    String printername = "";
    printername = printername(delay1);
    int name_length = printername.toString().trim().length();
    if (name_length < 3)
      return "-1"; 
    sendcommand(message);
    try {
      Thread.sleep(delay2);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    Thread ReceiveThread = new Thread(new Runnable() {
          public void run() {
            boolean mRunning = true;
            if (mRunning) {
              try {
                Thread.sleep(100L);
              } catch (InterruptedException interruptedException) {}
              byte[] dest = new byte[64];
              int timeoutMillis = 100;
              int readAmt = 512;
              int length = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.usbEndpointIn, TSCUSBActivity.this.readBuf, TSCUSBActivity.this.readBuf.length, timeoutMillis);
              if (length > 0) {
                TSCUSBActivity.this.response = 0;
                TSCUSBActivity.this.receivelength = length;
                for (int i = 0; i < TSCUSBActivity.this.receivelength - 1; i++)
                  TSCUSBActivity.this.strIN.append((char)TSCUSBActivity.this.readBuf[i]); 
              } else {
                TSCUSBActivity.this.response = 1;
              } 
            } 
          }
        });
    ReceiveThread.start();
    try {
      Thread.sleep(100L);
    } catch (InterruptedException interruptedException) {}
    if (this.readBuf[0] == 2 && this.readBuf[5] == 3) {
      for (int tim = 0; tim <= 7; tim++) {
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 64 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Ready";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 96 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Head Open";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 64 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 96 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Head Open";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 72 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Ribbon Jam";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 68 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Ribbon Empty";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 65 && this.readBuf[tim + 5] == 3) {
          printerstatus = "No Paper";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 66 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Paper Jam";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 65 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Paper Empty";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 67 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Cutting";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 75 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Waiting to Press Print Key";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 76 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Waiting to Take Label";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 80 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Printing Batch";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 96 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Pause";
          this.readBuf = new byte[1024];
          break;
        } 
        if (this.readBuf[tim] == 2 && this.readBuf[tim + 1] == 69 && this.readBuf[tim + 2] == 64 && this.readBuf[tim + 3] == 64 && this.readBuf[tim + 4] == 64 && this.readBuf[tim + 5] == 3) {
          printerstatus = "Pause";
          this.readBuf = new byte[1024];
          break;
        } 
      } 
      return printerstatus;
    } 
    return "";
  }
  
  public String printer_completestatus() {
    if (mUsbConnection == null)
      return "-1"; 
    this.readBuf = new byte[1024];
    this.strIN = new StringBuilder();
    byte[] message = { 27, 33, 83 };
    int length = 0;
    String query = "";
    sendcommand(message);
    try {
      Thread.sleep(150L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    Thread ReceiveThread = new Thread(new Runnable() {
          public void run() {
            boolean mRunning = true;
            if (mRunning) {
              try {
                Thread.sleep(100L);
              } catch (InterruptedException interruptedException) {}
              byte[] dest = new byte[64];
              int timeoutMillis = 100;
              int readAmt = 512;
              int length = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.usbEndpointIn, TSCUSBActivity.this.readBuf, TSCUSBActivity.this.readBuf.length, timeoutMillis);
              if (length > 0) {
                TSCUSBActivity.this.response = 0;
                TSCUSBActivity.this.receivelength = length;
                for (int i = 0; i < TSCUSBActivity.this.receivelength - 1; i++)
                  TSCUSBActivity.this.strIN.append((char)TSCUSBActivity.this.readBuf[i]); 
              } else {
                TSCUSBActivity.this.response = 1;
              } 
            } 
          }
        });
    ReceiveThread.start();
    try {
      Thread.sleep(500L);
    } catch (InterruptedException interruptedException) {}
    if (this.strIN.toString().length() > 1)
      return this.strIN.toString(); 
    return "";
  }
  
  public String printer_completestatus(int delaytime, int delaytime2) {
    if (mUsbConnection == null)
      return "-1"; 
    this.readBuf = new byte[1024];
    this.strIN = new StringBuilder();
    byte[] message = { 27, 33, 83 };
    int length = 0;
    String query = "";
    sendcommand(message);
    try {
      Thread.sleep(delaytime);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    Thread ReceiveThread = new Thread(new Runnable() {
          public void run() {
            boolean mRunning = true;
            if (mRunning) {
              try {
                Thread.sleep(100L);
              } catch (InterruptedException interruptedException) {}
              byte[] dest = new byte[64];
              int timeoutMillis = 100;
              int readAmt = 512;
              int length = TSCUSBActivity.mUsbConnection.bulkTransfer(TSCUSBActivity.usbEndpointIn, TSCUSBActivity.this.readBuf, TSCUSBActivity.this.readBuf.length, timeoutMillis);
              if (length > 0) {
                TSCUSBActivity.this.response = 0;
                TSCUSBActivity.this.receivelength = length;
                for (int i = 0; i < TSCUSBActivity.this.receivelength - 1; i++)
                  TSCUSBActivity.this.strIN.append((char)TSCUSBActivity.this.readBuf[i]); 
              } else {
                TSCUSBActivity.this.response = 1;
              } 
            } 
          }
        });
    ReceiveThread.start();
    try {
      Thread.sleep(delaytime2);
    } catch (InterruptedException interruptedException) {}
    if (this.strIN.toString().length() > 1)
      return this.strIN.toString(); 
    return "";
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
        if (total == 0)
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
    return "1";
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
        if (total == 0)
          stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8)); 
      } 
    } 
    sendcommand(command);
    sendcommand(stream);
    sendcommand("\r\n");
    return "1";
  }
  
  public void sendpicture(int x_coordinates, int y_coordinates, String path) {
    Bitmap original_bitmap = null;
    Bitmap gray_bitmap = null;
    Bitmap binary_bitmap = null;
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPurgeable = true;
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
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
        if (total == 0)
          stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8)); 
      } 
    } 
    sendcommand(command);
    sendcommand(stream);
    sendcommand("\r\n");
  }
  
  public void sendpicture(int x_coordinates, int y_coordinates, Bitmap original_bitmap) {
    Bitmap gray_bitmap = null;
    Bitmap binary_bitmap = null;
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPurgeable = true;
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
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
        if (total == 0)
          stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8)); 
      } 
    } 
    sendcommand(command);
    sendcommand(stream);
    sendcommand("\r\n");
  }
  
  public void sendpicture_halftone(int x_coordinates, int y_coordinates, String path) {
    Bitmap original_bitmap = null;
    Bitmap gray_bitmap = null;
    Bitmap binary_bitmap = null;
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPurgeable = true;
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
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
        if (total == 0)
          stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8)); 
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
        if (total == 0)
          stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8)); 
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
        if (total == 0)
          stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8)); 
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
        if (total == 0)
          stream[y * (Width + 7) / 8 + x / 8] = (byte)(stream[y * (Width + 7) / 8 + x / 8] ^ (byte)(128 >> x % 8)); 
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
    if (mUsbConnection == null)
      return "-1"; 
    byte[] message = { 27, 33, 82 };
    String default_command = "WLAN DEFAULT\r\n";
    sendcommand(default_command);
    sendcommand(message);
    return "1";
  }
  
  public String WiFi_SSID(String SSID) {
    if (mUsbConnection == null)
      return "-1"; 
    String command = "WLAN SSID \"" + SSID + "\"\r\n";
    sendcommand(command);
    return "1";
  }
  
  public String WiFi_WPA(String WPA) {
    if (mUsbConnection == null)
      return "-1"; 
    String command = "WLAN WPA \"" + WPA + "\"\r\n";
    sendcommand(command);
    return "1";
  }
  
  public String WiFi_WEP(int number, String WEP) {
    if (mUsbConnection == null)
      return "-1"; 
    String command = "WLAN WEP " + Integer.toString(number) + "," + "\"" + WEP + "\"\r\n";
    sendcommand(command);
    return "1";
  }
  
  public String WiFi_DHCP() {
    if (mUsbConnection == null)
      return "-1"; 
    String command = "WLAN DHCP\r\n";
    sendcommand(command);
    return "1";
  }
  
  public String WiFi_Port(int port) {
    if (mUsbConnection == null)
      return "-1"; 
    String command = "WLAN PORT " + Integer.toString(port) + "\r\n";
    sendcommand(command);
    return "1";
  }
  
  public String WiFi_StaticIP(String ip, String mask, String gateway) {
    if (mUsbConnection == null)
      return "-1"; 
    String command = "WLAN IP \"" + ip + "\"" + "," + "\"" + mask + "\"" + "," + "\"" + gateway + "\"\r\n";
    sendcommand(command);
    return "1";
  }
  
  public String NFC_Read_data(int delay) {
    if (mUsbConnection == null)
      return "-1"; 
    sendcommand("NFC MODE OFF\r\n");
    sendcommand("NFC READ\r\n");
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e) {
      e.printStackTrace();
    } 
    String nfc_data = USBReceive();
    return nfc_data;
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
        page.render(bitmap, null, null, 1);
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
        page.render(resize_bitmap, null, null, 1);
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
        page.render(bitmap, null, null, 1);
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
        page.render(bitmap, null, null, 2);
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
        page.render(resize_bitmap, null, null, 1);
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
        page.render(bitmap, null, null, 1);
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
        return bitmaps;
      } 
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
    return bitmaps;
  }
  
  public String smartbattery_status(int index) {
    sendcommand_getstring("DIAGNOSTIC INTERFACE USB\r\n");
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
  
  private Bitmap rotateBitmap(Bitmap original, float degrees) {
    int width = original.getWidth();
    int height = original.getHeight();
    Matrix matrix = new Matrix();
    matrix.preRotate(degrees);
    Bitmap rotatedBitmap = Bitmap.createBitmap(original, 0, 0, width, height, matrix, true);
    return rotatedBitmap;
  }
  
  public String windowsfont(int x_coordinates, int y_coordinates, int fontsize, int Direction, Typeface typeface, int maxWidth, int align, String textToPrint) {
    Layout.Alignment my_align;
    Bitmap original_bitmap = null;
    Bitmap gray_bitmap = null;
    Bitmap binary_bitmap = null;
    Paint paint = new Paint();
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(-16777216);
    paint.setAntiAlias(true);
    paint.setTypeface(typeface);
    paint.setTextSize(fontsize);
    TextPaint textpaint = new TextPaint(paint);
    if (align == 2) {
      my_align = Layout.Alignment.ALIGN_OPPOSITE;
    } else if (align == 1) {
      my_align = Layout.Alignment.ALIGN_CENTER;
    } else {
      my_align = Layout.Alignment.ALIGN_NORMAL;
    } 
    StaticLayout staticLayout = new StaticLayout(
        textToPrint, 0, textToPrint.length(), textpaint, maxWidth, 
        my_align, 1.0F, 0.0F, false);
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
    switch (Direction) {
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
        return "-1";
    } 
    gray_bitmap = bitmap2Gray(rotate_bmp);
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
        if (total == 0)
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
    return "1";
  }
  
  public void sendbitmap(int x_coordinates, int y_coordinates, Bitmap original_bitmap) {
    sendbitmap(x_coordinates, y_coordinates, original_bitmap, 128);
  }
  
  public void sendbitmap(int x_coordinates, int y_coordinates, Bitmap original_bitmap, int threshold) {
    Bitmap gray_bitmap = null;
    Bitmap binary_bitmap = null;
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inPurgeable = true;
    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
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
        if (total == 0)
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
    header = null;
    encoded_data = null;
  }
  
  private void sendbitmap_resize(int x_coordinates, int y_coordinates, Bitmap original_bitmap, int resize_width, int resize_height) {
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
          page.render(bitmap, new Rect(0, 0, width, height), null, 1);
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
          page.render(bitmap, new Rect(0, 0, width, height), null, 1);
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
        page.render(bitmap, new Rect(0, 0, width, height), null, 1);
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
        page.render(bitmap, new Rect(0, 0, width, height), null, 1);
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
  
  public String printPDFbyFile(File file, int x_coordinates, int y_coordinates, int printer_dpi) {
    try {
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
        page.render(bitmap, new Rect(0, 0, width, height), null, 1);
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
        page.render(bitmap, new Rect(0, 0, width, height), null, 1);
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
}
 {
  
}
