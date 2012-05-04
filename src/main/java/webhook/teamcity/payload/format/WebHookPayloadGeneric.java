/**
 * 
 */
package webhook.teamcity.payload.format;

import java.util.SortedMap;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.ResponsibilityInfo;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SFinishedBuild;
import jetbrains.buildServer.serverSide.SRunningBuild;
import webhook.teamcity.BuildState;
import webhook.teamcity.BuildStateEnum;
import webhook.teamcity.payload.WebHookPayload;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.payload.content.WebHookPayloadContent;

public abstract class WebHookPayloadGeneric implements WebHookPayload {
	
	WebHookPayloadManager myManager;
	
	public WebHookPayloadGeneric(WebHookPayloadManager manager){
		this.setPayloadManager(manager);
	}

	public void setPayloadManager(WebHookPayloadManager manager){
		myManager = manager;
	}
	
	public abstract void register();
		
	
	public String beforeBuildFinish(SRunningBuild runningBuild, SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.BEFORE_BUILD_FINISHED, extraParameters);
		return getStatusAsString(content);
	}

	/**
	 * buildChangedStatus has been deprecated because it alluded to build history status, which was incorrect. 
	 * It will no longer be called by the WebHookListener
	 */
	@Deprecated
	public String buildChangedStatus(SRunningBuild runningBuild, SFinishedBuild previousBuild,
			Status oldStatus, Status newStatus,
			SortedMap<String, String> extraParameters) {
		return "";
	}

	public String buildFinished(SRunningBuild runningBuild, SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.BUILD_FINISHED, extraParameters);
		return getStatusAsString(content);
	}

	public String buildInterrupted(SRunningBuild runningBuild, SFinishedBuild previousBuild,
			SortedMap<String, String> extraParameters) {
		WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.BUILD_INTERRUPTED, extraParameters);
		return getStatusAsString(content);
	}

	public String buildStarted(SRunningBuild runningBuild, SFinishedBuild previousBuild, 
			SortedMap<String, String> extraParameters) {
		WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), runningBuild, previousBuild, BuildStateEnum.BUILD_STARTED, extraParameters);
		return getStatusAsString(content);
	}

	public String responsibleChanged(SBuildType buildType,
			ResponsibilityInfo responsibilityInfoOld,
			ResponsibilityInfo responsibilityInfoNew, boolean isUserAction,
			SortedMap<String, String> extraParameters) {
		
		WebHookPayloadContent content = new WebHookPayloadContent(myManager.getServer(), buildType, BuildStateEnum.RESPONSIBILITY_CHANGED, extraParameters);
		content.setMessage("Build " + buildType.getFullName().toString()
				+ " has changed responsibility from " 
				+ " " + responsibilityInfoOld.getResponsibleUser().getDescriptiveName()
				+ " to "
				+ responsibilityInfoNew.getResponsibleUser().getDescriptiveName()
			);
		content.setText(buildType.getFullName().toString()
				+ " changed responsibility from " 
				+ responsibilityInfoOld.getResponsibleUser().getUsername()
				+ " to "
				+ responsibilityInfoNew.getResponsibleUser().getUsername()
			);
		
		content.setComment(responsibilityInfoNew.getComment());
		return getStatusAsString(content);
	}

	protected abstract String getStatusAsString(WebHookPayloadContent content);

	public abstract String getContentType();

	public abstract Integer getRank();

	public abstract void setRank(Integer rank);

	public abstract String getCharset();

	
}