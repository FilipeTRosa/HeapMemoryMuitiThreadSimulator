package HeapMemorySimulator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PersistenciaRequisicoes {

    public static void salvarLote(String caminho, List<RequisicaoMemoria> lote) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(caminho))) {
            for (RequisicaoMemoria req : lote) {
                writer.write(req.getTamanho() + "\n"); // salvamos apenas o tamanho
            }
        }
    }

    public static List<RequisicaoMemoria> carregarLote(String caminho) throws IOException {
        List<RequisicaoMemoria> lote = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(caminho))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                int tamanho = Integer.parseInt(linha.trim());
                lote.add(new RequisicaoMemoria(tamanho));
            }
        }
        return lote;
    }
}
