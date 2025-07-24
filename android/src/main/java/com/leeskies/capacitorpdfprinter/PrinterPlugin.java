package com.leeskies.capacitorpdfprinter;

import android.Manifest;
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

import java.io.File;

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
    @PluginMethod
    public void print(@NonNull PluginCall call) {
        String uri = call.getString("uri");
        try {
            String IPAddress = call.getString("IPAddress");
            Integer port = call.getInt("port");
            if (port == null) {
                call.reject("Please provide valid values.");
                return;
            }
            implementation.print(port, IPAddress, uri, call);
            call.resolve();
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }
    @PluginMethod
    public void printPdf(@NonNull PluginCall call) {
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
            implementation.printPdf(port, IPAddress, base64String, x, y, dpi, call);
            call.resolve();
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void printPdfWithSetup(@NonNull PluginCall call) {
        String base64String = call.getString("base64String");
        try {
            String IPAddress = call.getString("IPAddress");
            Integer x = call.getInt("offsetX");
            Integer y = call.getInt("offsetY");
            Integer port = call.getInt("port");
            Integer width = call.getInt("width");
            Integer height = call.getInt("height");
            Integer dpi = call.getInt("dpi");
            if (port == null) {
                call.reject("Please provide valid values.");
                return;
            }
            implementation.printPdf(port, IPAddress, base64String, x, y, width, height, dpi, call);
            call.resolve();
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }
}