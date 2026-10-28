package br.com.sistemas.chamados.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.sistemas.chamados.dto.ClienteResponse;
import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import br.com.sistemas.chamados.service.ChamadoService;
import br.com.sistemas.chamados.service.ClienteService;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService service;

    @MockitoBean
    private ChamadoService chamadoService;

    @Test
    void criarRetorna201ComLocation() throws Exception {
        when(service.criar(any()))
            .thenReturn(new ClienteResponse(1L, "Ana Souza", "ana@exemplo.com", "47999999999"));

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Ana Souza","email":"ana@exemplo.com","telefone":"47999999999"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/clientes/1"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("ana@exemplo.com"));
    }

    @Test
    void dadosInvalidosRetornam400() throws Exception {
        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"","email":"email-invalido","telefone":"123"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detalhes.length()").value(2));
    }

    @Test
    void emailDuplicadoRetorna409() throws Exception {
        when(service.criar(any()))
            .thenThrow(new RegraNegocioException("Já existe cliente com o e-mail ana@exemplo.com"));

        mockMvc.perform(post("/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"nome":"Outro Cliente","email":"ana@exemplo.com","telefone":"47977777777"}
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void idInexistenteRetorna404() throws Exception {
        when(service.buscar(99999L))
            .thenThrow(new RecursoNaoEncontradoException("Cliente 99999 não encontrado"));

        mockMvc.perform(get("/clientes/99999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404));
    }
}
