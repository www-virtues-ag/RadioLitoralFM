package br.com.fivecom.litoralfm.utils;

import br.com.fivecom.litoralfm.services.AgendaService;
import br.com.fivecom.litoralfm.services.ProgramacaoAPIService;

/**
 * Gerenciador centralizado de serviços para evitar múltiplas instâncias
 */
public class ServiceManager {
    
    private static ProgramacaoAPIService programacaoService;
    private static AgendaService agendaService;
    
    /**
     * Retorna instância única de ProgramacaoAPIService
     */
    public static synchronized ProgramacaoAPIService getProgramacaoService() {
        if (programacaoService == null) {
            programacaoService = new ProgramacaoAPIService();
        }
        return programacaoService;
    }
    
    /**
     * Retorna instância única de AgendaService
     */
    public static synchronized AgendaService getAgendaService() {
        if (agendaService == null) {
            agendaService = new AgendaService();
        }
        return agendaService;
    }
    
    /**
     * Limpa as instâncias (útil para testes ou reset)
     */
    public static synchronized void clear() {
        programacaoService = null;
        agendaService = null;
    }
}
