/**
 * Annotations for marking and categorizing Digital Twin Shadowing Function methods.
 * <p>
 * This package provides metadata annotations that formalize the shadowing function
 * architecture, enabling:
 * <ul>
 *   <li>Machine-readable documentation of shadowing behavior</li>
 *   <li>Runtime introspection and validation tools</li>
 *   <li>Automated metrics collection and observability</li>
 *   <li>Code generation and scaffolding utilities</li>
 * </ul>
 *
 * <h2>Core Annotations</h2>
 * <ul>
 *   <li>{@link it.wldt.core.model.annotation.ShadowingFunction} -
 *       Marks methods as shadowing function callbacks</li>
 *   <li>{@link it.wldt.core.model.annotation.ShadowingType} -
 *       Categorizes shadowing operations by event source</li>
 * </ul>
 *
 * @see it.wldt.core.model.DigitalTwinModel
 * @since 0.4.0
 */
package it.wldt.core.model.annotation;