package com.example.hellopush;

import java.util.HashSet;
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

    static HashSet<HellopushUI> uis = new HashSet<HellopushUI>();

    TextField message = new TextField();
    Button button = new Button("Send");

    @Override
    protected void init(VaadinRequest request) {
        buildLayout();

        button.addClickListener(new SendButtonListener());
        addShortcutListener(new ShortcutListener("Enter", KeyCode.ENTER, null) {
            @Override
            public void handleAction(Object sender, Object target) {
                button.click();
            }
        });

        synchronized (uis) {
            uis.add(this);
        }
    }

    @Override
    public void close() {
        super.close();
        synchronized (uis) {
            uis.remove(this);
        }
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

    class SendButtonListener implements ClickListener {
        public void buttonClick(ClickEvent event) {
            final String m = message.getValue();
            message.setValue("");
            message.focus();
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                public void run() {
                    synchronized (uis) {
                        for (HellopushUI ui : uis) {
                            ui.access(new Runnable() {
                                public void run() {
                                    Notification.show(m);
                                }
                            });
                        }
                    }
                }
            });
        }
    }

}