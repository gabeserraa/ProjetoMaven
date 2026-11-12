package br.com.sistemas.chamados.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.com.sistemas.chamados.dto.CategoriaRequest;
import br.com.sistemas.chamados.dto.ChamadoRequest;
import br.com.sistemas.chamados.dto.ClienteRequest;
import br.com.sistemas.chamados.dto.ComentarioRequest;
import br.com.sistemas.chamados.dto.TecnicoRequest;
import br.com.sistemas.chamados.entity.Perfil;
import br.com.sistemas.chamados.entity.Prioridade;
import br.com.sistemas.chamados.entity.StatusChamado;
import br.com.sistemas.chamados.service.CategoriaService;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;
import br.com.sistemas.chamados.service.ComentarioService;
import br.com.sistemas.chamados.service.TecnicoService;

/**
 * Testa as telas Thymeleaf de ponta a ponta: renderização de todos os templates,
 * envio de formulários (POST, e PUT/DELETE via campo _method), validação e regras de negócio.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TelasIntegracaoTest {

    @Autowired MockMvc mvc;
    @Autowired ClienteService clienteService;
    @Autowired CategoriaService categoriaService;
    @Autowired TecnicoService tecnicoService;
    @Autowired ChamadoService chamadoService;
    @Autowired ComentarioService comentarioService;

    Long clienteId;
    Long categoriaId;
    Long tecnicoId;
    Long chamadoId;
    Long comentarioId;

    @BeforeEach
    void dados() {
        clienteId = clienteService.criar(new ClienteRequest("Mariana Costa", "mariana@teste.com", "(11) 98877-1200")).id();
        categoriaId = categoriaService.criar(new CategoriaRequest("Rede", "Quedas de conexão")).id();
        tecnicoId = tecnicoService.criar(new TecnicoRequest("Diego Ferreira", "diego@teste.com", Perfil.TECNICO)).id();
        chamadoId = chamadoService.criar(new ChamadoRequest("Internet cai", "Cai a cada 2 horas",
            Prioridade.ALTA, clienteId, categoriaId, tecnicoId)).id();
        comentarioId = comentarioService.criar(chamadoId, new ComentarioRequest("Verifiquei o roteador", "Diego")).id();
    }

    @Test
    void todasAsTelasRenderizam() throws Exception {
        List<String> telas = List.of(
            "/",
            "/app/chamados", "/app/chamados?status=ABERTO", "/app/chamados/novo",
            "/app/chamados/" + chamadoId, "/app/chamados/" + chamadoId + "/editar",
            "/app/clientes", "/app/clientes/novo", "/app/clientes/" + clienteId, "/app/clientes/" + clienteId + "/editar",
            "/app/tecnicos", "/app/tecnicos/novo", "/app/tecnicos/" + tecnicoId + "/editar",
            "/app/categorias", "/app/categorias/novo", "/app/categorias/" + categoriaId + "/editar",
            "/app/comentarios", "/app/comentarios/novo", "/app/comentarios/" + comentarioId + "/editar");
        for (String tela : telas) {
            mvc.perform(get(tela)).andExpect(status().isOk());
        }
    }

    @Test
    void painelMostraResumoECssVersionadoPeloResourceChain() throws Exception {
        mvc.perform(get("/"))
            .andExpect(view().name("painel"))
            .andExpect(content().string(containsString("Precisam de atenção")))
            .andExpect(content().string(containsString("Internet cai")))
            .andExpect(content().string(matchesPattern("(?s).*/css/app-[0-9a-f]{32}\\.css.*")));
    }

    @Test
    void formularioDeEdicaoVemPreenchido() throws Exception {
        mvc.perform(get("/app/clientes/" + clienteId + "/editar"))
            .andExpect(content().string(containsString("value=\"mariana@teste.com\"")));
    }

    // ------------------------------------------------------------------ clientes

    @Test
    void criaClienteERedirecionaComMensagem() throws Exception {
        mvc.perform(post("/app/clientes").param("nome", "Lucas Almeida").param("email", "lucas@teste.com"))
            .andExpect(redirectedUrl("/app/clientes"))
            .andExpect(flash().attribute("sucesso", "Cliente cadastrado."));
        assertThat(clienteService.listar()).anyMatch(c -> c.email().equals("lucas@teste.com"));
    }

    @Test
    void clienteInvalidoVoltaAoFormularioComErrosNosCampos() throws Exception {
        mvc.perform(post("/app/clientes").param("nome", "").param("email", "nao-e-email"))
            .andExpect(status().isOk())
            .andExpect(view().name("clientes/form"))
            .andExpect(model().attributeHasFieldErrors("cliente", "nome", "email"))
            .andExpect(content().string(containsString("Nome é obrigatório")))
            .andExpect(content().string(containsString("E-mail inválido")));
    }

    @Test
    void emailDuplicadoMostraMotivoNoFormulario() throws Exception {
        mvc.perform(post("/app/clientes").param("nome", "Outra").param("email", "mariana@teste.com"))
            .andExpect(view().name("clientes/form"))
            .andExpect(model().attributeHasErrors("cliente"))
            .andExpect(content().string(containsString("Já existe cliente com o e-mail mariana@teste.com")));
    }

    @Test
    void editaClienteViaPutDoFormulario() throws Exception {
        mvc.perform(post("/app/clientes/" + clienteId).param("_method", "put")
                .param("nome", "Mariana C. Silva").param("email", "mariana@teste.com"))
            .andExpect(redirectedUrl("/app/clientes/" + clienteId))
            .andExpect(flash().attribute("sucesso", "Alterações salvas."));
        assertThat(clienteService.buscar(clienteId).nome()).isEqualTo("Mariana C. Silva");
    }

    @Test
    void naoExcluiClienteComChamadoEVoltaComOMotivo() throws Exception {
        String origem = "/app/clientes/" + clienteId;
        mvc.perform(post(origem).param("_method", "delete").header("Referer", origem))
            .andExpect(redirectedUrl(origem))
            .andExpect(flash().attribute("erro", containsString("possui chamados vinculados")));
        assertThat(clienteService.buscar(clienteId)).isNotNull();
    }

    @Test
    void excluiClienteSemChamados() throws Exception {
        Long id = clienteService.criar(new ClienteRequest("Sem Chamados", "sem@teste.com", null)).id();
        mvc.perform(post("/app/clientes/" + id).param("_method", "delete"))
            .andExpect(redirectedUrl("/app/clientes"))
            .andExpect(flash().attribute("sucesso", "Cliente excluído."));
        assertThat(clienteService.listar()).noneMatch(c -> c.id().equals(id));
    }

    // ------------------------------------------------------------------ técnicos e categorias

    @Test
    void criaEditaEExcluiTecnico() throws Exception {
        mvc.perform(post("/app/tecnicos").param("nome", "Carla").param("email", "carla@teste.com").param("perfil", "ADMIN"))
            .andExpect(redirectedUrl("/app/tecnicos"));
        Long id = tecnicoService.listar().stream().filter(t -> t.email().equals("carla@teste.com")).findFirst().orElseThrow().id();
        mvc.perform(post("/app/tecnicos/" + id).param("_method", "put")
                .param("nome", "Carla Mendes").param("email", "carla@teste.com").param("perfil", "TECNICO"))
            .andExpect(redirectedUrl("/app/tecnicos"));
        assertThat(tecnicoService.buscar(id).perfil()).isEqualTo(Perfil.TECNICO);
        mvc.perform(post("/app/tecnicos/" + id).param("_method", "delete"))
            .andExpect(flash().attribute("sucesso", "Técnico excluído."));
    }

    @Test
    void criaEExcluiCategoria() throws Exception {
        mvc.perform(post("/app/categorias").param("nome", "Telefonia").param("descricao", "Ramais"))
            .andExpect(redirectedUrl("/app/categorias"));
        Long id = categoriaService.listar().stream().filter(c -> c.nome().equals("Telefonia")).findFirst().orElseThrow().id();
        mvc.perform(post("/app/categorias/" + id).param("_method", "delete"))
            .andExpect(flash().attribute("sucesso", "Categoria excluída."));
    }

    @Test
    void categoriaComNomeRepetidoMostraMotivo() throws Exception {
        mvc.perform(post("/app/categorias").param("nome", "Rede"))
            .andExpect(view().name("categorias/form"))
            .andExpect(content().string(containsString("Já existe categoria com o nome Rede")));
    }

    // ------------------------------------------------------------------ chamados

    @Test
    void abreChamadoPeloFormulario() throws Exception {
        mvc.perform(post("/app/chamados").param("titulo", "Monitor piscando").param("descricao", "Pisca ao ligar")
                .param("prioridade", "CRITICA").param("clienteId", clienteId.toString())
                .param("categoriaId", categoriaId.toString()).param("tecnicoId", ""))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attribute("sucesso", "Chamado aberto."));
        assertThat(chamadoService.listar(null, Prioridade.CRITICA, null, Pageable.unpaged()).getContent())
            .anyMatch(c -> c.titulo().equals("Monitor piscando") && c.tecnico() == null);
    }

    @Test
    void chamadoSemCamposObrigatoriosMostraErros() throws Exception {
        mvc.perform(post("/app/chamados").param("titulo", "").param("descricao", ""))
            .andExpect(view().name("chamados/form"))
            .andExpect(model().attributeHasFieldErrors("chamado", "titulo", "descricao", "clienteId", "categoriaId"));
    }

    @Test
    void fluxoDeStatusRespeitaAsTransicoesDoServico() throws Exception {
        String detalhe = "/app/chamados/" + chamadoId;
        mvc.perform(post(detalhe + "/status").param("status", "AGUARDANDO_CLIENTE").header("Referer", detalhe))
            .andExpect(redirectedUrl(detalhe))
            .andExpect(flash().attribute("erro", containsString("não é permitida")));
        mvc.perform(post(detalhe + "/status").param("status", "EM_ATENDIMENTO"))
            .andExpect(flash().attribute("sucesso", "Status alterado."));
        mvc.perform(post(detalhe + "/status").param("status", "FECHADO"))
            .andExpect(flash().attribute("sucesso", "Status alterado."));
        assertThat(chamadoService.buscar(chamadoId).status()).isEqualTo(StatusChamado.FECHADO);

        // Fechado: tela mostra o aviso e a edição é recusada pelo serviço
        mvc.perform(get(detalhe))
            .andExpect(content().string(containsString("Chamados fechados não aceitam mais edição")));
        mvc.perform(post(detalhe).param("_method", "put").header("Referer", detalhe)
                .param("titulo", "X").param("descricao", "Y").param("prioridade", "BAIXA")
                .param("clienteId", clienteId.toString()).param("categoriaId", categoriaId.toString()))
            .andExpect(redirectedUrl(detalhe))
            .andExpect(flash().attribute("erro", containsString("está fechado")));
    }

    @Test
    void atribuiTecnico() throws Exception {
        Long outro = tecnicoService.criar(new TecnicoRequest("Paula", "paula@teste.com", Perfil.TECNICO)).id();
        mvc.perform(post("/app/chamados/" + chamadoId + "/atribuir").param("tecnicoId", outro.toString()))
            .andExpect(flash().attribute("sucesso", "Técnico atribuído."));
        assertThat(chamadoService.buscar(chamadoId).tecnico().id()).isEqualTo(outro);
    }

    @Test
    void comentaNoChamado() throws Exception {
        mvc.perform(post("/app/chamados/" + chamadoId + "/comentarios").param("texto", "Troquei o cabo").param("autor", "Carla"))
            .andExpect(redirectedUrl("/app/chamados/" + chamadoId + "#comentarios"));
        assertThat(comentarioService.listarPorChamado(chamadoId)).anyMatch(c -> c.texto().equals("Troquei o cabo"));
    }

    @Test
    void comentarioVazioMostraErroNoDetalhe() throws Exception {
        mvc.perform(post("/app/chamados/" + chamadoId + "/comentarios").param("texto", "").param("autor", ""))
            .andExpect(status().isOk())
            .andExpect(view().name("chamados/detalhe"))
            .andExpect(model().attributeHasFieldErrors("comentario", "texto", "autor"));
    }

    @Test
    void naoExcluiChamadoComComentario() throws Exception {
        String detalhe = "/app/chamados/" + chamadoId;
        mvc.perform(post(detalhe).param("_method", "delete").header("Referer", detalhe))
            .andExpect(redirectedUrl(detalhe))
            .andExpect(flash().attribute("erro", containsString("possui comentários")));
    }

    // ------------------------------------------------------------------ comentários

    @Test
    void novoComentarioExigeChamado() throws Exception {
        mvc.perform(post("/app/comentarios").param("texto", "Oi").param("autor", "Ana"))
            .andExpect(view().name("comentarios/form"))
            .andExpect(content().string(containsString("Selecione o chamado")));
    }

    @Test
    void criaComentarioPelaTelaDeComentarios() throws Exception {
        mvc.perform(post("/app/comentarios").param("chamadoId", chamadoId.toString()).param("texto", "Oi").param("autor", "Ana"))
            .andExpect(redirectedUrl("/app/comentarios"));
        assertThat(comentarioService.listarPorChamado(chamadoId)).hasSize(2);
    }

    @Test
    void editaEExcluiComentario() throws Exception {
        mvc.perform(post("/app/comentarios/" + comentarioId).param("_method", "put")
                .param("texto", "Roteador trocado").param("autor", "Diego"))
            .andExpect(redirectedUrl("/app/chamados/" + chamadoId + "#comentarios"));
        assertThat(comentarioService.buscar(comentarioId).texto()).isEqualTo("Roteador trocado");
        mvc.perform(post("/app/comentarios/" + comentarioId).param("_method", "delete").header("Referer", "/app/comentarios"))
            .andExpect(redirectedUrl("/app/comentarios"));
        assertThat(comentarioService.listarPorChamado(chamadoId)).isEmpty();
    }

    // ------------------------------------------------------------------ erros

    @Test
    void registroInexistenteMostraPagina404() throws Exception {
        mvc.perform(get("/app/chamados/999999"))
            .andExpect(status().isNotFound())
            .andExpect(view().name("error/404"))
            .andExpect(content().string(containsString("Chamado 999999 não encontrado")));
    }

    @Test
    void apiRestContinuaRespondendoJson() throws Exception {
        mvc.perform(get("/clientes/999999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.erro").value("Cliente 999999 não encontrado"));
    }
}
