package com.finix.captures.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.datamodeling.TransactionWriteRequest;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItem;
import com.amazonaws.services.dynamodbv2.model.TransactWriteItemsRequest;
import com.finix.captures.model.CaptureSubmission;
import cyclops.control.Try;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CaptureSubmissionDaoImpl implements CaptureSubmissionDao {

  @Autowired
  private DynamoDBMapper mapper;

  public Try<Boolean, Exception> writeAll(List<CaptureSubmission> captureSubmissions)  {
    TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();
    captureSubmissions.stream().map(cs -> transactionWriteRequest.addPut(cs));
    try{
      mapper.transactionWrite(transactionWriteRequest);
      return Try.success(true);
    } catch (Exception e){
      return Try.failure(e);
    }
  }

  @Override
  public Try<PaginatedQueryList<CaptureSubmission>, Exception> getCapturesForSubmissionId(
      String submissionId) {
    CaptureSubmission cs = new CaptureSubmission();
    cs.setSubmissionId(submissionId);

    DynamoDBQueryExpression<CaptureSubmission> queryExpression =
        new DynamoDBQueryExpression<CaptureSubmission>()
            .withScanIndexForward(false)
            .withHashKeyValues(cs);
    return Try
        .withCatch(() -> mapper.query(CaptureSubmission.class, queryExpression), Exception.class);
  }

  @Override
  public Try<PaginatedQueryList<CaptureSubmission>, Exception> getCaptureForSubmissionIdAndTransferId(
      String submissionId,
      String transferId) {
    CaptureSubmission cs = new CaptureSubmission();
    cs.setSubmissionId(submissionId);

    Condition transferIdEqualsCondition = new Condition()
        .withComparisonOperator(ComparisonOperator.EQ)
        .withAttributeValueList(
            new AttributeValue()
                .withS(transferId));

    // Another generic query implementation would be thru ExpressionAttributeValues
    DynamoDBQueryExpression<CaptureSubmission> queryExpression =
        new DynamoDBQueryExpression<CaptureSubmission>()
            .withHashKeyValues(cs)
            .withRangeKeyCondition(
                "transfer_id",
                transferIdEqualsCondition)
            .withScanIndexForward(false);
    return Try
        .withCatch(() -> mapper.query(CaptureSubmission.class, queryExpression), Exception.class);
  }
}
