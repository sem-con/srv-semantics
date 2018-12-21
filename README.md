# Validation Service
version 1.1.0 (17.12.2018)
* `mvn clean install`
* `java -jar target/validation-service-1.1-SNAPSHOT-jar-with-dependencies.jar`

## Usage Policy Matching Service  

### Instruction:  
The service is available at   
  - `http://localhost:2806/api/validate/usage-policy`
  
### How the input file look like?

The input file would be of type `application/json` to wrap a turtle file using JSON key "usage-policy".  
The turtle file should have two classes below:
* `sc:DataSubjectPolicy` that represent usage policy of user, and
* `sc:DataControllerPolicy` that represent usage policy of data controller.
We provided four example turtle files in the `test/resources/usage-policy` for your references, 
and the subsequent `*.ttl.json` files that wrap up the turtle files as JSON.

The output would be `200` if the policy of controllers is not violating the policy of users, or `500` otherwise.

Contact `fajar.ekaputra@tuwien.ac.at` for further questions. 

## Init File Validation Service  

### Instruction:  
The service is available at 
  - `http://localhost:2806/api/validate/init`
  
### How the input file look like?

The input file would be of type application/json` to wrap a TRIG file using JSON key "init-config".  
The TRIG file should:
* follow the template provided in the `test/resources/init/example-init.trig`, 
and the subsequent `test/resources/init/example-init.trig.json` file that wrap up the TRIG file as JSON. 
* and will be tested against the base constraints `main/resources/init/base-constraints.ttl`. 

The output would be `200` if the init file conform to the template, or `500` otherwise.

Contact `fajar.ekaputra@tuwien.ac.at` for further questions. 

