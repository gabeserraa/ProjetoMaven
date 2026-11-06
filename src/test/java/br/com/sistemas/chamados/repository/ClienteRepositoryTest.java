package br.com.sistemas.chamados.repository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import br.com.sistemas.chamados.entity.Cliente;

@DataJpaTest
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository repository;

    @Test
    void existsByEmailIndicaSeOEmailJaFoiCadastrado() {
        repository.save(new Cliente("Ana Souza", "ana@exemplo.com", "47999999999"));

        assertTrue(repository.existsByEmail("ana@exemplo.com"));
        assertFalse(repository.existsByEmail("outro@exemplo.com"));
    }

    @Test
    void existsByEmailAndIdNotIgnoraOProprioCliente() {
        Cliente ana = repository.save(new Cliente("Ana Souza", "ana@exemplo.com", null));
        Cliente bruno = repository.save(new Cliente("Bruno Lima", "bruno@exemplo.com", null));

        assertFalse(repository.existsByEmailAndIdNot("ana@exemplo.com", ana.getId()));
        assertTrue(repository.existsByEmailAndIdNot("ana@exemplo.com", bruno.getId()));
    }
}
