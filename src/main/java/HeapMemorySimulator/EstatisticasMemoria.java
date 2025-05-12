package HeapMemorySimulator;

public class EstatisticasMemoria {
    private int totalRequisicoes = 0;
    private long somaTamanhos = 0;
    private int desalocadas = 0;
    private long somaDesalocadas = 0;
    private long tempoExecucao = 0;
    private int falhas = 0;
    private int desfragmentacoes = 0;

    public void novaRequisicao(int tamanho) {
        totalRequisicoes++;
        somaTamanhos += tamanho;
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
}

