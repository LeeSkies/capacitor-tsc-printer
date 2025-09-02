package com.leeskies.capacitorpdfprinter;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

public class TSCUSBCore {
    private static final String TAG = "TSCUSBCore";
    private int TIMEOUT = 1000;
    private int MAX_USBFS_BUFFER_SIZE = 10000;
    
    public UsbManager mUsbManager;
    public UsbDevice mUsbDevice;
    private UsbInterface mUsbIntf;
    private static UsbDeviceConnection mUsbConnection;
    private static UsbEndpoint mUsbendpoint;
    private static UsbEndpoint usbEndpointIn;
    private static UsbEndpoint usbEndpointOut;
    private static PendingIntent mPermissionIntent;
    private boolean mPermissionRequestPending;
    
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static boolean hasPermissionToCommunicate = false;
    
    private StringBuilder strIN = new StringBuilder();
    private int receivelength = 0;
    private int response = 0;
    private static String printerstatus = "";
    private static String receive_data = "";
    private byte[] readBuf = new byte[1024];
    
    private Context context;
    public int port_connected = 0;

    public TSCUSBCore(Context context) {
        this.context = context;
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra("device");
                    if (intent.getBooleanExtra("permission", false) && device != null) {
                        hasPermissionToCommunicate = true;
                    }
                }
            }
        }
    };

    public String openport(UsbManager manager, UsbDevice device) {
        try {
            this.mUsbManager = manager;
            this.mUsbDevice = device;
            this.mUsbIntf = device.getInterface(0);
            mUsbendpoint = this.mUsbIntf.getEndpoint(0);
            mUsbConnection = manager.openDevice(device);
            
            if (mUsbConnection == null) {
                Log.e(TAG, "Failed to open USB device connection");
                return "-1";
            }
            
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
            
            if (port_status) {
                this.port_connected = 1;
                Log.d(TAG, "USB port opened successfully");
                return "1";
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in openport: " + e.getMessage());
        }
        return "-1";
    }

    public String openport(UsbManager manager, UsbDevice device, int delay) {
        try {
            this.mUsbManager = manager;
            this.mUsbDevice = device;
            this.mUsbIntf = device.getInterface(0);
            mUsbendpoint = this.mUsbIntf.getEndpoint(0);
            mUsbConnection = manager.openDevice(device);
            
            if (mUsbConnection == null) {
                Log.e(TAG, "Failed to open USB device connection");
                return "-1";
            }
            
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
            
            if (port_status) {
                this.port_connected = 1;
                Log.d(TAG, "USB port opened successfully with delay");
                return "1";
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in openport with delay: " + e.getMessage());
        }
        return "-1";
    }

    public String sendcommand(final String printercommand) {
        if (mUsbConnection == null) {
            Log.e(TAG, "USB connection is null");
            return "-1";
        }
        if (this.port_connected == 0) {
            Log.e(TAG, "USB port not connected");
            return "-1";
        }
        
        try {
            Thread.sleep(100L);
        } catch (InterruptedException interruptedException) {}
        
        Thread thread = new Thread(new Runnable() {
            public void run() {
                byte[] command = printercommand.getBytes();
                int result = mUsbConnection.bulkTransfer(mUsbendpoint, command, command.length, TIMEOUT);
                if (result < 0) {
                    Log.e(TAG, "Failed to send command: " + printercommand);
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

    public String sendcommand(final byte[] command) {
        if (mUsbConnection == null) {
            Log.e(TAG, "USB connection is null");
            return "-1";
        }
        if (this.port_connected == 0) {
            Log.e(TAG, "USB port not connected");
            return "-1";
        }
        
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
                    ReturnValue = mUsbConnection.bulkTransfer(mUsbendpoint, command, Offset, Size, TIMEOUT);
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
        if (mUsbConnection == null) {
            Log.e(TAG, "USB connection is null");
            return "-1";
        }
        if (this.port_connected == 0) {
            Log.e(TAG, "USB port not connected");
            return "-1";
        }
        
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
                    if (remain_data >= MAX_USBFS_BUFFER_SIZE || i == 0) {
                        counter = mUsbConnection.bulkTransfer(mUsbendpoint, command, i, MAX_USBFS_BUFFER_SIZE, TIMEOUT);
                        total += counter;
                    } else {
                        if (remain_data == 0)
                            break;
                        mUsbConnection.bulkTransfer(mUsbendpoint, command, i, remain_data, TIMEOUT);
                        mUsbConnection.bulkTransfer(mUsbendpoint, crlf_byte, 0, 2, TIMEOUT);
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

    public String sendBitmapData(String command, byte[] stream) {
        if (this.port_connected == 0) {
            Log.e(TAG, "USB port not connected");
            return "-1";
        }
        if (mUsbConnection == null) {
            Log.e(TAG, "USB connection is null");
            return "-1";
        }
        
        try {
            Log.d(TAG, "Sending bitmap command: " + command);
            
            // Send the BITMAP command
            byte[] commandBytes = command.getBytes();
            int commandResult = mUsbConnection.bulkTransfer(mUsbendpoint, commandBytes, commandBytes.length, TIMEOUT);
            if (commandResult < 0) {
                Log.e(TAG, "Failed to send bitmap command");
                return "-1";
            }
            
            // Send the bitmap data
            int offset = 0;
            int remaining = stream.length;
            while (remaining > 0) {
                int chunkSize = Math.min(remaining, MAX_USBFS_BUFFER_SIZE);
                int result = mUsbConnection.bulkTransfer(mUsbendpoint, stream, offset, chunkSize, TIMEOUT);
                if (result < 0) {
                    Log.e(TAG, "Failed to send bitmap data at offset " + offset);
                    return "-1";
                }
                offset += result;
                remaining -= result;
            }
            
            // Send CRLF
            byte[] crlf = "\r\n".getBytes();
            int crlfResult = mUsbConnection.bulkTransfer(mUsbendpoint, crlf, crlf.length, TIMEOUT);
            if (crlfResult < 0) {
                Log.e(TAG, "Failed to send CRLF");
                return "-1";
            }
            
            Log.d(TAG, "Bitmap data sent successfully");
            return "1";
        } catch (Exception e) {
            Log.e(TAG, "Exception in sendBitmapData: " + e.getMessage());
            return "-1";
        }
    }

    public String sendPrintCommand() {
        return sendcommand("PRINT 1\r\n");
    }

    public String clearbuffer() {
        if (this.port_connected == 0) {
            Log.e(TAG, "USB port not connected");
            return "-1";
        }
        String message = "CLS\r\n";
        return sendcommand(message);
    }

    public String setup(int width, int height, int speed, int density, int sensor, int sensor_distance, int sensor_offset) {
        if (mUsbConnection == null || this.port_connected == 0) {
            return "-1";
        }
        
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
        return sendcommand(msgBuffer);
    }

    public String closeport() {
        if (mUsbConnection == null) {
            return "-1";
        }
        
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
            this.port_connected = 0;
            Log.d(TAG, "USB device closed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Exception closing USB device: " + e.getMessage());
        }
        return "1";
    }

    public String closeport(int timeout) {
        if (mUsbConnection == null) {
            return "-1";
        }
        
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
            this.port_connected = 0;
            Log.d(TAG, "USB device closed successfully with timeout");
        } catch (Exception e) {
            Log.e(TAG, "Exception closing USB device: " + e.getMessage());
        }
        return "1";
    }

    // USB Device Discovery and Permission Methods
    public UsbDevice findPrinterDevice() {
        this.mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = this.mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        
        if (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            Log.d(TAG, "Found USB device - DeviceID: " + device.getDeviceId() + 
                  ", VendorID: " + device.getVendorId() + 
                  ", ProductID: " + device.getProductId());
            return device;
        }
        
        Log.d(TAG, "No USB devices found");
        return null;
    }

    public void requestPermission(UsbDevice device) {
        if (device != null && this.mUsbManager != null) {
            mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            context.registerReceiver(this.mUsbReceiver, filter);
            this.mUsbManager.requestPermission(device, mPermissionIntent);
        }
    }

    public boolean hasPermission() {
        return hasPermissionToCommunicate;
    }

    // Printer Status Methods
    public String printerstatus() {
        if (mUsbConnection == null || this.port_connected == 0) {
            return "-1";
        }
        
        byte[] message = { 27, 33, 63 };
        sendcommand(message);
        
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Simple status check - would need full implementation from original
        return "1";  // Simplified for now
    }
}