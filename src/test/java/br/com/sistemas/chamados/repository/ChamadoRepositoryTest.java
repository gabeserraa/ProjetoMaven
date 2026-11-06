package br.com.sistemas.chamados.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import br.com.sistemas.chamados.entity.Categoria;
import br.com.sistemas.chamados.entity.Chamado;
import br.com.sistemas.chamados.entity.Cliente;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;

@DataJpaTest
class ChamadoRepositoryTest {

    @Autowired
    private ChamadoRepository repository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Cliente ana;

    @BeforeEach
    void setUp() {
        ana = clienteRepository.save(new Cliente("Ana Souza", "ana@exemplo.com", null));
        Cliente bruno = clienteRepository.save(new Cliente("Bruno Lima", "bruno@exemplo.com", null));
        Categoria hardware = categoriaRepository.save(new Categoria("Hardware", null));

        repository.save(new Chamado("A", "Impressora", Prioridade.ALTA, ana, hardware, null));
        Chamado fechado = new Chamado("B", "Mouse", Prioridade.BAIXA, ana, hardware, null);
        fechado.alterarStatus(StatusChamado.FECHADO);
        repository.save(fechado);
        repository.save(new Chamado("C", "Monitor", Prioridade.ALTA, bruno, hardware, null));
    }

    @Test
    void semFiltrosRetornaTodos() {
        assertEquals(3, repository.filtrar(null, null, null, PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    void filtraPorStatus() {
        assertEquals(2, repository.filtrar(StatusChamado.ABERTO, null, null, PageRequest.of(0, 10)).getTotalElements());
    }

    @Test
    void combinaFiltrosDePrioridadeECliente() {
        Page<Chamado> pagina = repository.filtrar(null, Prioridade.ALTA, ana.getId(), PageRequest.of(0, 10));

        assertEquals(1, pagina.getTotalElements());
        assertEquals("A", pagina.getContent().get(0).getTitulo());
    }

    @Test
    void paginaEOrdena() {
        Page<Chamado> pagina = repository.filtrar(null, null, null,
            PageRequest.of(0, 2, Sort.by("titulo").descending()));

        assertEquals(3, pagina.getTotalElements());
        assertEquals(2, pagina.getTotalPages());
        assertEquals(2, pagina.getContent().size());
        assertEquals("C", pagina.getContent().get(0).getTitulo());
    }
}
