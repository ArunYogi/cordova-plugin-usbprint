package com.usbprint.cordova;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbDeviceConnection;
import android.util.Log;

import java.io.UnsupportedEncodingException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

public class Printer {

    private static final String TAG = "USBPrint";
    private String printername = null;
    private boolean connected = false;
    private UsbDevice device;
    private UsbManager usbManager;
    private UsbEndpoint ep = null;
    private UsbInterface usbInt = null;
    private UsbDeviceConnection conn = null;
    private CallbackContext callbackContext;

    public Printer(UsbManager usbManager, UsbDevice usbDevice, String printer_name, CallbackContext callbackContext) {
        this.usbManager = usbManager;
        this.device = usbDevice;
        this.printername = printer_name;
        this.callbackContext = callbackContext;
    }

    public String getPrinterName() {
        return printername;
    }

    public synchronized void changeStateToConnected() {
        this.connected = true;
        if (this.callbackContext != null) {
            PluginResult res = new PluginResult(PluginResult.Status.OK, "Connected");
            res.setKeepCallback(true);
            this.callbackContext.sendPluginResult(res);
        }
    }

    public byte revByte() {
        byte[] bits = new byte[2];
        try {
            if (this.conn == null) {
                this.conn = this.usbManager.openDevice(this.device);
            }
            this.conn.controlTransfer(161, 1, 0, 0, bits, bits.length, 0);
        } catch (Exception exp) {
            Log.e(TAG, "Exception thrown while connecting to usb printer", exp);
            this.close();
            return bits[0];
        }
        return bits[0];
    }

    public boolean isPaperAvailable() {
        byte isHasPaper = this.revByte();
        if (isHasPaper == 0x38) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isPermissionGranted() {
        return this.usbManager.hasPermission(this.device);
    }

    public synchronized void close() {
        this.connected = false;
        if (this.conn != null) {
            this.conn.close();
            this.ep = null;
            this.usbInt = null;
            this.conn = null;
        }
        if (this.callbackContext != null) {
            this.callbackContext.error("DisConnected");
            this.callbackContext = null;
        }
    }

    public synchronized void cutPaper(int n) {
        byte[] bits = new byte[4];
        bits[0] = 29;
        bits[1] = 86;
        bits[2] = 66;
        bits[3] = ((byte) n);
        sendByte(bits);
    }

    public synchronized void catPaperByMode(int mode) {
        byte[] bits = new byte[3];
        switch (mode) {
        case 0:
            bits[0] = 29;
            bits[1] = 86;
            bits[2] = 48;
            break;
        case 1:
            bits[0] = 29;
            bits[1] = 86;
            bits[2] = 49;
            break;
        }
        sendByte(bits);
    }

    public synchronized void openCashBox() {
        byte[] bits = new byte[5];
        bits[0] = 27;
        bits[1] = 112;
        bits[2] = 0;
        bits[3] = 64;
        bits[4] = 80;
        sendByte(bits);
    }

    public synchronized void defaultBuzzer() {
        byte[] bits = new byte[4];
        bits[0] = 27;
        bits[1] = 66;
        bits[2] = 4;
        bits[3] = 1;
        sendByte(bits);
    }

    public synchronized void buzzer(int n, int time) {
        byte[] bits = new byte[4];
        bits[0] = 27;
        bits[1] = 66;
        bits[2] = ((byte) n);
        bits[3] = ((byte) time);
        sendByte(bits);
    }

    public synchronized void setBuzzerMode(int n, int time, int mode) {
        byte[] bits = new byte[5];
        bits[0] = 27;
        bits[1] = 67;
        bits[2] = ((byte) n);
        bits[3] = ((byte) time);
        bits[4] = ((byte) mode);
        sendByte(bits);
    }

    public synchronized void sendMsg(String msg, String charset) {
        if (msg.length() == 0) {
            return;
        }
        byte[] send;
        try {
            send = msg.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            send = msg.getBytes();
        }
        sendByte(send);
        sendByte(new byte[] { 13, 10 });
    }

    public void sendByte(byte[] bits) {
        if (bits == null) {
            return;
        }
        if ((this.ep != null) && (this.usbInt != null) && (this.conn != null)) {
            this.conn.bulkTransfer(this.ep, bits, bits.length, 0);
        } else {
            if (this.conn == null) {
                this.conn = this.usbManager.openDevice(this.device);
            }
            if (this.device.getInterfaceCount() == 0) {
                return;
            }
            this.usbInt = this.device.getInterface(0);
            if (this.usbInt.getEndpointCount() == 0) {
                return;
            }
            for (int i = 0; i < this.usbInt.getEndpointCount(); i++) {
                if ((this.usbInt.getEndpoint(i).getType() == 2) && (this.usbInt.getEndpoint(i).getDirection() != 128)) {
                    this.ep = this.usbInt.getEndpoint(i);
                }
            }
            if (this.conn.claimInterface(this.usbInt, true)) {
                this.conn.bulkTransfer(this.ep, bits, bits.length, 0);
            }
        }
    }
}