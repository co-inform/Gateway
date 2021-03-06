package eu.coinform.gateway.controller;

import eu.coinform.gateway.controller.exceptions.MissingRenewToken;
import eu.coinform.gateway.controller.exceptions.NoSuchRenewToken;
import eu.coinform.gateway.db.LinkTimedOutException;
import eu.coinform.gateway.db.NoSuchTokenException;
import eu.coinform.gateway.db.UserNotVerifiedException;
import eu.coinform.gateway.db.UsernameAlreadyExistException;
import eu.coinform.gateway.jwt.JwtAuthenticationException;
import eu.coinform.gateway.jwt.UserLoggedOutException;
import eu.coinform.gateway.model.NoSuchQueryIdException;
import eu.coinform.gateway.model.NoSuchTransactionIdException;
import eu.coinform.gateway.util.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
        return ErrorResponse.NOSUCHUSER;
    }

    @ResponseBody
    @ExceptionHandler(NoSuchTokenException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView noSuchTokenException(NoSuchTokenException ex) {
        ModelAndView mav = new ModelAndView("alreadyverified");
        mav.addObject("token", ex.getMessage());
        return mav;
    }

    @ResponseBody
    @ExceptionHandler(LinkTimedOutException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView linkTimedOutException(LinkTimedOutException ex) {
        return new ModelAndView("timedout");
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

    @ResponseBody
    @ExceptionHandler(UserLoggedOutException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse userLoggedOutException(UserLoggedOutException ex){
        return ErrorResponse.USERLOGGEDOUT;
    }

    @ResponseBody
    @ExceptionHandler(MissingRenewToken.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse missingRenewToken(MissingRenewToken ex) {
        return ErrorResponse.MISSINGRENEWTOKEN;
    }

    @ResponseBody
    @ExceptionHandler(NoSuchRenewToken.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse noSuchRenewToken(NoSuchRenewToken ex) {
        return ErrorResponse.NOSUCHRENEWTOKEN;
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse missingBodyParameter(MethodArgumentNotValidException ex) {
        return ErrorResponse.MISSINGARGUMENT;
    }
}
