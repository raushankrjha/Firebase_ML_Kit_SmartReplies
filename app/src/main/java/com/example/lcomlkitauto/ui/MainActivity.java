package com.example.lcomlkitauto.ui;

import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lcomlkitauto.MessageViewModel;
import com.example.lcomlkitauto.R;
import com.example.lcomlkitauto.model.Message;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MessageAdapter messageAdapter;
    private MessageViewModel messageViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final RecyclerView messagesRecyclerView = findViewById(R.id.messages);
        ImageButton sendMessageButton = findViewById(R.id.send_message);
        final AppCompatEditText inputEditText = findViewById(R.id.message_input);
        final ChipGroup smartReplyChipGroup = findViewById(R.id.smart_replies);

        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        messageViewModel = ViewModelProviders.of(this).get(MessageViewModel.class);
        messageAdapter = new MessageAdapter();
        messagesRecyclerView.setAdapter(messageAdapter);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Editable editable = inputEditText.getText();
                final String message = editable != null ? editable.toString() : "";
                inputEditText.setText("");
                messageViewModel.addMessage(message);
            }
        });

        messageViewModel.getMessages().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                messageAdapter.setMessages(messages);

                if (messageAdapter.getItemCount() > 0) {
                    messagesRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                }
            }
        });

        messageViewModel.getSimulateRemoteUser().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean simulateRemoteUser) {
                setUpActionBarTitle(simulateRemoteUser);
            }
        });

        messageViewModel.getSmartReplies().observe(this, new Observer<List<SmartReplySuggestion>>() {
            @Override
            public void onChanged(List<SmartReplySuggestion> smartReplySuggestions) {
                smartReplyChipGroup.removeAllViews();

                for (final SmartReplySuggestion smartReplySuggestion : smartReplySuggestions) {
                    Chip dynamicChip = createSmartReplyChip(smartReplySuggestion.getText(), new SmartReplyClickListener() {
                        @Override
                        public void onSmartReplyClick(@NonNull String smartReply) {
                            messageViewModel.addMessage(smartReply);
                        }
                    });

                    smartReplyChipGroup.addView(dynamicChip);
                }

                if (smartReplySuggestions.isEmpty()) {
                    smartReplyChipGroup.setVisibility(View.GONE);
                } else {
                    smartReplyChipGroup.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private Chip createSmartReplyChip(final String smartReplySuggestion,
                                      @NonNull final SmartReplyClickListener clickListener) {
        Chip dynamicChip = new Chip(MainActivity.this);
        dynamicChip.setText(smartReplySuggestion);
        dynamicChip.setChipStrokeColorResource(R.color.chip_stroke_color);
        dynamicChip.setChipStrokeWidth(getResources().getDimensionPixelSize(R.dimen.chip_stroke_width));
        dynamicChip.setChipBackgroundColorResource(R.color.white);
        dynamicChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onSmartReplyClick(smartReplySuggestion);
            }
        });

        return dynamicChip;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_swap_users) {
            swapUsers();
        } else if (item.getItemId() == R.id.action_clear_chat_history) {
            messageViewModel.clearChatHistory();
        }

        return super.onOptionsItemSelected(item);
    }

    private void swapUsers() {
        messageAdapter.setSimulateRemoteUser(!messageAdapter.getSimulateRemoteUser());
        messageViewModel.swapUsers();
    }

    private void setUpActionBarTitle(boolean simulateRemoteUser) {
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            if (simulateRemoteUser) {
                actionBar.setTitle("Hitesh");
            } else {
                actionBar.setTitle("Saksham");
            }
        }
    }

    public interface SmartReplyClickListener {
        void onSmartReplyClick(@NonNull String smartReply);
    }
}
