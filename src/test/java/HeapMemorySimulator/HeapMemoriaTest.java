package HeapMemorySimulator;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HeapMemoriaTest {

    private HeapMemoria heap;

    @BeforeEach
    void setup() {
       heap = new HeapMemoria(1); // 1 KB de heap = 256 ints
       //heap.alocarFirstFit(1, 10);
    }

    @Test
    void deveRetornarTamanhoDaHeap() {
        assertEquals(256, heap.getTamanho()); // 1 KB = 1024 bytes / 4 = 256 ints
    }

    @Test
    void deveResetarAHeap() {
        heap.alocarFirstFit(1, 10);
        heap.resetar();
        assertEquals(100.0, heap.calcularFragmentacao(), 0.01);
    }

    @Test
    void deveResetarOcupacaoHeap() {
        heap.alocarFirstFit(1, 10);
        heap.resetar();
        assertEquals(0, heap.getOcupacaoInt());
    }

    @Test
    void deveAlocarFirstFitNaHeap() {
        boolean resultado = heap.alocarFirstFit(7, 10); // tenta alocar 10 posições com id 7
        assertTrue(resultado, "A alocação deveria ter sucesso");

        // Verifica se os 10 primeiros elementos agora têm valor 7
        for (int i = 0; i < 10; i++) {
            assertEquals(7, heap.getValorNaPosicao(i), "Valor incorreto na posição " + i);
        }
    }

    @Test
    void deveLiberarRequisicaoDaHeap() {
        heap.alocarFirstFit(5, 10);
        heap.alocarFirstFit(6, 10);
        heap.alocarFirstFit(7, 8);
        heap.alocarFirstFit(8, 10);

        assertEquals(5, heap.getValorNaPosicao(9)); //verifica se alocou certo
        assertEquals(8, heap.getValorNaPosicao(29));//verifica se alocou certo

        int liberados = heap.liberarRequisicao(6);
        liberados += heap.liberarRequisicao(7);

        assertEquals(18, liberados); // verifica se liberou 18 inteiros
        
        for (int i = 10; i < 28; i++) { //verifica se realmente limpou a heap nas posições
            assertEquals(0, heap.getValorNaPosicao(i), "Valor incorreto na posicao " + i);
        }
    }

    @Test
    void desfragmentar() {
        heap.alocarFirstFit(5, 10);
        heap.alocarFirstFit(6, 10);
        heap.alocarFirstFit(7, 8);
        heap.alocarFirstFit(8, 10);

        assertEquals(5, heap.getValorNaPosicao(9)); //verifica se alocou certo
        assertEquals(8, heap.getValorNaPosicao(29));//verifica se alocou certo
    }

    @Test
    void calcularFragmentacao() {
        heap.alocarFirstFit(1, 10);
        assertEquals(96.1, heap.calcularFragmentacao(), 0.01);
    }

    @Test
    void isFragmentacaoCritica() {
    }

    @Test
    void representarAscii() {
    }
}