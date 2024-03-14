package it.wldt.augmentation;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 */
public interface AugmentationFunctionListener<T> {

    public void onFunctionResult(T result);

    public void onFunctionError(String errorMessage);

}
