package com.finix.captures.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.datamodeling.TransactionWriteRequest;
import com.finix.captures.model.CaptureSubmission;
import cyclops.control.Try;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class CaptureSubmissionDaoImpl implements CaptureSubmissionDao {

  private static long DAYS_TO_EXPIRE = 15;

  private DynamoDBMapper mapper;
  private String ddbTableName;

  public CaptureSubmissionDaoImpl(String tableName) {
    this.ddbTableName = tableName;
    this.mapper = dynamoDBMapper();
  }

  private DynamoDBMapper dynamoDBMapper() {
    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    final DynamoDBMapperConfig mapperConfig = DynamoDBMapperConfig.builder()
        .withTableNameOverride(TableNameOverride.withTableNameReplacement(ddbTableName))
        .build();
    return new DynamoDBMapper(client, mapperConfig);
  }

  public Try<Boolean, Exception> writeAll(List<CaptureSubmission> captureSubmissions)  {
    TransactionWriteRequest transactionWriteRequest = new TransactionWriteRequest();
    final long expiresAt = Instant.now().plus(DAYS_TO_EXPIRE, ChronoUnit.DAYS).getEpochSecond();
    captureSubmissions.forEach(cs -> {
      cs.setExpiresAt(expiresAt);
      transactionWriteRequest.addPut(cs);
    });
    try{
      mapper.transactionWrite(transactionWriteRequest);
      return Try.success(true);
    } catch (Exception e){
      return Try.failure(e);
    }
  }
}
