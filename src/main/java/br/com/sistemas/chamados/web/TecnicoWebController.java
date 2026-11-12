package br.com.sistemas.chamados.web;

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

import br.com.sistemas.chamados.dto.TecnicoRequest;
import br.com.sistemas.chamados.dto.TecnicoResponse;
import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.TecnicoService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/app/tecnicos")
public class TecnicoWebController {

    private final TecnicoService service;

    public TecnicoWebController(TecnicoService service) {
        this.service = service;
    }

    @ModelAttribute("perfis")
    public Perfil[] perfis() {
        return Perfil.values();
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tecnicos", service.listar());
        return "tecnicos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("tecnico", new TecnicoRequest(null, null, Perfil.TECNICO));
        return "tecnicos/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("tecnico") TecnicoRequest dto, BindingResult result,
                        RedirectAttributes redirect) {
        if (result.hasErrors()) return "tecnicos/form";
        try {
            service.criar(dto);
        } catch (RegraNegocioException ex) {
            result.reject("regra", ex.getMessage());
            return "tecnicos/form";
        }
        redirect.addFlashAttribute("sucesso", "Técnico cadastrado.");
        return "redirect:/app/tecnicos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        TecnicoResponse t = service.buscar(id);
        model.addAttribute("id", id);
        model.addAttribute("tecnico", new TecnicoRequest(t.nome(), t.email(), t.perfil()));
        return "tecnicos/form";
    }

    @PutMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("tecnico") TecnicoRequest dto,
                            BindingResult result, Model model, RedirectAttributes redirect) {
        model.addAttribute("id", id);
        if (result.hasErrors()) return "tecnicos/form";
        try {
            service.atualizar(id, dto);
        } catch (RegraNegocioException ex) {
            result.reject("regra", ex.getMessage());
            return "tecnicos/form";
        }
        redirect.addFlashAttribute("sucesso", "Alterações salvas.");
        return "redirect:/app/tecnicos";
    }

    @DeleteMapping("/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        service.excluir(id);
        redirect.addFlashAttribute("sucesso", "Técnico excluído.");
        return "redirect:/app/tecnicos";
    }
}
