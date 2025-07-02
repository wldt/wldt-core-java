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
