package ma.richebois.gestioninterp.Service;

import ma.richebois.gestioninterp.DTO.DechargeDto;
import ma.richebois.gestioninterp.Enum.DechargeState;
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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DechargeImpService implements DechargeService{


    @Autowired
    private DechargeRepository dechargeRepository;

    @Autowired
    private MaterielRepository materielRepository;

    @Autowired
    private AffaireRepository affaireRepository;

    @Autowired
    private IndividuRepository individuRepository;

    @Autowired
    private DechargeDetRepository dechargeDetRepository;

    @Autowired
    private DechargePJRepository dechargePJRepository;


    @Override
    public List<Decharge> getAllDecharge(String statut) {

        if (statut.equals("All")){
            return dechargeRepository.findAll(Sort.by(Sort.Direction.DESC,"id"));
        }
        else
        return dechargeRepository.findAllByStatutOrderByIdDesc(statut);
    }

    @Override
    public Decharge saveDecharge(DechargeDto decharge){
        Optional<Individu> individu = individuRepository.findById(decharge.getIndividu());
        Optional<Affaire> affaire = affaireRepository.findById(decharge.getChantier());
        List<DechargeDet> dechargeDetList = new ArrayList<>();
        Decharge decharge1 = new Decharge();
        decharge1.setDateDecharge(decharge.getDateDecharge());
        decharge1.setIndividu(individu.get());
        decharge1.setChantier(affaire.get());
        decharge1.setStatut(DechargeState.BROUILLON.name());
        Decharge dechargeSave = dechargeRepository.save(decharge1);
        decharge.getDechargeDetList().forEach(dechargeDetDTO -> {
            Optional<Materiel> materiel = materielRepository.findById(dechargeDetDTO.getMateriel());
            DechargeDet dechargeDet = new DechargeDet();
            dechargeDet.setMateriel(materiel.get());
            dechargeDet.setQuantite(dechargeDetDTO.getQuantite());
            dechargeDet.setMarque(dechargeDetDTO.getMarque());
            dechargeDet.setModel(dechargeDetDTO.getModel());
            dechargeDet.setDecharge(dechargeSave);
            DechargeDet dechargeDet1 = dechargeDetRepository.save(dechargeDet);
            dechargeDetList.add(dechargeDet1);
        });
        dechargeSave.setDechargeDetList(dechargeDetList);

        dechargeRepository.save(dechargeSave);
        return dechargeSave;
    }

    @Override
    public List<Materiel> getAllMaterial() {
        return null;
    }

    @Override
    public ResponseEntity<byte[]> generateDecharge(long id) throws IOException, JRException {

        Optional<Decharge> decharge = dechargeRepository.findById(id);
        List<DechargeDet> dechargeDetList = dechargeDetRepository.findAllByDecharge(decharge.get());

        Resource resource = new ClassPathResource("File/Decharge.jrxml");

        JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(dechargeDetList);
        JasperReport compileReport = JasperCompileManager.compileReport(new FileInputStream(resource.getURL().getPath()));

        HashMap<String, Object> map = new HashMap<>();
        JasperPrint report = JasperFillManager.fillReport(compileReport, map, beanCollectionDataSource);

        byte[] data = JasperExportManager.exportReportToPdf(report);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=Programme_Approvisionnement.pdf");

        decharge.get().setStatut(DechargeState.IMPRIMEE.name());
        dechargeRepository.save(decharge.get());

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(data);
    }

    @Override
    public Decharge expedier(long id) {
        Optional<Decharge> decharge = dechargeRepository.findById(id);
        decharge.get().setStatut(DechargeState.EXPEDIEE.name());
        return dechargeRepository.save(decharge.get());
    }

    @Override
    public Decharge annuler(long id) {
        Optional<Decharge> decharge = dechargeRepository.findById(id);
        decharge.get().setStatut(DechargeState.ANNULEE.name());
        return dechargeRepository.save(decharge.get());
    }

    @Override
    public Decharge aRecuperer(long id) {
        Optional<Decharge> decharge = dechargeRepository.findById(id);
        decharge.get().setStatut(DechargeState.A_RECUPERER.name());
        return dechargeRepository.save(decharge.get());
    }

    @Override
    public Decharge signer(long id,MultipartFile file) throws IOException {
        Optional<Decharge> decharge = dechargeRepository.findById(id);
        String directory = decharge.get().getIndividu().getNom().trim().replaceAll("\\s", "") + decharge.get().getIndividu().getPrenom().trim().replaceAll("\\s", "");
        Path root = Paths.get("/home/PortailRH-BETA-DOC/" + directory);
        Path rootDecharge = Paths.get("/home/PortailRH-BETA-DOC/" + directory+"/Decharge");
        DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
//        Path root = Paths.get("C:\\Users\\e.abderrahmane\\Desktop\\Doc\\" + directory);
//        Path rootDecharge = Paths.get("C:\\Users\\e.abderrahmane\\Desktop\\Doc\\" + directory+"\\Decharge");

        if (Files.notExists(root)) {
            Files.createDirectory(root);
        }
        if (Files.exists(root)){
            if (Files.notExists(rootDecharge)){
                Files.createDirectory(rootDecharge);
            }
        }

        DechargePJ dechargePJ = new DechargePJ();
        dechargePJ.setDecharge(decharge.get());
        dechargePJ.setName("Decharge_"+dateFormat.format(decharge.get().getDateDecharge())+"_"+directory+".pdf");
        Files.copy(file.getInputStream(), rootDecharge.resolve("Decharge_"+dateFormat.format(decharge.get().getDateDecharge())+"_"+directory+".pdf"), StandardCopyOption.REPLACE_EXISTING);
        dechargePJ.setDecharge(decharge.get());
        DechargePJ pj = dechargePJRepository.save(dechargePJ);
        decharge.get().setStatut(DechargeState.SIGNEE.name());
        return dechargeRepository.save(decharge.get());
    }

    @Override
    public Decharge liberer(long id) {
        Optional<Decharge> decharge = dechargeRepository.findById(id);
        decharge.get().setStatut(DechargeState.LIBEREE.name());
        return dechargeRepository.save(decharge.get());
    }

    @Override
    public Page<Decharge> dechargeList(Pageable pageable,String statut) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Decharge> listCons;

        if (getAllDecharge(statut).size() < startItem) {
            listCons = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, getAllDecharge(statut).size());
            listCons = getAllDecharge(statut).subList(startItem, toIndex);
        }
        Page<Decharge> consultPage = new PageImpl<Decharge>(listCons, PageRequest.of(currentPage, pageSize), getAllDecharge(statut).size());

        return consultPage;
    }
}
