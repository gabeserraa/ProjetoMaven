package br.com.sistemas.chamados.dto;

import java.time.LocalDateTime;
import java.util.List;

import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Comentario;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;

public record ChamadoDetalheResponse(
    Long id,
    String titulo,
    String descricao,
    LocalDateTime dataAbertura,
    LocalDateTime dataFechamento,
    Prioridade prioridade,
    StatusChamado status,
    ResumoResponse cliente,
    ResumoResponse categoria,
    ResumoResponse tecnico,
    List<ComentarioResponse> comentarios
) {
    public static ChamadoDetalheResponse de(Chamado c, List<Comentario> comentarios) {
        ChamadoResponse r = ChamadoResponse.de(c);
        return new ChamadoDetalheResponse(
            r.id(),
            r.titulo(),
            r.descricao(),
            r.dataAbertura(),
            r.dataFechamento(),
            r.prioridade(),
            r.status(),
            r.cliente(),
            r.categoria(),
            r.tecnico(),
            comentarios.stream().map(ComentarioResponse::de).toList()
        );
    }
}
