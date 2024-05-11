package it.virtualthreads;

import it.common.Report;

public interface ReportObserver {
    void notifyNewReport(Report report);
}
