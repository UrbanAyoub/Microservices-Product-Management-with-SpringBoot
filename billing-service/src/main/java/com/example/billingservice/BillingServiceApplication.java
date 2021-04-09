package com.example.billingservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.config.Projection;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Data @AllArgsConstructor @NoArgsConstructor
class Bill{
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private Date billingDate;
	//@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Long customerID;
	@Transient
	private Customer customer;
	@OneToMany(mappedBy = "bill")
	private Collection<ProductItem> productItems;
}
@RepositoryRestResource
interface BillRepository extends JpaRepository<Bill, Long>{

}
@Projection(name = "fullBill", types = Bill.class)
interface BillProjection{
	public Long getId();
	public Date getBillingDate();
	public Long getCustomerID();
	public Collection<ProductItem> getProductItems();

}
@Entity
@Data @AllArgsConstructor @NoArgsConstructor
class ProductItem{
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
//	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Long productID;
	@Transient
	private Product product;
	private double price;
	private double quantity;
	@ManyToOne

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Bill bill;
}
@RepositoryRestResource
interface ProductItemRepository extends JpaRepository<ProductItem, Long>{

	List<ProductItem> findByBillId(Long billID);
}

@Data
class Customer{
	private Long id;
	private String name;
	private String email;

}

@FeignClient(name = "CUSTOMER-SERVICE")
interface CustomerService{
	@GetMapping("/customers/{id}?projection=fullCustomer")
	public Customer findCustomerById(@PathVariable(name="id") Long id);
}
@Data
class Product{
	private Long id;
	private String name;
	private double price;

}
@FeignClient(name = "INVENTORY-SERVICE")
interface InventoryService{
	@GetMapping("/products/{id}")
	public Product findProductById(@PathVariable(name="id") Long id);
	@GetMapping("/products/")
	public PagedModel<Product> findAllProducts();
}

@SpringBootApplication
@EnableFeignClients
public class BillingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillingServiceApplication.class, args);
	}
	@Bean
	CommandLineRunner start(BillRepository billRepository,
							ProductItemRepository productItemRepository,
							CustomerService customerService,
							InventoryService inventoryService){


		return args -> {
			Customer c1 = customerService.findCustomerById(1L);
			System.out.println("**********************");
			System.out.println("ID="+c1.getId());
			System.out.println("Name="+c1.getName());
			System.out.println("Email="+c1.getEmail());
			System.out.println("**********************");

			Bill bill1 = billRepository.save(new Bill(null, new Date(), c1.getId(),null, null));

			PagedModel<Product> products =inventoryService.findAllProducts();
			products.getContent().forEach(p->{
			productItemRepository.save(new ProductItem(null,p.getId(),null,p.getPrice(),30,bill1));

			});

		/*	Product p1 =inventoryService.findProductById(1L);
			System.out.println("**********************");
			System.out.println("ProductID="+p1.getId());
			System.out.println("Product Name="+p1.getName());
			System.out.println("Price="+p1.getPrice());
			System.out.println("**********************");

			productItemRepository.save(new ProductItem(null,p1.getId(),p1.getPrice(),30,bill1));

			Product p2 =inventoryService.findProductById(2L);
			productItemRepository.save(new ProductItem(null,p2.getId(),p2.getPrice(),30,bill1));

			Product p3 =inventoryService.findProductById(3L);
			productItemRepository.save(new ProductItem(null,p3.getId(),p3.getPrice(),30,bill1));
*/
		};
	}
}
@RestController
class BillRestController{
	private BillRepository billRepository;
	private ProductItemRepository productItemRepository;
	private CustomerService customerService;
	private InventoryService inventoryService;

	BillRestController(BillRepository billRepository, ProductItemRepository productItemRepository,
					   CustomerService customerService, InventoryService inventoryService) {
		this.billRepository = billRepository;
		this.productItemRepository = productItemRepository;
		this.customerService = customerService;
		this.inventoryService = inventoryService;
	}

	@GetMapping(value= "/fullBill/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
	public Bill getBill(@PathVariable(name = "id") Long id){
		Bill bill= billRepository.findById(id).get();
		bill.setCustomer(customerService.findCustomerById(bill.getCustomerID()));
	//	bill.setProductItems(productItemRepository.findByBillId(id));
		bill.getProductItems().forEach(pi->{
			pi.setProduct(inventoryService.findProductById(pi.getProductID()));
		});
		return bill;
	}
}