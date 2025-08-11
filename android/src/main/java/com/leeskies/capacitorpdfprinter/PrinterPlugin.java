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
            
            // Get timeout parameter (default 5000ms)
            Integer timeoutMs = call.getInt("timeoutMs");
            if (timeoutMs == null) {
                timeoutMs = 5000; // Default 5 second timeout
            }
            
            Log.d("PrinterPlugin", "Discovery timeout set to: " + timeoutMs + "ms");
            
            // Create network core instance for discovery
            Context context = this.getActivity() != null ? this.getActivity() : this.getContext();
            TSCNetworkCore networkCore = new TSCNetworkCore(context);
            
            // Perform discovery
            List<TSCNetworkCore.PrinterInfo> discoveredPrinters = networkCore.discoverNetworkPrinters(timeoutMs);
            
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