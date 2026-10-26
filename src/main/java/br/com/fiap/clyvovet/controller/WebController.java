package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.PetDashboardDTO;
import br.com.fiap.clyvovet.dto.PetRequestDTO;
import br.com.fiap.clyvovet.dto.PetResponseDTO;
import br.com.fiap.clyvovet.dto.ConsultaResponseDTO;
import br.com.fiap.clyvovet.dto.VacinaRequestDTO;
import br.com.fiap.clyvovet.exception.RecursoNaoEncontradoException;
import br.com.fiap.clyvovet.exception.RegraNegocioException;
import br.com.fiap.clyvovet.repository.DonoRepository;
import br.com.fiap.clyvovet.service.BaixaVacinaResultado;
import br.com.fiap.clyvovet.service.BaixaVacinaService;
import br.com.fiap.clyvovet.service.IntelligenceService;
import br.com.fiap.clyvovet.service.PainelService;
import br.com.fiap.clyvovet.service.PetService;
import br.com.fiap.clyvovet.service.VacinaService;
import br.com.fiap.clyvovet.service.ConsultaService;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Controller
public class WebController {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PainelService painelService;
    private final PetService petService;
    private final DonoRepository donoRepository;
    private final IntelligenceService intelligenceService;
    private final BaixaVacinaService baixaVacinaService;
    private final VacinaService vacinaService;
    private final ConsultaService consultaService;

    public WebController(PainelService painelService,
                         PetService petService,
                         DonoRepository donoRepository,
                         IntelligenceService intelligenceService,
                         BaixaVacinaService baixaVacinaService,
                         VacinaService vacinaService,
                         ConsultaService consultaService) {
        this.painelService = painelService;
        this.petService = petService;
        this.donoRepository = donoRepository;
        this.intelligenceService = intelligenceService;
        this.baixaVacinaService = baixaVacinaService;
        this.vacinaService = vacinaService;
        this.consultaService = consultaService;
    }

    @ModelAttribute("hoje")
    public LocalDate hoje() {
        return LocalDate.now();
    }

    @GetMapping("/")
    public String raiz() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // O Spring Security encaminha para cá preservando o método original,
    // então a página precisa responder também ao POST barrado.
    @RequestMapping("/acesso-negado")
    public String acessoNegado() {
        return "acesso-negado";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        List<PetDashboardDTO> pets = painelService.listarPets();

        model.addAttribute("pets", pets);
        model.addAttribute("totalPets", pets.size());
        model.addAttribute("totalPendentes", painelService.contarVacinasPendentes(pets));
        model.addAttribute("totalAtrasadas", pets.stream().flatMap(p -> p.getVacinas().stream())
                .filter(v -> "P".equals(v.getStatus()) && v.getData().isBefore(LocalDate.now())).count());
        model.addAttribute("usuario", nomeDoUsuario(authentication));

        return "dashboard";
    }

    @GetMapping("/pets")
    public String listarPets(@RequestParam(defaultValue = "") String q,
                             Model model, Authentication authentication) {
        String busca = normalizar(q.strip());
        List<PetDashboardDTO> pets = painelService.listarPets();
        model.addAttribute("totalPets", pets.size());
        model.addAttribute("pets", pets.stream().filter(p -> normalizar(
                p.getNome() + " " + p.getEspecie() + " " + p.getRaca() + " " + p.getTutorNome())
                .contains(busca)).toList());
        model.addAttribute("busca", q.strip());
        model.addAttribute("usuario", nomeDoUsuario(authentication));
        return "pets";
    }

    @GetMapping("/pets/novo")
    public String formularioDePet(Model model, Authentication authentication) {
        model.addAttribute("pet", new PetRequestDTO());
        prepararFormularioDePet(model, authentication, null);
        return "pet-form";
    }

    @GetMapping("/pets/{id}/editar")
    public String editarPet(@PathVariable Long id, Model model, Authentication authentication) {
        PetResponseDTO atual = petService.buscarPorId(id);
        PetRequestDTO pet = new PetRequestDTO();
        pet.setNome(atual.getNome());
        pet.setEspecie(atual.getEspecie());
        pet.setRaca(atual.getRaca());
        pet.setDataNascimento(atual.getDataNascimento());
        pet.setPeso(atual.getPeso());
        pet.setTutorId(atual.getTutorId());
        model.addAttribute("pet", pet);
        prepararFormularioDePet(model, authentication, id);
        return "pet-form";
    }

    @PostMapping("/pets/salvar")
    public String salvarPet(@Valid @ModelAttribute("pet") PetRequestDTO pet,
                            BindingResult resultado,
                            Model model,
                            Authentication authentication,
                            RedirectAttributes redirect) {

        if (resultado.hasErrors()) {
            prepararFormularioDePet(model, authentication, null);
            return "pet-form";
        }

        try {
            PetResponseDTO criado = petService.criar(pet);
            redirect.addFlashAttribute("sucesso", "Pet '" + pet.getNome() + "' cadastrado. Agende a primeira vacina para iniciar o acompanhamento.");
            return "redirect:/pets/" + criado.getId();
        } catch (RecursoNaoEncontradoException e) {
            resultado.rejectValue("tutorId", "tutor.inexistente", "Este tutor não está mais disponível. Selecione outro.");
            prepararFormularioDePet(model, authentication, null);
            return "pet-form";
        }
    }

    @PostMapping("/pets/{id}/atualizar")
    public String atualizarPet(@PathVariable Long id, @Valid @ModelAttribute("pet") PetRequestDTO pet,
                               BindingResult resultado, Model model, Authentication authentication,
                               RedirectAttributes redirect) {
        petService.buscarPorId(id);
        if (resultado.hasErrors()) {
            prepararFormularioDePet(model, authentication, id);
            return "pet-form";
        }
        try {
            petService.atualizar(id, pet);
        } catch (RecursoNaoEncontradoException e) {
            resultado.rejectValue("tutorId", "tutor.inexistente", "Este tutor não está mais disponível. Selecione outro.");
            prepararFormularioDePet(model, authentication, id);
            return "pet-form";
        }
        redirect.addFlashAttribute("sucesso", "Dados de '" + pet.getNome() + "' atualizados.");
        return "redirect:/pets/" + id;
    }

    @GetMapping("/pets/{id}")
    public String detalharPet(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("pet", painelService.buscarPet(id));
        model.addAttribute("analise", intelligenceService.calcularPorPetId(id));
        model.addAttribute("consultas", consultaService.buscarPorPet(id).stream()
                .sorted(Comparator.comparing(ConsultaResponseDTO::getData).reversed()).toList());
        model.addAttribute("usuario", nomeDoUsuario(authentication));
        return "pet-detalhe";
    }

    @GetMapping("/pets/{id}/vacinas/novo")
    public String formularioDeVacina(@PathVariable Long id, Model model, Authentication authentication) {
        VacinaRequestDTO vacina = new VacinaRequestDTO();
        vacina.setPetId(id);
        vacina.setStatus("P");
        vacina.setDataAplicacao(LocalDate.now());
        model.addAttribute("vacina", vacina);
        prepararFormularioDeVacina(id, model, authentication);
        return "vacina-form";
    }

    @PostMapping("/pets/{id}/vacinas/salvar")
    public String salvarVacina(@PathVariable Long id, @Valid @ModelAttribute("vacina") VacinaRequestDTO vacina,
                              BindingResult resultado, Model model, Authentication authentication,
                              RedirectAttributes redirect) {
        prepararFormularioDeVacina(id, model, authentication);
        // O agendamento pertence ao pet da URL e só a baixa registra a aplicação.
        vacina.setPetId(id);
        vacina.setStatus("P");
        if (resultado.hasErrors()) {
            return "vacina-form";
        }
        try {
            vacinaService.criar(vacina);
        } catch (RegraNegocioException e) {
            resultado.reject("vacina.invalida", e.getMessage());
            return "vacina-form";
        }
        redirect.addFlashAttribute("sucesso", "Vacina '" + vacina.getNome() + "' agendada para "
                + vacina.getDataAplicacao().format(DATA) + ".");
        return "redirect:/pets/" + id;
    }

    @PostMapping("/vacinas/{id}/aplicar")
    @CacheEvict(value = "inteligenciaPreventiva", allEntries = true)
    public String aplicarVacina(@PathVariable Long id,
                                RedirectAttributes redirect) {
        Long petId = vacinaService.buscarPorId(id).getPetId();
        try {
            BaixaVacinaResultado resultado = baixaVacinaService.aplicar(id, null);
            redirect.addFlashAttribute("sucesso", mensagemDaBaixa(resultado));
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/pets/" + petId;
    }

    private String mensagemDaBaixa(BaixaVacinaResultado resultado) {
        String base = "Vacina '" + resultado.aplicada().getNome() + "' aplicada.";
        return base + switch (resultado.reforco()) {
            case AGENDADO_AGORA -> " Próxima dose agendada automaticamente para "
                    + resultado.proximaDose().getData().format(DATA) + ".";
            case JA_AGENDADO -> " A próxima dose já estava agendada.";
            case SEM_INTERVALO -> " Esta vacina não tem intervalo de reforço cadastrado.";
        };
    }

    private String nomeDoUsuario(Authentication authentication) {
        return authentication != null ? authentication.getName() : "";
    }

    private void prepararFormularioDePet(Model model, Authentication authentication, Long id) {
        model.addAttribute("tutores", donoRepository.findAll());
        model.addAttribute("usuario", nomeDoUsuario(authentication));
        model.addAttribute("petId", id);
    }

    private void prepararFormularioDeVacina(Long id, Model model, Authentication authentication) {
        model.addAttribute("pet", painelService.buscarPet(id));
        model.addAttribute("usuario", nomeDoUsuario(authentication));
    }

    private String normalizar(String valor) {
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
