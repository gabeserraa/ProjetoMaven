package br.com.sistemas.chamados.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.sistemas.chamados.dto.ComentarioRequest;
import br.com.sistemas.chamados.dto.ComentarioResponse;
import br.com.sistemas.chamados.service.ComentarioService;
import jakarta.validation.Valid;

@RestController
public class ComentarioController {

    private final ComentarioService service;

    public ComentarioController(ComentarioService service) {
        this.service = service;
    }

    @PostMapping("/chamados/{chamadoId}/comentarios")
    public ResponseEntity<ComentarioResponse> criar(
        @PathVariable Long chamadoId,
        @Valid @RequestBody ComentarioRequest dto) {
        ComentarioResponse criado = service.criar(chamadoId, dto);
        return ResponseEntity.created(URI.create("/comentarios/" + criado.id())).body(criado);
    }

    @GetMapping("/chamados/{chamadoId}/comentarios")
    public List<ComentarioResponse> listarPorChamado(@PathVariable Long chamadoId) {
        return service.listarPorChamado(chamadoId);
    }

    @GetMapping("/comentarios/{id}")
    public ComentarioResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PutMapping("/comentarios/{id}")
    public ComentarioResponse atualizar(
        @PathVariable Long id,
        @Valid @RequestBody ComentarioRequest dto) {
        return service.atualizar(id, dto);
    }

    @DeleteMapping("/comentarios/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
