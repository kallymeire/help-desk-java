package br.com.helpdesk.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Registro de uma mensagem/andamento dentro de um chamado.
 */
public class Interacao {

    private int id;
    private Pessoa autor;
    private String texto;
    private LocalDateTime dataHora;

    public Interacao(int id, Pessoa autor, String texto) {
        this.id = id;
        this.autor = autor;
        this.texto = texto;
        this.dataHora = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Pessoa getAutor() { return autor; }
    public String getTexto() { return texto; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    @Override
    public String toString() {
        return "[" + dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "] "
                + autor.getNome() + ": " + texto;
    }
}
