package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.ConsultaRequestDTO;
import br.com.fiap.clyvovet.dto.ConsultaResponseDTO;
import br.com.fiap.clyvovet.exception.ResourceNotFoundException;
import br.com.fiap.clyvovet.model.Consulta;
import br.com.fiap.clyvovet.model.Pet;
import br.com.fiap.clyvovet.repository.ConsultaRepository;
import br.com.fiap.clyvovet.repository.PetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConsultaService {

    private final ConsultaRepository repository;
    private final PetRepository petRepository;

    public ConsultaService(ConsultaRepository repository, PetRepository petRepository) {
        this.repository = repository;
        this.petRepository = petRepository;
    }

    @Transactional(readOnly = true)
    public Page<ConsultaResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public ConsultaResponseDTO buscarPorId(Long id) {
        Consulta consulta = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta não encontrada com o ID: " + id));
        return mapToResponseDTO(consulta);
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponseDTO> buscarPorVeterinario(String nome) {
        return repository.findByVeterinarioContainingIgnoreCase(nome).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponseDTO> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return repository.findByDataBetween(inicio, fim).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultaResponseDTO> buscarPorPet(Long petId) {
        if (!petRepository.existsById(petId)) {
            throw new ResourceNotFoundException("Pet não encontrado com o ID: " + petId);
        }
        return repository.findByPetId(petId).stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional
    public ConsultaResponseDTO criar(ConsultaRequestDTO dto) {
        Consulta consulta = mapToEntity(dto);
        Consulta salva = repository.save(consulta);
        return mapToResponseDTO(salva);
    }

    @Transactional
    public ConsultaResponseDTO atualizar(Long id, ConsultaRequestDTO dto) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Consulta não encontrada com o ID: " + id);
        }
        Consulta consulta = mapToEntity(dto);
        consulta.setId(id);
        Consulta salva = repository.save(consulta);
        return mapToResponseDTO(salva);
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Consulta não encontrada com o ID: " + id);
        }
        repository.deleteById(id);
    }

    public ConsultaResponseDTO mapToResponseDTO(Consulta consulta) {
        if (consulta == null) return null;
        return new ConsultaResponseDTO(
                consulta.getId(),
                consulta.getData(),
                consulta.getDescricao(),
                consulta.getVeterinario(),
                consulta.getObservacoes(),
                consulta.getPet() != null ? consulta.getPet().getId() : null
        );
    }

    public Consulta mapToEntity(ConsultaRequestDTO dto) {
        if (dto == null) return null;
        Consulta consulta = new Consulta();
        consulta.setData(dto.getData());
        consulta.setDescricao(dto.getDescricao());
        consulta.setVeterinario(dto.getVeterinario());
        consulta.setObservacoes(dto.getObservacoes());

        if (dto.getPetId() != null) {
            Pet pet = petRepository.findById(dto.getPetId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pet não encontrado com o ID: " + dto.getPetId()));
            consulta.setPet(pet);
        }
        return consulta;
    }
}
