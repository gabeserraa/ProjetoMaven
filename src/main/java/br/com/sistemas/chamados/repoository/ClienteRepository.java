packge.br.com.sistemas.chamados.repository;

import br.com.sistemas.chamados.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Cliente, Long> {
    boolean exitsByEmail(String email);
    boolean exitsByEmailAndIdNot(String email, Long id);
}