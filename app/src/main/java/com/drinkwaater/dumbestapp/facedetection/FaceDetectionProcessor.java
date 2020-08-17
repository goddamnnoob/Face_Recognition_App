// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.drinkwaater.dumbestapp.facedetection;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.drinkwaater.dumbestapp.R;
import com.drinkwaater.dumbestapp.CameraImageGraphic;
import com.drinkwaater.dumbestapp.FrameMetadata;
import com.drinkwaater.dumbestapp.GraphicOverlay;
import com.drinkwaater.dumbestapp.VisionProcessorBase;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FaceDetectionProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

    private static final String TAG = "FaceDetectionProcessor";
    Context context;
    private final FirebaseVisionFaceDetector detector;
    public int n = 0;
    MediaPlayer mp = new MediaPlayer();

    private final Bitmap overlayBitmap;

    public FaceDetectionProcessor(Resources resources,Context context) {
        this.context = context;
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .build();

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
        overlayBitmap = BitmapFactory.decodeResource(resources, R.drawable.nose);
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
        for (int i = 0; i < faces.size(); ++i) {
            FirebaseVisionFace face = faces.get(i);
            if(n==0){
                ++n;
                if(face != null){
                    AudioPlaying();
                }
            }
            int cameraFacing =
                    frameMetadata != null ? frameMetadata.getCameraFacing() :
                            Camera.CameraInfo.CAMERA_FACING_BACK;
            com.drinkwaater.dumbestapp.facedetection.FaceGraphic faceGraphic = new com.drinkwaater.dumbestapp.facedetection.FaceGraphic(graphicOverlay, face, cameraFacing, overlayBitmap,context);
            graphicOverlay.add(faceGraphic);
        }
        graphicOverlay.postInvalidate();

    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }
    private void AudioPlaying(){
        List<Integer> tracks = new ArrayList<>();
        tracks.add(R.raw.audio1);
        tracks.add(R.raw.audio2);
        tracks.add(R.raw.audio3);
        tracks.add(R.raw.audio4);
        tracks.add(R.raw.swearingaudio5);
        int option = (int) (Math.random() * 4 + 0);
        if (option < 5) {
            mp = MediaPlayer.create(context, tracks.get(option));
        }
        try {
            mp.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        if (mp.isPlaying()) {
            //do nothing
        } else {
            Log.d(TAG,String.valueOf(n));
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mp.stop();
                    mp.release();
                    n = 0;
                }
            });
        }
    }

}
