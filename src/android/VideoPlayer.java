package com.alejandro.videoplayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Override;
import java.lang.String;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;


public class VideoPlayer extends CordovaPlugin{
    private static final String YOU_TUBE = "youtube.com";
    private static final String ASSETS = "file:///android_asset/";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext){
        PluginResult.Status status = PluginResult.Status.OK;
        String result = "";

        try{
            if(action.equals("playVideo")){
                playVideo(args.getString(0));
            }
            else{
                status = PluginResult.Status.INVALID_ACTION;
            }
            callbackContext.sendPluginResult(new PluginResult(status, result));
        }catch (JSONException e){
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.JSON_EXCEPTION));
        }catch (IOException e){
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.IO_EXCEPTION));
        }
        return true;
    }//end execute

    private void playVideo(String url) throws IOException{
        //create URI
        Uri uri = Uri.parse(url);

        Intent intent = null;
        //Checks to see if trying to play youtube
        //TODO change this?
        if(url.contains(YOU_TUBE)){
            //OGNOTE: If we don't do it this way you don't have the option for youtube
            uri = Uri.parse("vnd.youtube:" + uri.getQueryParameter("v"));
            if(isYouTubeInstalled()){
                intent = new Intent(Intent.ACTION_VIEW, uri);
            }else{
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=com.google.android.youtube"));
            }
        }else if (url.contains(ASSETS)){
            //get file path in assets folder
            String filepath = url.replace(ASSETS,"");
            //get actual filename from path as command to write to internal storage doesn't like folders
            String filename = filepath.substring(filepath.lastIndexOf("/")+1, filepath.length());

            //Don't copy the file if it already exists
            File fp = new File(this.cordova.getActivity().getFilesDir() + "/" + filename);
            if(!fp.exists()){
                this.copy(filepath, filename);
            }

            //change uri to be the new file in internal storage
            uri = Uri.parse("file://" + this.cordova.getActivity().getFilesDir() + "/" + filename);

            //Display the video player
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/*");
        }else{
            //Display video player
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/*");
        }
        this.cordova.getActivity().startActivity(intent);
    } //end playVideo

    private void copy(String fileFrom, String fileTo) throws IOException{
        //get file to be copied from assets
        InputStream in = this.cordova.getActivity().getAssets().open(fileFrom);
        //get file where copied too, in general storage
        //must be MODE_WORLD_READABLE or Android can't play it
        FileOutputStream out = this.cordova.getActivity().openFileOutput(fileTo, Context.MODE_WORLD_READABLE);

        //Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while((len = in.read(buf)) > 0)
            out.write(buf, 0, len);
        in.close();
        out.close();
    } //end copy

    private boolean isYouTubeInstalled() {
        PackageManager pm = this.cordova.getActivity().getPackageManager();
        try{
            pm.getPackageInfo("com.google.android.youtube", PackageManager.GET_ACTIVITIES);
            return true;
        }catch (PackageManager.NameNotFoundException e){
            return false;
        }
    } //end isYouTubeInstalled
}