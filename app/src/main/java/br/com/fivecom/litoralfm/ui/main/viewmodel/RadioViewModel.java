package br.com.fivecom.litoralfm.ui.main.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import br.com.fivecom.litoralfm.models.scheduler.ProgramaAPI;

public class RadioViewModel extends ViewModel {
    public MutableLiveData<ProgramaAPI> programaAtual = new MutableLiveData<>();
    public MutableLiveData<List<ProgramaAPI>> programas = new MutableLiveData<>();
}
