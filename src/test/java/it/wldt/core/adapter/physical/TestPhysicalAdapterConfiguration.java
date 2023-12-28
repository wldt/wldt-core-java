package it.wldt.core.adapter.physical;

public class TestPhysicalAdapterConfiguration {

    private String brokerIp = "127.0.0.1";

    private int brokerPort = 1883;

    private String username = "demo";

    private String password = "demo";

    public TestPhysicalAdapterConfiguration() {
    }

    public TestPhysicalAdapterConfiguration(String brokerIp, int brokerPort, String username, String password) {
        this.brokerIp = brokerIp;
        this.brokerPort = brokerPort;
        this.username = username;
        this.password = password;
    }

    public String getBrokerIp() {
        return brokerIp;
    }

    public void setBrokerIp(String brokerIp) {
        this.brokerIp = brokerIp;
    }

    public int getBrokerPort() {
        return brokerPort;
    }

    public void setBrokerPort(int brokerPort) {
        this.brokerPort = brokerPort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DemoPhysicalAdapterConfiguration{");
        sb.append("brokerIp='").append(brokerIp).append('\'');
        sb.append(", brokerPort=").append(brokerPort);
        sb.append(", username='").append(username).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
