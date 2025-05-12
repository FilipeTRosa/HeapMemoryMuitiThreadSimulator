package HeapMemorySimulator;


import java.util.LinkedList;
import java.util.Queue;

public class FilaFIFORequisicoes {
    private Queue<RequisicaoMemoria> fila = new LinkedList<>();

    public void adicionar(RequisicaoMemoria req) {
        fila.offer(req);
    }

    public int liberarMemoria(HeapMemoria heap, int totalHeap, int porcentagem) {
        double liberacao = porcentagem / 100.0;
        int totalParaLiberar = (int) (totalHeap * liberacao);
        int liberados = 0;
        int reqLiberadas = 0;

        while (!fila.isEmpty() && liberados < totalParaLiberar) {
            RequisicaoMemoria req = fila.poll();
            heap.liberarRequisicao(req.getId());
            liberados += req.getTamanho();
            reqLiberadas++;
            //System.out.println("Total Liberados: " + liberados);
        }

        return reqLiberadas;
    }
}
