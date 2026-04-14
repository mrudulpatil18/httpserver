package dev.mrudul.model;

import lombok.Data;

import java.util.Set;

@Data
public class FormDataBody {
    Set<Header> headers;
    byte[]body;
}
