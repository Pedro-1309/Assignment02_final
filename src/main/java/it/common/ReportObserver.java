package it.common;

import it.common.Report;

public interface ReportObserver {
    void notifyNewReport(Report report);
}
