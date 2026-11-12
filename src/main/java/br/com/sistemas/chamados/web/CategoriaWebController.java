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

import br.com.sistemas.chamados.dto.CategoriaRequest;
import br.com.sistemas.chamados.dto.CategoriaResponse;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.CategoriaService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/app/categorias")
public class CategoriaWebController {

    private final CategoriaService service;

    public CategoriaWebController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", service.listar());
        return "categorias/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("categoria", new CategoriaRequest(null, null));
        return "categorias/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("categoria") CategoriaRequest dto, BindingResult result,
                        RedirectAttributes redirect) {
        if (result.hasErrors()) return "categorias/form";
        try {
            service.criar(dto);
        } catch (RegraNegocioException ex) {
            result.reject("regra", ex.getMessage());
            return "categorias/form";
        }
        redirect.addFlashAttribute("sucesso", "Categoria cadastrada.");
        return "redirect:/app/categorias";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        CategoriaResponse c = service.buscar(id);
        model.addAttribute("id", id);
        model.addAttribute("categoria", new CategoriaRequest(c.nome(), c.descricao()));
        return "categorias/form";
    }

    @PutMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("categoria") CategoriaRequest dto,
                            BindingResult result, Model model, RedirectAttributes redirect) {
        model.addAttribute("id", id);
        if (result.hasErrors()) return "categorias/form";
        try {
            service.atualizar(id, dto);
        } catch (RegraNegocioException ex) {
            result.reject("regra", ex.getMessage());
            return "categorias/form";
        }
        redirect.addFlashAttribute("sucesso", "Alterações salvas.");
        return "redirect:/app/categorias";
    }

    @DeleteMapping("/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        service.excluir(id);
        redirect.addFlashAttribute("sucesso", "Categoria excluída.");
        return "redirect:/app/categorias";
    }
}
