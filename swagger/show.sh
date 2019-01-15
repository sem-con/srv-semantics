#!/bin/bash

docker run -it --rm -e SWAGGER_JSON=/local/swagger.json -p 8080:8080 -v $PWD:/local swaggerapi/swagger-ui