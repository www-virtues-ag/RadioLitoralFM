
package br.com.fivecom.litoralfm.models.clima;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Location implements Serializable {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("totalRows")
    @Expose
    private Object totalRows;
    @SerializedName("totalPages")
    @Expose
    private Object totalPages;
    @SerializedName("page")
    @Expose
    private Object page;
    @SerializedName("data")
    @Expose
    private List<LocationData> data = null;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Location withSuccess(Boolean success) {
        this.success = success;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Location withMessage(String message) {
        this.message = message;
        return this;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Location withTime(String time) {
        this.time = time;
        return this;
    }

    public Object getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Object totalRows) {
        this.totalRows = totalRows;
    }

    public Location withTotalRows(Object totalRows) {
        this.totalRows = totalRows;
        return this;
    }

    public Object getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Object totalPages) {
        this.totalPages = totalPages;
    }

    public Location withTotalPages(Object totalPages) {
        this.totalPages = totalPages;
        return this;
    }

    public Object getPage() {
        return page;
    }

    public void setPage(Object page) {
        this.page = page;
    }

    public Location withPage(Object page) {
        this.page = page;
        return this;
    }

    public List<LocationData> getData() {
        return data;
    }

    public void setData(List<LocationData> data) {
        this.data = data;
    }

    public Location withData(List<LocationData> data) {
        this.data = data;
        return this;
    }

}
