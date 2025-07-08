package HeapMemorySimulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GerenciadorDeArenas {
    private final List<ParticaoHeap> particoes;
    private final int numParticoes;
    private final Queue<RequisicaoMemoria> filaGeral = new ConcurrentLinkedQueue<>();
    private final ReadWriteLock travaSistema = new ReentrantReadWriteLock();
    private final Lock travaLeitura = travaSistema.readLock();
    private final Lock travaEscrita = travaSistema.writeLock();

    // --- ATRIBUTO DE ESTATÍSTICAS ADICIONADO ---
    private final EstatisticasMemoria stats;

    public GerenciadorDeArenas(int tamanhoTotalKb, int numParticoes) {
        this.numParticoes = numParticoes;
        this.particoes = new ArrayList<>(numParticoes);
        int tamanhoParticaoKb = tamanhoTotalKb / numParticoes;
        for (int i = 0; i < numParticoes; i++) {
            particoes.add(new ParticaoHeap(tamanhoParticaoKb));
        }
        // Instancia o objeto de estatísticas
        this.stats = new EstatisticasMemoria();
    }

    public boolean alocar(RequisicaoMemoria req) {
        stats.novaRequisicao(req.getTamanho());
        // NOVO: Registra a ocupação atual do sistema ANTES de tentar alocar.
        //stats.adicionaOcupacao(getOcupacaoTotalAgregada());
        // Passo 1: Tentativa de alocação normal, concorrente, com trava de leitura.
        if (tentarAlocarComTravaDeLeitura(req)) {
            return true;
        }
        // Se falhou, inicia o processo de fallback com trava exclusiva.
        // O método .lock() espera o tempo que for necessário para obter a trava,
        // garantindo que a "operação de limpeza" será executada.
        travaEscrita.lock();
        try {
            // Passo 1.5: Re-verifica para evitar condição de corrida.
            // É possível que, enquanto esperávamos pela trava, outra thread liberou
            // espaço. Esta verificação evita operações de limpeza desnecessárias.
            if (tentarAlocarComTravaDeLeitura(req)) {
                return true;
            }
            // Passo 2: Tentativa com Desalocação FIFO.
            //System.out.println("Thread " + Thread.currentThread().getId() + ": Falha na alocação. Iniciando desalocação global...");
            desalocarGlobalmente(30);
            if (tentarAlocarComTravaDeLeitura(req)) {
                return true;
            }
            // Passo 3: Tentativa com Desfragmentação.
            //System.out.println("Thread " + Thread.currentThread().getId() + ": Ainda sem espaço. Iniciando desfragmentação...");
            desfragmentarTodasAsParticoes();
            stats.incrementarDesfragmentacoes();
            // Última tentativa de alocação após desfragmentar.
            if (tentarAlocarComTravaDeLeitura(req)) {
                return true;
            }


        } finally {
            // O bloco finally garante que a trava exclusiva será SEMPRE liberada,
            // mesmo que o método retorne 'true' de dentro do 'try'.
            travaEscrita.unlock();
        }

        // Passo 4: Falha Final. Se chegamos até aqui, não há mais o que fazer.
        stats.incrementarFalhas();
        System.err.println("Thread " + Thread.currentThread().getId() + ": FALHA FINAL de alocação para req ID " + req.getId());
        return false;
    }

    /**
     * NOVO: Método público para forçar uma desfragmentação global de todas as partições.
     * Este método é seguro para ser chamado externamente (ex: do Main para testes),
     * pois ele adquire a trava de escrita exclusiva, pausando todas as outras
     * operações para reorganizar a memória de forma segura.
     */
    public void forcarDesfragmentacaoGlobal() {
        travaEscrita.lock(); // Pede a trava exclusiva e espera o necessário.
        try {
            //System.out.println("\n[AVISO] Forçando desfragmentação global de todas as partições...");
            desfragmentarTodasAsParticoes();
            stats.incrementarDesfragmentacoes(); // Registra o evento nas estatísticas
        } finally {
            travaEscrita.unlock(); // Garante a liberação da trava.
        }
    }

    /**
     * NOVO: Método que comanda todas as partições para se desfragmentarem.
     * SÓ PODE SER CHAMADO de um contexto que já detém a travaEscrita.
     */
    private void desfragmentarTodasAsParticoes() {
        for (ParticaoHeap particao : particoes) {
            particao.desfragmentar();
        }
    }

    private void desalocarGlobalmente(int porcentagem) {
        int tamanhoTotal = 0;
        for (ParticaoHeap p : particoes) tamanhoTotal += p.getTamanho();

        long totalParaLiberar = (long) (tamanhoTotal * (porcentagem / 100.0));
        long liberados = 0;
        int reqLiberadas = 0;

        while (!filaGeral.isEmpty() && liberados < totalParaLiberar) {
            RequisicaoMemoria reqAntiga = filaGeral.poll();
            if (reqAntiga != null) {
                ParticaoHeap p = particoes.get(reqAntiga.getIdParticao());
                liberados += p.liberarRequisicao(reqAntiga.getId());
                reqLiberadas++;
            }
        }
        // --- ATUALIZA ESTATÍSTICAS ---
        stats.incrementarDesalocadas(reqLiberadas);
    }

    // --- MÉTODOS PÚBLICOS PARA ACESSAR AS ESTATÍSTICAS ---
    public void imprimirEstatisticas() {
        this.stats.imprimir();
    }

    public void setTempoExecucao(long tempo) {
        this.stats.setTempoExecucao(tempo);
    }

//    public void resetar() {
//        for (ParticaoHeap p : particoes) {
//            p.resetar(); // Supondo que ParticaoHeap tenha um método resetar
//        }
//        filaGeral.clear();
//        stats.resetarStats();
//        System.out.println("Simulador e estatísticas reiniciados.");
//    }

    // ... resto dos métodos (tentarAlocarComTravaDeLeitura, etc.)
    private boolean tentarAlocarComTravaDeLeitura(RequisicaoMemoria req) {
        travaLeitura.lock();
        try {
            long threadId = Thread.currentThread().getId();
            int particaoPreferencial = (int) (threadId % numParticoes);

            if (alocarNaParticao(particoes.get(particaoPreferencial), req, particaoPreferencial)) {
                return true;
            }

            for (int i = 0; i < numParticoes; i++) {
                if (i == particaoPreferencial) continue;
                if (alocarNaParticao(particoes.get(i), req, i)) {
                    return true;
                }
            }
            return false;
        } finally {
            travaLeitura.unlock();
        }
    }

    private boolean alocarNaParticao(ParticaoHeap particao, RequisicaoMemoria req, int idParticao) {
        if (particao.alocar(req)) {
            req.setIdParticao(idParticao);
            filaGeral.offer(req);
            return true;
        }
        return false;
    }

    public int getTamanhoParticao(){
        return particoes.get(0).getTamanho();
    }

    @Override
    public String toString() {
        StringBuilder strBuilder = new StringBuilder();
        int contador = 0;

        // Itera sobre a lista de todas as partições
        for (ParticaoHeap particao : particoes) {
            // 1. Adiciona um título para cada partição
            strBuilder.append("--- Partição ").append(contador).append(" ---\n");

            // 2. Chama o método da partição para obter sua representação em texto
            strBuilder.append(particao.representarAscii());
            strBuilder.append("\n"); // Adiciona uma linha em branco para separar melhor

            contador++;
        }

        // Retorna a string completa com todas as partições
        return strBuilder.toString();
    }


    /**
     * NOVO: Calcula a porcentagem de ocupação agregada de todo o sistema.
     * Ele usa a trava de leitura para garantir uma leitura consistente do estado
     * das partições sem bloquear totalmente o sistema.
     * @return A porcentagem de ocupação total (0.0 a 100.0).
     */
    public double getOcupacaoTotalAgregada() {
        travaLeitura.lock(); // Garante que não haverá uma limpeza global durante o cálculo
        try {
            long totalOcupado = 0;
            long tamanhoTotal = 0;

            for (ParticaoHeap particao : particoes) {
                totalOcupado += particao.getPosicoesOcupadas();
                tamanhoTotal += particao.getTamanho();
            }

            if (tamanhoTotal == 0) {
                return 0.0;
            }

            return (double) totalOcupado * 100.0 / tamanhoTotal;
        } finally {
            travaLeitura.unlock();
        }
    }
    /**
     * Retorna a instância de EstatisticasMemoria para que a Main possa usá-la.
     */
    public EstatisticasMemoria getEstatisticas() {
        return this.stats;
    }

    /**
     * Calcula a porcentagem de ocupação total do sistema, somando todas as partições.
     * @return A porcentagem de ocupação final.
     */
    public double getOcupacaoFinalPercentual() {
        // Usa a trava de leitura para garantir uma leitura consistente do estado das partições
        travaLeitura.lock();
        try {
            long totalOcupado = 0;
            long tamanhoTotal = 0;
            for (ParticaoHeap p : particoes) {
                totalOcupado += p.getOcupacaoInt();
                tamanhoTotal += p.getTamanho();
            }

            if (tamanhoTotal == 0) {
                return 0.0;
            }
            return (double) totalOcupado * 100.0 / tamanhoTotal;
        } finally {
            travaLeitura.unlock();
        }
    }

}