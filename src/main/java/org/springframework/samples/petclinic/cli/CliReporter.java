package org.springframework.samples.petclinic.cli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;

import java.util.Collection;

@ComponentScan(basePackages = "org.springframework.samples.petclinic")
public class CliReporter implements CommandLineRunner {

	private final VetRepository vetRepository;

	// Use Spring's dependency injection to get the repository.
	public CliReporter(VetRepository vetRepository) {
		this.vetRepository = vetRepository;
	}

	public static void main(String[] args) {
		SpringApplication.run(CliReporter.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("--- Starting Vet Information Report ---");
		generateVetReport();
		System.out.println("--- Report Generation Complete ---");
		System.exit(0); // Exit the application after report generation
	}

	private void generateVetReport() {
		System.out.println("Fetching all veterinarians from the database...");
		Collection<Vet> vets = this.vetRepository.findAll();

		System.out.println("\n--- VETERINARIAN ROSTER ---");
		for (Vet vet : vets) {
			System.out.printf("Vet: %s, %s | Specialties: ", vet.getLastName(), vet.getFirstName());
			if (vet.getSpecialties().isEmpty()) {
				System.out.print("None\n");
			} else {
				vet.getSpecialties().forEach(specialty -> System.out.printf("%s ", specialty.getName()));
				System.out.print("\n");
			}
		}
	}
}
