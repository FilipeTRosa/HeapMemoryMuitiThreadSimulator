package HeapMemorySimulator;

import java.util.Queue;

public class TrabalhadorDeAlocacao implements Runnable {

    private final GerenciadorDeArenas gerente;
    private final Queue<RequisicaoMemoria> filaDeTarefas;
    private final Object lock; // O objeto monitor compartilhado
    private volatile boolean rodando = true;

    public TrabalhadorDeAlocacao(GerenciadorDeArenas gerente, Queue<RequisicaoMemoria> fila, Object lock) {
        this.gerente = gerente;
        this.filaDeTarefas = fila;
        this.lock = lock;
    }

    @Override
    public void run() {
        System.out.println("Thread " + Thread.currentThread().getName() + " iniciada.");
        while (rodando) {
            RequisicaoMemoria tarefa;

            // Bloco sincronizado para acessar a fila de forma segura
            synchronized (lock) {
                // Se a fila estiver vazia, a thread deve esperar.
                // O loop 'while' é crucial para se proteger contra "despertares espúrios".
                while (filaDeTarefas.isEmpty() && rodando) {
                    try {
                        lock.wait(); // Libera a trava e entra em estado de espera
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        rodando = false; // Encerra se for interrompida
                    }
                }

                // Se foi acordada para desligar, sai do loop
                if (!rodando) {
                    break;
                }

                // Pega a próxima tarefa da fila
                tarefa = filaDeTarefas.poll();
            }

            // --- Processa a tarefa FORA do bloco synchronized ---
            // Isso permite que outras threads peguem tarefas da fila enquanto esta está ocupada.
            if (tarefa != null) {
                gerente.alocar(tarefa);
            }
        }
        System.out.println("Thread " + Thread.currentThread().getName() + " encerrada.");
    }

    public void shutdown() {
        this.rodando = false;
    }
}