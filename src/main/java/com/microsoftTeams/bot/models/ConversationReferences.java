package com.microsoftTeams.bot.models;

import com.microsoft.bot.schema.ConversationReference;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "conversationReferences")
@SuppressWarnings("unused")
public class ConversationReferences {

    @Id
    private String id;
    private String email;
    private ConversationReference conversationReference;

    // Constructors, getters, setters
    public ConversationReferences(String email, ConversationReference conversationReference) {
        this.email = email;
        this.conversationReference = conversationReference;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ConversationReference getConversationReference() {
        return conversationReference;
    }

    public void setConversationReference(ConversationReference conversationReference) {
        this.conversationReference = conversationReference;
    }
}

