package pt.feup.industrial.mes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MesSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(MesSystemApplication.class, args);
	}

}
