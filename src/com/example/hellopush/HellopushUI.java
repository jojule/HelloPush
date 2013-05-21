package com.example.hellopush;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.vaadin.annotations.Push;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Push
public class HellopushUI extends UI {

    static private final ExecutorService service = Executors
            .newSingleThreadExecutor();
    static private final Set<HellopushUI> chatroom = Collections
            .newSetFromMap(new ConcurrentHashMap<HellopushUI, Boolean>());

    private final TextField message = new TextField();
    private final Button button = new Button("Send message");

    @Override
    protected void init(VaadinRequest request) {
        chatroom.add(this);

        buildLayout();
        wireButton();
    }

    private void wireButton() {
        button.addClickListener(new ClickListener() {
            public void buttonClick(ClickEvent event) {
                service.execute(new SpeakAction(HellopushUI.this, message
                        .getValue()));
                message.setValue("");
                message.focus();
            }
        });
        addShortcutListener(new ShortcutListener("Enter", KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                button.click();
            }
        });
    }

    @Override
    public void detach() {
        super.close();
        chatroom.remove(this);
    }

    private void buildLayout() {
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSizeFull();
        setContent(layout);
        layout.addComponent(message);
        layout.addComponent(button);
        layout.setComponentAlignment(message, Alignment.BOTTOM_CENTER);
        layout.setComponentAlignment(button, Alignment.TOP_CENTER);
        layout.setSpacing(true);
    }

    private class SpeakAction implements Runnable {
        String message;
        HellopushUI speaker;

        SpeakAction(HellopushUI speaker, String message) {
            this.speaker = speaker;
            this.message = message;
        }

        public void run() {
            for (HellopushUI ui : chatroom) {
                if (ui != speaker) {
                    ui.access(new Runnable() {
                        public void run() {
                            Notification.show(message);
                        }
                    });
                }
            }
        }
    }
}