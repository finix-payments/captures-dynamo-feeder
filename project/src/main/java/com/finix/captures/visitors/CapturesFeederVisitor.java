package com.finix.captures.visitors;

import com.finix.captures.model.CaptureSubmission;
import com.finix.shared.event.ConsumerEnvelope;
import com.finix.shared.event.ConsumerMessage;
import com.finix.shared.event.ConsumerVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

public class CapturesFeederVisitor implements ConsumerVisitor {

  @Getter
  private final List<CaptureSubmission> captures;

  public CapturesFeederVisitor() {
    this.captures = new ArrayList<>();
  }

  @Override
  public boolean captureDynamoSubmission(ConsumerMessage<io.finix.event.CaptureSubmission> message) {
    final CaptureSubmission capture = Optional.ofNullable(message)
        .map(ConsumerMessage::getPayload)
        .map(ConsumerEnvelope::getMessage)
        .map(this::convertToDbModel)
        .orElseThrow(
            () -> new RuntimeException("Could not unwrap ConsumerMessage to CaptureSubmission."));
    captures.add(capture);
    return true;
  }

  private CaptureSubmission convertToDbModel(io.finix.event.CaptureSubmission cs){
    return CaptureSubmission.builder()
        .submissionId(cs.getBatchSubmissionId())
        .transferId(cs.getTransferId())
        .amount(cs.getAmount())
        .processorOperationId(cs.getProcessorTxnId())
        .orderId(cs.getOrderId())
        .reportGroup(cs.getReportGroup())
        .merchantId(cs.getMerchantId())
        .build();
  }
}
