
package br.com.fivecom.litoralfm.models.clima;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class WeatherNow implements Serializable {

    @SerializedName("data")
    @Expose
    public Data data;

    public class Data implements Serializable {
        @SerializedName("getWeatherNow")
        @Expose
        public List<GetWeatherNow> getWeatherNow;
    }

    public class GetWeatherNow implements Serializable {

        @SerializedName("data")
        @Expose
        private List<WeatherData> data = null;

        public List<WeatherData> getData() {
            return data;
        }

        public void setData(List<WeatherData> data) {
            this.data = data;
        }

    }

}
