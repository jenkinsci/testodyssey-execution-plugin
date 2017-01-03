package com.ekatechserv.eaf.plugin;

import com.ekatechserv.eaf.plugin.model.Browser;
import com.ekatechserv.eaf.plugin.model.EnvModMapper;
import com.ekatechserv.eaf.plugin.model.Execution;
import com.ekatechserv.eaf.plugin.model.ExecutionRunException;
import com.ekatechserv.eaf.plugin.model.Logger;
import com.ekatechserv.eaf.plugin.model.OperatingSystem;
import com.ekatechserv.eaf.plugin.model.RunResult;
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.ListBoxModel;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked and a new
 * {@link TestOdysseyBuilder} is created. The created instance is persisted to
 * the project configuration XML by using XStream, so this allows you to use
 * instance fields (like {@link #name}) to remember the configuration.
 *
 * <p>
 * When a build is performed, the
 * {@link #perform(AbstractBuild, Launcher, BuildListener)} method will be
 * invoked.
 *
 * @author Kohsuke Kawaguchi
 */
public class TestOdysseyBuilder extends Builder {

    private static final String RESULT_STATUS_COMPLETED = "Completed";
    private static final String RESULT_STATUS_NOT_STARTED = "Yet to start";

    private final String execName;
    private final String projectName;
    private final String baseUrl;
    private final String browserId;
    private final String osId;
    private final String minPassPercentage;

    private final String loglevel;

    private final Execution execution;
    private ExecutionRun testRun;
    private HttpCommunicator communicator;

    @DataBoundConstructor
    public TestOdysseyBuilder(String execName, String projectName, String baseUrl, String browserId, String osId, String minPassPercentage) {
        this.execName = execName;
        this.projectName = projectName;
        this.baseUrl = baseUrl;
        this.browserId = browserId;
        this.osId = osId;
        this.minPassPercentage = minPassPercentage;
        this.loglevel = "normal";
        this.execution = new Execution(execName);
        EnvModMapper mapper = new EnvModMapper(osId, browserId, baseUrl);
        this.execution.setEnvMods(Arrays.asList(mapper));
        this.execution.setExecutionType("559e9995ca1f05adcaff70ca");//sequential
        this.execution.setOrgShortCode(this.getDescriptor().getOrgShortCode());
        this.execution.setUserId(this.getDescriptor().getUserId());
    }

    public String getProjectName() {
        return projectName;
    }

    public String getLoglevel() {
        return loglevel;
    }

    public Execution getExecution() {
        return execution;
    }

    public ExecutionRun getTestRun() {
        return testRun;
    }

    public HttpCommunicator getCommunicator() {
        return communicator;
    }

    public String getMinPassPercentage() {
        return minPassPercentage;
    }

    public String getExecName() {
        return execName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getBrowserId() {
        return browserId;
    }

    public String getOsId() {
        return osId;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        Logger.init(listener.getLogger(), this.loglevel);
        Logger.info("Test Odyssey process started !!");
        Logger.traceln("Inside builder.perform method - build started");
        communicator = HttpCommunicator.getInstance();
        this.getExecution().setProjectId(this.getProjectName());
        testRun = new ExecutionRun(this.getExecution(), this.getCommunicator());
        try {
            verifyUserAndGetProjectMap(testRun);
            testRun.createExecution();
            pollRunResult();
        } catch (ExecutionRunException ex) {
            Logger.exception(ex);
            return false;
        }
        Logger.traceln("Exiting builder.perform method - build ended");
        return validateExecRules();
    }

    private void verifyUserAndGetProjectMap(ExecutionRun run) throws ExecutionRunException {
        Logger.traceln("Entering verifyUserAndGetProjectMap");
        String userId = this.getDescriptor().getUserId();
        String password = this.getDescriptor().getPassword();
        String orgCode = this.getDescriptor().getOrgShortCode();
        run.verifyUser(userId, password, orgCode);
        Logger.traceln("Exiting verifyUserAndGetProjectMap for user Id: " + userId + " under OrgCode : " + orgCode);
    }

    private void pollRunResult() throws ExecutionRunException {
        Logger.traceln("Entering pollRunResult");
        RunResult result = this.getTestRun().getResult();
        String resultStatus = result != null ? result.getResultStatus() : RESULT_STATUS_NOT_STARTED;
        Date date = new Date();
        long startTime = date.getTime();
        while (!RESULT_STATUS_COMPLETED.equalsIgnoreCase(resultStatus)) {
            this.getTestRun().checkResultStatus();
            if (this.getTestRun().getResult() == null) {
                Date endDate = new Date();
                long endTime = endDate.getTime();
                double durationMins = (double) (endTime - startTime) / 60000;
                long duration = Math.round(durationMins);
                if (duration >= 5) {
                    throw new ExecutionRunException("No response from the server. Execution timedout after 5 minutes.");
                }
            } else {
                resultStatus = this.getTestRun().getResult().getResultStatus();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                throw new ExecutionRunException("Thread Interrupted while sleeping - ", ex);
            }
        }
    }

    private boolean validateExecRules() {
        long temp = Long.parseLong(this.getMinPassPercentage());
        if (temp > 0l) {
            RunResult result = this.getTestRun().getResult();
            long actual = result.getTcPassed() * 100 / result.getTcExecuted();
            boolean execPassed = actual >= temp;
            long tcFailed = result.getTcExecuted() - result.getTcPassed();
            Logger.infoln("Total TC  : " + result.getTcTotal() + ", TCs executed: " + result.getTcExecuted() + ", TCs Passed : " + result.getTcPassed() + ", TCs Failed :" + tcFailed + ", TCs Skipped : " + result.getTcSkipped());
            Logger.infoln("Min. Pass Percentage : " + temp + "%");
            Logger.infoln("Run  Pass Percentage : " + actual + "%");
            return execPassed;
        }
        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link TestOdysseyBuilder}. Used as a singleton. The class
     * is marked as public so that it can be accessed from views.
     *
     * <p>
     * See
     * <tt>src/main/resources/hudson/plugins/hello_world/TestOdysseyBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * To persist global configuration information, simply store it in a
         * field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String userId;
        private String password;
        private String orgShortCode;
        private ExecutionRun testRun;
        private HttpCommunicator communicator;

        /**
         * Performs on-the-fly validation of the form field 'execName'.
         *
         * @param value This parameter receives the value that the user has
         * typed.
         * @return Indicates the outcome of the validation. This is sent to the
         * browser.
         * @throws java.io.IOException
         * @throws javax.servlet.ServletException
         */
        public FormValidation doCheckExecName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please provide an execution name");
            }
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field 'projectName'.
         *
         * @param value This parameter receives the value that the user has
         * typed.
         * @return Indicates the outcome of the validation. This is sent to the
         * browser.
         * @throws java.io.IOException
         * @throws javax.servlet.ServletException
         */
        public FormValidation doCheckProjectName(@QueryParameter String value)
                throws IOException, ServletException {
            if (this.testRun == null || this.testRun.getProjectMap() == null || this.testRun.getProjectMap().isEmpty()) {
                return FormValidation.error("User credentials not provided in global configuration of Test Odyssey plugin or Test Odyssey server is down - contact support.");
            }
            if (value.length() == 0) {
                return FormValidation.error("Please provide project name as configured in Test-Odyssey.");
            }
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field 'baseUrl'.
         *
         * @param value This parameter receives the value that the user has
         * typed.
         * @return Indicates the outcome of the validation. This is sent to the
         * browser.
         * @throws java.io.IOException
         * @throws javax.servlet.ServletException
         */
        public FormValidation doCheckBaseUrl(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please provide base url of the application to test.");
            }
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field 'minPassPercentage'.
         *
         * @param value This parameter receives the value that the user has
         * typed.
         * @return Indicates the outcome of the validation. This is sent to the
         * browser.
         * @throws java.io.IOException
         * @throws javax.servlet.ServletException
         */
        public FormValidation doCheckMinPassPercentage(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please provide minimum pass percentage to pass the build.");
            }
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field 'userId'.
         *
         * @param value This parameter receives the value that the user has
         * typed.
         * @return Indicates the outcome of the validation. This is sent to the
         * browser.
         * @throws java.io.IOException
         * @throws javax.servlet.ServletException
         */
        public FormValidation doCheckUserId(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please provide userId as configured in Test-Odyssey.");
            }
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field 'password'.
         *
         * @param value This parameter receives the value that the user has
         * typed.
         * @return Indicates the outcome of the validation. This is sent to the
         * browser.
         * @throws java.io.IOException
         * @throws javax.servlet.ServletException
         */
        public FormValidation doCheckPassword(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please provide password as configured in Test-Odyssey for the provided userId.");
            }
            return FormValidation.ok();
        }

        /**
         * Performs on-the-fly validation of the form field 'orgShortCode'.
         *
         * @param value This parameter receives the value that the user has
         * typed.
         * @return Indicates the outcome of the validation. This is sent to the
         * browser.
         * @throws java.io.IOException
         * @throws javax.servlet.ServletException
         */
        public FormValidation doCheckOrgShortCode(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please provide organization code as provided by Test-Odyssey.");
            }
            return FormValidation.ok();
        }

        /**
         * Builds the project list for the user-credentials provided to be
         * displayed in the project dropdown.
         *
         * @return ListBoxModel list of all projects user is associated with
         */
        public ListBoxModel doFillProjectNameItems() {
            ListBoxModel items = new ListBoxModel();
            for (Map.Entry<String, String> entry : testRun.getProjectMap().entrySet()) {
                items.add(entry.getKey(), entry.getValue());
            }
            return items;
        }

        /**
         * Builds the OS list to be displayed in the OS dropdown.
         *
         * @return ListBoxModel OS list
         */
        public ListBoxModel doFillOsIdItems() {
            ListBoxModel items = new ListBoxModel();
            for (Map.Entry<String, String> entry : OperatingSystem.getOsMap().entrySet()) {
                items.add(entry.getValue(), entry.getKey());
            }
            return items;
        }

        /**
         * Builds the browser list to be displayed in the browser dropdown.
         *
         * @return ListBoxModel browser list
         */
        public ListBoxModel doFillBrowserIdItems() {
            ListBoxModel items = new ListBoxModel();
            for (Map.Entry<String, String> entry : Browser.getBrowserMap().entrySet()) {
                items.add(entry.getValue(), entry.getKey());
            }
            return items;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         *
         * @return Display name in Global configuration
         */
        @Override
        public String getDisplayName() {
            return "Test Odyssey";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().

            this.userId = formData.getString("userId");
            this.password = formData.getString("password");
            this.orgShortCode = formData.getString("orgShortCode");
            if (StringUtils.isEmpty((this.getUserId()))) {
                throw new FormException("Provide valid user credentials for Test Odyssey plugin. UserId missing.", userId);
            }
            if (StringUtils.isEmpty((this.getPassword()))) {
                throw new FormException("Provide valid user credentials for Test-Odyssey plugin. Password missing.", password);
            }
            if (StringUtils.isEmpty((this.getOrgShortCode()))) {
                throw new FormException("Provide valid user credentials for Test-Odyssey plugin. Organization code missing.", orgShortCode);
            }
            communicator = HttpCommunicator.getInstance();
            testRun = new ExecutionRun(communicator);
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            try {
                testRun.verifyUser(userId, password, orgShortCode);
            } catch (ExecutionRunException ex) {
                Logger.info("Invalid user credentials for Test Odyssey." + ex.getMessage());
                throw new FormException(ex, userId);
            }
            Logger.trace("Save in configure ");
            return super.configure(req, formData);
        }

        public String getUserId() {
            return userId;
        }

        public String getPassword() {
            return password;
        }

        public String getOrgShortCode() {
            return orgShortCode;
        }

        public ExecutionRun getTestRun() {
            return testRun;
        }

        public HttpCommunicator getCommunicator() {
            return communicator;
        }

    }
}
