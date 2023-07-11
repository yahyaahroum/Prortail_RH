package ma.richebois.gestioninterp.Service;

import ma.richebois.gestioninterp.DTO.CongeDTO;
import ma.richebois.gestioninterp.DTO.DemandeDTO;
import ma.richebois.gestioninterp.Domaine.MyConstants;
import ma.richebois.gestioninterp.Enum.DemandeState;
import ma.richebois.gestioninterp.Model.*;
import ma.richebois.gestioninterp.Repository.*;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

@Service
public class DemandeImpService implements DemandeService {

    @Autowired
    private DemandeRepository demandeRepository;

    @Autowired
    private TypeDemandeRepository typeDemandeRepository;

    @Autowired
    private UserImpService userImpService;

    @Autowired
    private IndividuRepository individuRepository;

    @Autowired
    private ContratRepository contratRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private LoginRepository loginRepository;

    @Autowired
    public JavaMailSender emailSender;

    @Autowired
    private AjoutRepository ajoutRepository;


    @Override
    public Demande saveDemande(Demande demande,String numTele) {
        Login login = userImpService.findbyusername();
        Individu individu = individuRepository.findByIndividu(login.getMatricule());
        individu.setTele(numTele);
        demande.setEtat(DemandeState.Demandée.name());
        demande.setEmp(login);
        if (demande.getTypeDemande().getType().equals("Demande congé")) {
            if (demande.getDateFin().before(demande.getDateDebut())){
                return null;
            }
        }
        Demande demande1 = demandeRepository.save(demande);
        individuRepository.save(individu);
        return demande1;
    }

    @Override
    public Page<Demande> getAllByEmpPeagable(Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Demande> listCons;

        if (getAllByEmp().size() < startItem) {
            listCons = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, getAllByEmp().size());
            listCons = getAllByEmp().subList(startItem, toIndex);
        }
        Page<Demande> consultPage = new PageImpl<Demande>(listCons, PageRequest.of(currentPage, pageSize), getAllByEmp().size());

        return consultPage;
    }

    @Override
    public List<Demande> getAllByEmp() {
        Login login = userImpService.findbyusername();
        List<Role> roles= login.getRoles();
        for (Role role : roles) {
            if (role.getType().equals("employé")) {
                List<Demande> getdemandeByRole = demandeRepository.findAllByEmpOrderByIdDesc(login);
                return getdemandeByRole;
            }else
                return demandeRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        }
        return null;
    }

    @Override
    public List<TypeDemande> getAllType() {

        return typeDemandeRepository.findAll();
    }

    @Override
    public boolean deleteDemande(Long id) {
        demandeRepository.deleteById(id);
        return true;
    }

    @Override
    public Demande valider(Long id) {
        Optional<Demande> demande = demandeRepository.findById(id);
        demande.get().setEtat(DemandeState.Validée.name());
        return demandeRepository.save(demande.get());
    }

    @Override
    public Demande refuser(Long id,String motifRefus) {
        Optional<Demande> demande = demandeRepository.findById(id);
        demande.get().setEtat(DemandeState.Refusée.name());
        demande.get().setMotifRefus(motifRefus);
        return demandeRepository.save(demande.get());
    }

    @Override
    public List<Demande> getAllByEmpAndState(String state) {
        Login login = userImpService.findbyusername();
        List<Role> roles= login.getRoles();
        for (Role role : roles) {
            if (role.getType().equals("Employé")) {
                if (state.equals("All")){
                    List<Demande> getdemandeByRole = demandeRepository.findAllByEmpOrderByIdDesc(login);
                    return getdemandeByRole;
                }
                if (state.equals("Demandée")){
                    List<Demande> getdemandeByRoleAndState = demandeRepository.findAllByEmpAndEtatOrderByIdDesc(login,DemandeState.Demandée.name());
                    return getdemandeByRoleAndState;
                }
                if (state.equals("Validée")){
                    List<Demande> getdemandeByRoleAndState = demandeRepository.findAllByEmpAndEtatOrderByIdDesc(login,DemandeState.Validée.name());
                    return getdemandeByRoleAndState;
                }
                if (state.equals("Refusée")){
                    List<Demande> getdemandeByRoleAndState = demandeRepository.findAllByEmpAndEtatOrderByIdDesc(login,DemandeState.Refusée.name());
                    return getdemandeByRoleAndState;
                }
                if (state.equals("En_cours_de_signature")){
                    List<Demande> getdemandeByRoleAndState = demandeRepository.findAllByEmpAndEtatOrderByIdDesc(login,DemandeState.En_cours_de_signature.name());
                    return getdemandeByRoleAndState;
                }

            } if (!role.getType().equals("Employé")) {
                if (role.getType().equals("RH") || role.getType().equals("admin")){
                    if (state.equals("All")){
                        return demandeRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
                    }
                    if (state.equals("Demandée")){
                        return demandeRepository.findAllByEtatOrderByIdDesc(DemandeState.Demandée.name());
                    }
                    if (state.equals("Validée")){
                        return demandeRepository.findAllByEtatOrderByIdDesc(DemandeState.Validée.name());
                    }
                    if (state.equals("Refusée")){
                        return demandeRepository.findAllByEtatOrderByIdDesc(DemandeState.Refusée.name());
                    }
                    if (state.equals("En_cours_de_signature")){
                        List<Demande> getdemandeByRoleAndState = demandeRepository.findAllByEtatOrderByIdDesc(DemandeState.En_cours_de_signature.name());
                        return getdemandeByRoleAndState;
                    }
                }

            }

        }
        return null;
    }

    @Override
    public Page<Demande> findByEmpAndState(Pageable pageable, String state) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Demande> listCons;

        if (getAllByEmpAndState(state).size() < startItem) {
            listCons = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, getAllByEmpAndState(state).size());
            listCons = getAllByEmpAndState(state).subList(startItem, toIndex);
        }
        Page<Demande> consultPage = new PageImpl<Demande>(listCons, PageRequest.of(currentPage, pageSize), getAllByEmpAndState(state).size());

        return consultPage;
    }

    @Override
    public ResponseEntity<byte[]> generateDemande(Long id) throws JRException, IOException {

        Optional<Demande> demande = demandeRepository.findById(id);
        Individu individu = individuRepository.findAllByIndividuOrderByIndividuDesc(demande.get().getEmp().getMatricule());
        List<Contrat> contrat = contratRepository.findAllByMatriculeOrderByNumcontratDesc(individu.getIndividu());

        if (demande.get().getTypeDemande().getType().equals("Demande congé") && individu!=null && !contrat.isEmpty()){
            CongeDTO congeDTO = new CongeDTO();

            Calendar cal = Calendar.getInstance();
            cal.setTime(demande.get().getDateFin());
            cal.add(Calendar.DATE, 1);

            if (cal.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY){
                cal.add(Calendar.DATE, 1);
            }

            congeDTO.setMatricule(individu.getIndividu());
            congeDTO.setNom(individu.getNom());
            congeDTO.setPrenom(individu.getPrenom());
            congeDTO.setAdresserue(individu.getAdresserue());
            congeDTO.setDateentree(contrat.get(0).getDateembauche());
            congeDTO.setDateDebut(demande.get().getDateDebut());
            congeDTO.setDateFin(demande.get().getDateFin());
            congeDTO.setMotif(demande.get().getMotif());
            congeDTO.setException(demande.get().getException());
            if (!individu.getTele().equals(null)){
                congeDTO.setNumTele(individu.getTele());
            }
            congeDTO.setDateReprise(cal.getTime());
            congeDTO.setNbrJour((int) ((demande.get().getDateFin().getTime()-demande.get().getDateDebut().getTime())/(1000*60*60*24))+1);
            if (demande.get().getInterime()!=null){
                congeDTO.setInterime(demande.get().getInterime().getNom()+" "+demande.get().getInterime().getPrenom());
            }
            if (demande.get().getInterime()==null){
                congeDTO.setInterime("Non renseigné");
            }

            Resource resource = new ClassPathResource("File/DemandeCongeEmp.jrxml");

            JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(Collections.singleton(congeDTO));
            JasperReport compileReport = JasperCompileManager.compileReport(new FileInputStream(resource.getURL().getPath()));

            HashMap<String, Object> map = new HashMap<>();
            JasperPrint report = JasperFillManager.fillReport(compileReport, map, beanCollectionDataSource);

            byte[] data = JasperExportManager.exportReportToPdf(report);
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=Demande_Congé_"+congeDTO.getNom().trim().toUpperCase()+"_"+congeDTO.getPrenom().trim().toUpperCase()+".pdf");

            if (!demande.get().getEtat().equals("Validée") || !demande.get().getEtat().equals("Refusée")){
                demande.get().setEtat(DemandeState.En_cours_de_signature.name());
                demandeRepository.save(demande.get());
            }

            return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(data);
        }
        if (demande.get().getTypeDemande().getType().equals("Demande Attestation de travail") && individu!=null && !contrat.isEmpty()){
            List<Object[]> demandeObject = demandeRepository.getDemande(demande.get().getId());
            DemandeDTO demandeDTO= new DemandeDTO();

            demandeDTO.setNom((String) demandeObject.get(0)[1]);
            demandeDTO.setPrenom((String) demandeObject.get(0)[3]);
            demandeDTO.setLibelle((String) demandeObject.get(0)[2]);
            demandeDTO.setDateentree((Date) demandeObject.get(0)[4]);
            demandeDTO.setCodecin((String) demandeObject.get(0)[6]);
            demandeDTO.setCnss((String) demandeObject.get(0)[7]);

            Resource resource = new ClassPathResource("File/ATTESTATIONTRAVAIL.jrxml");

            JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(Collections.singleton(demandeDTO));
            JasperReport compileReport = JasperCompileManager.compileReport(new FileInputStream(resource.getURL().getPath()));

            HashMap<String, Object> map = new HashMap<>();
            JasperPrint report = JasperFillManager.fillReport(compileReport, map, beanCollectionDataSource);

            byte[] data = JasperExportManager.exportReportToPdf(report);
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=Demande_Attestation_Travail_"+demandeDTO.getNom().trim().toUpperCase()+"_"+demandeDTO.getPrenom().trim().toUpperCase()+".pdf");

            if (!demande.get().getEtat().equals("Validée") || !demande.get().getEtat().equals("Refusée")){
                demande.get().setEtat(DemandeState.En_cours_de_signature.name());
                demandeRepository.save(demande.get());
            }
            return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(data);
        }
        return null;
    }

    @Override
    public void sendEmailDemande(Demande demande) {
        Role role = roleRepository.findByType("RH");
        List<Role> roles = new ArrayList<>();
        roles.add(role);
        List<Login> rhusers = loginRepository.findAllByRolesIn(roles);
        for (Login user : rhusers) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(MyConstants.MY_EMAIL);
            message.setTo(user.getEmail());
            message.setSubject("Validation d'une demande sur PORTAILRH");
            message.setText("Bonjour \n \n " +
                    "       Merci de valider la demande : "+demande.getTypeDemande().getType().trim().toUpperCase()+" préparé par : " +demande.getEmp().getNom().toUpperCase().trim()+" "+demande.getEmp().getPrenom().toUpperCase().trim()+" . \n\n"
                    +"Sincéres salutations");
            this.emailSender.send(message);
        }
    }

}
