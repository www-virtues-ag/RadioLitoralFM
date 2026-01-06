package br.com.fivecom.litoralfm.ui.locutores;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.fivecom.litoralfm.models.locutores.Locutor;

/**
 * Repositório para gerenciar locutores.
 * Mantém locutores padrão como fallback e permite atualização via API.
 */
public final class LocutoresRepo {
    private static final String TAG = "LocutoresRepo";

    private static Map<String, Locutor> locutoresMap = new HashMap<>();
    private static final List<Locutor> DEFAULT_LOCUTORES;

    // Inicializar locutores padrão (fallback)
    static {
        DEFAULT_LOCUTORES = new ArrayList<>();

        DEFAULT_LOCUTORES.add(new Locutor(
                "sol",
                "SOL",
                "sol",
                "A musa fitness da Litoral tem nome! Nossa Sol é alegre e anima os nossos ouvintes com sua energia inigualável.\n\nSolange Correa realiza ações externas, é amiga do público e conhecida por onde passa. A atleta da Rádio Litoral está sempre presente nas redes sociais mostrando sua rotina de exercícios físicos e os pagodinhos do fim de semana.",
                "",
                "",
                ""
        ));

        DEFAULT_LOCUTORES.add(new Locutor(
                "cleide",
                "CLEIDE",
                "cleide",
                "Cleide é a diversão em pessoa! A principal locutora da Rádio Litoral leva sua alegria por onde passa. \n\nEla acredita que, seja online ou offline, a função de um comunicador é conquistar as pessoas de maneira leve e divertida. Animando as manhãs dos capixabas com sua voz marcante, a nossa Cleide cativa os ouvintes!",
                "",
                "",
                ""
        ));

        DEFAULT_LOCUTORES.add(new Locutor(
                "sergio",
                "SERGIO",
                "sergio",
                "Nosso locutor de externas é divertido, engraçado e tem uma voz muuuito marcante! \n\nConhecido por divertir o público nas ruas, Sérgio Pontes é aquele que realiza as ações e conhece vocês bem de pertinho. Ele se diverte e ainda deixa a nossa marca por aí!",
                "",
                "",
                ""
        ));

        DEFAULT_LOCUTORES.add(new Locutor(
                "bruninho",
                "BRUNINHO",
                "bruninho",
                "Falaaa, mulekote! Nosso locutor mais jovem é diferenciado. \n\nBruninho Andrade se destaca nas manhãs e tardes da Litoral e marca presença nas redes sociais mostrando sua rotina e seu jeito pra lá de engraçado. É muito querido pelos nossos ouvintes e ama recebê-los aqui no nosso estúdio pra bater um papo.",
                "",
                "",
                ""
        ));

        DEFAULT_LOCUTORES.add(new Locutor(
                "nat",
                "NAT",
                "nat",
                "A Natizinha da Litoral é a nossa mãe de pet! Apaixonada por animais, ela sempre está disposta a ajudá-los. \n\nCarismática, animada e autêntica: esses são os adjetivos que podem definir a Nathália Ferreira. Nossa comunicadora é formada em Jornalismo e cumpre seu papel de locutora lindamente, com a diversão que vocês gostam. Nossa caçulinha é demais!",
                "",
                "",
                ""
        ));

        DEFAULT_LOCUTORES.add(new Locutor(
                "alex",
                "ALEX",
                "alex",
                "Locutor da Litoral FM, Alex Bonno é conhecido pela voz marcante, pelo bom humor e pela forma autêntica de se comunicar com o público. Amante de gatos (pai orgulhoso de dois), adora dividir momentos do dia a dia com leveza e criatividade. Nas redes, mistura lifestyle, cultura pop e bastidores do rádio, sempre com conteúdo descontraído, atual e de olho no que engaja.",
                "",
                "",
                ""
        ));

        DEFAULT_LOCUTORES.add(new Locutor(
                "jonas",
                "JONAS",
                "jonas",
                "É ele, nosso DJ do Segue o Baile Litoral! Ele é o responsável por escolher as verdadeiras pedradas que escutamos na nossa tarde, e vamos combinar, ele não erra nunca! \n\nJonas Braum tem um carisma diferenciado, e apesar de parecer muito sério, é muito divertido. Nosso DJ realiza eventos externos e é multifunções, já que além de escolher as melhores músicas para sua rádio preferida, ainda é sonoplasta, produtor de áudio e locutor.",
                "",
                "",
                ""
        ));

        DEFAULT_LOCUTORES.add(new Locutor(
                "roliber",
                "ROLIBER",
                "roliber",
                "Olha, essa voz é impossível você não reconhecer… Roliber Anderson não passa despercebido em lugar nenhum! \n\nNosso locutor, fala francês, é advogado e chama atenção principalmente dos ouvintes! São 26 anos de Rádio Litoral, uma voz cativante e um carisma sem igual!",
                "",
                "",
                ""
        ));

        // Carregar locutores padrão no mapa inicialmente
        loadDefaultLocutores();
    }

    private LocutoresRepo() {}

    /**
     * Retorna um locutor pelo ID
     */
    public static Locutor get(String id) {
        return locutoresMap.get(id);
    }

    /**
     * Retorna todos os locutores
     */
    public static List<Locutor> getAll() {
        return new ArrayList<>(locutoresMap.values());
    }

    /**
     * Atualiza os locutores com dados da API
     */
    public static void updateLocutores(List<Locutor> locutores) {
        Log.d(TAG, "📝 Atualizando locutores com dados da API");
        locutoresMap.clear();

        for (Locutor locutor : locutores) {
            // Mesclar com descrições padrão se necessário
            if (locutor.getDescricao() == null || locutor.getDescricao().isEmpty()) {
                Locutor defaultLocutor = findDefaultLocutor(locutor.getNome());
                if (defaultLocutor != null) {
                    locutor.setDescricao(defaultLocutor.getDescricao());
                }
            }

            locutoresMap.put(locutor.getId(), locutor);
        }

        Log.d(TAG, "✅ Total de locutores atualizado: " + locutoresMap.size());
    }

    /**
     * Carrega locutores padrão (fallback)
     */
    public static void loadDefaultLocutores() {
        Log.d(TAG, "⚠️ Carregando locutores padrão (fallback)");
        locutoresMap.clear();

        for (Locutor locutor : DEFAULT_LOCUTORES) {
            locutoresMap.put(locutor.getId(), locutor);
        }
    }

    /**
     * Busca um locutor padrão pelo nome
     */
    private static Locutor findDefaultLocutor(String nome) {
        for (Locutor locutor : DEFAULT_LOCUTORES) {
            if (locutor.getNome().equalsIgnoreCase(nome)) {
                return locutor;
            }
        }
        return null;
    }
}
