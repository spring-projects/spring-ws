This is where the master build to create releases of spring-ws resides.

To build a new release:

1. Update project.properties to contain the new release version, if necessary.

2. From this directory, run:
		ant release
   The release archive will created and placed in:
		target/release
	
Questions? See forum.springframework.org