# Validation Service
version 1.1.1 (9.1.2019)

Build instructions:  
* `mvn clean install`
* `java -jar target/validation-service-1.1-SNAPSHOT-jar-with-dependencies.jar`

Swagger documentation: https://api-docs.ownyourdata.eu/semcon-validation/  


## About Semantic Containers
Semantic Containers enable secure and traceable data exchange between multiple parties.  
more infos and documentation: https://ownyourdata.eu/en/semcon

## Init File Validation Service  

### Instruction:  
The service is available at:  
  - `https://semantic.ownyourdata.eu/api/validate/init`
  - `http://localhost:2806/api/validate/init`

Example usage: `curl -s -o /dev/null -w "%{http_code}" -H "Content-Type: application/json" -d "$(< src/test/resources/init/example-init.trig.json)" -X POST https://semantic.ownyourdata.eu/api/validate/init`  

### Input file format

The input file is of type `application/json` to wrap a TRIG file using JSON key "init-config".  
The TRIG file should:
* follow the template provided in the `test/resources/init/example-init.trig`, 
and the subsequent `test/resources/init/example-init.trig.json` file that wrap up the TRIG file as JSON. 
* and will be tested against the base constraints `main/resources/init/base-constraints.ttl`. 

The output is `200` if the init file conform to the template, or `500` otherwise.


## Usage Policy Matching Service  

### Instruction:  
The service is available at   
  - `https://semantic.ownyourdata.eu/api/validate/usage-policy`
  - `http://localhost:2806/api/validate/usage-policy`

Example usage: `curl -s -o /dev/null -w "%{http_code}" -H "Content-Type: application/json" -d "$(< src/test/resources/usage-policy/template.ttl.json)" -X POST https://semantic.ownyourdata.eu/api/validate/usage-policy`  

### Input file format

The input file is of type `application/json` to wrap a turtle file using JSON key "usage-policy".  
The turtle file should have two classes below:
* `sc:DataSubjectPolicy` that represent usage policy of user, and
* `sc:DataControllerPolicy` that represent usage policy of data controller.
There are four example turtle files in the `test/resources/usage-policy` directory for your references, 
and the subsequent `*.ttl.json` files that wrap up the turtle files as JSON.

The output is `200` if the policy of controllers is not violating the policy of users, or `500` otherwise.


## Data Validation Service  

### Instruction:  
The service is available at   
  - `https://semantic.ownyourdata.eu/api/validate/data`
  - `http://localhost:2806/api/validate/data`

Example usage: `curl -s -o /dev/null -w "%{http_code}" -H "Content-Type: application/json" -d "$(< src/test/resources/data/zamg-data.json)" -X POST https://semantic.ownyourdata.eu/api/validate/data`  

### Input file format

The input file is of type `application/json` to wrap data and constraints (both in turtle format) using JSON keys "content-data" and "content-constraints".  
There are multiple example files in the `test/resources/data` directory for your references, 
and the subsequent `*.ttl.json` files that wrap up the turtle files as JSON.

The output is `200` if the data complies to the given constraints, or `500` otherwise.

## Contact  
Contact `fajar.ekaputra@tuwien.ac.at` for further questions. 
