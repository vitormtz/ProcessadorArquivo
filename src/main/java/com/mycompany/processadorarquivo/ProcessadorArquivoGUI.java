/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.processadorarquivo;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author vitor
 */
public class ProcessadorArquivoGUI extends JFrame {

    private JTextField txtArquivoEntrada;
    private JTextField txtArquivoSaida;
    private JTextArea txtLog;
    private JButton btnSelecionarEntrada;
    private JButton btnSelecionarSaida;
    private JButton btnProcessar;

    public ProcessadorArquivoGUI() {
        // Configuração da janela
        setTitle("Processador de Arquivo");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Criação do painel principal
        JPanel painelPrincipal = new JPanel();
        painelPrincipal.setLayout(new BorderLayout(10, 10));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Painel para seleção de arquivos
        JPanel painelArquivos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Arquivo de entrada
        gbc.gridx = 0;
        gbc.gridy = 0;
        painelArquivos.add(new JLabel("Arquivo de Entrada:"), gbc);

        txtArquivoEntrada = new JTextField(30);
        txtArquivoEntrada.setEditable(false);
        gbc.gridx = 1;
        painelArquivos.add(txtArquivoEntrada, gbc);

        btnSelecionarEntrada = new JButton("Selecionar");
        gbc.gridx = 2;
        painelArquivos.add(btnSelecionarEntrada, gbc);

        // Arquivo de saída
        gbc.gridx = 0;
        gbc.gridy = 1;
        painelArquivos.add(new JLabel("Arquivo de Saída:"), gbc);

        txtArquivoSaida = new JTextField(30);
        txtArquivoSaida.setEditable(false);
        gbc.gridx = 1;
        painelArquivos.add(txtArquivoSaida, gbc);

        btnSelecionarSaida = new JButton("Selecionar");
        gbc.gridx = 2;
        painelArquivos.add(btnSelecionarSaida, gbc);

        // Botão processar
        btnProcessar = new JButton("Processar Arquivo");
        btnProcessar.setEnabled(false);
        gbc.gridx = 1;
        gbc.gridy = 2;
        painelArquivos.add(btnProcessar, gbc);

        // Área de log
        txtLog = new JTextArea(10, 40);
        txtLog.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(txtLog);

        // Adiciona os componentes ao painel principal
        painelPrincipal.add(painelArquivos, BorderLayout.NORTH);
        painelPrincipal.add(scrollLog, BorderLayout.CENTER);

        // Adiciona o painel principal à janela
        add(painelPrincipal);

        // Configuração dos eventos dos botões
        configurarEventos();
    }

    private void configurarEventos() {
        btnSelecionarEntrada.addActionListener(e -> selecionarArquivo(txtArquivoEntrada, "Selecionar arquivo de entrada"));
        btnSelecionarSaida.addActionListener(e -> selecionarArquivo(txtArquivoSaida, "Selecionar arquivo de saída"));

        btnProcessar.addActionListener(e -> {
            try {
                processarArquivo(txtArquivoEntrada.getText(), txtArquivoSaida.getText());
                JOptionPane.showMessageDialog(this, "Arquivo processado com sucesso!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao processar arquivo: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void selecionarArquivo(JTextField campo, String titulo) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(titulo);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            campo.setText(fileChooser.getSelectedFile().getAbsolutePath());
            verificarCampos();
        }
    }

    private void verificarCampos() {
        btnProcessar.setEnabled(!txtArquivoEntrada.getText().isEmpty()
                && !txtArquivoSaida.getText().isEmpty());
    }

    private void log(String mensagem) {
        txtLog.append(mensagem + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }

    public void processarArquivo(String caminhoEntrada, String caminhoSaida) throws IOException {
        Map<String, ComandoValor> comandos = new HashMap<>();

        log("Iniciando processamento do arquivo...");
        log("Lendo arquivo de entrada: " + caminhoEntrada);

        // Lê o arquivo e encontra o menor valor para cada comando
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoEntrada))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                if (linha.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linha.split("\"");
                if (partes.length >= 2) {
                    String comando = partes[0].trim();
                    String valorStr = partes[1].trim();
                    double valor = Double.parseDouble(valorStr);

                    // Se o comando já existe, verifica se o novo valor é menor
                    if (!comandos.containsKey(comando) || valor < comandos.get(comando).valor) {
                        comandos.put(comando, new ComandoValor(linha, valor));
                        log("Encontrado " + comando + " com valor " + valor);
                    }
                }
            }
        }

        log("\nProcessando comandos...");

        // Escreve apenas os comandos com menor valor no arquivo de saída
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(caminhoSaida))) {
            for (Map.Entry<String, ComandoValor> entry : comandos.entrySet()) {
                ComandoValor cv = entry.getValue();
                bw.write(cv.linhaOriginal);
                bw.newLine();
                log("Mantendo comando " + entry.getKey() + " com menor valor: " + cv.valor);
            }
        }

        log("\nResumo do processamento:");
        log("Total de comandos únicos encontrados: " + comandos.size());
        log("Arquivo de saída gerado: " + caminhoSaida);
        log("Processamento concluído!\n");
    }

    static class ComandoValor {

        String linhaOriginal;
        double valor;

        ComandoValor(String linhaOriginal, double valor) {
            this.linhaOriginal = linhaOriginal;
            this.valor = valor;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ProcessadorArquivoGUI gui = new ProcessadorArquivoGUI();
            gui.setVisible(true);
        });
    }
}
