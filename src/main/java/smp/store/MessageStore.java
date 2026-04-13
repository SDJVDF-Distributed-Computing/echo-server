package smp.store;

import smp.model.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MessageStore {
    private final List<Message> messages = new ArrayList<>();

    public synchronized void addMessage(UUID sender, String text) {
        Message entry = new Message(sender, text);
        messages.add(entry);
    }

    public synchronized List<Message> getAllMessages() {
        return Collections.unmodifiableList(new ArrayList<>(messages));
    }

    public synchronized int size() {
        return messages.size();
    }
}
