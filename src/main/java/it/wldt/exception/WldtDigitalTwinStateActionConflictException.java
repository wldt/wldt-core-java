package it.wldt.exception;

/**
 * Author:
 *      Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/03/2023
 *
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class WldtDigitalTwinStateActionConflictException extends Exception {

    public WldtDigitalTwinStateActionConflictException(String errorMsg){
        super(errorMsg);
    }

}
