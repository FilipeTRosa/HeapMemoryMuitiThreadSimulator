package HeapMemorySimulator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class EstatisticasMemoria {
    // Atributos atômicos para garantir thread-safety
    private final AtomicInteger totalRequisicoes = new AtomicInteger(0);
    private final AtomicLong somaTamanhos = new AtomicLong(0);
    private final AtomicInteger desalocadas = new AtomicInteger(0);
    private final AtomicInteger falhas = new AtomicInteger(0);
    private final AtomicInteger desfragmentacoes = new AtomicInteger(0);
    private long tempoExecucao = 0;

    public void novaRequisicao(int tamanho) {
        totalRequisicoes.incrementAndGet();
        somaTamanhos.addAndGet(tamanho);
    }

    public void incrementarFalhas() {
        falhas.incrementAndGet();
    }

    public void incrementarDesalocadas(int i) {
        desalocadas.addAndGet(i);
    }

    public void incrementarDesfragmentacoes() {
        desfragmentacoes.incrementAndGet();
    }

    public void resetarStats() {
        totalRequisicoes.set(0);
        somaTamanhos.set(0);
        desalocadas.set(0);
        falhas.set(0);
        desfragmentacoes.set(0);
        tempoExecucao = 0;
    }

    public void setTempoExecucao(long tempo) {
        this.tempoExecucao = tempo;
    }

    public void imprimir() {
        System.out.println("=== Estatísticas da Simulação ===");
        System.out.println("Total de requisições processadas: " + totalRequisicoes.get());
        System.out.println("Tamanho médio das requisições: " +
                (totalRequisicoes.get() > 0 ? somaTamanhos.get() / totalRequisicoes.get() : 0));
        System.out.println("Requisições desalocadas (via FIFO): " + desalocadas.get());
        System.out.println("Falhas de alocação (sem espaço): " + falhas.get());
        System.out.println("Desfragmentações: " + desfragmentacoes.get());
        System.out.println("Tempo total de execução: " + tempoExecucao + " ms");
    }

    /**
     * Salva os resultados consolidados da simulação em um arquivo CSV.
     * Se o arquivo não existir, cria e adiciona o cabeçalho.
     * Se já existir, apenas anexa a nova linha de dados.
     *
     * @param nomeArquivo     O nome do arquivo CSV (ex: "resultados.csv").
     * @param tamanhoHeap     O tamanho total da Heap em KB.
     * @param totalThreads    O número de threads trabalhadoras.
     * @param totalParticoes  O número de partições (arenas).
     * @param ocupacaoFinal   A porcentagem de ocupação final da memória.
     */
    public void salvarEmCSV(String nomeArquivo, int tamanhoHeap, int totalThreads, int totalParticoes, double ocupacaoFinal) {
        File arquivo = new File(nomeArquivo);
        boolean precisaCabecalho = !arquivo.exists() || arquivo.length() == 0;

        // Usa try-with-resources para garantir que o writer seja fechado automaticamente
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo, true))) { // true para modo de anexar (append)
            if (precisaCabecalho) {
                writer.write("totalRequisicoes,tamanhoHeap,totalThreads,totalParticoes,ocupacaoFinal,tamanhoMedio,desalocadas,mediaDesalocacao,tempoExecucao,falhas,desfragmentacoes\n");
            }

            int reqs = totalRequisicoes.get();
            long tamMedio = (reqs > 0) ? somaTamanhos.get() / reqs : 0;
            int des = desalocadas.get();
            double mediaDes = (reqs > 0) ? (double) des / reqs : 0.0;

            // Formata a linha de dados usando Locale.US para garantir o ponto como separador decimal
            String linha = String.format(Locale.US, "%d,%d,%d,%d,%.2f,%d,%d,%.4f,%d,%d,%d",
                    reqs,
                    tamanhoHeap,
                    totalThreads,
                    totalParticoes,
                    ocupacaoFinal,
                    tamMedio,
                    des,
                    mediaDes,
                    tempoExecucao,
                    falhas.get(),
                    desfragmentacoes.get()
            );

            writer.write(linha);
            writer.newLine(); // Adiciona uma nova linha para a próxima entrada

            System.out.println("Estatísticas salvas com sucesso em " + nomeArquivo);

        } catch (IOException e) {
            System.err.println("Erro ao salvar o arquivo CSV: " + e.getMessage());
        }
    }
}