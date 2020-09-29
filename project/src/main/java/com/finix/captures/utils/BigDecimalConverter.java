package com.finix.captures.utils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import java.math.BigDecimal;

public class BigDecimalConverter implements DynamoDBTypeConverter<String, BigDecimal> {

  @Override
  public String convert(BigDecimal object) {
    return object.toPlainString();
  }

  @Override
  public BigDecimal unconvert(String object) {
    return new BigDecimal(object);
  }
}
