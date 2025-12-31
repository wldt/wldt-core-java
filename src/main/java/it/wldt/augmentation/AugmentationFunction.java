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
