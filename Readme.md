## order-service

## Others
- we use mvn clean install to build and install the jar file to the local maven repository
- use depgraph-maven-plugin to generate the dependency graph: mvn com.github.ferstl:depgraph-maven-plugin:aggregate -DcreateImage=true -DreduceEdges=false -Dscope=compile "-Dincludes=com.spring.food.ordering.system:*"

## common
- common-domain will have entity, value object, exception that can be used across different services
- value object is immutable, thats why we use final keyword
