package it.wldt.augmentation;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 */
public class TestAugmentation {

    public static void main(String[] args) {

        AverageHrAugmentationFunction avgTest1 = new AverageHrAugmentationFunction("TestFunction1", 10, new AugmentationFunctionListener<Double>() {
            @Override
            public void onFunctionResult(Double result) {
                System.out.println("Function Result:" + result);
            }

            @Override
            public void onFunctionError(String errorMessage) {
                System.out.println("Function Error ! Msg:" + errorMessage);
            }
        });

        AverageHrAugmentationFunction avgTest2 = new AverageHrAugmentationFunction("TestFunction2", 10, new AugmentationFunctionListener<Double>() {
            @Override
            public void onFunctionResult(Double result) {
                System.out.println("Function2 Result:" + result);
            }

            @Override
            public void onFunctionError(String errorMessage) {
                System.out.println("Function2 Error ! Msg:" + errorMessage);
            }
        });

        AverageHrAugmentationFunction avgTest3 = new AverageHrAugmentationFunction("TestFunction3", 10, new AugmentationFunctionListener<Double>() {
            @Override
            public void onFunctionResult(Double result) {
                System.out.println("Function3 Result:" + result);
            }

            @Override
            public void onFunctionError(String errorMessage) {
                System.out.println("Function3 Error ! Msg:" + errorMessage);
            }
        });


        AugmentationFunctionExecutor.getInstance().addFunction(avgTest1);
        AugmentationFunctionExecutor.getInstance().addFunction(avgTest2);
        AugmentationFunctionExecutor.getInstance().addFunction(avgTest3);

        AugmentationFunctionExecutor.getInstance().execute();

    }

}
