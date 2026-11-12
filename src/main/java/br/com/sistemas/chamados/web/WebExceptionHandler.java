package br.com.sistemas.chamados.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import br.com.sistemas.chamados.exception.RecursoNaoEncontradoException;
import br.com.sistemas.chamados.exception.RegraNegocioException;
import jakarta.servlet.http.HttpServletRequest;

/** Tratamento de erros das telas. */
@ControllerAdvice(basePackages = "br.com.sistemas.chamados.web")
public class WebExceptionHandler {

    /** Registro inexistente: página 404. */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ModelAndView naoEncontrado(RecursoNaoEncontradoException ex) {
        ModelAndView mav = new ModelAndView("error/404", HttpStatus.NOT_FOUND);
        mav.addObject("mensagem", ex.getMessage());
        return mav;
    }

    /** Regra de negócio violada (ex.: excluir cliente com chamados): volta à página anterior com o motivo. */
    @ExceptionHandler(RegraNegocioException.class)
    public String regraNegocio(RegraNegocioException ex, HttpServletRequest request) {
        RequestContextUtils.getOutputFlashMap(request).put("erro", ex.getMessage());
        String anterior = request.getHeader("Referer");
        return "redirect:" + (anterior != null ? anterior : "/");
    }
}
