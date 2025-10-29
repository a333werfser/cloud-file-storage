package edu.example.project.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.example.project.dto.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class UserNotFoundEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        ResponseMessage responseMessageDto = new ResponseMessage();
        if (authException instanceof BadCredentialsException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            responseMessageDto.setMessage("User not found");

            String body = new ObjectMapper().writeValueAsString(responseMessageDto);
            response.getWriter().write(body);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

}
