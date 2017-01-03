package com.ekatechserv.eaf.plugin.model;

import java.io.Serializable;
import java.util.Date;

public class RunResult implements Serializable {

    /**
     * unique identifier for each execution run
     */
    private String id;

    /**
     * Result status of the execution run : Value : Interim - When the execution
     * has started and not completed yet Completed - When the execution
     * completes
     */
    private String resultStatus;

    /**
     * Status fo the execution run - In Progress, Completed, Stopped etc
     */
    private String status;

    /**
     * Time when execution was started
     */
    private Date startTime;

    /**
     * Time when execution ended
     */
    private Date endTime;

    /**
     * Duration of the execution run
     */
    private long duration;

    /**
     * Total number test-cases in the project
     */
    private long tcTotal;

    /**
     * Number of testcases having execution flag as No and were Not Run during
     * execution run
     */
    private long tcSkipped;

    /**
     * Number of testcases actually executed during execution run
     */
    private long tcExecuted;

    /**
     * Number of testcases passed during execution run
     */
    private long tcPassed;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getStartTime() {
        return  new Date(startTime.getTime());
    }

    public void setStartTime(Date startTime) {
        this.startTime =  new Date(startTime.getTime());
    }

    public Date getEndTime() {
        return  new Date(endTime.getTime());
    }

    public void setEndTime(Date endTime) {
        this.endTime =  new Date(endTime.getTime());
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getTcTotal() {
        return tcTotal;
    }

    public void setTcTotal(long tcTotal) {
        this.tcTotal = tcTotal;
    }

    public long getTcSkipped() {
        return tcSkipped;
    }

    public void setTcSkipped(long tcSkipped) {
        this.tcSkipped = tcSkipped;
    }

    public long getTcExecuted() {
        return tcExecuted;
    }

    public void setTcExecuted(long tcExecuted) {
        this.tcExecuted = tcExecuted;
    }

    public long getTcPassed() {
        return tcPassed;
    }

    public void setTcPassed(long tcPassed) {
        this.tcPassed = tcPassed;
    }

    @Override
    public String toString() {
        return "ResultStatus=" + resultStatus + ", Total TCs=" + tcTotal + ", Skipped TCs=" + tcSkipped + ", Executed TCs=" + tcExecuted + ", Passed TCs=" + tcPassed + '}';
    }

}
