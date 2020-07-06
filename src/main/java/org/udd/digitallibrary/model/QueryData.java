package org.udd.digitallibrary.model;

import lombok.Value;

@Value
public class QueryData {
    String field;
    String value;
    String operation;
}
