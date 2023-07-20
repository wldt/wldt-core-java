package it.wldt.exception;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 27/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class WldtRuntimeException extends Exception {
    public WldtRuntimeException(String errorMsg) {
        super(errorMsg);
    }
}
