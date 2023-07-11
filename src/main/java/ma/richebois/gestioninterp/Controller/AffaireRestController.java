package ma.richebois.gestioninterp.Controller;

import ma.richebois.gestioninterp.Model.Affaire;
import ma.richebois.gestioninterp.Model.Login;
import ma.richebois.gestioninterp.Repository.AffaireRepository;
import ma.richebois.gestioninterp.Service.AffaireService;
import ma.richebois.gestioninterp.Service.UserImpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class AffaireRestController {

    @Autowired
    private AffaireRepository affaireRepository;

    @Autowired
    private AffaireService affaireService;

    @Autowired
    private UserImpService userImpService;

    @PostMapping("/editable/{id}")
    public Affaire editertable(@PathVariable("id") Long id, @RequestBody Affaire affaire){

        Optional<Affaire> aff = affaireRepository.findById(id);

        System.out.println(affaire.getNewDesign());

        aff.get().setNewDesign(affaire.getNewDesign().toUpperCase());
        return affaireRepository.save(aff.get());
    }

    @GetMapping("/Affaire/All")
    public List<Affaire> getAllProject(){
        Login login = userImpService.findbyusername();
        return affaireService.listChantierByRole(login.getRoles());
    }

}
