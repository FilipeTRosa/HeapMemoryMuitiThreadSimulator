package HeapMemorySimulator;

import java.util.concurrent.atomic.AtomicInteger;
/**
 * Representa uma requisição de memória.
 * Cada requisição possui um tamanho e um ID único.
 */
public class RequisicaoMemoria {
    private static final AtomicInteger contadorId = new AtomicInteger(1);

    private int id;
    private int tamanho;
    private long timestamp;
    private int idParticao; // NOVO: Guarda o ID da partição onde foi alocada.

    public RequisicaoMemoria(int tamanho) {
        this.id = contadorId.getAndIncrement();
        this.tamanho = tamanho;
        this.timestamp = System.currentTimeMillis();
        this.idParticao = -1; // Inicia com -1 para indicar que não foi alocada ainda.
    }

    /**
     * NOVO: Define o ID da partição onde a requisição foi alocada.
     * @param idParticao O ID da partição.
     */
    public void setIdParticao(int idParticao) {
        this.idParticao = idParticao;
    }

    /**
     * NOVO: Retorna o ID da partição onde a requisição está alocada.
     * @return O ID da partição.
     */
    public int getIdParticao() {
        return this.idParticao;
    }

    /**
     * Retorna o ID único da requisição.
     */
    public int getId() {
        return id;
    }
    /**
     * Retorna o tamanho da requisição (em inteiros).
     */
    public int getTamanho() {
        return tamanho;
    }
    /**
     * Retorna o time em que a requisição foi criada.
     */
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "RequisicaoMemoria{" +
                "id=" + id +
                ", tamanho=" + tamanho +
                ", idParticao=" + idParticao +
                '}';
    }
}