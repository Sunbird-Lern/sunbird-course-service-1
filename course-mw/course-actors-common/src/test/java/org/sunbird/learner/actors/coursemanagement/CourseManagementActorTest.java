package org.sunbird.learner.actors.coursemanagement;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.RequestBodyEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.common.models.response.Response;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.ProjectUtil;
import org.sunbird.common.request.Request;
import org.sunbird.keys.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static akka.testkit.JavaTestKit.duration;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({
        ProjectUtil.class,
        Unirest.class
})
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*"})
public class CourseManagementActorTest {

    private static ActorSystem system;
    private static final Props props =
            Props.create(org.sunbird.learner.actors.course.CourseManagementActor.class);


    @Before
    public void setUp() {
        PowerMockito.mockStatic(ProjectUtil.class);
        PowerMockito.mockStatic(Unirest.class);
        system = ActorSystem.create("system");
        when(ProjectUtil.getConfigValue(JsonKey.EKSTEP_BASE_URL))
                .thenReturn("ekstep_api_base_url");
        when(ProjectUtil.getConfigValue(JsonKey.CONTENT_PROPS_TO_ADD))
                .thenReturn("learning.content.props.to.add");
    }

    @Test
    public void testCourseCreateSuccess() throws UnirestException {
        mockResponseUnirest();
        Response response = (Response) doRequest(createCourseRequest());
        Assert.assertNotNull(response);
    }

    @Test
    public void testCourseCreateCopySuccess() throws UnirestException {
        mockResponseUnirest();
        Response response = (Response) doRequest(createCourseCopyRequest());
        Assert.assertNotNull(response);
    }

    private Object doRequest(Map<String, Object> data) {
        TestKit probe = new TestKit(system);
        ActorRef toc = system.actorOf(props);
        Request request = new Request();
        request.getRequest().put(SunbirdKey.COURSE, data);
        request.setOperation("createCourse");
        toc.tell(request, probe.getRef());
        Response response = probe.expectMsgClass(duration("10 second"), Response.class);
        return response;
    }

    private void mockResponseUnirest() throws UnirestException {
        HttpRequestWithBody http = Mockito.mock(HttpRequestWithBody.class);
        RequestBodyEntity entity = Mockito.mock(RequestBodyEntity.class);
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);
        when(Unirest.post(Mockito.anyString())).thenReturn(http);
        when(http.headers(Mockito.anyMap())).thenReturn(http);
        when(http.body(Mockito.anyString())).thenReturn(entity);
        when(entity.asString()).thenReturn(response);
        when(response.getBody()).thenReturn("{\"responseCode\" :\"OK\" }");
    }

    private Map<String, Object> createCourseRequest() {
        Map<String, Object> courseMap = new HashMap<>();
        courseMap.put(SunbirdKey.NAME, "Test_CurriculumCourse With 3 Units");
        courseMap.put(SunbirdKey.DESCRIPTION, "Test_CurriculumCourse description");
        courseMap.put(SunbirdKey.MIME_TYPE, "application/vnd.ekstep.content-collection");
        courseMap.put(SunbirdKey.CONTENT_TYPE, "Course");
        courseMap.put(SunbirdKey.CODE, "Test_CurriculumCourse");
        Map<String, Object> requestMap = new HashMap<String, Object>() {{
            put(SunbirdKey.REQUEST, new HashMap<String, Object>() {{
                put(SunbirdKey.COURSE, courseMap);
            }});
        }};
        return requestMap;
    }

    private Map<String, Object> createCourseCopyRequest() {
        Map<String, Object> courseMap = new HashMap<>();
        courseMap.put(SunbirdKey.NAME, "Test_CurriculumCourse With 3 Units");
        courseMap.put(SunbirdKey.CODE, "Test_CurriculumCourse");
        courseMap.put(SunbirdKey.COPY_SCHEME, "TextBookToCourse");
        courseMap.put(SunbirdKey.CREATED_BY, "testCreatedBy");
        courseMap.put(SunbirdKey.COURSE_CREATED_FOR, Arrays.asList("abc"));
        courseMap.put(SunbirdKey.FRAMEWORK, "testFramework");
        courseMap.put(SunbirdKey.ORGANISATION, Arrays.asList("abc"));
        Map<String, Object> requestMap = new HashMap<String, Object>() {{
            put(SunbirdKey.REQUEST, new HashMap<String, Object>() {{
                put(SunbirdKey.SOURCE, "do_123");
                put(SunbirdKey.COURSE, courseMap);
            }});
        }};
        return requestMap;
    }

}