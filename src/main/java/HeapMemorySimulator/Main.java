package HeapMemorySimulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        // --- Configuração da Arquitetura ---
        System.out.println("Digite o tamanho TOTAL da HEAP em Kb (ex: 1024):");
        int heapSize = scanner.nextInt();
        System.out.println("Digite o número de PARTIÇÕES (Arenas) (ex: 8):");
        int numParticoes = scanner.nextInt();
        System.out.println("Digite o número de THREADS TRABALHADORAS (ex: 16):");
        int numThreads = scanner.nextInt();

        // --- Recursos Compartilhados ---
        final GerenciadorDeArenas gerenteDeArenas = new GerenciadorDeArenas(heapSize, numParticoes);
        final Queue<RequisicaoMemoria> filaDeTarefas = new LinkedList<>();
        final Object lock = new Object(); // Nosso objeto monitor
        final List<TrabalhadorDeAlocacao> trabalhadores = new ArrayList<>();
        final List<Thread> threads = new ArrayList<>();

        // --- Criando e Iniciando as Threads Consumidoras ---
        for (int i = 0; i < numThreads; i++) {
            TrabalhadorDeAlocacao trabalhador = new TrabalhadorDeAlocacao(gerenteDeArenas, filaDeTarefas, lock);
            trabalhadores.add(trabalhador);
            Thread t = new Thread(trabalhador, "Trabalhador-" + i);
            threads.add(t);
            t.start();
        }

        // --- Lógica do Produtor (Menu Principal) ---
        System.out.println("\n--- Configuração do Lote de Requisições ---");
        System.out.println("Digite a quantidade de requisições (ex: 5000):");
        int totalReq = scanner.nextInt();

        GeradorRequisicoes gerador = new GeradorRequisicoes();
        List<RequisicaoMemoria> lote = gerador.gerarLote(totalReq, 16, 1024);

        long inicio = System.currentTimeMillis();

        // Alimenta a fila com as tarefas
        for (RequisicaoMemoria r : lote) {
            synchronized (lock) {
                filaDeTarefas.add(r);
                lock.notify();
            }
        }

        // Espera as tarefas terminarem
        while (true) {
            boolean filaVazia;
            synchronized (lock) {
                filaVazia = filaDeTarefas.isEmpty();
            }
            if (filaVazia) {
                Thread.sleep(500);
                // Re-verificar após o sleep para garantir que a fila ainda está vazia
                synchronized (lock) {
                    if (filaDeTarefas.isEmpty()) {
                        break;
                    }
                }
            }
            Thread.sleep(100);
        }

        // --- Encerramento  ---
        System.out.println("\n--- Encerrando as Threads Trabalhadoras ---");
        for (TrabalhadorDeAlocacao t : trabalhadores) {
            t.shutdown();
        }

        synchronized (lock) {
            lock.notifyAll();
        }

        for (Thread t : threads) {
            t.join();
        }
        long fim = System.currentTimeMillis();

        double ocupacaoFinal = gerenteDeArenas.getOcupacaoFinalPercentual();
        // --- Apresentação Final ---
        System.out.println("\n--- Processamento Concluído ---");
        gerenteDeArenas.setTempoExecucao(fim - inicio);
        gerenteDeArenas.imprimirEstatisticas();
        System.out.println("Tamanho Heap: " + heapSize + "Kb");
        System.out.println("Nro Partições: " + numParticoes);
        System.out.println("Nro Threads: " + numThreads);
        System.out.println(String.format("Ocupação Final: %.2f%%", ocupacaoFinal));


        // --Salvar resultados em CSV ---
        System.out.println("\n--- Salvando Estatísticas em CSV ---");
        try {
            //Double ocupacaoFinal = gerenteDeArenas.getOcupacaoFinalPercentual();
            EstatisticasMemoria stats = gerenteDeArenas.getEstatisticas();
            stats.salvarEmCSV(
                    "resultados_simulacao.csv",
                    heapSize,
                    numThreads,
                    numParticoes,
                    ocupacaoFinal
            );
        } catch (Exception e) {
            System.err.println("Ocorreu um erro ao gerar o arquivo CSV: " + e.getMessage());
        }

        scanner.close();
        System.out.println("Simulação finalizada.");
    }
}