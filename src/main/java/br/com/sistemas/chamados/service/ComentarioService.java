package br.com.sistemas.chamados.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.ComentarioRequest;
import br.com.sistemas.chamados.dto.ComentarioResponse;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Comentario;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.repository.ComentarioRepository;

@Service
public class ComentarioService {

    private final ComentarioRepository repository;
    private final ChamadoService chamadoService;

    public ComentarioService(ComentarioRepository repository, ChamadoService chamadoService) {
        this.repository = repository;
        this.chamadoService = chamadoService;
    }

    @Transactional
    public ComentarioResponse criar(Long chamadoId, ComentarioRequest dto) {
        Chamado chamado = chamadoService.buscarEntity(chamadoId);
        Comentario salvo = repository.save(new Comentario(dto.texto(), dto.autor(), chamado));
        return ComentarioResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listarPorChamado(Long chamadoId) {
        chamadoService.buscarEntity(chamadoId);
        return repository.findByChamadoIdOrderByDataAsc(chamadoId).stream()
            .map(ComentarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ComentarioResponse buscar(Long id) {
        return ComentarioResponse.de(buscarEntity(id));
    }

    @Transactional
    public ComentarioResponse atualizar(Long id, ComentarioRequest dto) {
        Comentario comentario = buscarEntity(id);
        comentario.setTexto(dto.texto());
        comentario.setAutor(dto.autor());
        return ComentarioResponse.de(comentario);
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(buscarEntity(id));
    }

    private Comentario buscarEntity(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Comentário " + id + " não encontrado"));
    }
}
