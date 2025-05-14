package com.joel.br.FlightMatrix.services;

import com.joel.br.FlightMatrix.DTO.AeroportoDTO;
import com.joel.br.FlightMatrix.exceptions.ResourceNotFoundException;
import com.joel.br.FlightMatrix.models.Aeroporto;
import com.joel.br.FlightMatrix.repository.AeroportoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AeroportoService {


    private AeroportoRepository aeroportoRepository;

    @Transactional(readOnly = true)
    public List<AeroportoDTO> listarTodos() {
        return aeroportoRepository.findAll().stream().map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AeroportoDTO buscarPorCodigo(String codigo) {

        Aeroporto aeroporto = aeroportoRepository.findById(codigo).orElseThrow(() -> new ResourceNotFoundException("Aeroporto não encotrado "));
        return converterParaDto(aeroporto);
    }
    @Transactional(readOnly = true)
    public List<AeroportoDTO> buscarPorCidade(String cidade) {

        return aeroportoRepository.findByCidadeContainingIgnoreCase(cidade).stream()
                .map(this::converterParaDto)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<AeroportoDTO> buscarPorPais(String pais ) {

        return  aeroportoRepository.findByPaisContainingIgnoreCase(pais)
                .stream().map(this::converterParaDto)
                .collect(Collectors.toList());
    }

    private AeroportoDTO converterParaDto(Aeroporto aeroporto) {
         AeroportoDTO aeroportoDTO = new AeroportoDTO();

         aeroportoDTO.setCidade(aeroporto.getCidade());
         aeroportoDTO.setNome(aeroporto.getNome());
         aeroportoDTO.setPais(aeroporto.getPais());
         aeroportoDTO.setCodigo(aeroporto.getCodigo());
        return aeroportoDTO;
    }

    private Aeroporto converterParaModel(AeroportoDTO aeroportoDTO){
        Aeroporto aeroporto = new Aeroporto();
        aeroporto.setCidade(aeroportoDTO.getCidade());
        aeroporto.setPais(aeroportoDTO.getPais());
        aeroporto.setNome(aeroportoDTO.getNome());
        aeroporto.setCodigo(aeroportoDTO.getCodigo());
        return aeroporto;
    }
}
