package com.teknorota.tm_native_media;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/** TMNativeMedia */
public class TMNativeMedia implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  static EventChannel.EventSink transcodeEventSink = null;
  private Context context;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "tm_native_media");
    EventChannel transcodeEventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "transcode_event_channel");
    channel.setMethodCallHandler(this);
    context = flutterPluginBinding.getApplicationContext();

    transcodeEventChannel.setStreamHandler(new EventChannel.StreamHandler() {
      @Override
      public void onListen(Object arguments, EventChannel.EventSink events) {
        transcodeEventSink = events;
      }

      @Override
      public void onCancel(Object arguments) {
        transcodeEventSink = null;
      }
    });
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    switch (call.method) {
      case "compressImage":
        String inputPath = call.argument("input");
        String outputPath = call.argument("output");
        int width = call.argument("width") == null ? 0 : (int) call.argument("width");
        int height = call.argument("height") == null ? 0 : (int) call.argument("height");
        int quality = call.argument("quality");
        try {
          File outputFile = new File(outputPath);
          if(!outputFile.exists()) {
              outputFile.createNewFile();
          }
          Bitmap scaledImage = ImageTools.scaleImage(inputPath, height, width);
          ByteArrayOutputStream compressStream = ImageTools.compressImage(scaledImage, quality);
          OutputStream outputStream = new FileOutputStream(outputPath);
          compressStream.writeTo(outputStream);

          result.success(outputPath);
        } catch (Exception ex) {
          result.error("Exception", ex.getMessage(), null);
        }
        break;
      case "processImage":
        String inputPath1 = call.argument("input");
        String outputPath1 = call.argument("output");
        int quality1 = call.argument("quality");
        int cropX1 = call.argument("cropX") == null ? 0 : (int) call.argument("cropX");
        int cropY1 = call.argument("cropY") == null ? 0 : (int) call.argument("cropY");
        int cropWidth1 = call.argument("cropWidth") == null ? 0 : (int) call.argument("cropWidth");
        int cropHeight1 = call.argument("cropHeight") == null ? 0 : (int) call.argument("cropHeight");
        boolean verticalFlip1 = call.argument("verticalFlip");
        boolean horizontalFlip1 = call.argument("horizontalFlip");
        int rotate1 = call.argument("rotate") == null ? 0 : (int) call.argument("rotate");
        try {
          File outputFile1 = new File(outputPath1);
          if(!outputFile1.exists()) {
              outputFile1.createNewFile();
          }
          Bitmap inputBitmap1 = ImageTools.scaleImage(inputPath1, 0, 0);
          Bitmap croppedBitmap1 = ImageTools.cropImage(inputBitmap1, cropX1, cropY1, cropWidth1, cropHeight1);
          Bitmap rotatedBitmap1 = ImageTools.rotateImage(croppedBitmap1, rotate1);
          Bitmap flippedBitmap1 = rotatedBitmap1;
          if(verticalFlip1) {
            flippedBitmap1 = ImageTools.flipImageVertical(rotatedBitmap1);
          }
          if(horizontalFlip1) {
            flippedBitmap1 = ImageTools.flipImageHorizontal(flippedBitmap1);
          }
          ByteArrayOutputStream compressStream1 = ImageTools.compressImage(flippedBitmap1, quality1);
          OutputStream outputStream1 = new FileOutputStream(outputPath1);
          compressStream1.writeTo(outputStream1);

          result.success(outputPath1);
        } catch (Exception ex) {
          result.error("Exception", ex.getMessage(), null);
        }
        break;
      case "videoInformation":
        String inputPath2 = call.argument("input");
        result.success(VideoTools.getVideoInformation(inputPath2).toHashMap());
        break;
      case "processVideo":
        processVideo(call, result);
        break;
      case "addToGallery":
        addToGallery(call, result);
        break;
      default:
        result.notImplemented();
        break;
    }
  }

  public static void sendTranscodeEvent(VideoTranscodeEvent event) {
    if(transcodeEventSink != null) {
      transcodeEventSink.success(event.toHashMap());
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  private void processVideo(MethodCall call, Result result) {
    String id = call.argument("id");
    String inputPath = call.argument("input");
    String outputPath = call.argument("output");
    int trimStartMillis = call.argument("trimStartMillis") == null ? 0 : (int) call.argument("trimStartMillis");
    int trimEndMillis = call.argument("trimEndMillis") == null ? 0 : (int) call.argument("trimEndMillis");
    int rotate = call.argument("rotate") == null ? 0 : (int) call.argument("rotate");

    try {
      File outputFile = new File(outputPath);
      if(!outputFile.exists()) {
        outputFile.createNewFile();
      }
      VideoTools.processVideo(id, inputPath, outputPath, trimStartMillis, trimEndMillis, rotate, context);

      result.success(outputPath);
    } catch (Exception ex) {
      result.error("Exception", ex.getMessage(), null);
    }
  }

  private void addToGallery(MethodCall call, Result result) {
    String path = call.argument("path");
    File file = new File(path);

    try {
       MediaScannerConnection.scanFile(context, new String[]{file.toString()}, null, null);

      result.success(true);
    } catch (Exception ex) {
      result.error("Exception", ex.getMessage(), null);
    }
  }
}
