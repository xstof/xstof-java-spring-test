package fta.identity.sample.aad3auth;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    //@PreAuthorize("permitAll()")
    public String Me(
        Model model//,
        /*OAuth2AuthenticationToken authentication*/){
        // @RegisteredOAuth2AuthorizedClient("azure") OAuth2AuthorizedClient authorizedClient){

        Logger logger = LoggerFactory.getLogger(IndexController.class);
        logger.info("authentication sample - entered home controller");

        //model.addAttribute("username", authentication.getName());
        model.addAttribute("username", "TEST");
        // logger.info("authentication - username: {}", authentication.getName());

        // model.addAttribute("userdetails", authentication.getDetails());

        // Collection<GrantedAuthority> authorities  = authentication.getAuthorities();
        // for (GrantedAuthority grantedAuthority : authorities) {
        //     logger.info("authentication - found one authority: {}", grantedAuthority.getAuthority());
        // }

        // model.addAttribute("authorities", authentication.getAuthorities().toArray());
        
        // model.addAttribute("principal", authentication.getPrincipal());
        // logger.info("authentication - principal name: {}", authentication.getPrincipal().getName());

        // model.addAttribute("clientregid", authentication.getAuthorizedClientRegistrationId());
        // logger.info("authentication - client registration id: {}", authentication.getAuthorizedClientRegistrationId());

        return "index";
    }

    @GetMapping("/authenticated")
    //@PreAuthorize("permitAll()")
    //@PreAuthorize("isAuthenticated()")
    public String AuthenticatedPage(
        Model model,
        OAuth2AuthenticationToken authentication){

        Logger logger = LoggerFactory.getLogger(IndexController.class);
        logger.info("authentication sample - entered authenticated controller");

        model.addAttribute("authorities", authentication.getAuthorities().toArray());
        model.addAttribute("username", "TEST");

        // AAD roles that come in the incoming id_token are not translated into an Authority by default; rather can be found in the "roles" attribute of the principal, as follows:
        logger.info("authentication sample - roles that were found in attributes of principal: {}", authentication.getPrincipal().getAttribute("roles").toString());

        return "authenticated";
    }
}
