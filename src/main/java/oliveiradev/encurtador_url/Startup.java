package oliveiradev.encurtador_url;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "oliveiradev.encurtador_url.application",
    "oliveiradev.encurtador_url.domain",
    "oliveiradev.encurtador_url.infrastructure",
    "oliveiradev.encurtador_url.interfaces",
    "oliveiradev.encurtador_url.infra"
})
public class Startup {

	public static void main(String[] args) {
		SpringApplication.run(Startup.class, args);
	}

}
