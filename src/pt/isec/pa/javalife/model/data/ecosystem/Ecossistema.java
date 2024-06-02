package pt.isec.pa.javalife.model.data.ecosystem;

import pt.isec.pa.javalife.model.data.area.Area;
import pt.isec.pa.javalife.model.data.elements.*;
import pt.isec.pa.javalife.model.data.fsm.FaunaContext;
import pt.isec.pa.javalife.model.gameengine.IGameEngine;
import pt.isec.pa.javalife.model.gameengine.IGameEngineEvolve;
import pt.isec.pa.javalife.model.memento.IMemento;
import pt.isec.pa.javalife.model.memento.IOriginator;
import pt.isec.pa.javalife.model.memento.Memento;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

public class Ecossistema implements IGameEngineEvolve, IOriginator,Serializable {
    private final Set<IElemento> elementos;
    private int totalPassos = 0;
    private int escalaUnidade = 2;
    private int unidadesX = 300;
    private int unidadesY = 300;
    private static int nextFaunaId = 1;
    private static int nextFloraId = 1;
    private static int nextInanimadoId = 1;
    private final PropertyChangeSupport support;
    private boolean solAtivo = false;
    private long tempoSolRestante = 0;
    private final Map<Fauna, Double> velocidadesOriginais = new HashMap<>();


    public Ecossistema() {
        this.elementos = new HashSet<>();
        this.support = new PropertyChangeSupport(this);
    }

    public int getLargura() {
        return unidadesX;
    }

    public int getAltura() {
        return unidadesY;
    }

    public void definirUnidadesX(int unidadesX) {
        this.unidadesX = unidadesX;
    }

    public void definirUnidadesY(int unidadesY) {
        this.unidadesY = unidadesY;
    }

    public IElemento encontrarElementoMaisProximo(Area origem, Elemento tipo) {
        IElemento maisProximo = null;
        double menorDistancia = Double.MAX_VALUE;
        for (IElemento elemento : elementos) {
            if (elemento.getTipo() == tipo) {
                double distancia = Area.distancia(origem, elemento.getArea());
                if (distancia < menorDistancia) {
                    menorDistancia = distancia;
                    maisProximo = elemento;
                }
            }
        }
        return maisProximo;
    }

    public boolean isAreaFree(Area area) {
        for (IElemento elemento : elementos) {
            if (elemento.getArea().intersecta(area)) {
                return false;
            }
        }
        return true;
    }

    public int getUnidadesX() {
        return unidadesX;
    }

    public int getUnidadesY() {
        return unidadesY;
    }

    public Set<IElemento> getElementos() {
        return elementos;
    }

    public Set<IElemento> obterElementos() {
        return elementos;
    }

    public int obterPassos() {
        return totalPassos;
    }

    public void resetarContadorDePassos() {
        totalPassos = 0;
    }

    public Fauna encontrarFaunaMaisFraca(int ignorarID) {
        double menorForca = Double.MAX_VALUE;
        Fauna maisFraca = null;

        for (IElemento elemento : elementos) {
            if (elemento.getTipo() == Elemento.FAUNA && elemento.getId() != ignorarID) {
                Fauna fauna = (Fauna) elemento;
                if (fauna.getForca() < menorForca && maisFraca == null) {
                    menorForca = fauna.getForca();
                    maisFraca = fauna;
                }
            }
        }
        return maisFraca;
    }

    public void limparElementos() {
        elementos.clear();
        support.firePropertyChange("elementos", null, null); // Notifica a mudança
    }

    public Fauna encontrarFaunaMaisForte(int ignorarID) {
        double maiorForca = 0;
        Fauna maisForte = null;
        for (IElemento elemento : elementos) {
            if (elemento.getTipo() == Elemento.FAUNA && elemento.getId() != ignorarID) {
                Fauna fauna = (Fauna) elemento;
                if (fauna.getForca() > maiorForca) {
                    maiorForca = fauna.getForca();
                    maisForte = fauna;
                }
            }
        }
        return maisForte;
    }

    public IElemento buscarElemento(int id) {
        return elementos.stream().filter(elemento -> elemento.getId() == id).findFirst().orElse(null);
    }

    // Método para aplicar herbicida em um elemento do tipo Flora
    public void aplicarHerbicida(Flora flora) {
        if (flora != null) {
            removerElemento(flora.getId());
            support.firePropertyChange("elemento_removido", null, flora);
        }
    }

    // Método para injetar força em um elemento do tipo Fauna
    public void injetarForca(Fauna fauna) {
        if (fauna != null) {
            fauna.setForca(fauna.getForca() + 50);
            support.firePropertyChange("forca_injetada", null, fauna);
        }
    }

    // Método para aplicar o efeito do Sol
//    public void aplicarSol() {
//        solAtivo = true;
//        tempoSolRestante = 10; // 10 unidades de tempo
//        support.firePropertyChange("sol_aplicado", null, null);
//    }


    // Método para aplicar o efeito do Sol
//    public void aplicarSol() {
//        solAtivo = true;
//        tempoSolRestante = 10; // 10 unidades de tempo
//        for (IElemento elemento : elementos) {
//            if (elemento instanceof Fauna) {
//                Fauna fauna = (Fauna) elemento;
//                velocidadesOriginais.put(fauna, fauna.getVelocidade());
//                fauna.setVelocidade(fauna.getVelocidade() / 2); // Reduz a velocidade à metade
//            }
//        }
//        support.firePropertyChange("sol_aplicado", null, null);
//    }

    public void removerElemento(int id) {
        IElemento elemento = buscarElemento(id);
        if (elemento instanceof Inanimado) {
            Inanimado inanimado = (Inanimado) elemento;
            if (inanimado.isPertenceACerca()) {
                System.out.println("Não é possível remover um agente inanimado da cerca.");
                return;
            }
        }
        if (elemento != null) {
            elementos.remove(elemento);
            support.firePropertyChange("elemento_removido", null, elemento); // Notifica a mudança
        }
    }

    public void adicionarElemento(IElemento elemento) {
        elementos.add(elemento);
    }

    public boolean verificarAreaLivre(Area area) {
        for (IElemento elemento : elementos) {
            if (elemento.getArea().intersecta(area)) {
                return false;
            }
        }
        return true;
    }



    public boolean verificarLimites(Area area) {
        return area.esquerda() < 0 || area.direita() > getLargura() || area.cima() < 0 || area.baixo() > getAltura();
    }

//    @Override
//    public void evolve(IGameEngine gameEngine, long currentTime) {
//        Set<IElemento> elementosParaRemover = new HashSet<>();
//
//        for (IElemento elemento : new HashSet<>(elementos)) {
//            if (elemento.getTipo() == Elemento.FAUNA) {
//                Fauna fauna = (Fauna) elemento;
//                FaunaContext context = fauna.getFaunaContext();
//                context.setData(fauna);
//                boolean mudou = context.executar();
//
//                if (!fauna.isVivo()) {
//                    elementosParaRemover.add(fauna);
//                }
//            } else if (elemento.getTipo() == Elemento.FLORA) {
//                Flora flora = (Flora) elemento;
//                flora.setForca(flora.getForca() + 0.5);
//                if (flora.getForca() >= 90 && flora.getNumeroReproducoes() < 2) {
//                    Area areaLivre = encontrarAreaAdjacenteLivre(flora.getArea());
//                    if (areaLivre != null) {
//                        Flora novaFlora = criarFlora(areaLivre, 50, flora.getImagem());
//                        adicionarElemento(novaFlora);
//                        flora.setForca(50);
//                        flora.incrementaNumeroReproducoes();
//                    }
//                }
//            }
//        }
//
//        elementos.removeAll(elementosParaRemover);
//        totalPassos++;
//        support.firePropertyChange("evolucao", null, null); // Notifica a mudança
//    }
@Override
public void evolve(IGameEngine gameEngine, long currentTime) {
    Set<IElemento> elementosParaRemover = new HashSet<>();

    for (IElemento elemento : new HashSet<>(elementos)) {
        if (elemento.getTipo() == Elemento.FAUNA) {
            Fauna fauna = (Fauna) elemento;
            FaunaContext context = fauna.getFaunaContext();
            context.setData(fauna);
            context.executar();


            for (IElemento outroElemento : elementos) {
                if (outroElemento instanceof Flora) {
                    Flora flora = (Flora) outroElemento;
                        if (flora.getForca() <= 0) {
                            elementosParaRemover.add(flora);
                        }
                }
            }

//            // Se o sol estiver ativo, a fauna se move à metade da velocidade
//            if (solAtivo) {
//                fauna.setVelocidade(fauna.getVelocidade() / 2);
//            }

            if (!fauna.isVivo()) {
                elementosParaRemover.add(fauna);
            }
        } else if (elemento.getTipo() == Elemento.FLORA) {
           // Flora flora = (Flora) elemento;
             //   flora.setForca(flora.getForca() + 0.5);


            Flora flora = (Flora) elemento;
            if (solAtivo) {
                flora.setForca(flora.getForca() + 1.0); // Flora ganha força ao dobro da velocidade
            } else {
                flora.setForca(flora.getForca() + 0.5);
            }
            flora.evolve(this, currentTime);

//            if (flora.getForca() >= 90 && flora.getNumeroReproducoes() < 2) {
//
//                Area areaLivre = encontrarAreaAdjacenteLivre(flora.getArea());
//                if (areaLivre != null) {
//                    Flora novaFlora = criarFlora(areaLivre, 50, flora.getImagem());
//                    adicionarElemento(novaFlora);
//                    flora.setForca(50);
//                    flora.incrementaNumeroReproducoes();
//                }
//            }
        }
    }

//    // Atualiza o tempo restante do efeito do sol
//    if (solAtivo) {
//        tempoSolRestante--;
//        // System.out.println(tempoSolRestante);
//        if (tempoSolRestante <= 0) {
//            solAtivo = false;
//            support.firePropertyChange("sol_expirado", null, null);
//        }
//    }

    if (solAtivo) {
        tempoSolRestante--;
        if (tempoSolRestante <= 0) {
            solAtivo = false;
            for (Fauna fauna : velocidadesOriginais.keySet()) {
                fauna.setVelocidade(velocidadesOriginais.get(fauna));
            }
            velocidadesOriginais.clear();
            support.firePropertyChange("sol_expirado", null, null);
        }
    }

    elementos.removeAll(elementosParaRemover);
    totalPassos++;
    support.firePropertyChange("evolucao", null, null); // Notifica a mudança

}

public void aplicarSol() {
        solAtivo = true;
        tempoSolRestante = 10; // 10 unidades de tempo
        for (IElemento elemento : elementos) {
            if (elemento instanceof Fauna) {
                Fauna fauna = (Fauna) elemento;
                velocidadesOriginais.put(fauna, fauna.getVelocidade());
                fauna.setVelocidade(fauna.getVelocidade() / 2); // Reduz a velocidade à metade
            }
        }
        support.firePropertyChange("sol_aplicado", null, null);
    }


    public void cerca() {
        int espessuraParede = 4;

        // Parede Superior
        Inanimado topo = new Inanimado(0, 0,espessuraParede,this.getLargura(),true);
        topo.setArea(0, 0, espessuraParede, this.getLargura());

        // Parede Inferior
        Inanimado fundo = new Inanimado(0, 0,espessuraParede,this.getLargura(),true);
        fundo.setArea(this.getAltura() - espessuraParede, 0, this.getAltura(), this.getLargura());

        // Parede Esquerda
        Inanimado esquerda = new Inanimado(0, 0,espessuraParede,this.getAltura(),true);
        esquerda.setArea(0, 0, this.getAltura(), espessuraParede);

        // Parede Direita
        Inanimado direita = new Inanimado(0, this.getLargura() - espessuraParede,this.getAltura(),espessuraParede,true);
        direita.setArea(0, this.getLargura() - espessuraParede, this.getAltura(), this.getLargura());

        // Adiciona as paredes ao ecossistema
        adicionarElemento(topo);
        adicionarElemento(fundo);
        adicionarElemento(esquerda);
        adicionarElemento(direita);
    }

    public Area encontrarAreaAdjacenteLivre(Area area) {
        double[][] posicoesAdjacentes = {
                {area.esquerda() - area.direita(), area.cima()},
                {area.esquerda() + area.direita(), area.cima()},
                {area.esquerda(), area.cima() - area.baixo()},
                {area.esquerda(), area.cima() + area.baixo()}
        };

        for (double[] posicao : posicoesAdjacentes) {
            Area novaArea = new Area(posicao[1], posicao[0], posicao[1] + area.baixo() - area.cima(), posicao[0] + area.direita() - area.esquerda());
            if (verificarAreaLivre(novaArea) && !verificarLimites(novaArea)) {
                return novaArea;
            }
        }

        return null;
    }

    public int gerarProximoIdFauna() {
        return nextFaunaId++;
    }

    public int gerarProximoIdFlora() {
        return nextFloraId++;
    }

    public int gerarProximoIdInanimado() {
        return nextInanimadoId++;
    }

    public Fauna criarFauna(double cima, double esquerda) {
        Fauna fauna = new Fauna(cima, esquerda, this);
        adicionarElemento(fauna);
        return fauna;
    }

    public Flora criarFlora(Area area, double forca, String imagem) {
        Flora flora = new Flora(area.cima(), area.esquerda());
        flora.setForca(forca);
        flora.setImagem(imagem);
        adicionarElemento(flora);
        return flora;
    }

    public Inanimado criarInanimado(Area area) {
        Inanimado inanimado = new Inanimado(area.cima(), area.esquerda());
        adicionarElemento(inanimado);
        return inanimado;
    }

    public void setEcossistema(Ecossistema novoEcossistema) {
        limparElementos();
        elementos.addAll(novoEcossistema.obterElementos());
        support.firePropertyChange("ecossistema_atualizado", null, this); // Notifica a mudança
    }

    public void adicionarElementoAleatoriamente(Elemento tipoElemento) {
        int altura = getUnidadesY();
        int largura = getUnidadesX();
        Random random = new Random();

        boolean adicionado = false;
        while (!adicionado) {
            double x = random.nextInt(largura);
            double y = random.nextInt(altura);

            Area area = null;
            IElemento elemento = null;
            Random rand = new Random();
            int valorRandom = 10 + rand.nextInt(50 - 10 + 1);

            switch (tipoElemento) {
                case INANIMADO:
                    area = new Area(y, x, y + valorRandom, x + valorRandom);
                    if (isAreaFree(area)) {
                        elemento = new Inanimado(y, x);
                        adicionarElemento(elemento);
                        adicionado = true;
                    }
                    break;
                case FAUNA:
                    area = new Area(y, x, y + valorRandom, x + valorRandom); // Supondo tamanho de fauna como 32x32
                    if (isAreaFree(area)) {
                        elemento = new Fauna(y, x, this);
                        adicionarElemento(elemento);
                        adicionado = true;
                    }
                    break;
                case FLORA:
                    area = new Area(y, x, y + valorRandom, x + valorRandom); // Supondo tamanho de flora como 13x13
                    if (isAreaFree(area)) {
                        elemento = new Flora(y, x);
                        adicionarElemento(elemento);
                        adicionado = true;
                    }
                    break;
            }
        }
    }

    // Métodos para gerenciar os listeners
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public void exportarElementosParaCSV(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.append("Tipo;Forca;PosX;PosY\n");
            for (IElemento elemento : elementos) {
                String tipo = elemento.getClass().getSimpleName();
                double forca = (elemento instanceof IElementoComForca) ? ((IElementoComForca) elemento).getForca() : 0;
                double posX = elemento.getArea().esquerda();
                double posY = elemento.getArea().cima();
                writer.append(String.format("%s;%.2f;%.2f;%.2f\n", tipo, forca, posX, posY));
            }
        }
    }

    public void importarElementosDeCSV(File file) throws IOException {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.FRANCE); // Utiliza Locale.FRANCE para tratar vírgulas como separadores decimais

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // Ignora a linha do cabeçalho
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length < 4) continue; // Verifica se a linha tem ao menos 4 colunas

                String tipo = parts[0];
                double forca = parseDouble(parts[1], numberFormat);
                double posX = parseDouble(parts[2], numberFormat);
                double posY = parseDouble(parts[3], numberFormat);

                double largura = 13; // Tamanho padrão, ajuste conforme necessário
                double altura = 13;  // Tamanho padrão, ajuste conforme necessário
                if (tipo.equals("Inanimado")) {
                    largura = altura = 13; // Supondo tamanho fixo para Inanimado, ajuste conforme necessário
                } else if (tipo.equals("Flora")) {
                    largura = altura = 13; // Supondo tamanho fixo para Flora, ajuste conforme necessário
                }

                Area area = new Area(posY, posX, posY + altura, posX + largura);
                if (verificarAreaLivre(area) && !verificarLimites(area)) {
                    switch (tipo) {
                        case "Fauna":
                            Fauna fauna = new Fauna(posY, posX, this);
                            fauna.setForca(forca);
                            adicionarElemento(fauna);
                            break;
                        case "Flora":
                            Flora flora = new Flora(posY, posX);
                            flora.setForca(forca);
                            adicionarElemento(flora);
                            break;
                        case "Inanimado":
                            Inanimado inanimado = new Inanimado(posY, posX);
                            adicionarElemento(inanimado);
                            break;
                    }
                }
            }
        }
        support.firePropertyChange("elementos_importados", null, null); // Notifica a mudança
    }

    private double parseDouble(String value, NumberFormat numberFormat) {
        try {
            return numberFormat.parse(value).doubleValue();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Formato de número inválido: " + value, e);
        }
    }

    public void setLargura(int largura) {
        this.unidadesX = largura;
    }

    public void setAltura(int altura) {
        this.unidadesY = altura;
    }

    public Flora criarFloraComTamanho(Area area, double forca, String imagem, int largura, int altura) {
        Flora flora = new Flora(area.cima(), area.esquerda(), largura, altura);
        flora.setForca(forca);
        flora.setImagem(imagem);
        adicionarElemento(flora);
        System.out.println("Nova flora criada: " + flora);
        return flora;
    }
    @Override
    public IMemento save() {
        return new Memento(this);
    }
    @Override
    public void restore(IMemento memento) {
        Ecossistema estado = (Ecossistema) memento.getSnapshot();
        this.elementos.clear();
        this.elementos.addAll(estado.obterElementos());
        this.totalPassos = estado.obterPassos();
        this.unidadesX = estado.getUnidadesX();
        this.unidadesY = estado.getUnidadesY();
    }
}