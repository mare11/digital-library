package org.udd.digitallibrary.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResultData {
    String magazine;
    String title;
    String authors;
    String keywords;
    String areas;
    String highlight;
}
