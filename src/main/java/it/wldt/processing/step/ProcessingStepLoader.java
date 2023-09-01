package it.wldt.processing.step;

import it.wldt.processing.ProcessingStep;

import javax.inject.Named;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Deprecated
/**
 * Author:
 *      Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/03/2023
 *
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class ProcessingStepLoader {

    private String DEFAULT_PACKAGE_NAME = "it.unimore.dipi.iot.wldt.processing.step";

    private List<Class<ProcessingStep>> loadedClasses = null;

    public ProcessingStepLoader() {
        try {
            loadExistingHandler();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public ProcessingStep loadAnnotated(String annotationValue) {

        try {

            Class<ProcessingStep> myServiceSubscriptionHandler = this.loadedClasses.stream().filter(aClass -> {
                Named myAnnotation = aClass.getAnnotation(Named.class);
                if (myAnnotation != null && myAnnotation.value() != null && myAnnotation.value().equals(annotationValue))
                    return true;
                else
                    return false;
            }).findFirst().get();

            return myServiceSubscriptionHandler.getDeclaredConstructor().newInstance();

        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void loadExistingHandler() throws ClassNotFoundException {

        // Prepare.
        String packageName = DEFAULT_PACKAGE_NAME;
        this.loadedClasses = new ArrayList<Class<ProcessingStep>>();
        URL root = Thread.currentThread().getContextClassLoader().getResource(packageName.replace(".", "/"));

        // Filter .class files.
        File[] files = new File(root.getFile()).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".class");
            }
        });

        if(files != null && files.length > 0){

            // Find classes implementing ICommand.
            for (File file : files) {
                String className = file.getName().replaceAll(".class$", "");
                Class<?> cls = Class.forName(packageName + "." + className);
                if (ProcessingStep.class.isAssignableFrom(cls)) {
                    this.loadedClasses.add((Class<ProcessingStep>) cls);
                }
            }

        }
    }

}
