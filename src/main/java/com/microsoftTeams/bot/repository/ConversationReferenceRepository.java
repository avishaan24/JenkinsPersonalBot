package com.microsoftTeams.bot.repository;


import com.microsoftTeams.bot.models.ConversationReferences;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationReferenceRepository extends MongoRepository<ConversationReferences, String> {
    ConversationReferences findByEmail(String email);
}
