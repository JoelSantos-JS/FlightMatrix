package com.joel.br.FlightMatrix.Factory;


import com.joel.br.FlightMatrix.Adapter.DecolarAdapter;
import com.joel.br.FlightMatrix.Adapter.FontePassagemAdapter;
import com.joel.br.FlightMatrix.Adapter.MaxMilhasAdapter;
import com.joel.br.FlightMatrix.Adapter.MilhasAdapter;
import com.joel.br.FlightMatrix.models.FontePassagem;
import org.springframework.stereotype.Component;



import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Factory para criar instâncias de adaptadores de fontes de passagens aéreas.
 */
@Component
public class FontePassagemFactory {

    private final Map<String, FontePassagemAdapter> adapters = new HashMap<>();

    /**
     * Cria um adaptador para a fonte de passagem especificada
     *
     * @param fonte FontePassagem com as informações da fonte
     * @return Instância do adaptador correspondente à fonte, ou Optional vazio se não houver implementação
     */
    public Optional<FontePassagemAdapter> criarAdapter(FontePassagem fonte) {
        if (!fonte.getAtiva()) {
            return Optional.empty();
        }

        // Verifica se já existe um adaptador criado para esta fonte
        if (adapters.containsKey(fonte.getNome())) {
            return Optional.of(adapters.get(fonte.getNome()));
        }

        // Cria um novo adaptador conforme o nome da fonte
        FontePassagemAdapter adapter = null;

        switch (fonte.getNome().toLowerCase()) {
            case "decolar":
                adapter = new DecolarAdapter(fonte);
                break;
            case "maxmilhas":
                adapter = new MaxMilhasAdapter(fonte);
                break;
            case "123milhas":
                adapter = new MilhasAdapter(fonte);
                break;
            default:
                return Optional.empty();
        }

        // Armazena o adaptador criado para reutilização
        adapters.put(fonte.getNome(), adapter);

        return Optional.of(adapter);
    }

    /**
     * Limpa o cache de adaptadores, forçando a recriação na próxima solicitação
     */
    public void limparCache() {
        adapters.clear();
    }

}