package it.wldt.augmentation.result;

import it.wldt.augmentation.error.AugmentationFunctionError;

import java.util.*;
import java.util.function.Consumer;

/**
 * A typed list of {@link AugmentationFunctionResult} items that can also carry an
 * {@link AugmentationFunctionError}.
 *
 * <p>When an error is set, all mutating operations (add, set, remove, clear …) are
 * disabled and throw an {@link IllegalStateException}.  Read-only operations remain
 * available so callers can still inspect any partial state that was present before
 * the error was recorded.
 *
 * <p>This design makes the error condition explicit: if the list is in an error
 * state the library will ignore its contents anyway, and any attempt to write into
 * it is a programming mistake that should be surfaced immediately.
 */
public class AugmentationFunctionResultList extends ArrayList<AugmentationFunctionResult<?>> {

    /**
     * If non-null, this list is in an error state and all mutating operations will throw
     * {@link IllegalStateException}.  The library will ignore the contents of this list when an error is present,
     * so the error condition must be handled by the caller and is not recoverable by the library.
     */
    private AugmentationFunctionError augmentationFunctionError = null;

    /**
     * Creates an empty AugmentationFunctionResultList with no error.
     */
    public AugmentationFunctionResultList() {
        super();
    }

    /**
     * Creates an AugmentationFunctionResultList containing a single result, with no error.
     * @param augmentationFunctionResult the single result to include in this list
     */
    public AugmentationFunctionResultList(AugmentationFunctionResult<?> augmentationFunctionResult) {
        super(Collections.singletonList(augmentationFunctionResult));
    }

    /**
     * Creates an AugmentationFunctionResultList in an error state with the given error.  All mutating operations
     * will throw {@link IllegalStateException} until the error is cleared.
     * @param augmentationFunctionError the error to associate with this list, or {@code null} for no error
     */
    public AugmentationFunctionResultList(AugmentationFunctionError augmentationFunctionError) {
        super();
        this.augmentationFunctionError = augmentationFunctionError;
    }

    /**
     * Throws {@link IllegalStateException} if an {@link AugmentationFunctionError}
     * has been set on this list, preventing any further mutation.
     */
    private void guardNoError() {
        if (augmentationFunctionError != null) {
            throw new IllegalStateException(
                "AugmentationFunctionResultList is in an error state (" +
                augmentationFunctionError + ") – mutating operations are not allowed. " +
                "Results will be ignored by the library when an error is present."
            );
        }
    }

    @Override public int size()                        { return super.size(); }
    @Override public boolean isEmpty()                 { return super.isEmpty(); }
    @Override public boolean contains(Object o)        { return super.contains(o); }
    @Override public int indexOf(Object o)             { return super.indexOf(o); }
    @Override public int lastIndexOf(Object o)         { return super.lastIndexOf(o); }
    @Override public Object clone()                    { return super.clone(); }
    @Override public AugmentationFunctionResult<?> getFirst() { return super.getFirst(); }
    @Override public AugmentationFunctionResult<?> getLast()  { return super.getLast(); }
    @Override public AugmentationFunctionResult<?> get(int index) { return super.get(index); }

    @Override
    public void forEach(Consumer<? super AugmentationFunctionResult<?>> action) {
        super.forEach(action);
    }

    @Override public int hashCode()          { return super.hashCode(); }
    @Override public boolean equals(Object o) { return super.equals(o); }

    @Override
    public boolean add(AugmentationFunctionResult<?> augmentationFunctionResult) {
        guardNoError();
        return super.add(augmentationFunctionResult);
    }

    @Override
    public void add(int index, AugmentationFunctionResult<?> element) {
        guardNoError();
        super.add(index, element);
    }

    @Override
    public void addFirst(AugmentationFunctionResult<?> element) {
        guardNoError();
        super.addFirst(element);
    }

    @Override
    public void addLast(AugmentationFunctionResult<?> element) {
        guardNoError();
        super.addLast(element);
    }

    @Override
    public boolean addAll(Collection<? extends AugmentationFunctionResult<?>> c) {
        guardNoError();
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends AugmentationFunctionResult<?>> c) {
        guardNoError();
        return super.addAll(index, c);
    }

    @Override
    public AugmentationFunctionResult<?> set(int index, AugmentationFunctionResult<?> element) {
        guardNoError();
        return super.set(index, element);
    }

    @Override
    public boolean remove(Object o) {
        guardNoError();
        return super.remove(o);
    }

    @Override
    public AugmentationFunctionResult<?> remove(int index) {
        guardNoError();
        return super.remove(index);
    }

    @Override
    public AugmentationFunctionResult<?> removeFirst() {
        guardNoError();
        return super.removeFirst();
    }

    @Override
    public AugmentationFunctionResult<?> removeLast() {
        guardNoError();
        return super.removeLast();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        guardNoError();
        return super.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        guardNoError();
        return super.retainAll(c);
    }

    @Override
    public void clear() {
        guardNoError();
        super.clear();
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        guardNoError();
        super.removeRange(fromIndex, toIndex);
    }

    /** @return {@code true} if this list is in an error state. */
    public boolean hasError() {
        return augmentationFunctionError != null;
    }

    /**
     * Returns the {@link AugmentationFunctionError} associated with this list, or
     * {@code null} if no error is present.
     * @return the error associated with this list, or {@code null} if no error is present
     */
    public AugmentationFunctionError getAugmentationFunctionError() {
        return augmentationFunctionError;
    }

    /**
     * Sets the error on this list.  Once set, all mutating operations will throw
     * {@link IllegalStateException} until the error is cleared.
     * @param augmentationFunctionError the error to associate with this list, or {@code null} to clear any existing error
     */
    public void setAugmentationFunctionError(AugmentationFunctionError augmentationFunctionError) {
        this.augmentationFunctionError = augmentationFunctionError;
    }
}
