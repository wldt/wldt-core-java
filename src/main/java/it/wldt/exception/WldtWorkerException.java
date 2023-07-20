package it.wldt.exception;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 08/03/2021
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class WldtWorkerException extends Exception {

    public WldtWorkerException(String errorMsg){
        super(errorMsg);
    }
}
