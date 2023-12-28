package it.wldt.process;

import it.wldt.core.engine.WldtEngine;
import it.wldt.process.digital.DemoDigitalAdapter;
import it.wldt.process.digital.DemoDigitalAdapterConfiguration;
import it.wldt.process.physical.DemoPhysicalAdapter;
import it.wldt.process.physical.DemoPhysicalAdapterConfiguration;
import it.wldt.process.shadowing.DemoShadowingFunction;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 28/12/2023 - 15:10
 */
public class TestProcess {

    public static void main(String[] args)  {
        try{

            WldtEngine digitalTwinEngine = new WldtEngine(new DemoShadowingFunction(), "test-digital-twin");

            //Default Physical and Digital Adapter
            //digitalTwinEngine.addPhysicalAdapter(new DemoPhysicalAdapter("test-physical-adapter"));
            //digitalTwinEngine.addDigitalAdapter(new DemoDigitalAdapter("test-digital-adapter"));

            //Physical and Digital Adapters with Configuration
            digitalTwinEngine.addPhysicalAdapter(new DemoPhysicalAdapter("test-physical-adapter", new DemoPhysicalAdapterConfiguration(), true));
            digitalTwinEngine.addDigitalAdapter(new DemoDigitalAdapter("test-digital-adapter", new DemoDigitalAdapterConfiguration()));

            digitalTwinEngine.startLifeCycle();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
