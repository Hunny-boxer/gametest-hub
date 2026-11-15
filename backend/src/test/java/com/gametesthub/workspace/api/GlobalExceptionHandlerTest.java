
package com.gametesthub.workspace.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import com.gametesthub.api.GlobalExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    private HttpServletRequest createRequest() {
        return new MockHttpServletRequest(
                "POST",
                "/api/v1/workspaces"
        );
    }

    @Test
    void shouldReturnValidationErrors() {

        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError(
                "request",
                "name",
                "Workspace name is required"
        );

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(fieldError));

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response =
                handler.handleValidationErrors(
                        exception,
                        createRequest()
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(
                "Request validation failed.",
                response.getBody().message()
        );

        assertEquals(
                "name",
                response.getBody().validationErrors().get(0).field()
        );

        assertEquals(
                "Workspace name is required",
                response.getBody().validationErrors().get(0).message()
        );
    }

    @Test
    void shouldReturnBadRequestForMalformedJson() {

        HttpMessageNotReadableException exception =
                mock(HttpMessageNotReadableException.class);

        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response =
                handler.handleInvalidRequestBody(
                        exception,
                        createRequest()
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(
                "Request body is missing or malformed.",
                response.getBody().message()
        );
    }

    @Test
    void shouldReturnForbiddenForAccessDenied() {

        AccessDeniedException exception =
                new AccessDeniedException("Private internal detail");

        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response =
                handler.handleAccessDenied(
                        exception,
                        createRequest()
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(
                "Access to this resource is denied.",
                response.getBody().message()
        );
    }

    @Test
    void shouldReturnConflictForDuplicateResource() {

        IllegalStateException exception =
                new IllegalStateException(
                        "A workspace with this slug already exists."
                );

        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response =
                handler.handleConflict(
                        exception,
                        createRequest()
                );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(
                "A workspace with this slug already exists.",
                response.getBody().message()
        );
    }

    @Test
    void shouldReturnBadRequestForInvalidArguments() {

        IllegalArgumentException exception =
                new IllegalArgumentException(
                        "Invalid workspace slug."
                );

        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response =
                handler.handleBadRequest(
                        exception,
                        createRequest()
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        assertEquals(
                "Invalid workspace slug.",
                response.getBody().message()
        );
    }

    @Test
    void shouldHideInternalDetailsForUnexpectedErrors() {

        RuntimeException exception =
                new RuntimeException(
                        "Sensitive database connection details"
                );

        ResponseEntity<GlobalExceptionHandler.ApiErrorResponse> response =
                handler.handleUnexpectedError(
                        exception,
                        createRequest()
                );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().timestamp());

        assertEquals(
                "An unexpected server error occurred.",
                response.getBody().message()
        );

        assertEquals(
                "/api/v1/workspaces",
                response.getBody().path()
        );
    }
}
