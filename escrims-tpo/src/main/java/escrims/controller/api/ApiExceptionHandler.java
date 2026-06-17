package escrims.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiDtos.ErrorResponse handleIllegalArgument(IllegalArgumentException e) {
        return new ApiDtos.ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiDtos.ErrorResponse handleUnauthorized(UnauthorizedException e) {
        return new ApiDtos.ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiDtos.ErrorResponse handleForbidden(ForbiddenException e) {
        return new ApiDtos.ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(TooManyRequestsException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ApiDtos.ErrorResponse handleTooManyRequests(TooManyRequestsException e) {
        return new ApiDtos.ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiDtos.ErrorResponse handleIllegalState(IllegalStateException e) {
        return new ApiDtos.ErrorResponse(e.getMessage());
    }
}
