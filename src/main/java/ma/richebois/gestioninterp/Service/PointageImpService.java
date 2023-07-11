package ma.richebois.gestioninterp.Service;

import ma.richebois.gestioninterp.Model.*;
import ma.richebois.gestioninterp.Repository.AffaireRepository;
import ma.richebois.gestioninterp.Repository.ContratRepository;
import ma.richebois.gestioninterp.Repository.IndividuRepository;
import ma.richebois.gestioninterp.Repository.PointageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Service
public class PointageImpService implements PointageService{

    @Autowired
    private PointageRepository pointageRepository;

    @Autowired
    private IndividuRepository individuRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private AffaireRepository affaireRepository;

    @Autowired
    private ContratRepository contratRepository;

    @Override
    public Page<Pointage> pointageList(Pageable pageable) {
        Login login = userService.findbyusername();
        List<Affaire> affaires = login.getChantier();
        List<Affaire> affaires1 = affaireRepository.findAll();
        for (Role role : login.getRoles()){
            if (role.getType().equals("Pointeur") || role.getType().equals("ChefProjet")){
                Page<Pointage> pointagePage = pointageRepository.findAllByAffaireInOrderByDatePointageDesc(pageable,affaires);
                return pointagePage;
            }else {
                Page<Pointage> pointagePage = pointageRepository.findAllByAffaireInOrderByDatePointageDesc(pageable,affaires1);
                return pointagePage;
            }

        }

        return null;
    }

    @Override
    public List<Pointage> pointages() {
        return pointageRepository.findAll();
    }

    @Override
    public int countPointageByChantier() {
        Login login = userService.findbyusername();
        List<Affaire> affaires = login.getChantier();

        return 0;
    }

    @Override
    public void controlePointage(String dateStart,String dateEnd) throws ParseException {
        List<Contrat> contrats = contratRepository.findAllByContratactif(2);
        List<Pointage> pointageList = new ArrayList<>();

        for (Contrat c : contrats) {
            Individu individu = individuRepository.findByIndividu(c.getMatricule());

            System.out.println("contrat : "+ c.getMatricule());
            System.out.println("individu : "+ individu.getIndividu());
            Date datestart = new SimpleDateFormat("yyyy-MM-dd").parse(dateStart);
            Date dateend = new SimpleDateFormat("yyyy-MM-dd").parse(dateEnd);

            Calendar start = Calendar.getInstance();
            start.setTime(datestart);
            Calendar end = Calendar.getInstance();
            end.setTime(dateend);

            Affaire affaire = null;

            for (Date date = start.getTime(); start.before(end); start.add(Calendar.DATE, 1), date = start.getTime()) {
                System.out.println("Avant Pointage Date : "+date +" Individu :" +individu.getIndividu());
                Pointage p = pointageRepository.findByEmpAndDatePointage(individu,date);
                if (p!=null){
                    affaire = p.getAffaire();

                }
                if (p==null){
                    Pointage pointage = new Pointage();
                    pointage.setDatePointage(date);
                    if (affaire==null){
                        pointage.setAffaire(null);
                    }
                    if (affaire!=null){
                        pointage.setAffaire(affaire);
                    }
                    if (!c.getCodechantier().equals("") || c.getCodechantier()!=null){
                        Affaire aff = affaireRepository.findByCode(c.getCodechantier());
                        pointage.setAffaire(aff);
                    }
                    pointage.setEmp(individu);
                    pointage.setNbrHeure(0);
                    pointageRepository.save(pointage);
                }

            }

        }

    }

}
