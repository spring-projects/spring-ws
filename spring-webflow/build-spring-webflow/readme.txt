This is where the master build that creates releases of Spring Web Flow resides.

- To build all Spring Web Flow related projects:

	1. From this directory, run:
		ant dist
        
- To build a new Spring Web Flow product release:

  1. Update project.properties to reflect the new release version, if necessary.

  2. From this directory, run:
		ant release
		
     The release archive will created and placed in:
     	target/release
	
Questions? See forum.springframework.org