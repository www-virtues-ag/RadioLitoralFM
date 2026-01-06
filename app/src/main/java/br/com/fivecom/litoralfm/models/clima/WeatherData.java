
package br.com.fivecom.litoralfm.models.clima;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WeatherData implements Serializable {

    @SerializedName("locale")
    @Expose
    private Locale locale;
    @SerializedName("weather")
    @Expose
    private Weather weather = null;

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

}
