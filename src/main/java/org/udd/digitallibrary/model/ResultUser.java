package org.udd.digitallibrary.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResultUser {
    String firstName;
    String lastName;
    String locationName;
}
