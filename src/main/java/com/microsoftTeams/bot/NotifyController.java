package com.microsoftTeams.bot;

import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.integration.Configuration;
import com.microsoft.bot.schema.*;
import com.microsoftTeams.bot.models.BuildInfo;
import com.microsoftTeams.bot.models.ConversationReferences;
import com.microsoftTeams.bot.repository.BuildInfoRepository;
import com.microsoftTeams.bot.repository.ConversationReferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.microsoft.bot.integration.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * This controller will receive POST requests at /api/buildIndo and send a message
 * to users using  ConversationReferences.
 *
 * @see JenkinsBot
 * @see Application
 */
@RestController
@SuppressWarnings("unused")
public class NotifyController {
    /**
     * The BotFrameworkHttpAdapter to use. Note it is provided by dependency
     * injection via the constructor.
     *
     * @see com.microsoft.bot.integration.spring.BotDependencyConfiguration
     */
    private final BotFrameworkHttpAdapter adapter;
    @Autowired
    private ConversationReferenceRepository conversationReferenceRepository;

    @Autowired
    private BuildInfoRepository buildInfoRepository;
    private final String appId;

    @Autowired
    public NotifyController(
        BotFrameworkHttpAdapter withAdapter,
        Environment environment,
        Configuration configuration
    ) {
        adapter = withAdapter;
        appId = configuration.getProperty("MicrosoftAppId");
    }

    /**
     * sending build related notification from Jenkins to MS Teams
     */
    @PostMapping("/api/buildInfo")
    public CompletableFuture<Void> sendNotification(@RequestBody  BuildInfo buildInfo){
        BuildInfo build = buildInfoRepository.findByBuildJobNameAndBuildNumber(buildInfo.getBuildJobName(), buildInfo.getBuildNumber());
        if(build == null){
            buildInfoRepository.save(buildInfo);
            Long timeFrame = (long) (2 * 60 * 1000);
            Long threshold = buildInfo.getBuildStartTime() - timeFrame;
            List<BuildInfo> builds = buildInfoRepository.findByBuildJobNameAndBuildStartTimeGreaterThanOrBuildEndTimeGreaterThan(buildInfo.getBuildJobName(), threshold, threshold);
            for(BuildInfo buildInformation: builds){
                if(buildInformation.getBuildNumber() == buildInfo.getBuildNumber() || buildInformation.getBuildUserEmail().equals(buildInfo.getBuildUserEmail())){
                    continue;
                }
                if(compareParameters(buildInformation.getParameters(), buildInfo.getParameters())){
                    if(buildInformation.getBuildStatus() == null || buildInformation.getBuildStatus().equals("SUCCESS")){
                        ConversationReferences conversationReferences = conversationReferenceRepository.findByEmail(buildInformation.getBuildUserEmail());
                        HeroCard heroCard = getHeroCardOverrideNotification(buildInfo, buildInformation);

                        // send notification only to the first user for testing purpose
                        if(conversationReferences != null) {
                            ConversationReference reference = conversationReferences.getConversationReference();
                            adapter.continueConversation(
                                    appId, reference, turnContext -> turnContext.sendActivity(MessageFactory.attachment(heroCard.toAttachment())).thenApply(resourceResponse -> null)
                            );
                        }
                    }
                }
            }
        }
        else{
            build.setBuildStatus(buildInfo.getBuildStatus());
            build.setBuildEndTime(buildInfo.getBuildEndTime());
            buildInfoRepository.save(build);
            HeroCard heroCard = getHeroCardBuildStatus(buildInfo.getBuildStatus(), build);

            ConversationReferences conversationReferences = conversationReferenceRepository.findByEmail(build.getBuildUserEmail());

            if(conversationReferences != null) {
                ConversationReference reference = conversationReferences.getConversationReference();
                adapter.continueConversation(
                        appId, reference, turnContext -> turnContext.sendActivity(MessageFactory.attachment(heroCard.toAttachment())).thenApply(resourceResponse -> null)
                );
            }
        }
        return null;
    }

    private static HeroCard getHeroCardOverrideNotification(BuildInfo buildInfo, BuildInfo buildInformation) {
        HeroCard heroCard = new HeroCard();
        heroCard.setTitle("Overridden Notification");
        heroCard.setSubtitle("Hii, " + buildInformation.getBuildUser());
        heroCard.setText("Your Jenkins build on job " + buildInformation.getBuildJobName().toUpperCase() + ", labeled as number " + buildInformation.getBuildNumber() + ", was override by " + buildInfo.getBuildUser());
        heroCard.setButtons(new CardAction(ActionTypes.OPEN_URL, "View Override Build", buildInfo.getBuildUrl()));
        return heroCard;
    }

    private HeroCard getHeroCardBuildStatus(String status, BuildInfo build) {
        HeroCard heroCard = new HeroCard();
        switch (status) {
            case "SUCCESS":
                heroCard.setTitle("Build Success");
                break;
            case "FAILURE":
                heroCard.setTitle("Build Failed");
                break;
            case "UNSTABLE":
                heroCard.setTitle("Build Unstable");
                break;
        }
        heroCard.setSubtitle("Hii, " + build.getBuildUser());
        heroCard.setText("Your Jenkins build on job " + build.getBuildJobName().toUpperCase() + ", labeled as number " + build.getBuildNumber() + ", has achieved " + status);
        heroCard.setButtons(new CardAction(ActionTypes.OPEN_URL, "View Build", build.getBuildUrl()));
        return heroCard;
    }

    private boolean compareParameters(Map<String, String> parameter1, Map<String, String> parameter2) {
        // Check if maps have the same size
        if (parameter1.size() != parameter2.size()) {
            return false;
        }
        // Iterate through parameter1 and compare key-value pairs with parameter2
        for (Map.Entry<String, String> entry : parameter1.entrySet()) {
            if(!entry.getValue().equals(parameter2.get(entry.getKey()))){
                return false;
            }
        }
        return true;
    }
}
