package ma.enset.customerservice;

import ma.enset.customerservice.entities.Customer;
import ma.enset.customerservice.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.util.SystemPropertyUtils;
import rx.Producer;

@SpringBootApplication
public class CustomerServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomerServiceApplication.class, args);
	}



@Bean
CommandLineRunner start(CustomerRepository customerRepository,
						RepositoryRestConfiguration restConfiguration){
	return args ->{
		restConfiguration.exposeIdsFor(Customer.class);
		customerRepository.save(new Customer(null, "enset", "enset@gmail.com"));
		customerRepository.save(new Customer(null, "karim", "karim@gmail.com"));
		customerRepository.save(new Customer(null, "hamid", "hamid@gmail.com"));
		customerRepository.findAll().forEach(c->{
			System.out.println(c.toString());
		});
	};
}}