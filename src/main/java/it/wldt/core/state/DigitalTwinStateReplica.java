/*
 * Copyright (c) 2025 - Current Year
 * Marco Picone Ph.D
 * Email: picone.m@gmail.com
 * Website: https://www.marcopicone.net/
 * All rights reserved.
 *
 * This program is provided under a Dual Licensing model:
 * 1) GNU General Public License version 3.0 (GPL-3.0) for open-source, academic,
 *    research, non-profit, and other non-commercial use; or
 * 2) Commercial License, for any commercial use, proprietary development, or
 *    closed-source distribution. To obtain a Commercial License, please contact: Marco Picone (picone.m@gmail.com)
 *
 * By using this software, you agree to comply with the terms of the applicable license.
 * This applies to all forms of the software, including source code and compiled/binary forms.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package it.wldt.core.state;

import it.wldt.core.event.WldtEvent;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Local, <b>unfiltered</b> copy of the Digital Twin State kept by the components that observe the state on the
 * event bus (Digital Adapters, Storage Manager, Augmentation Function Handlers).
 * <p>
 * The event bus carries the complete {@link DigitalTwinStateDescription} only when the structure changes and a delta
 * of {@link DigitalTwinStateValues} (only the changed values) at every values update. The replica is seeded with the
 * description and the values received at synchronization time ({@link #seed(DigitalTwinStateDescription, DigitalTwinStateValues)})
 * and is then kept up to date by applying the two kinds of event.
 * <p>
 * <b>Usage:</b> the apply methods return whether the event has been applied. Right after an apply the owner reads
 * {@link #getDescription()} / {@link #getValues()}, which are the state right after that event, and the changes from
 * the event itself ({@link DigitalTwinStateManager#getDescriptionChanges(WldtEvent)},
 * {@link DigitalTwinStateManager#getValuesChanges(WldtEvent)}). The owner should hold the monitor of the replica
 * ({@code synchronized (replica)}) while applying an event and notifying its observers, to keep snapshots and
 * callbacks ordered. When a description is applied, the owner must call {@link #applyNextPendingValues()} until it
 * returns {@code null} to process the values events that were waiting for it.
 * <p>
 * <b>Ordering:</b> the description event is always published before the values events that refer to it, but the two
 * topics are delivered independently. A values event that refers to a description that the replica does not know yet
 * is kept in a list (in arrival order) and processed, one at a time, with {@link #applyNextPendingValues()} after the
 * description has been applied. Values events never get discarded because of their description id (only the events
 * that precede the seeding of the replica are skipped, since they are already included in the seeded values).
 * <p>
 * <b>Foreign events:</b> description ids carry the creation timestamp of the {@link DigitalTwinStateManager} that
 * produced them. Once seeded, the replica ignores (with a debug log) the events whose description id belongs to a
 * different manager: they do not describe the state it follows (for example a stale publisher of another instance
 * of a Digital Twin with the same id) and could never be matched with a known description.
 */
//TODO: evaluate renaming the class to "cache" or "snapshot"
public class DigitalTwinStateReplica {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DigitalTwinStateReplica.class);

    /**
     * After this time a values event still waiting for its description is reported with a warning (it is not discarded)
     */
    private static final long PENDING_WARN_TIMEOUT_MS = 5000;

    private DigitalTwinStateDescription description = null;
    private DigitalTwinStateValues values = null;
    private Instant seedInstant = null;

    /**
     * Values events received before their description, in arrival order
     */
    private final List<WldtEvent<?>> pending = new ArrayList<>();
    private long lastPendingWarnMs = 0;

    /**
     * Initializes (or re-initializes) the replica with the description and the values received at synchronization
     * time. Pending values are dropped.
     * @param description the complete description
     * @param values the complete values
     */
    public synchronized void seed(DigitalTwinStateDescription description, DigitalTwinStateValues values) {
        this.description = description;
        this.values = values;
        this.seedInstant = values != null ? values.getEvaluationInstant() : null;
        this.pending.clear();
    }

    /**
     * Drops the content of the replica (for example when the Digital Twin is not synchronized anymore)
     */
    public synchronized void clear() {
        this.description = null;
        this.values = null;
        this.seedInstant = null;
        this.pending.clear();
    }

    /**
     * @return true if the replica has been seeded
     */
    public synchronized boolean isSeeded() {
        return description != null && values != null;
    }

    /**
     * @return the current complete description, null if the replica has not been seeded
     */
    public synchronized DigitalTwinStateDescription getDescription() {
        return description;
    }

    /**
     * @return the current complete (unfiltered) values, null if the replica has not been seeded
     */
    public synchronized DigitalTwinStateValues getValues() {
        return values;
    }

    /**
     * @return the number of values events waiting for their description
     */
    public synchronized int getPendingValuesCount() {
        return pending.size();
    }

    /**
     * Applies a description update event. The values are aligned to the new description.
     * @param event the event published on the description update topic (body = {@link DigitalTwinStateDescription})
     * @return true if the event has been applied, false otherwise (not a description event, foreign description, or
     * description that is not newer than the current one)
     */
    public synchronized boolean applyDescriptionEvent(WldtEvent<?> event) {

        if (event == null || !(event.getBody() instanceof DigitalTwinStateDescription))
            return false;

        DigitalTwinStateDescription newDescription = (DigitalTwinStateDescription) event.getBody();

        // Description of a different state manager
        if (description != null && !DigitalTwinStateDescription.sameLineage(newDescription.getId(), description.getId())) {
            logger.debug("Skipping description {} that does not belong to the followed state (current: {})", newDescription.getId(), description.getId());
            return false;
        }

        // A description that is not newer than the current one has already been received (it precedes the seeding)
        if (description != null && DigitalTwinStateDescription.compareIds(newDescription.getId(), description.getId()) <= 0) {
            logger.debug("Skipping description {} (current: {})", newDescription.getId(), description.getId());
            return false;
        }

        this.description = newDescription;
        this.values = (this.values != null ? this.values : DigitalTwinStateValues.empty(newDescription.getId())).alignTo(newDescription);

        return true;
    }

    /**
     * Applies a values update event (the body is a delta with only the changed values).
     * @param event the event published on the values update topic (body = {@link DigitalTwinStateValues})
     * @return true if the event has been applied, false otherwise (not a values event, foreign state, already
     * included in the seeded values, or referring to a description not received yet: in that last case it is kept and
     * processed with {@link #applyNextPendingValues()})
     */
    public synchronized boolean applyValuesEvent(WldtEvent<?> event) {

        if (event == null || !(event.getBody() instanceof DigitalTwinStateValues))
            return false;

        DigitalTwinStateValues delta = (DigitalTwinStateValues) event.getBody();

        // Values of a different state manager
        if (description != null && !DigitalTwinStateDescription.sameLineage(delta.getDescriptionId(), description.getId())) {
            logger.debug("Skipping values update of description {} that does not belong to the followed state (current: {})", delta.getDescriptionId(), description.getId());
            return false;
        }

        // Events already included in the seeded values
        if (seedInstant != null && delta.getEvaluationInstant() != null && !delta.getEvaluationInstant().isAfter(seedInstant)
                && description != null && DigitalTwinStateDescription.compareIds(delta.getDescriptionId(), description.getId()) <= 0) {
            logger.debug("Skipping values update (instant {}) already included in the seeded values", delta.getEvaluationInstant());
            return false;
        }

        // Unknown (newer) description or replica not seeded yet: keep the event, in arrival order
        if (!canApply(delta)) {
            pending.add(event);
            warnStalePending();
            return false;
        }

        apply(delta);
        return true;
    }

    /**
     * Applies the first (in arrival order) values event that was waiting for a description and can now be processed.
     * To be called, after a description has been applied, until it returns null.
     * @return the pending values event that has just been applied (the owner reads its changes from it), null if no
     * pending event can be applied
     */
    public synchronized WldtEvent<?> applyNextPendingValues() {
        Iterator<WldtEvent<?>> iterator = pending.iterator();
        while (iterator.hasNext()) {
            WldtEvent<?> event = iterator.next();
            DigitalTwinStateValues delta = (DigitalTwinStateValues) event.getBody();
            if (canApply(delta)) {
                iterator.remove();
                apply(delta);
                return event;
            }
        }
        return null;
    }

    private boolean canApply(DigitalTwinStateValues delta) {
        return description != null && values != null && DigitalTwinStateDescription.compareIds(delta.getDescriptionId(), description.getId()) <= 0;
    }

    private void apply(DigitalTwinStateValues delta) {
        this.values = this.values.applyUpdate(delta);
        // The complete values always refer to the current description
        if (!description.getId().equals(this.values.getDescriptionId()))
            this.values = this.values.alignTo(description);
    }

    private void warnStalePending() {
        long now = System.currentTimeMillis();
        if (now - lastPendingWarnMs < PENDING_WARN_TIMEOUT_MS)
            return;
        for (WldtEvent<?> event : pending)
            if (now - event.getCreationTimestamp() > PENDING_WARN_TIMEOUT_MS) {
                lastPendingWarnMs = now;
                logger.warn("Values update referring to description {} is waiting for its description since more than {} ms (current description: {})",
                        ((DigitalTwinStateValues) event.getBody()).getDescriptionId(), PENDING_WARN_TIMEOUT_MS, description != null ? description.getId() : null);
                return;
            }
    }
}
