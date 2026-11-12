package br.com.sistemas.chamados.web;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.service.ChamadoService;

@Controller
public class PainelController {

    private final ChamadoService chamadoService;

    public PainelController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    @GetMapping("/")
    public String painel(Model model) {
        List<ChamadoResponse> chamados = chamadoService.listar(null, null, null,
            Pageable.unpaged(Sort.by(Sort.Direction.DESC, "dataAbertura"))).getContent();

        Map<StatusChamado, Long> porStatus = new LinkedHashMap<>();
        for (StatusChamado s : StatusChamado.values()) {
            porStatus.put(s, chamados.stream().filter(c -> c.status() == s).count());
        }

        model.addAttribute("porStatus", porStatus);
        model.addAttribute("atencao", chamados.stream()
            .filter(c -> c.status() != StatusChamado.FECHADO)
            .filter(c -> c.prioridade() == Prioridade.CRITICA || c.prioridade() == Prioridade.ALTA || c.tecnico() == null)
            .limit(5).toList());
        model.addAttribute("recentes", chamados.stream().limit(5).toList());
        return "painel";
    }
}
