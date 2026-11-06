package br.com.sistemas.chamados.service;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.ChamadoDetalheResponse;
import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ComentarioRepository;

@Service
public class ChamadoService {

    private final ChamadoRepository repository;
    private final ComentarioRepository comentarioRepository;
    private final ClienteService clienteService;
    private final CategoriaService categoriaService;
    private final TecnicoService tecnicoService;

    public ChamadoService(ChamadoRepository repository,
                          ComentarioRepository comentarioRepository,
                          ClienteService clienteService,
                          CategoriaService categoriaService,
                          TecnicoService tecnicoService) {
        this.repository = repository;
        this.comentarioRepository = comentarioRepository;
        this.clienteService = clienteService;
        this.categoriaService = categoriaService;
        this.tecnicoService = tecnicoService;
    }

    @Transactional
    public ChamadoResponse criar(ChamadoRequest dto) {
        Cliente cliente = clienteService.buscarEntity(dto.clienteId());
        Categoria categoria = categoriaService.buscarEntity(dto.categoriaId());
        Tecnico tecnico = buscarTecnicoOpcional(dto.tecnicoId());

        Chamado salvo = repository.save(new Chamado(
            dto.titulo(), dto.descricao(), dto.prioridade(), cliente, categoria, tecnico));
        return ChamadoResponse.de(salvo);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listar(StatusChamado status, Prioridade prioridade,
                                        Long clienteId, Pageable pageable) {
        return repository.filtrar(status, prioridade, clienteId, pageable).map(ChamadoResponse::de);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listarPorCliente(Long clienteId, Pageable pageable) {
        clienteService.buscarEntity(clienteId);
        return repository.filtrar(null, null, clienteId, pageable).map(ChamadoResponse::de);
    }

    @Transactional(readOnly = true)
    public ChamadoDetalheResponse buscar(Long id) {
        Chamado chamado = buscarEntity(id);
        return ChamadoDetalheResponse.de(chamado, comentarioRepository.findByChamadoIdOrderByDataAsc(id));
    }

    @Transactional
    public ChamadoResponse atualizar(Long id, ChamadoRequest dto) {
        Chamado chamado = buscarEntity(id);
        impedirAlteracaoSeFechado(chamado);
        chamado.setTitulo(dto.titulo());
        chamado.setDescricao(dto.descricao());
        chamado.setPrioridade(dto.prioridade());
        chamado.setCliente(clienteService.buscarEntity(dto.clienteId()));
        chamado.setCategoria(categoriaService.buscarEntity(dto.categoriaId()));
        chamado.setTecnico(buscarTecnicoOpcional(dto.tecnicoId()));
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public ChamadoResponse atribuir(Long id, Long tecnicoId) {
        Chamado chamado = buscarEntity(id);
        if (chamado.isFechado()) {
            throw new RegraNegocioException("Chamado " + id + " está fechado e não pode receber técnico");
        }
        chamado.setTecnico(tecnicoService.buscarEntity(tecnicoId));
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public ChamadoResponse alterarStatus(Long id, StatusChamado novoStatus) {
        Chamado chamado = buscarEntity(id);
        impedirAlteracaoSeFechado(chamado);
        if (!transicoesPermitidas(chamado.getStatus()).contains(novoStatus)) {
            throw new RegraNegocioException(
                "Transição de " + chamado.getStatus() + " para " + novoStatus + " não é permitida");
        }
        chamado.alterarStatus(novoStatus);
        return ChamadoResponse.de(chamado);
    }

    @Transactional
    public void excluir(Long id) {
        Chamado chamado = buscarEntity(id);
        if (comentarioRepository.existsByChamadoId(id)) {
            throw new RegraNegocioException("Chamado " + id + " possui comentários vinculados e não pode ser excluído");
        }
        repository.delete(chamado);
    }

    public Chamado buscarEntity(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException(
                "Chamado " + id + " não encontrado"));
    }

    private Set<StatusChamado> transicoesPermitidas(StatusChamado atual) {
        return switch (atual) {
            case ABERTO -> Set.of(StatusChamado.EM_ATENDIMENTO, StatusChamado.FECHADO);
            case EM_ATENDIMENTO -> Set.of(StatusChamado.AGUARDANDO_CLIENTE, StatusChamado.FECHADO);
            case AGUARDANDO_CLIENTE -> Set.of(StatusChamado.EM_ATENDIMENTO, StatusChamado.FECHADO);
            case FECHADO -> Set.of();
        };
    }

    private void impedirAlteracaoSeFechado(Chamado chamado) {
        if (chamado.isFechado()) {
            throw new RegraNegocioException("Chamado " + chamado.getId() + " está fechado e não pode ser alterado");
        }
    }

    private Tecnico buscarTecnicoOpcional(Long tecnicoId) {
        return tecnicoId == null ? null : tecnicoService.buscarEntity(tecnicoId);
    }
}
