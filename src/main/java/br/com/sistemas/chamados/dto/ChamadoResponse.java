package br.com.sistemas.chamados.dto;

import java.time.LocalDateTime;

import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.entity.Tecnico;

public record ChamadoResponse(
    Long id,
    String titulo,
    String descricao,
    LocalDateTime dataAbertura,
    LocalDateTime dataFechamento,
    Prioridade prioridade,
    StatusChamado status,
    ResumoResponse cliente,
    ResumoResponse categoria,
    ResumoResponse tecnico
) {
    public static ChamadoResponse de(Chamado c) {
        Tecnico t = c.getTecnico();
        return new ChamadoResponse(
            c.getId(),
            c.getTitulo(),
            c.getDescricao(),
            c.getDataAbertura(),
            c.getDataFechamento(),
            c.getPrioridade(),
            c.getStatus(),
            new ResumoResponse(c.getCliente().getId(), c.getCliente().getNome()),
            new ResumoResponse(c.getCategoria().getId(), c.getCategoria().getNome()),
            t == null ? null : new ResumoResponse(t.getId(), t.getNome())
        );
    }
}
