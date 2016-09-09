package webhook.teamcity.server.rest.request;

import static org.junit.Assert.*;
import static webhook.teamcity.server.rest.request.TemplateRequest.API_TEMPLATES_URL;

import javax.ws.rs.core.MediaType;

import jetbrains.buildServer.server.rest.data.PermissionChecker;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.SecurityContext;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import webhook.teamcity.server.rest.model.template.NewTemplateDescription;
import webhook.teamcity.server.rest.model.template.Templates;

import com.riffpie.common.testing.AbstractSpringAwareJerseyTest;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.ContextLoaderListener;

//@RunWith(MockitoJUnitRunner.class)
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {
//	    "classpath:/TestSpringContext.xml"
//	    })

public class CreateNewTemplateTest extends WebHookAbstractSpringAwareJerseyTest {

    @Test
    public void testXmlTemplatesRequest() {
        WebResource webResource = resource();
        Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_XML_TYPE).get(Templates.class);
        assertTrue(responseMsg.count == 0);
    }
    
    @Test
    public void testJsonTemplatesRequest() {
        WebResource webResource = resource();
        Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
        assertTrue(responseMsg.count == 0);
    }
    
    @Test
    public void testJsonRequestAndUpdate() {
    	WebResource webResource = resource();
    	Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertTrue(responseMsg.count == 0);
    	
    	NewTemplateDescription newTemplateDescription = new NewTemplateDescription();
    	newTemplateDescription.description = "A test template";
    	newTemplateDescription.name = "testTemplateFromUnitTest";
    	

    	webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).post(newTemplateDescription);
    	Templates updatedResponse = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertTrue(updatedResponse.count == 1);

    }
    
    @Test(expected=com.sun.jersey.api.client.UniformInterfaceException.class)
    public void testJsonRequestAndUpdateFailsForSameTemplateName() {
    	WebResource webResource = resource();
    	Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertTrue(responseMsg.count == 0);
    	
    	NewTemplateDescription newTemplateDescription = new NewTemplateDescription();
    	newTemplateDescription.description = "A test template";
    	newTemplateDescription.name = "testTemplateFromUnitTest";
    	
    	
    	webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).post(newTemplateDescription);
    	Templates updatedResponse = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertTrue(updatedResponse.count == 1);
    	
    	webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).post(newTemplateDescription);
    	updatedResponse = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	
    }
    
    @Test
    public void testJsonRequestAndUpdateWithMoreTemplateDetail() {
    	WebResource webResource = resource();
    	Templates responseMsg = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertTrue(responseMsg.count == 0);
    	
    	NewTemplateDescription newTemplateDescription = new NewTemplateDescription();
    	newTemplateDescription.description = "A test template";
    	newTemplateDescription.name = "testTemplateFromUnitTest";
    	
    	
    	webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).post(newTemplateDescription);
    	Templates updatedResponse = webResource.path(API_TEMPLATES_URL).accept(MediaType.APPLICATION_JSON_TYPE).get(Templates.class);
    	assertTrue(updatedResponse.count == 1);
    	
    	prettyPrint(updatedResponse);
    	
    }
}