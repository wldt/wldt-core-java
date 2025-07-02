# Configurable Physical and Digital Adapters

The WLDT library provides a native method to define Configurable Physical ad Digital Adapters specifying a
custom configuration class passed as parameter in the constructor.

Starting with the Physical Adapter created in the previous example ```TestPhysicalAdapter ``` instead of extending the
base class ```PhysicalAdapter``` we can extend now ```ConfigurablePhysicalAdapter<C>``` where ```C``` is the name of the
that we would like to use as configuration.

In our example we can create a simple configuration class called ```TestPhysicalAdapterConfiguration``` where we move
the constant variable used to implement the behaviour of our demo physical adapter. The resulting class will be the
following:

```java
public class TestPhysicalAdapterConfiguration {

    private final static int MESSAGE_UPDATE_TIME = 1000;
    private final static int MESSAGE_UPDATE_NUMBER = 10;
    private final static double TEMPERATURE_MIN_VALUE = 20;
    private final static double TEMPERATURE_MAX_VALUE = 30;

    private int messageUpdateTime = MESSAGE_UPDATE_TIME;

    private int messageUpdateNumber = MESSAGE_UPDATE_NUMBER;

    private double temperatureMinValue = TEMPERATURE_MIN_VALUE;

    private double temperatureMaxValue = TEMPERATURE_MAX_VALUE;

    public TestPhysicalAdapterConfiguration() {
    }

    public TestPhysicalAdapterConfiguration(int messageUpdateTime, int messageUpdateNumber, double temperatureMinValue, double temperatureMaxValue) {
        this.messageUpdateTime = messageUpdateTime;
        this.messageUpdateNumber = messageUpdateNumber;
        this.temperatureMinValue = temperatureMinValue;
        this.temperatureMaxValue = temperatureMaxValue;
    }

    public int getMessageUpdateTime() {
        return messageUpdateTime;
    }

    public void setMessageUpdateTime(int messageUpdateTime) {
        this.messageUpdateTime = messageUpdateTime;
    }

    public int getMessageUpdateNumber() {
        return messageUpdateNumber;
    }

    public void setMessageUpdateNumber(int messageUpdateNumber) {
        this.messageUpdateNumber = messageUpdateNumber;
    }

    public double getTemperatureMinValue() {
        return temperatureMinValue;
    }

    public void setTemperatureMinValue(double temperatureMinValue) {
        this.temperatureMinValue = temperatureMinValue;
    }

    public double getTemperatureMaxValue() {
        return temperatureMaxValue;
    }

    public void setTemperatureMaxValue(double temperatureMaxValue) {
        this.temperatureMaxValue = temperatureMaxValue;
    }
}
```

Now we can create or update our Physical Adapter extending ```ConfigurablePhysicalAdapter<DemoPhysicalAdapterConfiguration>```
as illustrated in the following snippet:

```java
public class TestPhysicalAdapter extends ConfigurablePhysicalAdapter<DemoPhysicalAdapterConfiguration> { 
  [...]
}
```

Extending this class also the constructor should be updated getting as a parameter the expected configuration instance.
Our constructor will be the following:

```java
public TestConfPhysicalAdapter(String id, DemoPhysicalAdapterConfiguration configuration) {
    super(id, configuration);
}
```

After that change since we removed and moved the used constant values into the new Configuration class we have also to
update the ```deviceEmulation()``` method having access to the configuration through the method ```getConfiguration()``` or ```this.getConfiguration()```
directly on the adapter.

```java
private Runnable deviceEmulation(){
    return () -> {
        try {


            System.out.println("[DemoPhysicalAdapter] -> Sleeping before Starting Physical Device Emulation ...");

            //Sleep 5 seconds to emulate device startup
            Thread.sleep(10000);

            System.out.println("[DemoPhysicalAdapter] -> Starting Physical Device Emulation ...");

            //Create a new random object to emulate temperature variations
            Random r = new Random();

            //Publish an initial Event for a normal condition
            publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(OVERHEATING_EVENT_KEY, "normal"));

            //Emulate the generation on 'n' temperature measurements
            for(int i = 0; i < getConfiguration().getMessageUpdateNumber(); i++){

                //Sleep to emulate sensor measurement
                Thread.sleep(getConfiguration().getMessageUpdateTime());

                //Update the
                double randomTemperature = getConfiguration().getTemperatureMinValue() + (getConfiguration().getTemperatureMaxValue() - getConfiguration().getTemperatureMinValue()) * r.nextDouble();

                //Create a new event to notify the variation of a Physical Property
                PhysicalAssetPropertyWldtEvent<Double> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(TEMPERATURE_PROPERTY_KEY, randomTemperature);

                //Publish the WLDTEvent associated to the Physical Property Variation
                publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
            }

            //Publish a demo Physical Event associated to a 'critical' overheating condition
            publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(OVERHEATING_EVENT_KEY, "critical"));

        } catch (EventBusException | InterruptedException e) {
            e.printStackTrace();
        }
    };
}
```

A similar approach can be adopted also for the Digital Adapter with the small difference that the base class
```DigitalAdapter``` already allow the possibility to specify a configuration. For this reason in the previous example
we extended ```DigitalAdapter<Void>``` avoiding to specifying a configuration.

In this updated version we can create a new ```TestDigitalAdapterConfiguration``` class containing the parameter association
to the emulation of the action and then update our adapter to support the new configuration. Our new configuration class will be:

```java
public class TestDigitalAdapterConfiguration {

  private static final int SLEEP_TIME_MS = 1000;

  private static final int EMULATED_ACTION_COUNT = 5;

  private final static double TEMPERATURE_MIN_VALUE = 20;

  private final static double TEMPERATURE_MAX_VALUE = 30;

  private int sleepTimeMs = SLEEP_TIME_MS;

  private int emulatedActionCount = EMULATED_ACTION_COUNT;

  private double temperatureMinValue = TEMPERATURE_MIN_VALUE;

  private double temperatureMaxValue = TEMPERATURE_MAX_VALUE;

  public TestDigitalAdapterConfiguration() {
  }

  public TestDigitalAdapterConfiguration(int sleepTimeMs, int emulatedActionCount, double temperatureMinValue, double temperatureMaxValue) {
    this.sleepTimeMs = sleepTimeMs;
    this.emulatedActionCount = emulatedActionCount;
    this.temperatureMinValue = temperatureMinValue;
    this.temperatureMaxValue = temperatureMaxValue;
  }

  public int getSleepTimeMs() {
    return sleepTimeMs;
  }

  public void setSleepTimeMs(int sleepTimeMs) {
    this.sleepTimeMs = sleepTimeMs;
  }

  public int getEmulatedActionCount() {
    return emulatedActionCount;
  }

  public void setEmulatedActionCount(int emulatedActionCount) {
    this.emulatedActionCount = emulatedActionCount;
  }

  public double getTemperatureMinValue() {
    return temperatureMinValue;
  }

  public void setTemperatureMinValue(double temperatureMinValue) {
    this.temperatureMinValue = temperatureMinValue;
  }

  public double getTemperatureMaxValue() {
    return temperatureMaxValue;
  }

  public void setTemperatureMaxValue(double temperatureMaxValue) {
    this.temperatureMaxValue = temperatureMaxValue;
  }
}
```

After that we can update the declaration of our Digital Adapter and modify its constructor to accept the configuration.
The resulting class will be:

```java
public class TestDigitalAdapter extends DigitalAdapter<TestDigitalAdapterConfiguration> { 
  
  public TestDigitalAdapter(String id, TestDigitalAdapterConfiguration configuration) {
    super(id, configuration);
  }
  
  [...]
}
```

Of course the possibility to have this configuration will allow us to improve the ```emulateIncomingDigitalAction``` method
in the following way having access to the configuration through the method ```getConfiguration()``` or ```this.getConfiguration()```
directly on the adapter:

```java
private Runnable emulateIncomingDigitalAction(){
    return () -> {
        try {

            System.out.println("[DemoDigitalAdapter] -> Sleeping before Emulating Incoming Digital Action ...");
            Thread.sleep(5000);
            Random random = new Random();

            //Emulate the generation on 'n' temperature measurements
            for(int i = 0; i < getConfiguration().getEmulatedActionCount(); i++){

                //Sleep to emulate sensor measurement
                Thread.sleep(getConfiguration().getSleepTimeMs());

                double randomTemperature = getConfiguration().getTemperatureMinValue() + (getConfiguration().getTemperatureMaxValue() - getConfiguration().getTemperatureMinValue()) * random.nextDouble();
                publishDigitalActionWldtEvent("set-temperature-action-key", randomTemperature);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    };
}
```

When we have updated both adapters making them configurable we can update our ```main``` function in the process
that we have previouly device using the updated adapters and passing the configurations:

````java
public class TestDigitalTwin {

    public static void main(String[] args)  {
        try{

            WldtEngine digitalTwinEngine = new WldtEngine(new DemoShadowingFunction("test-shadowing-function"), "test-digital-twin");

            //Default Physical and Digital Adapter
            //digitalTwinEngine.addPhysicalAdapter(new TestPhysicalAdapter("test-physical-adapter"));
            //digitalTwinEngine.addDigitalAdapter(new TestDigitalAdapter("test-digital-adapter"));

            //Physical and Digital Adapters with Configuration
            digitalTwinEngine.addPhysicalAdapter(new TestConfPhysicalAdapter("test-physical-adapter", new TestPhysicalAdapterConfiguration()));
            digitalTwinEngine.addDigitalAdapter(new TestConfDigitalAdapter("test-digital-adapter", new TestDigitalAdapterConfiguration()));

            digitalTwinEngine.startLifeCycle();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
````