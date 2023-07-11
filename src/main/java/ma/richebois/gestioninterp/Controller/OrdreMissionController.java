package ma.richebois.gestioninterp.Controller;
import ma.richebois.gestioninterp.Model.Login;
import ma.richebois.gestioninterp.Model.OrdreMission;
import ma.richebois.gestioninterp.Model.Role;
import ma.richebois.gestioninterp.Repository.AffaireRepository;
import ma.richebois.gestioninterp.Repository.IndividuRepository;
import ma.richebois.gestioninterp.Repository.OrdreMissionRepository;
import ma.richebois.gestioninterp.Service.OrdreMissionService;
import ma.richebois.gestioninterp.Service.UserImpService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Controller
public class OrdreMissionController  {
   @Autowired
   private OrdreMissionService ordreMissionService;
   @Autowired 
   private OrdreMissionRepository ordreMissionRepository;
   @Autowired
   private AffaireRepository affaireRepository;
   @Autowired
   private IndividuRepository individuRepository;
    @Autowired
    private UserImpService loginImpService;

   
    public Login UserConnecte(){
        return loginImpService.findbyusername();
    }
    @GetMapping("/OrdreMission/Tous")
    public String getAllAOrdreMission(Model model, @RequestParam("page") Optional<Integer> page,
                                   @RequestParam("size") Optional<Integer> size){
      
   

        Login login = this.UserConnecte();

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(40);

        Page<OrdreMission> ordresmissions = ordreMissionService.findPaginatedOrdresMissions(PageRequest.of(currentPage - 1, pageSize),login);
        model.addAttribute("ordresmissions",ordresmissions);
        model.addAttribute("emps",individuRepository.getContratActif());
        model.addAttribute("affaire",affaireRepository.findAll());
        OrdreMission ordreM=new OrdreMission();
        model.addAttribute("ordreM",ordreM);


        int totalPages = ordresmissions.getTotalPages();
        if (totalPages >0){
            List<Integer> pageNumbers = IntStream.rangeClosed(1,totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "OrdreMission/ListeOrdresMission";

    }


    @PostMapping("/OrdreMission/new")

    public String saveOM(@ModelAttribute("sanction") OrdreMission ordreMission){
        Login login = this.UserConnecte();
        ordreMission.setLogin(login);
        if(!ordreMission.getMoyenTransport().equals("Vehicule personnel de Mr")){
            ordreMission.setIndividu(null);
        }

        ordreMissionService.saveOrdreMission(ordreMission);
        return "redirect:/OrdreMission/Tous";
    }
@GetMapping("/OrdreMission/Editer/{id}")

    public String getdetailOM(Model model, @PathVariable("id") Long id){
      
        model.addAttribute("emps",individuRepository.getContratActif());
        model.addAttribute("affaire",affaireRepository.findAll());
        model.addAttribute("OrdreMission",ordreMissionRepository.findById(id).get());

        return "OrdreMission/EditOrdreMission";
    }
    @PostMapping("/OrdreMission/Modifier/{id}")

    public String updateOM(OrdreMission ordreMission, @PathVariable("id") long id){
        Login login = this.UserConnecte();
        OrdreMission OM=ordreMissionService.getOrdreMission(id).get();       
        boolean imprime=OM.getImpr();
       
        ordreMission.setLogin(login);
      
        
      if(!imprime){
            ordreMission.setImprime(true);
            ordreMissionService.updateOrdreMission(ordreMission,id);
      }
         
     
 
        return "redirect:/OrdreMission/Tous";
    }

    @GetMapping("/OrdreMission/Supprimer/{id}")

    public String deleteSanction(@PathVariable("id") long id){

       OrdreMission OM=ordreMissionService.getOrdreMission(id).get();       
       boolean imprime=OM.getImpr();
       if(!imprime){
        
       ordreMissionService.deleteOrdreMission(id);
       }
        return "redirect:/OrdreMission/Tous";
    }

    @GetMapping(value = "/OrdreMission/pdf/{id}", produces = MediaType.APPLICATION_PDF_VALUE)
   // @PreAuthorize("hasAnyAuthority('admin','RH','Pointeur')")
    public ResponseEntity<byte[]> ordreMissionPDF(@PathVariable Long id) throws JRException, IOException {
        Login login = this.UserConnecte();
        OrdreMission OM = ordreMissionService.getOrdreMission(id).get();
        ordreMissionService.UpdateEtatImprime(OM,id);
        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(Collections.singleton(OM), false);

        Map<String, Object> parameters = new HashMap<>();

        parameters.put("fonction", OM.getLogin().getFonction().getLibelle());   
        parameters.put("nom", login.getNom().trim().toUpperCase());
        parameters.put("prenom", login.getPrenom().trim().toUpperCase());
        parameters.put("libelleaffaire", OM.getAffaire().getDesignation());
        parameters.put("codeaffaire", OM.getAffaire().getCode());

        //Code Affichage Moyen de dplacement
        String MT1,MT2,MT3,MT4,MT5;
        String moyenTransport = OM.getMoyenTransport();

        if (moyenTransport.equals("Vehicule de letablissement")) {
            MT1="X";
    }else {
            MT1="";
        }
        if (moyenTransport.equals("Vehicule personnel")) {
            MT2="X";
        }else {
            MT2="";
        }
        if (moyenTransport.equals("Vehicule personnel de Mr") ) {
            MT3=OM.getIndividu().getNom().trim()+" "+ OM.getIndividu().getPrenom().trim();
        }else {
            MT3="";
        }
        if (moyenTransport.equals("Transport Commun") ) {
            MT4="X";
        }else {
            MT4="";
        }
        if (moyenTransport.equals("Autre") ) {
            MT5="X";
        }else {
            MT5="";
        }

        parameters.put("MT1", MT1);
        parameters.put("MT2", MT2);
        parameters.put("MT3", MT3);
        parameters.put("MT4", MT4);
        parameters.put("MT5", MT5);
        parameters.put("id", OM.getId());


        Resource resource = new ClassPathResource("File/Ordre_Mission.jrxml");
        JasperReport compileReport = JasperCompileManager
                .compileReport(new FileInputStream(resource.getURL().getPath()));

        JasperPrint jasperPrint = JasperFillManager.fillReport(compileReport, parameters,beanCollectionDataSource);


        byte data[] = JasperExportManager.exportReportToPdf(jasperPrint);

        System.err.println(data);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=Ordre_Mission.pdf");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(data);
        
    }

}







