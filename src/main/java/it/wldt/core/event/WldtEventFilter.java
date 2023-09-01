package it.wldt.core.event;

import java.util.ArrayList;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This method allows the creation of a new Filter with the list of event types used by a subscriber to
 * specify which events should be received. It can be used also to cancel a subscription and remove events types
 * that have been previously monitored.
 */
public class WldtEventFilter extends ArrayList<String> {
}
