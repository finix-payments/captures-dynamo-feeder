package com.finix.captures.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.finix.captures.visitors.CaptureSubmissionVisitor;
import com.finix.shared.event.ConsumerEventHandlingOrchestrator;
import com.finix.shared.event.ConsumerEventHandlingOrchestrator.DefaultConsumerEventHandlingOrchestrator;
import com.finix.shared.event.DynamoDbData;
import com.finix.shared.event.PublisherEnvelope;
import com.finix.shared.event.PublisherEnvelopeSQSTransformer;
import cyclops.control.Either;
import cyclops.control.Try;
import cyclops.reactive.ReactiveSeq;
import java.util.Map;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.Put;

public class CapturesDynamoFeeder implements RequestHandler<SQSEvent, Void> {

  private static final PublisherEnvelopeSQSTransformer eventTransformer = new PublisherEnvelopeSQSTransformer();
  private static final DynamoDbClient dynamoDbClient = DynamoDbClient.create();
  private static final String eventTable = System.getenv("DDB_CAPTURES_SUBMISSIONS_TABLE");
  private final DefaultConsumerEventHandlingOrchestrator eventHandlingOrchestrator = ConsumerEventHandlingOrchestrator
      .defaultPlan();
  private final CaptureSubmissionVisitor captureSubmitterVisitor = new CaptureSubmissionVisitor();

  @Override
  public Void handleRequest(SQSEvent event, Context context) {
    final LambdaLogger logger = context.getLogger();
    ReactiveSeq.fromStream(event
        .getRecords().stream())
        .map()
//    event.getRecords().stream().
    return null;
  }

  private Either<Exception, Put> handleMessage(SQSMessage msg) {
    final String body = msg.getBody();
    final Map<String, String> headers = msg.getAttributes();
    Try.withCatch(msg::getBody, Exception.class)

        .flatMap(eventTransformer::deserialize)
        .map(publisherEnvelope -> buildRequest(publisherEnvelope))
        .toEither();
//    return Try.withCatch(msg::getBody, Exception.class)
//        .flatMap(eventTransformer::deserialize)
//        .map(pub -> buildRequest(pub, index));
  }

  private Put buildRequest(PublisherEnvelope envelope) {
    final DynamoDbData dynamoData = envelopeDynamoTransformer.serialize(envelope, index);
    return Put.builder()
        .tableName(eventTable)
        .item(dynamoData.getItemFields())
        .conditionExpression(dynamoData.getItemKey().buildConditionalExpression())
        .build();
  }
}
