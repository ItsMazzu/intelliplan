package com.planner.notificacao.entity;

import com.planner.usuario.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notificacoes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_notificacao_evento",
                columnNames = {"usuario_id", "chave_evento"}
        ),
        indexes = @Index(name = "ix_notificacao_usuario_criada", columnList = "usuario_id, criada_em")
)
public class Notificacao {

    public enum Tipo {
        LEMBRETE_TAREFA,
        TAREFA_ATRASADA,
        LEMBRETE_COMPROMISSO,
        LEMBRETE_HABITO,
        ALERTA_PROJETO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Tipo tipo;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @Column(name = "lida_em")
    private LocalDateTime lidaEm;

    @Column(name = "chave_evento", nullable = false, length = 200)
    private String chaveEvento;

    @Column(name = "tipo_referencia", nullable = false, length = 30)
    private String tipoReferencia;

    @Column(name = "referencia_id", nullable = false)
    private Long referenciaId;

    @Column(name = "url_destino", nullable = false, length = 500)
    private String urlDestino;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public Notificacao() {
        this.criadaEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public LocalDateTime getLidaEm() {
        return lidaEm;
    }

    public void setLidaEm(LocalDateTime lidaEm) {
        this.lidaEm = lidaEm;
    }

    public String getChaveEvento() {
        return chaveEvento;
    }

    public void setChaveEvento(String chaveEvento) {
        this.chaveEvento = chaveEvento;
    }

    public String getTipoReferencia() {
        return tipoReferencia;
    }

    public void setTipoReferencia(String tipoReferencia) {
        this.tipoReferencia = tipoReferencia;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
    }

    public String getUrlDestino() {
        return urlDestino;
    }

    public void setUrlDestino(String urlDestino) {
        this.urlDestino = urlDestino;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
