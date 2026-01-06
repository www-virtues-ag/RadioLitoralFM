package br.com.fivecom.litoralfm.models;

public class DeviceItem {

    // Connection states
    public enum ConnectionState {
        AVAILABLE,      // Device is available but not connected
        CONNECTING,     // Currently connecting to device
        CONNECTED,      // Successfully connected
        DISCONNECTING   // Currently disconnecting
    }

    private String name;
    private String id;
    private boolean isConnected;
    private ConnectionState connectionState;
    private String type; // "chromecast" or "bluetooth"
    private String status; // For Bluetooth: "Paired" or "Unpaired"
    private int icon; // Icon resource ID
    private String address; // Device address
    private boolean isPlaying; // For Chromecast: playback state

    // Constructor for Chromecast devices
    public DeviceItem(String name, String id, boolean isConnected, String type) {
        this.name = name;
        this.id = id;
        this.isConnected = isConnected;
        this.type = type;
        this.connectionState = isConnected ? ConnectionState.CONNECTED : ConnectionState.AVAILABLE;
        this.isPlaying = false;
    }

    // Constructor for Bluetooth devices
    public DeviceItem(String name, String status, int icon, String address) {
        this.name = name;
        this.status = status;
        this.icon = icon;
        this.address = address;
        this.type = "bluetooth";
        this.isConnected = status != null && status.contains("Pair");
        this.connectionState = this.isConnected ? ConnectionState.CONNECTED : ConnectionState.AVAILABLE;
        this.isPlaying = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getIconResource() {
        return icon;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(ConnectionState connectionState) {
        this.connectionState = connectionState;
        // Update isConnected based on state
        this.isConnected = (connectionState == ConnectionState.CONNECTED);
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DeviceItem that = (DeviceItem) obj;
        if (address != null) {
            return address.equals(that.address);
        }
        if (id != null) {
            return id.equals(that.id);
        }
        return name != null && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        if (address != null) return address.hashCode();
        if (id != null) return id.hashCode();
        return name != null ? name.hashCode() : 0;
    }
}

