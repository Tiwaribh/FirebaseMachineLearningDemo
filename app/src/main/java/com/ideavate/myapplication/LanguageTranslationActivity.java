package com.ideavate.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

public class LanguageTranslationActivity extends AppCompatActivity {


    private TextView tv;
    private Button btnTranslate;
    private Button btnMove;
    private Button btnFaceDetection;
    private EditText etSource;
    private FirebaseTranslator englishHindiTranslator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lanuage_translation);

        tv = findViewById(R.id.tv);
        btnFaceDetection = findViewById(R.id.btnFaceDetection);
        btnTranslate = findViewById(R.id.btnTranslate);
        btnMove = findViewById(R.id.btnMove);
        etSource = findViewById(R.id.etSource);
        btnTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doTranslation();
            }
        });
        btnMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LanguageTranslationActivity.this, SmartReplyActivity.class));
            }
        });
        btnFaceDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LanguageTranslationActivity.this, FaceDetectionActivity.class));
            }
        });
        createTranslator();
    }

    private void createTranslator() {
        // Create an English-Hindi translator:
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(FirebaseTranslateLanguage.EN)
                        .setTargetLanguage(FirebaseTranslateLanguage.HI)
                        .build();
        englishHindiTranslator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        downloadTranslatorModel();
    }


    void downloadTranslatorModel() {
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        englishHindiTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                // Model downloaded successfully. Okay to start translating.
                                // (Set a flag, unhide the translation UI, etc.)
                                log("Downloaded the model for translation..");
                                doTranslation();
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Model could’t be downloaded or other internal error.
                                // ...
                                log(" error model not download");
                            }
                        });
    }

    private void doTranslation() {
        log("Do translate called!");

        englishHindiTranslator.translate(etSource.getText().toString())
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                // Translation successful.
                                tv.setText(translatedText);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Error.
                                // ...
                                log(" error while translation..!!!");
                            }
                        });
    }


    private void log(String msg) {
        Log.d("FIREBASE Translate=== ", msg);
    }
}
