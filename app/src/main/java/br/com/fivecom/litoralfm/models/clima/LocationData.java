
package br.com.fivecom.litoralfm.models.clima;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LocationData implements Serializable {

    @SerializedName("idlocale")
    @Expose
    private Integer idlocale;
    @SerializedName("idcity")
    @Expose
    private Integer idcity;
    @SerializedName("capital")
    @Expose
    private Boolean capital;
    @SerializedName("idcountry")
    @Expose
    private Integer idcountry;
    @SerializedName("ac")
    @Expose
    private String ac;
    @SerializedName("country")
    @Expose
    private String country;
    @SerializedName("uf")
    @Expose
    private String uf;
    @SerializedName("city")
    @Expose
    private String city;
    @SerializedName("region")
    @Expose
    private String region;
    @SerializedName("seaside")
    @Expose
    private Boolean seaside;
    @SerializedName("latitude")
    @Expose
    private Double latitude;
    @SerializedName("longitude")
    @Expose
    private Double longitude;
    @SerializedName("tourist")
    @Expose
    private Boolean tourist;
    @SerializedName("agricultural")
    @Expose
    private Boolean agricultural;
    @SerializedName("distance")
    @Expose
    private Double distance;

    public Integer getIdlocale() {
        return idlocale;
    }

    public void setIdlocale(Integer idlocale) {
        this.idlocale = idlocale;
    }

    public LocationData withIdlocale(Integer idlocale) {
        this.idlocale = idlocale;
        return this;
    }

    public Integer getIdcity() {
        return idcity;
    }

    public void setIdcity(Integer idcity) {
        this.idcity = idcity;
    }

    public LocationData withIdcity(Integer idcity) {
        this.idcity = idcity;
        return this;
    }

    public Boolean getCapital() {
        return capital;
    }

    public void setCapital(Boolean capital) {
        this.capital = capital;
    }

    public LocationData withCapital(Boolean capital) {
        this.capital = capital;
        return this;
    }

    public Integer getIdcountry() {
        return idcountry;
    }

    public void setIdcountry(Integer idcountry) {
        this.idcountry = idcountry;
    }

    public LocationData withIdcountry(Integer idcountry) {
        this.idcountry = idcountry;
        return this;
    }

    public String getAc() {
        return ac;
    }

    public void setAc(String ac) {
        this.ac = ac;
    }

    public LocationData withAc(String ac) {
        this.ac = ac;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public LocationData withCountry(String country) {
        this.country = country;
        return this;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public LocationData withUf(String uf) {
        this.uf = uf;
        return this;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocationData withCity(String city) {
        this.city = city;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public LocationData withRegion(String region) {
        this.region = region;
        return this;
    }

    public Boolean getSeaside() {
        return seaside;
    }

    public void setSeaside(Boolean seaside) {
        this.seaside = seaside;
    }

    public LocationData withSeaside(Boolean seaside) {
        this.seaside = seaside;
        return this;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public LocationData withLatitude(Double latitude) {
        this.latitude = latitude;
        return this;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocationData withLongitude(Double longitude) {
        this.longitude = longitude;
        return this;
    }

    public Boolean getTourist() {
        return tourist;
    }

    public void setTourist(Boolean tourist) {
        this.tourist = tourist;
    }

    public LocationData withTourist(Boolean tourist) {
        this.tourist = tourist;
        return this;
    }

    public Boolean getAgricultural() {
        return agricultural;
    }

    public void setAgricultural(Boolean agricultural) {
        this.agricultural = agricultural;
    }

    public LocationData withAgricultural(Boolean agricultural) {
        this.agricultural = agricultural;
        return this;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public LocationData withDistance(Double distance) {
        this.distance = distance;
        return this;
    }

}
