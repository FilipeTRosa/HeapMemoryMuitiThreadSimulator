package HeapMemorySimulator;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Gerenciador de Memória Simulado
 *
 * Este projeto simula a alocação de memória usando uma política First-Fit em uma heap representada por um vetor de inteiros.
 * Quando a memória está cheia, 30% da heap é liberada com uma política FIFO.
 *
 * Funcionalidades:
 * - Geração de requisições de memória de tamanho aleatório
 * - Alocação First-Fit
 * - Liberação de memória usando FIFO (30%)
 * - Estatísticas detalhadas: requisições, desalocações, tempo de execução, fragmentação
 * - Visualização gráfica simples do estado da heap
 *
 * Desenvolvido por: [Bianca Durgante, Davi Lemos e Filipe Teixeira]
 * Data: [Em andamento]
 * Curso: Engenharia da Computação – UNIPAMPA
 */

/**
 * Classe principal para testar o Gerenciador de Memória.
 * Gera um lote de requisições, tenta alocar, e imprime estatísticas ao final.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        int opcao;
        long inicio, fim;


        //preparando a heap
        System.out.println("Digite o tamanho da HEAP em Kb:");
        System.out.println("==Conversão==\n1024Kb -> 1Mb\n65536Kb -> 64Mb\n131072Kb -> 128Mb\n262144Kb -> 256Mb\n524288Kb -> 512Mb\n1048576Kb -> 1Gb");
        int heapSize = scanner.nextInt();
        GerenciadorMemoria gerenteMemoria = new GerenciadorMemoria(heapSize);
        GeradorRequisicoes gerador = new GeradorRequisicoes();
        System.out.println("Tamanho Heap: " + gerenteMemoria.tamanhoHeap());

        do {
            System.out.println("\n=== MENU DO SIMULADOR ===");
            System.out.println("1 - Gerar lote de requisições");
            System.out.println("2 - Exibir estado da heap");
            System.out.println("3 - ...");
            System.out.println("4 - ...");
            System.out.println("5 - Reiniciar simulador");
            System.out.println("6 - Sair");
            System.out.print("Escolha uma opção: ");
            opcao = scanner.nextInt();

            switch (opcao) {
                case 1:
                    System.out.println("\nGerando lote de requisições...\nValores padrão para teste:\nMínimo = 16Bytes [4 int]\nMáximo = 1Kb -> 1024Bytes [256 int]");
                    System.out.println("Digite um valor mínimio em bytes para cada requisição:");
                    //criar condição para garantir valores mínimos.....
                    int min = scanner.nextInt();
                    System.out.println("Digite um valor máximo em bytes para cada requisição:");
                    int max = scanner.nextInt();
                    System.out.println("Digite a quantidade de requisições que deseja fazer:");
                    int totalReq = scanner.nextInt();

                    //cria uma List de reqMem com chamada "lote"
                    List<RequisicaoMemoria> lote = gerador.gerarLote(totalReq, min, max);
                    PersistenciaRequisicoes.salvarLote("lote.txt", lote); // salva o lote para testes futuros


                    //teste com o alocar padrão
                    System.out.println("\n===Memoria sem desfragmentar===");
                    inicio = System.currentTimeMillis();
                    for (RequisicaoMemoria r : lote) { //enquanto houver req em lote
                        gerenteMemoria.alocar(r,false, 30); //aloca cada req
                    }
                    fim = System.currentTimeMillis();
                    System.out.println(gerenteMemoria.toString());
                    gerenteMemoria.setTempoExecucao(fim - inicio);
                    gerenteMemoria.imprimirEstatisticas();

                    gerenteMemoria.resetarHeap();
                    gerenteMemoria.getEstatisticas().salvarCSV("estatisticas.csv");


                    //teste com o alocar que desfragmenta
                    System.out.println("\n===Memoria com desfragmentação===");
                    inicio = System.currentTimeMillis();
                    for (RequisicaoMemoria r : lote) { //enquanto houver lote
                        gerenteMemoria.alocar(r,true, 30);//aloca cada req
                    }
                    fim = System.currentTimeMillis();
                    gerenteMemoria.setTempoExecucao(fim - inicio);
                    gerenteMemoria.imprimirEstatisticas();

                    gerenteMemoria.resetarHeap();
                    break;
                case 2:
                    System.out.println("Exibindo estado da heap...");
                    //Aqui vamos imprimir os Status atuais da heap
                    // tamanho.... % Ocupara.... Fragmentacao
                    //
                    break;
                case 3:
                    System.out.println("Exibindo estatísticas...");
                    // exibir estatísticas acumuladas
                    break;
                case 4:
                    System.out.println("...");

                    break;
                case 5:
                    System.out.println("Reiniciando simulador...");
                    // resetar heap e estatísticas
                    gerenteMemoria.resetarHeap();
                    break;
                case 6:
                    System.out.println("Encerrando o programa...");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        } while (opcao != 6);

        scanner.close();

        String gerente = gerenteMemoria.toString();
        System.out.println(gerente);
        gerenteMemoria.imprimirEstatisticas();
        System.out.printf("Fragmentação final da heap: %.2f%%\n", gerenteMemoria.calcularFragmentacaoTotal());
    }
}