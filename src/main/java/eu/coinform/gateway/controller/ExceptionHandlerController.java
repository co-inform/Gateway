package eu.coinform.gateway.controller;

import eu.coinform.gateway.model.NoSuchTransactionIdException;
import eu.coinform.gateway.model.NoSuchQueryIdException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionHandlerController {

    @ResponseBody
    @ExceptionHandler(NoSuchQueryIdException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String noSuchQueryIdException(NoSuchQueryIdException e) {
        return e.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(NoSuchTransactionIdException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String NoSuchTransactionIdHandler(NoSuchTransactionIdException ex) {
        return ex.getMessage();
    }
}
