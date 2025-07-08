package HeapMemorySimulator;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Representa uma partição (ou arena) da heap de memória.
 * Cada partição é autônoma, gerenciando seu próprio array, sua ocupação
 * e, o mais importante, sua própria trava de concorrência.
 */
public class ParticaoHeap {
    private int[] heap;
    private int posicoesOcupadas;
    private final Lock travaLocal = new ReentrantLock(); // Trava PRÓPRIA e exclusiva

    public ParticaoHeap(int tamanhoEmKb) {
        int totalInteiros = (tamanhoEmKb * 1024) / 4;
        this.heap = new int[totalInteiros];
        this.posicoesOcupadas = 0;
    }

    /**
     * Retorna o número de posições de memória (inteiros) que estão atualmente ocupadas.
     * @return O total de posições ocupadas.
     */
    public int getOcupacaoInt() {
        return this.posicoesOcupadas;
    }

    public void desfragmentar() {
        travaLocal.lock();
        try {
            //System.out.println("Thread " + Thread.currentThread().getId() + " desfragmentando uma partição...");
            int[] novaHeap = new int[this.heap.length];
            int proximoIndiceLivre = 0;

            // Copia todos os elementos diferente de 0 para o início da nova heap
            for (int i = 0; i < this.heap.length; i++) {
                if (this.heap[i] != 0) {
                    novaHeap[proximoIndiceLivre] = this.heap[i];
                    proximoIndiceLivre++;
                }
            }

            // Substitui a heap antiga pela nova, já compactada
            this.heap = novaHeap;

        } finally {
            travaLocal.unlock();
        }
    }

    // --- MÉTODO FALTANTE ADICIONADO AQUI ---
    /**
     * Libera todas as células de memória ocupadas por um determinado ID de requisição.
     * Este método é thread-safe, operando apenas em sua própria partição.
     *
     * @param id O ID da requisição a ser liberada.
     * @return O número de células de memória (inteiros) que foram liberadas.
     */
    public int liberarRequisicao(int id) {
        travaLocal.lock(); // Garante acesso exclusivo a esta partição durante a liberação.
        try {
            int espacoLiberado = 0;
            // Varre a heap desta partição procurando pelo ID.
            for (int i = 0; i < this.heap.length; i++) {
                if (this.heap[i] == id) {
                    this.heap[i] = 0; // Libera a célula
                    espacoLiberado++;
                }
            }

            // Se alguma célula foi liberada, atualiza o contador de ocupação.
            if (espacoLiberado > 0) {
                this.posicoesOcupadas -= espacoLiberado;
            }
            return espacoLiberado;
        } finally {
            travaLocal.unlock(); // Garante que a trava seja sempre liberada.
        }
    }

    public boolean alocar(RequisicaoMemoria req) {
        if (!travaLocal.tryLock()) {
            return false;
        }
        try {
            int id = req.getId();
            int tamanho = req.getTamanho();

            if (tamanho > heap.length) return false;

            for (int i = 0; i <= heap.length - tamanho; i++) {
                boolean espacoLivre = true;
                if (heap[i] == 0) {
                    for (int j = 0; j < tamanho; j++) {
                        if (heap[i + j] != 0) {
                            espacoLivre = false;
                            //i += j;
                            break;
                        }
                    }
                }else{
                    espacoLivre = false;
                }
                if (espacoLivre) {
                    for (int j = 0; j < tamanho; j++) {
                        heap[i + j] = id;
                    }
                    this.posicoesOcupadas += tamanho;
                    return true;
                }
            }
            return false;
        } finally {
            travaLocal.unlock();
        }
    }

    public int getTamanho() {
        return this.heap.length;
    }

    private void incrementaOcupacao(int valor) {
        this.posicoesOcupadas += valor;
    }

    public String representarAscii() {
        travaLocal.lock(); // Trava para garantir uma leitura consistente do estado da partição
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < heap.length; i++) {
                // ESTA É A LÓGICA PRINCIPAL:
                // Se heap[i] for 0, anexa ".", senão, anexa o ID da requisição.
                sb.append(heap[i] == 0 ? ". " : String.valueOf(heap[i]) + " ");

                // Adiciona uma quebra de linha para a saída não ficar muito longa
                if ((i + 1) % 50 == 0) {
                    sb.append("\n");
                }
            }
            sb.append("\n");
            return sb.toString();
        } finally {
            travaLocal.unlock(); // Garante a liberação da trava
        }
    }

    /**
     * NOVO: Retorna o número de posições ocupadas nesta partição.
     * O método é sincronizado pela trava local para garantir uma leitura consistente.
     * @return O número de células ocupadas.
     */
    public int getPosicoesOcupadas() {
        travaLocal.lock();
        try {
            return this.posicoesOcupadas;
        } finally {
            travaLocal.unlock();
        }
    }


}