package ma.richebois.gestioninterp.Controller;

import ma.richebois.gestioninterp.Enum.DemandeState;
import ma.richebois.gestioninterp.Model.*;
import ma.richebois.gestioninterp.Repository.AjoutRepository;
import ma.richebois.gestioninterp.Repository.IndividuRepository;
import ma.richebois.gestioninterp.Repository.RoleRepository;
import ma.richebois.gestioninterp.Service.DemandeService;
import ma.richebois.gestioninterp.Service.UserImpService;
import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class DemandeController {

    @Autowired
    private DemandeService demandeService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserImpService userImpService;

    @Autowired
    private AjoutRepository ajoutRepository;

    @Autowired
    private IndividuRepository individuRepository;


    @GetMapping("/Demande/Toutes")
    public String getAllDem(Model model,@RequestParam("page") Optional<Integer> page,
                            @RequestParam("size") Optional<Integer> size,@RequestParam("etat") String state){
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(30);
        Login login = userImpService.findbyusername();

        Page<Demande> listDemande =demandeService.findByEmpAndState(PageRequest.of(currentPage - 1, pageSize),state);
        Role employe = roleRepository.findByType("Employé");
        boolean numtel = false;
        Individu individu = individuRepository.findByIndividu(login.getMatricule());
        List<Ajout> ajoutList = ajoutRepository.findAllByMatriculeOrderByIdDesc(login.getMatricule());
        if (individu.getTele()==null){
            if (!ajoutList.isEmpty()){
                if (ajoutList.get(0).getNumtele().length()<10){

                    model.addAttribute("numTelBoolean", numtel);
                }
                if (ajoutList.get(0).getNumtele().length()>=10){
                    numtel = true;
                    model.addAttribute("numTelBoolean", numtel);
                }
            }
        }
        if (individu.getTele()!=null){
            numtel = true;
            model.addAttribute("numTelBoolean", numtel);
        }

        model.addAttribute("numTelBoolean", numtel);
        model.addAttribute("listDemande",listDemande);
        model.addAttribute("state",state);
        model.addAttribute("role",employe);
        model.addAttribute("login",login);
        if (individu.getService()!=null){
            model.addAttribute("indByServ",individuRepository.findAllByServiceOrderByNom(individu.getService()));
        }

        int totalPages = listDemande.getTotalPages();
        if (totalPages>0){
            List<Integer> pageNumbers = IntStream.rangeClosed(1,totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        model.addAttribute("typeDemandes",demandeService.getAllType());
        return "Demande/ListDemande";
    }

    @PostMapping("/Demande/Ajouter")
    public String saveDemande(Demande demande,String numTele){
            Demande demande1 = demandeService.saveDemande(demande,numTele);
           demandeService.sendEmailDemande(demande1);
        return "redirect:/Demande/Toutes?etat=All";
    }

    @GetMapping("/Demande/Supprimer/{id}")
    public String deleteDemande(@PathVariable("id") Long id){
        demandeService.deleteDemande(id);
        return "redirect:/Demande/Toutes?etat=All";
    }

    @PostMapping("/Demande/Valider/{id}")
    public String validDemande(@PathVariable("id") Long id){
        demandeService.valider(id);
        return "redirect:/Demande/Toutes?etat=All";
    }

    @PostMapping("/Demande/Refuser/{id}")
    public String RefusDemande(@PathVariable("id") Long id, String motifRefus){
        demandeService.refuser(id,motifRefus);
        return "redirect:/Demande/Toutes?etat=All";
    }

    @GetMapping("/Demande/Generer/{id}")
    public ResponseEntity<byte[]> generateDemande(@PathVariable("id") Long id) throws JRException, IOException {
        return demandeService.generateDemande(id);
    }

}
