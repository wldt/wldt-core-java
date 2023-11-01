package it.wldt.augmentation;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 26/10/2023 - 15:54
 */
public interface AugmentationFunctionListener<T> {

    public void onFunctionResult(T result);

    public void onFunctionError(String errorMessage);

}
