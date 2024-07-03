package com.microsoftTeams.bot;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.teams.TeamsInfo;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.schema.*;
import com.microsoftTeams.bot.helpers.UserInfo;
import com.microsoftTeams.bot.models.ConversationReferences;
import com.microsoftTeams.bot.models.LockingComponent;
import com.microsoftTeams.bot.repository.ConversationReferenceRepository;
import com.microsoftTeams.bot.repository.LockingComponentRepository;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.time.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class implements the functionality of the Bot.
 *
 * <p>
 * This is where application specific logic for interacting with the users would
 * be added. In this, the {@link #onMessageActivity(TurnContext)} takes argument for locking components
 * and share locking information to the user and updates the shared
 * {@link ConversationReferences}. The
 * {@link #onMembersAdded(List, TurnContext)} will send a greeting to new
 * conversation participants with instructions for sending a proactive message.
 * </p>
 */

@SuppressWarnings("unused")
public class JenkinsBot extends ActivityHandler {

    // Message to send to users when the bot receives a Conversation Update event
    private final String welcomeMessage =
        "Successfully added, we will notify you for your build related information of Jenkins.\n" + "\nThanks!!";

    private static final Logger logger = LoggerFactory.getLogger(JenkinsBot.class);

    @Autowired
    private Configuration configuration;

    @Autowired
    @SuppressWarnings("assigned")
    private ConversationReferenceRepository conversationReferenceRepository;
    @Autowired
    private LockingComponentRepository lockingComponentRepository;


    public JenkinsBot() {
    }

    /**
     * override the onMessageActivity, taking commands related to the user and share information related to the locking
     * @param turnContext events occur in one turn of the message
     */
    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {

        final String notify = "Please use the available commands: \n" +
                "\n1.add <componentName> (to add some component in the library) \n" +
                "\n2.lock <componentName> (to lock available components) \n" +
                "\n3.unlock <componentName> (to unlock components which are locked by you) \n" +
                "\n4.list (to see the list of available components)";

        // save conversation reference for further proactive messaging
        addConversationReference(turnContext);
        String[] text = turnContext.getActivity().getText().toLowerCase().split(" ");
        List<String> words = Arrays.asList(text);

        if(words.size() == 2){
            // if the command contains add as the first argument then first check the list if it is already present then notify user or create that component
            switch (words.get(0)) {
                case "add": {
                    // finding index of the component
                    LockingComponent lockingComponent = lockingComponentRepository.findByComponent(words.get(1));

                    // if it is already present then notify user about this
                    if (lockingComponent != null) {
                        return turnContext
                                .sendActivity(MessageFactory.text(String.format("'%s' already present in the library", words.get(1))))
                                .thenApply(sendResult -> null);
                    }
                    // else create one and add in the components list
                    else {
                        LockingComponent lockingComponent1 = new LockingComponent(words.get(1));
                        lockingComponentRepository.save(lockingComponent1);
                        return turnContext
                                .sendActivity(MessageFactory.text(String.format("'%s' added successfully", words.get(1))))
                                .thenApply(sendResult -> null);
                    }
                }
                // if command contains lock as the first argument then first check the status of component and if available then lock the component
                case "lock": {
                    // find the index of the component
                    LockingComponent lockingComponent = lockingComponentRepository.findByComponent(words.get(1));
                    // if the component is not present in the list
                    if (lockingComponent == null) {
                        return turnContext
                                .sendActivity(MessageFactory.text("'%s' not found in the components."))
                                .thenApply(sendResult -> null);
                    } else {
                        // fetching the userInfo of the user who previously locked the same component
                        UserInfo userInfo = lockingComponent.getUserInfo();
                        // if it is null then locked the component with this user
                        if (userInfo == null) {
                            lockingComponent.setUserInfo(new UserInfo(turnContext.getActivity().getConversationReference().getUser().getId(), turnContext.getActivity().getConversationReference().getUser().getName(), ZonedDateTime.now(ZoneId.of("UTC"))));
                            lockingComponentRepository.save(lockingComponent);
                            return turnContext
                                    .sendActivity(MessageFactory.text(String.format("'%s' locked successfully for next 4 hours.", words.get(1))))
                                    .thenApply(sendResult -> null);
                        } else {
                            // if it is locked by some user previously then calculate the time difference
                            long minuteDifference = ChronoUnit.MINUTES.between(userInfo.getTime(), ZonedDateTime.now(ZoneId.of("UTC")));

                            // if it is greater than 240 minutes then unlock from that user and lock
                            if (minuteDifference >= 240) {
                                lockingComponent.setUserInfo(new UserInfo(turnContext.getActivity().getConversationReference().getUser().getId(), turnContext.getActivity().getConversationReference().getUser().getName(), ZonedDateTime.now(ZoneId.of("UTC"))));
                                lockingComponentRepository.save(lockingComponent);
                                return turnContext
                                        .sendActivity(MessageFactory.text(String.format("'%s' locked successfully for next 4 hours.", words.get(1))))
                                        .thenApply(sendResult -> null);
                            }
                            // if it is less than 240 minutes then notify user to wait
                            else {
                                long remain = 240 - minuteDifference;
                                if (userInfo.getUserId().equals(turnContext.getActivity().getConversationReference().getUser().getId())) {
                                    return turnContext
                                            .sendActivity(MessageFactory.text(String.format("'%s' is already locked by you.\n \n It will be available after " + remain + " minutes for all.", words.get(1))))
                                            .thenApply(sendResult -> null);
                                }
                                return turnContext
                                        .sendActivity(MessageFactory.text(String.format("'%s' will be available after " + remain + " minutes.", words.get(1))))
                                        .thenApply(sendResult -> null);
                            }
                        }
                    }
                }
                // if command is unlock then check first that the component is present and if present then check it is locked by same user or not and then notify accordingly
                case "unlock": {
                    // find component index
                    LockingComponent lockingComponent = lockingComponentRepository.findByComponent(words.get(1));

                    // if component not found
                    if (lockingComponent == null) {
                        return turnContext
                                .sendActivity(MessageFactory.text("'%s' cannot found in the components."))
                                .thenApply(sendResult -> null);
                    } else {
                        // fetching the userInfo of the user who previously locked the same component
                        UserInfo userInfo = lockingComponent.getUserInfo();

                        // if it is available
                        if (userInfo == null) {
                            return turnContext
                                    .sendActivity(MessageFactory.text(String.format("'%s' is available.", words.get(1))))
                                    .thenApply(sendResult -> null);
                        } else {
                            // if it is locked by some user previously then calculate the time difference
                            long minuteDifference = ChronoUnit.MINUTES.between(userInfo.getTime(), ZonedDateTime.now(ZoneId.of("UTC")));

                            // if greater than 240 then unlock that component
                            if (minuteDifference >= 240) {
                                lockingComponent.setUserInfo(null);
                                lockingComponentRepository.save(lockingComponent);
                                return turnContext
                                        .sendActivity(MessageFactory.text(String.format("'%s' is available.", words.get(1))))
                                        .thenApply(sendResult -> null);
                            }
                            // if same user is trying to unlock
                            else if (userInfo.getUserId().equals(turnContext.getActivity().getConversationReference().getUser().getId())) {
                                lockingComponent.setUserInfo(null);
                                lockingComponentRepository.save(lockingComponent);
                                return turnContext
                                        .sendActivity(MessageFactory.text(String.format("'%s' unlocked successfully", words.get(1))))
                                        .thenApply(sendResult -> null);
                            }
                            // if different user is trying to unlock
                            else {
                                return turnContext
                                        .sendActivity(MessageFactory.text(String.format("You are not allowed to unlock '%s'. \n" + "\nLocked by " + userInfo.getUserName(), words.get(1))))
                                        .thenApply(sendResult -> null);
                            }
                        }
                    }
                }
            }
        }
        // if user wants to know the list of components
        else if(words.size() == 1 && words.get(0).equals("list")){
            return turnContext
                    .sendActivity(MessageFactory.text("List of available components: \n" + getString(lockingComponentRepository.findAllComponentStrings())))
                    .thenApply(sendResult -> null);
        }
        // notify to use the available command
        return turnContext
                .sendActivity(MessageFactory.text(notify))
                .thenApply(sendResult -> null);
    }

    /**
     * create available components list as string
     * @param component components present in the database
     * @return formatted String
     */
    private String getString(List<String> component){
        if(component.isEmpty()){
            return "\nComponents list is empty (add some component using the specified command)";
        }
        StringBuilder sb = new StringBuilder();
        ObjectMapper mapper = new ObjectMapper();
        for (int i = 0; i < component.size(); i++) {
            try{
                JsonNode jsonNode = mapper.readTree(component.get(i));
                String value = jsonNode.get("component").asText();
                sb.append((i + 1)).append(". ").append(value).append("\n");
            }catch (JsonProcessingException e) {
                // Handle JSON parsing exception
                logger.error("A JsonProcessingException occurred : {}", e.getMessage(), e);
            }
        }
        return sb.toString();
    }

    /**
     * override onMembersAdded method and greet member
     * @param membersAdded added members list
     * @param turnContext turnContext of the conversation
     */
    @Override
    protected CompletableFuture<Void> onMembersAdded(
        List<ChannelAccount> membersAdded,
        TurnContext turnContext
    ) {
        return membersAdded.stream()
            .filter(
                // Greet anyone that was not the target (recipient) of this message.
                member -> !StringUtils
                    .equals(member.getId(), turnContext.getActivity().getRecipient().getId())
            )
            .map(
                channel -> turnContext
                    .sendActivity(MessageFactory.text(String.format(welcomeMessage)))
            )
            .collect(CompletableFutures.toFutureList())
            .thenApply(resourceResponses -> null);
    }

    @Override
    protected CompletableFuture<Void> onConversationUpdateActivity(TurnContext turnContext) {
        addConversationReference(turnContext);
        return super.onConversationUpdateActivity(turnContext);
    }

    // adds a ConversationReference to the shared Map.
    private void addConversationReference(TurnContext turnContext) {
        ConversationReference conversationReference = turnContext.getActivity().getConversationReference();
        AtomicReference<String> email = new AtomicReference<>();
        TeamsInfo.getMember(turnContext, turnContext.getActivity().getFrom().getId())
                .thenApply(member -> {
                    email.set(member.getEmail());
                    return null;
                }).join();


        ConversationReferences conversationReferenceDb = conversationReferenceRepository.findByEmail(email.get());
        if(conversationReferenceDb == null){
            ConversationReferences conversationReferencesAdd = new ConversationReferences(email.get(), conversationReference);
            conversationReferenceRepository.save(conversationReferencesAdd);
        }
        else{
            conversationReferenceDb.setConversationReference(conversationReference);
            conversationReferenceRepository.save(conversationReferenceDb);
        }

    }
}