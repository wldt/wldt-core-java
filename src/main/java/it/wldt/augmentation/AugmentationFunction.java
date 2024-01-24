package it.wldt.augmentation;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project HelloJava
 * @created 26/10/2023 - 15:43
 */
public abstract class AugmentationFunction<T> implements Runnable {

    private String id;

    private AugmentationFunctionListener<T> listener;

    private AugmentationFunction(){

    }

    public AugmentationFunction(String id, AugmentationFunctionListener<T> listener) {
        this.id = id;
        this.listener = listener;
    }

    @Override
    public void run() {
        try{

            T result = execute();
            if(this.listener != null)
                this.listener.onFunctionResult(result);

        }catch (Exception e){
            if(this.listener != null)
                this.listener.onFunctionError(e.getLocalizedMessage());
            //TODO Else + Log

        }
    }

    public abstract T execute();
}
