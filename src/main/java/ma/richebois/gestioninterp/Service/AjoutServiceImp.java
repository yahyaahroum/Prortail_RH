package ma.richebois.gestioninterp.Service;

import ma.richebois.gestioninterp.Enum.IndividuState;
import ma.richebois.gestioninterp.Model.*;

import ma.richebois.gestioninterp.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AjoutServiceImp implements AjoutService {


    @Autowired
    private AjoutRepository ajoutRepository;

    @Autowired
    private ImportRepository importRepository;

    @Autowired
    private IndividuRepository individuRepository;

    @Autowired
    private ContratRepository contratRepository;

    @Autowired
    private AffaireRepository affaireRepository;

    @Autowired
    private FonctionRepository fonctionRepository;

    @Autowired
    private UserImpService userImpService;




    @Override
    public List<Ajout> listImport(long id) {
        Optional<Import> imp = importRepository.findById(id);

        List<Ajout> listImport = ajoutRepository.findAllByImp(imp);
        List<Ajout> listFreshImport = new ArrayList<Ajout>();


        for (Ajout person : listImport) {
            List<Individu> ind = individuRepository.findAllByCodecinOrderByIndividuDesc(person.getCodecin());
            if (ind.size() > 0) {
                Contrat contrat = contratRepository.findAllByMatriculeOrderByNumcontratDesc(ind.get(0).getIndividu()).get(0);
                person.setExist(true);

                if (contrat.getContratactif() == 2) {
                    person.setCactif(true);
                }
                if (contrat.getSuspendu() == 2) {
                    person.setCsuspendu(true);
                }
            }
            ajoutRepository.save(person);
            listFreshImport.add(person);

        }
        return listFreshImport;
    }


    @Override
    public List<Ajout> getAll() {
        return ajoutRepository.findAll(Sort.by(Sort.Direction.ASC,"nom"));
    }

    @Override
    public Ajout getByCin(String codecin) {
        return ajoutRepository.findByCodecin(codecin);
    }

    @Override
    public Ajout savePerson(MultipartFile file1, MultipartFile file2, MultipartFile file3, MultipartFile file4, MultipartFile file5, MultipartFile file6, Ajout ajout, String pessai) throws IOException {

        ajout.setFanthrop(false);
        ajout.setFcnss(false);
        ajout.setFrib(false);
        ajout.setFcv(false);

        String directory = ajout.getNom().replaceAll("\\s", "") + ajout.getPrenom().replaceAll("\\s", "");

//         Path root = Paths.get("C:\\Users\\e.abderrahmane\\Desktop\\Nouveaudossierp" + directory);
         Path root = Paths.get("/home/PortailRH-BETA-DOC/" + directory);
        if (Files.notExists(root)) {
            Files.createDirectory(root);
        }

        Files.copy(file1.getInputStream(), root.resolve("CIN Recto" + "_" + ajout.getCodecin().trim() + "_" + ajout.getCodechantier() + directory + ".pdf"),StandardCopyOption.REPLACE_EXISTING);
        Files.copy(file2.getInputStream(), root.resolve("CIN Verso" + "_" + ajout.getCodecin().trim() + "_" + ajout.getCodechantier() + directory + ".pdf"),StandardCopyOption.REPLACE_EXISTING);
        ajout.setFcin(true);

        if (file3.isEmpty()==false){
            Files.copy(file3.getInputStream(), root.resolve("Fiche_Anthropometrique" + "_" + ajout.getCodecin().trim() + "_" + ajout.getCodechantier() + directory + ".pdf"),StandardCopyOption.REPLACE_EXISTING);
            ajout.setFanthrop(true);
        }

        if (file4.isEmpty()==false){
            Files.copy(file4.getInputStream(), root.resolve("Attestation_CNSS" + "_" + ajout.getCodecin().trim() + "_" + ajout.getCodechantier() + directory + ".pdf"),StandardCopyOption.REPLACE_EXISTING);
            ajout.setFcnss(true);
        }

        if (file5.isEmpty()==false){
            Files.copy(file5.getInputStream(), root.resolve("Attestation_RIB" + "_" + ajout.getCodecin().trim() + "_" + ajout.getCodechantier() + directory + ".pdf"),StandardCopyOption.REPLACE_EXISTING);
            ajout.setFrib(true);
        }

        if (file6.isEmpty()==false){
            Files.copy(file6.getInputStream(), root.resolve("CV" + "_" + ajout.getCodecin().trim() + "_" + ajout.getCodechantier() + directory + ".pdf"),StandardCopyOption.REPLACE_EXISTING);
            ajout.setFcv(true);
        }

        if (pessai.equals("")) {
            ajout.setPessai("15");
        } else if (pessai != "") {
            ajout.setPessai(pessai);
        }

        if (ajout.getBulletin()==null){ajout.setBulletin(false);}
        if (ajout.getWhatsapp()==null){ajout.setWhatsapp(false);}

        ajout.setModeregl("V");
        ajout.setOrigine("Saisi");
        ajout.setTypecontrat("CDD");


        ajout.setDesignation(affaireRepository.findByCode(ajout.getCodechantier()).getDesignation());


        ajout.setLibelle(fonctionRepository.findByCodefonction(ajout.getFonction()).getLibelle());
        ajout.setState(IndividuState.A_Valider.name());
        return ajoutRepository.save(ajout);
    }

    @Override
    public Ajout updatePerson(MultipartFile file1, MultipartFile file2, MultipartFile file3, MultipartFile file4, MultipartFile file5, MultipartFile file6, Ajout ajout) throws IOException {

        ajout.setFcin(false);
        ajout.setFanthrop(false);
        ajout.setFcnss(false);
        ajout.setFrib(false);
        ajout.setFcv(false);

        String directory = ajout.getNom().replaceAll("\\s", "") + ajout.getPrenom().replaceAll("\\s", "");

        Path root = Paths.get("/home/PortailRH-BETA-DOC/" + directory +"/");

        if (Files.notExists(root)) {
            Files.createDirectory(root);
        }

        if (ajout.getBulletin()==null){ajout.setBulletin(false);}
        if (ajout.getWhatsapp()==null){ajout.setWhatsapp(false);}


        if (file1.isEmpty()==false){
        Files.copy(file1.getInputStream(), root.resolve("CIN Recto" + "_" + ajout.getCodecin().trim() + "_" + ajout.getCodechantier() + directory + ".pdf"), StandardCopyOption.REPLACE_EXISTING);
        ajout.setFcin(true);
        }

        if (file2.isEmpty()==false){
            Files.copy(file2.getInputStream(), root.resolve("CIN Verso" + "_" + ajout.getCodecin().trim() + "_" + ajout.getCodechantier() + directory + ".pdf"), StandardCopyOption.REPLACE_EXISTING);
            ajout.setFcin(true);
        }
        if (file3.isEmpty()==false){
            Files.copy(file3.getInputStream(), root.resolve("Fiche_Anthropometrique" + "_" + ajout.getCodecin().trim() + "_" + ajout.getCodechantier() + directory + ".pdf"), StandardCopyOption.REPLACE_EXISTING);
            ajout.setFanthrop(true);
        }
        if (file4.isEmpty()==false){
            Files.copy(file4.getInputStream(), root.resolve("Attestation_CNSS" + "_" + ajout.getCodecin().trim() + "_" + ajout.getCodechantier() + directory + ".pdf"), StandardCopyOption.REPLACE_EXISTING);
            ajout.setFcnss(true);
        }
        if (file5.isEmpty()==false){
            Files.copy(file5.getInputStream(), root.resolve("Attestation_RIB" + "_" + ajout.getCodecin().trim() + "_" + ajout.getCodechantier() + directory + ".pdf"), StandardCopyOption.REPLACE_EXISTING);
            ajout.setFrib(true);
        }
        if (file6.isEmpty()==false){
            Files.copy(file6.getInputStream(), root.resolve("CV" + "_" + ajout.getCodecin().trim() + "_" + ajout.getCodechantier() + directory + ".pdf"), StandardCopyOption.REPLACE_EXISTING);
            ajout.setFcv(true);
        }

        ajout.setModeregl("V");
        ajout.setOrigine("Saisi");
        ajout.setTypecontrat("CDD");
        ajout.setDesignation(affaireRepository.findByCode(ajout.getCodechantier()).getDesignation());
        ajout.setLibelle(fonctionRepository.findByCodefonction(ajout.getFonction()).getLibelle());
        ajout.setState(IndividuState.A_Valider.name());

        return ajoutRepository.save(ajout);
    }

    @Override
    public Page<Ajout> findPaginatedAjout(Pageable pageable) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Ajout> listCons;

        if (getAll().size() < startItem) {
            listCons = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, getAll().size());
            listCons = getAll().subList(startItem, toIndex);
        }
        Page<Ajout> consultPage = new PageImpl<Ajout>(listCons, PageRequest.of(currentPage, pageSize), getAll().size());


        return consultPage;
    }


    @Override
    public Ajout valider(long id, String salaire, String pessai) {

        Optional<Ajout> ajout = ajoutRepository.findById(id);
        int lastMatricule = getLastMAtricul(ajout.get());

        ajout.get().setMatricule(lastMatricule);
        ajout.get().setState(IndividuState.Validé.name());

        ajout.get().setSalaire(Double.parseDouble(salaire));
        ajout.get().setPessai(pessai);
        ajoutRepository.save(ajout.get());

        return ajout.get();
    }

    @Override
    public Ajout rejeter(long id,String motifRejet) {
        Optional<Ajout> ajout = ajoutRepository.findById(id);
        ajout.get().setState(IndividuState.Rejeté.name());
        ajout.get().setMotifRejet(motifRejet);
        ajoutRepository.save(ajout.get());
        return null;
    }

    @Override
    public Ajout findById(long id) {
        Optional<Ajout> p = ajoutRepository.findById(id);
        return p.get();
    }


    @Override
    public Boolean deletePerson(Long id) {
        ajoutRepository.deleteById(id);

        return true;
    }


    @Override
    public Integer getLastMAtricul(Ajout ajout) {

        List<String> states = new ArrayList<String>();
        states.add(IndividuState.Validé.name());
        states.add(IndividuState.A_Imprimer.name());
        states.add(IndividuState.Imprimé.name());

        List<Individu> individu = individuRepository.findAllByCodecinOrderByIndividuDesc(ajout.getCodecin());

        if (individu.isEmpty()){
            List<Ajout> ajoutList = ajoutRepository.findAllByStateInOrderByMatriculeDesc(states);

            int lastmatriculeajout = ajoutList.get(0).getMatricule();

            List<Individu> individuList = individuRepository.findAll(Sort.by(Sort.Direction.DESC,"individu"));

            int lastIndividu = individuList.get(0).getIndividu();

            if (lastmatriculeajout != 0 && lastmatriculeajout > lastIndividu) {
                return lastmatriculeajout + 1;
            } else
                return lastIndividu + 1;
        }else
            return individu.get(0).getIndividu();






    }

    @Override
    public List<Ajout> getPersonnesSaisie(String start,String end) throws ParseException {
        Date datestart = new SimpleDateFormat("yyyy-MM-dd").parse(start);
        Date dateend = new SimpleDateFormat("yyyy-MM-dd").parse(end);
        List<String> states = new ArrayList<>();
        states.add(IndividuState.Validé.name());
        states.add(IndividuState.A_Imprimer.name());
        states.add(IndividuState.Imprimé.name());
        List<String> origine = new ArrayList<>();
        origine.add("Saisi");
        List<Ajout> ajoutSaisie = ajoutRepository.findAllByStateInAndOrigineInAndDateentreeBetweenOrderByMatriculeAsc(states,origine,datestart,dateend);

        return ajoutSaisie;
    }



    @Override
    public Ajout personneContractUpload(Long id, MultipartFile file1, MultipartFile file2, MultipartFile file3, Optional<Ajout> ajout) throws IOException {

        ajout = ajoutRepository.findById(id);
        String directory = ajout.get().getNom().trim().replaceAll("\\s", "") + ajout.get().getPrenom().trim().replaceAll("\\s", "");


        Path root = Paths.get("/home/PortailRH-BETA-DOC/" + directory);

        if (Files.notExists(root)) {
            Files.createDirectory(root);
        }

        Files.copy(file1.getInputStream(), root.resolve("Contrat" + "_" + ajout.get().getCodecin().trim() + "_" + ajout.get().getCodechantier() + ".pdf"));
        Files.copy(file2.getInputStream(), root.resolve("Contrat_Decision" + "_" + ajout.get().getCodecin().trim() + "_" + ajout.get().getCodechantier() + ".pdf"));
        Files.copy(file3.getInputStream(), root.resolve("Contrat_Decision_Equip" + "_" + ajout.get().getCodecin().trim() + "_" + ajout.get().getCodechantier() + ".pdf"));

        ajout.get().setState(IndividuState.Imprimé.name());


        return ajoutRepository.save(ajout.get());
    }

    @Override
    public Individu getIndividu(Long id) {
        Optional<Individu> individu = individuRepository.findById(id);

        return individu.get();
    }

    @Override
    public List<Ajout> getPersonByState(String state) {
        Login login = userImpService.findbyusername();
        List<Role> roles = login.getRoles();
        List<String> codeChantier = new ArrayList<String>();
        List<Affaire> affaireList = login.getChantier();
        List<Ajout> ajoutList = new ArrayList<>();

        for (Role role : roles){
            if (role.getType().equals("Pointeur")){
                for (Affaire chantier:affaireList){
                    codeChantier.add(chantier.getCode());
                    if (state.equals("All")){
                        List<Ajout> ajoutList1 = ajoutRepository.findAllByCodechantierInAndOrigineOrderByNomAsc(codeChantier,"Saisi");
                        ajoutList.addAll(ajoutList1);
                    }
                    if (!state.equals("All")){
                        List<Ajout> listPerStateAndAffaire = ajoutRepository.findAllByCodechantierInAndOrigineAndStateOrderByNom(codeChantier,"Saisi",state);
                        ajoutList.addAll(listPerStateAndAffaire);
                    }

                }
                return ajoutList;
            }else
            if (state.equals("All")){
               return ajoutRepository.findAllByOrigineOrderByNomAsc("Saisi");
            }
            if (!state.equals("All")){
                return ajoutRepository.findAllByStateAndOrigineOrderByNomAsc(state,"Saisi");
            }

        }
        return null;
    }

    @Override
    public Page<Ajout> findPaginatedAjoutByState(Pageable pageable, String state) {
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Ajout> listCons;

        if (getPersonByState(state).size() < startItem) {
            listCons = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, getPersonByState(state).size());
            listCons = getPersonByState(state).subList(startItem, toIndex);
        }
        Page<Ajout> consultPage = new PageImpl<Ajout>(listCons, PageRequest.of(currentPage, pageSize), getPersonByState(state).size());


        return consultPage;
    }


}
