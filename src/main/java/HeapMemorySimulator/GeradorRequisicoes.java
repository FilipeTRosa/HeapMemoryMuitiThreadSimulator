package HeapMemorySimulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Gera requisições de memória com tamanho aleatório dentro de um intervalo.
 */
public class GeradorRequisicoes {
    private final Random random = new Random();
    /**
     * Gera uma nova requisição de memória com tamanho aleatório.
     */
    public RequisicaoMemoria gerar(int min, int max) {
        //calcula os valores em inteiros baseados em bytes
        int minimo = min / 4;
        int maximo = max / 4;
        int tamanho = minimo + random.nextInt(maximo); // [4, 256]
        return new RequisicaoMemoria(tamanho);
    }
    /**
     * Gera um lote de requisições de memória com tamanho aleatório. A quantidade de requisições
     * por lote será informada pelo usuario.
     */
    public List<RequisicaoMemoria> gerarLote(int n, int min, int max) {
        List<RequisicaoMemoria> lote = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            lote.add(gerar(min,max));
        }
        return lote;
    }
}