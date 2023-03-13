import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';

class TMNativeMedia {
  static bool _transcodeEventLoaded = false;
  static const MethodChannel _channel = const MethodChannel('tm_native_media');
  static const EventChannel _transcodeEventChannel =
      EventChannel('transcode_event_channel');
  static Map<String, Function(VideoTranscodeEvent)> _transcodeEventCallbacks =
      {};
  static Map<String, Completer<File>> _transcodeEventCompleters = {};

  static Future<File> compressImage(
    String inputPath,
    String outputPath, {
    int quality = 70,
    int width = 0,
    int height = 0,
  }) async {
    var file = await _channel.invokeMethod("compressImage", {
      'input': inputPath,
      'output': outputPath,
      'quality': quality,
      'width': width,
      'height': height
    });

    return new File(file);
  }

  static Future<File> processImage(
    String inputPath,
    String outputPath, {
    int quality = 70,
    int cropX = 0,
    int cropY = 0,
    int cropWidth = 0,
    int cropHeight = 0,
    bool verticalFlip = false,
    bool horizontalFlip = false,
    int rotate = 0,
  }) async {
    var file = await _channel.invokeMethod("processImage", {
      'input': inputPath,
      'output': outputPath,
      'quality': quality,
      'cropX': cropX,
      'cropY': cropY,
      'cropWidth': cropWidth,
      'cropHeight': cropHeight,
      'verticalFlip': verticalFlip,
      'horizontalFlip': horizontalFlip,
      'rotate': rotate
    });

    return new File(file);
  }

  static Future<MediaInformation> videoInformation(
    String inputPath,
  ) async {
    Map info = await _channel.invokeMethod("videoInformation", {
      'input': inputPath,
    });

    final mi = MediaInformation(
      width: info['width'],
      height: info['height'],
      durationMs: info['durationMs'],
      orientation: info['orientation'],
      mimeType: info['mimeType'],
    );

    return mi;
  }

  static Future<File> processVideo(
    String inputPath,
    String outputPath, {
    String id = "default",
    int rotate = 0,
    int trimStartMillis = 0,
    int trimEndMillis = 0,
    Function(VideoTranscodeEvent)? onProgress,
  }) async {
    loadTranscodeEvent();
    if (onProgress != null) {
      _transcodeEventCallbacks[id] = onProgress;
    }
    await _channel.invokeMethod("processVideo", {
      'id': id,
      'input': inputPath,
      'output': outputPath,
      'rotate': rotate,
      'trimStartMillis': trimStartMillis,
      'trimEndMillis': trimEndMillis,
    });
    _transcodeEventCompleters[id] = new Completer();

    return _transcodeEventCompleters[id]!.future;
  }

  static Future<bool> addToGallery(
    String path, {
    bool isVideo = false,
  }) async {
    var result = await _channel.invokeMethod("addToGallery", {
      'path': path,
      'isVideo': isVideo,
    });

    return result;
  }

  static void loadTranscodeEvent() {
    if (!_transcodeEventLoaded) {
      _transcodeEventChannel.receiveBroadcastStream().listen((data) {
        VideoTranscodeEvent event = VideoTranscodeEvent(
          data['id'],
          data['inputPath'],
          data['outputPath'],
          data['percentage'],
          data['success'],
          data['error'],
          data['errorMessage'],
        );
        if (_transcodeEventCallbacks.containsKey(event.id)) {
          _transcodeEventCallbacks[event.id]!(event);
        }
        if (event.percentage == 100) {
          if (_transcodeEventCompleters.containsKey(event.id)) {
            _transcodeEventCompleters[event.id]!.complete(
              new File(event.outputPath),
            );
            _transcodeEventCompleters.remove(event.id);
          }
        }
      });
      _transcodeEventLoaded = true;
    }
  }

  static Future<MediaInformation> imageInformation(
    String inputPath,
  ) async {
    Map info = await _channel.invokeMethod("imageInformation", {
      'input': inputPath,
    });

    final mi = MediaInformation(
      width: info['width'],
      height: info['height'],
      durationMs: info['durationMs'],
      orientation: info['orientation'],
      mimeType: info['mimeType'],
    );

    return mi;
  }
}

class VideoTranscodeEvent {
  String id;
  String inputPath;
  String outputPath;
  int percentage;
  bool success = false;
  bool error = false;
  String errorMessage = "";

  VideoTranscodeEvent(
    this.id,
    this.inputPath,
    this.outputPath,
    this.percentage,
    this.success,
    this.error,
    this.errorMessage,
  );
}

class MediaInformation {
  final int width;
  final int height;
  final int durationMs;
  final int orientation;
  final String mimeType;

  MediaInformation({
    this.width = 0,
    this.height = 0,
    this.durationMs = 0,
    this.orientation = 0,
    this.mimeType = "",
  });
}
