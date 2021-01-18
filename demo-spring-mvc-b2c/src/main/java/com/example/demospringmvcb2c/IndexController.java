package com.example.demospringmvcb2c;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

	@GetMapping("/")
	public String index(Model model,
						@RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
						@AuthenticationPrincipal OidcUser oauth2User,
						HttpServletRequest request) {
		
		Logger logger = LoggerFactory.getLogger(IndexController.class);

		model.addAttribute("userName", oauth2User.getName());
		model.addAttribute("clientName", authorizedClient.getClientRegistration().getClientName());
		model.addAttribute("userAttributes", oauth2User.getAttributes());
		model.addAttribute("authorities", oauth2User.getAuthorities());

		// this works when roles are provided by AAD in id_token:
		// if(request.isUserInRole("writerole")){
		// 	logger.info("The user is in writer role");
		// }

		// if(request.isUserInRole("readrole")){
		// 	logger.info("The user is in read role");
		// }

		return "index";
	}
}