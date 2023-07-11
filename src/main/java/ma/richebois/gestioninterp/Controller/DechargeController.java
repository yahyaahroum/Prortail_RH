package ma.richebois.gestioninterp.Controller;


import ma.richebois.gestioninterp.Model.Decharge;
import ma.richebois.gestioninterp.Model.Login;
import ma.richebois.gestioninterp.Model.Materiel;
import ma.richebois.gestioninterp.Repository.IndividuRepository;
import ma.richebois.gestioninterp.Repository.MaterielRepository;
import ma.richebois.gestioninterp.Service.AffaireService;
import ma.richebois.gestioninterp.Service.DechargeService;
import ma.richebois.gestioninterp.Service.UserImpService;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.math3.ml.neuralnet.twod.util.SmoothedDataHistogram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class DechargeController {

    @Autowired
    private IndividuRepository individuRepository;

    @Autowired
    private DechargeService dechargeService;

    @Autowired
    private AffaireService affaireService;

    @Autowired
    private MaterielRepository materielRepository;

    @Autowired
    private UserImpService userImpService;

    @GetMapping("/Decharge/Liste")
    public String getPointagePage(Model model,@RequestParam("page") Optional<Integer> page,@RequestParam("statut") String statut,
                                  @RequestParam("size") Optional<Integer> size){

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(30);

        Page<Decharge> listDecharge = dechargeService.dechargeList(PageRequest.of(currentPage - 1, pageSize),statut);
        model.addAttribute("dechargeList",listDecharge);

        int totalPages = listDecharge.getTotalPages();
        if (totalPages>0){
            List<Integer> pageNumbers = IntStream.rangeClosed(1,totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        Login login = userImpService.findbyusername();
        model.addAttribute("statut",statut);
        model.addAttribute("chantier", affaireService.listChantierByRole(login.getRoles()));
        model.addAttribute("emps",individuRepository.getContratActif());
        model.addAttribute("dechargeList",listDecharge);
        model.addAttribute("materielList",materielRepository.findAll(Sort.by(Sort.Direction.ASC,"Type")));
       return "Decharge/ListDecharge";
    
    }

    @GetMapping("/Decharge/generer/{id}")
    public ResponseEntity<byte[]> genererDecharge(@PathVariable("id") long id) throws JRException, IOException {
        return dechargeService.generateDecharge(id);
    }

    @GetMapping("/Decharge/Annuler/{id}")
    public String Annuler(@PathVariable("id") long id){
        dechargeService.annuler(id);
        return "redirect:/Decharge/Liste?statut=All";
        
    }
    
    @GetMapping("/Decharge/ARecuperer/{id}")
    public String aRecuperer(@PathVariable("id") long id){
        dechargeService.aRecuperer(id);
        return "redirect:/Decharge/Liste?statut=All";
    
    }
    @PostMapping("/Decharge/Signer/{id}")
    public String signer(@PathVariable("id") long id,@RequestParam("file") MultipartFile file) throws IOException {
        dechargeService.signer(id,file);
        return "redirect:/Decharge/Liste?statut=All";
         
    }

    @GetMapping("/Decharge/expedier/{id}")
    public String expedier(@PathVariable("id") long id) {
        dechargeService.expedier(id);
        return "redirect:/Decharge/Liste?statut=All";
      
    }

    @GetMapping("/Decharge/liberer/{id}")
    public String liberer(@PathVariable("id") long id){
        dechargeService.liberer(id);
       return "redirect:/Decharge/Liste?statut=All";
      
    }
}
