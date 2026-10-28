package br.com.sistemas.chamados.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ChamadoResponse;
import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.entity.Tecnico;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.repository.ChamadoRepository;
import br.com.sistemas.chamados.repository.ComentarioRepository;

@ExtendWith(MockitoExtension.class)
class ChamadoServiceTest {

    @Mock
    private ChamadoRepository repository;

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private TecnicoService tecnicoService;

    @InjectMocks
    private ChamadoService service;

    private Chamado chamado;

    @BeforeEach
    void setUp() {
        chamado = new Chamado("Impressora", "Não imprime", Prioridade.ALTA,
            new Cliente("Ana Souza", "ana@exemplo.com", null), new Categoria("Hardware", null), null);
    }

    @Test
    void chamadoNasceAbertoComDataAbertura() {
        assertEquals(StatusChamado.ABERTO, chamado.getStatus());
        assertNotNull(chamado.getDataAbertura());
        assertNull(chamado.getDataFechamento());
    }

    @Test
    void transicaoValidaAlteraStatus() {
        when(repository.findById(1L)).thenReturn(Optional.of(chamado));

        ChamadoResponse resposta = service.alterarStatus(1L, StatusChamado.EM_ATENDIMENTO);

        assertEquals(StatusChamado.EM_ATENDIMENTO, resposta.status());
    }

    @Test
    void fecharPreencheDataFechamento() {
        when(repository.findById(1L)).thenReturn(Optional.of(chamado));

        ChamadoResponse resposta = service.alterarStatus(1L, StatusChamado.FECHADO);

        assertEquals(StatusChamado.FECHADO, resposta.status());
        assertNotNull(resposta.dataFechamento());
    }

    @Test
    void transicaoInvalidaLancaRegraNegocio() {
        when(repository.findById(1L)).thenReturn(Optional.of(chamado));

        assertThrows(RegraNegocioException.class,
            () -> service.alterarStatus(1L, StatusChamado.AGUARDANDO_CLIENTE));
        assertEquals(StatusChamado.ABERTO, chamado.getStatus());
    }

    @Test
    void chamadoFechadoNaoPodeMudarDeStatus() {
        chamado.alterarStatus(StatusChamado.FECHADO);
        when(repository.findById(1L)).thenReturn(Optional.of(chamado));

        assertThrows(RegraNegocioException.class,
            () -> service.alterarStatus(1L, StatusChamado.EM_ATENDIMENTO));
    }

    @Test
    void chamadoFechadoNaoPodeSerAtualizado() {
        chamado.alterarStatus(StatusChamado.FECHADO);
        when(repository.findById(1L)).thenReturn(Optional.of(chamado));
        ChamadoRequest dto = new ChamadoRequest("Novo título", "Nova descrição", Prioridade.BAIXA, 1L, 1L, null);

        assertThrows(RegraNegocioException.class, () -> service.atualizar(1L, dto));
    }

    @Test
    void atribuirDefineOTecnico() {
        when(repository.findById(1L)).thenReturn(Optional.of(chamado));
        when(tecnicoService.buscarEntity(5L))
            .thenReturn(new Tecnico("Bruno Lima", "bruno@exemplo.com", Perfil.TECNICO));

        ChamadoResponse resposta = service.atribuir(1L, 5L);

        assertEquals("Bruno Lima", resposta.tecnico().nome());
    }

    @Test
    void chamadoFechadoNaoRecebeTecnico() {
        chamado.alterarStatus(StatusChamado.FECHADO);
        when(repository.findById(1L)).thenReturn(Optional.of(chamado));

        assertThrows(RegraNegocioException.class, () -> service.atribuir(1L, 5L));
        verify(tecnicoService, never()).buscarEntity(any());
    }
}
