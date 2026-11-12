package pe.edu.unsch.pnihce.mpi;

import org.springframework.boot.SpringApplication;

public class TestMpiApplication {

	public static void main(String[] args) {
		SpringApplication.from(MpiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
