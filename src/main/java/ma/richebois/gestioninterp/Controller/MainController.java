package ma.richebois.gestioninterp.Controller;

import ma.richebois.gestioninterp.Model.Login;
import ma.richebois.gestioninterp.Service.LoginImpService;
import ma.richebois.gestioninterp.Service.UserImpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @Autowired
    private UserImpService userImpService;


    @GetMapping("/login")
    public String login() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "login";
        }else
            return "redirect:/";

    }

    @GetMapping("/logout")
    public String getlogout(){
        return "login";
    }

}
