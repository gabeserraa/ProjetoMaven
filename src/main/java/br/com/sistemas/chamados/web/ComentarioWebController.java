package br.com.sistemas.chamados.web;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemas.chamados.dto.ComentarioRequest;
import br.com.sistemas.chamados.dto.ComentarioResponse;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ComentarioService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/app/comentarios")
public class ComentarioWebController {

    private final ComentarioService service;
    private final ChamadoService chamadoService;

    public ComentarioWebController(ComentarioService service, ChamadoService chamadoService) {
        this.service = service;
        this.chamadoService = chamadoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("comentarios", service.listarTodos());
        return "comentarios/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("comentario", new ComentarioRequest(null, null));
        return formulario(model);
    }

    @PostMapping
    public String criar(@RequestParam(required = false) Long chamadoId,
                        @Valid @ModelAttribute("comentario") ComentarioRequest dto, BindingResult result,
                        Model model, RedirectAttributes redirect) {
        if (chamadoId == null) result.reject("chamado", "Selecione o chamado");
        if (result.hasErrors()) return formulario(model);
        service.criar(chamadoId, dto);
        redirect.addFlashAttribute("sucesso", "Comentário publicado.");
        return "redirect:/app/comentarios";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        ComentarioResponse c = service.buscar(id);
        model.addAttribute("id", id);
        model.addAttribute("chamadoId", c.chamadoId());
        model.addAttribute("comentario", new ComentarioRequest(c.texto(), c.autor()));
        return "comentarios/form";
    }

    @PutMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("comentario") ComentarioRequest dto,
                            BindingResult result, Model model, RedirectAttributes redirect) {
        Long chamadoId = service.buscar(id).chamadoId();
        if (result.hasErrors()) {
            model.addAttribute("id", id);
            model.addAttribute("chamadoId", chamadoId);
            return "comentarios/form";
        }
        service.atualizar(id, dto);
        redirect.addFlashAttribute("sucesso", "Comentário atualizado.");
        return "redirect:/app/chamados/" + chamadoId + "#comentarios";
    }

    @DeleteMapping("/{id}")
    public String excluir(@PathVariable Long id, @RequestHeader(value = "Referer", required = false) String anterior,
                          RedirectAttributes redirect) {
        service.excluir(id);
        redirect.addFlashAttribute("sucesso", "Comentário excluído.");
        return "redirect:" + (anterior != null ? anterior : "/app/comentarios");
    }

    private String formulario(Model model) {
        model.addAttribute("chamados", chamadoService.listar(null, null, null, Pageable.unpaged()).getContent());
        return "comentarios/form";
    }
}
