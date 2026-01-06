
package br.com.fivecom.litoralfm.models.clima;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Weather implements Serializable {

    @SerializedName("temperature")
    @Expose
    private int temperature;
    @SerializedName("icon")
    @Expose
    private String icon;
    @SerializedName("condition")
    @Expose
    private String condition;
    @SerializedName("humidity")
    @Expose
    private int humidity;
    @SerializedName("sensation")
    @Expose
    private int sensation;
    @SerializedName("windVelocity")
    @Expose
    private int windVelocity;
    @SerializedName("pressure")
    @Expose
    private int pressure;
    @SerializedName("date")
    @Expose
    private String date;

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getSensation() {
        return sensation;
    }

    public void setSensation(int sensation) {
        this.sensation = sensation;
    }

    public int getWindVelocity() {
        return windVelocity;
    }

    public void setWindVelocity(int windVelocity) {
        this.windVelocity = windVelocity;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
