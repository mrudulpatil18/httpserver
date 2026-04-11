package org.redbananas.model;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class FormDataBody {
    Set<Header> headers;
    byte[]body;
}
