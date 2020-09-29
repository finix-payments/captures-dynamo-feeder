package com.finix.captures.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.finix.captures.model.CaptureSubmission;
import cyclops.control.Try;
import java.util.List;

public interface CaptureSubmissionDao {
  Try<PaginatedQueryList<CaptureSubmission>, Exception> getCapturesForSubmissionId(
      String submissionId);

  Try<PaginatedQueryList<CaptureSubmission>, Exception> getCaptureForSubmissionIdAndTransferId(
      String submissionId, String transferId);

  Try<Boolean, Exception> writeAll(List<CaptureSubmission> captureSubmissions);
}
