package HeapMemorySimulator;


public class HeapMemoria {
    private int[] heap;
    private int posicoesOcupadas;

    public HeapMemoria(int tamanhoKB) {
        int totalInteiros = (tamanhoKB * 1024) / 4;
        this.heap = new int[totalInteiros];
    }

    public void incrementaOcupacao(int valor){
        this.posicoesOcupadas += valor;
    }

    public void decrementaOcupacao(int valor){
        this.posicoesOcupadas -= valor;
    }

    public int getOcupacaoInt(){
        return this.posicoesOcupadas;
    }

    public double getPorcentagemOcupacao(){
        if(this.posicoesOcupadas == 0){
            //throw new ArithmeticException("Não é possível calcular porcentagem: Posicoes ocupadas é zero.");
            return 0.0;
        }
        return (this.posicoesOcupadas * 100.0) / this.heap.length;
    }

    public int getTamanho() {
        return heap.length;
    }

    public void resetar() {
        for (int i = 0; i < heap.length; i++) heap[i] = 0;
    }

    public boolean alocarFirstFit(int id, int tamanho) {
        for (int i = 0; i <= heap.length - tamanho; i++) {
            boolean podeAlocar = true;
            for (int j = 0; j < tamanho; j++) {
                if (heap[i + j] != 0) {
                    podeAlocar = false;
                    break;
                }
            }
            if (podeAlocar) {
                for (int j = 0; j < tamanho; j++) {
                    heap[i + j] = id;
                }
                incrementaOcupacao(tamanho);
                return true;
            }
        }
        return false;
    }

    public int liberarRequisicao(int id) {
        int liberados = 0;
        for (int i = 0; i < heap.length; i++) {
            if (heap[i] == id) {
                heap[i] = 0;
                liberados++;
            }
        }
        return liberados;
    }

    public void desfragmentar() {
        int[] novaHeap = new int[heap.length];
        int index = 0;
        for (int i = 0; i < heap.length; i++) {
            if (heap[i] != 0) {
                novaHeap[index++] = heap[i];
            }
        }
        heap = novaHeap;
    }

    public double calcularFragmentacao() {
        int livres = 0;
        for (int i : heap) {
            if (i == 0) livres++;
        }
        return (livres * 100.0) / heap.length;
    }

    public boolean isFragmentacaoCritica(int tamanho) {
        int livres = 0, maiorContiguo = 0, atual = 0;
        for (int i = 0; i < heap.length; i++) {
            if (heap[i] == 0) {
                livres++;
                atual++;
                if (atual > maiorContiguo) maiorContiguo = atual;
            } else {
                atual = 0;
            }
        }
        double percLivre = (double) livres / heap.length;
        return percLivre > 0.0 && maiorContiguo < tamanho;
    }

    public String representarAscii() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < heap.length; i++) {
            sb.append(heap[i] == 0 ? ". " : heap[i] + " ");
            if ((i + 1) % 64 == 0) sb.append("\n");
        }
        if (heap.length % 64 != 0) sb.append("\n");
        return sb.toString();
    }
}
