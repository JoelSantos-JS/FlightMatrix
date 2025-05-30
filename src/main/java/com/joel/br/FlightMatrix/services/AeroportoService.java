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

    @Transactional
    public AeroportoDTO criar(AeroportoDTO aeroportoDTO) {
        // Adicionar validação se o código já existe, se necessário
        if (aeroportoRepository.existsById(aeroportoDTO.getCodigo())) {
            // Lançar uma exceção apropriada, ex: AirportAlreadyExistsException
            throw new IllegalArgumentException("Aeroporto com código " + aeroportoDTO.getCodigo() + " já existe.");
        }
        Aeroporto aeroporto = converterParaModel(aeroportoDTO);
        Aeroporto aeroportoSalvo = aeroportoRepository.save(aeroporto);
        return converterParaDto(aeroportoSalvo);
    }

    @Transactional
    public AeroportoDTO atualizar(String codigo, AeroportoDTO aeroportoDTO) {
        Aeroporto aeroportoExistente = aeroportoRepository.findById(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Aeroporto não encontrado com código: " + codigo));

        aeroportoExistente.setNome(aeroportoDTO.getNome());
        aeroportoExistente.setCidade(aeroportoDTO.getCidade());
        aeroportoExistente.setPais(aeroportoDTO.getPais());
        // Se o código puder ser alterado, adicione lógica para isso (geralmente não é uma boa ideia para PKs)

        Aeroporto aeroportoAtualizado = aeroportoRepository.save(aeroportoExistente);
        return converterParaDto(aeroportoAtualizado);
    }

    @Transactional
    public void remover(String codigo) {
        if (!aeroportoRepository.existsById(codigo)) {
            throw new ResourceNotFoundException("Aeroporto não encontrado com código: " + codigo);
        }
        aeroportoRepository.deleteById(codigo);
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
