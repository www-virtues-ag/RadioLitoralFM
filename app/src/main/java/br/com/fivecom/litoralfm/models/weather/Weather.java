package br.com.fivecom.litoralfm.models.weather;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

import br.com.fivecom.litoralfm.R;

public class Weather {

    @SerializedName("msHealth")
    @Expose
    public MsHealth msHealth;
    @SerializedName("currentWeather")
    @Expose
    public CurrentWeather currentWeather;
    @SerializedName("nearbyAirports")
    @Expose
    public List<NearbyAirport> nearbyAirports;
    @SerializedName("nearbyCities")
    @Expose
    public List<NearbyCity> nearbyCities;
    @SerializedName("volumeOfRain")
    @Expose
    public VolumeOfRain volumeOfRain;
    @SerializedName("dailyForecast")
    @Expose
    public List<DailyForecast> dailyForecast;
    @SerializedName("hourlyForecast")
    @Expose
    public Map<String, List<HourlyForecast>> hourlyForecast;

    public class Afternoon {
        @SerializedName("min")
        @Expose
        public Integer min;
        @SerializedName("max")
        @Expose
        public Integer max;
    }


    public class AirQuality {
        @SerializedName("condition")
        @Expose
        public String condition;
        @SerializedName("incidence")
        @Expose
        public Integer incidence;
    }

    public class Cold {
        @SerializedName("condition")
        @Expose
        public String condition;
        @SerializedName("incidence")
        @Expose
        public Integer incidence;
    }

    public class Cold__1 {
        @SerializedName("condition")
        @Expose
        public Object condition;
    }

    public class CurrentWeather {
        @SerializedName("id")
        @Expose
        public Integer id;
        @SerializedName("name")
        @Expose
        public String name;
        @SerializedName("date")
        @Expose
        public String date;
        @SerializedName("dayWeek")
        @Expose
        public String dayWeek;
        @SerializedName("dateUpdate")
        @Expose
        public String dateUpdate;
        @SerializedName("temperature")
        @Expose
        public Integer temperature;
        @SerializedName("windDirection")
        @Expose
        public String windDirection;
        @SerializedName("windVelocity")
        @Expose
        public Integer windVelocity;
        @SerializedName("windDirectionDegrees")
        @Expose
        public Integer windDirectionDegrees;
        @SerializedName("humidity")
        @Expose
        public Integer humidity;
        @SerializedName("condition")
        @Expose
        public String condition;
        @SerializedName("pressure")
        @Expose
        public Integer pressure;
        @SerializedName("icon")
        @Expose
        public String icon;
        @SerializedName("iconClass")
        @Expose
        public List<String> iconClass;
        @SerializedName("sensation")
        @Expose
        public Integer sensation;
        @SerializedName("slugCondition")
        @Expose
        public String slugCondition;

        public int getIcon(){
            switch (icon) {
                case "0":
                case "1":
                case "1a":
                    return R.drawable.ic_weather_sol;
                case "2":
                case "2a":
                    return R.drawable.ic_weather_nublado;
                case "3":
                case "3a":
                    return R.drawable.ic_weather_chuva;
                case "4":
                case "4a":
                    return R.drawable.ic_weather_sol_entre_nuvens;
                case "5":
                case "5a":
                    return R.drawable.ic_weather_chuva;
                case "6":
                case "6a":
                    return R.drawable.ic_weather_chuva;
                case "7":
                case "7a":
                    return R.drawable.ic_weather_nublado;
                case "8":
                case "8a":
                    return R.drawable.ic_weather_nublado;
                case "9":
                case "9a":
                    return R.drawable.ic_weather_nublado;
                default:
                    return R.drawable.ic_weather_sol;
            }
        }
    }

    public class DailyForecast {
        @SerializedName("date")
        @Expose
        public String date;
        @SerializedName("sun")
        @Expose
        public Sun sun;
        @SerializedName("textIcon")
        @Expose
        public TextIcon textIcon;
        @SerializedName("temperature")
        @Expose
        public Temperature temperature;
        @SerializedName("thermalSensation")
        @Expose
        public ThermalSensation thermalSensation;
        @SerializedName("rain")
        @Expose
        public Rain rain;
        @SerializedName("wind")
        @Expose
        public Wind wind;
        @SerializedName("humidity")
        @Expose
        public Humidity humidity;
        @SerializedName("rainbow")
        @Expose
        public Rainbow rainbow;
        @SerializedName("uv")
        @Expose
        public Uv uv;
        @SerializedName("mosquito")
        @Expose
        public Mosquito__1 mosquito;
        @SerializedName("skindryness")
        @Expose
        public Skindryness__1 skindryness;
        @SerializedName("cold")
        @Expose
        public Cold__1 cold;
        @SerializedName("flu")
        @Expose
        public Flu__1 flu;
        @SerializedName("wave")
        @Expose
        public Wave wave;
        @SerializedName("moonPhases")
        @Expose
        public MoonPhases moonPhases;
        @SerializedName("day")
        @Expose
        public String day;
        @SerializedName("dayWeek")
        @Expose
        public String dayWeek;
        @SerializedName("dayWeekFull")
        @Expose
        public String dayWeekFull;
        @SerializedName("dayWeekFullMin")
        @Expose
        public String dayWeekFullMin;
        @SerializedName("hasTemperatureAlert")
        @Expose
        public Boolean hasTemperatureAlert;
        @SerializedName("weekdayPhrase")
        @Expose
        public Object weekdayPhrase;
    }


    public class Dawn {
        @SerializedName("min")
        @Expose
        public Integer min;
        @SerializedName("max")
        @Expose
        public Integer max;
    }


    public class Flu {
        @SerializedName("condition")
        @Expose
        public String condition;
        @SerializedName("incidence")
        @Expose
        public Integer incidence;
    }


    public class Flu__1 {
        @SerializedName("condition")
        @Expose
        public Object condition;
    }


    public class HourlyForecast {
        @SerializedName("date")
        @Expose
        public String date;
        @SerializedName("textIcon")
        @Expose
        public TextIcon textIcon;
        @SerializedName("wind")
        @Expose
        public Wind__1 wind;
        @SerializedName("temperature")
        @Expose
        public Temperature__1 temperature;
        @SerializedName("rain")
        @Expose
        public Rain__1 rain;
        @SerializedName("rain2")
        @Expose
        public Rain rain2;
        @SerializedName("pressure")
        @Expose
        public Pressure pressure;
        @SerializedName("humidity")
        @Expose
        public Humidity__1 humidity;
        @SerializedName("icon")
        @Expose
        public Icon__1 icon;
        @SerializedName("uf1")
        public String uf;
    }

    public class Humidity {
        @SerializedName("min")
        @Expose
        public Integer min;
        @SerializedName("max")
        @Expose
        public Integer max;

    }

    public class Humidity__1 {
        @SerializedName("relativeHumidity")
        @Expose
        public Integer relativeHumidity;
    }

    public class Icon {
        @SerializedName("dawn")
        @Expose
        public String dawn;
        @SerializedName("morning")
        @Expose
        public String morning;
        @SerializedName("afternoon")
        @Expose
        public String afternoon;
        @SerializedName("night")
        @Expose
        public String night;
        @SerializedName("day")
        @Expose
        public String day;
    }


    public class Icon__1 {
        @SerializedName("resource")
        @Expose
        public String resource;
    }


    public class Info {
        @SerializedName("idlocale")
        @Expose
        public Integer idlocale;
        @SerializedName("idairport")
        @Expose
        public Integer idairport;
        @SerializedName("idcity")
        @Expose
        public Integer idcity;
        @SerializedName("uf")
        @Expose
        public String uf;
        @SerializedName("airport")
        @Expose
        public String airport;
        @SerializedName("region")
        @Expose
        public String region;
        @SerializedName("longitude")
        @Expose
        public Double longitude;
        @SerializedName("latitude")
        @Expose
        public Double latitude;
        @SerializedName("distance")
        @Expose
        public Double distance;
        @SerializedName("slugAirport")
        @Expose
        public String slugAirport;
    }


    public class Iuv {
        @SerializedName("condition")
        @Expose
        public String condition;
        @SerializedName("incidence")
        @Expose
        public Integer incidence;

    }


    public class MoonPhases {
        @SerializedName("date")
        @Expose
        public String date;
        @SerializedName("phase")
        @Expose
        public String phase;
        @SerializedName("dateFormatted")
        @Expose
        public String dateFormatted;
        @SerializedName("state")
        @Expose
        public String state;
    }


    public class Morning {
        @SerializedName("min")
        @Expose
        public Integer min;
        @SerializedName("max")
        @Expose
        public Integer max;
    }


    public class Mosquito {
        @SerializedName("condition")
        @Expose
        public String condition;
        @SerializedName("incidence")
        @Expose
        public Integer incidence;
    }


    public class Mosquito__1 {
        @SerializedName("condition")
        @Expose
        public Object condition;
    }


    public class MsHealth {
        @SerializedName("mosquito")
        @Expose
        public Mosquito mosquito;
        @SerializedName("skindryness")
        @Expose
        public Skindryness skindryness;
        @SerializedName("cold")
        @Expose
        public Cold cold;
        @SerializedName("flu")
        @Expose
        public Flu flu;
        @SerializedName("iuv")
        @Expose
        public Iuv iuv;
        @SerializedName("vitaminD")
        @Expose
        public VitaminD vitaminD;
        @SerializedName("airQuality")
        @Expose
        public AirQuality airQuality;
    }

    public class NearbyAirport {
        @SerializedName("info")
        @Expose
        public Info info;
        @SerializedName("weather")
        @Expose
        public Weather__1 weather;
    }


    public class NearbyCity {
        @SerializedName("idlocale")
        @Expose
        public Integer idlocale;
        @SerializedName("idcity")
        @Expose
        public Integer idcity;
        @SerializedName("capital")
        @Expose
        public Boolean capital;
        @SerializedName("idcountry")
        @Expose
        public Integer idcountry;
        @SerializedName("ac")
        @Expose
        public String ac;
        @SerializedName("country")
        @Expose
        public String country;
        @SerializedName("uf")
        @Expose
        public String uf;
        @SerializedName("city")
        @Expose
        public String city;
        @SerializedName("region")
        @Expose
        public String region;
        @SerializedName("seaside")
        @Expose
        public Boolean seaside;
        @SerializedName("latitude")
        @Expose
        public Double latitude;
        @SerializedName("longitude")
        @Expose
        public Double longitude;
        @SerializedName("tourist")
        @Expose
        public Boolean tourist;
        @SerializedName("agricultural")
        @Expose
        public Boolean agricultural;
        @SerializedName("distance")
        @Expose
        public Double distance;
        @SerializedName("slugCity")
        @Expose
        public String slugCity;
        @SerializedName("momentIcon")
        @Expose
        public String momentIcon;
        @SerializedName("momentTemperature")
        @Expose
        public Integer momentTemperature;
        @SerializedName("momentSensation")
        @Expose
        public Integer momentSensation;

    }

    public class Night {
        @SerializedName("min")
        @Expose
        public Integer min;
        @SerializedName("max")
        @Expose
        public Integer max;

    }

    public class Pressure {
        @SerializedName("pressure")
        @Expose
        public Integer pressure;
    }

    public class Rain {
        @SerializedName("probability")
        @Expose
        public Integer probability;
        @SerializedName("precipitation")
        @Expose
        public String precipitation;
    }

    public class Rain__1 {
        @SerializedName("precipitation")
        @Expose
        public String precipitation;
    }

    public class Rainbow {
        @SerializedName("text")
        @Expose
        public String text;
    }

    public class Skindryness {
        @SerializedName("condition")
        @Expose
        public String condition;
        @SerializedName("incidence")
        @Expose
        public Integer incidence;
    }

    public class Skindryness__1 {
        @SerializedName("condition")
        @Expose
        public Object condition;

    }

    public class Sun {
        @SerializedName("sunshine")
        @Expose
        public String sunshine;
        @SerializedName("sunset")
        @Expose
        public String sunset;
    }


    public class Temperature {
        @SerializedName("min")
        @Expose
        public Integer min;
        @SerializedName("max")
        @Expose
        public Integer max;
        @SerializedName("dawn")
        @Expose
        public Dawn dawn;
        @SerializedName("morning")
        @Expose
        public Morning morning;
        @SerializedName("afternoon")
        @Expose
        public Afternoon afternoon;
        @SerializedName("night")
        @Expose
        public Night night;
    }


    public class Temperature__1 {
        @SerializedName("temperature")
        @Expose
        public Integer temperature;
    }

    public class Text {
        @SerializedName("pt")
        @Expose
        public String pt;
        @SerializedName("en")
        @Expose
        public String en;
        @SerializedName("es")
        @Expose
        public String es;
        @SerializedName("reducedPhrase")
        @Expose
        public String reducedPhrase;
        @SerializedName("morningPhrase")
        @Expose
        public String morningPhrase;
        @SerializedName("afternoonPhrase")
        @Expose
        public String afternoonPhrase;
        @SerializedName("nightPhrase")
        @Expose
        public String nightPhrase;
        @SerializedName("dawnPhrase")
        @Expose
        public String dawnPhrase;
    }

    public class TextIcon {
        @SerializedName("icon")
        @Expose
        public Icon icon;
        @SerializedName("text")
        @Expose
        public Text text;
        @SerializedName("iconClass")
        @Expose
        public Object iconClass;

    }

    public class ThermalSensation {
        @SerializedName("thermalSensation")
        @Expose
        public Object thermalSensation;
        @SerializedName("thermalSensationMin")
        @Expose
        public Object thermalSensationMin;
    }

    public class Uv {
        @SerializedName("max")
        @Expose
        public Integer max;

    }

    public class VitaminD {
        @SerializedName("condition")
        @Expose
        public String condition;
        @SerializedName("incidence")
        @Expose
        public Integer incidence;

    }

    public class VolumeOfRain {
        @SerializedName("averageMonthlyRainfall")
        @Expose
        public Integer averageMonthlyRainfall;
        @SerializedName("sumRainInTheMonth")
        @Expose
        public Integer sumRainInTheMonth;
        @SerializedName("onlyDayBefore")
        @Expose
        public String onlyDayBefore;
        @SerializedName("currentMonth")
        @Expose
        public String currentMonth;
        @SerializedName("currentYear")
        @Expose
        public String currentYear;
        @SerializedName("expectedRainInTheMonth")
        @Expose
        public Integer expectedRainInTheMonth;
        @SerializedName("todayTmin")
        @Expose
        public Double todayTmin;
        @SerializedName("todayTmax")
        @Expose
        public Double todayTmax;
        @SerializedName("todayAverage")
        @Expose
        public Integer todayAverage;
        @SerializedName("todayPrecipitation")
        @Expose
        public String todayPrecipitation;
        @SerializedName("tomorrowTmin")
        @Expose
        public Double tomorrowTmin;
        @SerializedName("tomorrowTmax")
        @Expose
        public Double tomorrowTmax;
        @SerializedName("tomorrowPrecipitation")
        @Expose
        public String tomorrowPrecipitation;

    }

    public class Wave {
        @SerializedName("height")
        @Expose
        public Integer height;
        @SerializedName("direction")
        @Expose
        public Integer direction;
        @SerializedName("cardinal_direction")
        @Expose
        public Integer cardinalDirection;

    }

    public class Weather__1 {
        @SerializedName("id")
        @Expose
        public Integer id;
        @SerializedName("name")
        @Expose
        public String name;
        @SerializedName("date")
        @Expose
        public String date;
        @SerializedName("dayWeek")
        @Expose
        public String dayWeek;
        @SerializedName("dateUpdate")
        @Expose
        public String dateUpdate;
        @SerializedName("temperature")
        @Expose
        public Integer temperature;
        @SerializedName("windDirection")
        @Expose
        public String windDirection;
        @SerializedName("windVelocity")
        @Expose
        public Integer windVelocity;
        @SerializedName("windDirectionDegrees")
        @Expose
        public Integer windDirectionDegrees;
        @SerializedName("humidity")
        @Expose
        public Integer humidity;
        @SerializedName("condition")
        @Expose
        public String condition;
        @SerializedName("pressure")
        @Expose
        public Integer pressure;
        @SerializedName("icon")
        @Expose
        public String icon;
        @SerializedName("sensation")
        @Expose
        public Integer sensation;
        @SerializedName("slugCondition")
        @Expose
        public String slugCondition;
    }


    public class Wind {
        @SerializedName("minVelocity")
        @Expose
        public Integer minVelocity;
        @SerializedName("maxVelocity")
        @Expose
        public Integer maxVelocity;
        @SerializedName("avgVelocity")
        @Expose
        public Double avgVelocity;
        @SerializedName("maxGust")
        @Expose
        public Integer maxGust;
        @SerializedName("direction_degrees")
        @Expose
        public Double directionDegrees;
        @SerializedName("direction")
        @Expose
        public String direction;
    }

    public class Wind__1 {
        @SerializedName("velocity")
        @Expose
        public Integer velocity;
        @SerializedName("direction_degrees")
        @Expose
        public Integer directionDegrees;
        @SerializedName("direction")
        @Expose
        public String direction;
    }

}
