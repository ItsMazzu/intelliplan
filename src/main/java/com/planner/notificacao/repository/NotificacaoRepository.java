package com.planner.notificacao.repository;

import com.planner.notificacao.entity.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findTop30ByUsuarioIdOrderByCriadaEmDesc(Long usuarioId);

    long countByUsuarioIdAndLidaEmIsNull(Long usuarioId);

    Optional<Notificacao> findByIdAndUsuarioId(Long id, Long usuarioId);

    @Modifying
    @Query(value = """
            insert into notificacoes (
                titulo, descricao, tipo, criada_em, chave_evento,
                tipo_referencia, referencia_id, url_destino, usuario_id
            ) values (
                :titulo, :descricao, :tipo, current_timestamp, :chaveEvento,
                :tipoReferencia, :referenciaId, :urlDestino, :usuarioId
            )
            on conflict (usuario_id, chave_evento) do nothing
            """, nativeQuery = true)
    int inserirSeNovoEvento(
            @Param("titulo") String titulo,
            @Param("descricao") String descricao,
            @Param("tipo") String tipo,
            @Param("chaveEvento") String chaveEvento,
            @Param("tipoReferencia") String tipoReferencia,
            @Param("referenciaId") Long referenciaId,
            @Param("urlDestino") String urlDestino,
            @Param("usuarioId") Long usuarioId
    );

    @Modifying
    @Query("""
            update Notificacao notificacao
            set notificacao.lidaEm = current_timestamp
            where notificacao.usuario.id = :usuarioId
              and notificacao.lidaEm is null
            """)
    int marcarTodasComoLidas(@Param("usuarioId") Long usuarioId);
}
