package com.crudexample.democrud;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.crudexample.democrud.model.Exp;
import com.crudexample.democrud.model.User;
import com.crudexample.democrud.repo.UserRepository;

@SpringBootApplication
public class DemocrudApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemocrudApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner loadData(UserRepository repository) {
		return (args) -> {
			// save a couple of customers		
			repository.save(new User("ibrahim", "percin","ipercin@me.com",Exp.One,LocalDate.of(1994, 10, 24)));
			repository.save(new User("ibrahim", "lastname","A@A.com"));
			repository.save(new User("ibrahim", "lastname2","C@C.d",Exp.Three,LocalDate.of(1994, 10, 24)));
			repository.save(new User("ibrahim", "lastname3","D@D.E",Exp.Two,LocalDate.of(1994, 10, 24)));
			repository.save(new User("ibrahim", "lastname4","E@E.c",Exp.Four,LocalDate.of(1994, 10, 24)));
			repository.save(new User("ibrahim", "percin","ipercin@me.com",Exp.One,LocalDate.of(1994, 10, 24)));
			repository.save(new User("ibrahim", "lastname","A@A.com"));
			repository.save(new User("ibrahim", "lastname2","C@C.d",Exp.Three,LocalDate.of(1994, 10, 24)));
			repository.save(new User("ibrahim", "lastname3","D@D.E",Exp.Two,LocalDate.of(1994, 10, 24)));
			repository.save(new User("ibrahim", "lastname4","E@E.c",Exp.Four,LocalDate.of(1994, 10, 24)));
			repository.save(new User("ibrahim", "percin","ipercin@me.com",Exp.One,LocalDate.of(1994, 10, 24)));
			repository.save(new User("ibrahim", "lastname","A@A.com"));
			repository.save(new User("ibrahim", "lastname2","C@C.d",Exp.Three,LocalDate.of(1994, 10, 24)));
			repository.save(new User("ibrahim", "lastname3","D@D.E",Exp.Two,LocalDate.of(1994, 10, 24)));
			repository.save(new User("ibrahim", "lastname4","E@E.c",Exp.Four,LocalDate.of(1994, 10, 24)));
		};
	}
}
