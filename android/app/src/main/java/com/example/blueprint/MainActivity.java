package com.example.blueprint;

import android.util.Log;

import androidx.annotation.NonNull;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_ADVERTISE;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Writer;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import BpPrinter.mylibrary.BpPrinter;
import BpPrinter.mylibrary.CardReader;
import BpPrinter.mylibrary.CardScanner;
import BpPrinter.mylibrary.Scrybe;
import BpPrinter.mylibrary.BluetoothConnectivity;

@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends FlutterActivity implements CardScanner, Scrybe{
    private static final String RECEIVE_CHANNEL = "samples.flutter.dev/blueprint";
    private static final String SEND_CHANNEL = "samples.flutter.dev/blueprint_send";

    BluetoothConnectivity m_AemScrybeDevice;
    CardReader m_cardReader = null;
    public BpPrinter m_AemPrinter = null;
    ArrayList<String> printerList;
    int glbPrinterWidth = 32;
    int numChars;
    private static final String[] INITIAL_PERMS = {
            BLUETOOTH_SCAN,
            BLUETOOTH,
            BLUETOOTH_CONNECT,
            BLUETOOTH_ADVERTISE,
            ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION,
            READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE,
            MANAGE_EXTERNAL_STORAGE,
            BLUETOOTH_ADMIN};
    private static final int INITIAL_REQUEST = 1337;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), RECEIVE_CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            if (call.method.equals("connect")) {
                                //switchSearch();
                                Log.d("TAG", "successfully connected to the printer!");
                                result.success("connected!!!");
                            } else if (call.method.equals("print")){
                                Log.d("TAG", "printing the document.");
                                result.success("printing.........");
                            } else {
                                result.notImplemented();
                            }
                        }
                );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this, INITIAL_PERMS, INITIAL_REQUEST);// Request for Permissions for access foe Android

        m_AemScrybeDevice = new BluetoothConnectivity(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {// Selection of Printer for Bluetooth Connection
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Select Printer to connect");

        for (int i = 0; i < printerList.size(); i++) {
            menu.add(0, v.getId(), 0, printerList.get(i));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);
        String printerName = item.getTitle().toString();
        try {
            m_AemScrybeDevice.connectToPrinter(printerName);
            m_cardReader = m_AemScrybeDevice.getCardReader(this);
            m_AemPrinter = m_AemScrybeDevice.getAemPrinter();
            Toast.makeText(MainActivity.this, "Connected with " + printerName, Toast.LENGTH_SHORT).show();
            // btn_fw_upgrade.setEnabled(true);
        } catch (IOException e) {
            if (e.getMessage().contains("Service discovery failed")) {
                Toast.makeText(MainActivity.this, "Not Connected\n" + printerName + " is unreachable or off otherwise it is connected with other device", Toast.LENGTH_SHORT).show();
            } else if (e.getMessage().contains("Device or resource busy")) {
                Toast.makeText(MainActivity.this, "the device is already connected", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    @Override
    public void onScanMSR(String s, CardReader.CARD_TRACK card_track) {

    }

    @Override
    public void onScanDLCard(String s) {

    }

    @Override
    public void onScanRCCard(String s) {

    }

    @Override
    public void onScanRFD(String s) {

    }

    @Override
    public void onScanPacket(String s) {

    }

    @Override
    public void onScanFwUpdateRespPacket(byte[] bytes) {

    }

    @Override
    public void onDiscoveryComplete(ArrayList<String> arrayList) {

    }

    public void onPairedPrinters(View view) {

        String p = (String) m_AemScrybeDevice.pairPrinter("BTprinter0314");
        // showAlert(p);
        printerList = (ArrayList<String>) m_AemScrybeDevice.getPairedPrinters();

        if (printerList.size() > 0)
            openContextMenu(view);
        else
            showAlert("No Paired Printers found");
    }

    public void showAlert(String alertMsg) {
        AlertDialog.Builder alertBox = new AlertDialog.Builder(MainActivity.this);

        alertBox.setMessage(alertMsg).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                return;
            }
        });

        AlertDialog alert = alertBox.create();
        alert.show();
    }

    public void onTestPrinter(View view) throws IOException {
        String data1 = "CENTER ALIGNMENT\n";
        m_AemPrinter.POS_Set_Text_alingment((byte) 0x01);
        m_AemPrinter.print(data1);
        String data2 = "LEFT ALIGNMENT\n";
        m_AemPrinter.POS_Set_Text_alingment((byte) 0x02);
        m_AemPrinter.print(data2);
        String data3 = "RIGHT ALIGNMENT\n";
        m_AemPrinter.POS_Set_Text_alingment((byte) 0x00);
        m_AemPrinter.print(data3);
        String data4 = "TAHOMA\n";
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x01);
        m_AemPrinter.print(data4);
        String data5 = "CALIBRI\n";
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x02);
        m_AemPrinter.print(data5);
        String data6 = "VERDANA\n";
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x03);
        m_AemPrinter.print(data6);
        String data7 = "NORMAL\n";
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x00);
        m_AemPrinter.print(data7);
        String data8 = "DOUBLE HEIGHT\n";
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x10);
        m_AemPrinter.print(data8);
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x00);
        String data9 = "DOUBLE WIDTH\n";
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x20);
        m_AemPrinter.print(data9);
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x00);
        String data10 = "UNDERLINE TEXT\n";
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x80);
        m_AemPrinter.print(data10);
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x00);
        String data11 = "NEGATIVE TEXT";
        m_AemPrinter.POS_text_Character_Spacing((byte) 0x00);
        m_AemPrinter.POS_text_Reverse_Printing((byte) 0x01);
        m_AemPrinter.print(data11);
        m_AemPrinter.setCarriageReturn();
        m_AemPrinter.setCarriageReturn();
        m_AemPrinter.POS_text_Reverse_Printing((byte) 0x00);
        // m_AemPrinter.printTextAsImage(data11);
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x00);
        m_AemPrinter.Initialize_Printer();
        m_AemPrinter.setCarriageReturn();
        m_AemPrinter.setCarriageReturn();
        m_AemPrinter.POS_Set_Text_alingment((byte) 0x01);
        numChars = glbPrinterWidth;//CheckPrinterWidth();
        onPrintBillBluetooth(numChars);
    }

    public void onPrintBillBluetooth(int numChars) {

        if (m_AemPrinter == null) {
            Toast.makeText(MainActivity.this, "Printer not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        m_AemPrinter.POS_Set_Text_alingment((byte) 0x01);
        String data = "TWO INCH PRINTER: TEST PRINT\n ";
        m_AemPrinter.POS_Set_Text_alingment((byte) 0x01);
        String d = " _______________________________\n";
        try {
            if (numChars == 32) {
                m_AemPrinter.print(data);
                m_AemPrinter.print(d);
                m_AemPrinter.POS_Set_Text_alingment((byte) 0x01);
                data = "CODE|DESC|RATE(Rs)|QTY |AMT(Rs)\n";
                m_AemPrinter.print(data);
                m_AemPrinter.print(d);
                data = "13|ColgateGel |35.00|02|70.00\n" +
                        "29|Pears Soap |25.00|01|25.00\n" +
                        "88|Lux Shower |46.00|01|46.00\n" +
                        "15|Dabur Honey|65.00|01|65.00\n" +
                        "52|Dairy Milk |20.00|10|200.00\n" +
                        "128|Maggie TS |36.00|04|144.00\n" +
                        "_______________________________\n";

                m_AemPrinter.print(data);
                m_AemPrinter.POS_Set_Text_alingment((byte) 0x01);
                data = "  TOTAL AMOUNT (Rs.)   550.00\n";
                m_AemPrinter.print(data);
                m_AemPrinter.print(d);
                data = "Thank you! \n";

            } else {
                data = "         THREE INCH PRINTER: TEST PRINT\n";
                m_AemPrinter.print(data);
                d = "________________________________________________\n";
                m_AemPrinter.print(d);
                data = "CODE|   DESCRIPTION   |RATE(Rs)|QTY |AMOUNT(Rs)\n";
                m_AemPrinter.print(data);
                m_AemPrinter.print(d);
                data = " 13 |Colgate Total Gel | 35.00  | 02 |  70.00\n" +
                        " 29 |Pears Soap 250g   | 25.00  | 01 |  25.00\n" +
                        " 88 |Lux Shower Gel 500| 46.00  | 01 |  46.00\n" +
                        " 15 |Dabur Honey 250g  | 65.00  | 01 |  65.00\n" +
                        " 52 |Cadbury Dairy Milk| 20.00  | 10 | 200.00\n" +
                        "128 |Maggie Totamto Sou| 36.00  | 04 | 144.00\n" +
                        "______________________________________________\n";

                m_AemPrinter.print(data);
                data = "          TOTAL AMOUNT (Rs.)   550.00\n";
                m_AemPrinter.print(data);
                m_AemPrinter.print(d);
                data = "        Thank you! \n";
            }
            m_AemPrinter.print(data);


        } catch (IOException e) {
            if (e.getMessage().contains("socket closed"))
                Toast.makeText(MainActivity.this, "Printer not connected", Toast.LENGTH_SHORT).show();

        }

    }

    public void onPrintBarcode(View view) {
        BarcodeBT();
    }

    public void BarcodeBT() {
        if (m_AemPrinter == null) {
            showAlert("Printer not connected");
            return;
        }
        String text = "*BLUPRINTS*";
        if (text.isEmpty()) {
            showAlert("Write Text TO Generate Barcode");
        } else {
            try {
                m_AemPrinter.printBarcode(text, BpPrinter.BARCODE_TYPE.CODE39,BpPrinter.BARCODE_HEIGHT.HT_SMALL, BpPrinter.CHAR_POSITION.POS_BELOW);
                m_AemPrinter.setLineFeed(2);
                String text1 = "12345678901";
                m_AemPrinter.printBarcode(text1, BpPrinter.BARCODE_TYPE.UPCE,BpPrinter.BARCODE_HEIGHT.HT_SMALL, BpPrinter.CHAR_POSITION.POS_ABOVE);
                m_AemPrinter.setLineFeed(2);
                String text2 = "0123456";
                m_AemPrinter.printBarcode(text2, BpPrinter.BARCODE_TYPE.EAN8,BpPrinter.BARCODE_HEIGHT.HT_MEDIUM,BpPrinter.CHAR_POSITION.POS_NONE);
                m_AemPrinter.setLineFeed(2);
                String text3 = "123456789123";
                m_AemPrinter.printBarcode(text3, BpPrinter.BARCODE_TYPE.EAN13,BpPrinter.BARCODE_HEIGHT.HT_LARGE,BpPrinter.CHAR_POSITION.POS_BELOW);
                m_AemPrinter.setLineFeed(2);
                m_AemPrinter.printBarcode(text1,BpPrinter.BARCODE_TYPE.UPCA,BpPrinter.BARCODE_HEIGHT.HT_LARGE,BpPrinter.CHAR_POSITION.POS_BOTH);
                m_AemPrinter.setLineFeed(2);
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.AutoCut();
            } catch (IOException e) {
                showAlert("Printer not connected");
            }
        }
    }

    public void onPrintImage(View view) {
        onPrintImage();
    }

    private void onPrintImage() {
        try {
            InputStream is = getAssets().open("bluprintlogo1.jpg");
            Bitmap inputBitmap = BitmapFactory.decodeStream(is);
            Bitmap resizedBitmap = null;
            if (glbPrinterWidth == 32)
                resizedBitmap = Bitmap.createScaledBitmap(inputBitmap, 384, 384, false);

            else
                resizedBitmap = Bitmap.createScaledBitmap(inputBitmap, 384, 384, false);
            //m_AemPrinter.printBitImage(resizedBitmap,BluetoothActivity.this,m_AemPrinter.IMAGE_CENTER_ALIGNMENT);

            m_AemPrinter.POS_Set_Text_alingment((byte) 0x01);
            m_AemPrinter.printImage(resizedBitmap);
            m_AemPrinter.setCarriageReturn();
            m_AemPrinter.setCarriageReturn();
            m_AemPrinter.setCarriageReturn();
            m_AemPrinter.setCarriageReturn();
        } catch (IOException e) {
        }
    }

    public void onPrintQRCodeRaster(View view) throws WriterException, IOException {
        if (m_AemPrinter == null) {
            Toast.makeText(MainActivity.this, "Printer not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        String text = "www.bluprints.in";
        if (text.isEmpty()) {
            showAlert("Write Text To Generate QR Code");
        } else {
            Writer writer = new QRCodeWriter();
            String finalData = Uri.encode(text, "UTF-8");
            showAlert("QR " + text);
            try {

                BitMatrix bm = writer.encode(finalData, BarcodeFormat.QR_CODE, 300, 300);
                Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
                for (int i = 0; i < 300; i++) {
                    for (int j = 0; j < 300; j++) {
                        bitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                    }
                }
                //showAlert("generating qr bitmap " + wifiConnection);
                Bitmap resizedBitmap = null;
                resizedBitmap = Bitmap.createScaledBitmap(bitmap, 384, 430, false);
                m_AemPrinter.printImage(resizedBitmap);
                m_AemPrinter.setLineFeed(8);


            } catch (WriterException e) {
                showAlert("Error WrQR: " + e.toString());
            }
        }
    }

    public void onPrint(View view) throws IOException {
        String data1 = "CENTER ALIGNMENT\n";
        m_AemPrinter.POS_Set_Text_alingment((byte) 0x01);
        m_AemPrinter.print(data1);
        String data2 = "LEFT ALIGNMENT\n";
        m_AemPrinter.POS_Set_Text_alingment((byte) 0x02);
        m_AemPrinter.print(data2);
        String data3 = "RIGHT ALIGNMENT\n";
        m_AemPrinter.POS_Set_Text_alingment((byte) 0x00);
        m_AemPrinter.print(data3);
        String data4 = "TAHOMA\n";
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x01);
        m_AemPrinter.print(data4);
        String data5 = "CALIBRI\n";
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x02);
        m_AemPrinter.print(data5);
        String data6 = "VERDANA\n";
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x03);
        m_AemPrinter.print(data6);
        String data7 = "NORMAL\n";
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x00);
        m_AemPrinter.print(data7);
        String data8 = "DOUBLE HEIGHT\n";
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x10);
        m_AemPrinter.print(data8);
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x00);
        String data9 = "DOUBLE WIDTH\n";
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x20);
        m_AemPrinter.print(data9);
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x00);
        String data10 = "UNDERLINE TEXT\n";
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x80);
        m_AemPrinter.print(data10);
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x00);
        String data11 = "NEGATIVE TEXT";
        m_AemPrinter.POS_text_Character_Spacing((byte) 0x00);
        m_AemPrinter.POS_text_Reverse_Printing((byte) 0x01);
        m_AemPrinter.print(data11);
        m_AemPrinter.setCarriageReturn();
        m_AemPrinter.setCarriageReturn();
        m_AemPrinter.POS_text_Reverse_Printing((byte) 0x00);
        m_AemPrinter.POS_Set_Char_Mode((byte) 0x00);
        m_AemPrinter.Initialize_Printer();
        m_AemPrinter.setCarriageReturn();
        m_AemPrinter.setCarriageReturn();
        m_AemPrinter.POS_Set_Text_alingment((byte) 0x01);

        String data = "TWO INCH PRINTER: TEST PRINT\n ";
        m_AemPrinter.POS_Set_Text_alingment((byte) 0x01);
        String d = " _______________________________\n";
        try {
            if (numChars == 32) {
                m_AemPrinter.print(data);
                m_AemPrinter.print(d);
                m_AemPrinter.POS_Set_Text_alingment((byte) 0x01);
                data = "CODE|DESC|RATE(Rs)|QTY |AMT(Rs)\n";
                m_AemPrinter.print(data);
                m_AemPrinter.print(d);
                data = "13|ColgateGel |35.00|02|70.00\n" +
                        "29|Pears Soap |25.00|01|25.00\n" +
                        "88|Lux Shower |46.00|01|46.00\n" +
                        "15|Dabur Honey|65.00|01|65.00\n" +
                        "52|Dairy Milk |20.00|10|200.00\n" +
                        "128|Maggie TS |36.00|04|144.00\n" +
                        "_______________________________\n";

                m_AemPrinter.print(data);
                m_AemPrinter.POS_Set_Text_alingment((byte) 0x01);
                data = "  TOTAL AMOUNT (Rs.)   550.00\n";
                m_AemPrinter.POS_LineSpacing((byte) 10);
                m_AemPrinter.print(data);
                m_AemPrinter.POS_LineSpacing((byte)11);
                m_AemPrinter.print(data);
                m_AemPrinter.print(d);
                data = "Thank you! \n";

            } else {
                data = "         THREE INCH PRINTER: TEST PRINT\n";
                m_AemPrinter.print(data);
                d = "________________________________________________\n";
                m_AemPrinter.print(d);
                data = "CODE|   DESCRIPTION   |RATE(Rs)|QTY |AMOUNT(Rs)\n";
                m_AemPrinter.print(data);
                m_AemPrinter.print(d);
                data = " 13 |Colgate Total Gel  | 35.00  | 02 |  70.00\n" +
                        " 29 |Pears Soap 250g   | 25.00  | 01 |  25.00\n" +
                        " 88 |Lux Shower Gel 500| 46.00  | 01 |  46.00\n" +
                        " 15 |Dabur Honey 250g  | 65.00  | 01 |  65.00\n" +
                        " 52 |Cadbury Dairy Milk| 20.00  | 10 | 200.00\n" +
                        "128 |Maggie Totamto Sou| 36.00  | 04 | 144.00\n" +
                        "______________________________________________\n";

                m_AemPrinter.print(data);
                data = "          TOTAL AMOUNT (Rs.)   550.00\n";
                m_AemPrinter.POS_LineSpacing((byte) 10);
                m_AemPrinter.print(data);
                m_AemPrinter.POS_LineSpacing((byte)11);
                m_AemPrinter.print(data);
                m_AemPrinter.print(d);
                data = "        Thank you! \n";
            }
            m_AemPrinter.print(data);
            m_AemPrinter.setCarriageReturn();
            m_AemPrinter.setCarriageReturn();
            m_AemPrinter.setCarriageReturn();

            try {

                InputStream is = getAssets().open("bluprintlogo1.jpg");
                Bitmap inputBitmap = BitmapFactory.decodeStream(is);
                Bitmap resizedBitmap = null;
                if (glbPrinterWidth == 32)
                    resizedBitmap = Bitmap.createScaledBitmap(inputBitmap, 384, 384, false);

                else
                    resizedBitmap = Bitmap.createScaledBitmap(inputBitmap, 384, 384, false);

                m_AemPrinter.POS_Set_Text_alingment((byte) 0x01);
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.setCarriageReturn();
                m_AemPrinter.printImage(resizedBitmap);

            } catch (IOException e) {
            }

            String text = "www.bluprints.in";
            if (text.isEmpty()) {
                showAlert("Write Text To Generate QR Code");
            } else {
                Writer writer = new QRCodeWriter();
                String finalData = Uri.encode(text, "UTF-8");
                try {

                    BitMatrix bm = writer.encode(finalData, BarcodeFormat.QR_CODE, 300, 300);
                    Bitmap bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
                    for (int i = 0; i < 300; i++) {
                        for (int j = 0; j < 300; j++) {
                            bitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                        }
                    }
                    Bitmap resizedBitmap = null;
                    resizedBitmap = Bitmap.createScaledBitmap(bitmap, 384, 430, false);
                    m_AemPrinter.printImage(resizedBitmap);
                    m_AemPrinter.setLineFeed(2);
                    m_AemPrinter.POS_LineSpacing((byte) 1);
                    m_AemPrinter.print(data);


                } catch (WriterException e) {
                    showAlert("Error WrQR: " + e.toString());

                }
            }
            String text1 = "*BLUPRINTS*";
            if (text1.isEmpty()) {
                showAlert("Write Text TO Generate Barcode");
            } else {
                try {
                    m_AemPrinter.printBarcode(text1, BpPrinter.BARCODE_TYPE.CODE39, BpPrinter.BARCODE_HEIGHT.HT_MEDIUM, BpPrinter.CHAR_POSITION.POS_BOTH);
                    m_AemPrinter.setLineFeed(2);
                    m_AemPrinter.setCarriageReturn();
                    m_AemPrinter.setCarriageReturn();
                    m_AemPrinter.setCarriageReturn();
                    String data16 = "\nData\n";
                    m_AemPrinter.print(data16);
                    m_AemPrinter.setCarriageReturn();
                    m_AemPrinter.setCarriageReturn();
                    m_AemPrinter.setCarriageReturn();

                } catch (IOException e) {
                    showAlert("Printer not connected");
                }
            }
        } finally {

        }
    }
}
