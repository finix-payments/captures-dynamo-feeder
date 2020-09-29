package com.finix.captures.visitors;

import com.finix.shared.event.ConsumerMessage;
import com.finix.shared.event.ConsumerVisitor;
import io.finix.event.CaptureSubmission;

public class CaptureSubmissionVisitor implements ConsumerVisitor {

  @Override
  public boolean captureDynamoSubmission(ConsumerMessage<CaptureSubmission> message) {
    return false;
  }
}
