package com.finix.captures.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.finix.captures.utils.BigDecimalConverter;
import java.math.BigDecimal;
import lombok.Data;

@Data
@DynamoDBTable(tableName = "capture_submissions")
public class CaptureSubmission {

  @DynamoDBHashKey(attributeName = "submission_id")
  private String submissionId;

  @DynamoDBRangeKey(attributeName = "transfer_id")
  private String transferId;

  @DynamoDBTypeConverted(converter = BigDecimalConverter.class)
  private BigDecimal amount;

  @DynamoDBAttribute(attributeName = "processor_operation_id")
  private String processorOperationId;

  @DynamoDBAttribute(attributeName = "order_id")
  private String orderId;

  @DynamoDBAttribute(attributeName = "report_group")
  private String reportGroup;

  @DynamoDBAttribute(attributeName = "merchant_id")
  private String merchantId;
}
