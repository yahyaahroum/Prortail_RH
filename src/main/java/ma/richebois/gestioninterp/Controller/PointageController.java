package ma.richebois.gestioninterp.Controller;

import ma.richebois.gestioninterp.Model.Login;
import ma.richebois.gestioninterp.Repository.IndividuRepository;
import ma.richebois.gestioninterp.Service.AffaireService;
import ma.richebois.gestioninterp.Service.IndividuImpService;
import ma.richebois.gestioninterp.Service.PointageService;
import ma.richebois.gestioninterp.Service.UserImpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PointageController {

    @Autowired
    private IndividuRepository individuRepository;

    @Autowired
    private UserImpService userImpService;

    @Autowired
    private AffaireService affaireService;

    @Autowired
    private PointageService pointageService;



    @GetMapping("/Pointage")
    public String getPointagePage(Model model){
        Login login = userImpService.findbyusername();
        model.addAttribute("chantier", affaireService.listChantierByRole(login.getRoles()));
        model.addAttribute("emps",individuRepository.getContratActif());
        return "ListPointage";
    }

    @GetMapping("/Pointage/FilterPage")
    public String getPointageFilterPage(Model model){
        Login login = userImpService.findbyusername();
        model.addAttribute("chantier", affaireService.listChantierByRole(login.getRoles()));
        model.addAttribute("emps",individuRepository.getContratActif());
        return "ListPointageFilter";
    }
}
