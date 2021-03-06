package com.ekatechserv.eaf.plugin;

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

/**
 * ExecutionRun - refers to a build process of the Test-Odyssey plugin
 *
 * @author EKA Techserv
 */
public class ExecutionRun {

    private static final String VERIFY_USER_URL = "/jenkins/verifyUser.do";
    private static final String CREATE_EXECUTION_URL = "/jenkins/createExecution.do";
    private static final String CHECK_RESULT_STATUS_URL = "/jenkins/checkResultStatus.do?executionId=";
    private static final String FETCH_ACTIVE_JOB_URL = "/jenkins/fetchActiveExecutionJob.do";

    private Map<String, String> projectMap;

    /**
     * jobId of the test-run returned from test-odyssey
     */
    private String jobId;

    /**
     * projectId of the test-run returned from test-odyssey
     */
    private String projectId;

    /**
     * Result of the test-run returned from test-odyssey
     */
    private RunResult result;

    /**
     * HttpCommunicator object which will be used for communicating with
     * test-odyssey
     */
    private final HttpCommunicator communicator;

    public ExecutionRun(HttpCommunicator communicator) {
        this.communicator = communicator;
    }

    public ExecutionRun(HttpCommunicator communicator, String jobId, String projectId) {
        this.communicator = communicator;
        this.projectId = projectId;
        this.jobId = jobId;
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
            throw new ExecutionRunException("Test Odyssey Error : Possible reasons - 1. Credentials provided are wrong  2. Role of the user is not Test-Engineer or Test-Manager in Test-Odyssey 3. No assigned project found in Test Odyssey for the given credentials 4. Organization licence is not premium");
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
        try {
            fields.add(new BasicNameValuePair("jobId", this.jobId));
            fields.add(new BasicNameValuePair("projectId", this.projectId));
            HttpResponse response = this.communicator.doPost(CREATE_EXECUTION_URL, fields);
            this.jobId = EntityUtils.toString(response.getEntity());
        } catch (IOException ex) {
            throw new ExecutionRunException("Test Odyssey Error : Exception creating exeution run in TestOdyssey", ex);
        }
        Logger.traceln("Execution created in test-odyssey with executionId : " + this.jobId);
    }

    /**
     * Checks for the result of the started execution. It updates the result
     * object every time it is called.
     *
     * @throws ExecutionRunException
     */
    public void checkResultStatus() throws ExecutionRunException {
        String uri = CHECK_RESULT_STATUS_URL + this.jobId;
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

    /**
     * fetch Active Execution Job options based on the projectId
     *
     * @param projectId
     * @return
     * @throws ExecutionRunException
     */
    public Map<String, String> fetchActiveExecutionJobs(String projectId) throws ExecutionRunException {
        Map<String, String> activeJobs = null;
        List<NameValuePair> fields = new ArrayList<>();
        fields.add(new BasicNameValuePair("projectId", projectId));
        HttpResponse response;
        try {
            response = this.communicator.doPost(FETCH_ACTIVE_JOB_URL, fields);
        } catch (IOException ex) {
            throw new ExecutionRunException("Test Odyssey Error : Exception connecting Url : " + FETCH_ACTIVE_JOB_URL, ex);
        }
        HttpEntity entity = response.getEntity();
        try {
            activeJobs = PluginUtil.convertEntityToMap(entity);
        } catch (IOException ex) {
            throw new ExecutionRunException("Test Odyssey Error : Exception fetching job returned from TestOdyssey ", ex);
        }
        Logger.traceln("Exiting fetchJenkinsExecutionActiveJobOptions" + activeJobs.size());
        return activeJobs;
    }
}
