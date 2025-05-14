package com.joel.br.FlightMatrix.controller;

import com.joel.br.FlightMatrix.DTO.AeroportoDTO;
import com.joel.br.FlightMatrix.repository.AeroportoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/aeroportos")
@RequiredArgsConstructor
public class AeroportoController {


    private final AeroportoRepository aeroportoRepository;



    @GetMapping
    public List<AeroportoDTO> listarTodos( ){
        return aeroportoRepository.findAll().stream()
                .map(this::)
    }
}
