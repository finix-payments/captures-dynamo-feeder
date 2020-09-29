package com.finix.captures.dao;

import com.finix.captures.model.CaptureSubmission;
import cyclops.control.Try;
import java.util.List;

public interface CaptureSubmissionDao {
  Try<Boolean, Exception> writeAll(List<CaptureSubmission> captureSubmissions);
}
