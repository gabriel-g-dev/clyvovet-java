package br.com.fiap.clyvovet.service;

import br.com.fiap.clyvovet.dto.TutorRequestDTO;
import br.com.fiap.clyvovet.dto.TutorResponseDTO;
import br.com.fiap.clyvovet.dto.PetResponseDTO;
import br.com.fiap.clyvovet.exception.ResourceNotFoundException;
import br.com.fiap.clyvovet.model.Tutor;
import br.com.fiap.clyvovet.repository.TutorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TutorService {

    private final TutorRepository repository;

    public TutorService(TutorRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<TutorResponseDTO> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public TutorResponseDTO buscarPorId(Long id) {
        Tutor tutor = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado com o ID: " + id));
        return mapToResponseDTO(tutor);
    }

    @Transactional(readOnly = true)
    public TutorResponseDTO buscarPorEmail(String email) {
        Tutor tutor = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado com o e-mail: " + email));
        return mapToResponseDTO(tutor);
    }

    @Transactional(readOnly = true)
    public TutorResponseDTO buscarComPets(Long id) {
        Tutor tutor = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor não encontrado com o ID: " + id));
        return mapToResponseDTO(tutor);
    }

    @Transactional
    public TutorResponseDTO criar(TutorRequestDTO dto) {
        Tutor tutor = mapToEntity(dto);
        Tutor salvo = repository.save(tutor);
        return mapToResponseDTO(salvo);
    }

    @Transactional
    public TutorResponseDTO atualizar(Long id, TutorRequestDTO dto) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Tutor não encontrado com o ID: " + id);
        }
        Tutor tutor = mapToEntity(dto);
        tutor.setId(id);
        Tutor salvo = repository.save(tutor);
        return mapToResponseDTO(salvo);
    }

    @Transactional
    public void deletar(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Tutor não encontrado com o ID: " + id);
        }
        repository.deleteById(id);
    }

    public TutorResponseDTO mapToResponseDTO(Tutor tutor) {
        if (tutor == null) return null;
        List<PetResponseDTO> petDTOs = null;
        if (tutor.getPets() != null) {
            petDTOs = tutor.getPets().stream()
                    .map(pet -> new PetResponseDTO(
                            pet.getId(),
                            pet.getNome(),
                            pet.getEspecie(),
                            pet.getRaca(),
                            pet.getDataNascimento(),
                            tutor.getId()
                    ))
                    .toList();
        }
        return new TutorResponseDTO(
                tutor.getId(),
                tutor.getNome(),
                tutor.getEmail(),
                tutor.getTelefone(),
                petDTOs
        );
    }

    public Tutor mapToEntity(TutorRequestDTO dto) {
        if (dto == null) return null;
        Tutor tutor = new Tutor();
        tutor.setNome(dto.getNome());
        tutor.setEmail(dto.getEmail());
        tutor.setTelefone(dto.getTelefone());
        return tutor;
    }
}
