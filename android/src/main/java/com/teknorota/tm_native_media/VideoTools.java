package com.teknorota.tm_native_media;

import android.content.Context;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.linkedin.android.litr.MediaTransformer;
import com.linkedin.android.litr.TransformationListener;
import com.linkedin.android.litr.TransformationOptions;
import com.linkedin.android.litr.analytics.TrackTransformationInfo;
import com.linkedin.android.litr.io.MediaRange;

import java.io.File;
import java.util.List;


public class VideoTools {
    public static MediaInformation getVideoInformation(String inputPath) {
        MediaInformation info = new MediaInformation();

        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(inputPath);
            info.width = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            info.height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            info.durationMs = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            info.orientation = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
            info.mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            retriever.release();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return info;
    }

    public static void processVideo(String id, final String inputPath, final String outputPath, int startMillis, int endMillis, int rotation, Context context) {
        MediaTransformer mediaTransformer = new MediaTransformer(context);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(inputPath);
        int inputWidth = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        int inputHeight = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        retriever.release();

        int shortSide = Math.min(inputWidth, inputHeight);

        if (shortSide > 480) {
            inputWidth = inputWidth / 2;
            inputHeight = inputHeight / 2;
        }

        MediaFormat mediaFormat = MediaFormat.createVideoFormat(
                MediaFormat.MIMETYPE_VIDEO_AVC,
                inputWidth,
                inputHeight
        );

        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, getBitRate(inputWidth, inputHeight, MediaTransformer.DEFAULT_KEY_FRAME_INTERVAL));
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 24);

        TransformationOptions.Builder transformationOptionsBuilder = new TransformationOptions.Builder()
                .setGranularity(MediaTransformer.GRANULARITY_DEFAULT);

        if(endMillis != 0) {
            MediaRange mediaRange = new MediaRange(startMillis * 1000, endMillis * 1000);
            transformationOptionsBuilder.setSourceMediaRange(mediaRange);
        }

                // .setVideoFilters(watermarkImageFilter)
                // .setSourceMediaRange(mediaRange)

        mediaTransformer.transform(
                id,
                Uri.fromFile(new File(inputPath)),
                outputPath,
                mediaFormat,
                null,
                new TransformationListener() {
                    @Override
                    public void onStarted(@NonNull String id) {
                        VideoTranscodeEvent event = new VideoTranscodeEvent();
                        event.id = id;
                        event.inputPath = inputPath;
                        event.outputPath = outputPath;
                        event.percentage = 0;
                        TMNativeMedia.sendTranscodeEvent(event);
                    }

                    @Override
                    public void onProgress(@NonNull String id, float progress) {
                        int percentage = (int)Math.round(progress * 100);

                        VideoTranscodeEvent event = new VideoTranscodeEvent();
                        event.id = id;
                        event.inputPath = inputPath;
                        event.outputPath = outputPath;
                        event.percentage = percentage;
                        TMNativeMedia.sendTranscodeEvent(event);
                    }

                    @Override
                    public void onCompleted(@NonNull String id, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {
                        VideoTranscodeEvent event = new VideoTranscodeEvent();
                        event.id = id;
                        event.inputPath = inputPath;
                        event.outputPath = outputPath;
                        event.percentage = 100;
                        event.success = true;
                        TMNativeMedia.sendTranscodeEvent(event);
                    }

                    @Override
                    public void onCancelled(@NonNull String id, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {
                        VideoTranscodeEvent event = new VideoTranscodeEvent();
                        event.id = id;
                        event.inputPath = inputPath;
                        event.outputPath = outputPath;
                        event.percentage = 0;
                        event.success = false;
                        event.error = true;
                        event.errorMessage = "Canceled by user.";
                        TMNativeMedia.sendTranscodeEvent(event);
                    }

                    @Override
                    public void onError(@NonNull String id, @Nullable Throwable cause, @Nullable List<TrackTransformationInfo> trackTransformationInfos) {
                        VideoTranscodeEvent event = new VideoTranscodeEvent();
                        event.id = id;
                        event.inputPath = inputPath;
                        event.outputPath = outputPath;
                        event.percentage = 0;
                        event.success = false;
                        event.error = true;
                        event.errorMessage = cause.getMessage();
                        TMNativeMedia.sendTranscodeEvent(event);
                    }
                },
                transformationOptionsBuilder.build()
        );
    }

    public static int getBitRate(int width, int height, int keyFrameInterval) {
        int shortSide = Math.min(width, height);
        if(shortSide < 241) {
            return 300000;
        } else if(shortSide < 361) {
            return 560000;
        } else if (shortSide < 481) {
            return 1000000;
        } else {
            return 1500000;
        }
    }
}
