package com.codeoftheweb.salvo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Date;


@SpringBootApplication
public class SalvoApplication {


	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class, args);
	}


	@Bean
	public CommandLineRunner initData(PlayerRepository playerRepository, GameRepository gameRepository, GamePlayerRepository gamePlayerRepository,ShipRepository shipRepository,SalvoRepository salvoRepository,ScoreRepository scoreRepository) {
		return (args) -> {

			Player p1 = new Player("Arya@stark.com",passwordEncoder().encode("stark"));
			Player p2 = new Player("Jon@Snow.com",passwordEncoder().encode("snow"));
			Player p3 = new Player("Tyrion@lannister.com",passwordEncoder().encode("lannister"));
			Player p4 = new Player("Dany@Targaryan.com",passwordEncoder().encode("targaryan"));

			Game g1 = new Game();
			Game g2 = new Game();
			Game g3 = new Game();
			Game g4 = new Game();
			//Game g5 = new Game();
			//Game g6 = new Game();

			Date f1 = new Date();
			g1.setCreationDate(f1);
			Date f2 = new Date();
			g2.setCreationDate(f2);
			Date f3 = new Date();


			GamePlayer gp1 = new GamePlayer( g1, p1);
			GamePlayer gp2 = new GamePlayer( g1, p2);
			GamePlayer gp3 = new GamePlayer( g2, p4);
			GamePlayer gp4 = new GamePlayer( g2, p3);
			GamePlayer gp5 = new GamePlayer(g3,p1);
			GamePlayer gp6 = new GamePlayer(g4,p2);



			playerRepository.save(p1);
			playerRepository.save(p2);
			playerRepository.save(p3);
			playerRepository.save(p4);

			gameRepository.save(g1);
			gameRepository.save(g2);
			gameRepository.save(g3);
			gameRepository.save(g4);
			//gameRepository.save(g5);
			//gameRepository.save(g6);

			gamePlayerRepository.save(gp1);
			gamePlayerRepository.save(gp2);
			gamePlayerRepository.save(gp3);
			gamePlayerRepository.save(gp4);
			gamePlayerRepository.save(gp5);
			gamePlayerRepository.save(gp6);

			/*List<String> locationShipList1 = new ArrayList<>();
			locationShipList1.add("H1");
			locationShipList1.add("H3");
			locationShipList1.add("H2");

			List<String> locationShipList2 = new ArrayList<>();
			locationShipList2.add("D1");
			locationShipList2.add("D3");
			locationShipList2.add("D2");

			List<String> locationShipList3 = new ArrayList<>();
			locationShipList3.add("A2");
			locationShipList3.add("A8");
			locationShipList3.add("A3");

			List<String> locationSalvoList1 = new ArrayList<>();
			locationSalvoList1.add("H1");
			locationSalvoList1.add("B5");
			locationSalvoList1.add("H3");

			List<String> locationSalvoList3 = new ArrayList<>();
			locationSalvoList3.add("D3");
			locationSalvoList3.add("D2");
			locationSalvoList3.add("D1");

			Ship sh1 = new Ship("ARMY",gp1,locationShipList1);
			Ship sh2 = new Ship("ARMY2",gp2,locationShipList2);
			Ship sh3 = new Ship("ARMY3",gp1,locationShipList2);

			shipRepository.save(sh1);
			shipRepository.save(sh2);
			shipRepository.save(sh3);

			Salvo salvo = new Salvo(12,gp1,locationSalvoList3);
			Salvo salvo2 = new Salvo(10,gp2,locationSalvoList3);

			salvoRepository.save(salvo);
			salvoRepository.save(salvo2);

			Score score = new Score(g1,p1,1,f3);
			Score score1= new Score(g1,p2,0,f3);

			Score score2= new Score(g2,p1,(float)0.5,f3);
			Score score3= new Score(g2,p2,(float)0.5,f3);

			Score score4= new Score(g3,p1,1,f3);
			Score score5= new Score(g3,p2,0,f3);


			Score score6= new Score(g4,p3,0,f3);
			Score score7= new Score(g4,p4,1,f3);


			scoreRepository.save(score);
			scoreRepository.save(score1);
			scoreRepository.save(score2);
			scoreRepository.save(score3);
			scoreRepository.save(score4);
			scoreRepository.save(score5);
			scoreRepository.save(score6);
			scoreRepository.save(score7);*/

		};
	}

	@Bean
	public PasswordEncoder passwordEncoder(){return PasswordEncoderFactories.createDelegatingPasswordEncoder();}

}


@Configuration
class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	PlayerRepository playerRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(inputName-> {
			Player player = playerRepository.findByUserName(inputName);
			if (player!= null) {
				return new User(player.getUserName(), player.getPassword(),
						AuthorityUtils.createAuthorityList("USER"));
			} else {
				throw new UsernameNotFoundException("Unknown user: " + inputName);
			}
		});
	}

}


@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/web/games_3.html").permitAll()
				.antMatchers("/web/**").permitAll()
				.antMatchers("/api/games").permitAll()
				.antMatchers("/api/players").permitAll()
				.antMatchers("/api/game_view/*").hasAuthority("USER")
				.antMatchers("/rest/*").permitAll()//denyAll()
				.anyRequest().permitAll();

		http.formLogin()
				.usernameParameter("username")
				.passwordParameter("password")
				.loginPage("/api/login");

		http.logout().logoutUrl("/api/logout");

		http.csrf().disable();

		// if user is not authenticated, just send an authentication failure response
		http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if login is successful, just clear the flags asking for authentication
		http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

		// if login fails, just send an authentication failure response
		http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

		// if logout is successful, just send a success response
		http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
	}

	private void clearAuthenticationAttributes(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
	}
}








