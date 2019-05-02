package com.example.lcomlkitauto;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.example.lcomlkitauto.model.Message;
import com.google.android.gms.tasks.*;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MessageViewModel extends AndroidViewModel {

    private final String remoteUserId = UUID.randomUUID().toString();

    private MutableLiveData<List<Message>> messages = new MutableLiveData<>();
    private MutableLiveData<Boolean> simulateRemoteUser = new MutableLiveData<>();
    private MediatorLiveData<List<SmartReplySuggestion>> smartReplies = new MediatorLiveData<>();

    public MessageViewModel(@NonNull Application application) {
        super(application);

        initSmartRepliesSources();
        simulateRemoteUser.postValue(false);
        addMessage("Hello");
    }

    public MutableLiveData<List<Message>> getMessages() {
        return messages;
    }

    public MutableLiveData<List<SmartReplySuggestion>> getSmartReplies() {
        return smartReplies;
    }

    public MutableLiveData<Boolean> getSimulateRemoteUser() {
        return simulateRemoteUser;
    }

    public void addMessage(String message) {
        List<Message> list = messages.getValue();
        if (list == null) {
            list = new ArrayList<>();
        }

        boolean isLocalUser = simulateRemoteUser.getValue() != null && !simulateRemoteUser.getValue();
        list.add(new Message(message, System.currentTimeMillis(), isLocalUser));
        clearSuggestions();
        messages.postValue(list);
    }

    public void clearChatHistory() {
        messages.postValue(new ArrayList<Message>());
    }

    private void clearSuggestions() {
        smartReplies.postValue(new ArrayList<SmartReplySuggestion>());
    }

    private void initSmartRepliesSources() {
        smartReplies.addSource(simulateRemoteUser, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEmulatingRemoteUser) {
                List<Message> list = messages.getValue();
                if (list == null || list.isEmpty()) {
                    return;
                }

                generateReplies(list, isEmulatingRemoteUser);
            }
        });

        smartReplies.addSource(messages, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> list) {
                Boolean isEmulatingRemoteUser = simulateRemoteUser.getValue();
                if (isEmulatingRemoteUser == null) {
                    return;
                }

                if (list.isEmpty()) {
                    smartReplies.postValue(new ArrayList<SmartReplySuggestion>());
                    return;
                }

                generateReplies(list, isEmulatingRemoteUser);
            }
        });
    }

    private void generateReplies(List<Message> messages, boolean isEmulatingRemoteUser) {
        Message lastMessage = messages.get(messages.size() - 1);

        // Only generate smart replies when the last message has been sent by the other user
        if (lastMessage.isLocalUser() && !isEmulatingRemoteUser || !lastMessage.isLocalUser() && isEmulatingRemoteUser) {
            return;
        }

        List<FirebaseTextMessage> chatHistory = new ArrayList<>();
        for (Message message : messages) {
            if (message.isLocalUser() && !isEmulatingRemoteUser || !message.isLocalUser() && isEmulatingRemoteUser) {
                chatHistory.add(FirebaseTextMessage.createForLocalUser(message.getContent(),
                        message.getTimestamp()));
            } else {
                chatHistory.add(FirebaseTextMessage.createForRemoteUser(message.getContent(),
                        message.getTimestamp(), remoteUserId));
            }
        }

        FirebaseNaturalLanguage.getInstance().getSmartReply().suggestReplies(chatHistory)
                .addOnSuccessListener(new OnSuccessListener<SmartReplySuggestionResult>() {
                    @Override
                    public void onSuccess(SmartReplySuggestionResult result) {
                        if (result.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                            // The conversation's language isn't supported, so the
                            // the result doesn't contain any suggestions.
                        } else if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                            // Task completed successfully
                            smartReplies.postValue(result.getSuggestions());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                    }
                });
    }

    /**
     * Swap users in order to answer for the remote user.
     */
    public void swapUsers() {
        clearSuggestions();
        simulateRemoteUser.postValue(!simulateRemoteUser.getValue());
    }
}
