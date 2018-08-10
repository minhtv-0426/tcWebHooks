package webhook.teamcity.testing;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.Loggers;
import webhook.teamcity.WebHookListener;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.settings.CustomMessageTemplate;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookFilterConfig;
import webhook.teamcity.settings.WebHookHeaderConfig;
import webhook.teamcity.settings.WebHookProjectSettings;
import webhook.teamcity.testing.model.WebHookExecutionRequest;
import webhook.teamcity.testing.model.WebHookTemplateExecutionRequest;

public class WebHookConfigFactoryImpl implements WebHookConfigFactory {
	
	private final SBuildServer myServer;
	private final ProjectSettingsManager myProjectSettingsManager;
	private final WebHookPayloadManager myWebHookPayloadManager;
	
	public WebHookConfigFactoryImpl(
			SBuildServer sBuildServer,
			ProjectSettingsManager projectSettingsManager,
			WebHookPayloadManager webHookPayloadManager
			
			) {
		myServer = sBuildServer;
		myProjectSettingsManager = projectSettingsManager;
		myWebHookPayloadManager = webHookPayloadManager;
	}
	
	@Override
	public WebHookConfig build(WebHookExecutionRequest webHookExecutionRequest) {
		return WebHookConfig.builder()
					 .url(webHookExecutionRequest.getUrl())
					 .payloadFormat(webHookExecutionRequest.getPayloadFormat())
					 .payloadTemplate(webHookExecutionRequest.getTemplateId())
					 .templates(new TreeMap<String,CustomMessageTemplate>())
					 .authEnabled(webHookExecutionRequest.isAuthEnabled())
					 .authType(webHookExecutionRequest.getAuthType())
					 .authParameters(webHookExecutionRequest.getAuthParameters())
					 .filters(new ArrayList<WebHookFilterConfig>())
					 .states(buildStates(webHookExecutionRequest.getConfigBuildStates()))
					 .extraParameters(new TreeMap<String,String>()) //TODO: Should we get from config somehow?
					 .build();
	}

	private BuildState buildStates(Map<BuildStateEnum, Boolean> configBuildStates) {
		BuildState buildState = new BuildState();
		for (Map.Entry<BuildStateEnum, Boolean> entry : configBuildStates.entrySet()) {
			buildState.setEnabled(entry.getKey(), entry.getValue());
		}
		return buildState;
	}

	@Override
	public WebHookConfig build(WebHookTemplateExecutionRequest webHookExecutionRequest) throws WebHookConfigNotFoundException {
		return findWebHookWithId(webHookExecutionRequest.getProjectExternalId(), webHookExecutionRequest.getUniqueKey()).copy();
	}

	private WebHookConfig findWebHookWithId(String projectId, String webHookConfigUniqueId) throws WebHookConfigNotFoundException {
		SProject myProject = myServer.getProjectManager().findProjectByExternalId(projectId);
		for (SProject project : myProject.getProjectPath()){
			WebHookProjectSettings projSettings = (WebHookProjectSettings) myProjectSettingsManager.getSettings(project.getProjectId(), WebHookListener.WEBHOOKS_SETTINGS_ATTRIBUTE_NAME);
		    	if (projSettings.isEnabled()){
			    	for (WebHookConfig whc : projSettings.getWebHooksConfigs()){
			    		if (whc.isEnabledForSubProjects() == false && !myProject.getProjectId().equals(project.getProjectId())){
			    			// Sub-projects are disabled and we are a subproject.
			    			if (Loggers.SERVER.isDebugEnabled()){
				    			Loggers.SERVER.debug(this.getClass().getSimpleName() + ":getListOfEnabledWebHooks() "
				    					+ ":: subprojects not enabled. myProject is: " + myProject.getProjectId() + ". webhook project is: " + project.getProjectId());
			    			}
			    			continue;
			    		}
			    		
			    		if (whc.getUniqueKey().equals(webHookConfigUniqueId)) {
						if (myWebHookPayloadManager.isRegisteredFormat(whc.getPayloadFormat())){
							return whc;
						} else {
							throw new WebHookConfigNotFoundException("No registered Payload Handler for " + whc.getPayloadFormat());
						}
					}
			    	}
		    	} else {
		    		Loggers.SERVER.debug("WebHookUserRequestedExecutorImpl :: WebHooks are disasbled for  " + projectId);
		    	}
		}
    		throw new WebHookConfigNotFoundException(String.format("Webhook Configuration %s was not found", webHookConfigUniqueId));
	}

	@Override
	public WebHookConfig buildSimple(WebHookTemplateExecutionRequest webHookTemplateExecutionRequest) {
		// TODO Auto-generated method stub
		return WebHookConfig.builder()
				 .url(webHookTemplateExecutionRequest.getUrl())
				 .payloadFormat(webHookTemplateExecutionRequest.getFormat())
				 //.payloadTemplate(webHookExecutionRequest.getTemplateId())
				 .templates(new TreeMap<String,CustomMessageTemplate>())
				 //.authEnabled(webHookExecutionRequest.isAuthEnabled())
				 //.authType(webHookExecutionRequest.getAuthType())
				 //.authParameters(webHookExecutionRequest.getAuthParameters())
				 //.filters(new ArrayList<WebHookFilterConfig>())
				 //.states(webHookExecutionRequest.getConfigbuildState())
				 .headers(new ArrayList<WebHookHeaderConfig>())
				 .extraParameters(new TreeMap<String,String>()) //TODO: Should we get from config somehow?
				 .build();
	}
	
}
