package org.udd.digitallibrary.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResultUnit {
    String filename;
    String magazine;
    String title;
    String authors;
    String keywords;
    String areas;
    String highlight;
}
