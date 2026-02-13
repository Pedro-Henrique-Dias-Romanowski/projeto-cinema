package com.romanowski.pedro.controller.handler;

import java.util.Date;

public record ExceptionResponse(
        Date timstamp, String message, String details
) {}
