package br.com.fivecom.litoralfm.ui.main.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import br.com.fivecom.litoralfm.models.weather.Weather;

public class MainViewModel extends ViewModel {

    public MainViewModel() {
        weatherModel = new MutableLiveData<>();
    }

    public final MutableLiveData<Weather> weatherModel;
}
