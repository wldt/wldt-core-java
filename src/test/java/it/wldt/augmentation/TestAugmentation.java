/*
 * Copyright [2025] [Marco Picone, Ph.D. - picone.m@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Marco Picone <picone.m@gmail.com> - https://www.marcopicone.net/
 */
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
