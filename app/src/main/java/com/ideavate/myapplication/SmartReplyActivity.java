package com.ideavate.myapplication;

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
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult;

import java.util.ArrayList;
import java.util.List;

public class SmartReplyActivity extends AppCompatActivity {
    private List<FirebaseTextMessage> conversation = new ArrayList<>();
    private String userIdOfSender = "12";
    private EditText editText;
    private TextView textview;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        textview = findViewById(R.id.textview);
        button = findViewById(R.id.button);
        editText = findViewById(R.id.editText);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                log("Button clicked!");
                conversation.add(FirebaseTextMessage.createForRemoteUser(
                        editText.getText().toString(), System.currentTimeMillis(), userIdOfSender));
                replyListener();
            }
        });
        setupSmartReply();
    }

    private void setupSmartReply() {

        conversation.add(FirebaseTextMessage.createForLocalUser(
                "heading out now", System.currentTimeMillis()));

        conversation.add(FirebaseTextMessage.createForRemoteUser(
                "Are you coming back soon?", System.currentTimeMillis(), userIdOfSender));


        replyListener();
    }

    private void replyListener() {
        FirebaseSmartReply smartReply = FirebaseNaturalLanguage.getInstance().getSmartReply();

        smartReply.suggestReplies(conversation)
                .addOnSuccessListener(new OnSuccessListener<SmartReplySuggestionResult>() {
                    @Override
                    public void onSuccess(SmartReplySuggestionResult result) {
                        if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                            // The conversation's language isn't supported, so the
                            // the result doesn't contain any suggestions.
                            log("==Error Language not supported==");
                        } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                            // Task completed successfully
                            // ...
                            List<SmartReplySuggestion> msgs = result.getSuggestions();
                            String lastMessage = "";
                            String lastMessage1 = "";
                            for (SmartReplySuggestion suggestion : msgs) {

                                lastMessage = lastMessage + "\n " + suggestion.getText();
                                lastMessage1 = suggestion.getText();

                            }
                            log(" Suggestion - " + lastMessage);
                            textview.setText(lastMessage);
                            conversation.add(FirebaseTextMessage.createForLocalUser(
                                    lastMessage1, System.currentTimeMillis()));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                        log("==Error on failure== " + e.toString());

                    }
                });
    }

    private void log(String msg) {
        Log.d("FIREBASE==== ", msg);
    }
}
