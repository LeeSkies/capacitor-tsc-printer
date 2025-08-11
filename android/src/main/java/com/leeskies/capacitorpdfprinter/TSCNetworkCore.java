package com.leeskies.capacitorpdfprinter;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;
import android.os.Handler;
import android.os.Looper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class TSCNetworkCore {
  private static final String TAG = "Printer";
  private static final boolean D = true;
  
  private Context context;
  
  private InputStream InStream = null;
  private OutputStream OutStream = null;
  private Socket socket = null;
  private int port_connected = 0;
  
  // UDP discovery fields
  private int[] decipaddress = new int[200];
  private static String[] get_NAME = new String[100];
  private static String[] get_IP = new String[100];
  private static String[] get_MAC = new String[100];
  private static String[] get_status = new String[100];
  public String[] UDP_NAME = new String[100];
  public String[] UDP_IP = new String[100];
  public String[] UDP_MAC = new String[100];
  public String[] UDP_status = new String[100];
  
  private byte[] udp_data = new byte[512];
  private static byte[] udpbyte = new byte[1024];
  private byte[] buffer = new byte[1024];
  private byte[] readBuf = new byte[1024];
  private static int counter = 0;
  private static String receive_data = "";
  
  public TSCNetworkCore(Context context) {
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
              TSCNetworkCore.this.socket = new Socket();
              TSCNetworkCore.this.socket.connect(new InetSocketAddress(ipaddress, portnumber), 2000);
              TSCNetworkCore.this.InStream = TSCNetworkCore.this.socket.getInputStream();
              TSCNetworkCore.this.OutStream = TSCNetworkCore.this.socket.getOutputStream();
              TSCNetworkCore.this.port_connected = 1;
            } catch (Exception ex) {
              TSCNetworkCore.this.port_connected = 0;
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
              TSCNetworkCore.this.socket = new Socket();
              TSCNetworkCore.this.socket.connect(new InetSocketAddress(ipaddress, portnumber), 2000);
              TSCNetworkCore.this.InStream = TSCNetworkCore.this.socket.getInputStream();
              TSCNetworkCore.this.OutStream = TSCNetworkCore.this.socket.getOutputStream();
              TSCNetworkCore.this.port_connected = 1;
            } catch (Exception ex) {
              TSCNetworkCore.this.port_connected = 0;
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

  public String sendcommand_thread(final String message) {
    if (this.port_connected == 0)
      return "-1"; 
    Log.e("sendcommand_thread", Thread.currentThread().toString());
    Thread thread = new Thread(new Runnable() {
          public void run() {
            Log.e("sendcommand_thread2", Thread.currentThread().toString());
            byte[] msgBuffer = message.getBytes();
            try {
              TSCNetworkCore.this.OutStream.write(msgBuffer);
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

  public String sendBitmapData(String command, byte[] stream) {
    if (this.port_connected == 0)
      return "-1"; 
    if (this.OutStream == null)
      return "-1";
    
    try {
      // Send the BITMAP command
      this.OutStream.write(command.getBytes());
      this.OutStream.flush();
      
      // Send the bitmap data
      this.OutStream.write(stream);
      this.OutStream.flush();
      
      // Send line ending
      this.OutStream.write("\r\n".getBytes());
      this.OutStream.flush();
      
      return "1";
    } catch (IOException e) {
      Log.e(TAG, "IOException in sendBitmapData: " + e.getMessage());
      return "-1";
    }
  }

  public String sendPrintCommand() {
    if (this.port_connected == 0)
      return "-1"; 
    String message = "PRINT 1,1\r\n";
    return sendcommand(message);
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
    receive_data = "";
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    }
    
    try {
      while (this.InStream.available() > 1) {
        int length = this.InStream.read(this.readBuf);
        receive_data = String.valueOf(receive_data) + new String(this.readBuf);
        try {
          Thread.sleep(100);
        } catch (InterruptedException e1) {
          e1.printStackTrace();
        }
      }
    } catch (IOException e) {
      return false;
    }
    
    if (receive_data.contains("ENDLINE\r\n")) {
      receive_data = receive_data.replace("ENDLINE\r\n", "");
      return true;
    }
    return false;
  }
  
  private boolean ReadStream_judge(int delay) {
    receive_data = "";
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e2) {
      e2.printStackTrace();
    }
    
    try {
      while (this.InStream.available() > 1) {
        int length = this.InStream.read(this.readBuf);
        receive_data = String.valueOf(receive_data) + new String(this.readBuf);
        try {
          Thread.sleep(100);
        } catch (InterruptedException e1) {
          e1.printStackTrace();
        }
      }
    } catch (IOException e) {
      return false;
    }
    
    if (receive_data.contains("ENDLINE\r\n")) {
      receive_data = receive_data.replace("ENDLINE\r\n", "");
      return true;
    }
    return false;
  }

}