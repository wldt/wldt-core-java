package it.wldt.augmentation;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 26/10/2023 - 15:52
 */
public class AverageHrAugmentationFunction extends AugmentationFunction<Double>{

    private int secTimeWindow;

    public AverageHrAugmentationFunction(String id, int secTimeWindow, AugmentationFunctionListener<Double> listener) {
        super(id, listener);
        this.secTimeWindow = secTimeWindow;
    }

    @Override
    public Double execute() {
        return 25.0;
    }

    public int getSecTimeWindow() {
        return secTimeWindow;
    }

    public void setSecTimeWindow(int secTimeWindow) {
        this.secTimeWindow = secTimeWindow;
    }
}
