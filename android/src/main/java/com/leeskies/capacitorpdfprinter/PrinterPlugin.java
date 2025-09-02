package com.leeskies.capacitorpdfprinter;

import android.Manifest;
import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.example.tscdll.*;
import com.getcapacitor.annotation.Permission;

import org.json.JSONException;
import org.json.JSONArray;

import java.io.File;
import java.util.List;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.net.DhcpInfo;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.LinkProperties;
import android.net.RouteInfo;
import java.net.Inet4Address;

@CapacitorPlugin(name = "Printer",
        permissions = {
                @Permission(
                        alias = "internet",
                        strings = { Manifest.permission.INTERNET }
                ),
                @Permission(
                        alias = "storage",
                        strings = { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE }
                )
        }
)
public class PrinterPlugin extends Plugin {
    private Printer implementation = new Printer();
    // @PluginMethod
    // public void print(@NonNull PluginCall call) {
    //     String uri = call.getString("uri");
    //     try {
    //         String IPAddress = call.getString("IPAddress");
    //         Integer port = call.getInt("port");
    //         if (port == null) {
    //             call.reject("Please provide valid values.");
    //             return;
    //         }
    //         implementation.print(port, IPAddress, uri, call);
    //         call.resolve();
    //     } catch (Exception e) {
    //         call.reject(e.getMessage());
    //     }
    // }
    @PluginMethod
    public void printPDFByNetwork(@NonNull PluginCall call) {
        String base64String = call.getString("base64String");
        try {
            
            String IPAddress = call.getString("IPAddress");
            Integer x = call.getInt("offsetX");
            Integer y = call.getInt("offsetY");
            Integer port = call.getInt("port");
            Integer dpi = call.getInt("dpi");
            if (port == null) {
                call.reject("Please provide valid values.");
                return;
            }
            Log.d("PrinterPlugin", "=== CALLING PRINTER.PRINTPDFBYNETWORK ===");
            Context activityContext = this.getActivity();
            Log.d("PrinterPlugin", "Using Activity context: " + (activityContext != null ? activityContext.getClass().getSimpleName() : "null"));
            implementation.printPDFByNetwork(port, IPAddress, base64String, x, y, dpi, call, activityContext != null ? activityContext : this.getContext());
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void printPDFByUSB(@NonNull PluginCall call) {
        String base64String = call.getString("base64String");
        try {
            Integer x = call.getInt("offsetX");
            Integer y = call.getInt("offsetY");
            Integer dpi = call.getInt("dpi");
            if (x == null || y == null || dpi == null) {
                call.reject("Please provide valid values for offsetX, offsetY, and dpi.");
                return;
            }
            Log.d("PrinterPlugin", "=== CALLING PRINTER.PRINTPDFBYUSB ===");
            Context activityContext = this.getActivity();
            Log.d("PrinterPlugin", "Using Activity context: " + (activityContext != null ? activityContext.getClass().getSimpleName() : "null"));
            implementation.printPDFByUSB(base64String, x, y, dpi, call, activityContext != null ? activityContext : this.getContext());
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void discoverNetworkPrinters(@NonNull PluginCall call) {
        try {
            Log.d("PrinterPlugin", "=== CALLING NETWORK PRINTER DISCOVERY ===");
            
            // Get optional timeout parameter (default 5000ms)
            Integer timeoutMs = call.getInt("timeoutMs");
            if (timeoutMs == null) {
                timeoutMs = 5000; // Default 5 second timeout
            }
            
            // Get optional returnFirst parameter (default false)
            Boolean returnFirst = call.getBoolean("returnFirst");
            if (returnFirst == null) {
                returnFirst = false; // Default to find all printers
            }
            
            // Get optional targetMacAddress parameter
            String targetMacAddress = call.getString("targetMacAddress");
            
            Log.d("PrinterPlugin", "Discovery timeout set to: " + timeoutMs + "ms, returnFirst: " + returnFirst + ", targetMacAddress: " + targetMacAddress);
            
            // Create network core instance for discovery
            Context context = this.getActivity() != null ? this.getActivity() : this.getContext();
            TSCNetworkCore networkCore = new TSCNetworkCore(context);
            
            // Perform discovery with parameters
            List<TSCNetworkCore.PrinterInfo> discoveredPrinters = networkCore.discoverNetworkPrinters(timeoutMs, returnFirst, targetMacAddress);
            
            // Convert results to JSON
            JSONArray printersArray = new JSONArray();
            for (TSCNetworkCore.PrinterInfo printer : discoveredPrinters) {
                JSObject printerObj = new JSObject();
                printerObj.put("ipAddress", printer.ipAddress);
                printerObj.put("name", printer.name);
                printerObj.put("macAddress", printer.macAddress);
                printerObj.put("status", printer.status);
                printersArray.put(printerObj);
            }
            
            // Return results
            JSObject result = new JSObject();
            result.put("printers", printersArray);
            result.put("count", discoveredPrinters.size());
            
            Log.d("PrinterPlugin", "Discovery completed, returning " + discoveredPrinters.size() + " printers");
            call.resolve(result);
            
        } catch (Exception e) {
            Log.e("PrinterPlugin", "Error during network printer discovery: " + e.getMessage(), e);
            call.reject("Network printer discovery failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void configureStaticIP(@NonNull PluginCall call) {
        try {
            Log.d("PrinterPlugin", "=== CALLING CONFIGURE STATIC IP ===");
            
            // Get required parameters
            String printerIP = call.getString("printerIP");
            Integer port = call.getInt("port");
            String staticIP = call.getString("staticIP");
            
            // Validate required parameters
            if (printerIP == null || staticIP == null) {
                call.reject("Missing required parameters: printerIP and staticIP are required");
                return;
            }
            
            if (port == null) {
                port = 9100; // Default printer port
            }
            
            // Get current network info automatically (works with WiFi and Ethernet)
            Context context = this.getActivity() != null ? this.getActivity() : this.getContext();
            
            NetworkInfo networkInfo = getNetworkInfo(context);
            if (networkInfo == null) {
                call.reject("Unable to get network information - ensure WiFi or Ethernet is connected");
                return;
            }
            
            // Auto-detect subnet mask and gateway from current network
            String gateway = networkInfo.gateway;
            String subnetMask = networkInfo.subnetMask;
            
            Log.d("PrinterPlugin", "Auto-detected network - Gateway: " + gateway + ", Subnet: " + subnetMask);
            Log.d("PrinterPlugin", "Configuring static IP - Printer: " + printerIP + ":" + port + 
                  ", New Static IP: " + staticIP);
            
            // Create network core and configure static IP
            TSCNetworkCore networkCore = new TSCNetworkCore(context);
            
            // Connect to printer
            String connectResult = networkCore.openport(printerIP, port);
            if (connectResult == null || connectResult.equals("-1") || connectResult.equals("-2")) {
                call.reject("Failed to connect to printer at " + printerIP + ":" + port + " (result: " + connectResult + ")");
                return;
            }
            
            Log.d("PrinterPlugin", "Connected to printer, sending static IP configuration");
            
            // Configure static IP using auto-detected network settings
            String configResult = networkCore.WiFi_StaticIP(staticIP, subnetMask, gateway);
            
            // Close connection
            networkCore.closeport(1000);
            
            if (configResult != null && configResult.equals("1")) {
                Log.d("PrinterPlugin", "Static IP configuration sent successfully");
                JSObject result = new JSObject();
                result.put("success", true);
                result.put("staticIP", staticIP);
                result.put("gateway", gateway);
                result.put("subnetMask", subnetMask);
                result.put("message", "Printer configured with static IP " + staticIP + ". Printer may restart to apply changes.");
                call.resolve(result);
            } else {
                call.reject("Failed to send static IP configuration to printer (result: " + configResult + ")");
            }
            
        } catch (Exception e) {
            Log.e("PrinterPlugin", "Error configuring static IP: " + e.getMessage(), e);
            call.reject("Static IP configuration failed: " + e.getMessage());
        }
    }

    private String intToIp(int ip) {
        return String.format("%d.%d.%d.%d",
            (ip & 0xff),
            (ip >> 8 & 0xff),
            (ip >> 16 & 0xff),
            (ip >> 24 & 0xff));
    }
    
    // Helper class to hold network information
    private static class NetworkInfo {
        String gateway;
        String subnetMask;
        String connectionType;
        
        NetworkInfo(String gateway, String subnetMask, String connectionType) {
            this.gateway = gateway;
            this.subnetMask = subnetMask;
            this.connectionType = connectionType;
        }
    }
    
    // Get network info that works with both WiFi and Ethernet
    private NetworkInfo getNetworkInfo(Context context) {
        try {
            // Try WiFi first (most common)
            NetworkInfo wifiInfo = getWiFiNetworkInfo(context);
            if (wifiInfo != null) {
                Log.d("PrinterPlugin", "Using WiFi network information");
                return wifiInfo;
            }
            
            // Fall back to general network info (works with Ethernet)
            NetworkInfo generalInfo = getGeneralNetworkInfo(context);
            if (generalInfo != null) {
                Log.d("PrinterPlugin", "Using general network information (likely Ethernet)");
                return generalInfo;
            }
            
            Log.w("PrinterPlugin", "No network information available");
            return null;
            
        } catch (Exception e) {
            Log.e("PrinterPlugin", "Error getting network info: " + e.getMessage());
            return null;
        }
    }
    
    // Get WiFi-specific network info
    private NetworkInfo getWiFiNetworkInfo(Context context) {
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null) return null;
            
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
            
            if (wifiInfo == null || dhcpInfo == null || !wifiManager.isWifiEnabled()) {
                return null;
            }
            
            String gateway = intToIp(dhcpInfo.gateway);
            String subnetMask = intToIp(dhcpInfo.netmask);
            
            if (gateway.equals("0.0.0.0") || subnetMask.equals("0.0.0.0")) {
                return null;
            }
            
            return new NetworkInfo(gateway, subnetMask, "WiFi");
            
        } catch (Exception e) {
            Log.w("PrinterPlugin", "Failed to get WiFi network info: " + e.getMessage());
            return null;
        }
    }
    
    // Get general network info (works with Ethernet and other connections)
    private NetworkInfo getGeneralNetworkInfo(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager == null) return null;
            
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) return null;
            
            LinkProperties linkProperties = connectivityManager.getLinkProperties(activeNetwork);
            if (linkProperties == null) return null;
            
            // Find gateway from routes
            String gateway = null;
            for (RouteInfo route : linkProperties.getRoutes()) {
                if (route.isDefaultRoute() && route.getGateway() instanceof Inet4Address) {
                    gateway = route.getGateway().getHostAddress();
                    break;
                }
            }
            
            if (gateway == null) {
                Log.w("PrinterPlugin", "No default gateway found");
                return null;
            }
            
            // For Ethernet and other connections, typically use standard subnet mask
            // This is a reasonable default for most networks
            String subnetMask = "255.255.255.0";
            
            android.net.NetworkInfo netInfo = connectivityManager.getNetworkInfo(activeNetwork);
            String connectionType = (netInfo != null) ? netInfo.getTypeName() : "Unknown";
            
            Log.d("PrinterPlugin", "Detected connection type: " + connectionType);
            
            return new NetworkInfo(gateway, subnetMask, connectionType);
            
        } catch (Exception e) {
            Log.w("PrinterPlugin", "Failed to get general network info: " + e.getMessage());
            return null;
        }
    }

    // @PluginMethod
    // public void printPdfWithSetup(@NonNull PluginCall call) {
    //     String base64String = call.getString("base64String");
    //     try {
    //         String IPAddress = call.getString("IPAddress");
    //         Integer x = call.getInt("offsetX");
    //         Integer y = call.getInt("offsetY");
    //         Integer port = call.getInt("port");
    //         Integer width = call.getInt("width");
    //         Integer height = call.getInt("height");
    //         Integer dpi = call.getInt("dpi");
    //         if (port == null) {
    //             call.reject("Please provide valid values.");
    //             return;
    //         }
    //         implementation.printPdf(port, IPAddress, base64String, x, y, width, height, dpi, call, this.getContext());
    //     } catch (Exception e) {
    //         call.reject(e.getMessage());
    //     }
    // }
}