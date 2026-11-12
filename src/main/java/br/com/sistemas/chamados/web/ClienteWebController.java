package br.com.sistemas.chamados.web;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sistemas.chamados.dto.ClienteRequest;
import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/app/clientes")
public class ClienteWebController {

    private final ClienteService service;
    private final ChamadoService chamadoService;

    public ClienteWebController(ClienteService service, ChamadoService chamadoService) {
        this.service = service;
        this.chamadoService = chamadoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", service.listar());
        return "clientes/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", service.buscar(id));
        model.addAttribute("chamados", chamadoService.listarPorCliente(id,
            Pageable.unpaged(Sort.by(Sort.Direction.DESC, "dataAbertura"))).getContent());
        return "clientes/detalhe";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new ClienteRequest(null, null, null));
        return "clientes/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("cliente") ClienteRequest dto, BindingResult result,
                        RedirectAttributes redirect) {
        if (result.hasErrors()) return "clientes/form";
        try {
            service.criar(dto);
        } catch (RegraNegocioException ex) {
            result.reject("regra", ex.getMessage());
            return "clientes/form";
        }
        redirect.addFlashAttribute("sucesso", "Cliente cadastrado.");
        return "redirect:/app/clientes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        ClienteResponse c = service.buscar(id);
        model.addAttribute("id", id);
        model.addAttribute("cliente", new ClienteRequest(c.nome(), c.email(), c.telefone()));
        return "clientes/form";
    }

    @PutMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("cliente") ClienteRequest dto,
                            BindingResult result, Model model, RedirectAttributes redirect) {
        model.addAttribute("id", id);
        if (result.hasErrors()) return "clientes/form";
        try {
            service.atualizar(id, dto);
        } catch (RegraNegocioException ex) {
            result.reject("regra", ex.getMessage());
            return "clientes/form";
        }
        redirect.addFlashAttribute("sucesso", "Alterações salvas.");
        return "redirect:/app/clientes/" + id;
    }

    @DeleteMapping("/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        service.excluir(id);
        redirect.addFlashAttribute("sucesso", "Cliente excluído.");
        return "redirect:/app/clientes";
    }
}
