package br.com.sistemas.chamados.web;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemas.chamados.dto.ChamadoDetalheResponse;
import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.dto.ComentarioRequest;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import br.com.sistemas.chamados.service.ComentarioService;
import br.com.sistemas.chamados.service.TecnicoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/app/chamados")
public class ChamadoWebController {

    private final ChamadoService service;
    private final ComentarioService comentarioService;
    private final ClienteService clienteService;
    private final CategoriaService categoriaService;
    private final TecnicoService tecnicoService;

    public ChamadoWebController(ChamadoService service, ComentarioService comentarioService,
                                ClienteService clienteService, CategoriaService categoriaService,
                                TecnicoService tecnicoService) {
        this.service = service;
        this.comentarioService = comentarioService;
        this.clienteService = clienteService;
        this.categoriaService = categoriaService;
        this.tecnicoService = tecnicoService;
    }

    @ModelAttribute("todosStatus")
    public StatusChamado[] todosStatus() {
        return StatusChamado.values();
    }

    @ModelAttribute("prioridades")
    public Prioridade[] prioridades() {
        return Prioridade.values();
    }

    @GetMapping
    public String listar(@RequestParam(required = false) StatusChamado status,
                         @PageableDefault(size = 10, sort = "dataAbertura", direction = Sort.Direction.DESC) Pageable pageable,
                         Model model) {
        model.addAttribute("pagina", service.listar(status, null, null, pageable));
        model.addAttribute("status", status);
        return "chamados/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("comentario", new ComentarioRequest(null, null));
        return detalhe(id, model, "chamados/detalhe");
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long clienteId, Model model) {
        model.addAttribute("chamado", new ChamadoRequest(null, null, Prioridade.MEDIA, clienteId, null, null));
        return formulario(model);
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("chamado") ChamadoRequest dto, BindingResult result,
                        Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) return formulario(model);
        ChamadoResponse salvo = service.criar(dto);
        redirect.addFlashAttribute("sucesso", "Chamado aberto.");
        return "redirect:/app/chamados/" + salvo.id();
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        ChamadoDetalheResponse c = service.buscar(id);
        model.addAttribute("id", id);
        model.addAttribute("chamado", new ChamadoRequest(c.titulo(), c.descricao(), c.prioridade(),
            c.cliente().id(), c.categoria().id(), c.tecnico() == null ? null : c.tecnico().id()));
        return formulario(model);
    }

    @PutMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("chamado") ChamadoRequest dto,
                            BindingResult result, Model model, RedirectAttributes redirect) {
        model.addAttribute("id", id);
        if (result.hasErrors()) return formulario(model);
        service.atualizar(id, dto);
        redirect.addFlashAttribute("sucesso", "Alterações salvas.");
        return "redirect:/app/chamados/" + id;
    }

    @PostMapping("/{id}/status")
    public String alterarStatus(@PathVariable Long id, @RequestParam StatusChamado status, RedirectAttributes redirect) {
        service.alterarStatus(id, status);
        redirect.addFlashAttribute("sucesso", "Status alterado.");
        return "redirect:/app/chamados/" + id;
    }

    @PostMapping("/{id}/atribuir")
    public String atribuir(@PathVariable Long id, @RequestParam Long tecnicoId, RedirectAttributes redirect) {
        service.atribuir(id, tecnicoId);
        redirect.addFlashAttribute("sucesso", "Técnico atribuído.");
        return "redirect:/app/chamados/" + id;
    }

    @PostMapping("/{id}/comentarios")
    public String comentar(@PathVariable Long id, @Valid @ModelAttribute("comentario") ComentarioRequest dto,
                           BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) return detalhe(id, model, "chamados/detalhe");
        comentarioService.criar(id, dto);
        redirect.addFlashAttribute("sucesso", "Comentário publicado.");
        return "redirect:/app/chamados/" + id + "#comentarios";
    }

    @DeleteMapping("/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        service.excluir(id);
        redirect.addFlashAttribute("sucesso", "Chamado excluído.");
        return "redirect:/app/chamados";
    }

    private String formulario(Model model) {
        model.addAttribute("clientes", clienteService.listar());
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("tecnicos", tecnicoService.listar());
        return "chamados/form";
    }

    private String detalhe(Long id, Model model, String view) {
        ChamadoDetalheResponse chamado = service.buscar(id);
        model.addAttribute("chamado", chamado);
        model.addAttribute("transicoes", service.transicoesPermitidas(chamado.status()));
        model.addAttribute("tecnicos", tecnicoService.listar());
        return view;
    }
}
