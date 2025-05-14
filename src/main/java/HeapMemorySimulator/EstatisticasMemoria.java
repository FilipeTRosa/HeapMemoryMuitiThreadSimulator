package HeapMemorySimulator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EstatisticasMemoria {
    private int totalRequisicoes = 0;
    private long somaTamanhos = 0;
    private int desalocadas = 0;
    private long somaDesalocadas = 0;
    private long tempoExecucao = 0;
    private int falhas = 0;
    private int desfragmentacoes = 0;
    private List<Double> ocupacaoHeap;

    public EstatisticasMemoria() {
        this.ocupacaoHeap = new ArrayList<>();
    }

    public void novaRequisicao(int tamanho) {
        totalRequisicoes++;
        somaTamanhos += tamanho;
    }

    public void adicionaOcupacao(double valor) {
        this.ocupacaoHeap.add(valor);
    }

    public void novaDesalocacao(int tamanho) {
        desalocadas++;
        somaDesalocadas += tamanho;
    }
    public void resetarStats(){
        totalRequisicoes = 0;
        somaTamanhos = 0;
        desalocadas = 0;
        somaDesalocadas = 0;
        tempoExecucao = 0;
        falhas = 0;
        desfragmentacoes = 0;
    }
    public void incrementarFalhas() { falhas++; }

    public void incrementarDesalocadas(int i) { desalocadas += i; }

    public void incrementarDesfragmentacoes() { desfragmentacoes++; }

    public void setTempoExecucao(long tempo) { tempoExecucao = tempo; }

    public void imprimir() {
        System.out.println("=== Estatísticas ===");
        System.out.println("Total de requisições geradas: " + totalRequisicoes);
        System.out.println("Tamanho médio: " +
                (totalRequisicoes > 0 ? somaTamanhos / totalRequisicoes : 0));
        System.out.println("Desalocadas: " + desalocadas);
        System.out.println("Média desalocação: " +
                (desalocadas > 0 ? somaDesalocadas / desalocadas : 0));
        System.out.println("Tempo execução: " + tempoExecucao + " ms");
        System.out.println("Falhas de alocação: " + falhas);
        System.out.println("Desfragmentações: " + desfragmentacoes);
    }
    public void salvarCSV(String caminho) {
        File arquivo = new File(caminho);
        boolean arquivoExiste = arquivo.exists();
        boolean escreverCabecalho = !arquivoExiste || arquivo.length() == 0;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo, true))) {
            if (escreverCabecalho) {
                writer.write("totalRequisicoes,tamanhoMedio,desalocadas,mediaDesalocacao,tempoExecucao,falhas,desfragmentacoes\n");
            }
            long tamanhoMedio = (totalRequisicoes > 0 ? somaTamanhos / totalRequisicoes : 0);
            long mediaDesalocacao = (desalocadas > 0 ? somaDesalocadas / desalocadas : 0);
            writer.write(String.format("%d,%d,%d,%d,%d,%d,%d\n",
                    totalRequisicoes, tamanhoMedio, desalocadas, mediaDesalocacao, tempoExecucao, falhas, desfragmentacoes));
        } catch (IOException e) {
            System.err.println("Erro ao salvar estatísticas: " + e.getMessage());
        }
    }

    public void salvarOcupacaoHeap(String caminho) {
        if (ocupacaoHeap == null || ocupacaoHeap.isEmpty()) {
            System.out.println("Nenhum dado de ocupação para salvar.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminho))) {
            for (double valor : ocupacaoHeap) {
                writer.write(valor + "\n");
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar ocupação da heap: " + e.getMessage());
        }
    }



}

