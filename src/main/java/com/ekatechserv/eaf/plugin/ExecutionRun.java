package com.ekatechserv.eaf.plugin;

import com.ekatechserv.eaf.plugin.model.Execution;
import com.ekatechserv.eaf.plugin.model.ExecutionRunException;
import com.ekatechserv.eaf.plugin.model.Logger;
import com.ekatechserv.eaf.plugin.model.RunResult;
import com.ekatechserv.eaf.plugin.util.PluginUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * ExecutionRun - refers to a build process of the Test-Odyssey plugin
 *
 * @author EKA Techserv
 */
public class ExecutionRun {

    private static final String VERIFY_USER_URL = "/jenkins/verifyUser.do";
    private static final String CREATE_EXECUTION_URL = "/jenkins/createExecution.do";
    private static final String CHECK_RESULT_STATUS_URL = "/jenkins/checkResultStatus.do?executionId=";

    private Map<String, String> projectMap;

    /**
     * Execution object sent to test-odyssey for starting a test-run
     */
    private final Execution execution;

    /**
     * Result of the test-run returned from test-odyssey
     */
    private RunResult result;

    /**
     * HttpCommunicator object which will be used for communicating with
     * test-odyssey
     */
    private final HttpCommunicator communicator;

    public ExecutionRun(Execution execution, HttpCommunicator communicator) {
        this.execution = execution;
        this.communicator = communicator;
    }

    public RunResult getResult() {
        return result;
    }

    public void setResult(RunResult result) {
        this.result = result;
    }

    public Map<String, String> getProjectMap() {
        return projectMap;
    }

    public void setProjectMap(Map<String, String> projectMap) {
        this.projectMap = projectMap;
    }

    /**
     * Method to verify whether the user-credentials provided in global config
     * exists in Test-Odyssey or not. After successfull verification it fetches
     * all the projects associated for the verified user.
     *
     * @param userId unique username as configured in Test-Odyssey
     * @param password password configured for the user in Test-Odyssey
     * @param orgShortCode organization code as given by Test-Odyssey for the
     * account
     * @return true when verification was successfull, false otherwise
     * @throws ExecutionRunException All exceptions thrown during verification
     * is wrapped into Execution
     */
    public boolean verifyUser(String userId, String password, String orgShortCode) throws ExecutionRunException {
        Logger.traceln("Inside verifyUser : " + userId + " ,password : " + password + " ,orgCode : " + orgShortCode);
        List<NameValuePair> fields = new ArrayList<>();
        fields.add(new BasicNameValuePair("userId", userId));
        fields.add(new BasicNameValuePair("password", password));
        fields.add(new BasicNameValuePair("orgShortCode", orgShortCode));
        HttpResponse response;
        try {
            response = this.communicator.doPost(VERIFY_USER_URL, fields);
        } catch (IOException ex) {
            throw new ExecutionRunException("Test Odyssey Error : Exception verifying user - " + userId, ex);
        }
        HttpEntity entity = response.getEntity();
        try {
            this.setProjectMap(PluginUtil.convertEntityToMap(entity));
        } catch (IOException ex) {
            throw new ExecutionRunException("Test Odyssey Error : Exception processing projects returned from TestOdyssey ", ex);
        }
        if (MapUtils.isEmpty(this.getProjectMap())) {
            throw new ExecutionRunException("Test Odyssey Error : Invalid credentials provided. No assigned project found in Test Odyssey for the given credentials.");
        }
        Logger.traceln("Exiting verifyUser" + this.getProjectMap().size());
        return MapUtils.isNotEmpty(this.getProjectMap());
    }

    /**
     * Creates an execution in test-odyssey which in turn will start
     * execution-run on the cloud for the selected project.
     *
     * @throws ExecutionRunException
     */
    public void createExecution() throws ExecutionRunException {
        Logger.traceln("Creating execution in test-odyssey");
        List<NameValuePair> fields = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            fields.add(new BasicNameValuePair("execution", mapper.writeValueAsString(this.execution)));
            HttpResponse response = this.communicator.doPost(CREATE_EXECUTION_URL, fields);
            this.execution.setId(EntityUtils.toString(response.getEntity()));
        } catch (IOException ex) {
            throw new ExecutionRunException("Test Odyssey Error : Exception creating exeution run in TestOdyssey", ex);
        }
        Logger.traceln("Execution created in test-odyssey with executionId : " + this.execution.getId());
    }

    /**
     * Checks for the result of the started execution. It updates the result
     * object every time it is called.
     *
     * @throws ExecutionRunException
     */
    public void checkResultStatus() throws ExecutionRunException {
        String uri = CHECK_RESULT_STATUS_URL + this.execution.getId();
        HttpResponse response;
        try {
            response = this.communicator.doGet(uri);
        } catch (IOException ex) {
            throw new ExecutionRunException("Test Odyssey Error : Exception connecting Url : " + uri, ex);
        }
        HttpEntity entity = response.getEntity();
        String entityStr;
        try {
            entityStr = EntityUtils.toString(entity);
        } catch (IOException | ParseException ex) {
            throw new ExecutionRunException("Test Odyssey Error : Exception reading result status", ex);
        }
        if ("exeIsNull".equalsIgnoreCase(entityStr)) {
            return;
        }
        try {
            this.setResult(PluginUtil.convertEntityToType(entityStr, RunResult.class));
        } catch (IOException ex) {
            throw new ExecutionRunException("Test Odyssey Error : Exception converting result returned from TestOdyssey", ex);
        }
        Logger.traceln("Result check : " + this.getResult());
    }
}
