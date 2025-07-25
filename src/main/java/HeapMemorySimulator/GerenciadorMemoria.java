package HeapMemorySimulator;
/**
 * Simula a gestão de uma heap de memória com alocação e desalocação de requisições.
 */
public class GerenciadorMemoria {
    private HeapMemoria heap;
    private FilaFIFORequisicoes fila;
    private EstatisticasMemoria stats;

    public GerenciadorMemoria(int tamanhoKB) {
        this.heap = new HeapMemoria(tamanhoKB);
        this.fila = new FilaFIFORequisicoes();
        this.stats = new EstatisticasMemoria();
    }

    public void resetarHeap() {
        heap.resetar();
        stats.resetarStats();
    }
    public double getOcupacao(){
        return heap.getPorcentagemOcupacao();
    }
    public boolean alocar(RequisicaoMemoria req, boolean comDesfragmentacao, int porcentagem) {
        stats.novaRequisicao(req.getTamanho());
       // stats.adicionaOcupacao(heap.getPorcentagemOcupacao());

        boolean sucesso = heap.alocarFirstFit(req.getId(), req.getTamanho());
        if (sucesso) {
            fila.adicionar(req);
            return true;
        }
        //Não deu certo a primeira tentativa de alocar. sucesso = False
        stats.incrementarDesalocadas(fila.liberarMemoria(heap, heap.getTamanho(), porcentagem));

        /*
        if (comDesfragmentacao && heap.isFragmentacaoCritica(req.getTamanho())) {
            heap.desfragmentar();
            stats.incrementarDesfragmentacoes();
        }
        */

        //Vai tentar alocar pela segunda vez
        sucesso = heap.alocarFirstFit(req.getId(), req.getTamanho());
        if (sucesso) {
            fila.adicionar(req);
           // stats.adicionaOcupacao(heap.getPorcentagemOcupacao());
            return true;
        }

        //falhou e está ativada a desfregmentação.
        //if (comDesfragmentacao) {
        heap.desfragmentar(); //desfragmenta
        stats.incrementarDesfragmentacoes(); //incrementa desfragmentacoes
        //}
        //boolean frag = heap.isFragmentacaoCritica(req.getTamanho()); // testa fragmentacao
        //if (frag) {
        stats.incrementarDesalocadas(fila.liberarMemoria(heap, heap.getTamanho(), porcentagem));
        //}
        sucesso = heap.alocarFirstFit(req.getId(), req.getTamanho());
        if (sucesso) {
            fila.adicionar(req);
            //stats.adicionaOcupacao(heap.getPorcentagemOcupacao());
            return true;
        }
        //se tudo deu errado... GRAXA
        stats.incrementarFalhas();
       // stats.adicionaOcupacao(heap.getPorcentagemOcupacao());
        return false;
    }

    public void setTempoExecucao(long tempo) {
        stats.setTempoExecucao(tempo);
    }

    public void imprimirEstatisticas() {
        stats.imprimir();
    }

    @Override
    public String toString() {
        return heap.representarAscii();
    }

    public double calcularFragmentacaoTotal() {
        return heap.calcularFragmentacao();
    }

    public int tamanhoHeap (){ return heap.getTamanho();}

    public EstatisticasMemoria getEstatisticas() {
        return stats;
    }
}
