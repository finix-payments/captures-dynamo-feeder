package com.finix.captures.visitors;

import com.finix.captures.model.CaptureSubmission;
import com.finix.shared.event.ConsumerEnvelope;
import com.finix.shared.event.ConsumerMessage;
import com.finix.shared.event.ConsumerVisitor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;

public class CapturesFeederVisitor implements ConsumerVisitor {

  @Getter
  private final List<CaptureSubmission> captures;

  private final Set<String> uniqueKeys;

  public CapturesFeederVisitor() {
    this.captures = new ArrayList<>();
    this.uniqueKeys = new HashSet<>();
  }

  @Override
  public boolean captureDynamoSubmission(ConsumerMessage<io.finix.event.CaptureSubmission> message) {
    final Optional<io.finix.event.CaptureSubmission> cs = Optional
        .ofNullable(message)
        .map(ConsumerMessage::getPayload)
        .map(ConsumerEnvelope::getMessage);
    if(cs.isPresent() && !uniqueKeys.contains(cs.get().getBatchSubmissionId() + "#" + cs.get().getTransferId())) {
      final CaptureSubmission capture = cs
          .map(this::convertToDbModel)
          .orElseThrow(
              () -> new RuntimeException(
                  "Could not unwrap ConsumerMessage to CaptureSubmission."));
      captures.add(capture);
      uniqueKeys.add(capture.getSubmissionId() + "#" + capture.getTransferId());
    }
    return true;
  }

  private CaptureSubmission convertToDbModel(io.finix.event.CaptureSubmission cs){
    return CaptureSubmission.builder()
        .submissionId(cs.getBatchSubmissionId())
        .transferId(cs.getTransferId())
        .amount(cs.getAmount().longValue())
        .processorOperationId(cs.getProcessorTxnId())
        .orderId(cs.getOrderId())
        .reportGroup(cs.getReportGroup())
        .merchantId(cs.getMerchantId())
        .build();
  }
}
