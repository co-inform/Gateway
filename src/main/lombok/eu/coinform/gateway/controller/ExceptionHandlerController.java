package eu.coinform.gateway.controller;

import eu.coinform.gateway.db.UsernameAlreadyExistException;
import eu.coinform.gateway.model.NoSuchTransactionIdException;
import eu.coinform.gateway.model.NoSuchQueryIdException;
import eu.coinform.gateway.util.ErrorResponse;
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
    public ErrorResponse noSuchQueryIdException(NoSuchQueryIdException e) {
        return ErrorResponse.NOSUCHQUERYID;
    }

    @ResponseBody
    @ExceptionHandler(NoSuchTransactionIdException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse noSuchTransactionIdHandler(NoSuchTransactionIdException ex) {
        return ErrorResponse.NOSUCHTRANSACTIONID;
    }

    @ResponseBody
    @ExceptionHandler(UsernameAlreadyExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse usernameAlreadyExistException(UsernameAlreadyExistException ex) {
        return ErrorResponse.USEREXISTS;
    }
}
