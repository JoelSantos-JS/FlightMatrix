package com.joel.br.FlightMatrix.controller;

import com.joel.br.FlightMatrix.DTO.AeroportoDTO;
import com.joel.br.FlightMatrix.models.Aeroporto;
import com.joel.br.FlightMatrix.repository.AeroportoRepository;
import com.joel.br.FlightMatrix.services.AeroportoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.plaf.PanelUI;
import java.util.List;

@RestController
@RequestMapping("/api/aeroportos")
@RequiredArgsConstructor
public class AeroportoController {


    private final AeroportoService aeroportoService;



    @GetMapping
    public ResponseEntity<List<AeroportoDTO>> listarTodos( ){
        return ResponseEntity.ok(aeroportoService.listarTodos());
    }


    @GetMapping("/{codigo}")
    public ResponseEntity<AeroportoDTO> buscarPorCodigo(String codigo) {

        return ResponseEntity.ok(aeroportoService.buscarPorCodigo(codigo));
    }

    @GetMapping("/cidade/{cidade}")
    public ResponseEntity<List<AeroportoDTO>> buscarPorCidade(@PathVariable  String cidade){

        return ResponseEntity.ok(aeroportoService.buscarPorCidade(cidade));
    }


    @GetMapping("/pais/{pais}")
    public ResponseEntity<List<AeroportoDTO>> buscarPorAeroporto(@PathVariable String pais){

        return ResponseEntity.ok(aeroportoService.buscarPorPais(pais));
    }


    @PostMapping
    public ResponseEntity<AeroportoDTO> criar(@Valid @RequestBody AeroportoDTO aeroporto) {
            AeroportoDTO aeroportoDTO = aeroportoService.criar(aeroporto);

            return ResponseEntity.status(HttpStatus.CREATED).body(aeroportoDTO);
    }


    @PutMapping
    public ResponseEntity<AeroportoDTO> atualizar(@PathVariable String codigo , @Valid @RequestBody AeroportoDTO aeroportoDTO) {
            AeroportoDTO aeroportoDTO1 = aeroportoService.atualizar(codigo, aeroportoDTO);
        return ResponseEntity.ok(aeroportoDTO1);
    }


    @DeleteMapping

    public ResponseEntity<Void> remover(@PathVariable String codigo) {

        aeroportoService.remover(codigo);
        return ResponseEntity.noContent().build();
    }
}
