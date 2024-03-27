About the fork
==============

currently (03/2024) all java based code for consul is not maintained. both Ecwid and consul-client
libraries linked via consul site https://developer.hashicorp.com/consul/api-docs/libraries-and-sdks are
not maintained anymore 😒

**because the original repository is not maintained anymore, no reaction to fixes (both security and updates on consul)
I can not guarantee that changes can be merged back into the original code.
this will properly never happen because the current state of the repositories.
I still appreciate the hard work done by all contributors on the original ecwid repository**

- my main focus is spring boot 2.7,  so we maintain this in light of this usage
- this fork has merged fix https://github.com/Ecwid/consul-api/issues/186 from ecwid
- this fork updates dependencies 
- this fork test against consul 1.17.0 instead of 1.6.0
- build for JDK11, JDK17 (LTS) is planned 
- it starts with version 1.5.0
- use maven as build tool, gradle is a mess (my opinion)

# todo :
- prepare for spring-boot 3.x / JDK 17
- support new ACL , legacy acl is still there but needs to be replaced
- new package groupId, so I can publish it on maven repository. For now only source (?)
- replace embedded-consul test with testcontainer based test as this project is also deprecated 😒

 
consul-api
==========

Java client for Consul HTTP API (http://consul.io)

Supports all API endpoints (http://www.consul.io/docs/agent/http.html), all consistency modes and parameters (tags, datacenters etc.)

## How to use
```java
ConsulClient client = new ConsulClient("localhost");

// set KV
byte[] binaryData = new byte[] {1,2,3,4,5,6,7};
client.setKVBinaryValue("someKey", binaryData);

client.setKVValue("com.my.app.foo", "foo");
client.setKVValue("com.my.app.bar", "bar");
client.setKVValue("com.your.app.foo", "hello");
client.setKVValue("com.your.app.bar", "world");

// get single KV for key
Response<GetValue> keyValueResponse = client.getKVValue("com.my.app.foo");
System.out.println(keyValueResponse.getValue().getKey() + ": " + keyValueResponse.getValue().getDecodedValue()); // prints "com.my.app.foo: foo"

// get list of KVs for key prefix (recursive)
Response<List<GetValue>> keyValuesResponse = client.getKVValues("com.my");
keyValuesResponse.getValue().forEach(value -> System.out.println(value.getKey() + ": " + value.getDecodedValue())); // prints "com.my.app.foo: foo" and "com.my.app.bar: bar"

//list known datacenters
Response<List<String>> response = client.getCatalogDatacenters();
System.out.println("Datacenters: " + response.getValue());

// register new service
NewService newService = new NewService();
newService.setId("myapp_01");
newService.setName("myapp");
newService.setTags(Arrays.asList("EU-West", "EU-East"));
newService.setPort(8080);
client.agentServiceRegister(newService);

// register new service with associated health check
NewService newService = new NewService();
newService.setId("myapp_02");
newService.setTags(Collections.singletonList("EU-East"));
newService.setName("myapp");
newService.setPort(8080);

NewService.Check serviceCheck = new NewService.Check();
serviceCheck.setScript("/usr/bin/some-check-script");
serviceCheck.setInterval("10s");
newService.setCheck(serviceCheck);

client.agentServiceRegister(newService);

// query for healthy services based on name (returns myapp_01 and myapp_02 if healthy)
HealthServicesRequest request = HealthServicesRequest.newBuilder()
					.setPassing(true)
					.setQueryParams(QueryParams.DEFAULT)
					.build();
Response<List<HealthService>> healthyServices = client.getHealthServices("myapp", request);

// query for healthy services based on name and tag (returns myapp_01 if healthy)
HealthServicesRequest request = HealthServicesRequest.newBuilder()
					.setTag("EU-West")
					.setPassing(true)
					.setQueryParams(QueryParams.DEFAULT)
					.build();
Response<List<HealthService>> healthyServices = client.getHealthServices("myapp", request);
```

## How to add consul-api into your project
**currently this is source only , and you have to build/upload it to you own repository our use local copy !**
### Gradle
```
compile "com.ecwid.consul:consul-api:1.5.0"
```
### Maven
```
<dependency>
  <groupId>com.ecwid.consul</groupId>
  <artifactId>consul-api</artifactId>
  <version>1.5.0</version>
</dependency>
```

## How to build from sources
* maven 3.8 or better 
* mvn clean install

