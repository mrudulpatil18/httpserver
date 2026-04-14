package dev.mrudul.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonHeaders {
    CONTENT_LENGTH("Content-Length"),
    TRANSFER_ENCODING("Transfer-Encoding");

    private final String headerName;

}
