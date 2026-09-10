package br.com.fiap.clyvovet.controller;

import br.com.fiap.clyvovet.dto.PetDashboardDTO;
import br.com.fiap.clyvovet.dto.PetRequestDTO;
import br.com.fiap.clyvovet.exception.RegraNegocioException;
import br.com.fiap.clyvovet.repository.DonoRepository;
import br.com.fiap.clyvovet.service.BaixaVacinaResultado;
import br.com.fiap.clyvovet.service.BaixaVacinaService;
import br.com.fiap.clyvovet.service.IntelligenceService;
import br.com.fiap.clyvovet.service.PainelService;
import br.com.fiap.clyvovet.service.PetService;
import jakarta.validation.Valid;
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
import java.util.List;

@Controller
public class WebController {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PainelService painelService;
    private final PetService petService;
    private final DonoRepository donoRepository;
    private final IntelligenceService intelligenceService;
    private final BaixaVacinaService baixaVacinaService;

    public WebController(PainelService painelService,
                         PetService petService,
                         DonoRepository donoRepository,
                         IntelligenceService intelligenceService,
                         BaixaVacinaService baixaVacinaService) {
        this.painelService = painelService;
        this.petService = petService;
        this.donoRepository = donoRepository;
        this.intelligenceService = intelligenceService;
        this.baixaVacinaService = baixaVacinaService;
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
        model.addAttribute("usuario", nomeDoUsuario(authentication));

        return "dashboard";
    }

    @GetMapping("/pets")
    public String listarPets(Model model, Authentication authentication) {
        model.addAttribute("pets", painelService.listarPets());
        model.addAttribute("usuario", nomeDoUsuario(authentication));
        return "pets";
    }

    @GetMapping("/pets/novo")
    public String formularioDePet(Model model, Authentication authentication) {
        model.addAttribute("pet", new PetRequestDTO());
        model.addAttribute("tutores", donoRepository.findAll());
        model.addAttribute("usuario", nomeDoUsuario(authentication));
        return "pet-form";
    }

    @PostMapping("/pets/salvar")
    public String salvarPet(@Valid @ModelAttribute("pet") PetRequestDTO pet,
                            BindingResult resultado,
                            Model model,
                            Authentication authentication,
                            RedirectAttributes redirect) {

        if (resultado.hasErrors()) {
            model.addAttribute("tutores", donoRepository.findAll());
            model.addAttribute("usuario", nomeDoUsuario(authentication));
            return "pet-form";
        }

        petService.criar(pet);
        redirect.addFlashAttribute("sucesso", "Pet '" + pet.getNome() + "' cadastrado com sucesso.");
        return "redirect:/pets";
    }

    @GetMapping("/pets/{id}")
    public String detalharPet(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("pet", painelService.buscarPet(id));
        model.addAttribute("analise", intelligenceService.calcularPorPetId(id));
        model.addAttribute("usuario", nomeDoUsuario(authentication));
        return "pet-detalhe";
    }

    @PostMapping("/vacinas/{id}/aplicar")
    public String aplicarVacina(@PathVariable Long id,
                                @RequestParam Long petId,
                                RedirectAttributes redirect) {
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
        if (!resultado.temProximaDose()) {
            return base + " Esta vacina não tem intervalo de reforço cadastrado.";
        }
        return base + " Próxima dose agendada automaticamente para "
                + resultado.proximaDose().getData().format(DATA) + ".";
    }

    private String nomeDoUsuario(Authentication authentication) {
        return authentication != null ? authentication.getName() : "";
    }
}
