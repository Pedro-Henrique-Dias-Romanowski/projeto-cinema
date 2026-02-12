package com.romanowski.pedro.controller.swagger;

import com.romanowski.pedro.dto.request.LoginRequestDTO;
import com.romanowski.pedro.dto.response.LoginResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface LoginControllerSwagger {

    ResponseEntity<LoginResponseDTO> efetuarLogin(@RequestBody LoginRequestDTO loginRequestDTO) throws Exception;
}
