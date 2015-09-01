package de.cyberkatze.phonegap.plugin;

import java.io.File;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import de.cyberkatze.phonegap.plugin.FileHelper;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PdfViewer extends CordovaPlugin {

    private static final String TAG = "PdfViewerPlugin";

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        String url = data.getString(0);

        if (action.equals("openPDF")) {
        	openPDF(url);
        }

        callbackContext.success();
        return true;
    }//execute

    private void openPDF(final String fileName) {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                File file = new File(cordova.getActivity().getExternalFilesDir(null), fileName);

                if (!file.exists()) {

                    InputStream in = null;
                    OutputStream out = null;

                    try {
                        in = FileHelper.getInputStreamFromUriString("file:///android_asset/www/" + fileName, cordova);
                        String filePath = file.getAbsolutePath();
                        filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
                        File dbPathFile = new File(filePath);
                        if (!dbPathFile.exists()) {
                            dbPathFile.mkdirs();
                        }

                        File newFile = new File(filePath + fileName);
                        out = new FileOutputStream(newFile);

                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        Log.v("info", "Copied file content to: " + newFile.getAbsolutePath());
                    } catch (IOException e) {
                        Log.v("openPDF", "No file found, Error=" + e.getMessage());
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException ignored) {
                            }
                        }

                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException ignored) {
                            }
                        }
                    }

                    file.getParentFile().mkdirs();
                }

                if (file.exists()) {

                    try {
                        Intent intent = new Intent();
                        //intent.setPackage("com.adobe.reader");
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                        cordova.getActivity().startActivity(intent);
                    } catch (android.content.ActivityNotFoundException e) {
                        Log.e(TAG, "PdfViewer: Error loading url " + fileName + ":" + e.toString());
                    }

                } else {
                    Log.e(TAG, "File not found: " + fileName);
                }
            }//run
        });//runOnUiThread
    }//openPDF
}
