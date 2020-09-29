package com.finix.captures.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.finix.captures.dao.CaptureSubmissionDao;
import com.finix.captures.dao.CaptureSubmissionDaoImpl;
import com.finix.captures.visitors.CapturesFeederVisitor;
import com.finix.shared.event.ConsumerEventHandlingOrchestrator;
import com.finix.shared.event.ConsumerEventHandlingOrchestrator.DefaultConsumerEventHandlingOrchestrator;
import com.finix.shared.event.PublisherEnvelope;
import com.finix.shared.event.PublisherEnvelopeSQSTransformer;
import com.google.common.base.Throwables;
import cyclops.control.Either;
import cyclops.control.Try;
import cyclops.data.Vector;
import cyclops.data.tuple.Tuple;
import cyclops.data.tuple.Tuple2;
import cyclops.reactive.ReactiveSeq;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class CapturesDynamoFeeder implements RequestHandler<SQSEvent, Void> {

  private static final String ddbTableName = System.getenv("DDB_CAPTURES_TABLE");

  private static final PublisherEnvelopeSQSTransformer eventTransformer = new PublisherEnvelopeSQSTransformer();
  private final DefaultConsumerEventHandlingOrchestrator eventHandlingOrchestrator = ConsumerEventHandlingOrchestrator
      .defaultPlan();
  private final CapturesFeederVisitor captureSubmitterVisitor = new CapturesFeederVisitor();

  @Override
  public Void handleRequest(SQSEvent event, Context context) {
    final LambdaLogger logger = context.getLogger();
    final ReactiveSeq<Either<Tuple2<SQSMessage, Exception>, Boolean>> seqEithers = ReactiveSeq.fromStream(event
        .getRecords().stream())
        .map(this::handleMessage);

    final Tuple2<Vector<Tuple2<SQSMessage, Exception>>, Vector<Boolean>> partitionEithers = Either
        .partitionEithers(seqEithers);
    final boolean hasErrors = !partitionEithers._1().isEmpty();
    if(hasErrors){
      partitionEithers._1()
          .forEach(t -> {
            logger.log("Sqs id " + t._1().getMessageId());
            logger.log("Sqs body " + t._1().getBody());
            logger.log("Failed to convert exception message " + t._2().getMessage());
            logger.log("Stack trace " + Throwables.getStackTraceAsString(t._2()));
          });
      throw new RuntimeException("Some messages in the SQS batch failed to deserialize.");
    }
    CaptureSubmissionDao dao = new CaptureSubmissionDaoImpl(ddbTableName);

    final int size = captureSubmitterVisitor.getCaptures().size();
    final Try<Boolean, Exception> writeToDynamo = dao.writeAll(captureSubmitterVisitor.getCaptures())
        .peek(t ->
            logger.log("Saved " + size +  "captures to DynamoDB."))
        .peekFailed(ex ->{
            logger.log("Failed to save " + size + " captures to DynamoDB.");
            logger.log("Dynamo Exception Message: " + ex.getMessage());
            logger.log("Dynamo stack trace: " + Throwables.getStackTraceAsString(ex));
        });

    if(writeToDynamo.isFailure()){
      throw new RuntimeException("Lambda job failed to write to DynamoDB.");
    } else {
      return null;
    }
  }

  private Either<Tuple2<SQSMessage, Exception>, Boolean> handleMessage(SQSMessage msg) {
    final Map<String, String> headers = msg.getAttributes();
    return eventHandlingOrchestrator.handle(headers, msg.getBody(), captureSubmitterVisitor)
        .toEither()
        .mapLeft(ex -> Tuple.tuple(msg, ex));
  }

  // Due to the Visitor pattern implicit in event-shared library, have to use side-effects to add
  // CaptureSubmission objects to the visitor's private List
  private Try<Boolean, Exception> buildRequest(PublisherEnvelope envelope, Map<String, String> headers) {
    return Try.withCatch(() -> Optional.ofNullable(envelope.getMessage())
        .map(Arrays::toString)
        .orElseThrow(() -> new RuntimeException("Publisher envelope body is empty.")), Exception.class)
            .flatMap(payload -> eventHandlingOrchestrator.handle(headers, payload,
                captureSubmitterVisitor));
  }
}
