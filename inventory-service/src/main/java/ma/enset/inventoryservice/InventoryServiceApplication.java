package ma.enset.inventoryservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.checkerframework.checker.interning.qual.InternedDistinct;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.rest.core.config.Projection;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;

@SpringBootApplication
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}
	@Bean
	CommandLineRunner start(ProductRepository productRepository, RepositoryRestConfiguration restConfiguration){
		return args ->{
			restConfiguration.exposeIdsFor(Product.class);
			productRepository.save(new Product(null,"computer",680,56));
			productRepository.save(new Product(null,"mouse",50,80));
			productRepository.save(new Product(null,"keyboard",50,60));
			productRepository.findAll().forEach(p->{
				System.out.println(p.getName());
			});

		};
	}
}

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @ToString
class Product{
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private double price;
	private double quantity;

}
//@RepositoryRestResource
//interface ProductRepository extends JpaRepository<Product,Long> { }


@RepositoryRestResource
interface ProductRepository extends JpaRepository<Product, Long>{
    @RestResource(path = "/byName")
    public List<Product> findByNameContains(@Param("kw") String name);
}
@Projection(name = "mobile", types = Product.class)
interface ProductProjection{
    public String getName();
    public double getPrice();

}

@Projection(name = "web", types = Product.class)
interface ProductProjection2{
    public String getName();
    public double getPrice();
    public double getQuantity();

}
