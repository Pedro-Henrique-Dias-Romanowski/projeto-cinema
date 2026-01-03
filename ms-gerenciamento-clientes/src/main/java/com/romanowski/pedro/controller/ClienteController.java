package com.romanowski.pedro.controller;

import com.romanowski.pedro.controller.swagger.ClienteControllerSwagger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
public class ClienteController implements ClienteControllerSwagger {
}
