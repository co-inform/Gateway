package eu.coinform.gateway.controller;

import eu.coinform.gateway.db.*;
import eu.coinform.gateway.jwt.JwtAuthenticationException;
import eu.coinform.gateway.model.NoSuchTransactionIdException;
import eu.coinform.gateway.model.NoSuchQueryIdException;
import eu.coinform.gateway.util.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

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

    @ResponseBody
    @ExceptionHandler(UserNotVerifiedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse userNotVerifiedException(UserNotVerifiedException ex){
        return ErrorResponse.USERNOTVERIFIED;
    }

    @ResponseBody
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse userNameNotFoundException(UsernameNotFoundException ex) {
        return ErrorResponse.NOUSER;
    }

    @ResponseBody
    @ExceptionHandler(NoSuchTokenException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView noSuchTokenException(NoSuchTokenException ex) {
        ModelAndView mav = new ModelAndView("notverified");
        mav.addObject("token", ex.getMessage());
        return mav;
    }

    @ResponseBody
    @ExceptionHandler(LinkTimedOutException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView linkTimedOutException(LinkTimedOutException ex) {
        ModelAndView mav = new ModelAndView("timedout");
        mav.addObject("token", ex.getMessage());
        return mav;
    }

    @ResponseBody
    @ExceptionHandler(JwtAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse jwtAuthenticationException(JwtAuthenticationException ex){
        return ErrorResponse.JWTEXCEPTION;
    }

    @ResponseBody
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse badCredentialsException(BadCredentialsException ex){
        return ErrorResponse.BADCREDENTIALS;
    }
}
