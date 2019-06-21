package com.ideavate.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FaceDetectionActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "FaceDetection==";
    private  ImageView imageview;
    private   Button button;
    private  Button btnLandmarkDetect;
    private Uri fileUri;
    private boolean isFaceDetection = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);
        imageview = findViewById(R.id.imageview);
        button = findViewById(R.id.button);
        btnLandmarkDetect = findViewById(R.id.btnLandmarDetect);
        button.setOnClickListener(this);
        btnLandmarkDetect.setOnClickListener(this);
    }

    void pickImage() {
        ImagePicker.Companion.with(this)
                .crop(1f, 1f)       //Crop Square image(Optional)
                .compress(1024)   //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080) //Final image resolution will be less than 1080 x 1080(Optional)
                .start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            fileUri = data.getData();
            imageview.setImageURI(fileUri);
            //You can get File object from intent
            File file = ImagePicker.Companion.getFile(data);
            String filePath = ImagePicker.Companion.getFilePath(data);
            if (isFaceDetection) {
                DetectFace();
            } else {
                DetectLandmark();
            }

        }
    }

    @Override
    public void onClick(View v) {
        if (v == button) {
            isFaceDetection = true;
            pickImage();
        } else {
            isFaceDetection = false;
            pickImage();
        }
    }


    void DetectFace() {


        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .setMinFaceSize(0.15f)
                        .enableTracking()
                        .build();


        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(this, fileUri);
        } catch (IOException e) {
            e.printStackTrace();
        }


        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        // Task completed successfully
                                        // ...

                                        detectFaces(faces);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Log.d(TAG, "onFailure: Error");
                                    }
                                });

    }

    private void detectFaces(List<FirebaseVisionFace> faces) {
        for (FirebaseVisionFace face : faces) {
            Rect bounds = face.getBoundingBox();
            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees
            log("rotY- " + rotY);
            log("rotZ- " + rotZ);


            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
            // nose available):
            FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
            if (leftEar != null) {
                FirebaseVisionPoint leftEarPos = leftEar.getPosition();
                log("leftEarPos- " + leftEarPos);

            }

            // If contour detection was enabled:
            List<FirebaseVisionPoint> leftEyeContour =
                    face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints();
            List<FirebaseVisionPoint> upperLipBottomContour =
                    face.getContour(FirebaseVisionFaceContour.UPPER_LIP_BOTTOM).getPoints();

            // If classification was enabled:
            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                float smileProb = face.getSmilingProbability();
                log("smileProb- " + smileProb);

            }
            if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                float rightEyeOpenProb = face.getRightEyeOpenProbability();
                log("rightEyeOpenProb- " + rightEyeOpenProb);
            }

            if (face.getLeftEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                float leftEyeOpenProb = face.getLeftEyeOpenProbability();
                log("leftEyeOpenProb- " + leftEyeOpenProb);
            }

            // If face tracking was enabled:
            if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                int id = face.getTrackingId();
                log("Id- " + id);
            }
        }

    }


    void log(String msg) {
        Log.d(TAG, "log: -" + msg);
    }


    void DetectLandmark() {


        FirebaseVisionCloudDetectorOptions options =
                new FirebaseVisionCloudDetectorOptions.Builder()
                        .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                        .setMaxResults(15)
                        .build();


        FirebaseVisionImage image = null;
        try {
            image = FirebaseVisionImage.fromFilePath(this, fileUri);
        } catch (IOException e) {
            e.printStackTrace();
        }


        FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
                .getVisionCloudLandmarkDetector(options);


        Task<List<FirebaseVisionCloudLandmark>> result = detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
                        // Task completed successfully
                        // ...
                        detectLandmarkData(firebaseVisionCloudLandmarks);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                        Log.d(TAG, "onFailure: Error");
                    }
                });


    }

    private void detectLandmarkData(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {


        for (FirebaseVisionCloudLandmark landmark : firebaseVisionCloudLandmarks) {

            Rect bounds = landmark.getBoundingBox();
            String landmarkName = landmark.getLandmark();
            String entityId = landmark.getEntityId();
            float confidence = landmark.getConfidence();
            log("landmarkName- " + landmarkName);
            log("entityId- " + entityId);
            log("confidence- " + confidence);

            // Multiple locations are possible, e.g., the location of the depicted
            // landmark and the location the picture was taken.
            for (FirebaseVisionLatLng loc : landmark.getLocations()) {
                double latitude = loc.getLatitude();
                double longitude = loc.getLongitude();
                log("latitude- " + latitude);
                log("longitude- " + longitude);

            }
        }

    }

}
